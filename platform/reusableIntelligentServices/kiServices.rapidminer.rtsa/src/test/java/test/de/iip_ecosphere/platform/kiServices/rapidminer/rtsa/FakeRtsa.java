/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.kiServices.rapidminer.rtsa;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.support.JarUtils;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;
import de.iip_ecosphere.platform.support.iip_aas.config.CmdLine;
import spark.Route;
import spark.Spark;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.delete;

/**
 * A very simple RTSA fake server as we are not allowed to publish RTSA. The FakeRTSA reads its functionality out
 * of a spec.yml file in the deployment.jar (packaged into a zip, there into folder home/deployments). The spec.yml is
 * intended to quickly adjust the behavior rather than doing coding/requiring a build process.
 * 
 * Format of the spec.yml:
 * path: <String>
 * mappings:
 *   <String>: <String>
 *  
 * The path indicates the desired REST path/endpoint attached to the base path services/
 * The mappings relate a field name to a function specification. As function specification, we currently offer 
 * PASS, SKIP, RANDOM_BOOLEAN, RANDOM_PROBABILITY, RANDOM_SELECT(args) whereby args are separated by comma, may be 
 * strings. Fields not given in the data but specified in spec.yml will be added to the output. 
 * 
 * For new classes in this package, please consider that the POM is doing a selective packaging for Fake*.class so 
 * either classes are contained/nested or their names start with Fake!
 * 
 * @author Holger Eichelberger, SSE
 * @author Ahmad Alamoush, SSE
 */
public class FakeRtsa {

    private static Random random = new Random();
    private static Map<String, FunctionMapping> functions = new HashMap<>();
    
    static {
        functions.put("PASS", p -> p);
        functions.put("SKIP", p -> null);
        functions.put("RANDOM_PROBABILITY", p -> random.nextDouble());
        functions.put("RANDOM_BOOLEAN", p -> random.nextBoolean());
        functions.put("RANDOM_SELECT", new RandomArgumentSelector());
    }

