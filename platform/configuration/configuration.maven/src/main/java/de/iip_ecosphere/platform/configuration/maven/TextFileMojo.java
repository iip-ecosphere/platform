/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.maven;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Allows simple line-based modifications to a text file.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo(name = "textFile", defaultPhase = LifecyclePhase.COMPILE)
public class TextFileMojo extends AbstractMojo {

    @Parameter(property = "configuration.textFile.skip", required = false, defaultValue = "false")
    private boolean skip;
    
    @Parameter(property = "configuration.textFile.file", required = true)
    private File file;

    @Parameter(property = "configuration.textFile.prepends", required = false)
    private List<String> prepends;

    @Parameter(property = "configuration.textFile.appends", required = false)
    private List<String> appends;

    @Parameter(property = "configuration.textFile.deletions", required = false)
    private Set<String> deletions;
    
    @Parameter(property = "configuration.textFile.disabled", required = false)
    private Set<String> disabled;

    @Parameter
    private List<ReplacementSpec> replacements;

    public static class ReplacementSpec {
        
        @Parameter(required = false)
        private String id;
        
        @Parameter(required = true)
        private String token;
        
        @Parameter(required = true)
        private String value;
        
        @Parameter(required = false, defaultValue = "")
        private String escapeValueIn;
        
    }
    
    /**
     * Checks whether the given {@code spec} is disabled.
     * 
     * @param spec the replacement specification
     * @return {@code true} for disabled, {@code false} else
     */
    private boolean isDisabled(ReplacementSpec spec) {
        boolean result = false;
        if (spec.id != null && spec.id.length() > 0 && disabled != null) {
            result = disabled.contains(spec.id);
        }
        return result;
    }
    
    /**
     * Applies the specified replacements to {@code line}.
     * 
     * @param line the line to apply the replacements to
     * @return the line with applied replacements
     */
    private String applyReplacements(String line) {
        if (null != replacements) {
            for (ReplacementSpec r : replacements) {
                if (r.token != null && r.value != null && !isDisabled(r)) {
                    String val = r.value;
                    if (r.escapeValueIn != null) {
                        switch (r.escapeValueIn.toLowerCase()) {
                        case "backslashes":
                            val = val.replace("\\", "\\\\");
                            break;
                        case "java":
                            val = StringEscapeUtils.escapeJava(val);
                            break;
                        case "ecma":
                            val = StringEscapeUtils.escapeEcmaScript(val);
                            break;
                        case "json":
                            val = StringEscapeUtils.escapeJson(val);
                            break;
                        default:
                            break;
                        }
                    }
                    line = line.replace(r.token, val);
                }
            }
        }
        return line;
    }
    
    /**
     * Returns whether a collection is not <b>null</b> and not empty.
     * 
     * @param coll the collection to test
     * @return {@code true} for not empty, {@code false} else
     */
    private static boolean isNonEmpty(Collection<?> coll) {
        return coll != null && coll.size() > 0;
    }
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!skip) {
            if (isNonEmpty(replacements) && isNonEmpty(disabled)) {
                getLog().info("Disabled replacement specs: " + disabled);
            }
            File tmp = null;
            try {
                tmp = Files.createTempFile("mvnTextFile", ".txt").toFile();
                Files.copy(file.toPath(), tmp.toPath(), LinkOption.NOFOLLOW_LINKS,
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new MojoExecutionException("Cannot copy source file " + file + ":" + e.getMessage());
            }

            try (LineNumberReader reader = new LineNumberReader(new FileReader(tmp)); 
                PrintWriter out = new PrintWriter(new FileWriter(file))) {
                if (null != prepends) {
                    for (String s : prepends) {
                        out.println(s);
                    }
                }
                String line;
                do {
                    line = reader.readLine();
                    if (null != line) {
                        if (null == deletions || !deletions.contains(String.valueOf(reader.getLineNumber()))) {
                            out.println(applyReplacements(line));
                        }
                    }
                } while (line != null);
                if (null != appends) {
                    for (String s : appends) {
                        out.println(s);
                    }
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Cannot perform modifications:" + e.getMessage());
            }

            FileUtils.deleteQuietly(tmp);
            getLog().info("Modified file " + file);
        }
    }

}
