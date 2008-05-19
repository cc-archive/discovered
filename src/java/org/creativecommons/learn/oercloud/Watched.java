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
@Table(name = "sc_watched")
@NamedQueries({@NamedQuery(name = "Watched.findByWId", query = "SELECT w FROM Watched w WHERE w.wId = :wId"), @NamedQuery(name = "Watched.findByUId", query = "SELECT w FROM Watched w WHERE w.uId = :uId"), @NamedQuery(name = "Watched.findByWatched", query = "SELECT w FROM Watched w WHERE w.watched = :watched")})
public class Watched implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "wId", nullable = false)
    private Integer wId;
    @Column(name = "uId", nullable = false)
    private int uId;
    @Column(name = "watched", nullable = false)
    private int watched;

    public Watched() {
    }

    public Watched(Integer wId) {
        this.wId = wId;
    }

    public Watched(Integer wId, int uId, int watched) {
        this.wId = wId;
        this.uId = uId;
        this.watched = watched;
    }

    public Integer getWId() {
        return wId;
    }

    public void setWId(Integer wId) {
        this.wId = wId;
    }

    public int getUId() {
        return uId;
    }

    public void setUId(int uId) {
        this.uId = uId;
    }

    public int getWatched() {
        return watched;
    }

    public void setWatched(int watched) {
        this.watched = watched;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (wId != null ? wId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Watched)) {
            return false;
        }
        Watched other = (Watched) object;
        if ((this.wId == null && other.wId != null) || (this.wId != null && !this.wId.equals(other.wId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "nbtest.Watched[wId=" + wId + "]";
    }

}
