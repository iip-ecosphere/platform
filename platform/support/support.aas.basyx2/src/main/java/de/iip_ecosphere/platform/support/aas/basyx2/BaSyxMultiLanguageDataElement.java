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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;

import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.MultiLanguageDataElement;

/**
 * Wraps a BaSyx multi-language data element.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxMultiLanguageDataElement extends BaSyxDataElement<
    org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty> implements MultiLanguageDataElement {

    /**
     * Creates a multi-language instance.
     * 
     * @param idShort the short id
     * @param texts the texts
     */
    public BaSyxMultiLanguageDataElement(String idShort, Collection<LangString> texts) {
        super(createInstance(idShort, texts));
    }
    
    /**
     * Creates a multi-language data element.
     * 
     * @param dataElement the data element
     */
    protected BaSyxMultiLanguageDataElement(org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty dataElement) {
        super(dataElement);
    }
    
    /**
     * Creates a multi-language property instance.
     * 
     * @param idShort the short id
     * @param texts the texts
     * @return the instance
     */
    private static org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty createInstance(String idShort, 
        Collection<LangString> texts) {
        org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty result 
            = new org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultMultiLanguageProperty();
        result.setIdShort(idShort);
        List<LangStringTextType> ls = new ArrayList<>();
        for (de.iip_ecosphere.platform.support.aas.LangString e : texts) {
            LangStringTextType t = new DefaultLangStringTextType();
            t.setLanguage(e.getLanguage());
            t.setText(e.getDescription());
        }
        result.setValue(ls);
        return result;
    }

}
