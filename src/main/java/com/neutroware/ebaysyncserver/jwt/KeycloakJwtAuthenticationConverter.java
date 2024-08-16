package com.neutroware.ebaysyncserver.jwt;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {


    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt source) {

        return new JwtAuthenticationToken(
                source,
                Stream.concat(
                                new JwtGrantedAuthoritiesConverter().convert(source).stream(),
                                extractUserRoles(source).stream())
                        .collect(toSet()));
    }

    private List<GrantedAuthority> extractUserRoles(Jwt source) {
        var result = new ArrayList<GrantedAuthority>();

        var realmAccess = source.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.get("roles") != null) {
            var roles = realmAccess.get("roles");
            if (roles instanceof List l) {
                l.forEach(role -> {
                    result.add(new SimpleGrantedAuthority("ROLE_" + role));
                    //With @EnableMethodSecurity, we can now add  @PreAuthorize("hasAuthority('ROLE_role')")
                    // to any controller method and this will only allow users
                    // with the specified role to access endpoint. If they don't have
                    // specified role, then it will return 403 forbidden
                });
            }
        }
        return result;
    }
}
