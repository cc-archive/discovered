/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.creativecommons.learn.oercloud;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.creativecommons.learn.aggregate.feed.OaiPmh;
import org.creativecommons.learn.aggregate.feed.Opml;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 *
 * @author nathan
 */
@Entity
@Table(name = "oer_feeds")
@NamedQueries({
    @NamedQuery(name = "OerFeed.findById", query = "SELECT o FROM OerFeed o WHERE o.id = :id"), 
    @NamedQuery(name = "OerFeed.findByUrl", query = "SELECT o FROM OerFeed o WHERE o.url = :url"), 
    @NamedQuery(name = "OerFeed.findByLastImport", query = "SELECT o FROM OerFeed o WHERE o.lastImport = :lastImport"), 
    @NamedQuery(name = "OerFeed.findByFeedType", query = "SELECT o FROM OerFeed o WHERE o.feedType = :feedType")
})
public class OerFeed implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "url")
    private String url;
    @Column(name = "last_import")
    private BigInteger lastImport;
    @Column(name = "feed_type")
    private String feedType;

    @ManyToOne()
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    public OerFeed() {
        this.lastImport = BigInteger.valueOf(0);
    }

    public OerFeed(Long id) {
        this.lastImport = BigInteger.valueOf(0);
        this.id = id;
    }
   
    public Long getId() {
        return id;
    }

    public void poll() throws IOException {
        // get the contents of the feed and emit events for each
        
        // OPML
        if (this.getFeedType().toLowerCase().equals("opml")) {
            
            new Opml().poll(this);
            
        } else {
            try {
                SyndFeedInput input = new SyndFeedInput();
                URLConnection feed_connection = new URL(this.getUrl()).openConnection();
                feed_connection.setConnectTimeout(30000);
                feed_connection.setReadTimeout(60000);
                
                SyndFeed feed = input.build(new XmlReader(feed_connection));

                List<SyndEntry> feed_entries = feed.getEntries();

                for (SyndEntry entry : feed_entries) {

                    // emit an event with the entry information
                    ObjectMgr.get().updateEntry(this, entry);

                } // for each entry
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(OerFeed.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FeedException ex) {
                // maybe OAI-PMH?
                new OaiPmh().poll(this);
                // XXX still need to log feed errors if it's not OAI-PMH
                Logger.getLogger(OerFeed.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } // not opml...
    } // poll
    
    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getLastImportDate() {
        return new Date(this.getLastImport().intValue() * 1000);
    }
    
    public void setLastImportDate(Date lastImportDate) {
        this.setLastImport(BigInteger.valueOf(
                BigInteger.valueOf(lastImportDate.getTime()).intValue() / 1000
                ));
    }
    
    public BigInteger getLastImport() {
        return lastImport;
    }

    public void setLastImport(BigInteger lastImport) {
        this.lastImport = lastImport;
    }

    public String getFeedType() {
        return feedType;
    }

    public void setFeedType(String feedType) {
        this.feedType = feedType;
    }

    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
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
        if (!(object instanceof OerFeed)) {
            return false;
        }
        OerFeed other = (OerFeed) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "nbtest.OerFeed[id=" + id + "]";
    }

}
