/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.creativecommons.learn.oercloud;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import static javax.persistence.CascadeType.*;

/**
 *
 * @author nathan
 */
@Entity
@Table(name = "sc_bookmarks")
@NamedQueries({
    @NamedQuery(name = "Bookmark.findByUrl", query = "SELECT b FROM Bookmark b WHERE b.bAddress = :url"),
    @NamedQuery(name = "Bookmark.findByBId", query = "SELECT b FROM Bookmark b WHERE b.bId = :bId"), 
    @NamedQuery(name = "Bookmark.findByUser", query = "SELECT b FROM Bookmark b WHERE b.user = :user"), 
    @NamedQuery(name = "Bookmark.findByBIp", query = "SELECT b FROM Bookmark b WHERE b.bIp = :bIp"), 
    @NamedQuery(name = "Bookmark.findByBStatus", query = "SELECT b FROM Bookmark b WHERE b.bStatus = :bStatus"), 
    @NamedQuery(name = "Bookmark.findByBDatetime", query = "SELECT b FROM Bookmark b WHERE b.bDatetime = :bDatetime"), 
    @NamedQuery(name = "Bookmark.findByBModified", query = "SELECT b FROM Bookmark b WHERE b.bModified = :bModified"), 
    @NamedQuery(name = "Bookmark.findByBHash", query = "SELECT b FROM Bookmark b WHERE b.bHash = :bHash"), 
    @NamedQuery(name = "Bookmark.findByBFlagCount", query = "SELECT b FROM Bookmark b WHERE b.bFlagCount = :bFlagCount")
})
public class Bookmark implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "bId", nullable = false)
    private Integer bId;
    @Column(name = "bIp")
    private String bIp;
    @Column(name = "bStatus", nullable = false)
    private boolean bStatus;
    @Column(name = "bDatetime", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date bDatetime;
    @Column(name = "bModified", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date bModified;
    @Lob
    @Column(name = "bTitle")
    private String bTitle;
    @Lob
    @Column(name = "bAddress", nullable = false)
    private String bAddress;
    @Lob
    @Column(name = "bDescription")
    private String bDescription;
    @Column(name = "bHash", nullable = false)
    private String bHash;
    @Column(name = "bFlagCount")
    private Short bFlagCount;
    @Lob
    @Column(name = "bFlaggedBy")
    private String bFlaggedBy;
    
    @ManyToOne()
    @JoinColumn(name = "uId", nullable = false)
    private User user;
    @OneToMany(mappedBy="bookmark", cascade=ALL)
    private Collection<Tag> tags;

    public Bookmark() {
        this.bDatetime = new Date();
        this.bModified = new Date();
    }

    public Bookmark(Integer bId) {
        this.bDatetime = new Date();
        this.bModified = new Date();

        this.bId = bId;
    }

    public Bookmark(User user, String uri) {
        this.bDatetime = new Date();
        this.bModified = new Date();

        this.user = user;
        this.setUrl(uri);    
    }

    public Bookmark(Integer bId, User user, boolean bStatus, Date bDatetime, Date bModified, String bAddress) {
        this.bId = bId;
        this.user = user;
        this.bStatus = bStatus;
        this.bDatetime = bDatetime;
        this.bModified = bModified;
        this.setUrl(bAddress);
    }

    public Integer getId() {
        return bId;
    }

    public void setId(Integer bId) {
        this.bId = bId;
    }

    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }

    public String getIp() {
        return bIp;
    }

    public void setIp(String bIp) {
        this.bIp = bIp;
    }

    public boolean getStatus() {
        return bStatus;
    }

    public void setStatus(boolean bStatus) {
        this.bStatus = bStatus;
    }

    public Date getDatetime() {
        return bDatetime;
    }

    public void setDatetime(Date bDatetime) {
        this.bDatetime = bDatetime;
    }

    public Date getModified() {
        return bModified;
    }

    public void setModified(Date bModified) {
        this.bModified = bModified;
    }

    public String getTitle() {
        return bTitle;
    }

    public void setTitle(String bTitle) {
        this.bTitle = bTitle;
    }

    public String getUrl() {
        return bAddress;
    }

    public void setUrl(String bAddress) {
        this.bAddress = bAddress;
        this.updateHash();
    }

    public String getDescription() {
        return bDescription;
    }

    public void setDescription(String bDescription) {
        this.bDescription = bDescription;
    }
    
    public Collection<Tag> getTags() {
        return this.tags;
    }
    
    public String getHash() {
        return bHash;
    }

    public void setHash(String bHash) {
        this.bHash = bHash;
    }

    public Short getFlagCount() {
        return bFlagCount;
    }

    public void setFlagCount(Short bFlagCount) {
        this.bFlagCount = bFlagCount;
    }

    public String getFlaggedBy() {
        return bFlaggedBy;
    }

    public void setFlaggedBy(String bFlaggedBy) {
        this.bFlaggedBy = bFlaggedBy;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (bId != null ? bId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Bookmark)) {
            return false;
        }
        Bookmark other = (Bookmark) object;
        if ((this.bId == null && other.bId != null) || (this.bId != null && !this.bId.equals(other.bId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "nbtest.Bookmark[bId=" + bId + "]";
    }

    private void updateHash() {
        try {
            // Recalculate the hash
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(this.getUrl().getBytes(), 0, this.getUrl().length());
            this.setHash(new BigInteger(1, m.digest()).toString(16));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Bookmark.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    } // updateHash

}
