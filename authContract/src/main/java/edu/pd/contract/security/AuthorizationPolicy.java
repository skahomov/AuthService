package edu.pd.contract.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("authorizationPolicy")
public class AuthorizationPolicy {
    private final boolean authEnabled;

    public AuthorizationPolicy(@Value("${hcc.auth.enabled:false}") boolean authEnabled) {
        this.authEnabled = authEnabled;
    }

    public boolean hasPermission(String permission) {
        if (!authEnabled) {
            return true;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String permissionAuthority = "PERMISSION_" + permission;
        String scopeAuthority = "SCOPE_" + permission;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String value = authority.getAuthority();
            if ("ROLE_ADMIN".equals(value)
                    || permissionAuthority.equals(value)
                    || scopeAuthority.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
