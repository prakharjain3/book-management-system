package com.sismics.books.rest.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sismics.books.core.dao.jpa.BookDao;
import com.sismics.books.core.dao.jpa.GenreDao;
import com.sismics.books.core.dao.jpa.TagDao;
import com.sismics.books.core.dao.jpa.UserBookDao;
import com.sismics.books.core.dao.jpa.UserDao;
import com.sismics.books.core.dao.jpa.criteria.UserBookCriteria;
import com.sismics.books.core.dao.jpa.dto.TagDto;
import com.sismics.books.core.dao.jpa.dto.UserBookDto;
import com.sismics.books.core.event.BookImportedEvent;
import com.sismics.books.core.model.context.AppContext;
import com.sismics.books.core.model.jpa.Book;
import com.sismics.books.core.model.jpa.Genre;
import com.sismics.books.core.model.jpa.Tag;
import com.sismics.books.core.model.jpa.User;
import com.sismics.books.core.model.jpa.UserBook;
import com.sismics.books.core.util.DirectoryUtil;
import com.sismics.books.core.util.jpa.PaginatedList;
import com.sismics.books.core.util.jpa.PaginatedLists;
import com.sismics.books.core.util.jpa.SortCriteria;
import com.sismics.books.core.constant.BookDetailsConstants;
import com.sismics.books.core.constant.BookErrorConstants;
import com.sismics.books.core.constant.TagsErrorMessages;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

/**
 * Book REST resources.
 * 
 * @author bgamard
 */
