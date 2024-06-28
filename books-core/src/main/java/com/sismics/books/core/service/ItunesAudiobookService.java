package com.sismics.books.core.service;

import com.sismics.books.core.model.jpa.Audiobook;

import java.util.concurrent.FutureTask;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.codehaus.jackson.JsonNode;

public class ItunesAudiobookService extends ItunesDataService {

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
                    JsonNode audiobookNode =  callAPI(name, true);
                    return audiobookNode;
                    // return buildAudiobook(audiobookNode);
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
	// 	audiobook.setId(UUID.randomUUID().toString());
	// 	audiobook.setTitle(result.get("collectionName").getTextValue());
	// 	audiobook.setSubtitle(result.get("primaryGenreName").getTextValue());
	// 	audiobook.setAuthor(result.get("artistName").getTextValue());
	// 	audiobook.setDescription(result.get("description").getTextValue());
	// 	audiobook.setDuration(new Date(1000 * 60 * 60));
	// 	audiobook.setAudiobookId(result.get("collectionId").getTextValue());
	// 	audiobook.setReleaseDate(formatter.parseDateTime(result.get("publishedDate").getTextValue()).toDate());
    //     audiobook.setLanguage(result.get("country").getTextValue());
		
	// 	return audiobook;
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
        audiobook.setReleaseDate(formatter.parseDateTime(date).toDate());

        return audiobook;
    }

    // TODO Download Thumbnail
}
