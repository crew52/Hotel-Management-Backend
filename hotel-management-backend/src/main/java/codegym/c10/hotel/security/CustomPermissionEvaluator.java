package codegym.c10.hotel.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Custom permission evaluator to support both direct permission checks
 * and parent/grouped permission checks.
 * 
 * This allows for expressions like:
 * - hasPermission('VIEW_ROOM')
 * - hasPermission('ROOM_MANAGEMENT')
 */
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || targetDomainObject == null || !(permission instanceof String)) {
            return false;
        }
        
        return hasPermission(authentication, permission.toString());
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || targetType == null || !(permission instanceof String)) {
            return false;
        }
        
        return hasPermission(authentication, permission.toString());
    }
    
    /**
     * Checks if the authenticated user has the specified permission directly or through a parent permission.
     * 
     * @param authentication The authentication object
     * @param permission The permission to check
     * @return true if the user has the permission, false otherwise
     */
    private boolean hasPermission(Authentication authentication, String permission) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Set<String> userPermissions = new HashSet<>();
        
        // Extract all permissions from granted authorities
        for (GrantedAuthority authority : authorities) {
            userPermissions.add(authority.getAuthority());
        }
        
        // Check for direct permission match
        if (userPermissions.contains(permission)) {
            return true;
        }
        
        // Check if user has parent permission
        String parentPermission = getParentPermission(permission);
        return parentPermission != null && userPermissions.contains(parentPermission);
    }
    
    /**
     * Determines the parent permission for a given permission.
     * For example, the parent of CREATE_ROOM is ROOM_MANAGEMENT.
     * 
     * @param permission The permission to find the parent for
     * @return The parent permission, or null if none exists
     */
    private String getParentPermission(String permission) {
        // Room permissions
        if (permission.endsWith("_ROOM")) {
            return PermissionConstants.ROOM_MANAGEMENT;
        }
        
        // Room Category permissions
        if (permission.endsWith("_ROOM_CATEGORY")) {
            return PermissionConstants.ROOM_CATEGORY_MANAGEMENT;
        }
        
        // Employee permissions
        if (permission.endsWith("_EMPLOYEE")) {
            return PermissionConstants.EMPLOYEE_MANAGEMENT;
        }
        
        // User permissions
        if (permission.endsWith("_USER")) {
            return PermissionConstants.USER_MANAGEMENT;
        }
        
        // Role permissions
        if (permission.endsWith("_ROLE")) {
            return PermissionConstants.ROLE_MANAGEMENT;
        }
        
        // Permission permissions
        if (permission.endsWith("_PERMISSION")) {
            return PermissionConstants.PERMISSION_MANAGEMENT;
        }
        
        // If system admin permission exists, it's a parent of all
        if (permission.equals(PermissionConstants.SYSTEM_ADMIN)) {
            return null; // System admin doesn't have a parent
        }
        
        return null;
    }
} 