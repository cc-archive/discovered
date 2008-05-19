/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.creativecommons.learn.oercloud;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author nathan
 */
@Entity
@Table(name = "sc_tags")
@NamedQueries({@NamedQuery(name = "Tag.findById", query = "SELECT t FROM Tag t WHERE t.id = :id"), @NamedQuery(name = "Tag.findByBookmark", query = "SELECT t FROM Tag t WHERE t.bookmark = :bookmark"), @NamedQuery(name = "Tag.findByTag", query = "SELECT t FROM Tag t WHERE t.tag = :tag")})
public class Tag implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;
    @ManyToOne()
    @JoinColumn(name = "bId", nullable = false)
    private Bookmark bookmark;
    @Column(name = "tag")
    private String tag;
    
    public Tag() {
    }

    public Tag(Integer id) {
        this.id = id;
    }

    public Tag(Integer id, Bookmark bookmark) {
        this.id = id;
        this.bookmark = bookmark;
    }
    
    public Tag(Bookmark bookmark, String tag) {
        this.tag = tag;
        this.bookmark = bookmark;
    }

    public Integer getId() {
        return id;
    }

    public Bookmark getBookmark() {
        return bookmark;
    }
    
    public void setBookmark(Bookmark new_bookmark) {
        this.bookmark = new_bookmark;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
/*
    public int getBId() {
        return bId;
    }

    public void setBId(int bId) {
        this.bId = bId;
    }
*/
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Tag)) {
            return false;
        }
        Tag other = (Tag) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "nbtest.Tag[id=" + id + "]";
    }

}
