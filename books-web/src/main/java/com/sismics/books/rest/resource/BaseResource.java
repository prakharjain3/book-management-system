package com.sismics.books.rest.resource;

import com.sismics.books.rest.constant.BaseFunction;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.security.UserPrincipal;
import org.codehaus.jettison.json.JSONException;

import javax.ws.rs.QueryParam;
import java.util.Set;

/**
 * Base class of REST resources.
 * 
 * @author jtremeaux
 */
public abstract class BaseResource extends AuthenticatedResource{
    /**
     * Application key.
     */
    @QueryParam("app_key")
    protected String appKey;
    
    /**
     * Checks if the user has a base function. Throw an exception if the check fails.
     * 
     * @param baseFunction Base function to check
     * @throws JSONException
     */
    protected void checkBaseFunction(BaseFunction baseFunction) throws JSONException {
        if (!hasBaseFunction(baseFunction)) {
            throw new ForbiddenClientException();
        }
    }
    
    /**
     * Checks if the user has a base function.
     * 
     * @param baseFunction Base function to check
     * @return True if the user has the base function
     * @throws JSONException
     */
    protected boolean hasBaseFunction(BaseFunction baseFunction) throws JSONException {
        if (principal == null || !(principal instanceof UserPrincipal)) {
            return false;
        }
        Set<String> baseFunctionSet = ((UserPrincipal) principal).getBaseFunctionSet();
        return baseFunctionSet != null && baseFunctionSet.contains(baseFunction.name());
    }
}
