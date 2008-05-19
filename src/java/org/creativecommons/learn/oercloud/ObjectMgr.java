/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.creativecommons.learn.oercloud;

import org.creativecommons.learn.aggregate.handlers.TripleStore;
import org.creativecommons.learn.aggregate.handlers.OerCloud;
import com.sun.syndication.feed.synd.SyndEntry;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 *
 * @author nathan
 */
public class ObjectMgr {
    
    private static ObjectMgr mgr = new ObjectMgr();
    private OerCloud oercloud = new OerCloud();
    private TripleStore triplestore = new TripleStore();
    
    private EntityManagerFactory emf;
    private EntityManager em;

    private ObjectMgr() {
        emf = Persistence.createEntityManagerFactory("OerCloudPU");
        em = emf.createEntityManager();
    }
    
    public static ObjectMgr get() {
        return mgr;
    } // get
    
    public EntityManager getEm() {
        return em;
    }
    
    // Registration/Dispatch methods    
    public void updateEntry(OerFeed feed, SyndEntry entry) {
        // XXX: Right now this just serves as a single point of dispatch
        // for our handlers (oer cloud, triple store, etc).  In the future
        // it should probably be significantly less tightly coupled
        this.oercloud.updateEntry(feed, entry);
        this.triplestore.updateEntry(feed, entry);
        
    } // updateEntry

    public List<OerFeed> getAllFeeds() {
        Query q = em.createQuery("select f from OerFeed f");
        return q.getResultList();
    }

    public OerFeed feedByUrl(String url) {
               
        Query q = this.getEm().createNamedQuery("OerFeed.findByUrl");
        q.setParameter("url", url);
        
        List<OerFeed> result = q.getResultList();
        if (result.size() > 0) {
            return result.get(0);
        }
        
        return null;
        
    } // feedByUrl

	public Bookmark getBookmark(String bookmark_url) {
		
        Query q = this.getEm().createNamedQuery("Bookmark.findByUrl");
        q.setParameter("url", bookmark_url);
        
        List<Bookmark> result = q.getResultList();
        if (result.size() > 0) {
            return result.get(0);
        }
        
        return null;
	} // getBookmark

} // ObjectMgr
