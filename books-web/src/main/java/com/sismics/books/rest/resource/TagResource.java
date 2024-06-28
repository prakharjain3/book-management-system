package com.sismics.books.rest.resource;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.books.core.dao.jpa.TagDao;
import com.sismics.books.core.model.jpa.Tag;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.books.core.constant.TagsErrorMessages;


/**
 * Tag REST resources.
 * 
 * @author bgamard
 */
@Path("/tag")
public class TagResource extends AuthenticatedResource{

    private final TagDao tagDao;

    /*
     * Constructor
     */

    public TagResource() {
        this.tagDao = new TagDao();
    }

    // Authentication helper method
    private void authenticateOrThrowForbidden() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
    }

    // Validation helper method
    private void validateInput (String name, String color, String visibility) throws JSONException {
        name = ValidationUtil.validateLength(name, "name", 1, 36, false);
        ValidationUtil.validateHexColor(color, "color", true);
        ValidationUtil.validateVisibility(visibility);
    }

    // Validation helper method checks if tag name contains spaces
    private void ensureNoSpacesInName(String name) throws JSONException{
        if (name.contains(" ")) {
            throw new ClientException(TagsErrorMessages.SPACES_NOT_ALLOWED, "Spaces are not allowed in tag name");
        }
    } 
    
    // Validation helper method checks if tag exists
    private void ensureTagExists(Tag tag, String tagId) throws JSONException {
        if (tag == null) {
            throw new ClientException(TagsErrorMessages.TAG_NOT_FOUND, MessageFormat.format("Tag not found: {0}", tagId));
        }
    }

    // Validation helper method checks if tag does not exist
    private void ensureTagDoesNotExist(Tag tag, String name) throws JSONException {
        if (tag != null) {
            throw new ClientException(TagsErrorMessages.ALREADY_EXISTING_TAG, MessageFormat.format("Tag already exists: {0}", name));
        }
    }

    // Validation helper method checks if tag name is not duplicate
    private void ensureNoNameDuplicate(String name, String id) throws JSONException {
        Tag tagDuplicate = this.tagDao.getByName(principal.getId(), name);
        if (tagDuplicate != null && !tagDuplicate.getId().equals(id)) {
            throw new ClientException(TagsErrorMessages.ALREADY_EXISTING_TAG, MessageFormat.format("Tag already exists: {0}", name));
        }
    }

    /**
     * Returns the list of all tags.
     * 
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() throws JSONException {
        // if (!authenticate()) {
        //     throw new ForbiddenClientException();
        // }
        authenticateOrThrowForbidden();
        
        // TagDao tagDao = new TagDao();
        // IPrincipal principal = this.baseResource.getPrincipal();
        List<Tag> tagList = this.tagDao.getByUserId(principal.getId());
        // List<Tag> publicTagList = this.tagDao.getPublicTags();

        // add public tags without duplicates
        Set<Tag> tagSet = new HashSet<>(tagList);
        // tagSet.addAll(publicTagList);
        tagSet.addAll(tagList);
        tagList = new ArrayList<Tag>(tagSet);

        JSONObject response = new JSONObject();
        List<JSONObject> items = new ArrayList<>();
        for (Tag tag : tagList) {
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
     * Creates a new tag.
     * 
     * @param name Name
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(
            @FormParam("name") String name,
            @FormParam("color") String color,
            @FormParam("visibility") String visibility) throws JSONException {
        // if (!authenticate()) {
        //     throw new ForbiddenClientException();
        // }
        authenticateOrThrowForbidden();

        if (visibility == null) {
            visibility = "private";
        }

        // Validate input data
        // name = ValidationUtil.validateLength(name, "name", 1, 36, false);
        // ValidationUtil.validateHexColor(color, "color", true);
        validateInput(name, color, visibility);
        
        // Don't allow spaces
        // if (name.contains(" ")) {
        //     throw new ClientException("SpacesNotAllowed", "Spaces are not allowed in tag name");
        // }
        ensureNoSpacesInName(name);
        
        // Get the tag
        // TagDao tagDao = new TagDao();
        // IPrincipal principal = this.baseResource.getPrincipal();
        Tag tag = this.tagDao.getByName(principal.getId(), name);
        // if (tag != null) {
        //     throw new ClientException("AlreadyExistingTag", MessageFormat.format("Tag already exists: {0}", name));
        // }
        ensureTagDoesNotExist(tag, name);
        
        // Create the tag
        tag = new Tag();
        tag.setName(name);
        tag.setColor(color);
        tag.setUserId(principal.getId());
        tag.setVisibility(visibility);
        String tagId = tagDao.create(tag);
        
        JSONObject response = new JSONObject();
        response.put("id", tagId);
        return Response.ok().entity(response).build();
    }

    /**
     * Update a tag.
     * 
     * @param name Name
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("id") String id,
            @FormParam("name") String name,
            @FormParam("color") String color,
            @FormParam("visibility") String visibility) throws JSONException {
        // if (!authenticate()) {
        //     throw new ForbiddenClientException();
        // }
        authenticateOrThrowForbidden();
        
        if (visibility == null) {
            visibility = "private";
        }

        // Validate input data
        // name = ValidationUtil.validateLength(name, "name", 1, 36, true);
        // ValidationUtil.validateHexColor(color, "color", true);
        validateInput(name, color, visibility);
        
        // Don't allow spaces
        // if (name.contains(" ")) {
        //     throw new ClientException("SpacesNotAllowed", "Spaces are not allowed in tag name");
        // }
        ensureNoSpacesInName(name);
        
        // Get the tag
        // TagDao tagDao = new TagDao();
        // IPrincipal principal = this.baseResource.getPrincipal();
        Tag tag = this.tagDao.getByTagId(principal.getId(), id);
        // if (tag == null) {
        //     throw new ClientException("TagNotFound", MessageFormat.format("Tag not found: {0}", id));
        // }
        ensureTagExists(tag, id);
        
        // Check for name duplicate
        // Tag tagDuplicate = this.tagDao.getByName(principal.getId(), name);
        // if (tagDuplicate != null && !tagDuplicate.getId().equals(id)) {
        //     throw new ClientException("AlreadyExistingTag", MessageFormat.format("Tag already exists: {0}", name));
        // }
        ensureNoNameDuplicate(name, id);
        
        // Update the tag
        if (!StringUtils.isEmpty(name)) {
            tag.setName(name);
        }
        if (!StringUtils.isEmpty(color)) {
            tag.setColor(color);
        }
        if (!StringUtils.isEmpty(visibility)) {
            tag.setVisibility(visibility);
        }
        
        JSONObject response = new JSONObject();
        response.put("id", id);
        return Response.ok().entity(response).build();
    }
    
    /**
     * Delete a tag.
     * 
     * @param tagId Tag ID
     * @return Response
     * @throws JSONException
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("id") String tagId) throws JSONException {
        // if (!authenticate()) {
        //     throw new ForbiddenClientException();
        // }
        authenticateOrThrowForbidden();
        
        // Get the tag
        // TagDao tagDao = new TagDao();
        // IPrincipal principal = this.baseResource.getPrincipal();
        Tag tag = this.tagDao.getByTagId(principal.getId(), tagId);
        // if (tag == null) {
        //     throw new ClientException("TagNotFound", MessageFormat.format("Tag not found: {0}", tagId));
        // }
        ensureTagExists(tag, tagId);
        
        // Delete the tag
        this.tagDao.delete(tagId);
        
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * makePublic a tag.
     * 
     * @param tagId Tag ID
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("makePublic/{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response makePublic(
            @PathParam("id") String tagId) throws JSONException {
        // if (!authenticate()) {
        //     throw new ForbiddenClientException();
        // }
        authenticateOrThrowForbidden();
        
        // Get the tag
        TagDao tagDao = new TagDao();
        // IPrincipal principal = this.baseResource.getPrincipal();
        Tag tag = this.tagDao.getByTagId(principal.getId(), tagId);
        // if (tag == null) {
        //     throw new ClientException("TagNotFound", MessageFormat.format("Tag not found: {0}", tagId));
        // }
        ensureTagExists(tag, tagId);
        
        // Delete the tag
        this.tagDao.makePublic(tagId);
        
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
