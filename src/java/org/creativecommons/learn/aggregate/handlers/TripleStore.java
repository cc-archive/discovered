/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.creativecommons.learn.aggregate.handlers;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndEntry;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.creativecommons.learn.oercloud.OerFeed;

/**
 *
 * @author nathan
 */
public class TripleStore {

    IDBConnection conn;
    
    public Model getModel() throws ClassNotFoundException {
        String className = "com.mysql.jdbc.Driver";         // path of driver class
        Class.forName (className);                          // Load the Driver
        String DB_URL =     "jdbc:mysql://localhost/oercloud";  // URL of database 
        String DB_USER =   "root";                          // database user id
        String DB_PASSWD = "";                          // database password
        String DB =        "MySQL";                         // database type

        // Create database connection
        conn = new DBConnection ( DB_URL, DB_USER, DB_PASSWD, DB );
        ModelMaker maker = ModelFactory.createModelRDBMaker(conn) ;

        // create or open the default model
        Model model = maker.createDefaultModel();

        return model;
    } // getModel
    
    public void close() {
        try {
            // Close the database connection
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(TripleStore.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    } // close
    
    public void updateEntry(OerFeed feed, SyndEntry entry) {
        try {
            Model model = this.getModel();

            // add the basic assertions about the resource (title, source, etc)
            Resource res = model.createResource(entry.getUri());
            model.add(res, DC.title, model.createLiteral(entry.getTitle()));
            model.add(res, RDF.type, model.createResource("http://learn.creativecommons.org/ns#Resource"));
            model.add(res, DC.contributor, model.createResource(feed.getUrl()));
            model.add(res, DC.description, 
                    model.createLiteral(entry.getDescription().getValue()));
                        
            // add categories, mapped to dc:subject
            for (Object category : entry.getCategories()) {
                model.add(res, DC.subject, 
                        model.createLiteral( ((SyndCategory)category).getName() ));
            } // for each category
            
            // close the connection
            this.close();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TripleStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    } // updateEntry

} // TripleStore
