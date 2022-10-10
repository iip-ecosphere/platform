/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.examples.kodex;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;

import de.iip_ecosphere.platform.services.environment.ServiceKind;
import iip.datatypes.KRec13Anon;
import iip.impl.KodexDataReceiverImpl;

/**
 * A simple receiver implementation just printing out the received data.
 * 
 * @author Holger Eichelberger, SSE
 */
public class KodexExampleReceiverImpl extends KodexDataReceiverImpl {

    /**
     * Fallback constructor.
     */
    public KodexExampleReceiverImpl() {
        super(ServiceKind.SINK_SERVICE);
    }

    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile   the YML file containing the YAML artifact with the service
     *                  descriptor
     */
    public KodexExampleReceiverImpl(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }

    @Override
    public void processKRec13Anon(KRec13Anon data) {
        System.out.println("RECEIVED " + data.get_kip() + " " + data.getStringField() + " " + data.getIntField());
        
        Connection conn = null; 
        Statement stmt = null; 
        String dbServerPort = System.getProperty("iip.app.db.server.port", "9595");
        
        try {
            String JDBC_DRIVER = "org.h2.Driver";
            String DB_URL = "jdbc:h2:tcp://localhost:" + dbServerPort + "/mem:TimeSeriesData";

            // Database credentials
            String USER = "sa";
            String PASS = "";

            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();

            String sql = "INSERT INTO TIMESERIES " + "VALUES (" + "99" + ", '" + data.getStringField() + "', "
                    + data.getIntField() + ", '" + Timestamp.from(Instant.now()) + "')";

            stmt.executeUpdate(sql);
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
        System.out.println("Record Inserted into the table...");
    }

}
