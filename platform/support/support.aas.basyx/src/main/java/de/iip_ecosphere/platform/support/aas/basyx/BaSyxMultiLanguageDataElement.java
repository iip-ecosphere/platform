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

import java.util.Collection;

import org.eclipse.basyx.submodel.metamodel.map.qualifier.LangStrings;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.MultiLanguageProperty;

import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.MultiLanguageDataElement;

/**
 * Wraps a BaSyx multi-language data element.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxMultiLanguageDataElement extends BaSyxDataElement<MultiLanguageProperty> 
    implements MultiLanguageDataElement {

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
    protected BaSyxMultiLanguageDataElement(MultiLanguageProperty dataElement) {
        super(dataElement);
    }
    
    /**
     * Creates a multi-language property instance.
     * 
     * @param idShort the short id
     * @param texts the texts
     * @return the instance
     */
    private static MultiLanguageProperty createInstance(String idShort, Collection<LangString> texts) {
        MultiLanguageProperty result = new MultiLanguageProperty(idShort);
        LangStrings ls = new LangStrings();
        for (de.iip_ecosphere.platform.support.aas.LangString e : texts) {
            ls.add(new org.eclipse.basyx.submodel.metamodel.map.qualifier.LangString(
                e.getLanguage(), e.getDescription()));
        }
        result.setValue(ls);
        return result;
    }

}
