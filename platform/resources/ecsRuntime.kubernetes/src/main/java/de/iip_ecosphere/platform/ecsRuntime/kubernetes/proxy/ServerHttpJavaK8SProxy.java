package de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class ServerHttpJavaK8SProxy {

    private static int localPort = 4411;
    private static String serverIP = "192.168.81.193";
    private static String serverPort = "6443";

    /**
     * Returns the port on localhost to receive new requests.
     * 
     * @return the port on localhost to receive new requests
     */
    public static int getLocalPort() {
        return localPort;
    }

    /**
     * Set the port on localhost to receive new requests.
     *
     * @param localPort the port on localhost to receive new requests
     */
    public static void setLocalPort(int localPort) {
        ServerHttpJavaK8SProxy.localPort = localPort;
    }

    /**
     * Returns the IP Address of the server (either the MasterProxy or K8S
     * apiserver).
     * 
     * @return the IP Address of the server (either the MasterProxy or K8S
     *         apiserver)
     */
    public static String getServerIP() {
        return serverIP;
    }

    /**
     * Set the IP Address of the server (either the MasterProxy or K8S apiserver).
     *
     * @param serverIP the IP Address of the server (either the MasterProxy or K8S
     *                 apiserver)
     */
    public static void setServerIP(String serverIP) {
        ServerHttpJavaK8SProxy.serverIP = serverIP;
    }

    /**
     * Returns the port of the server (either the MasterProxy or K8S apiserver).
     * 
     * @return the port of the server (either the MasterProxy or K8S apiserver)
     */
    public static String getServerPort() {
        return serverPort;
    }

    /**
     * Set the port of the server (either the MasterProxy or K8S apiserver).
     *
     * @param serverPort the port of the server (either the MasterProxy or K8S
     *                   apiserver).
     */
    public static void setServerPort(String serverPort) {
        ServerHttpJavaK8SProxy.serverPort = serverPort;
    }

    /**
     * The main method to run the server proxy.
     * 
     * @param args the main method arguments
     * 
     */
    public static void main(String[] args) {
        K8SJavaProxy httpJavaK8SProxy = new HttpK8SJavaProxy(ProxyType.MasterProxy, serverIP, serverPort);

        try {
            startMultiThreaded(httpJavaK8SProxy, localPort);
        } catch (UnrecoverableKeyException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException
                | CertificateException | InvalidKeySpecException | IOException e) {
            System.err.println("Exception in the starting the multi-threads method");
            e.printStackTrace();
        }
    }

    /**
     * Start multi-threads method to receive and process requests.
     * 
     * @param httpJavaK8SProxy the proxy used to receive the new requests
     * @param localPort        is the port on the localhost to receive the new
     *                         requests
     * 
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws UnrecoverableKeyException
     * 
     */
    public static void startMultiThreaded(final K8SJavaProxy httpJavaK8SProxy, int localPort)
            throws UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException,
            CertificateException, InvalidKeySpecException, IOException {

        ServerSocket serverSocket = httpJavaK8SProxy.getServerSocket(localPort, null, null, null);

        System.out.println("Started multi-threaded server at localhost port " + localPort);

        final Charset encoding = StandardCharsets.UTF_8;

        while (true) {
            final Socket socket = serverSocket.accept();
            System.out.println("Accept socket");

            Thread requestThread = new Thread() {
                public void run() {
                    InputStream reader = null;
                    BufferedWriter writer = null;

                    try {
                        reader = socket.getInputStream();

                        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), encoding.name()));

                        byte[] requestByte = httpJavaK8SProxy.extractK8SRequestByte(reader);

                        if (requestByte.length == 0) {
                            return;
                        }

                        K8SRequest request = httpJavaK8SProxy.createK8SRequest(requestByte);

                        String responseString = httpJavaK8SProxy.sendK8SRequest(request);

                        writer.write(responseString);

                        writer.flush();
                        System.out.println("socket thread ends normal");
                    } catch (IOException e) {
                        System.err.println("Exception while creating response");
                        e.printStackTrace();
                        System.out.println("socket thread ends Throwable");
                    } finally {
                        try {
                            writer.close();
                            reader.close();
                            socket.close();
                        } catch (IOException e) {
                            System.err.println("Could not close the streams");
                            e.printStackTrace();
                        }
                    }
                }
            };
            requestThread.start();
        }
    }
}