@Path("/book")
public class BookResource extends AuthenticatedResource implements BookCoverable {
    /**
     * Creates a new book.
     * 
     * @param isbn ISBN Number
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response add(
            @FormParam("isbn") String isbn) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        ValidationUtil.validateRequired(isbn, "isbn");
        
        // Fetch the book
        BookDao bookDao = new BookDao();
        Book book = bookDao.getByIsbn(isbn);
        if (book == null) {
            // Try to get the book from a public API
            try {
                book = AppContext.getInstance().getBookDataService().searchBook(isbn);
            } catch (Exception e) {
                throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, e.getCause().getMessage(), e);
            }
            
            // Save the new book in database
            bookDao.create(book);
        }
        
        // Create the user book if needed
        UserBook userBook = getUserBook(book);

        JSONObject response = new JSONObject();
        response.put("id", userBook.getId());
        return Response.ok().entity(response).build();
    }

    private UserBook getUserBook(Book book) throws JSONException {
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getByBook(book.getId(), principal.getId());
        if (userBook == null) {
            userBook = new UserBook();
            userBook.setUserId(principal.getId());
            userBook.setBookId(book.getId());
            userBook.setCreateDate(new Date());
            userBook.setType("book");
            userBookDao.create(userBook);
        } else {
            throw new ClientException(BookErrorConstants.BOOK_ALREADY_ADDED, "Book already added");
        }
        return userBook;
    }

    /**
     * Deletes a book.
     * 
     * @param userBookId User book ID
     * @return Response
     * @throws JSONException
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("id") String userBookId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId, principal.getId());
        if (userBook == null) {
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "Book not found with id " + userBookId);
        }
        
        // Delete the user book
        userBookDao.delete(userBook.getId());
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
    
    /**
     * Add a book manually.
     * 
     * @param title Title
     * @param description Description
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Path("manual")
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(
            @FormParam("title") String title,
            @FormParam("subtitle") String subtitle,
            @FormParam("author") String author,
            @FormParam("description") String description,
            @FormParam("isbn10") String isbn10,
            @FormParam("isbn13") String isbn13,
            @FormParam("page_count") Long pageCount,
            @FormParam("language") String language,
            @FormParam("publish_date") String publishDateStr,
            @FormParam("tags") List<String> tagList) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        title = ValidationUtil.validateLength(title, BookDetailsConstants.TITLE, 1, 255, false);
        subtitle = ValidationUtil.validateLength(subtitle, BookDetailsConstants.SUBTITLE, 1, 255, true);
        author = ValidationUtil.validateLength(author, BookDetailsConstants.AUTHOR, 1, 255, false);
        description = ValidationUtil.validateLength(description, BookDetailsConstants.DESCRIPTION, 1, 4000, true);
        isbn10 = ValidationUtil.validateLength(isbn10, BookDetailsConstants.ISBN10, 10, 10, true);
        isbn13 = ValidationUtil.validateLength(isbn13, BookDetailsConstants.ISBN13, 13, 13, true);
        language = ValidationUtil.validateLength(language, BookDetailsConstants.LANGUAGE, 2, 2, true);
        Date publishDate = ValidationUtil.validateDate(publishDateStr, BookDetailsConstants.PUBLISH_DATE, false);
        
        if (Strings.isNullOrEmpty(isbn10) && Strings.isNullOrEmpty(isbn13)) {
            throw new ClientException("ValidationError", "At least one ISBN number is mandatory");
        }
        
        // Check if this book is not already in database
        BookDao bookDao = new BookDao();
        Book bookIsbn10 = bookDao.getByIsbn(isbn10);
        Book bookIsbn13 = bookDao.getByIsbn(isbn13);
        if (bookIsbn10 != null || bookIsbn13 != null) {
            throw new ClientException(BookErrorConstants.BOOK_ALREADY_ADDED, "Book already added");
        }
        
        // Create the book
        Book book = new Book();
        book.setId(UUID.randomUUID().toString());

        createBookFromNonNull(title, subtitle, author, description, isbn10, isbn13, pageCount, language, publishDate, book);

        bookDao.create(book);
        
        // Create the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = new UserBook();
        userBook.setUserId(principal.getId());
        userBook.setBookId(book.getId());
        userBook.setCreateDate(new Date());
        userBook.setType("book");
        userBookDao.create(userBook);
        
        // Update tags
        if (tagList != null) {
            TagDao tagDao = new TagDao();
            Set<String> tagSet = new HashSet<>();
            Set<String> tagIdSet = new HashSet<>();
            List<Tag> tagDbList = tagDao.getByUserId(principal.getId());
            for (Tag tagDb : tagDbList) {
                tagIdSet.add(tagDb.getId());
            }
            for (String tagId : tagList) {
                if (!tagIdSet.contains(tagId)) {
                    throw new ClientException(TagsErrorMessages.TAG_NOT_FOUND, MessageFormat.format("Tag not found: {0}", tagId));
                }
                tagSet.add(tagId);
            }
            tagDao.updateTagList(userBook.getId(), tagSet);
        }
        
        // Returns the book ID
        JSONObject response = new JSONObject();
        response.put("id", userBook.getId());
        return Response.ok().entity(response).build();
    }
    
    /**
     * Updates the book.
     * 
     * @param title Title
     * @param description Description
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("id") String userBookId,
            @FormParam("title") String title,
            @FormParam("subtitle") String subtitle,
            @FormParam("author") String author,
            @FormParam("description") String description,
            @FormParam("isbn10") String isbn10,
            @FormParam("isbn13") String isbn13,
            @FormParam("page_count") Long pageCount,
            @FormParam("language") String language,
            @FormParam("publish_date") String publishDateStr,
            @FormParam("tags") List<String> tagList) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        title = ValidationUtil.validateLength(title, BookDetailsConstants.TITLE, 1, 255, true);
        subtitle = ValidationUtil.validateLength(subtitle, BookDetailsConstants.SUBTITLE, 1, 255, true);
        author = ValidationUtil.validateLength(author, BookDetailsConstants.AUTHOR, 1, 255, true);
        description = ValidationUtil.validateLength(description, BookDetailsConstants.DESCRIPTION, 1, 4000, true);
        isbn10 = ValidationUtil.validateLength(isbn10, BookDetailsConstants.ISBN10, 10, 10, true);
        isbn13 = ValidationUtil.validateLength(isbn13, BookDetailsConstants.ISBN13, 13, 13, true);
        language = ValidationUtil.validateLength(language, BookDetailsConstants.LANGUAGE, 2, 2, true);
        Date publishDate = ValidationUtil.validateDate(publishDateStr, BookDetailsConstants.PUBLISH_DATE, true);
        
        // Get the user book
        Book book = getBookFromUserbookDao(userBookId, isbn10, isbn13);

        // Update the book
        createBookFromNonNull(title, subtitle, author, description, isbn10, isbn13, pageCount, language, publishDate, book);

        // Update tags
        if (tagList != null) {
            TagDao tagDao = new TagDao();
            Set<String> tagSet = new HashSet<>();
            Set<String> tagIdSet = new HashSet<>();
            List<Tag> tagDbList = tagDao.getByUserId(principal.getId());
            for (Tag tagDb : tagDbList) {
                tagIdSet.add(tagDb.getId());
            }
            for (String tagId : tagList) {
                if (!tagIdSet.contains(tagId)) {
                    throw new ClientException(TagsErrorMessages.TAG_NOT_FOUND, MessageFormat.format("Tag not found: {0}", tagId));
                }
                tagSet.add(tagId);
            }
            tagDao.updateTagList(userBookId, tagSet);
        }
        
        // Returns the book ID
        JSONObject response = new JSONObject();
        response.put("id", userBookId);
        return Response.ok().entity(response).build();
    }

    private void createBookFromNonNull(@FormParam("title") String title, @FormParam("subtitle") String subtitle, @FormParam("author") String author, @FormParam("description") String description, @FormParam("isbn10") String isbn10, @FormParam("isbn13") String isbn13, @FormParam("page_count") Long pageCount, @FormParam("language") String language, Date publishDate, Book book) {
        if (title != null) {
            book.setTitle(title);
        }
        if (subtitle != null) {
            book.setSubtitle(subtitle);
        }
        if (author != null) {
            book.setAuthor(author);
        }
        if (description != null) {
            book.setDescription(description);
        }
        if (isbn10 != null) {
            book.setIsbn10(isbn10);
        }
        if (isbn13 != null) {
            book.setIsbn13(isbn13);
        }
        if (pageCount != null) {
            book.setPageCount(pageCount);
        }
        if (language != null) {
            book.setLanguage(language);
        }
        if (publishDate != null) {
            book.setPublishDate(publishDate);
        }
    }

    private Book getBookFromUserbookDao(String userBookId, String isbn10, String isbn13) throws JSONException {
        UserBookDao userBookDao = new UserBookDao();
        BookDao bookDao = new BookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId, principal.getId());
        if (userBook == null) {
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "Book not found with id " + userBookId);
        }

        // Get the book
        Book book = bookDao.getById(userBook.getBookId());

        // Check that new ISBN is not already in database
        if (!Strings.isNullOrEmpty(isbn10) && book.getIsbn10() != null && !book.getIsbn10().equals(isbn10)) {
            Book bookIsbn10 = bookDao.getByIsbn(isbn10);
            if (bookIsbn10 != null) {
                throw new ClientException(BookErrorConstants.BOOK_ALREADY_ADDED, "Book already added");
            }
        }

        if (!Strings.isNullOrEmpty(isbn13) && book.getIsbn13() != null && !book.getIsbn13().equals(isbn13)) {
            Book bookIsbn13 = bookDao.getByIsbn(isbn13);
            if (bookIsbn13 != null) {
                throw new ClientException(BookErrorConstants.BOOK_ALREADY_ADDED, "Book already added");
            }
        }
        return book;
    }

    /**
     * Get a book.
     * 
     * @param id User book ID
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response get(
            @PathParam("id") String userBookId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Fetch the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId, principal.getId());
        if (userBook == null) {
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "Book not found with id " + userBookId);
        }
        
        // Fetch the book
        BookDao bookDao = new BookDao();
        Book bookDb = bookDao.getById(userBook.getBookId());

        // Return book data
        JSONObject book = new JSONObject();
        book.put("id", userBook.getId());
        book.put(BookDetailsConstants.TITLE, bookDb.getTitle());
        book.put(BookDetailsConstants.SUBTITLE, bookDb.getSubtitle());
        book.put(BookDetailsConstants.AUTHOR, bookDb.getAuthor());
        if (bookDb.getPageCount() != null) {
            book.put("page_count", bookDb.getPageCount());
        }
        book.put(BookDetailsConstants.DESCRIPTION, bookDb.getDescription());
        if (bookDb.getIsbn10() != null) {
            book.put(BookDetailsConstants.ISBN10, bookDb.getIsbn10());
        }
        if (bookDb.getIsbn13() != null) {
            book.put(BookDetailsConstants.ISBN13, bookDb.getIsbn13());
        }
        book.put(BookDetailsConstants.LANGUAGE, bookDb.getLanguage());
        if (bookDb.getPublishDate() != null) {
            book.put(BookDetailsConstants.PUBLISH_DATE, bookDb.getPublishDate().getTime());
        }
        book.put("create_date", userBook.getCreateDate().getTime());
        if (userBook.getReadDate() != null) {
            book.put("read_date", userBook.getReadDate().getTime());
        }
        book.put("favourite", userBook.getFav());
        book.put("bookId", bookDb.getId());
        // add genres to book
       Set<Genre> genres = bookDb.getGenres();
       List<String> genreNames = new ArrayList<>();
       for (Genre genre : genres) {
           genreNames.add(genre.getName());
       }
       book.put("genres", genreNames);
        
        // Add tags
        TagDao tagDao = new TagDao();
        List<TagDto> tagDtoList = tagDao.getByUserBookId(userBookId);
        List<JSONObject> tags = new ArrayList<>();
        for (TagDto tagDto : tagDtoList) {
            JSONObject tag = new JSONObject();
            tag.put("id", tagDto.getId());
            tag.put("name", tagDto.getName());
            tag.put("color", tagDto.getColor());
            tags.add(tag);
        }
        book.put("tags", tags);
        
        return Response.ok().entity(book).build();
    }
    
    /**
     * Returns a book cover.
     * 
     * @param id User book ID
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}/cover")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response cover(
            @PathParam("id") final String userBookId) throws JSONException {
        // Get the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId);
        
        // Get the cover image
        File file = Paths.get(DirectoryUtil.getBookDirectory().getPath(), userBook.getBookId()).toFile();
        InputStream inputStream = null;
        try {
            if (file.exists()) {
                inputStream = new FileInputStream(file);
            } else {
                inputStream = new FileInputStream(new File(Objects.requireNonNull(getClass().getResource("/dummy.png")).getFile()));
            }
        } catch (FileNotFoundException e) {
            throw new ServerException("FileNotFound", "Cover file not found", e);
        }

        return Response.ok(inputStream)
                .header("Content-Type", "image/jpeg")
                .header("Expires", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date().getTime() + 3600000))
                .build();
    }
    
    /**
     * Updates a book cover.
     * 
     * @param userBookId User book ID
     * @param imageUrl Image URL
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/cover")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response updateCover(
            @PathParam("id") String userBookId,
            @FormParam("url") String imageUrl) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId, principal.getId());
        if (userBook == null) {
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "Book not found with id " + userBookId);
        }
        
        // Get the book
        BookDao bookDao = new BookDao();
        Book book = bookDao.getById(userBook.getBookId());

        // Download the new cover
        try {
            AppContext.getInstance().getBookDataService().downloadThumbnail(book, imageUrl);
        } catch (Exception e) {
            throw new ClientException("DownloadCoverError", "Error downloading the cover image");
        }
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok(response).build();
    }
    
    /**
     * Returns all books.
     * 
     * @param limit Page limit
     * @param offset Page offset
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc,
            @QueryParam("search") String search,
            @QueryParam("read") Boolean read,
            @QueryParam("tag") String tagName,
            @QueryParam("favourite") int favourite) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        JSONObject response = new JSONObject();
        List<JSONObject> books = new ArrayList<>();

        List<JSONObject> audiobooks = new ArrayList<>();

        List<JSONObject> podcasts = new ArrayList<>();
        
        UserBookDao userBookDao = new UserBookDao();
        TagDao tagDao = new TagDao();
        PaginatedList<UserBookDto> paginatedList = PaginatedLists.create(limit, offset);
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        UserBookCriteria criteria = new UserBookCriteria();
        criteria.setSearch(search);
        criteria.setRead(read);
        criteria.setFavourite(favourite);
        criteria.setUserId(principal.getId());
        if (!Strings.isNullOrEmpty(tagName)) {
            Tag tag = tagDao.getByName(principal.getId(), tagName);
            if (tag != null) {
                criteria.setTagIdList(Lists.newArrayList(tag.getId()));
            }
        }
        try {
            userBookDao.findByCriteria(paginatedList, criteria, sortCriteria);
        } catch (Exception e) {
            throw new ServerException("SearchError", "Error searching in books", e);
        }

        for (UserBookDto userBookDto : paginatedList.getResultList()) {
            JSONObject book = new JSONObject();
            book.put("id", userBookDto.getId());
            book.put(BookDetailsConstants.TITLE, userBookDto.getTitle());
            book.put(BookDetailsConstants.SUBTITLE, userBookDto.getSubtitle());
            book.put(BookDetailsConstants.AUTHOR, userBookDto.getAuthor());
            book.put(BookDetailsConstants.LANGUAGE, userBookDto.getLanguage());
            book.put(BookDetailsConstants.PUBLISH_DATE, userBookDto.getPublishTimestamp());
            book.put("create_date", userBookDto.getCreateTimestamp());
            book.put("read_date", userBookDto.getReadTimestamp());
            book.put("bookId", userBookDto.getBookId());
            
            // Get tags
            List<TagDto> tagDtoList = tagDao.getByUserBookId(userBookDto.getId());
            List<JSONObject> tags = new ArrayList<>();
            for (TagDto tagDto : tagDtoList) {
                JSONObject tag = new JSONObject();
                tag.put("id", tagDto.getId());
                tag.put("name", tagDto.getName());
                tag.put("color", tagDto.getColor());
                tags.add(tag);
            }
            book.put("tags", tags);

            if(userBookDto.getType().equals("audiobook")) {
                audiobooks.add(book);
            } else if(userBookDto.getType().equals("book")) {
                books.add(book);
            } else if(userBookDto.getType().equals("podcast")) {
                podcasts.add(book);
            }
            
            // books.add(book);
        }
        response.put("total", paginatedList.getResultCount());
        response.put("books", books);
        response.put("audiobooks", audiobooks);
        response.put("podcasts", podcasts);
        
        return Response.ok().entity(response).build();
    }
    
    /**
     * Set the genres for a book.
     * 
     * @param bookId Book ID
     * Id of the book whose genres is being set
     * @param genres List of strings of genre names to add
     * Genre names must be same as the name initialized in the db.
     * All genre names for the book must be provided.
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("genres/{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setGenres(@PathParam("id") String bookId, @FormParam("genres") List<String> genres) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // check for null bookId
        if (bookId == null) {
            throw new ClientException("NullBookId", "Book ID is required");
        }
        
        // check that the book exists
        BookDao bookDao = new BookDao();
        Book book = bookDao.getById(bookId);
        if (book == null) {
            throw new ClientException("BookDoesNotExist", "Book with that ID does not exist");
        }

        // create a set of genres and check if genre exists
        GenreDao genreDao = new GenreDao();
        Set<Genre> genresSet = new HashSet<>();
        for (String genre : genres) {
            Genre g = genreDao.getByName(genre);
            if (g == null) {
                throw new ClientException("GenreNotFoundError", "Genre not found: " + genre);
            }
            genresSet.add(genreDao.getByName(genre));
        }

        // set the genres
        book.setGenres(genresSet);
        
        return Response.ok().build();
    }

    /**
     * Imports books.
     * 
     * @param fileBodyPart File to import
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Consumes("multipart/form-data") 
    @Path("import")
    public Response importFile(
            @FormDataParam("file") FormDataBodyPart fileBodyPart) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        ValidationUtil.validateRequired(fileBodyPart, "file");

        UserDao userDao = new UserDao();
        User user = userDao.getById(principal.getId());
        
        InputStream in = fileBodyPart.getValueAs(InputStream.class);
        File importFile = null;
        try {
            // Copy the incoming stream content into a temporary file
            importFile = File.createTempFile("books_import", null);
            IOUtils.copy(in, Files.newOutputStream(importFile.toPath()));
            
            BookImportedEvent event = new BookImportedEvent();
            event.setUser(user);
            event.setImportFile(importFile);
            AppContext.getInstance().getImportEventBus().post(event);
            
            // Always return ok
            JSONObject response = new JSONObject();
            response.put("status", "ok");
            return Response.ok().entity(response).build();
        } catch (Exception e) {
            if (importFile != null) {
                try {
                    boolean deleted = importFile.delete();
                    if (!deleted) {
                        throw new ServerException("DeleteError", "Could not delete file");
                    }
                } catch (SecurityException e2) {
                    // NOP
                }
            }
            throw new ServerException("ImportError", "Error importing books", e);
        }
    }
    
    /**
     * Set a book as read/unread.
     * 
     * @param userBookIdj User book ID
     * @param read Read state
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/read")
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(
            @PathParam("id") final String userBookId,
            @FormParam("read") boolean read) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId, principal.getId());
        
        // Update the read date
        userBook.setReadDate(read ? new Date() : null);
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    @POST
    @Path("{id: [a-z0-9\\-]+}/favourite")
    @Produces(MediaType.APPLICATION_JSON)
    public Response favourite(
            @PathParam("id") final String userBookId,
            @FormParam("favourite") int favourite) throws Exception {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        if (favourite != 0 && favourite != 1) {
            throw new ClientException("ValidationError", "Favourite must be 0 or 1");
        }

        // Get the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId, principal.getId());

        // Update the favourite
        userBook.setFav(favourite);

        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
