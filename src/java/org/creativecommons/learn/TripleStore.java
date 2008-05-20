package org.creativecommons.learn;

import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.creativecommons.learn.oercloud.OerFeed;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 *
 * @author nathan
 */
public class TripleStore {

    private static IDBConnection conn = null;
    private static ModelMaker maker = null;
    
    private static void open() throws ClassNotFoundException {
    	
        String className = "com.mysql.jdbc.Driver";         // path of driver class
        Class.forName (className);                          // Load the Driver
        String DB_URL =     "jdbc:mysql://localhost/oercloud";  // URL of database 
        String DB_USER =   "root";                          // database user id
        String DB_PASSWD = "";                          // database password
        String DB =        "MySQL";                         // database type

        // Create database connection
        conn = new DBConnection ( DB_URL, DB_USER, DB_PASSWD, DB );
        maker = ModelFactory.createModelRDBMaker(conn) ;
    	
    } // open
    
    private static void close() {
        try {
            // Close the database connection
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(TripleStore.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    } // close

    public static Model getModel() throws ClassNotFoundException {

    	if (maker == null) {
    		TripleStore.open();
    	}
    	
        // create or open the default model
        return maker.createDefaultModel();

    } // getModel

} // TripleStore
