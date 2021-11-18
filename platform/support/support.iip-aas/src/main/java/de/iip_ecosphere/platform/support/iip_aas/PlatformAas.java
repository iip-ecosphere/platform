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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;

/**
 * The platform name/typeplate.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformAas implements AasContributor {

    public static final String NAME_SUBMODEL = "platform";
    public static final String NAME_PROPERTY_NAME = "name";
    public static final String NAME_PROPERTY_VERSION = "version";
    public static final String NAME_PROPERTY_RELEASE = "isRelease";
    public static final String NAME_PROPERTY_BUILDID = "buildId";
    public static final String NAME_OPERATION_SNAPSHOTAAS = "snapshotAas";
    private static final String MAVEN_SNAPSHOT_POSTFIX = "-SNAPSHOT";

    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null);
        if (smB.isNew()) { // incremental remote deployment, avoid double creation
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
            smB.createOperationBuilder(NAME_OPERATION_SNAPSHOTAAS)
                .addInputVariable("id", Type.STRING)
                .setInvocable(iCreator.createInvocable(NAME_OPERATION_SNAPSHOTAAS))
                .build(Type.STRING);
            smB.build();
        }
        return null;
    }

    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
        sBuilder.defineOperation(NAME_OPERATION_SNAPSHOTAAS, new JsonResultWrapper(p -> { 
            return snapshotAas(AasUtils.readString(p));
        }));
    }
    
    /**
     * Snapshots an AAS, i.e., the {@link AasPartRegistry#getIipAasInstance()}.
     * 
     * @param id an optional id to be placed into the file name, may be <b>null</b> or empty
     * @return the name of the file written, may be empty for none 
     * @throws ExecutionException if there is no IIP AAS instance or writing the instance fails for some reason
     */
    static String snapshotAas(String id) throws ExecutionException {
        String result = "";
        List<Aas> aas = AasPartRegistry.getIipAasInstance();
        if (null != aas) {
            String name = "platform-";
            if (null != id && id.length() > 0) {
                name += id + "-";
            }
           // for now, we just assume that AASX is supported
            name += System.currentTimeMillis() + ".aasx";
            try {
                File file = new File(FileUtils.getTempDirectory(), name);
                AasFactory.getInstance().createPersistenceRecipe().writeTo(aas, file);
                result = file.getAbsolutePath();  
            } catch (IOException e) {
                throw new ExecutionException(e);
            }
        } else {
            throw new ExecutionException("No suitable AAS instance available. Cannot write AAS.", null);
        }
        return result;
    }

    @Override
    public Kind getKind() {
        return Kind.ACTIVE;
    }

    @Override
    public boolean isValid() {
        return true;
    }

}
