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
import com.sismics.books.core.factory.ContentFactory;
import com.sismics.books.core.factory.SpotifyFactory;
import com.sismics.books.core.factory.ItunesFactory;
import com.sismics.books.core.model.jpa.Podcast;
import com.sismics.books.core.model.jpa.PodcastAdapter;
import com.sismics.books.core.model.jpa.AudiobookAdapter;
import com.sismics.books.core.model.jpa.Book;
import com.sismics.books.core.model.jpa.Content;
import com.sismics.books.core.model.jpa.UserBook;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;
import com.restfb.json.JsonArray;
import com.sismics.books.core.constant.BookDetailsConstants;
import com.sismics.books.core.constant.BookErrorConstants;
import com.sismics.books.core.service.ThumbnailService;

@Path("/podcast")
public class PodcastResource extends AuthenticatedResource{

    @PUT
    @Path("spotify/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response search_spotify(
            @FormParam("spotifyName") String name) throws Exception {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        ValidationUtil.validateRequired(name, "name");

        JsonNode podcasts = null;
        try {
            ContentFactory factory = new SpotifyFactory();
            podcasts = factory.searchPodcast(name);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "Podcast not found");
        }

        if(podcasts == null) {
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "Podcast not found");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String podcastsJson = objectMapper.writeValueAsString(podcasts);

        JSONObject response = new JSONObject();
        response.put("podcasts", new JSONArray(podcastsJson));
        return Response.ok().entity(response.toString()).build();
    }

    @POST
    @Path("spotify/add")
    public Response add_spotify(
        @FormParam("name") String title,
        // @FormParam("narrators[name]") List<String> authors,
        @FormParam("duration_ms") int duration,
        @FormParam("release_date") String releaseDate,
        @FormParam("description") String description,
        // @FormParam("edition") String edition,
        @FormParam("language") String language,
        @FormParam("id") String id,
        @FormParam("images[url]") List<String> images
    ) throws Exception {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Podcast podcast = buildPodcastFromNull(name, null, duration, description, language, language, id, images.get(2));
        ContentFactory factory = new SpotifyFactory();
        Podcast podcast = factory.buildPodcastFromNull(title, null, duration, releaseDate, description, language, language, id, images.get(2));

        ThumbnailService thumbnailService = new ThumbnailService();
        
        try {   
            thumbnailService.downloadThumbnail(podcast.getId(), images.get(2));
        } catch (Exception e) {
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "thumbnail not found");
        }
        
        BookDao bookDao = new BookDao();
        PodcastAdapter book = new PodcastAdapter(podcast);

        try {
            bookDao.create(book);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "Podcast not found");
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

        JsonNode podcasts = null;
        try {
            ContentFactory factory = new ItunesFactory();
            podcasts = factory.searchPodcast(name);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "Podcasts not found");
        }

        if(podcasts == null) {
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "Podcasts not found");
        }        

        ObjectMapper objectMapper = new ObjectMapper();
        String podcastsJson = objectMapper.writeValueAsString(podcasts);

        JSONObject response = new JSONObject();
        response.put("podcasts", new JSONArray(podcastsJson));
        return Response.ok().entity(response.toString()).build();
    }

    @POST
    @Path("itunes/add")
    public Response add_itunes(
        @FormParam("collectionName") String title,
        @FormParam("artistName") String artist,
        @FormParam("trackTimeMillis") int duration,
        @FormParam("releaseDate") String releaseDate,
        @FormParam("genres") List<String> genreList,
        @FormParam("primaryGenreName") String subTitle,
        @FormParam("country") String language,
        @FormParam("collectionId") String id,
        @FormParam("artworkUrl60") String imageUrl
    ) throws Exception {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        String description = String.join(", ", genreList);        
        // Podcast podcast = buildPodcastFromNull(title, artist, duration, releaseDate, description, subTitle, language, id, null);
        ContentFactory factory = new ItunesFactory();
        Podcast podcast = factory.buildPodcastFromNull(title, artist, duration, releaseDate, description, subTitle, language, id, imageUrl);

        ThumbnailService thumbnailService = new ThumbnailService();
        
        try {   
            thumbnailService.downloadThumbnail(podcast.getId(), imageUrl);
        } catch (Exception e) {
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "thumbnail not found");
        }
        
        BookDao bookDao = new BookDao();
        PodcastAdapter book = new PodcastAdapter(podcast);

        try {
            bookDao.create(book);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClientException(BookErrorConstants.BOOK_NOT_FOUND, "Podcast not found");
        }
        
        UserBook userBook = getUserBook(book);
        
        JSONObject response = new JSONObject();
        response.put("id", userBook.getId());
        return Response.ok().entity(response).build();
    }

    
    private UserBook getUserBook(Book adapter) {
        UserBookDao userBookDao = new UserBookDao();        UserBook userBook = userBookDao.getByBook(adapter.getId(), principal.getId());
        if (userBook == null) {
            userBook = new UserBook();
            userBook.setBookId(adapter.getId());
            userBook.setUserId(principal.getId());
            userBook.setCreateDate(new Date());
            userBook.setType("podcast");
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
        PodcastAdapter bookDb = bookDao.getPodcastAdapterById(userBook.getBookId());

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
