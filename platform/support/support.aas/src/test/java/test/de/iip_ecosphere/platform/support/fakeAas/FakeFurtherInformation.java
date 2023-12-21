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

package test.de.iip_ecosphere.platform.support.fakeAas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.FurtherInformation;

/**
 * A fake further information.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeFurtherInformation extends FakeSubmodelElementCollection implements FurtherInformation {
    
    private Map<String, Collection<LangString>> statements;
    private XMLGregorianCalendar validDate;
    
    /**
     * A fake builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class FakeFurtherInformationBuilder extends FakeSubmodelElementCollectionBuilder 
        implements FurtherInformationBuilder {

        private FakeFurtherInformation instance;

        /**
         * Creates an instance.
         * 
         * @param parent the parent builder
         */
        protected FakeFurtherInformationBuilder(FakeSubmodelElementContainerBuilder parent) {
            super(parent, "FurtherInformation", false, false);
        }

        @Override
        protected FakeSubmodelElementCollection createInstance(String idShort) {
            instance = new FakeFurtherInformation(idShort);
            return instance;
        }

        @Override
        public FurtherInformationBuilder addStatement(String name, LangString statement) {
            if (null == instance.statements) {
                instance.statements = new HashMap<>();
            }
            Collection<LangString> coll = instance.statements.get(name);
            if (null == coll) {
                coll = new ArrayList<LangString>();
                instance.statements.put(name, coll);
            }
            coll.add(statement);
            return this;
        }
        
    }

    /**
     * Creates an instance.
     * 
     * @param idShort the idShort
     */
    protected FakeFurtherInformation(String idShort) {
        super(idShort);
    }

    @Override
    public XMLGregorianCalendar getValidDate() {
        return validDate;
    }

    @Override
    public void setValidDate(XMLGregorianCalendar validDate) {
        this.validDate = validDate;
    }

    @Override
    public Map<String, Collection<LangString>> getStatements() {
        return statements;
    }

    @Override
    public void setStatements(Map<String, Collection<LangString>> statements) {
        this.statements = statements;
    }

}
