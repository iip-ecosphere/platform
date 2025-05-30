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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.xml.XmlDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.xml.XmlSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;

import de.iip_ecosphere.platform.support.ExtensionBasedFileFormat;
import de.iip_ecosphere.platform.support.FileFormat;
import de.iip_ecosphere.platform.support.aas.Aas;

/**
 * An XML persistence recipe.
 * 
 * @author Holger Eichelberger, SSE
 */
class XmlPersistenceRecipe extends AbstractPersistenceRecipe {

    private static final FileFormat XML = new ExtensionBasedFileFormat("xml", "AAS XML", "AAS in XML");

    /**
     * Creates an instance.
     */
    XmlPersistenceRecipe() {
        super(XML);
    }

    @Override
    public void writeTo(List<Aas> aas, File thumbnail, List<FileResource> resources, File file) throws IOException {
        XmlSerializer serializer = new XmlSerializer();
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
        try {
            XmlDeserializer serializer = new XmlDeserializer();
            result = transform(serializer.read(file), null);
        } catch (DeserializationException e) {
            throw new IOException(e);
        }
        return result;
    }

}