    /**
     * Executes the fake server.
     * 
     * @param args ignored
     * @throws IOException shall not occur
     */
    public static void main(String[] args) throws IOException {
        int serverPort = Integer.parseInt(System.getProperty("server.port", "8090")); 
        String defaultPath = CmdLine.getArg(args, "iip.rtsa.path", "iip_basic/score_v1");
        boolean verbose = CmdLine.getBooleanArg(args, "verbose", true);
        boolean waitAtStart = CmdLine.getBooleanArg(args, "waitAtStart", true);
        File baseDir = new File(System.getProperty("scoring-agent.baseDir", "."));
        System.out.println("This is FakeRtsa on port: " + serverPort + " with basedir " + baseDir);
        extractDeployments(baseDir);
        List<Deployment> deployments = loadDeployments(baseDir, defaultPath);
        for (Deployment d : deployments) {
            
            Route route = (req, res) -> { 
                String request = lines(req.body()).collect(Collectors.joining("\n"));
                if (verbose) {
                    System.out.println("FakeRtsa Received Request: " + request);
                }
                String respText = createResponse(request, d);
                res.body(respText);
                res.status(200);
                return res.body();
            };
            
            Spark.port(serverPort);
            String path = d.getPath();
            post("/services/" + path, route);
            get("/services/" + path, route);
            put("/services/" + path, route); // whyever
            delete("/services/" + path, route); // whyever
        }
        
        new Thread(() -> {
            if (waitAtStart) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // puuh
                }
                System.out.println("Started Application in 2500 ms"); // we need some output for state change
            } else {
                System.out.println("Started Application in 50 ms"); // we need some output for state change
            }
        }).start();
    }
    
    /**
     * Extracts the deployments.
     * 
     * @param baseDir the RTSA base directory
     */
    private static void extractDeployments(File baseDir) {
        File dep = new File(baseDir, "deployments");
        System.out.println("Extracting deployments from " + dep.getAbsolutePath());
        Path baseDirPath = baseDir.toPath();
        File[] deps = dep.listFiles();
        if (null != deps) {
            for (File d : deps) {
                if (d.getName().endsWith(".zip")) {
                    try {
                        FileInputStream fis = new FileInputStream(d);
                        JarUtils.extractZip(fis, baseDirPath);
                        fis.close();
                        System.out.println(" - unzipped: " + d.getName());
                    } catch (IOException e) {
                        System.out.println(" - Cannot unzip deployment " + d.getName() + ": " + e.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Represents a deployment specification.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class Deployment {
        private File file;
        private URLClassLoader loader;
        private String path;
        private Spec spec;
        
        /**
         * Creates a deployment specification.
         * 
         * @param file the file containing the deployment, must be a JAR
         * @param defaultPath in case that no path is specified in the JAR
         * @throws IOException if reading the file or attaching it to a class loader fails
         */
        private Deployment(File file, String defaultPath) throws IOException {
            this.file = file;
            this.loader = new URLClassLoader(new URL[] {file.toURI().toURL()});
            this.path = defaultPath;
            
            readSpec(loader.getResourceAsStream("spec.yml"));
        }
        
        /**
         * Reads the YAML specification file "spec.yml" in the deployment file.
         * 
         * @param in the input stream
         * @throws IOException
         */
        private void readSpec(InputStream in) throws IOException {
            if (null == in) {
                System.out.println("No spec in deployment " + file.getName() + ". Assuming defaults.");
            } else {
                spec = AbstractSetup.readFromYaml(Spec.class, in);
                if (null != path && path.length() > 0) {
                    path = spec.getPath();
                }
            }
        }
        
        /**
         * Returns the desired REST path.
         * 
         * @return the rest path
         */
        public String getPath() {
            return path;
        }

        /**
         * Returns a function mapping. [snakeyaml]
         * 
         * @param field the field to return the mapping for
         * @return the mapping or <b>null</b> for none/pass on
         */
        public FunctionMapping getMapping(String field) {
            FunctionMapping result = functions.get("PASS");
            if (null != spec) {
                String funcSpec = spec.getMappings().get(field);
                if (null != funcSpec) {
                    String[] args = null;
                    String funcName = funcSpec;
                    int argsStartPos = funcSpec.indexOf('(');
                    int argsEndPos = funcSpec.lastIndexOf(')');
                    if (argsStartPos > 1 && argsEndPos > 0) { // there shall be at least a "name"
                        funcName = funcSpec.substring(0, argsStartPos);
                        String argsTmp = "";
                        if (argsStartPos + 1 < argsEndPos) { // there shall be a bit of args
                            argsTmp = funcSpec.substring(argsStartPos + 1, argsEndPos);
                        }
                        args = argsTmp.split(","); // rather simple for now 
                    }
                    FunctionMapping fm = functions.get(funcName);
                    if (null != fm) {
                        result = fm.bind(args);
                    }
                }
            }
            return result;
        }

        /**
         * Returns all function mappings.
         * 
         * @return the mappings (data field name vs. function spec)
         */
        public Map<String, String> getMappings() {
            return null == spec ? new HashMap<>() : spec.getMappings();
        }
        
    }
    
    /**
     * Represents a function mapping.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface FunctionMapping {

        /**
         * Maps an input value to an output value.
         * 
         * @param value the input value
         * @return the output value
         */
        public Object map(Object value);
        
        /**
         * Binds the arguments to that function mapping and creates a new one.
         * 
         * @param args the args to be bound, may be <b>null</b> then result shall be <b>this</b>
         * @return by default <b>this</b>
         */
        public default FunctionMapping bind(String[] args) {
            return this; // no binding by default
        }
        
    }
    
    /**
     * A simple random argument selector.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class RandomArgumentSelector implements FunctionMapping {

        private String[] args;
        
        /**
         * Creates the prototype instance.
         */
        public RandomArgumentSelector() {
        }

        /**
         * Creates a random argument selector with arguments for binding.
         * 
         * @param args the arguments
         */
        private RandomArgumentSelector(String[] args) {
            this.args = args;
        }

        @Override
        public Object map(Object value) {
            Object result;
            if (null == args) {
                result = value;
            } else {
                result = args[random.nextInt(args.length)];
            }
            return result;
        }

        @Override
        public FunctionMapping bind(String[] args) {
            return new RandomArgumentSelector(args);
        }

    }

    /**
     * Represents a simple function mapping. Data fields not specified are passed on for convenience. 
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum FunctionMapping1 {
        
        /**
         * Returns a random number between 0 and 1.
         */
        RANDOM_PROBABILITY,
        
        /**
         * Returns a random Boolean.
         */
        RANDOM_BOOLEAN,
        
        /**
         * Skips the field, i.e., does not pass it on.
         */
        SKIP,
        
        /**
         * Passes the field on to the output.
         */
        PASS
    }
    
    /**
     * Represents the contents of a spec.yml file in a deployment.
     * The {@link #getPath()} will become a subpath of "/services".
     * 
     * @author Holger Eichelberger, SSE
     */
    public static final class Spec {
        
        private String path = "";
        private Map<String, String> mappings = new HashMap<>();
        
        /**
         * Returns the desired REST path.
         * 
         * @return the rest path
         */
        public String getPath() {
            return path;
        }

        /**
         * Defines the desired REST path. [snakeyaml]
         * 
         * @param path the REST path
         */
        public void setPath(String path) {
            this.path = path;
        }

        /**
         * Returns the function mappings. [snakeyaml]
         * 
         * @return the mappings (data field name vs. function spec)
         */
        public Map<String, String> getMappings() {
            return mappings;
        }
        
        /**
         * Sets the function mappings. [snakeyaml]
         * 
         * @param mappings the mappings (data field name vs. function spec)
         */
        public void setMappings(Map<String, String> mappings) {
            this.mappings = mappings;
        }
        
    }
    
    /**
     * Loads the unpacked deployments.
     * 
     * @param baseDir the base directory
     * @param defaultPath the default path if no one is specified
     * @return the deployments
     */
    private static List<Deployment> loadDeployments(File baseDir, String defaultPath) {
        System.out.println("Loading deployments:");
        File dep = new File(baseDir, "home/deployments");
        List<Deployment> result = new ArrayList<>();
        File[] deps = dep.listFiles();
        if (null != deps) {
            for (File d : deps) {
                if (d.getName().endsWith(".jar")) {
                    try {
                        result.add(new Deployment(d, defaultPath));
                        System.out.println(" - added: " + d.getName());
                    } catch (IOException e) {
                        System.out.println(" - cannot add " + d.getName() + ": " + e.getMessage());
                    }
                } else {
                    System.out.println(" - cannot add " + d.getName() + " as no JAR");
                }
            }
        }
        return result;
    }
    
    
    /**
     * Replacement for Java 11 {@code String.lines()}.
     * 
     * @param string the string to stream
     * @return the streamed string in terms of individual lines
     */
    public static Stream<String> lines(String string) {
        return Stream.of(string.replace("\r\n", "\n").split("\n"));
    }

    /**
     * Represents RTSA input.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Input {
        
        private Map<String, Object>[] data;
        
        /**
         * Changes the data.
         * 
         * @param data the data
         */
        public void setData(Map<String, Object>[] data) {
            this.data = data;
        }

        /**
         * Returns the data.
         * 
         * @return the data
         */
        public Map<String, Object>[] getData() {
            return data;
        }

    }
    
    /**
     * Creates a fake response. We do not have a JSON parser available unless we add libraries to the fake RTSA.
     * 
     * @param request the request
     * @param deployment the deployment spec 
     * @return the response
     */
    private static String createResponse(String request, Deployment deployment) {
        String result;
        try {
            ObjectMapper mapper = new ObjectMapper();
            Input input = mapper.readValue(request, Input.class);
            if (input.data != null) {
                for (Map<String, Object> values : input.data) {
                    Set<String> done = new HashSet<String>();
                    Set<String> toRemove = new HashSet<String>();
                    for (Map.Entry<String, Object> ent : values.entrySet()) {
                        done.add(ent.getKey());
                        FunctionMapping fm = deployment.getMapping(ent.getKey());
                        if (null != fm) {
                            Object newValue = fm.map(ent.getValue());
                            if (null != newValue) {
                                ent.setValue(newValue);
                            } else {
                                toRemove.add(ent.getKey());
                            }
                        }
                    }
                    for (String field : deployment.getMappings().keySet()) {
                        if (!done.contains(field)) {
                            FunctionMapping fm = deployment.getMapping(field);    
                            if (null != fm) {
                                Object newValue = fm.map(null);
                                if (null != newValue) {
                                    values.put(field, newValue);
                                }
                            }
                        }
                    }
                    for (String key : toRemove) {
                        values.remove(key);
                    }
                }
            }
            result = mapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            System.out.println("Cannot read input: " + request + ": " + e.getMessage());
            result = request;
        }
        return result;
    }

}
