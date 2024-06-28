package com.sismics.books.core.service;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.Date;

import org.codehaus.jackson.JsonNode;

import com.sismics.books.core.model.jpa.Podcast;

public class SpotifyPodcastService extends SpotifyDataService {

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
                    JsonNode podcastNode = callAPI(name, false);
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
    //     podcast.setTitle(result.get("name").getTextValue());
    //     podcast.setArtist(null);

    //     Long duration = result.get("duration_ms").getLongValue();
    //     podcast.setDuration(new Date(duration));
    //     podcast.setReleaseDate(formatter.parseDateTime(result.get("release_date").getTextValue()).toDate());
    //     podcast.setDescription(result.get("description").getTextValue());
    //     podcast.setSubtitle(result.get("language").getTextValue());
    //     podcast.setPodcastId(result.get("id").getTextValue());
    //     podcast.setLanguage(result.get("language").getTextValue());
        
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
        // podcast.setImages(new ArrayList<String>());
        
        return podcast;
    }

}
