/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.creativecommons.learn.oercloud;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author nathan
 */
@Entity
@Table(name = "sc_tagmaps")
@NamedQueries({@NamedQuery(name = "Tagmap.findById", query = "SELECT t FROM Tagmap t WHERE t.id = :id"), @NamedQuery(name = "Tagmap.findByFromTag", query = "SELECT t FROM Tagmap t WHERE t.fromTag = :fromTag"), @NamedQuery(name = "Tagmap.findByToTag", query = "SELECT t FROM Tagmap t WHERE t.toTag = :toTag")})
public class Tagmap implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "fromTag")
    private String fromTag;
    @Column(name = "toTag")
    private String toTag;

    public Tagmap() {
    }

    public Tagmap(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFromTag() {
        return fromTag;
    }

    public void setFromTag(String fromTag) {
        this.fromTag = fromTag;
    }

    public String getToTag() {
        return toTag;
    }

    public void setToTag(String toTag) {
        this.toTag = toTag;
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
        if (!(object instanceof Tagmap)) {
            return false;
        }
        Tagmap other = (Tagmap) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "nbtest.Tagmap[id=" + id + "]";
    }

}
