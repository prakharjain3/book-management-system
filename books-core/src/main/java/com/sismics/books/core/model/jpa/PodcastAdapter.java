package com.sismics.books.core.model.jpa;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import java.util.Date;

@Entity
@DiscriminatorValue("podcast")
public class PodcastAdapter extends Book {

    @Transient
    private Podcast podcast;

    public PodcastAdapter(Podcast podcast) {
        this.podcast = podcast;
        super.setId(podcast.getId());
        super.setTitle(podcast.getTitle());
        super.setSubtitle(podcast.getSubtitle());
        super.setAuthor(podcast.getArtist());
        super.setDescription(podcast.getDescription());
        super.setIsbn10(null);
        super.setIsbn13(null);
        super.setPageCount(podcast.getDuration().getTime());
        super.setLanguage(podcast.getLanguage());
        super.setPublishDate(podcast.getReleaseDate());
    }

    public PodcastAdapter() {
        this.podcast = new Podcast();
    }

    /**
     * Getter of id.
     * 
     * @return id
     */
    @Override
    public String getId() {
        return this.podcast.getId();
    }

    /**
     * Setter of id.
     * 
     * @param id id
     */
    @Override
    public void setId(String id) {
        this.podcast.setId(id);
    }

    /**
     * Getter of title.
     * 
     * @return title
     */
    @Override
    public String getTitle() {
        return this.podcast.getTitle();
    }

    /**
     * Setter of title.
     * 
     * @param title title
     */
    @Override
    public void setTitle(String title) {
        this.podcast.setTitle(title);
    }

    /**
     * Getter of subtitle.
     * 
     * @return subtitle
     */
    @Override
    public String getSubtitle() {
        return this.podcast.getSubtitle();
    }

    /**
     * Setter of subtitle.
     * 
     * @param subtitle subtitle
     */
    @Override
    public void setSubtitle(String subtitle) {
        this.podcast.setSubtitle(subtitle);
    }

    /**
     * Getter of author.
     * 
     * @return author
     */
    @Override
    public String getAuthor() {
        return this.podcast.getArtist();
    }

    /**
     * Setter of author.
     * 
     * @param author author
     */
    @Override
    public void setAuthor(String author) {
        this.podcast.setArtist(author);
    }

    /**
     * Getter of description.
     * 
     * @return description
     */
    @Override
    public String getDescription() {
        return this.podcast.getDescription();
    }

    /**
     * Setter of description.
     * 
     * @param description description
     */
    @Override
    public void setDescription(String description) {
        this.podcast.setDescription(description);
    }

    /**
     * Getter of isbn10.
     * 
     * @return isbn10
     */
    @Override
    public String getIsbn10() {
        return this.podcast.getPodcastId();
    }

    /**
     * Setter of isbn10.
     * 
     * @param isbn10 isbn10
     */
    @Override
    public void setIsbn10(String isbn10) {
        this.podcast.setPodcastId(isbn10);
    }

    /**
     * Getter of isbn13.
     * 
     * @return isbn13
     */
    @Override
    public String getIsbn13() {
        return null;
    }

    /**
     * Setter of isbn13.
     * 
     * @param isbn13 isbn13
     */
    @Override
    public void setIsbn13(String isbn13) {
        return;
    }

    /**
     * Getter of pageCount.
     * 
     * @return pageCount
     */
    @Override
    public Long getPageCount() {
        return this.podcast.getDuration().getTime();
    }

    /**
     * Setter of pageCount.
     * 
     * @param pageCount pageCount
     */
    @Override
    public void setPageCount(Long pageCount) {
        this.podcast.setDuration(new Date(pageCount));
    }

    /**
     * Getter of language.
     * 
     * @return language
     */
    @Override
    public String getLanguage() {
        return this.podcast.getLanguage();
    }

    /**
     * Setter of language.
     * 
     * @param language language
     */
    @Override
    public void setLanguage(String language) {
        this.podcast.setLanguage(language);
    }

    /**
     * Getter of publishDate.
     * 
     * @return publishDate
     */
    @Override
    public Date getPublishDate() {
        return this.podcast.getReleaseDate();
    }

    /**
     * Setter of publishDate.
     * 
     * @param publishedDate publishDate
     */
    @Override
    public void setPublishDate(Date publishDate) {
        this.podcast.setReleaseDate(publishDate);
    }

    /**
     * Getter of type.
     * 
     * @return type
     */
    public String getType() {
        return this.podcast.getType();
    }

}
