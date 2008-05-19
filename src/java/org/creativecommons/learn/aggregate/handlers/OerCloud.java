/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.creativecommons.learn.aggregate.handlers;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndEntry;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.creativecommons.learn.oercloud.Bookmark;
import org.creativecommons.learn.oercloud.ObjectMgr;
import org.creativecommons.learn.oercloud.OerFeed;
import org.creativecommons.learn.oercloud.Tag;

/**
 *
 * @author nathan
 */
public class OerCloud {

    protected Bookmark getBookmark(String url) {
        // return the previously persisted Bookmark instance for this URL

        EntityManager em = ObjectMgr.get().getEm();
        
        Query q = em.createNamedQuery("Bookmark.findByUrl");
        q.setParameter("url", url);
        
        List<Bookmark> result = q.getResultList();
        if (result.size() > 0) {
            return result.get(0);
        }
        
        return null;
        
    } // getBookmark
    
    public void updateEntry(OerFeed feed, SyndEntry entry) {
        
        EntityManager em = ObjectMgr.get().getEm();
        em.getTransaction().begin();
        
        // get the bookmark object
        Bookmark b = getBookmark(entry.getUri());
        if (b == null ) {
            // no existing bookmark
            b = new Bookmark(feed.getUser(), entry.getUri());
            feed.getUser().getBookmarks().add(b);
        }
        b.setTitle(entry.getTitle());
        b.setDescription(entry.getDescription().getValue());
        
        // clear existing tags
        
        // add new tags
        for (Object category : entry.getCategories()) {
            Tag t = new Tag(b, ((SyndCategory)category).getName());
            b.getTags().add(t);
            
        } // for each category
        
        em.getTransaction().commit();
        
    } // updateEntry

} // Cloud