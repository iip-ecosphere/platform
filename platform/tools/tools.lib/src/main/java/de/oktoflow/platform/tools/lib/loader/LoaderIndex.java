package de.oktoflow.platform.tools.lib.loader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Represents the class loader index.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LoaderIndex implements Serializable {
    
    private static final long serialVersionUID = -3350988607004003802L;

    private Map<String, String> locationIndex = new HashMap<>();
    private Map<String, String> classIndex = new HashMap<>();
    private Map<String, String> resourceIndex = new HashMap<>();

    /**
     * Creates an index for a given set of JAR files. Paths will be stored relatively to ease relocation.
     *
     * @param jars list of jar files
     * @return plugin index
     * @throws IOException if adding files fails
     */
    public static LoaderIndex createIndex(List<Path> jars) throws IOException {
        return addToIndex(new LoaderIndex(), jars);
    }

    /**
     * Adds the given {@code jars} to {@code index}. Paths will be stored relatively to ease relocation.
     *
     * @param index the index to add the information to 
     * @param jars list of jar files
     * @return {@code index}
     * @throws IOException if adding files fails
     */
    public static LoaderIndex addToIndex(LoaderIndex index, List<Path> jars) throws IOException {
        for (Path jarPath : jars) {
            addToIndex(index, jarPath.toFile(), null);
        }
        return index;
    }
    
    /**
     * Adds the given {@code jarFile} to {@code index}. If given, {@code location} replaces the relative path of 
     * {@code jarFile} in the index entry.
     * 
     * @param index the index
     * @param jarFile the jarFile to index
     * @param location optional actual location of {@code jarFile}, e.g., for relocation; if <b>null</b> or empty, 
     *   use the relative path of {@code jarFile} instead
     * @throws IOException if {@code jarFile} cannot be opened
     */
    public static void addToIndex(LoaderIndex index, File jarFile, String location) throws IOException {
        if (null == location || location.length() == 0) {
            location = jarFile.toString();
        }
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory()) {
                    continue;
                }
                if (name.endsWith(".class")) {
                    // Convert to fully qualified class name
                    String className = name
                            .replace('/', '.')
                            .replaceAll("\\.class$", "");
                    addToIndex(index, index.classIndex, className, location);
                } else {
                    addToIndex(index, index.classIndex, name, location);
                }
            }
        }
    }
    
    /**
     * Adds the resource {@code name} in {@code location} to {@code map} and adjusts the location index in 
     * {@code index}.
     * 
     * @param index the index instance
     * @param map the map to modify
     * @param name the resource name-key
     * @param location the resource file location-value
     */
    private static void addToIndex(LoaderIndex index, Map<String, String> map, String name, String location) {
        String loc = index.locationIndex.get(location);
        if (null == loc) {
            loc = String.valueOf(index.locationIndex.size());
            index.locationIndex.put(loc, location);
        }
        map.put(name, loc);
    }
    
    /**
     * Relocates an index instance.
     * 
     * @param index the index
     * @param prefix the path prefix indicating a path to relocate
     * @param replacement the replacement for prefix if prefix was found at the beginning of a path
     */
    public static void relocateIndex(LoaderIndex index, String prefix, String replacement) {
        relocateIndex(index.classIndex, prefix, replacement);
        relocateIndex(index.resourceIndex, prefix, replacement);
    }

    /**
     * Relocates an index map.
     * 
     * @param index the index map, entry values may be modified as a side effect
     * @param prefix the path prefix indicating a path to relocate
     * @param replacement the replacement for prefix if prefix was found at the beginning of a path
     */
    private static void relocateIndex(Map<String, String> index, String prefix, String replacement) {
        for (Map.Entry<String, String> entry : index.entrySet()) {
            String path = entry.getValue();
            if (path.startsWith(prefix)) {
                entry.setValue(replacement + path.substring(prefix.length()));
            }
        }
    }
    
    /**
     * Reads an index file, basically via serialization.
     * 
     * @param indexFile the index file
     * @return the index object
     * @throws IOException if the file cannot be read
     */
    public static LoaderIndex fromFile(File indexFile) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(indexFile)))) {
            return (LoaderIndex) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage(), e);
        }
    }
    
    /**
     * Saves {@code index} to {@code file}.
     * 
     * @param index the index to write
     * @param file the file to write to
     * @throws IOException if the index cannot be written
     */
    public static void toFile(LoaderIndex index, File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            oos.writeObject(index);
        }
    }
    
    /**
     * Adds the resource {@code name} in {@code location} in {@code index}. [testing]
     * 
     * @param index the index instance
     * @param isClass whether we shall register a class or a resource
     * @param name the resource name-key
     * @param location the resource file location-value
     */
    public static void addToIndex(LoaderIndex index, boolean isClass, String name, String location) {
        String loc = index.locationIndex.get(location);
        if (null == loc) {
            loc = String.valueOf(index.locationIndex.size());
            index.locationIndex.put(loc, location);
        }
        Map<String, String> map = isClass ? index.classIndex : index.resourceIndex;
        map.put(name, loc);
    }

    /**
     * Returns the location index.
     * 
     * @return the locationIndex
     */
    Map<String, String> getLocationIndex() {
        return locationIndex;
    }

    /**
     * Returns the class index.
     * 
     * @return the classIndex
     */
    Map<String, String> getClassIndex() {
        return classIndex;
    }

    /**
     * Returns the resource index.
     * 
     * @return the resourceIndex
     */
    Map<String, String> getResourceIndex() {
        return resourceIndex;
    }

}