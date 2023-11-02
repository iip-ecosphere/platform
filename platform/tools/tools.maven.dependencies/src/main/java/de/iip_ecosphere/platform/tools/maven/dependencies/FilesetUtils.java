package de.iip_ecosphere.platform.tools.maven.dependencies;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

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
     */
    public static void deletePaths(FileSet fileset, String[] paths, Set<String> excluded, Log log) {
        for (String p : paths) {
            if (!excluded.contains(p)) {
                File file = new File(fileset.getDirectory(), p);
                log.info("Deleting " + file);
                FileUtils.deleteQuietly(file);
            }
        }
    }

    /**
     * Deletes the specified included and not excluded paths.
     *
     * @param fileset the specifying fileset 
     * @param log the log instance
     */
    public static void deletePaths(FileSet fileset, Log log) {
        if (null != fileset) {
            FileSetManager fileSetManager = new FileSetManager();
            Set<String> excluded = new HashSet<>();
            excluded.add("");  // don't delete containing directory, it's part of included directories
            excluded.add(".");

            addExcludedPaths(fileSetManager.getExcludedDirectories(fileset), false, excluded);
            addExcludedPaths(fileSetManager.getExcludedFiles(fileset), true, excluded);

            deletePaths(fileset, fileSetManager.getIncludedFiles(fileset), excluded, log);
            deletePaths(fileset, fileSetManager.getIncludedDirectories(fileset), excluded, log);
        }
    }

}
