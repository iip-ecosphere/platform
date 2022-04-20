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

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.FileDataElement;

/**
 * A data element representing a file.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeFileDataElement implements FileDataElement {

    private String idShort;
    private String contents;
    private String mimeType;
    
    /**
     * The element builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class FakeFileDataElementBuilder implements FileDataElementBuilder {

        private FakeSubmodelElementContainerBuilder parent;
        private FakeFileDataElement instance;

        /**
         * Creates a file data element builder.
         * 
         * @param parent the parent builder
         * @param idShort the short id
         * @param contents the contents
         * @param mimeType the mime type
         */
        FakeFileDataElementBuilder(FakeSubmodelElementContainerBuilder parent, String idShort, 
            String contents, String mimeType) {
            this.parent = parent;
            this.instance = new FakeFileDataElement(idShort, contents, mimeType);
        }

        @Override
        public FileDataElement build() {
            return parent.register(instance);
        }
        
    }
    
    /**
     * Creates a fake data element.
     * 
     * @param idShort the short id
     * @param contents the contents
     * @param mimeType the mime type
     */
    FakeFileDataElement(String idShort, String contents, String mimeType) {
        this.idShort = idShort;
        this.contents = contents;
        this.mimeType = mimeType;
    }
    
    @Override
    public String getIdShort() {
        return idShort;
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitDataElement(this);
    }

    @Override
    public void update() {
    }

    @Override
    public String getContents() {
        return contents;
    }

    @Override
    public void setContents(String contents) {
        this.contents = contents;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

}
