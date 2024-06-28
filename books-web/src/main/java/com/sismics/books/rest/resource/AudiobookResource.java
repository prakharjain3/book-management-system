package com.sismics.books.rest.resource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.EntityExistsException;
import javax.ws.rs.Consumes;
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

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.books.core.dao.jpa.BookDao;
import com.sismics.books.core.dao.jpa.UserBookDao;
import com.sismics.books.core.dao.jpa.criteria.UserBookCriteria;
import com.sismics.books.core.dao.jpa.dto.UserBookDto;
import com.sismics.books.core.factory.ContentFactory;
import com.sismics.books.core.factory.SpotifyFactory;
import com.sismics.books.core.factory.ItunesFactory;
import com.sismics.books.core.model.jpa.Audiobook;
import com.sismics.books.core.model.jpa.AudiobookAdapter;
import com.sismics.books.core.model.jpa.Book;
import com.sismics.books.core.model.jpa.Content;
import com.sismics.books.core.model.jpa.UserBook;
import com.sismics.books.core.service.ThumbnailService;
import com.sismics.books.core.util.DirectoryUtil;
import com.sismics.books.core.util.jpa.PaginatedList;
import com.sismics.books.core.util.jpa.PaginatedLists;
import com.sismics.books.core.util.jpa.SortCriteria;
import com.sismics.books.core.util.mime.MimeType;
import com.sismics.books.core.util.mime.MimeTypeUtil;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import com.restfb.json.JsonArray;
import com.sismics.books.core.constant.BookDetailsConstants;
import com.sismics.books.core.constant.BookErrorConstants;


@Path("/audiobook")
public class AudiobookResource extends AuthenticatedResource {

    @PUT
    @Path("spotify/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response search_spotify(
            @FormParam("spotifyName") String name) throws Exception {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        ValidationUtil.validateRequired(name, "name");

        JsonNode audiobooks = null;
        try {
            ContentFactory factory = new SpotifyFactory();
            audiobooks = factory.searchAudiobook(name);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "Audiobook not found");
        }

        if(audiobooks == null) {
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "Audiobooks not found");
        }        

        ObjectMapper objectMapper = new ObjectMapper();
        String audiobooksJson = objectMapper.writeValueAsString(audiobooks);

        JSONObject response = new JSONObject();
        response.put("audiobooks", new JSONArray(audiobooksJson));
        return Response.ok().entity(response.toString()).build();
    }

    @POST
    @Path("spotify/add")
    // public Response add_spotify(String audiobookString) throws Exception {
    public Response add_spotify(
        @FormParam("name") String title,
        @FormParam("narrators[name]") List<String> authors,
        @FormParam("total_chapters") int duration,
        @FormParam("description") String description,
        @FormParam("edition") String subTitle,
        @FormParam("languages") List<String> languages,
        @FormParam("id") String id,
        @FormParam("images[url]") List<String> images
    ) throws Exception {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Audiobook audiobook = buildAudiobookFromNull(name, authors.get(0), total_chapters, description, edition, languages.get(0), id, images.get(2));
        ContentFactory factory = new SpotifyFactory();
        Audiobook audiobook = factory.buildAudiobookFromNull(title, authors.get(0), duration, description, subTitle, languages.get(0), id, images.get(2), null);
        
        ThumbnailService thumbnailService = new ThumbnailService();
        
        try {   
            thumbnailService.downloadThumbnail(audiobook.getId(), images.get(2));
        } catch (Exception e) {
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "thumbnail not found");
        }

        BookDao bookDao = new BookDao();
        AudiobookAdapter book = new AudiobookAdapter(audiobook);

        try {
            bookDao.create(book);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "Audiobook not found");
        }
        
        UserBook userBook = getUserBook(book);

        
        JSONObject response = new JSONObject();
        response.put("id", userBook.getId());
        return Response.ok().entity(response).build();
    }

    @PUT
    @Path("itunes/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response search_itunes(
            @FormParam("itunesName") String name) throws Exception {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        ValidationUtil.validateRequired(name, "name");

        JsonNode audiobooks = null;
        try {
            ContentFactory factory = new ItunesFactory();
            audiobooks = factory.searchAudiobook(name);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "Audiobook not found");
        }

        if(audiobooks == null) {
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "Audiobooks not found");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String audiobooksJson = objectMapper.writeValueAsString(audiobooks);

        JSONObject response = new JSONObject();
        response.put("audiobooks", new JSONArray(audiobooksJson));
        return Response.ok().entity(response.toString()).build();
    }

    @POST
    @Path("itunes/add")
    public Response add_itunes(
        @FormParam("collectionName") String title,
        @FormParam("artistName") String author,
        // @FormParam("total_chapters") int total_chapters,
        @FormParam("description") String description,
        @FormParam("primaryGenreName") String subTitle,
        @FormParam("country") String language,
        @FormParam("collectionId") String id,
        @FormParam("artworkUrl60") String imageUrl,
        @FormParam("releaseDate") String date
    ) throws Exception {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        ContentFactory factory = new ItunesFactory();
        Audiobook audiobook = factory.buildAudiobookFromNull(title, author, 1000 * 60 * 60, description, subTitle, language, id, imageUrl, date);
    
        ThumbnailService thumbnailService = new ThumbnailService();
        
        try {   
            thumbnailService.downloadThumbnail(audiobook.getId(), imageUrl);
        } catch (Exception e) {
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "thumbnail not found");
        }

        BookDao bookDao = new BookDao();
        AudiobookAdapter book = new AudiobookAdapter(audiobook);

        try {
            bookDao.create(book);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "Audiobook not found");
        }
        
        UserBook userBook = getUserBook(book);
        
        JSONObject response = new JSONObject();
        response.put("id", userBook.getId());
        return Response.ok().entity(response).build();
    }

    private UserBook getUserBook(Book adapter) {
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getByBook(adapter.getId(), principal.getId());
        if (userBook == null) {
            userBook = new UserBook();
            userBook.setBookId(adapter.getId());
            userBook.setUserId(principal.getId());
            userBook.setCreateDate(new Date());
            userBook.setType("audiobook");
            userBookDao.create(userBook);
        }


        return userBook;
    }

    @GET
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @PathParam("id") String userBookId) throws Exception {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

       // Fetch the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId);
        if (userBook == null) {
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "Userbook not found with id " + userBookId);
        }
        
        // Fetch the book
        BookDao bookDao = new BookDao();
        AudiobookAdapter bookDb = bookDao.getAudiobookAdapterById(userBook.getBookId());

        // Return book data
        JSONObject book = new JSONObject();
        book.put("id", userBook.getId());
        book.put(BookDetailsConstants.TITLE, bookDb.getTitle());
        book.put(BookDetailsConstants.SUBTITLE, bookDb.getSubtitle());
        book.put(BookDetailsConstants.AUTHOR, bookDb.getAuthor());
        // if (bookDb.getDuration() != null){   
        //     book.put("page_count", bookDb.getDuration());
        // }
        book.put(BookDetailsConstants.DESCRIPTION, bookDb.getDescription());
        book.put(BookDetailsConstants.LANGUAGE, bookDb.getLanguage());
        if (bookDb.getPublishDate() != null) {
            book.put(BookDetailsConstants.PUBLISH_DATE, bookDb.getPublishDate().getTime());
        }
        book.put("create_date", userBook.getCreateDate().getTime());
        if (userBook.getReadDate() != null) {
            book.put("read_date", userBook.getReadDate().getTime());
        }
        book.put("favourite", userBook.getFav());

        return Response.ok().entity(book).build();
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