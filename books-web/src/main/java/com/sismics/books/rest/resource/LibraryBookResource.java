package com.sismics.books.rest.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sismics.books.core.dao.jpa.*;
import com.sismics.books.core.dao.jpa.criteria.LibraryBookCriteria;
import com.sismics.books.core.dao.jpa.dto.LibraryBookDto;
import com.sismics.books.core.model.jpa.*;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.books.core.model.context.AppContext;
import com.sismics.books.core.util.DirectoryUtil;
import com.sismics.books.core.util.jpa.PaginatedList;
import com.sismics.books.core.util.jpa.PaginatedLists;
import com.sismics.books.core.util.jpa.SortCriteria;
import com.sismics.books.core.constant.BookDetailsConstants;
import com.sismics.books.core.constant.BookErrorConstants;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;

@Path("/library")
public class LibraryBookResource extends AuthenticatedResource implements BookCoverable {
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc,
            @QueryParam("search") String search
            // @QueryParam("min_rating") Integer minRating,
            // @QueryParam("genres") List<String> genres,
            // @QueryParam("authors") List<String> authors

    // @QueryParam("read") Boolean read
    ) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        JSONObject response = new JSONObject();
        List<JSONObject> books = new ArrayList<>();

        LibraryBookDao libraryBookDao = new LibraryBookDao();
        PaginatedList<LibraryBookDto> paginatedList = PaginatedLists.create(limit, offset);

        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        LibraryBookCriteria criteria = new LibraryBookCriteria();
        
        criteria.setSearch(search);
        criteria.setAuthorNames(null);
        criteria.setGenreList(null);
        criteria.setMinRating(0);

        try {
            // libraryBookDao.getAllBooks(paginatedList);
            libraryBookDao.findByCriteria(paginatedList, criteria, sortCriteria);
        } catch (Exception e) {
            throw new ServerException("SearchError", "Error searching in books", e);
        }

        for (LibraryBookDto libraryBookDto : paginatedList.getResultList()) {
            JSONObject book = getJsonObject(libraryBookDto);

            books.add(book);
        }

        response.put("total", paginatedList.getResultCount());
        response.put("books", books);

        return Response.ok().entity(response).build();
    }

    private static JSONObject getJsonObject(LibraryBookDto libraryBookDto) throws JSONException {
        JSONObject book = new JSONObject();
        book.put("id", libraryBookDto.getId());
        book.put("bookId", libraryBookDto.getBookId());
        book.put(BookDetailsConstants.TITLE, libraryBookDto.getTitle());
        book.put(BookDetailsConstants.SUBTITLE, libraryBookDto.getSubtitle());
        book.put(BookDetailsConstants.AUTHOR, libraryBookDto.getAuthor());
        book.put(BookDetailsConstants.LANGUAGE, libraryBookDto.getLanguage());
        book.put(BookDetailsConstants.PUBLISH_DATE, libraryBookDto.getPublishTimestamp());
        book.put(BookDetailsConstants.NUM_RATINGS, libraryBookDto.getNumRatings());
        book.put(BookDetailsConstants.AVG_RATING, libraryBookDto.getAvgRating());

        book.put("create_date", libraryBookDto.getCreateTimestamp());
        Set<Genre> genresOfABook = libraryBookDto.getGenres();
        List<String> genreNames = new ArrayList<>();
        for (Genre genre : genresOfABook) {
            genreNames.add(genre.getName());
        }
        book.put("genres", genreNames);
        return book;
    }


    @GET
    @Path("rating")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBooksBasedOnRating(
        @QueryParam("limit") Integer limit,
        @QueryParam("offset") Integer offset,
        @QueryParam("sort_column") Integer sortColumn,
        @QueryParam("asc") Boolean asc,
        @QueryParam("min_rating") Integer minRating
    )throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        JSONObject response = new JSONObject();
        List<JSONObject> books = new ArrayList<>();

        LibraryBookDao libraryBookDao = new LibraryBookDao();
        PaginatedList<LibraryBookDto> paginatedList = PaginatedLists.create(limit, offset);

        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        LibraryBookCriteria criteria = new LibraryBookCriteria();
        

        criteria.setSearch(null);
        criteria.setAuthorNames(null);
        criteria.setGenreList(null);
        criteria.setMinRating(minRating);

        try {
            libraryBookDao.findByCriteria(paginatedList, criteria, sortCriteria);
        } catch (Exception e) {
            throw new ServerException("SearchError", "Error searching in books", e);
        }

        for (LibraryBookDto libraryBookDto : paginatedList.getResultList()) {
            JSONObject book = getJsonObject(libraryBookDto);

            books.add(book);
        }

        response.put("total", paginatedList.getResultCount());
        response.put("books", books);

        return Response.ok().entity(response).build();
    }
          
    @GET
    @Path("genres")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBooksBasedOnGenres(
        @QueryParam("limit") Integer limit,
        @QueryParam("offset") Integer offset,
        @QueryParam("sort_column") Integer sortColumn,
        @QueryParam("asc") Boolean asc,
        @QueryParam("genres") List<String> genres
    )throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        JSONObject response = new JSONObject();
        List<JSONObject> books = new ArrayList<>();

        LibraryBookDao libraryBookDao = new LibraryBookDao();
        PaginatedList<LibraryBookDto> paginatedList = PaginatedLists.create(limit, offset);

        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        LibraryBookCriteria criteria = new LibraryBookCriteria();
        

        criteria.setSearch(null);
        criteria.setAuthorNames(null);
        criteria.setGenreList(genres);
        criteria.setMinRating(0);

        try {
            libraryBookDao.findByCriteria(paginatedList, criteria, sortCriteria);
        } catch (Exception e) {
            throw new ServerException("SearchError", "Error searching in books", e);
        }

        for (LibraryBookDto libraryBookDto : paginatedList.getResultList()) {
            JSONObject book = getJsonObject(libraryBookDto);

            books.add(book);
        }

        response.put("total", paginatedList.getResultCount());
        response.put("books", books);

        return Response.ok().entity(response).build();
    }

          
    @GET
    @Path("filter_authors")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBooksBasedOnAuthors(
        @QueryParam("limit") Integer limit,
        @QueryParam("offset") Integer offset,
        @QueryParam("sort_column") Integer sortColumn,
        @QueryParam("asc") Boolean asc,
        // @QueryParam("genres") List<String> genres
        @QueryParam("authors") List<String> authors
        )throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        JSONObject response = new JSONObject();
        List<JSONObject> books = new ArrayList<>();

        LibraryBookDao libraryBookDao = new LibraryBookDao();
        PaginatedList<LibraryBookDto> paginatedList = PaginatedLists.create(limit, offset);

        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        LibraryBookCriteria criteria = new LibraryBookCriteria();
        

        criteria.setSearch(null);
        criteria.setAuthorNames(authors);
        criteria.setGenreList(null);
        criteria.setMinRating(0);

        try {
            libraryBookDao.findByCriteria(paginatedList, criteria, sortCriteria);
        } catch (Exception e) {
            throw new ServerException("SearchError", "Error searching in books", e);
        }

        for (LibraryBookDto libraryBookDto : paginatedList.getResultList()) {
            JSONObject book = getJsonObject(libraryBookDto);

            books.add(book);
        }

        response.put("total", paginatedList.getResultCount());
        response.put("books", books);

        return Response.ok().entity(response).build();
    }


    /**
     * Adds a book to the library by fetching the book details using the book ID.
     *
     * @param bookId The ID of the book to add
     * @return Response
     * @throws JSONException
     * @author Prakhar Jain
     */
    @POST
    @Path("add")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    // Add a book to the library
    public Response add(@FormParam("bookId") String bookId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        BookDao bookDao = new BookDao();
        Book book = bookDao.getById(bookId);
        if (book == null) {
            throw new ClientException("BookNotFoundError", "No book found with the specified ID " + bookId);
        }

        LibraryBook libraryBook = getLibraryBook(book);

        // Create and return the response
        JSONObject response = new JSONObject();
        response.put("id", libraryBook.getId());
        return Response.ok().entity(response).build();
    }

    private LibraryBook getLibraryBook(Book book) throws JSONException {
        // Instantiate LibraryBookDao to interact with the database
        LibraryBookDao libraryBookDao = new LibraryBookDao();

        // Check if the book is already in the LibraryBook table
        LibraryBook existingLibraryBook = libraryBookDao.getByBookId(book.getId());
        if (existingLibraryBook != null) {
            // Throw an exception indicating the book is already in the library
            throw new ClientException(BookErrorConstants.BOOK_ALREADY_ADDED, "Book already added");
        }

        // Since the book is not in the library, create a new LibraryBook object
        LibraryBook newLibraryBook = new LibraryBook();
        newLibraryBook.setBookId(book.getId());
        newLibraryBook.setCreateDate(new Date());
        // Set initial ratings and average rating to 0
        newLibraryBook.setNumRatings(0);
        newLibraryBook.setAvgRating(0);

        // Save the new LibraryBook object using the LibraryBookDao
        libraryBookDao.create(newLibraryBook);

        return newLibraryBook;
    }

    @GET
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response get(
            @PathParam("id") String bookId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Fetch the book
        BookDao bookDao = new BookDao();
        Book bookDb = bookDao.getById(bookId);
        // Return book data
        JSONObject book = new JSONObject();
        book.put("id", bookId);
        book.put(BookDetailsConstants.TITLE, bookDb.getTitle());
        book.put(BookDetailsConstants.SUBTITLE, bookDb.getSubtitle());
        book.put(BookDetailsConstants.AUTHOR, bookDb.getAuthor());
        book.put("page_count", bookDb.getPageCount());
        book.put(BookDetailsConstants.DESCRIPTION, bookDb.getDescription());
        book.put(BookDetailsConstants.ISBN10, bookDb.getIsbn10());
        book.put(BookDetailsConstants.ISBN13, bookDb.getIsbn13());
        book.put(BookDetailsConstants.LANGUAGE, bookDb.getLanguage());
        if (bookDb.getPublishDate() != null) {
            book.put(BookDetailsConstants.PUBLISH_DATE, bookDb.getPublishDate().getTime());
        }
        book.put("bookId", bookDb.getId());

        // // add genres to the book
        Set<Genre> genres = bookDb.getGenres();
        List<String> genreNames = new ArrayList<>();
        for (Genre genre : genres) {
            genreNames.add(genre.getName());
        }
        book.put("genres", genreNames);

        return Response.ok().entity(book).build();
    }

    @POST
    @Path("rate/{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response rateBook(@PathParam("id") String bookId, @FormParam("rating") int rating) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        String userId = principal.getId();

        if (bookId == null) {
            throw new ClientException("BookIdError", "Book ID is required");
        }
        if (userId == null) {
            throw new ClientException("UserIdError", "User ID is required");
        }
        if (rating < 0 || rating > 10) {
            throw new ClientException("RatingError", "Rating must be between 0 and 10");
        }

        // check that the book exists in the library
        LibraryBookDao libraryBookDao = new LibraryBookDao();
        LibraryBook libraryBook = libraryBookDao.getByBookId(bookId);
        if (libraryBook == null) {
            throw new ClientException("BookNotInLibraryError", "Book not in library");
        }

        // check if book already rated by user
        LibraryBookRatingDao libraryBookRatingDao = new LibraryBookRatingDao();
        LibraryBookRating libraryBookRating = libraryBookRatingDao.getByUserIdAndBookId(userId, bookId);

        if (libraryBookRating != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new JSONObject().put("error", "BookAlreadyRatedError")
                            .put("message", "Book already rated by user"))
                    .build();
        }

        // Create the LibraryBookRating object and add to db
        libraryBookRating = new LibraryBookRating();
        libraryBookRating.setBookId(bookId);
        libraryBookRating.setUserId(userId);
        libraryBookRating.setRating(rating);
        libraryBookRating.setCreateDate(new Date());
        libraryBookRatingDao.create(libraryBookRating);

        // Notify the corresponding LibraryBook with the same bookId to update its
        // number of ratings and average rating
        libraryBookDao.addRatingForBook(bookId, rating);

        return Response.ok().build();
    }

    /**
     * Returns a book cover.
     *
     * @param bookId library book ID
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}/cover")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response cover(
            @PathParam("id") final String bookId) throws JSONException {

        BookDao BookDao = new BookDao();
        Book book = BookDao.getById(bookId);

        // Get the cover image
        File file = Paths.get(DirectoryUtil.getBookDirectory().getPath(), book.getId()).toFile();
        InputStream inputStream = null;
        try {
            if (file.exists()) {
                inputStream = new FileInputStream(file);
            } else {
                inputStream = new FileInputStream(
                        new File(Objects.requireNonNull(getClass().getResource("/dummy.png")).getFile()));
            }
        } catch (FileNotFoundException e) {
            throw new ServerException("FileNotFound", "Cover file not found", e);
        }

        return Response.ok(inputStream)
                .header("Content-Type", "image/jpeg")
                .header("Expires",
                        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date().getTime() + 3600000))
                .build();
    }

    /**
     * Returns the top 10 books based on average rating or number of ratings.
     *
     * @param selection The selection criteria (average_rating or number_of_ratings)
     * @param order     The order of the results (asc or desc)
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("top/{selection}/{order}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTop10Books(
            @PathParam("selection") String selection,
            @PathParam("order") String order) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        JSONObject response = new JSONObject();
        List<JSONObject> books = new ArrayList<>();

        List<LibraryBookDto> topBookDtos = getLibraryBookDtos(selection, order);

        for (LibraryBookDto libraryBookDto : topBookDtos) {
            JSONObject book = getJsonObject(libraryBookDto);

            books.add(book);
        }

        response.put("total", topBookDtos.size());
        response.put("books", books);

        return Response.ok().entity(response).build();
    }

    private static List<LibraryBookDto> getLibraryBookDtos(String selection, String order) throws JSONException {
        // check if selection is avgRating or numRatings
        if (!selection.equals("avgRating") && !selection.equals("numRatings")) {
            throw new ClientException("SelectionError", "Selection must be avgRating or numRatings");
        }

        // check if order is asc or desc
        if (!order.equals("asc") && !order.equals("desc")) {
            throw new ClientException("OrderError", "Order must be asc or desc");
        }

        boolean desc = order.equals("desc");
        boolean avgRating = selection.equals("avgRating");

        LibraryBookDao libraryBookDao = new LibraryBookDao();
        List<LibraryBookDto> topBookDtos = libraryBookDao.getTop10Books(desc, avgRating);
        return topBookDtos;
    }

    /**
     * Updates a book cover.
     *
     * @param libraryBookId library book ID
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/cover")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response updateCover(
            @PathParam("id") String libraryBookId,
            @FormParam("url") String imageUrl) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the user book
        // UserBookDao userBookDao = new UserBookDao();
        Book book = getBook(libraryBookId);

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

    private Book getBook(String libraryBookId) throws JSONException {
        LibraryBookDao libraryBookDao = new LibraryBookDao();
        LibraryBook libraryBook = libraryBookDao.getById(libraryBookId);
        if (libraryBook == null) {
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "Book not found with id " + libraryBookId);
        }

        // Get the book
        BookDao bookDao = new BookDao();
        return bookDao.getById(libraryBook.getBookId());
    }

    @GET
    @Path("/authors")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuthors() throws JSONException {
        if (!authenticate()) {
        throw new ForbiddenClientException();
        }

        LibraryBookDao libraryBookDao = new LibraryBookDao();
        List<String> authors = libraryBookDao.getAuthors();

        JSONObject response = new JSONObject();
        response.put("authors", authors);
        return Response.ok().entity(response).build();
    }

}
