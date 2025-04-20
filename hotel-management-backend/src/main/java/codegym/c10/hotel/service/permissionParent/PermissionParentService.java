package codegym.c10.hotel.service.permissionParent;

import codegym.c10.hotel.security.PermissionConstants;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the IPermissionParentService interface.
 * Defines parent-child relationships between permissions.
 */
@Service
public class PermissionParentService implements IPermissionParentService {

    @Override
    public List<String> getAllParentPermissions(String permission) {
        List<String> parentPermissions = new ArrayList<>();
        
        // Add parent permissions based on permission suffix/prefix patterns
        if (permission.endsWith("_ROOM")) {
            parentPermissions.add(PermissionConstants.ROOM_MANAGEMENT);
        }
        
        if (permission.endsWith("_ROOM_CATEGORY")) {
            parentPermissions.add(PermissionConstants.ROOM_CATEGORY_MANAGEMENT);
        }
        
        if (permission.endsWith("_EMPLOYEE")) {
            parentPermissions.add(PermissionConstants.EMPLOYEE_MANAGEMENT);
        }
        
        if (permission.endsWith("_USER")) {
            parentPermissions.add(PermissionConstants.USER_MANAGEMENT);
        }
        
        if (permission.endsWith("_ROLE")) {
            parentPermissions.add(PermissionConstants.ROLE_MANAGEMENT);
        }
        
        if (permission.endsWith("_PERMISSION")) {
            parentPermissions.add(PermissionConstants.PERMISSION_MANAGEMENT);
        }
        
        // Always consider SYSTEM_ADMIN as parent of all permissions
        parentPermissions.add(PermissionConstants.SYSTEM_ADMIN);
        
        return parentPermissions;
    }
} 