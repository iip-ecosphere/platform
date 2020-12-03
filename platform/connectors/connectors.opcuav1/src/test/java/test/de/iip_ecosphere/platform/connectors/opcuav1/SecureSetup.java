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

package test.de.iip_ecosphere.platform.connectors.opcuav1;

import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_USERNAME;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_X509;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfigBuilder;
import org.eclipse.milo.opcua.sdk.server.identity.CompositeValidator;
import org.eclipse.milo.opcua.sdk.server.identity.IdentityValidator;
import org.eclipse.milo.opcua.sdk.server.identity.UsernameIdentityValidator;
import org.eclipse.milo.opcua.sdk.server.identity.X509IdentityValidator;
import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaRuntimeException;
import org.eclipse.milo.opcua.stack.core.security.DefaultCertificateManager;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.util.CertificateUtil;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedHttpsCertificateBuilder;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration.Builder;
import org.eclipse.milo.opcua.stack.server.security.DefaultServerCertificateValidator;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;

/**
 * Describes a secure setup.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SecureSetup extends ServerSetup {

    private File securityTempDir;
    
    private X509Certificate certificate;
    private DefaultCertificateManager certificateManager;
    private DefaultTrustListManager trustListManager;
    private DefaultServerCertificateValidator certificateValidator;
    private KeyPair httpsKeyPair;
    private X509Certificate httpsCertificate;
    private X509IdentityValidator x509IdentityValidator;
    private String applicationUri;
    private IdentityValidator<String> identityValidator = new UsernameIdentityValidator(
        true,
        authChallenge -> {
            String username = authChallenge.getUsername();
            String password = authChallenge.getPassword();

            boolean userOk = "user".equals(username) && "password1".equals(password);
            boolean adminOk = "admin".equals(username) && "password2".equals(password);

            return userOk || adminOk;
        }
    );
    
    private X509Certificate clientCertificate;
    private KeyPair clientKeyPair;
    
    /**
     * Creates a server setup instance.
     * 
     * @param path the URL path on the endpoints (no trailing slash)
     * @param tcpPort the TCP port to serve
     * @param httpsPort the HTTPS port to serve (although not secured)
     */
    public SecureSetup(String path, int tcpPort, int httpsPort) {
        super(path, tcpPort, httpsPort);
    }

    // checkstyle: stop exception type check
    
    @Override
    public String initializeApplication() throws ExecutionException {
        try {
            File securityTempDir = new File(System.getProperty("java.io.tmpdir"), "security");
            FileUtils.deleteDirectory(securityTempDir);
            if (!securityTempDir.exists() && !securityTempDir.mkdirs()) {
                throw new IOException("unable to create security temp dir: " + securityTempDir);
            }
            LoggerFactory.getLogger(getClass()).info("security temp dir: {}", securityTempDir.getAbsolutePath());
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
        
        setupServer();
        setupClient();
        
        return applicationUri;
    }

    /**
     * Sets up the temporary (self-signed) server certificates. 
     * 
     * @throws ExecutionException if generating/obtaining the certificates fails
     */
    private void setupServer() throws ExecutionException {
        try {
            File securityTempDir = new File(System.getProperty("java.io.tmpdir"), "security");
            FileUtils.deleteDirectory(securityTempDir);
            if (!securityTempDir.exists() && !securityTempDir.mkdirs()) {
                throw new Exception("unable to create security temp dir: " + securityTempDir);
            }
            LoggerFactory.getLogger(getClass()).info("security temp dir: {}", securityTempDir.getAbsolutePath());
    
            ServerKeystoreLoader loader = new ServerKeystoreLoader().load(securityTempDir);
    
            certificateManager = new DefaultCertificateManager(
                loader.getServerKeyPair(),
                loader.getServerCertificateChain()
            );
    
            File pkiDir = securityTempDir.toPath().resolve("pki").toFile();
            trustListManager = new DefaultTrustListManager(pkiDir);
            LoggerFactory.getLogger(getClass()).info("pki dir: {}", pkiDir.getAbsolutePath());
    
            certificateValidator =
                new DefaultServerCertificateValidator(trustListManager);
    
            httpsKeyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
    
            SelfSignedHttpsCertificateBuilder httpsCertificateBuilder 
                = new SelfSignedHttpsCertificateBuilder(httpsKeyPair);
            httpsCertificateBuilder.setCommonName(HostnameUtil.getHostname());
            HostnameUtil.getHostnames("0.0.0.0").forEach(httpsCertificateBuilder::addDnsName);
            httpsCertificate = httpsCertificateBuilder.build();
    
            x509IdentityValidator = new X509IdentityValidator(c -> true);
    
            // If you need to use multiple certificates you'll have to be smarter than this.
            certificate = certificateManager.getCertificates()
                .stream()
                .findFirst()
                .orElseThrow(() -> new UaRuntimeException(StatusCodes.Bad_ConfigurationError, "no certificate found"));
    
            // The configured application URI must match the one in the certificate(s)
            applicationUri = CertificateUtil
                .getSanUri(certificate)
                .orElseThrow(() -> new UaRuntimeException(
                    StatusCodes.Bad_ConfigurationError,
                    "certificate is missing the application URI"));
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }
    
    /**
     * Sets up the temporary (self-signed) client certificates. To be called after {@link #setupServer()}.
     * 
     * @throws ExecutionException if generating/obtaining the certificates fails
     */
    private void setupClient() throws ExecutionException {
        ClientKeystoreLoader loader = new ClientKeystoreLoader().load(securityTempDir);
        clientCertificate = loader.getClientCertificate();
        clientKeyPair = loader.getClientKeyPair();
    }
    
    @Override
    public void shutdownApplication() throws ExecutionException {
        try {
            FileUtils.deleteDirectory(securityTempDir);
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }

    // checkstyle: resume exception type check

    @Override
    public void configureCommonEndpointBuilder(Builder builder) {
        builder
            .setCertificate(certificate)
            .addTokenPolicies(
                USER_TOKEN_POLICY_ANONYMOUS, // really?
                USER_TOKEN_POLICY_USERNAME,
                USER_TOKEN_POLICY_X509);
    }

    @Override
    public Builder configureNoSecurityBuilder(Builder builder) {
        return builder
            .setSecurityPolicy(SecurityPolicy.None)
            .setSecurityMode(MessageSecurityMode.None);
    }
    
    @Override
    public Builder configureTcpEndpointBuilder(Builder builder) {
        // TCP Basic256Sha256 / SignAndEncrypt
        return builder
            .setSecurityPolicy(SecurityPolicy.Basic256Sha256)
            .setSecurityMode(MessageSecurityMode.SignAndEncrypt);
    }

    @Override
    public Builder configureHttpsEndpointBuilder(Builder builder) {
        return builder
            .setSecurityPolicy(SecurityPolicy.Basic256Sha256)
            .setSecurityMode(MessageSecurityMode.Sign);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void configureServerBuilder(OpcUaServerConfigBuilder builder) {
        builder.setCertificateManager(certificateManager)
            .setTrustListManager(trustListManager)
            .setCertificateValidator(certificateValidator)
            .setHttpsKeyPair(httpsKeyPair)
            .setHttpsCertificate(httpsCertificate)
            .setIdentityValidator(new CompositeValidator(identityValidator, x509IdentityValidator));
    }

    @Override
    public ConnectorParameter getConnectorParameter() {
        //Map<String, IdentityToken> identityToken = new HashMap<String, IdentityToken>();
        return ConnectorParameterBuilder.newBuilder("localhost", getHttpsPort())
            .setEndpointPath(getPath())
            .setApplicationInformation("urn:eclipse:milo:examples:client", "eclipse milo opc-ua client")
            //.setIdentities(identityToken) // unclear, the example also has none. May require creating IdentityTokens
            .setSecurityInformation(clientCertificate, clientKeyPair)
            .setNotificationInterval(1000) // test waits for that
            .build();
    }
    
}
