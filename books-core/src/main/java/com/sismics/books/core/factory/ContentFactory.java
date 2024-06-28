package com.sismics.books.core.factory;

// import com.sismics.books.core.model.jpa.Content;
import com.sismics.books.core.model.jpa.Audiobook;
import com.sismics.books.core.model.jpa.Podcast;

// import com.sismics.books.core.model.jpa.Podcast;
import org.codehaus.jackson.JsonNode;

public interface ContentFactory {
    	
	/*
	 * Create a new audiobook
	 */
    public JsonNode searchAudiobook(String name) throws Exception;

	public Audiobook buildAudiobookFromNull(String title, String author, int duration, String description, String subtitle, String language, String id, String imageUrl, String date);
	
	/*
	 * Create a new podcast
	 */
    public JsonNode searchPodcast(String name) throws Exception;

	public Podcast buildPodcastFromNull(String title, String artist, int duration, String date, String description, String subtitle, String language, String id, String imageUrl);

	

}
