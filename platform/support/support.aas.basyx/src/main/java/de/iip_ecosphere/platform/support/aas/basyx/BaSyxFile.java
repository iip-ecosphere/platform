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

    public static class BaSyxFileDataElementBuilder implements FileDataElementBuilder {

        private BaSyxSubmodelElementContainerBuilder<?> parentBuilder;
        private BaSyxFile instance;
        
        /**
         * Creates a file data element builder.
         * 
         * @param parentBuilder the parent builder
         * @param idShort the short id
         * @param contents the contents
         * @param mimeType the mime type
         */
        BaSyxFileDataElementBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, String idShort, 
            String contents, String mimeType) {
            this.parentBuilder = parentBuilder;
            this.instance = new BaSyxFile(idShort, contents, mimeType);
        }

        @Override
        public FileDataElement build() {
            return parentBuilder.register(instance);
        }
        
    }
    
    /**
     * Creates a BaSyx file.
     * 
     * @param idShort the short id of the data element
     * @param value the file contents/value
     * @param mimeType the mime type of the file
     */
    public BaSyxFile(String idShort, String value, String mimeType) {
        super(new File(value, mimeType));
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
    public String getContents() {
        return getDataElement().getValue();
    }
    
    @Override
    public void setContents(String contents) {
        getDataElement().setValue(contents);
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
