package com.sismics.books.core.model.jpa;

import java.util.Date;

public class Audiobook extends Content {
    private String id;

    private String title;

    private String author;

    private Date duration;

    private String language;

    private Date releaseDate;

    private String description;

    private String subtitle;

    private String audiobookId;

    private String type = "audiobook";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Date getDuration() {
        return duration;
    }

    public void setDuration(Date duration) {
        this.duration = duration;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getAudiobookId() {
        return audiobookId;
    }

    public void setAudiobookId(String audiobookId) {
        this.audiobookId = audiobookId;
    }

    public String getType() {
        return type;
    }

    public String toString() {
        return "Audiobook [id=" + id + ", title=" + title + ", author=" + author + ", duration=" + duration + ", language=" + language + ", releaseDate=" + releaseDate + ", description=" + description + ", subtitle=" + subtitle + ", audiobookId=" + audiobookId + ", type=" + type + "]";
    }
}