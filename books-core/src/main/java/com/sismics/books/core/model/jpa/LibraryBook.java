package com.sismics.books.core.model.jpa;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;

@Entity
@Table(name = "T_LIBRARY_BOOK")
public class LibraryBook implements Serializable, AssociatedBook {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "LBK_ID_C", length = 36)
    private String id;

    @Column(name = "LBK_IDBOOK_C", nullable = false, length = 36)
    private String bookId;

    @Column(name = "LBK_CREATEDATE_D", nullable = false)
    private Date createDate;

    @Column(name = "LBK_DELETEDATE_D", nullable = false)
    private Date deleteDate;

    @Column(name = "LBK_NUMRATINGS_I", nullable = false)
    private int numRatings = 0;

    @Column(name = "LBK_AVGRATING_F", nullable = false)
    private double avgRating = 0.0;

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

    public int getNumRatings() {
        return numRatings;
    }

    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    public void updateAvgRating(int rating) {
        avgRating = (avgRating * numRatings);
        numRatings++;
        avgRating = (avgRating + rating) / numRatings;
    }

    // ! Todo: equal checking with other fields as well
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
        LibraryBook other = (LibraryBook) obj;
        if (bookId == null) {
            if (other.bookId != null) {
                return false;
            }
        } else if (!bookId.equals(other.bookId)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .toString();
    }
}
