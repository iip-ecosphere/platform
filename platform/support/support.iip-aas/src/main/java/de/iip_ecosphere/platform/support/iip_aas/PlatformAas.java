/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.iip_aas;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;

/**
 * The platform typeplate.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformAas implements AasContributor {

    public static final String NAME_SUBMODEL = "platform";
    public static final String NAME_PROPERTY_NAME = "name";
    public static final String NAME_PROPERTY_VERSION = "version";
    public static final String NAME_PROPERTY_RELEASE = "isRelease";
    public static final String NAME_PROPERTY_BUILDID = "buildId";
    private static final String MAVEN_SNAPSHOT_POSTFIX = "-SNAPSHOT";

    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null);
        String ver = "??";
        String buildId = "??";
        boolean isRelease = false;
        InputStream is = getClass().getClassLoader().getResourceAsStream("iip-version.properties");
        if (null != is) {
            Properties prop = new Properties();
            try {
                prop.load(is);
                is.close();
            } catch (IOException e) {
            }
            ver = prop.getOrDefault("version", ver).toString();
            if (ver.endsWith(MAVEN_SNAPSHOT_POSTFIX)) {
                ver = ver.substring(0, ver.length() - MAVEN_SNAPSHOT_POSTFIX.length());
            } else {
                isRelease = true;
            }
            buildId = prop.getOrDefault("buildId", buildId).toString();
        }
        smB.createPropertyBuilder(NAME_PROPERTY_NAME)
            .setValue(Type.STRING, "IIP-Ecosphere platform")
            .build();
        smB.createPropertyBuilder(NAME_PROPERTY_VERSION)
            .setValue(Type.STRING, ver)
            .build();
        smB.createPropertyBuilder(NAME_PROPERTY_RELEASE)
            .setValue(Type.BOOLEAN, isRelease)
            .build();
        smB.createPropertyBuilder(NAME_PROPERTY_BUILDID)
            .setValue(Type.STRING, buildId)
            .build();
        smB.build();
        return null;
    }

    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
        // no active AAS
    }

    @Override
    public Kind getKind() {
        return Kind.PASSIVE;
    }

}
