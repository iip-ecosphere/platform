/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.AASXDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.AASXSerializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.InMemoryFile;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultResource;

import de.iip_ecosphere.platform.support.ExtensionBasedFileFormat;
import de.iip_ecosphere.platform.support.FileFormat;
import de.iip_ecosphere.platform.support.aas.Aas;

/**
 * Persistence recipe for AASX.
 * 
 * @author Holger Eichelberger, SSE
 */
class AasxPersistenceRecipe extends AbstractPersistenceRecipe {

    private static final FileFormat AASX = new ExtensionBasedFileFormat("aasx", "AASX", "AASX package");
    
    /**
     * Creates a recipe instance.
     */
    AasxPersistenceRecipe() {
        super(AASX);
    }
    
    @Override
    public void writeTo(List<Aas> aas, File thumbnail, List<FileResource> resources, File file) throws IOException {
        AASXSerializer serializer = new AASXSerializer();
        // TODO preliminary, change interface

        Collection<InMemoryFile> inMemoryFiles = new ArrayList<>();
        if (null != thumbnail && thumbnail.exists()) {
            String name = thumbnail.getName();
            String extension = "";
            int pos = name.lastIndexOf(".");
            if (pos > 0 && pos < name.length() - 1) {
                extension = name.substring(pos + 1);
            }
            extension = extension.toUpperCase();
            String path = file.getName(); // TODO check
            DefaultResource tRes = new DefaultResource.Builder()
                .contentType(extension)
                .path(path)
                .build();
            for (Aas a : aas) {
                AssetInformation info = (((AbstractAas<?>) a).getAas()).getAssetInformation();
                if (null != info && info.getDefaultThumbnail() != null) {
                    info.setDefaultThumbnail(tRes);
                }
            }
            inMemoryFiles.add(new InMemoryFile(FileUtils.readFileToByteArray(thumbnail), path));
        }
        Environment env = buildEnvironment(aas);
        if (null != resources) {
            for (FileResource f: resources) {
                inMemoryFiles.add(new InMemoryFile(f.getFileContent(), f.getPath()));
            }
        }
        try (FileOutputStream out = new FileOutputStream(file)) {
            serializer.write(env, inMemoryFiles, out);
        } catch (SerializationException | IOException e) {
            throw new IOException(e);
        }
    }

    @Override
    public List<Aas> readFrom(File file) throws IOException {
        List<Aas> result;
        try (FileInputStream in = new FileInputStream(file)) {
            AASXDeserializer serializer = new AASXDeserializer(in);
            result = transform(serializer.read(), serializer.getRelatedFiles());
        } catch (InvalidFormatException | DeserializationException | IOException e) {
            throw new IOException(e);
        }
        return result;
    }

}
