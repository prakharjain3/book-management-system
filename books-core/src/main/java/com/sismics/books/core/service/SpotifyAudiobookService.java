package com.sismics.books.core.service;

import java.util.UUID;
// import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.Date;

import org.codehaus.jackson.JsonNode;

import com.sismics.books.core.model.jpa.Audiobook;

// import com.sismics.books.core.model.jpa.Audiobook;

public class SpotifyAudiobookService extends SpotifyDataService {

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
                    JsonNode audioBookNode = callAPI(name, true);
                    return audioBookNode;
                    // return buildAudiobook(audioBookNode);
                } catch (Exception e) {
                    throw e;
                }
            }
        };
        FutureTask<JsonNode> futuretask = new FutureTask<JsonNode>(callable);
        executor.execute(futuretask);

        return futuretask.get();
    }

    // private Audiobook buildAudiobook(JsonNode result) {
    //     Audiobook audiobook = new Audiobook();
    //     audiobook.setId(UUID.randomUUID().toString());
    //     audiobook.setTitle(result.get("name").getTextValue());
    //     audiobook.setAuthor(result.get("authors").get(0).getTextValue());
    //     audiobook.setDuration(new Date(result.get("total_chapters").getIntValue()));
    //     audiobook.setReleaseDate(null);
    //     audiobook.setDescription(result.get("description").getTextValue());
    //     audiobook.setSubtitle(result.get("edition").getTextValue());
    //     audiobook.setAudiobookId(result.get("id").getTextValue());
    //     audiobook.setLanguage(result.get("languages").get(0).getTextValue());

    //     return audiobook;
    // }

    public Audiobook buildAudiobook(String title, String author, int duration, String description, String subtitle, String language, String id, String imageUrl, String date) {
        Audiobook audiobook = new Audiobook();
        audiobook.setId(UUID.randomUUID().toString());        
        audiobook.setTitle(title);
        audiobook.setAuthor(author);
        audiobook.setDuration(new Date(duration));
        audiobook.setDescription(description);
        audiobook.setSubtitle(subtitle);
        audiobook.setLanguage(language);
        audiobook.setAudiobookId(id);
        audiobook.setReleaseDate(null);
        
        return audiobook;
    }
}
