package de.iip_ecosphere.platform.tools.maven.python;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

public class FilesetUtils {

    /**
     * Determines excluded paths from the given paths, i.e., split the directories add them to {@code excluded}.
     * 
     * @param paths the paths to be excluded
     * @param isFile whether path represents a file (than ignore the last path) or whether it is a folder
     * @param excluded the excluded paths to be modified as a side effect
     */
    public static void addExcludedPaths(String[] paths, boolean isFile, Set<String> excluded) {
        for (String p : paths) {
            addExcludedPaths(p, isFile, excluded);
        }
    }

    /**
     * Determines excluded paths from the given path, i.e., split the directories add them to {@code excluded}.
     * 
     * @param path the path to be excluded
     * @param isFile whether path represents a file (than ignore the last path) or whether it is a folder
     * @param excluded the excluded paths to be modified as a side effect
     */
    public static void addExcludedPaths(String path, boolean isFile, Set<String> excluded) {
        excluded.add(path);
        String[] subPaths = path.replace("\\", "/").split("/");
        if (subPaths.length > 0) {
            String tmp = "";
            for (int i = 0; i < subPaths.length - (isFile ? 1 : 0); i++) { // if isFile, not the file name
                if (i > 0) {
                    tmp += "/";
                }
                tmp += subPaths[i];
                excluded.add(tmp);
            }
        }
    }
    
    /**
     * Deletes the given paths.
     *
     * @param fileset the fileset to operate on 
     * @param paths the paths to be deleted
     * @param excluded paths that shall not be deleted (excluded)
     * @param log the log instance
     * @see #iteratePaths(FileSet, String[], Set, Consumer)
     */
    public static void deletePaths(FileSet fileset, String[] paths, Set<String> excluded, Log log) {
        iteratePaths(fileset, paths, excluded, file -> {
            log.info("Deleting " + file);
            FileUtils.deleteQuietly(file);
        });
    }

    /**
     * Iterates over paths.
     * 
     * @param fileset the fileset to operate on 
     * @param paths the paths to be iterated
     * @param excluded paths that shall not be deleted (excluded)
     * @param fileConsumer function that operates on the included files
     */
    public static void iteratePaths(FileSet fileset, String[] paths, Set<String> excluded, 
        Consumer<File> fileConsumer) {
        for (String p : paths) {
            if (!excluded.contains(p)) {
                File file = new File(fileset.getDirectory(), p);
                fileConsumer.accept(file);
            }
        }
    }

    /**
     * Deletes the specified included and not excluded paths.
     *
     * @param fileset the specifying fileset 
     * @param log the log instance
     * @see #determineFiles(FileSet, boolean, Consumer)
     */
    public static void deletePaths(FileSet fileset, Log log) {
        determineFiles(fileset, true, file -> {
            log.info("Deleting " + file);
            FileUtils.deleteQuietly(file);
        });
    }

    /**
     * Determines the files to be processed.
     * 
     * @param fileset the specifying fileset 
     * @param considerDirectories whether directories shall be considered in the result
     * @param fileConsumer a consumer receiving the results
     * @see #addExcludedPaths(String[], boolean, Set)
     * @see #iteratePaths(FileSet, String[], Set, Consumer)
     */
    public static void determineFiles(FileSet fileset, boolean considerDirectories, Consumer<File> fileConsumer) {
        if (null != fileset) {
            FileSetManager fileSetManager = new FileSetManager();
            Set<String> excluded = new HashSet<>();
            excluded.add("");  // don't delete containing directory, it's part of included directories
            excluded.add(".");
    
            if (considerDirectories) {
                addExcludedPaths(fileSetManager.getExcludedDirectories(fileset), false, excluded);
            }
            addExcludedPaths(fileSetManager.getExcludedFiles(fileset), true, excluded);

            iteratePaths(fileset, fileSetManager.getIncludedFiles(fileset), excluded, fileConsumer);
            if (considerDirectories) {
                iteratePaths(fileset, fileSetManager.getIncludedDirectories(fileset), excluded, fileConsumer);
            }
        }
    }

    /**
     * Streams the files specified by {@code fileset}.
     * 
     * @param fileset the specifying fileset 
     * @param considerDirectories whether directories shall be considered in the result
     * @return the stream of files
     * @see #determineFiles(FileSet, boolean, Consumer)
     */
    public static Stream<File> streamFiles(FileSet fileset, boolean considerDirectories) {
        Stream.Builder<File> resultBuilder = Stream.builder(); 
        determineFiles(fileset, considerDirectories, f -> resultBuilder.add(f));
        return resultBuilder.build();
    }

    /**
     * Materializes the files specified by {@code fileset} as list.
     * 
     * @param fileset the specifying fileset 
     * @param considerDirectories whether directories shall be considered in the result
     * @return the files
     * @see #determineFiles(FileSet, boolean, Consumer)
     */
    public static List<File> listFiles(FileSet fileset, boolean considerDirectories) {
        List<File> result = new ArrayList<>(); 
        determineFiles(fileset, considerDirectories, f -> result.add(f));
        return result;
    }
    
    /**
     * Touches {@code file} if not <b>null</b>. Emits a warning into {@code log} if touching is not possible.
     * 
     * @param file the file to touch
     * @param log the log to be used
     */
    public static void touch(File file, Log log) {
        if (file != null && file.getPath().length() > 0) {
            try {
                FileUtils.touch(file);
            } catch (IOException e) {
                log.warn("Cannot touch " + file + ": " + e.getMessage());
            }
        }
    }

}
