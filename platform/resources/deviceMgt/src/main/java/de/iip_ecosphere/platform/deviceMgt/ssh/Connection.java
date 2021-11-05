package de.iip_ecosphere.platform.deviceMgt.ssh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

/**
 * Creates a connection between a remote and a client socket with the help
 * of two instances of {@link Proxy}. Its connecting them by configure one proxy
 * to proxy the client to the server and the other proxy to proxy the server to
 * the client.
 *
 * Main parts were acquired from https://github.com/oksuz/tcp-proxy (MIT)
 *
 * @author oksuz, Github on 29/10/2017.
 */
public class Connection implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);

    private final Socket clientSocket;
    private final String remoteIp;
    private final int remotePort;
    private Socket serverConnection = null;

    private Thread clientServerThread;
    private Thread serverClientThread;

    /**
     * Sets up a connection between a client and a remote.
     *
     * @param clientSocket the client socket
     * @param remoteIp the remote ip address
     * @param remotePort the remote port
     */
    public Connection(Socket clientSocket, String remoteIp, int remotePort) {
        this.clientSocket = clientSocket;
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
    }

    /**
     * Runs the connection, should be run in combination with a thread.
     *
     * This method also checks if the connection is still up. After the
     * client disconnects, the connection is closed.
     */
    @Override
    public void run() {
        LOGGER.info("new connection {}:{}", clientSocket.getInetAddress().getHostName(), clientSocket.getPort());
        try {
            serverConnection = new Socket(remoteIp, remotePort);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        LOGGER.info("Proxy {}:{} <-> {}:{}", clientSocket.getInetAddress().getHostName(), clientSocket.getPort(), 
            serverConnection.getInetAddress().getHostName(), serverConnection.getPort());

        clientServerThread = new Thread(new Proxy(clientSocket, serverConnection));
        clientServerThread.start();
        serverClientThread = new Thread(new Proxy(serverConnection, clientSocket));
        serverClientThread.start();

        while (true) {
            if (clientSocket.isClosed()) {
                LOGGER.info("client socket ({}:{}) closed", clientSocket.getInetAddress().getHostName(), 
                    clientSocket.getPort());
                closeServerConnection();
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
                clientServerThread.interrupt();
                serverClientThread.interrupt();
            }
        }

    }

    /**
     * Close the server Connection.
     */
    private void closeServerConnection() {
        if (serverConnection != null && !serverConnection.isClosed()) {
            try {
                LOGGER.info("closing remote host connection {}:{}", serverConnection.getInetAddress().getHostName(), 
                    serverConnection.getPort());
                serverConnection.close();

                clientServerThread.interrupt();
                serverClientThread.interrupt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}