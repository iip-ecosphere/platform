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

package test.de.iip_ecosphere.platform.support.fakeAas;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.BlobDataElement;

/**
 * A data element representing a file.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeBlobDataElement implements BlobDataElement {

    private String idShort;
    private byte[] value;
    private String mimeType;
    private String semanticId;
    
    /**
     * The element builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class FakeBlobDataElementBuilder implements BlobDataElementBuilder {

        private FakeSubmodelElementContainerBuilder parent;
        private FakeBlobDataElement instance;

        /**
         * Creates a file data element builder.
         * 
         * @param parent the parent builder
         * @param idShort the short id
         * @param contents the contents
         * @param mimeType the mime type
         */
        FakeBlobDataElementBuilder(FakeSubmodelElementContainerBuilder parent, String idShort, 
            String contents, String mimeType) {
            this.parent = parent;
            this.instance = new FakeBlobDataElement(idShort, contents, mimeType);
        }

        @Override
        public BlobDataElement build() {
            if (instance.value == null) {
                LoggerFactory.getLogger(getClass()).warn("Blob data element has null value");
            }
            return parent.register(instance);
        }

        @Override
        public DataElementBuilder<BlobDataElement> setSemanticId(String semanticId) {
            instance.semanticId = semanticId;
            return this;
        }

        @Override
        public BlobDataElementBuilder setValue(String value) {
            instance.value = value.getBytes();
            return this;
        }

        @Override
        public BlobDataElementBuilder setValue(byte[] value) {
            instance.value = value;
            return this;
        }
        
    }
    
    /**
     * Creates a fake data element.
     * 
     * @param idShort the short id
     * @param value the value
     * @param mimeType the mime type
     */
    FakeBlobDataElement(String idShort, String value, String mimeType) {
        this.idShort = idShort;
        this.value = null == value ? null : value.getBytes();
        this.mimeType = mimeType;
    }
    
    @Override
    public String getIdShort() {
        return idShort;
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitBlobDataElement(this);
    }

    @Override
    public void update() {
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String getValue() {
        return new String(value);
    }

    @Override
    public byte[] getValueAsByteArray() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value.getBytes();
    }

    @Override
    public void setValue(byte[] value) {
        this.value = value;
    }
    
    @Override
    public String getSemanticId(boolean stripPrefix) {
        return semanticId;
    }

    @Override
    public void setSemanticId(String semanticId) {
        this.semanticId = semanticId;
    }

}
