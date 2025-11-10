package de.iip_ecosphere.platform.support.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.function.Consumer;

/**
 * Simple process stream gobbler.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StreamGobbler extends Thread {
    
    private InputStream in;
    private PrintStream out;
    private Consumer<String> consumer;

    /**
     * Creates a stream gobbler.
     * 
     * @param in the process stream
     * @param out the output stream where to write the process output to
     */
    public StreamGobbler(InputStream in, PrintStream out) {
        this.in = in;
        this.out = out;
        start();
    }
    
    /**
     * Adds a line consumer.
     * 
     * @param consumer the line consumer, may be <b>null</b> for none
     * @return <b>this</b> for chaining
     */
    public StreamGobbler addConsumer(Consumer<String> consumer) {
        this.consumer = consumer;
        return this;
    }

    @Override
    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while ((line = input.readLine()) != null) {
                out.println(line);
                if (null != consumer) {
                    consumer.accept(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Reading process stream failed: " + e.getMessage());
        }
    }

    /**
     * Attaches two default stream gobblers to the given {@code process}.
     * 
     * @param process the process to attach to
     */
    public static void attach(Process process) {
        attach(process, null, null);
    }

    /**
     * Attaches two default stream gobblers to the given {@code process} with given consumers.
     * 
     * @param process the process to attach to
     * @param outConsumer the output stream consumer, may be <b>null</b> for none
     * @param errConsumer the error stream consumer, may be <b>null</b> for none
     */
    public static void attach(Process process, Consumer<String> outConsumer, Consumer<String> errConsumer) {
        attach(process, System.out, outConsumer, System.err, errConsumer);
    }

    /**
     * Attaches two stream gobblers to the given {@code process} with given consumers.
     * 
     * @param process the process to attach to
     * @param out the stream where the process output shall be written to
     * @param outConsumer the output stream consumer, may be <b>null</b> for none
     * @param err the stream where the process errors shall be written to
     * @param errConsumer the error stream consumer, may be <b>null</b> for none
     */
    public static void attach(Process process, PrintStream out, Consumer<String> outConsumer, PrintStream err, 
        Consumer<String> errConsumer) {
        new StreamGobbler(process.getInputStream(), out).addConsumer(outConsumer);
        new StreamGobbler(process.getErrorStream(), err).addConsumer(errConsumer);
    }
    
}