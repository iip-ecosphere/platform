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

package de.iip_ecosphere.platform.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.ssehub.easy.varModel.model.ModelElement;
import net.ssehub.easy.varModel.model.Project;

/**
 * General introspective information about the (meta-)model.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ModelInfo {

    private static Map<Project, Boolean> metaProjects = new HashMap<>();
    
    /**
     * Returns the conventional file name of a project.
     * 
     * @param prj the project
     * @return the file name
     */
    public static String getFileName(Project prj) {
        return prj.getName() + ".ivml";
    }
    
    /**
     * Returns whether {@code prj} is a meta project, i.e., one defining the IIPEcosphere meta model.
     * 
     * @param prj the project to check
     * @return {@code true} for meta project, {@code false} if it is no meta but probably a configuration project.
     */
    public static boolean isMetaProject(Project prj) {
        Boolean result = metaProjects.get(prj);
        if (null == result) {
            File base = ConfigurationSetup.getSetup().getEasyProducer().getIvmlMetaModelFolder();
            result = contains(base, getFileName(prj));
            metaProjects.put(prj, result);
        }
        return result;
    }

    /**
     * Returns whether {@code folder} contains a file with name {@code file}.
     * 
     * @param folder the folder
     * @param file the file name
     * @return {@code true} if {@code folder} contains a file with name {@code file}, {@code false} else
     */
    private static boolean contains(File folder, String file) {
        boolean found = false;  
        File[] files = folder.listFiles();
        if (null != files) {
            for (File f: files) {
                if (f.isFile() && f.getName().equals(file)) {
                    found = true;
                    break;
                } else if (f.isDirectory()) {
                    found = contains(f, file);
                    if (found) {
                        break;
                    }
                }
            }
        }
        return found;
    }
    
    /**
     * Returns whether {@code elt} has a comment/description.
     * 
     * @param elt the element to look for
     * @return the comment
     * @see #getCommentSafe(ModelElement)
     */
    public static boolean hasComment(ModelElement elt) {
        return getCommentSafe(elt).length() > 0;
    }
    
    /**
     * Returns the comment of {@code elt}.
     * 
     * @param elt the comment
     * @return the comment, may be empty for none
     */
    public static String getCommentSafe(ModelElement elt) {
        String result = elt.getComment();
        return null == result ? "" : result;
    }

    /**
     * Returns the actual local.
     * 
     * @return the locale
     */
    public static Locale getLocale() {
        return Locale.getDefault(); // if not explicitly changed in EASy, null or default
    }

}
