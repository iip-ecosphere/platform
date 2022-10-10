package com.database.springbooth2database;

import java.sql.Connection; 
import java.sql.DriverManager; 
import java.sql.SQLException; 
import java.sql.Statement;
import java.time.Instant;
import java.sql.Timestamp;

public class H2jdbcInsertDemo { 
   // JDBC driver name and database URL 
   static final String JDBC_DRIVER = "org.h2.Driver";   
   static final String DB_URL = "jdbc:h2:tcp://localhost:9090/mem:TimeSeriesData";
   
   //  Database credentials 
   static final String USER = "sa"; 
   static final String PASS = ""; 
  
   public static void main(String[] args) { 
      Connection conn = null; 
      Statement stmt = null; 
      try{
         // STEP 1: Register JDBC driver 
         Class.forName(JDBC_DRIVER);  
         
         // STEP 2: Open a connection 
         System.out.println("Connecting to a selected database..."); 
         conn = DriverManager.getConnection(DB_URL,USER,PASS); 
         System.out.println("Connected database successfully..."); 
         
         // STEP 3: Execute a query 
         stmt = conn.createStatement();  
         
         String sql = "INSERT INTO TIMESERIES " + "VALUES (100, 'test1', 30, '" + Timestamp.from(Instant.now()) + "')"; 
         
         stmt.executeUpdate(sql); 
         sql = "INSERT INTO TIMESERIES " + "VALUES (101, 'test2', 40, '" + Timestamp.from(Instant.now()) + "')"; 
         
         stmt.executeUpdate(sql); 
         sql = "INSERT INTO TIMESERIES " + "VALUES (102, 'test3', 50, '" + Timestamp.from(Instant.now()) + "')"; 
         
         stmt.executeUpdate(sql); 
         sql = "INSERT INTO TIMESERIES " + "VALUES(103, 'test4', 60, '" + Timestamp.from(Instant.now()) + "')"; 
         
         stmt.executeUpdate(sql); 
         System.out.println("Inserted records into the table..."); 
         
         // STEP 4: Clean-up environment 
         stmt.close(); 
         conn.close(); 
      } catch(SQLException se) { 
         // Handle errors for JDBC 
         se.printStackTrace(); 
      } catch(Exception e) { 
         // Handle errors for Class.forName 
         e.printStackTrace(); 
      } finally { 
         // finally block used to close resources 
         try {
            if(stmt!=null) stmt.close();  
         } catch(SQLException se2) { 
         } // nothing we can do 
         try { 
            if(conn!=null) conn.close(); 
         } catch(SQLException se) { 
            se.printStackTrace(); 
         } // end finally try 
      } // end try 
      System.out.println("Goodbye!"); 
   } 
}