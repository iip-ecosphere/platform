/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.easyProducer.ivml;

import static de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationManager.getAasIvmlMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.ZipUtils;
import de.iip_ecosphere.platform.support.aas.AasUtils;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.IdentifierType;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.Operation.OperationBuilder;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.json.JsonResultWrapper;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.transport.status.TaskUtils;

/**
 * AAS mapping utilities.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasMapperUtils {
    
    public static final String PROGRESS_COMPONENT_ID = "Configuration";

    /**
     * Creates an operation builder on {@code smBuilder} with {@code opName} as operation and invocable name using 
     * {@code iCreator} as invocable creator.
     * 
     * @param smBuilder the submodel builder to create the operation on
     * @param opName the operation name used also as invocable name
     * @param iCreator the invocables creator for creating the invocable
     * @return the operation builder instance
     */
    public static OperationBuilder createOperationBuilder(SubmodelBuilder smBuilder, String opName, 
        InvocablesCreator iCreator) {
        return smBuilder.createOperationBuilder(opName)
            .setInvocable(iCreator.createInvocable(opName));
    }
    
    /**
     * Creates a AAS collection representing an IVML type.
     * 
     * @param smBuilder the submodel builder
     * @param typeName the type name (turned into an AAS shortID)
     * @return the collection builder
     */
    public static SubmodelElementCollectionBuilder createTypeCollectionBuilder(SubmodelBuilder smBuilder, 
        String typeName) {
        return smBuilder.createSubmodelElementCollectionBuilder(AasUtils.fixId(typeName));
    }
    
    /**
     * Binds an instantiation operation.
     * 
     * @param sBuilder the server builder
     * @param opName the operation name
     * @param mode the instantiation mode
     * @param withFile if {@code true} bind a second argument as string to pass through the file name, else 
     *     pass no argument/<b>null</b>
     */
    static void bindInstantiationOperation(ProtocolServerBuilder sBuilder, String opName, 
        InstantiationMode mode, Boolean withFile) {
        sBuilder.defineOperation(opName, 
            new JsonResultWrapper(a -> {
                return TaskUtils.executeAsTask(PROGRESS_COMPONENT_ID, 
                    p -> getAasIvmlMapper().instantiate(mode, AasUtils.readString(p), 
                        withFile ? AasUtils.readString(p, 1) : null)
                    , a);
            })
        );
    }
    
    /**
     * Adds an instantiation operation.
     * 
     * @param smBuilder the submodel builder representing the target
     * @param opName the operation name
     * @param iCreator the invocables creator for operations
     * @param aDesc the authentication descriptor
     * @param withFile if {@code true} bind with a String file parameter (second parameter), else bind with 
     *     one parameter
     */
    static void addInstantiateOperation(SubmodelBuilder smBuilder, String opName, InvocablesCreator iCreator, 
        AuthenticationDescriptor aDesc, boolean withFile) {
        OperationBuilder opBuilder = createOperationBuilder(smBuilder, opName, iCreator)
            .addInputVariable("appId", Type.STRING);
        if (withFile) {
            opBuilder.addInputVariable("codeFile", Type.STRING);
        }
        opBuilder.build(Type.STRING, aDesc);
    }
    
    /**
     * Checking and fixing ZIP file conventions for instantiation. The instantiation requires that
     * either the project is directly located in the root folder of the ZIP or in a single contained
     * folder having the same name as the ZIP file. If possible, through renaming/copying, we fix the 
     * second situation here.
     * 
     * @param file the original ZIP file
     * @return the fixed ZIP file, may be {@code file}
     */
    public static File fixZipConvention(File file) {
        File result = file;
        Set<String> folders = new HashSet<String>();
        Set<String> files = new HashSet<String>();
        try {
            ZipUtils.listFiles(new FileInputStream(file), f -> true, f -> {
                String path = f.toString();
                int pos = path.indexOf("/");
                if (pos > 0) {
                    folders.add(path.substring(0, pos));
                } else {
                    files.add(path);
                }
            });
        } catch (IOException e) {
            LoggerFactory.getLogger(AasIvmlMapper.class).warn("Cannot scan ZIP: {}", e.getMessage());
        }
        if (folders.size() == 1 && files.size() == 0) {
            String name = file.getName();
            if (name.endsWith(".zip")) {
                name = name.substring(0, name.length() - 4);
            }
            Optional<String> folder = folders.stream().findAny();
            if (folder.isPresent() && !name.equals(folder.get())) {
                result = new File(FileUtils.getTempDirectory(), folder.get() + ".zip");
                try {
                    Files.copy(file.toPath(), result.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    LoggerFactory.getLogger(AasIvmlMapper.class).info("Zip convention: Using {} instead of {}", 
                        result, file);
                } catch (IOException e) {
                    LoggerFactory.getLogger(AasIvmlMapper.class).warn("Cannot copy ZIP: {}", e.getMessage());
                    result = file; // stay with the original
                }
            }
        }
        return result;
    }
    
    /**
     * Returns whether {@code file} is acceptable for template copying/publishing.
     * 
     * @param file the file to check
     * @param startTime the start timestamp of the generation
     * @return {@code true} for acceptable, {@code false} else
     */
    static boolean acceptTemplateFile(File file, long startTime) {
        boolean accept = file.isDirectory();
        if (!accept && file.getName().endsWith(".zip")) {
            accept = file.getName().startsWith("impl.") || file.getName().startsWith("ApplicationInterfaces.");
            //accept &= file.lastModified() > startTime;
        }
        return accept;
    }

    /**
     * Returns whether {@code string} is a non-empty string.
     * 
     * @param string the string to test
     * @return {@code true} for a non-empty string, {@code false} for an empty string or <b>null</b>
     */
    static boolean isNonEmptyString(String string) {
        return string != null && string.length() > 0;
    }

    /**
     * Turns {@code string} into an identifier, i.e., each non-Java identifier characters into a "_".
     * 
     * @param string the string to be checked
     * @return the validated string, potentially modified to be an id
     */
    static String toId(String string) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (!Character.isJavaIdentifierPart(c)) {
                c = '_';
            }
            result.append(c);
        }
        return result.toString();
    }
    
    
    /**
     * Sets a semantic id if available. 
     * 
     * @param pBuilder the property builder
     * @param semanticId the semantic id, may be <b>null</b>
     */
    static void setSemanticId(PropertyBuilder pBuilder, String semanticId) {
        if (null != semanticId) {
            if (!semanticId.startsWith(IdentifierType.IRDI_PREFIX)) {
                semanticId = IdentifierType.IRDI_PREFIX + semanticId;
            }
            pBuilder.setSemanticId(semanticId);
        }
    }

}
