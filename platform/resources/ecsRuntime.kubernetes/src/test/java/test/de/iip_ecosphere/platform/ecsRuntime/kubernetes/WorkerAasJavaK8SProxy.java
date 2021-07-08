package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

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
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.AasK8SJavaProxy;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.HttpK8SJavaProxy;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.K8SJavaProxy;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.K8SRequest;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.ProxyType;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;

public class WorkerAasJavaK8SProxy {
  
    private static int localPort = 6443;
    private static int vabPort = 5511;
    private static int aasPort = 6611;
    private static String serverIP = "192.168.81.199";
    private static String serverPort = "8811";
    
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
        WorkerAasJavaK8SProxy.localPort = localPort;
    }

    /**
     * Returns the IP Address of the server.
     * 
     * @return the IP Address of the server
     */
    public static String getServerIP() {
        return serverIP;
    }

    /**
     * Set the IP Address of the server.
     * 
     * @param serverIP the IP Address of the server 
     */
    public static void setServerIP(String serverIP) {
        WorkerAasJavaK8SProxy.serverIP = serverIP;
    }

    /**
     * Returns the port of the server (either the Aas port or K8S apiserver port).
     * 
     * @return the port of the server (either the Aas port or K8S apiserver port)
     */
    public static String getServerPort() {
        return serverPort;
    }

    /**
     * Set the port of the server (either the Aas port or K8S apiserver port).
     *
     * @param serverPort the port of the server (either the Aas port or K8S apiserver port).
     */
    public static void setServerPort(String serverPort) {
        WorkerAasJavaK8SProxy.serverPort = serverPort;
    }

    /**
     * Returns the vab port.
     * 
     * @return the vab port
     */
    public int getVabPort() {
        return vabPort;
    }

    /**
     * Set the vab port.
     *
     * @param vabPort the vab port
     */
    public void setVabPort(int vabPort) {
        this.vabPort = vabPort;
    }

    /**
     * Returns the aas port.
     * 
     * @return the aas port
     */
    public int getAasPort() {
        return aasPort;
    }

    /**
     * Set the aas port.
     *
     * @param aasPort the aas port
     */
    public void setAasPort(int aasPort) {
        this.aasPort = aasPort;
    }
    
    /**
     * The main method to run the server proxy.
     * 
     * @param args the main method arguments
     * 
     */
    public static void main(String[] args) {
        
        WorkerK8SAas aas = new WorkerK8SAas(serverIP, serverPort, vabPort, aasPort);
        ArrayList<Server> servers = aas.startLocalAas();
        
        K8SJavaProxy aasK8SJavaProxy = new AasK8SJavaProxy(ProxyType.WorkerProxy, aasPort, serverIP, serverPort);
        
        try {
            startMultiThreaded(aasK8SJavaProxy, localPort);
        } catch (UnrecoverableKeyException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException
                | CertificateException | InvalidKeySpecException | IOException e) {
            System.err.println("Exception in the starting the multi-threads method");
            e.printStackTrace();
        }
        
//        for (Server server : servers) {
//            server.stop(true);
//        }
    }

    /**
     * Start multi-threads method to receive and process requests.
     * 
     * @param aasK8SJavaProxy  the proxy used to receive the new requests
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
    public static void startMultiThreaded(final K8SJavaProxy aasK8SJavaProxy, int localPort)
            throws UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException,
            CertificateException, InvalidKeySpecException, IOException {

        ServerSocket serverSocket = aasK8SJavaProxy.getServerSocket(localPort, null, null, null);

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

                        byte[] requestByte = aasK8SJavaProxy.extractK8SRequestByte(reader);

                        if (requestByte.length == 0) {
                            return;
                        }

                        K8SRequest request = aasK8SJavaProxy.createK8SRequest(requestByte);

                        String responseString = aasK8SJavaProxy.sendK8SRequest(request);

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