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

package de.iip_ecosphere.platform.support.semanticId.eclass;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.ws.rs.ProcessingException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.identities.IdentityStore;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.semanticId.DefaultSemanticIdResolutionResult;
import de.iip_ecosphere.platform.support.semanticId.DefaultSemanticIdResolutionResult.DefaultNaming;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolutionResult;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolver;
import de.iip_ecosphere.platform.support.semanticId.eclass.api.EclassJsonReadServicesApi;
import de.iip_ecosphere.platform.support.semanticId.eclass.handler.ApiException;
import de.iip_ecosphere.platform.support.semanticId.eclass.model.ReadProperty;
import de.iip_ecosphere.platform.support.semanticId.eclass.model.ReadUnit;
import de.iip_ecosphere.platform.support.semanticId.eclass.model.TranslatableLabel;

/**
 * Initial caching semantic id resolver for Eclass Ids.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EclassSemanticIdResolver extends SemanticIdResolver {

    public static final String RELEASE_LATEST = "LATEST";
    private static final String REGEX = "^[0-9]{4}-[A-Z0-9:_.]{1,35}((-[A-Z0-9:_.]{1,35}"
        + "(-[A-Z0-9]{1}(-[A-Z0-9:_.]{1,70})?)?)?|-([A-Z0-9:_.]{1,35})?--[A-Z0-9:_.]{1,70}|---[A-Z0-9:_.]{1,70})"
        + "#[0-9A-Z]{2}-[A-Z0-9:_.]{1,131}#[0-9]{1,10}$";
    private Pattern pattern;
    private EclassJsonReadServicesApi eclassApi;
    private boolean disabled = false;
    private Map<String, DefaultSemanticIdResolutionResult> cache = Collections.synchronizedMap(new HashMap<>());
    private String keystoreKey = System.getProperty("iip.eclass.keystoreKey", "eclassCert");
    private Locale preferredLanguage = new Locale(
        System.getProperty("iip.eclass.locale", Locale.getDefault().toString()));
    
    /**
     * Creates an instance.
     */
    public EclassSemanticIdResolver() {
        try {
            pattern = Pattern.compile(REGEX);
        } catch (PatternSyntaxException e) {
            LoggerFactory.getLogger(EclassSemanticIdResolver.class).error(
                "Syntax error in IRDI RegEx: {} Disabling this resolver.", e.getMessage());
            disabled = true;
        }
    }

    /**
     * Tries to initialize the API lazily.
     */
    private void initialize() {
        if (null == eclassApi) {
            try {
                IdentityStore iStore = IdentityStore.getInstance();
                IdentityToken tok = iStore.getToken(keystoreKey);
                if (null == tok) {
                    LoggerFactory.getLogger(EclassSemanticIdResolver.class).error(
                        "No authentication token for '{}'. Disabling this resolver.", keystoreKey);
                    disabled = true;
                } else {
                    KeyManager[] keyManagers = null;
                    InputStream keyStoreStream = iStore.getKeystoreAsStream(keystoreKey);
                    if (null != keyStoreStream) {
                        try {                    
                            KeyStore keyStores = KeyStore.getInstance("PKCS12"); // EClass cert default
                            char[] pw = tok.getTokenDataAsCharArray();
                            if (null == pw) {
                                pw = "".toCharArray(); // EClass default, if not set up otherwise
                            }
                            keyStores.load(keyStoreStream, pw);
                            // Eclass cert default -> SHA256
                            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
                            keyManagerFactory.init(keyStores, pw);
                            keyManagers = keyManagerFactory.getKeyManagers();
/*                            SSLContext context = SSLContext.getInstance("TLS");
                            context.init(keyManagerFactory.getKeyManagers(), null, null);
                            AuthApiClient.setSslContext(context);*/
                        } catch (GeneralSecurityException e) {
                            LoggerFactory.getLogger(EclassSemanticIdResolver.class).error(
                                "Cannot load certificate. Disabling this resolver.", e.getMessage());
                            disabled = true;
                        }
                    } else {
                        LoggerFactory.getLogger(EclassSemanticIdResolver.class).error(
                            "No keystore stream found. Disabling this resolver.");
                        disabled = true;
                    }
                    EclassJsonReadServicesApi api = new EclassJsonReadServicesApi();
                    AuthApiClient apiClient = new AuthApiClient();
                    apiClient.setKeyManagers(keyManagers);
                    api.setApiClient(apiClient);
                    apiClient.setBasePath("https://eclass-cdp.com/");
                    eclassApi = api;
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(EclassSemanticIdResolver.class).error(
                    "Cannot create Eclass API/connection. Disabling this resolver.", e.getMessage());
                disabled = true;
            }
        }
    }
    
    /**
     * Creates a result instance.
     * 
     * @param semanticId the semantic id to create the result for
     * @return the instance
     */
    public static DefaultSemanticIdResolutionResult createInstance(String semanticId) {
        DefaultSemanticIdResolutionResult result = new DefaultSemanticIdResolutionResult();
        result.setSemanticId(semanticId);
        result.setPublisher("Eclass");
        result.setKind("IRDI");
        int pos1 = semanticId.indexOf('-');
        int pos2 = semanticId.indexOf('#', pos1);
        if (pos1 > 0 && pos2 > pos1) {
            result.setRevision(semanticId.substring(pos1 + 1, pos2));
        }
        pos2 = semanticId.lastIndexOf('#');
        if (pos2 > 0) {
            String ver = semanticId.substring(pos2 + 1);
            while (ver.startsWith("0") && ver.length() > 0) {
                ver = ver.substring(1);
            }
            result.setVersion(ver);
        }
        return result;
    }
    
    /**
     * Creates a naming structure based on two labels. [public for testing]
     * 
     * @param preferredName the preferred name, may be <b>null</b>
     * @param structuredName the structured name, may be <b>null</b>
     * @param description the description, may be <b>null</b>
     * @return the naming map
     */
    public static Map<String, DefaultNaming> createNaming(TranslatableLabel preferredName, 
        TranslatableLabel structuredName, TranslatableLabel description) {
        Map<String, DefaultNaming> result = new HashMap<String, DefaultNaming>();
        if (null != preferredName) {
            createNaming(preferredName, (n, v) -> n.setName(v), result);
        }
        if (null != structuredName) {
            createNaming(structuredName, (n, v) -> n.setStructuredName(v), result);
        }
        if (null != description) {
            createNaming(description, (n, v) -> n.setDescription(v), result);
        }        
        return result;
    }
    
    /**
     * Setter interface to generically set {@code value} on {@code naming}.
     * 
     * @author Holger Eichelberger, SSE
     */
    private interface Setter {
        
        /**
         * Sets {@code value} on {@code naming}.
         * 
         * @param naming the naming to be changed
         * @param value the value to set
         * 
         * @author Holger Eichelberger, SSE
         */
        public void apply(DefaultNaming naming, String value);
    }
    
    /**
     * Takes over information from the label.
     * 
     * @param label the label
     * @param setter the setter function
     * @param result the (modified) result set
     */
    private static void createNaming(TranslatableLabel label, Setter setter, Map<String, DefaultNaming> result) {
        for (Map.Entry<String, String> e : label.entrySet()) {
            Locale loc = new Locale(e.getKey());
            DefaultNaming naming = result.get(loc.getLanguage());
            if (null == naming) {
                naming = new DefaultNaming();
                result.put(loc.getLanguage(), naming);
            }
            setter.apply(naming, e.getValue());
        }
    }
    
    @Override
    public SemanticIdResolutionResult resolveSemanticId(String semanticId) {
        DefaultSemanticIdResolutionResult result = cache.get(semanticId);
        if (null == result) {
            initialize();
            if (null != eclassApi) {
                String prefLang = preferredLanguage.toString();
                // may be there is a way to get the type/kind out??
                try {
                    ReadUnit res = eclassApi.jsonapiV1UnitsIrdiGet(semanticId, prefLang, "false", RELEASE_LATEST);
                    if (res != null) {
                        result = createInstance(semanticId);
                        // no access to revision, version, description; just guessing/parsing
                        result.setNamingTyped(createNaming(res.getShortName(), res.getPreferredName(), null));
                    }
                } catch (ApiException e) {
                    LoggerFactory.getLogger(EclassSemanticIdResolver.class).error("API error: {} code {} body {}", 
                        e.getMessage(), e.getCode(), e.getResponseBody());
                } catch (ProcessingException e) {
                    LoggerFactory.getLogger(EclassSemanticIdResolver.class).error(
                        "Processing error: {}", e.getMessage());
                }
                if (null == result) {
                    try {
                        ReadProperty res = eclassApi.jsonapiV1PropertiesIrdiGet(semanticId, 
                            prefLang, "false", RELEASE_LATEST);
                        if (res != null) {
                            result = createInstance(semanticId);
                            // no access to revision, version, description; just guessing/parsing
                            result.setNamingTyped(createNaming(res.getPreferredName(), res.getPreferredName(), null));
                        }
                    } catch (ApiException e) {
                        LoggerFactory.getLogger(EclassSemanticIdResolver.class).error("API error: {} code {} body {}", 
                            e.getMessage(), e.getCode(), e.getResponseBody());
                    } catch (IllegalArgumentException e) {
                        
                    } catch (ProcessingException e) {
                        LoggerFactory.getLogger(EclassSemanticIdResolver.class).error(
                                "Processing error: {}", e.getMessage());
                    }
                }
            }
            if (null != result) {
                cache.put(semanticId, result);
            }
        }
        return result;
    }

    @Override
    public boolean isResponsible(String semanticId) {
        // check whether it is an eclass IRDI?
        return disabled ? false : pattern.matcher(semanticId).matches();
    }

}
