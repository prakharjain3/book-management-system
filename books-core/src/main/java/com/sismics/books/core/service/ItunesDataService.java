package com.sismics.books.core.service;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.RateLimiter;


public class ItunesDataService extends AbstractIdleService implements ServiceStrategy{

    /**
     * Logger.
     */
    protected static final Logger log = LoggerFactory.getLogger(ItunesDataService.class);

    /**
     * Executor for book API requests.
     */
    protected ExecutorService executor;

    /**
     * Itunes API rate limiter.
     */
    protected RateLimiter rateLimiter = RateLimiter.create(0.33);
    
    /**
     * Parser for multiple date formats;
     */
    protected static DateTimeFormatter formatter;

    /**
     * Podcasts API Search URL.
     */
    private static final String PODCASTS_SEARCH_FORMAT = "https://itunes.apple.com/search?term=%s&media=podcast&limit=10";

    /**
	 * AudioBooks API Search URL.
	 */
    private static final String AUDIO_BOOKS_SEARCH_FORMAT = "https://itunes.apple.com/search?term=%s&media=audiobook&limit=10";

    
    static {
        // Initialize date parser
        DateTimeParser[] parsers = { 
            DateTimeFormat.forPattern("yyyy").getParser(),
            DateTimeFormat.forPattern("yyyy-MM").getParser(),
            DateTimeFormat.forPattern("yyyy-MM-dd").getParser(),
            DateTimeFormat.forPattern("MMM d, yyyy").getParser(),
            DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").getParser()
        };
        formatter = new DateTimeFormatterBuilder().append( null, parsers ).toFormatter();
    }

    @Override
    protected void startUp() throws Exception {
        executor = Executors.newSingleThreadExecutor(); 
        if (log.isInfoEnabled()) {
            log.info("Itunes data service started");
        }
    }


    @Override
    protected void shutDown() throws Exception {
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        if (log.isInfoEnabled()) {
            log.info("Itunes data service stopped");
        }
        
    }

    public JsonNode callAPI(String name, boolean isAudiobook) throws Exception {
        rateLimiter.acquire();

        URL url = new URL(String.format(AUDIO_BOOKS_SEARCH_FORMAT, name));

        if (isAudiobook == false) {
            url = new URL(String.format(PODCASTS_SEARCH_FORMAT, name));
        }

        URLConnection connection = url.openConnection();
        connection.setRequestProperty("Accept-Charset", "utf-8");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.62 Safari/537.36");        
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        try { 
            InputStream inputStream = connection.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readValue(inputStream, JsonNode.class);
            ArrayNode results = (ArrayNode) rootNode.get("results");
            if (results.size() == 0) {
                throw new IllegalArgumentException("No result found");
            }

            // JsonNode result = results.get(0);
            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
