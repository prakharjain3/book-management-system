package com.sismics.books.core.factory;

// import com.sismics.books.core.model.jpa.Content;
import com.sismics.books.core.model.jpa.Audiobook;
import com.sismics.books.core.model.jpa.Podcast;
import com.sismics.books.core.model.context.AppContext;
import org.codehaus.jackson.JsonNode;
public class ItunesFactory implements ContentFactory {

    @Override
    public JsonNode searchAudiobook(String name) throws Exception{
      return AppContext.getInstance().getItunesAudiobookService().search(name);
    }

    @Override
    public Audiobook buildAudiobookFromNull(String title, String author, int duration, String description, String subtitle, String language, String id, String imageUrl, String date){
      return AppContext.getInstance().getItunesAudiobookService().buildAudiobook(title, author, duration, description, subtitle, language, id, imageUrl, date);
    }

    @Override
    public JsonNode searchPodcast(String name) throws Exception{
      return AppContext.getInstance().getItunesPodcastService().search(name);
    }

    @Override
    public Podcast buildPodcastFromNull(String title, String artist, int duration, String date, String description, String subtitle, String language, String id, String imageUrl){
      return AppContext.getInstance().getItunesPodcastService().buildPodcast(title, artist, duration, date, description, subtitle, language, id, imageUrl);
    }
}