package de.iip_ecosphere.platform.support.aas.basyx.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.basyx.extensions.shared.authorization.internal.IRoleAuthenticator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.IdentityTokenWithRole;
import de.iip_ecosphere.platform.support.identities.IdentityToken;

/**
 * Authentication manager based on {@link AuthenticationDescriptor#getServerUsers()}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AuthenticationDescriptorBasedAuthenticationManager implements AuthenticationManager {

    public static final IRoleAuthenticator<Authentication> AUTHENTICATOR = new IRoleAuthenticator<Authentication>() {
        
        @Override
        public List<String> getRoles(Authentication subjectInformation) {
            List<String> result = null;
            Object details = subjectInformation.getDetails(); // see below
            if (null != details) {
                result = CollectionUtils.toList(details.toString());
            }
            return result;
        }
    };

    private Map<String, IdentityTokenWithRole> users = new HashMap<>();

    /**
     * Creates an authentication manager for the given authentication descriptor.
     * 
     * @param desc the descriptor
     */
    public AuthenticationDescriptorBasedAuthenticationManager(AuthenticationDescriptor desc) {
        for (IdentityTokenWithRole u : desc.getServerUsers()) {
            users.put(u.getUserName(), u);
        }
    }
        
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        IdentityTokenWithRole user = users.get(username);
        if (null != user) {
            String presentedPassword = (String) authentication.getCredentials();
            String pwPrefix = "";
            String tea = user.getTokenEncryptionAlgorithm();
            if (IdentityToken.ENC_PLAIN_UTF_8.equalsIgnoreCase(tea)) {
                pwPrefix = "{noop}";
            } else if (IdentityToken.ENC_BCRYPT.equalsIgnoreCase(tea)) {
                pwPrefix = "{bcrypt}";
            } else if (IdentityToken.ENC_SHA256.equalsIgnoreCase(tea)) {
                pwPrefix = "{sha256}";
            }
            if (PasswordEncoderFactories.createDelegatingPasswordEncoder().matches(presentedPassword, 
                pwPrefix + user.getTokenDataAsString())) {
                UsernamePasswordAuthenticationToken tok = UsernamePasswordAuthenticationToken.authenticated(
                    user, user.getTokenDataAsString(), null);
                tok.setDetails(user.getRole()); // see above
                return tok;
            } else {
                throw new BadCredentialsException("Presented password/token for user " + username 
                    + " does not match.");
            }
        } else {
            throw new UsernameNotFoundException("User " + username + " unknown.");
        }
    }
}