/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.creativecommons.learn.oercloud;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
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
@Table(name = "sc_users")
@NamedQueries({@NamedQuery(name = "User.findByUId", query = "SELECT u FROM User u WHERE u.uId = :uId"), @NamedQuery(name = "User.findByUsername", query = "SELECT u FROM User u WHERE u.username = :username"), @NamedQuery(name = "User.findByPassword", query = "SELECT u FROM User u WHERE u.password = :password"), @NamedQuery(name = "User.findByUDatetime", query = "SELECT u FROM User u WHERE u.uDatetime = :uDatetime"), @NamedQuery(name = "User.findByUModified", query = "SELECT u FROM User u WHERE u.uModified = :uModified"), @NamedQuery(name = "User.findByName", query = "SELECT u FROM User u WHERE u.name = :name"), @NamedQuery(name = "User.findByEmail", query = "SELECT u FROM User u WHERE u.email = :email"), @NamedQuery(name = "User.findByHomepage", query = "SELECT u FROM User u WHERE u.homepage = :homepage"), @NamedQuery(name = "User.findByUIp", query = "SELECT u FROM User u WHERE u.uIp = :uIp"), @NamedQuery(name = "User.findByUStatus", query = "SELECT u FROM User u WHERE u.uStatus = :uStatus"), @NamedQuery(name = "User.findByIsFlagged", query = "SELECT u FROM User u WHERE u.isFlagged = :isFlagged"), @NamedQuery(name = "User.findByIsAdmin", query = "SELECT u FROM User u WHERE u.isAdmin = :isAdmin"), @NamedQuery(name = "User.findByActivationKey", query = "SELECT u FROM User u WHERE u.activationKey = :activationKey")})
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "uId", nullable = false)
    private Integer uId;
    @Column(name = "username")
    private String username;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "uDatetime", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date uDatetime;
    @Column(name = "uModified", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date uModified;
    @Column(name = "name")
    private String name;
    @Column(name = "email", nullable = false)
    private String email;
    @Column(name = "homepage")
    private String homepage;
    @Lob
    @Column(name = "uContent")
    private String uContent;
    @Column(name = "uIp")
    private String uIp;
    @Column(name = "uStatus", nullable = false)
    private boolean uStatus;
    @Column(name = "isFlagged", nullable = false)
    private boolean isFlagged;
    @Column(name = "isAdmin", nullable = false)
    private boolean isAdmin;
    @Column(name = "activation_key")
    private String activationKey;
    
    @OneToMany(mappedBy="user", cascade=ALL)
    private Collection<Bookmark> bookmarks;  
    @OneToMany(mappedBy="user", cascade=ALL)
    private Collection<OerFeed> feeds;

    public User() {
    }

    public User(Integer uId) {
        this.uId = uId;
    }

    public User(Integer uId, String password, Date uDatetime, Date uModified, String email, boolean uStatus, boolean isFlagged, boolean isAdmin) {
        this.uId = uId;
        this.password = password;
        this.uDatetime = uDatetime;
        this.uModified = uModified;
        this.email = email;
        this.uStatus = uStatus;
        this.isFlagged = isFlagged;
        this.isAdmin = isAdmin;
    }

    public Integer getUId() {
        return uId;
    }

    public void setUId(Integer uId) {
        this.uId = uId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getUDatetime() {
        return uDatetime;
    }

    public void setUDatetime(Date uDatetime) {
        this.uDatetime = uDatetime;
    }

    public Date getUModified() {
        return uModified;
    }

    public void setUModified(Date uModified) {
        this.uModified = uModified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getUContent() {
        return uContent;
    }

    public void setUContent(String uContent) {
        this.uContent = uContent;
    }

    public String getUIp() {
        return uIp;
    }

    public void setUIp(String uIp) {
        this.uIp = uIp;
    }

    public boolean getUStatus() {
        return uStatus;
    }

    public void setUStatus(boolean uStatus) {
        this.uStatus = uStatus;
    }

    public boolean getIsFlagged() {
        return isFlagged;
    }

    public void setIsFlagged(boolean isFlagged) {
        this.isFlagged = isFlagged;
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }
    
    public Collection<Bookmark> getBookmarks() {
        return this.bookmarks;
    }
    
    public Collection<OerFeed> getFeeds() {
        return this.feeds;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (uId != null ? uId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof User)) {
            return false;
        }
        User other = (User) object;
        if ((this.uId == null && other.uId != null) || (this.uId != null && !this.uId.equals(other.uId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "nbtest.User[uId=" + uId + "]";
    }

}
