/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx;

import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.File;

import de.iip_ecosphere.platform.support.aas.FileDataElement;

/**
 * Wraps a BaSyx file data element. Shall be created by respective builder methods.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxFile extends BaSyxDataElement<File> implements FileDataElement {

    /**
     * Creates a BaSyx file.
     * 
     * @param idShort the short id of the data element
     * @param file the file name, relative or absolute with extension
     * @param mimeType the mime type of the file
     */
    public BaSyxFile(String idShort, String file, String mimeType) {
        super(new File(file, mimeType));
        getDataElement().setIdShort(idShort);
    }

    /**
     * Crates a wrapper instance.
     * 
     * @param file the BaSyx file instance
     */
    public BaSyxFile(File file) {
        super(file);
    }
    
    @Override
    public String getFile() {
        return getDataElement().getValue();
    }
    
    @Override
    public void setFile(String file) {
        getDataElement().setValue(file);
    }
    
    @Override
    public String getMimeType() {
        return getDataElement().getMimeType();
    }

    @Override
    public void setMimeType(String mimeType) {
        getDataElement().setMimeType(mimeType);
    }
    
}
