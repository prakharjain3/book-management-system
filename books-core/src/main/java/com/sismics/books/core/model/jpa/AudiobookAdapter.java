package com.sismics.books.core.model.jpa;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("audiobook")
public class AudiobookAdapter extends Book {

    @Transient
    private Audiobook audiobook;

    public AudiobookAdapter(Audiobook audiobook) {
        this.audiobook = audiobook;
        super.setId(audiobook.getId());
        super.setTitle(audiobook.getTitle());
        super.setSubtitle(audiobook.getSubtitle());
        super.setAuthor(audiobook.getAuthor());
        super.setDescription(audiobook.getDescription());
        super.setIsbn10(null);
        super.setIsbn13(null);
        super.setPageCount(audiobook.getDuration().getTime());
        super.setLanguage(audiobook.getLanguage());
        super.setPublishDate(audiobook.getReleaseDate());
        // super.setType("book");      
    }

    public AudiobookAdapter() {
        this.audiobook = new Audiobook();
    }

    /**
     * Getter of id.
     * 
     * @return id
     */
    @Override
    public String getId() {
        return audiobook.getId();
    }

    /**
     * Setter of id.
     * 
     * @param id id
     */
    @Override
    public void setId(String id) {
        this.audiobook.setId(id);
    }

    /**
     * Getter of title.
     * 
     * @return title
     */
    @Override
    public String getTitle() {
        return audiobook.getTitle();
    }

    /**
     * Setter of title.
     * 
     * @param title title
     */
    @Override
    public void setTitle(String title) {
        this.audiobook.setTitle(title);
    }

    /**
     * Getter of subtitle.
     * 
     * @return subtitle
     */
    @Override
    public String getSubtitle() {
        return audiobook.getSubtitle();
    }

    /**
     * Setter of subtitle.
     * 
     * @param subtitle subtitle
     */
    @Override
    public void setSubtitle(String subtitle) {
        this.audiobook.setSubtitle(subtitle);
    }

    /**
     * Getter of author.
     * 
     * @return author
     */
    @Override
    public String getAuthor() {
        return this.audiobook.getAuthor();
    }

    /**
     * Setter of author.
     * 
     * @param author author
     */
    @Override
    public void setAuthor(String author) {
        this.audiobook.setAuthor(author);
    }

    /**
     * Getter of description.
     * 
     * @return description
     */
    @Override
    public String getDescription() {
        return this.audiobook.getDescription();
    }

    /**
     * Setter of description.
     * 
     * @param description description
     */
    @Override
    public void setDescription(String description) {
        this.audiobook.setDescription(description);
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
     * Getter of isbn10.
     * 
     * @return isbn10
     */
    @Override
    public String getIsbn10() {
        return this.audiobook.getAudiobookId();
    }

    /**
     * Setter of isbn10.
     * 
     * @param isbn10 isbn10
     */
    @Override
    public void setIsbn10(String isbn10) {
        this.audiobook.setAudiobookId(isbn10);
    }

    /**
     * Getter of pageCount.
     * 
     * @return pageCount
     */
    @Override
    public Long getPageCount() {
        return this.audiobook.getDuration().getTime();
    }

    /**
     * Setter of pageCount.
     * 
     * @param pageCount pageCount
     */
    @Override
    public void setPageCount(Long duration) {
        this.audiobook.setDuration(new Date(duration));
    }

    /**
     * Getter of language.
     * 
     * @return language
     */
    @Override
    public String getLanguage() {
        return audiobook.getLanguage();
    }

    /**
     * Setter of language.
     * 
     * @param language language
     */
    @Override
    public void setLanguage(String language) {
        this.audiobook.setLanguage(language);
    }

    /**
     * Getter of publishDate.
     * 
     * @return publishDate
     */
    @Override
    public Date getPublishDate() {
        return audiobook.getReleaseDate();
    }

    /**
     * Setter of publishDate.
     * 
     * @param publishedDate publishDate
     */
    @Override
    public void setPublishDate(Date publishDate) {
        this.audiobook.setReleaseDate(publishDate);
    }

    /**
     * Getter of type.
     * 
     * @return type
     */
    // @Override
    // public String getType() {
    //     return audiobook.getType();
    // }

    public String toString() {
        return "AudiobookAdapter [audiobook=" + audiobook.getId() + "]";
    }
}
