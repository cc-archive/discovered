package org.creativecommons.learn.oercloud;

import java.sql.*;

public class TagLoader {

    private String USERNAME = "root";
    private String PASSWORD = "";
    private String DB_URL = "jdbc:mysql://localhost/oercloud";

    private Connection dbConn = null;

    protected void connect() 
	throws SQLException {

	if (this.dbConn != null) {
	    // don't reopen
	    return;
	}

	try {
	    Class.forName ("com.mysql.jdbc.Driver").newInstance ();
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	} catch (InstantiationException e) {
	    e.printStackTrace();
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	}

	this.dbConn = DriverManager.getConnection (DB_URL, USERNAME, PASSWORD);
	
    } // connect

    public Integer urlId(String url) 
	throws SQLException {
	// return the integer ID for this URL; if the URL is not found return
	// -1

	    this.connect();
	    Statement s = this.dbConn.createStatement();
            
	    String sql_bid = "SELECT bId FROM sc_bookmarks WHERE bAddress = '" +
		url + "'";
	    ResultSet rs_bid = s.executeQuery(sql_bid);

	    if (rs_bid.next()) 
		return new Integer(rs_bid.getInt("bId"));
	    else
		return new Integer(-1);

    } // urlId

    public String tagsAsString(String url) 
	throws SQLException {
	// return the tags for a given URL as a space separated string

	    this.connect();

	    // Get the bookmark ID for the given URL
	    Integer url_id = this.urlId(url);
	    if (url_id.intValue() == -1)
		return "";

	    Statement s = this.dbConn.createStatement();

	    // Get the tags for the bookmark ID obtained above
	    String sql_tags = "SELECT tag FROM sc_tags WHERE bId = '" + 
		url_id.toString() + "'";
	    ResultSet rs_tags = s.executeQuery(sql_tags);
	    String result = "";
	    while ( rs_tags.next() ) {
		result = result + " " + rs_tags.getString("tag");
	    }

	    return result;
    } // tagsAsString

} // TagLoader