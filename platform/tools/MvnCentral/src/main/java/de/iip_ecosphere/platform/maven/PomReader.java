package de.iip_ecosphere.platform.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Implements some functions regarding pom files. Taken over from Qualimaster.
 * @author Patu
 *
 */
public class PomReader {

    /**
     * Simple wrapper class for extracted information.
     * @author Patu
     *
     */
    public static class PomInfo {
        
        private String fullPath = null;
        private String groupId = null;
        private String artifactId = null;
        private String version = null;
        private String parentGroupId = null;
        private String parentArtifactId = null;
        private String parentVersion = null;
        
        /**
         * Returns the fullPath.
         * 
         * @return the fullPath.
         */
        public String getFullPath() {
            return this.fullPath;
        }
        
        /**
         * Returns the GroupId.
         * 
         * @return the GroupId.
         */
        public String getGroupId() {
            return null == groupId ? parentGroupId : groupId;
        }

        /**
         * Returns the optional parent GroupId.
         * 
         * @return the parent GroupId (may be <b>null</b>).
         */
        public String getParentGroupId() {
            return parentGroupId;
        }

        /**
         * Returns the GroupId as path.
         * 
         * @return the GroupId as path (i.e. eu/qualimaster/).
         */
        public String getGroupPath() {
            String result = "";
            for (String s : getGroupId().split("\\.")) {
                result += s + "/";
            }
            return result;
        }
        
        /**
         * Returns the ArtifactId.
         * 
         * @return the ArtifactId.
         */
        public String getArtifactId() {
            return artifactId;
        }
        
        /**
         * Returns the optional parent ArtifactId.
         * 
         * @return the parent ArtifactId (may be <b>null</b>).
         */
        public String getParentArtifactId() {
            return parentArtifactId;
        }
        
        /**
         * Returns the Version.
         * 
         * @return the Version.
         */
        public String getVersion() {
            return null == version ? parentVersion : version;
        }

        /**
         * Returns the optional parent Version.
         * @return the parent version (may be <b>null</b>).
         */
        public String getParentVersion() {
            return parentVersion;
        }

        @Override
        public String toString() {
            return getGroupId() + ":" + getArtifactId() + ":" + this.version;
        }
        
    }
    
    /**
     * Returns the full classpath from a pom file.
     * @param file The pom file to read from.
     * @return The full classpath or NULL if no classpath was available.
     */
    public static PomInfo getInfo(File file) {
        
        //initialize
        final PomInfo info = new PomInfo();
        try {
            readPom(file, new PomHandler() {
                
                @Override
                public void handlePomVersion(Node node, String value) {
                    info.version = value;
                }
                
                @Override
                public void handlePomGroupId(Node node, String value) {
                    info.groupId = value;
                }
                
                @Override
                public void handlePomArtifactId(Node node, String value) {
                    info.artifactId = value;
                }

                @Override
                public void handleParentPomGroupId(Node node, String groupId) {
                    info.parentGroupId = groupId;
                }

                @Override
                public void handleParentPomArtifactId(Node node, String artifactId) {
                    info.parentArtifactId = artifactId;
                }

                @Override
                public void handleParentPomVersion(Node node, String version) {
                    info.parentVersion = version;
                }

                @Override
                public void handleParentPom(Node node) {
                    // ignore
                }

                @Override
                public boolean wasModified() {
                    return false;
                }
                
            });
        } catch (IOException e) {
            System.out.println("FAILED! " + e.getMessage());   
        }
        PomInfo result;
        //generate the full path if possible (example: de.uni-hildesheim.sse.ivml)
        if (null != info.getGroupId() && null != info.getArtifactId()) {
            result = info;
            result.fullPath = result.getGroupId() + "." + result.getArtifactId();
        } else {
            result = null;
        }
        
        return result;
        
    }
    
    /**
     * A simplified POM reading handler.
     * 
     * @author Holger Eichelberger, SSE
     */
    private interface PomHandler {
        
        /**
         * Whether the POM was modified and shall be re-written.
         * 
         * @return {@code true} for modified, {@code false} else
         */
        public boolean wasModified();
        
        /**
         * Called for the POM group id.
         * 
         * @param node the node
         * @param groupId the group id
         */
        public void handlePomGroupId(Node node, String groupId);

        /**
         * Called for the POM artifact id.
         * 
         * @param node the node
         * @param artifactId the artifact id
         */
        public void handlePomArtifactId(Node node, String artifactId);

        /**
         * Called for the POM version.
         * 
         * @param node the node
         * @param version the version
         */
        public void handlePomVersion(Node node, String version);

        /**
         * Called for the optional parent POM group id.
         * 
         * @param node the node
         * @param groupId the group id
         */
        public void handleParentPomGroupId(Node node, String groupId);

        /**
         * Called for the optional parent POM artifact id.
         * 
         * @param node the node
         * @param artifactId the artifact id
         */
        public void handleParentPomArtifactId(Node node, String artifactId);

        /**
         * Called for the optional parent POM version.
         * 
         * @param node the node
         * @param version the version
         */
        public void handleParentPomVersion(Node node, String version);
        
        /**
         * Called for an optional POM parent.
         * 
         * @param node the node
         */
        public void handleParentPom(Node node);
        
    }
    
    /**
     * Checks the given strings for equality but only if none is <b>null</b>.
     * 
     * @param s1 the first string
     * @param s2 the second string
     * @return {@code true} if both are equal, {@code false} else
     */
    public static boolean equalsSafe(String s1, String s2) {
        boolean result = false;
        if (s1 != null && s2 != null) {
            result = s1.equals(s2);
        }
        return result;
    }

