package de.iip_ecosphere.platform.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.net.NetworkManager;
import de.iip_ecosphere.platform.support.net.NetworkManagerDescriptor;

/**
 * Descriptor for {@link PersistentLocalNetworkManagerImpl}. Reads back 
 * {@link #getFile()} if exists and not outdated ({@value #STORE_TIMEOUT} ms). 
 * Shall not be an inner class to not interfere on command line with shell expansion. This specialized 
 * network manager (descriptor) is intended for testing only, not for production use!
 * 
 * @author Holger Eichelberger, SSE
 */
public class PersistentLocalNetworkManagerDescriptor implements NetworkManagerDescriptor {

    public static final long STORE_TIMEOUT = 60000;
    
    /**
     * Returns the file to be used for persisting.
     * 
     * @return the file
     */
    public static File getFile() {
        return new File(FileUtils.getTempDirectory(), "iip-persNetwMgr.ser"); // timestamp may be added
    }

    @Override
    public NetworkManager createInstance() {
        PersistentLocalNetworkManagerImpl result = new PersistentLocalNetworkManagerImpl();
        File f = getFile();
        if (!f.exists()) {
            LoggerFactory.getLogger(getClass()).info("No persisted network manager information found. "
                + "Initializing empty.");
        } else if (System.currentTimeMillis() - f.lastModified() > STORE_TIMEOUT) {
            LoggerFactory.getLogger(getClass()).info("Persisted network manager information found, but outdated "
                + "({} ms). Initializing empty.", STORE_TIMEOUT);
        } else {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
                result.readFrom(ois);
                ois.close();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(f)));
                LoggerFactory.getLogger(getClass()).info("Loaded persistent local network manager data");
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Cannot read persistent local network manager data: {}", 
                    e.getMessage());
            }
        } 
        return result;
    }
    
}