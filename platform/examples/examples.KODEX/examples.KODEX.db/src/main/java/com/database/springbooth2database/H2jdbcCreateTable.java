package com.database.springbooth2database;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;

public class H2jdbcCreateTable {

	public void CreateTimeSeriesTable(String dbServerPort) {
	    
	    // JDBC driver name and database URL
	    String JDBC_DRIVER = "org.h2.Driver";
	    String DB_URL = "jdbc:h2:tcp://localhost:" + dbServerPort + "/mem:TimeSeriesData";

	    // Database credentials
	    String USER = "sa";
	    String PASS = "";
	    
		Connection conn = null;
		Statement stmt = null;
		try {
			// STEP 1: Register JDBC driver
			Class.forName(JDBC_DRIVER);

			// STEP 2: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			// STEP 3: Execute a query
			System.out.println("Creating table in given database...");
			stmt = conn.createStatement();
			String sql = "CREATE TABLE   TIMESERIES " + "(ID INTEGER not NULL, " + " StringField VARCHAR(255), "
					+ " IntField INTEGER, " + " Seires TIMESTAMP)";
			stmt.executeUpdate(sql);
			System.out.println("Created table in given database...");

			// STEP 4: Clean-up environment
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		System.out.println("Goodbye!");
	}

}
