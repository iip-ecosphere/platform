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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Represents the class loader index.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LoaderIndex implements Serializable {
    
    public static final String RESOURCE_SEPARATOR = ",";
    private static final long serialVersionUID = -3350988607004003802L;

    private List<String> files = new ArrayList<>();
    private Map<String, String> locationIndex = new HashMap<>();
    private Map<String, String> locationReverseIndex = new HashMap<>();
    private Map<String, String> classIndex = new HashMap<>();
    private Map<String, String> resourceIndex = new HashMap<>();

    /**
     * Creates an index for a given set of JAR files. Paths will be stored relatively to ease relocation.
     * Updates the {@link #files} list in the specified sequence of {@code jars}.
     *
     * @param jars list of jar files
     * @param consumer optional exception consumer to tolerantly handle IO exceptions, exceptions are thrown if 
     *     <b>null</b>
     * @return plugin index
     * @throws IOException if adding files fails
     */
    public static LoaderIndex createIndex(List<Path> jars, Consumer<IOException> consumer) throws IOException {
        return addToIndex(new LoaderIndex(), jars, consumer);
    }

    /**
     * Adds the given {@code jars} to {@code index}. Paths will be stored relatively to ease relocation.
     * Updates the {@link #files} list in the specified sequence of {@code jars}.
     *
     * @param index the index to add the information to 
     * @param jars list of jar files
     * @param consumer optional exception consumer to tolerantly handle IO exceptions, exceptions are thrown if 
     *     <b>null</b>
     * @return {@code index}
     * @throws IOException if adding files fails
     */
    public static LoaderIndex addToIndex(LoaderIndex index, List<Path> jars, Consumer<IOException> consumer) 
        throws IOException {
        for (Path jarPath : jars) {
            addToIndex(index, jarPath.toFile(), null, consumer);
        }
        return index;
    }
    
    /**
     * Adds the given {@code jarFile} to {@code index}. If given, {@code location} replaces the relative path of 
     * {@code jarFile} in the index entry. Updates the {@link #files} list by adding {@code jarFile}.
     * 
     * @param index the index
     * @param jarFile the jarFile to index
     * @param location optional actual location of {@code jarFile}, e.g., for relocation; if <b>null</b> or empty, 
     *   use the relative path of {@code jarFile} instead
     * @param consumer optional exception consumer to tolerantly handle IO exceptions, exceptions are thrown if 
     *     <b>null</b>
     * @throws IOException if {@code jarFile} cannot be opened
     */
    public static void addToIndex(LoaderIndex index, File jarFile, String location, Consumer<IOException> consumer) 
        throws IOException {
        if (null == location || location.length() == 0) {
            location = jarFile.toString();
        }
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory()) {
                    addToIndex(index, index.resourceIndex, name, location, true);
                    continue;
                } else if (name.endsWith(".class")) {
                    // Convert to fully qualified class name
                    String className = name
                            .replace('/', '.')
                            .replaceAll("\\.class$", "");
                    addToIndex(index, index.classIndex, className, location, true);
                } else {
                    addToIndex(index, index.resourceIndex, name, location, true);
                }
            }
            index.files.add(jarFile.toString());
        } catch (IOException e) {
            if (null == consumer) {
                throw e;
            } else {
                consumer.accept(e);
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
     * @param append whether the value shall be appended to an already known value using {@link #RESOURCE_SEPARATOR} 
     *   or the known value shall be overwritten by the new value
     */
    private static void addToIndex(LoaderIndex index, Map<String, String> map, String name, String location, 
        boolean append) {
        String loc = index.locationReverseIndex.get(location);
        if (null == loc) {
            loc = String.valueOf(index.locationIndex.size());
            index.locationIndex.put(loc, location);
            index.locationReverseIndex.put(location, loc);
        }
        String known = map.get(name);
        if (append) {
            if (known != null) {
                loc = known + RESOURCE_SEPARATOR + loc;
            }
            map.put(name, loc);
        } else {
            if (known == null) { // do not override, allow for shadowing
                map.put(name, loc);
            }
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
        addToIndex(index, isClass ? index.classIndex : index.resourceIndex, name, location, !isClass);
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

    /**
     * Returns the number of known classes.
     * 
     * @return the number of known classes
     */
    public int getClassesCount() {
        return classIndex.size();
    }

    /**
     * Returns the number of known resources.
     * 
     * @return the number of known resources
     */
    public int getResourcesCount() {
        return resourceIndex.size();
    }

    /**
     * Returns the number of known locations.
     * 
     * @return the number of known locations
     */
    public int getLocationsCount() {
        return locationIndex.size();
    }

    /**
     * Returns the known classes.
     * 
     * @return the known classes
     */
    public Iterable<String> getClasses() {
        return classIndex.keySet();
    }

    /**
     * Returns the known resources.
     * 
     * @return the known resources
     */
    public Iterable<String> getResources() {
        return resourceIndex.keySet();
    }

    /**
     * Returns the known locations.
     * 
     * @return the known locations
     */
    public Iterable<String> getLocations() {
        return locationReverseIndex.keySet();
    }

    /**
     * Returns the location of the specified class {@code cls}.
     * 
     * @param cls the class
     * @return the location, may be <b>null</b> if either class or location is unknown
     */
    public String getClassLocation(String cls) {
        String result = null;
        String loc = classIndex.get(cls);
        if (null != loc) {
            result = locationIndex.get(loc);
        }
        return result;
    }

    /**
     * Returns the location(s) of the specified resource {@code res}.
     * 
     * @param res the resource
     * @return the location(s), may be <b>null</b> if either resource or location is unknown
     */
    public String[] getResourceLocations(String res) {
        String[] result = null;
        String loc = resourceIndex.get(res);
        if (null != loc) {
            result = loc.split(RESOURCE_SEPARATOR);
            for (int r = 0; r < result.length; r++) {
                result[r] = locationIndex.get(result[r]);
            }
        }
        return result;
    }
    
    /**
     * Normalizes {@code path} to URL/jar path notation.
     * 
     * @param path the path to normalize
     * @return the normalized path
     */
    public static String normalize(Path path) {
        return normalize(path.toString());
    }
    
    /**
     * Normalizes {@code path} to URL/jar path notation.
     * 
     * @param path the path to normalize
     * @return the normalized path
     */
    public static String normalize(String path) {
        return path.replace('\\', '/');
    }
    
    /**
     * Returns the files that were used to construct this index via {@link #createIndex(List, Consumer)}.
     * 
     * @return the files, in the actual construction/classpath sequence
     */
    List<String> getFilesList() {
        return files;
    }
    
    /**
     * Returns the files that were used to construct this index via {@link #createIndex(List, Consumer)}.
     * 
     * @return the files, in the actual construction/classpath sequence
     */
    public Iterable<String> getFiles() {
        return files;
    }
    
    /**
     * Substitutes locations in the location index and the files list by those given in {@code mapping}. Use this method
     * to relocate the index.
     * 
     * @param mapping old-new file location mapping, shall be given with "/" as separator; unmatched entries will 
     * be ignored
     */
    public void substituteLocations(Map<String, String> mapping) {
        for (String v : mapping.values()) {
            if (!new File(v).exists()) {
                System.out.println("WARNING: substitution " + v + " does not exist.");
            }
        }
        for (String oldVal : mapping.keySet()) {
            String loc = locationReverseIndex.remove(oldVal);
            if (null != loc) {
                String newVal = mapping.get(oldVal);
                locationReverseIndex.put(newVal, loc);
                locationIndex.put(loc, newVal);
            }
        }
        for (int f = 0; f < files.size(); f++) {
            String loc  = normalize(files.get(f).toString());
            String newLoc = mapping.get(loc);
            if (null != newLoc) {
                files.set(f, newLoc);
            }
        }
    }
    
}