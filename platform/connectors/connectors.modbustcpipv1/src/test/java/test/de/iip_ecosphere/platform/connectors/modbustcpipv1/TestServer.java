/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.connectors.modbustcpipv1;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;

/**
 * Implements a MODBUS TCP/IP test server.
 * 
 * So far only modbus holding registers are implemented.
 * 
 * @author Christian Nikolajew
 */
public class TestServer {

    /**
     * Determines the amount of holding registers for the TestServer.
     */
    private static final int TEST_SIZE = 20;

    private ModbusTCPListener mListener = null;

    private InetAddress mHost;
    private int mPort = Modbus.DEFAULT_PORT;

    /**
     * Creates a TestServer instance.
     */
    public TestServer() {

        SimpleProcessImage spi = null;
        spi = new SimpleProcessImage();

        for (int i = 0; i < TEST_SIZE; i++) {
            spi.addRegister(new SimpleRegister(0));
        }

        ModbusCoupler.getReference().setProcessImage(spi);
        ModbusCoupler.getReference().setMaster(false);
        ModbusCoupler.getReference().setUnitID(1);

        mListener = new ModbusTCPListener(3);
        mListener.setPort(mPort);

        try {
            mHost = InetAddress.getLocalHost();
            mListener.setAddress(mHost);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the server.
     */
    public void start() {

        if (mListener != null) {
            mListener.start();
        }

    }

    /**
     * Shuts down the server.
     */
    public void stop() {

        if (mListener != null) {
            mListener.stop();
        }

    }

    /**
     * Returns the host.
     * 
     * @return the host
     */
    public String getHost() {
        return mHost.getHostAddress();
    }

    /**
     * Returns the port.
     * 
     * @return the port
     */
    public int getPort() {
        return mPort;
    }

}