package ch.modul295.yannisstebler.financeapp.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public class AuthenticationRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    
    private final String appName;

    // Constructor that initializes the appName (required for extracting roles specific to the app)
    public AuthenticationRoleConverter(String appName) {
        defaultGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        defaultGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        this.appName = appName;
    }

    /**
     * Extract roles for the application from the 'resource_access' claim in the JWT.
     * @param jwt The JWT token
     * @return A collection of roles as GrantedAuthority
     */
    private Collection<? extends GrantedAuthority> extractResourceRoles(final Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        Collection<String> resourceRoles;
        
        if (resourceAccess != null) {
            // Retrieve roles for the specific appName (application)
            Object appObject = resourceAccess.get(appName);
            Map<String, Collection<String>> app = appObject instanceof Map<?, ?> ? 
                (Map<String, Collection<String>>) appObject : Collections.emptyMap();
            
            // Extract the roles for the application, if present
            if ((resourceRoles = app.get("roles")) != null) {
                return resourceRoles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());
            }
        }
        return Collections.emptySet();
    }

    /**
     * Convert the JWT into an AbstractAuthenticationToken by adding roles.
     * @param source The JWT token
     * @return A JwtAuthenticationToken that contains the authorities
     */
    @Override
    public AbstractAuthenticationToken convert(final Jwt source) {
        Collection<GrantedAuthority> authorities = Stream.concat(
                defaultGrantedAuthoritiesConverter.convert(source).stream(), 
                extractResourceRoles(source).stream() 
        ).collect(Collectors.toSet()); 
        
        // Return a JwtAuthenticationToken with the combined authorities
        return new JwtAuthenticationToken(source, authorities);
    }
}
