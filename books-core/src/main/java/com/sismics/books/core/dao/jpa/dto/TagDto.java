package com.sismics.books.core.dao.jpa.dto;

import javax.persistence.Id;

/**
 * Tag DTO.
 *
 * @author bgamard 
 */
public class TagDto {
    /**
     * Tag ID.
     */
    @Id
    private String id;
    
    /**
     * Name.
     */
    private String name;
    
    /**
     * Color.
     */
    private String color;

    /**
     * Visibility
     */
    private String visibility;

    /**
     * Getter of id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter of id.
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter of name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter of name.
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter of color.
     *
     * @return the color
     */
    public String getColor() {
        return color;
    }

    /**
     * Getter of visibility.
     * @return visibility
     */
    public String getVisibility() {
        return visibility;
    }

    /**
     * Setter of visibility.
     * @param visibility visibility
     */
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    /**
     * Setter of color.
     *
     * @param color color
     */
    public void setColor(String color) {
        this.color = color;
    }
}
