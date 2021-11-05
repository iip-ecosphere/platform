package de.iip_ecosphere.platform.deviceMgt.ssh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * A basic transparent proxy implementation, which should be run in a thread.
 *
 * Main parts were acquired from https://github.com/oksuz/tcp-proxy (MIT)
 *
 * @author oksuz, Github on 29/10/2017.
 */
public class Proxy implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Proxy.class);
    private final Socket in;
    private final Socket out;

    /**
     * Creates a one-directional proxy between in and out.
     *
     * @param in Socket to get the data from
     * @param out Socket to pump the data into
     */
    public Proxy(Socket in, Socket out) {
        this.in = in;
        this.out = out;
    }

    /**
     * This method basically pumps the data from {@code in.getInputStream()}
     * to {@code out.getOutputStream()}, which creates a one-directional link between
     * in and out.
     */
    @Override
    public void run() {
        LOGGER.info("Proxy {}:{} --> {}:{}", in.getInetAddress().getHostName(), in.getPort(), 
            out.getInetAddress().getHostName(), out.getPort());
        try {
            InputStream inputStream = getInputStream();
            OutputStream outputStream = getOutputStream();

            if (inputStream == null || outputStream == null) {
                return;
            }

            byte[] reply = new byte[4096];
            int bytesRead;
            while (-1 != (bytesRead = inputStream.read(reply))) {
                outputStream.write(reply, 0, bytesRead);
            }
        } catch (SocketException ignored) {
        } catch (IOException e) {
            LOGGER.error("IOException " + e.getMessage());
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                LOGGER.error("IOException " + e.getMessage());
            }
        }
    }

    /**
     * Returns the input stream.
     * 
     * @return the input stream
     */
    private InputStream getInputStream() {
        try {
            return in.getInputStream();
        } catch (IOException e) {
            LOGGER.error("IOException obtaining input stream " + e.getMessage());
        }

        return null;
    }

    /**
     * Returns the output stream.
     * 
     * @return the output stream
     */
    private OutputStream getOutputStream() {
        try {
            return out.getOutputStream();
        } catch (IOException e) {
            LOGGER.error("IOException obtaining output stream " + e.getMessage());
        }

        return null;
    }
}
