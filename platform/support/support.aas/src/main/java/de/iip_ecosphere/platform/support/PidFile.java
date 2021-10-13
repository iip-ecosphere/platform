package de.iip_ecosphere.platform.support;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

/**
 * Process ID (PID) file abstraction that writes the current PID into a file and optionally
 * removes it on system exit. This class is taken from elastic search (adapted from 
 * http://home.apache.org/~rmuir/es-coverage/combined/org.elasticsearch.common/PidFile.java.html) 
 * as a dependency to elastic search (in particular on this level) is currently not intended.
 * 
 * @author elasicsearch 
 */
public final class PidFile {

    /**
     * Denotes the directory where PID files shall be created within. If not specified, 
     * {@link FileUtils#getTempDirectoryPath() the temporary directory} is used.
     */
    public static final String PID_DIR_PROPERTY_NAME = "iip.pid.dir";
    
    private final long pid;
    private final Path path;
    private final boolean deleteOnExit;

    /**
     * Creates an instance.
     * 
     * @param path the path where to create the file within
     * @param deleteOnExit delete the file on exit of the JVM
     * @param pid the PID to write into the file
     * @throws IOException if an I/O problem occurs while creating the PID file
     * @throws IllegalArgumentException if the parent of {@code path} is not a directory or the denoted file is not 
     *     a regular file
     */
    private PidFile(Path path, boolean deleteOnExit, long pid) throws IOException {
        this.path = path;
        this.deleteOnExit = deleteOnExit;
        this.pid = pid;
    }

    /**
     * Creates a new PidFile and writes the current process ID into the provided path.
     *
     * @param path the path to the PID file. The file is newly created or truncated if it already exists.
     * @param deleteOnExit if <code>true</code> the PID file is deleted with best effort on system exit
     * @return the PidFile instance
     * @throws IOException if an I/O problem occurs
     * @throws IllegalArgumentException if the parent of {@code path} is not a directory or the denoted file is not 
     *     a regular file
     */
    public static PidFile create(Path path, boolean deleteOnExit) throws IOException {
        return create(path, deleteOnExit, getJvmPid());
    }

    /**
     * Creates a new PidFile in the system's temporary directory as a file with given {@code name} and writes the 
     * current process ID into that file.
     *
     * @param name the name of the file within the system'S temporary directory. An existing file is deleted.
     * @param deleteOnExit if <code>true</code> the PID file is deleted with best effort on system exit
     * @return the PidFile instance
     * @throws IOException if an I/O problem occurs
     * @throws IllegalArgumentException if the parent of {@code path} is not a directory or the denoted file is not 
     *     a regular file
     */
    public static PidFile createInDefaultDir(String name, boolean deleteOnExit) throws IOException {
        return createInDefaultDir(name, deleteOnExit, true);
    }

    /**
     * Creates a new PidFile in the system's temporary directory as a file with given {@code name} and writes the 
     * current process ID into that file.
     *
     * @param name the name of the file within the system'S temporary directory. 
     * @param deleteOnExit if <code>true</code> the PID file is deleted with best effort on system exit
     * @param deleteIfExists if <code>true</code>, an existing file is deleted; if <code>false</code> the file is newly 
     *     created or truncated if it already exists.
     * @return the PidFile instance
     * @throws IOException if an I/O problem occurs
     * @throws IllegalArgumentException if the parent of {@code path} is not a directory or the denoted file is not 
     *     a regular file
     */
    public static PidFile createInDefaultDir(String name, boolean deleteOnExit, boolean deleteIfExists) 
        throws IOException {
        File f = new File(System.getProperty(PID_DIR_PROPERTY_NAME, FileUtils.getTempDirectoryPath()), name);
        if (deleteIfExists) {
            FileUtils.deleteQuietly(f);
        }
        return create(f.toPath(), deleteOnExit);
    }

    /**
     * Creates a new PidFile with given process identifier and writes the identifier into the provided path.
     *
     * @param path the path to the PID file. The file is newly created or truncated if it already exists.
     * @param deleteOnExit if <code>true</code> the pid file is deleted with best effort on system exit
     * @param pid the PID to write into the file
     * @return the PidFile instance
     * @throws IOException if an I/O problem occurs
     * @throws IllegalArgumentException if the parent of {@code path} is not a directory or the denoted file is not 
     *     a regular file
     */
    static PidFile create(Path path, boolean deleteOnExit, long pid) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            if (Files.exists(parent) && !Files.isDirectory(parent)) {
                throw new IllegalArgumentException(parent + " exists but is not a directory");
            }
            if (!Files.exists(parent)) {
                // only do this if it doesn't exists we get a better exception further down
                // if there are security issues etc. this also doesn't work if the parent exists
                // and is a soft-link like on many linux systems /var/run can be a link and that should
                // not prevent us from writing the PID
                Files.createDirectories(parent);
            }
        }
        if (Files.exists(path) && !Files.isRegularFile(path)) {
            throw new IllegalArgumentException(path + " exists but is not a regular file");
        }

        try (OutputStream stream = Files.newOutputStream(path, StandardOpenOption.CREATE, 
            StandardOpenOption.TRUNCATE_EXISTING)) {
            stream.write(Long.toString(pid).getBytes(StandardCharsets.UTF_8));
        }

        if (deleteOnExit) {
            addShutdownHook(path);
        }
        return new PidFile(path, deleteOnExit, pid);
    }


    /**
     * Returns the PID of the current JVM.
     * 
     * @return the PID, may be {@code -1} if the PID cannot be determined
     */
    public long getPid() {
        return pid;
    }

    /**
     * Returns the PID file path.
     * 
     * @return PID file path
     */
    public Path getPath() {
        return path;
    }

    /**
     * Returns <code>true</code> iff the process id file is deleted on system exit. 
     * 
     * @return <code>true</code> of deletion of system exit is enabled, otherwise <code>false</code>.
     */
    public boolean isDeleteOnExit() {
        return deleteOnExit;
    }

    /**
     * Adds a shutdown hook in order to get rid of the PID file when the JVM terminates.
     * 
     * @param path the path to the file
     */
    private static void addShutdownHook(final Path path) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    LoggerFactory.getLogger(PidFile.class).error("Failed to delete PID file '{}': {}", 
                        path, e.getMessage());
                }
            }
        });
    }
    
    /**
     * Returns the process identifier of the containing JVM.
     * 
     * @return the process identifier, may be <code>-1</code> if not available
     */
    public static long getJvmPid() {
        long pid;
        String xPid = ManagementFactory.getRuntimeMXBean().getName();
        try {
            xPid = xPid.split("@")[0];
            pid = Long.parseLong(xPid);
        } catch (NumberFormatException e) {
            pid = -1;
        }        
        return pid;
    }
}
