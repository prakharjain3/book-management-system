package com.sismics.books.rest.resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sismics.books.core.constant.BookDetailsConstants;
import com.sismics.books.core.dao.jpa.TagDao;
import com.sismics.books.core.dao.jpa.UserBookDao;
import com.sismics.books.core.dao.jpa.criteria.UserBookCriteria;
import com.sismics.books.core.dao.jpa.dto.TagDto;
import com.sismics.books.core.dao.jpa.dto.UserBookDto;
import com.sismics.books.core.model.jpa.Tag;
import com.sismics.books.core.util.jpa.PaginatedList;
import com.sismics.books.core.util.jpa.PaginatedLists;
import com.sismics.books.core.util.jpa.SortCriteria;
import com.sismics.rest.exception.ServerException;

import com.sismics.books.core.dao.jpa.TagDao;


@Path("/publicBook")
public class PublicBookResource {
    
    /**
     * Returns the list of all tags.
     * 
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("/listPublicTags")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listPublicTags() throws JSONException {


        TagDao tagDao = new TagDao();
        // IPrincipal principal = this.baseResource.getPrincipal();
        // List<Tag> tagList = this.tagDao.getByUserId(principal.getId());
        List<Tag> publicTagList = tagDao.getPublicTags();

        // add public tags without duplicates
        Set<Tag> tagSet = new HashSet<>(publicTagList);
        // tagSet.addAll(publicTagList);
        // tagSet.addAll(tagList);
        publicTagList = new ArrayList<Tag>(tagSet);

        JSONObject response = new JSONObject();
        List<JSONObject> items = new ArrayList<>();
        for (Tag tag : publicTagList) {
            JSONObject item = new JSONObject();
            item.put("id", tag.getId());
            item.put("name", tag.getName());
            item.put("color", tag.getColor());
            item.put("visibility", tag.getVisibility());
            items.add(item);
        }
        response.put("tags", items);
        return Response.ok().entity(response).build();
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
            // @QueryParam("tag") String tagName,
            @QueryParam("tag") String tagId,
            @QueryParam("favourite") int favourite) throws JSONException {
        
        JSONObject response = new JSONObject();
        List<JSONObject> books = new ArrayList<>();

        // List<JSONObject> audiobooks = new ArrayList<>();

        // List<JSONObject> podcasts = new ArrayList<>();
        
        UserBookDao userBookDao = new UserBookDao();
        TagDao tagDao = new TagDao();
        PaginatedList<UserBookDto> paginatedList = PaginatedLists.create(limit, offset);
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        UserBookCriteria criteria = new UserBookCriteria();
        criteria.setSearch(search);
        // criteria.setRead(read);
        // criteria.setFavourite(favourite);
        // criteria.setUserId(principal.getId());
        if (!Strings.isNullOrEmpty(tagId)) {
            Tag tag = tagDao.getPublicByID(tagId);
            
            if (tag != null) {
                criteria.setTagIdList(Lists.newArrayList(tag.getId()));
            }
        }
        
        try {
            tagDao.getPublicBooks(paginatedList, criteria, sortCriteria);
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
            
            books.add(book);
        }
        response.put("total", paginatedList.getResultCount());
        response.put("books", books);
        // response.put("audiobooks", audiobooks);
        // response.put("podcasts", podcasts);
        
        return Response.ok().entity(response).build();
    }
}
