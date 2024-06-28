package com.sismics.books.core.model.jpa;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;

@Entity
@Table(name = "T_LIBRARY_BOOK_RATING")
public class LibraryBookRating implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "LBR_ID_C", length = 36)
    private String id;

    @Id
    @Column(name = "LBR_IDBOOK_C", nullable = false, length = 36)
    private String bookId;

    @Id
    @Column(name = "LBR_IDUSER_C", nullable = false, length = 36)
    private String userId;

    @Column(name = "LBR_RATING_I", nullable = false, length = 36)
    private int rating;

    @Column(name = "LBR_CREATEDATE_D", nullable = false)
    private Date createDate;

    @Column(name = "LBR_DELETEDATE_D")
    private Date deleteDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bookId == null) ? 0 : bookId.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        result = prime * result + rating;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LibraryBookRating other = (LibraryBookRating) obj;
        if (bookId == null) {
            if (other.bookId != null) {
                return false;
            }
        } else if (!bookId.equals(other.bookId)) {
            return false;
        }

        if (userId == null) {
            return other.userId == null;
        } else if (!userId.equals(other.userId)) {
            return false;
        } else {
            return this.rating == other.getRating();
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .toString();
    }
}
