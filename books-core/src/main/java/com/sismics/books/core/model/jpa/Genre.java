package com.sismics.books.core.model.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;

@Entity
@Table(name = "T_GENRE")
public class Genre implements Serializable {
    @Id
    @Column(name = "GNR_ID_C", length = 36)
    private String id;

    @Column(name = "GNR_NAME_C", nullable = false, length = 255)
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .toString();
    }
}
