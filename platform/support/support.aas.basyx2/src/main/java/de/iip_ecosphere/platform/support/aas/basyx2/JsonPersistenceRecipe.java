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
import java.io.PrintStream;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;

import de.iip_ecosphere.platform.support.ExtensionBasedFileFormat;
import de.iip_ecosphere.platform.support.FileFormat;
import de.iip_ecosphere.platform.support.aas.Aas;

/**
 * Implements the JSON persistence recipe. Unfortunately, so far, BaSyx only supports writing non-connected AAS as far 
 * as we can see.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JsonPersistenceRecipe extends AbstractPersistenceRecipe {

    private static final FileFormat JSON = new ExtensionBasedFileFormat("json", "AAS JSON", "AAS in JSON");

    /**
     * Creates a JSON persistence recipe.
     */
    JsonPersistenceRecipe() {
        super(JSON);
    }
    
    @Override
    public void writeTo(List<Aas> aas, File thumbnail, List<FileResource> resources, File file) throws IOException {
        JsonSerializer serializer = new JsonSerializer();
        Environment env = buildEnvironment(aas);
        try (PrintStream fos = new PrintStream(new FileOutputStream(file))) {
            fos.print(serializer.write(env)); // why is there no write to stream???
        } catch (SerializationException | IOException e) {
            throw new IOException(e);
        }
    }

    @Override
    public List<Aas> readFrom(File file) throws IOException {
        List<Aas> result;
        try (FileInputStream fis = new FileInputStream(file)) {
            JsonDeserializer serializer = new JsonDeserializer();
            result = transform(serializer.read(fis, Environment.class), null);
        } catch (DeserializationException e) {
            throw new IOException(e);
        }
        return result;
    }

}
