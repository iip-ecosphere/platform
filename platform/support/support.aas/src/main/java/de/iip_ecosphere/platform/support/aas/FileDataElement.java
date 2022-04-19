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

package de.iip_ecosphere.platform.support.aas;

/**
 * A data element representing a file.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface FileDataElement extends DataElement {

    /**
     * Builds a data element.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface FileDataElementBuilder extends DataElementBuilder<FileDataElement> {
    }
    
    /**
     * Returns the file contents.
     * 
     * @return the contents/value.
     */
    public String getContents();

    /**
     * Changes the file contents.
     * 
     * @param contents the new file contents/value.
     */
    public void setContents(String contents);
    
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
