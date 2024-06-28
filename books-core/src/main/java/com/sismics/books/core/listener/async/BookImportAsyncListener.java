package com.sismics.books.core.listener.async;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.sismics.books.core.dao.jpa.BookDao;
import com.sismics.books.core.dao.jpa.TagDao;
import com.sismics.books.core.dao.jpa.UserBookDao;
import com.sismics.books.core.dao.jpa.dto.TagDto;
import com.sismics.books.core.event.BookImportedEvent;
import com.sismics.books.core.model.context.AppContext;
import com.sismics.books.core.model.jpa.Book;
import com.sismics.books.core.model.jpa.Tag;
import com.sismics.books.core.model.jpa.UserBook;
import com.sismics.books.core.util.TransactionUtil;
import com.sismics.books.core.util.math.MathUtil;

public class BookImportAsyncListener {
    private static final Logger log = LoggerFactory.getLogger(BookImportAsyncListener.class);
    private DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd");

    @Subscribe
    public void on(final BookImportedEvent bookImportedEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Books import requested event: {0}", bookImportedEvent.toString()));
        }

        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                BookImportAsyncListener.this.processImportFile(bookImportedEvent);
            }
        });
    }

    private void processImportFile(BookImportedEvent event) {
        try (CSVReader reader = new CSVReader(new FileReader(event.getImportFile()))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (!"Book Id".equals(line[0])) { // Skip header
                    processBookEntry(line, event);
                }
            }
            TransactionUtil.commit();
        } catch (FileNotFoundException e) {
            log.error("Unable to read CSV file", e);
        } catch (Exception e) {
            log.error("Error processing import file", e);
        }
    }

    private void processBookEntry(String[] line, BookImportedEvent event) {
        String isbn = resolveIsbn(line);
        if (Strings.isNullOrEmpty(isbn)) {
            log.warn("No ISBN number for Goodreads book ID: " + line[0]);
            return;
        }

        Book book = fetchOrCreateBook(isbn);
        if (book == null) return; // Skip if book couldn't be fetched or created

        UserBook userBook = fetchOrCreateUserBook(book, line, event);
        Set<String> tagIds = fetchOrCreateTags(line[16], event);
        updateBookTags(userBook, tagIds);
    }

    private String resolveIsbn(String[] line) {
        return Strings.isNullOrEmpty(line[6]) ? line[5] : line[6];
    }

    private Book fetchOrCreateBook(String isbn) {
        BookDao bookDao = new BookDao();
        Book book = bookDao.getByIsbn(isbn);
        if (book == null) {
            try {
                book = AppContext.getInstance().getBookDataService().searchBook(isbn);
                bookDao.create(book);
            } catch (Exception e) {
                log.error("Failed to fetch or create book for ISBN: " + isbn, e);
                return null;
            }
        }
        return book;
    }

    private UserBook fetchOrCreateUserBook(Book book, String[] line, BookImportedEvent event) {
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getByBook(book.getId(), event.getUser().getId());
        if (userBook == null) {
            userBook = new UserBook();
            userBook.setUserId(event.getUser().getId());
            userBook.setBookId(book.getId());
            userBook.setCreateDate(new Date());
            userBook.setReadDate(parseDate(line[14]));
            userBook.setCreateDate(parseDate(line[15]));
            userBookDao.create(userBook);
        }
        return userBook;
    }

    private Date parseDate(String dateString) {
        if (!Strings.isNullOrEmpty(dateString)) {
            return formatter.parseDateTime(dateString).toDate();
        }
        return null;
    }

    private Set<String> fetchOrCreateTags(String bookshelfList, BookImportedEvent event) {
        TagDao tagDao = new TagDao();
        Set<String> tagIds = new HashSet<>();
        String[] bookshelfArray = bookshelfList.split(",");
        for (String bookshelf : bookshelfArray) {
            bookshelf = bookshelf.trim();
            if (!Strings.isNullOrEmpty(bookshelf)) {
                Tag tag = tagDao.getByName(event.getUser().getId(), bookshelf);
                if (tag == null) {
                    tag = new Tag();
                    tag.setName(bookshelf);
                    tag.setColor(MathUtil.randomHexColor());
                    tag.setUserId(event.getUser().getId());
                    tagDao.create(tag);
                }
                tagIds.add(tag.getId());
            }
        }
        return tagIds;
    }

    private void updateBookTags(UserBook userBook, Set<String> tagIds) {
        TagDao tagDao = new TagDao();
        if (!tagIds.isEmpty()) {
            // Optionally merge existing tag IDs
            List<TagDto> tagDtoList = tagDao.getByUserBookId(userBook.getId());
            for (TagDto tag : tagDtoList) {
                tagIds.add(tag.getId());
            }
            tagDao.updateTagList(userBook.getId(), tagIds);
        }
    }
}

