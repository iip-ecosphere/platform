/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas;

/**
 * Represents a BLOB.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface BlobDataElement extends DataElement {

    /**
     * Builder for a BLOB data element.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface BlobDataElementBuilder extends DataElementBuilder<BlobDataElement> {

        /**
         * Sets the value of the BLOB as String.
         * 
         * @param value the value
         * @return <b>this</b>
         */
        public BlobDataElementBuilder setValue(String value);

        /**
         * Sets the value of the BLOB as byte array.
         * 
         * @param value the value
         * @return <b>this</b>
         */
        public BlobDataElementBuilder setValue(byte[] value);

    }
    
    /**
     * Returns the BLOB value.
     * 
     * @return the value.
     */
    public String getValue();

    /**
     * Returns the BLOB value.
     * 
     * @return the value.
     */
    public byte[] getValueAsByteArray();

    /**
     * Changes the BLOB value.
     * 
     * @param value the value.
     */
    public void setValue(String value);

    /**
     * Changes the BLOB value as byte array.
     * 
     * @param value the value.
     */
    public void setValue(byte[] value);

    /**
     * Returns mime type of the content of the file.
     * 
     * @return the mime type
     */
    public String getMimeType();
    
    /**
     * Defines the mime type of the content of the file.
     * 
     * @param mimeType the mime type
     */
    public void setMimeType(String mimeType);

}
