package org.creativecommons.learn;

import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import thewebsemantic.Bean2RDF;
import thewebsemantic.Filler;
import thewebsemantic.NotFoundException;
import thewebsemantic.RDF2Bean;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 *
 * @author nathan
 */
public class TripleStore {

	private static TripleStore instance = null;
	
    private IDBConnection conn = null;
    private ModelMaker maker = null;
    private Model model = null;
    
    private RDF2Bean loader = null;
    private Bean2RDF saver = null;
    
    private TripleStore() {
    	// private constructor
    	super();
    	
    	try {
        	this.loader = new RDF2Bean(this.getModel());
			this.saver = new Bean2RDF(this.getModel());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static TripleStore get() {
    	
    	if (instance == null) {
    		instance = new TripleStore();
    	}
    	
    	return instance;
    }
    
    private void open() throws ClassNotFoundException {
    	
        String className = "com.mysql.jdbc.Driver";         // path of driver class
        Class.forName (className);                          // Load the Driver
        String DB_URL =     "jdbc:mysql://localhost/oercloud?autoReconnect=true";  // URL of database 
        String DB_USER =   "discovered";                          // database user id
        String DB_PASSWD = "";                          // database password
        String DB =        "MySQL";                         // database type

        // Create database connection
        conn = new DBConnection ( DB_URL, DB_USER, DB_PASSWD, DB );
        maker = ModelFactory.createModelRDBMaker(conn) ;
    	
    } // open
 
    private void close() {
        try {
            // Close the database connection
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(TripleStore.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    } // close

    public Model getModel() throws ClassNotFoundException {

    	if (maker == null) {
    		this.open();
    	}
 
    	if (model == null) {
            // create or open the default model
    		this.model = maker.createDefaultModel();
    	}
    	
        return this.model;

    } // getModel
    

    /* Delegate Methods */
    /* **************** */
    
	public boolean exists(Class<?> c, String id) {
		return loader.exists(c, id);
	}

	public boolean exists(String uri) {
		return loader.exists(uri);
	}

	public void fill(Object o, String propertyName) {
		loader.fill(o, propertyName);
	}

	public Filler fill(Object o) {
		return loader.fill(o);
	}

	public <T> T load(Class<T> c, String id) throws NotFoundException {
		return loader.load(c, id);
	}

	public <T> Collection<T> load(Class<T> c) {
		return loader.load(c);
	}

	public <T> T loadDeep(Class<T> c, String id) throws NotFoundException {
		return loader.loadDeep(c, id);
	}

	public <T> Collection<T> loadDeep(Class<T> c) {
		return loader.loadDeep(c);
	}

	public void delete(Object bean) {
		saver.delete(bean);
	}

	public Resource save(Object bean) {
		return saver.save(bean);
	}

	public Resource saveDeep(Object bean) {
		return saver.saveDeep(bean);
	}
	
	public static void deleteSingleton() {
		instance = null;
	}

} // TripleStore
