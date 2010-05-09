package org.creativecommons.learn.feed;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.VCARD;

import de.fuberlin.wiwiss.ng4j.NamedGraph;
import de.fuberlin.wiwiss.ng4j.NamedGraphModel;
import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
import de.fuberlin.wiwiss.ng4j.db.NamedGraphSetDB;

public class RaffiTest {
	
	public static void main(String args[]) throws SQLException {
		
		System.out.println("This is just so Raffi can figure out what he's doing.");
		
		// some definitions
		String personURI    = "http://mantinea/Diotima";
		String givenName    = "Diotima";
		String familyName   = "Mantinea";
		String fullName     = givenName + " " + familyName;
		
		// Task: this triple should be stored in a named graph 

		// create an empty Model
		String url = "jdbc:mysql://localhost/discovered?autoReconnect=true"; 
		String user = "discovered"; 
		String password = "";
		
		Connection c = DriverManager.getConnection(url, user, password);
		
		java.sql.Statement statement = c.createStatement();
		
		String sql = "CREATE DATABASE IF NOT EXISTS discovered";
		statement.executeUpdate(sql);
		
		NamedGraphSet s = new NamedGraphSetDB(c);
		NamedGraph g = s.createGraph("http://example.com#graphName");
		NamedGraphModel m = new NamedGraphModel(s, g.getGraphName().toString());
		
		// create a small hierarchy of resources
		Resource diotima = m.createResource(personURI);
		diotima.addProperty(VCARD.FN, fullName);
		
		Resource blank = m.createResource();
		blank.addProperty(VCARD.Given, givenName);
		blank.addProperty(VCARD.Family, familyName);
		
		diotima.addProperty(VCARD.N, blank);

		StmtIterator iter = m.listStatements();

		while(iter.hasNext()) {
			Statement stmt = iter.nextStatement();
			System.out.println("isReified? " + stmt.isReified());
			Resource subject = stmt.getSubject();
			Property predicate = stmt.getPredicate();
			RDFNode object = stmt.getObject();
			
			System.out.print(subject.toString() +
					" " + predicate.toString() + " ");
			if (object instanceof Resource) {
				System.out.print(object.toString());
			}
			else {
				// object is a literal, which means it's just a string of characters
				System.out.print(" \"" + object.toString() + "\"");
			}

			System.out.println(" .");

		}
		
	}
}