    /**
     * Checks whether {@code s1} starts with {@code s2} but only if none is <b>null</b>.
     * 
     * @param s1 the containing string
     * @param s2 the prefix string
     * @return {@code true} if {@code s1} starts with {@code s2}, {@code false} else
     */
    private static boolean startsWithSafe(String s1, String s2) {
        boolean result = false;
        if (s1 != null && s2 != null) {
            result = s1.startsWith(s2);
        }
        return result;
    }

    /**
     * Reads a POM file and informs the handler. Writes back the POM to {@code file} if {@code handler}
     * indicates a modification.
     * 
     * @param file the file to read
     * @param handler the handler
     * @throws IOException if reading fails
     */
    private static void readPom(File file, PomHandler handler) throws IOException {
        DocumentBuilderFactory factory = null;
        DocumentBuilder builder = null;
        Document doc = null;
        String modelVersion = null;
        
        //parse the file
        if (file != null && file.exists() && file.length() > 0) {
            try { 
                factory = DocumentBuilderFactory.newInstance();
                builder = factory.newDocumentBuilder();
                doc = builder.parse(file);
            } catch (SAXException exc) {
                throw new IOException(exc);
            } catch (ParserConfigurationException e) {
                throw new IOException(e);
            }
            
            Element root = doc.getDocumentElement();
            if (!(equalsSafe("project", root.getNodeName()) 
                && startsWithSafe(root.getAttribute("xmlns"), "http://maven.apache.org/POM"))) { // heuristic
                throw new IOException("No POM file.");
            }
            NodeList list = root.getChildNodes();
            //read in the pom information if available.
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                String nodeName = node.getNodeName();
                if (nodeName.equals("groupId")) {
                    handler.handlePomGroupId(node, node.getTextContent());
                } else if (nodeName.equals("artifactId")) {
                    handler.handlePomArtifactId(node, node.getTextContent());
                } else if (nodeName.equals("version")) {
                    handler.handlePomVersion(node, node.getTextContent());
                } else if (nodeName.equals("parent")) {
                    parseParent(node, handler);
                } else if (nodeName.equals("modelVersion")) {
                    modelVersion = node.getTextContent();
                }
            }
            if (null == modelVersion) {
                throw new IOException("No POM file: No model version found");
            }
            if (!equalsSafe(modelVersion, "4.0.0")) {
                throw new IOException("Unsupported POM model version: " + modelVersion);
            }
            
            if (handler.wasModified()) {
                try {
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    FileWriter writer = new FileWriter(file);
                    StreamResult result = new StreamResult(writer);
                    DOMSource source = new DOMSource(doc);
                    transformer.transform(source, result);
                } catch (TransformerException e) {
                    throw new IOException(e);
                }
            }
        } else {
            throw new FileNotFoundException();
        }
    }
    
    /**
     * Parses a POM parent information.
     * 
     * @param node the node identified as parent
     * @param handler the POM handler
     */
    private static void parseParent(Node node, PomHandler handler) {
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);
            String nodeName = child.getNodeName();
            if (nodeName.equals("groupId")) {
                handler.handleParentPomGroupId(child, child.getTextContent());
            } else if (nodeName.equals("artifactId")) {
                handler.handleParentPomArtifactId(child, child.getTextContent());
            } else if (nodeName.equals("version")) {
                handler.handleParentPomVersion(child, child.getTextContent());
            }
        }
        handler.handleParentPom(node);
    }
    
    /**
     * Replaces POM versions.
     * 
     * @param file the POM file
     * @param oldPomVersion the old POM version (may be <b>null</b> to ignore)
     * @param newPomVersion the new POM version (may be <b>null</b> to ignore)
     * @param oldParentPomVersion the old parent POM version (may be <b>null</b> to ignore)
     * @param newParentPomVersion the new parent POM version (may be <b>null</b> to ignore)
     * @return whether {@code file} was modified
     * @throws IOException if reading/writing fails
     */
    public static boolean replaceVersion(File file, String oldPomVersion, String newPomVersion, 
        String oldParentPomVersion, String newParentPomVersion) throws IOException {
        
        PomHandler handler = new PomHandler() {
            
            private boolean modified = false;

            @Override
            public void handlePomGroupId(Node node, String groupId) {
            }

            @Override
            public void handlePomArtifactId(Node node, String artifactId) {
            }

            @Override
            public void handlePomVersion(Node node, String version) {
                if (null != oldPomVersion && null != newPomVersion) {
                    if (equalsSafe(version, oldPomVersion)) {
                        node.setTextContent(newPomVersion);
                        modified = true;
                    }
                }
            }

            @Override
            public void handleParentPomGroupId(Node node, String groupId) {
            }

            @Override
            public void handleParentPomArtifactId(Node node, String artifactId) {
            }

            @Override
            public void handleParentPomVersion(Node node, String version) {
                if (null != oldParentPomVersion && null != newParentPomVersion) {
                    if (equalsSafe(version, oldParentPomVersion)) {
                        node.setTextContent(newParentPomVersion);
                        modified = true;
                    }
                }
            }

            @Override
            public void handleParentPom(Node node) {
            }
            
            @Override
            public boolean wasModified() {
                return modified;
            }

        };
        readPom(file, handler);
        return handler.wasModified();
    }
    
}
