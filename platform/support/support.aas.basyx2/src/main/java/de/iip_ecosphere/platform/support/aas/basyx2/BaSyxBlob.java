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

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.BlobDataElement;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Wraps a BaSyx BLOB data element. Shall be created by respective builder methods.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxBlob extends BaSyxDataElement<org.eclipse.digitaltwin.aas4j.v3.model.Blob> 
    implements BlobDataElement {
    
    /**
     * BaSyx BLOB builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxBlobDataElementBuilder implements BlobDataElementBuilder {
        
        private BaSyxSubmodelElementContainerBuilder<?> parentBuilder;
        private BaSyxBlob instance;
        private boolean hasValue = false;
        
        /**
         * Creates a BLOB data element builder.
         * 
         * @param parentBuilder the parent builder
         * @param idShort the short id
         * @param value the value, may be <b>null</b> for none
         * @param mimeType the mime type
         */
        BaSyxBlobDataElementBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, 
            String idShort, String value, String mimeType) {
            this.parentBuilder = parentBuilder;
            instance = new BaSyxBlob(idShort, value, mimeType);
            hasValue = value != null;
        }
        
        @Override
        public BlobDataElementBuilder setValue(String value) {
            hasValue = value != null;
            instance.getDataElement().setValue(null == value ? null : value.getBytes());
            return this;
        }

        @Override
        public BlobDataElementBuilder setValue(byte[] value) {
            hasValue = value != null;
            instance.getDataElement().setValue(value);
            return this;
        }

        @Override
        public BlobDataElementBuilder setSemanticId(String semanticId) {
            return Tools.setSemanticId(this, semanticId, instance.getDataElement());
        }

        @Override
        public BlobDataElement build() {
            if (!hasValue) {
                LoggerFactory.getLogger(getClass()).warn("Blob data element has null value");
            }
            return updateInBuild(true, parentBuilder.register(instance));
        }
        
    }
    
    /**
     * Creates a BaSyx BLOB.
     * 
     * @param idShort the short id of the data element
     * @param value the value, may be <b>null</b> for none
     * @param mimeType the mime type of the file
     */
    public BaSyxBlob(String idShort, String value, String mimeType) {
        super(new org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultBlob());
        getDataElement().setIdShort(idShort);
        setMimeType(mimeType);
        if (null != value) {
            setValue(value);
        }
    }
    
    /**
     * Crates a wrapper instance.
     * 
     * @param blob the BaSyx blob
     */
    public BaSyxBlob(org.eclipse.digitaltwin.aas4j.v3.model.Blob blob) {
        super(blob);
    }        

    @Override
    public String getValue() {
        return new String(getDataElement().getValue());
    }

    @Override
    public byte[] getValueAsByteArray() {
        return getDataElement().getValue();
    }

    @Override
    public void setValue(String value) {
        getDataElement().setValue(value.getBytes());
        updateConnectedSubmodelElement();
    }

    @Override
    public void setValue(byte[] value) {
        getDataElement().setValue(value);
        updateConnectedSubmodelElement();
    }

    @Override
    public String getMimeType() {
        return getDataElement().getContentType();
    }

    @Override
    public void setMimeType(String mimeType) {
        getDataElement().setContentType(mimeType);
        updateConnectedSubmodelElement();
    }
    
    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitBlobDataElement(this);
    }

}
