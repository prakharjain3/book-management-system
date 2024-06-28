package com.sismics.books.core.service;

import java.util.concurrent.FutureTask;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.codehaus.jackson.JsonNode;


import com.sismics.books.core.model.jpa.Podcast;


public class ItunesPodcastService extends ItunesDataService {

    public JsonNode search(String rawName) throws Exception {
        
        final String name = rawName;

        // validate name
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }

        Callable<JsonNode> callable = new Callable<JsonNode>() {
            @Override
            public JsonNode call() throws Exception {
                try {
                    JsonNode podcastNode =  callAPI(name, false);
                    return podcastNode;
                    // return buildPodcast(podcastNode);
                } catch (Exception e) {
                    throw e;
                }
            }
        };
        FutureTask<JsonNode> futuretask = new FutureTask<JsonNode>(callable);
        executor.execute(futuretask);

        return futuretask.get();
    }

    // private Podcast buildPodcast(JsonNode result) {

    //     Podcast podcast = new Podcast();
    //     podcast.setId(UUID.randomUUID().toString());
    //     podcast.setTitle(result.get("collectionName").getTextValue());
    //     podcast.setArtist(result.get("artistName").getTextValue());

    //     Long duration = result.get("trackTimeMillis").getLongValue();
    //     podcast.setDuration(new Date(duration));
    //     podcast.setReleaseDate(formatter.parseDateTime(result.get("publishedDate").getTextValue()).toDate());

    //     ArrayList<String> genresList = new ArrayList<String>();
    //     genresList.add(result.get("genres").getTextValue());
    //     podcast.setDescription(String.join(", ", genresList));

    //     podcast.setSubtitle(result.get("primaryGenreName").getTextValue());
    //     podcast.setPodcastId(result.get("collectionId").getTextValue());
    //     podcast.setLanguage(result.get("country").getTextValue());
    //     return podcast;
        
    // }

    public Podcast buildPodcast(String title, String artist, int duration, String date, String description, String subtitle, String language, String id, String imageUrl) {
        Podcast podcast = new Podcast();
        podcast.setId(UUID.randomUUID().toString());
        podcast.setTitle(title);
        podcast.setArtist(artist);
        podcast.setDuration(new Date(duration));
        podcast.setReleaseDate(formatter.parseDateTime(date).toDate());
        podcast.setDescription(description);
        podcast.setSubtitle(subtitle);
        podcast.setPodcastId(id);
        podcast.setLanguage(language);
        
        return podcast;
    }
  
}
