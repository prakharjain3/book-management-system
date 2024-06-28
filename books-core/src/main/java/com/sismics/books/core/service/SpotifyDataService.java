package com.sismics.books.core.service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.RateLimiter;
import com.sismics.books.core.constant.ConfigType;
import com.sismics.books.core.model.jpa.Audiobook;
import com.sismics.books.core.util.ConfigUtil;
import com.sismics.books.core.util.DirectoryUtil;
import com.sismics.books.core.util.TransactionUtil;
import com.sismics.books.core.util.mime.MimeType;
import com.sismics.books.core.util.mime.MimeTypeUtil;


public class SpotifyDataService extends AbstractIdleService implements ServiceStrategy{

    protected static final Logger log = LoggerFactory.getLogger(SpotifyDataService.class);

    protected ExecutorService executor;

    protected RateLimiter rateLimiter = RateLimiter.create(1);

    protected String clientIdSpotify = null;

	protected String clientSecretSpotify = null;
    
    protected static DateTimeFormatter formatter;


    private static final String AUDIO_BOOKS_SEARCH_FORMAT = "https://api.spotify.com/v1/search/?q=%s&type=audiobook&limit=10";

    private static final String PODCAST_SEARCH_FORMAT = "https://api.spotify.com/v1/search/?q=%s&type=episode&market=ES&limit=10";

    private static final String GET_ACCESS_TOKEN_FORMAT = "https://accounts.spotify.com/api/token";

    private static String access_token = null;

    
    static {
        // Initialize date parser
        DateTimeParser[] parsers = { 
                DateTimeFormat.forPattern("yyyy").getParser(),
                DateTimeFormat.forPattern("yyyy-MM").getParser(),
                DateTimeFormat.forPattern("yyyy-MM-dd").getParser(),
                DateTimeFormat.forPattern("MMM d, yyyy").getParser(),
                DateTimeFormat.forPattern("MMM dd, yyyy h:mm:ss a").getParser(),
            };
        formatter = new DateTimeFormatterBuilder().append( null, parsers ).toFormatter();
    }

    @Override
    protected void startUp() throws Exception {
        initConfig();
        executor = Executors.newSingleThreadExecutor(); 
        if (log.isInfoEnabled()) {
            log.info("Spotify data service started");
        }
    }

    /**
     * Initialize service configuration.
     */
    public void initConfig() {
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                clientIdSpotify = ConfigUtil.getConfigStringValue(ConfigType.CLIENT_ID_SPOTIFY);
				clientSecretSpotify = ConfigUtil.getConfigStringValue(ConfigType.CLIENT_SECRET_SPOTIFY);
            }
        });
    }
    

    @Override
    protected void shutDown() throws Exception {
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        if (log.isInfoEnabled()) {
            log.info("Spotify data service stopped");
        }
        
    }
    
    private void getAccessToken() throws Exception {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((clientIdSpotify + ":" + clientSecretSpotify).getBytes(StandardCharsets.UTF_8));
        String requestBody = "grant_type=client_credentials";
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GET_ACCESS_TOKEN_FORMAT))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", authHeader)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode accessTocken = mapper.readValue(responseBody, JsonNode.class);
                access_token = accessTocken.get("access_token").getTextValue();
            }else{
                log.error("Error while getting access token: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JsonNode callAPI(String name, boolean isAudiobook) throws Exception {
        rateLimiter.acquire();

        if(access_token == null){
            getAccessToken();
        }

        name = name.replaceAll(" ", "%20");

        String authHeader = "Bearer " + access_token;
        HttpClient httpClient = HttpClient.newBuilder().build();

        String url = String.format(AUDIO_BOOKS_SEARCH_FORMAT, name);

        if (isAudiobook == false) {
            url = String.format(PODCAST_SEARCH_FORMAT, name);
        }
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Authorization", authHeader)
            .build();

        
        
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readValue(responseBody, JsonNode.class);
                
                ArrayNode results = null;
                if (isAudiobook) {
                    results = (ArrayNode) rootNode.get("audiobooks").get("items");
                }
                else {
                    results = (ArrayNode) rootNode.get("episodes").get("items");
                }

                if (results == null || results.size() == 0) {
                    throw new IllegalArgumentException("No result found");
                }
                
                // JsonNode result = results.get(0);
                return results;

            } else if (response.statusCode() == 401 || response.statusCode() == 403){
                getAccessToken();
                callAPI(name, isAudiobook);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
}
