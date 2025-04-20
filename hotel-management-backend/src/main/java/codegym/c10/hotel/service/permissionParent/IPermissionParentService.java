package codegym.c10.hotel.service.permissionParent;

import java.util.List;

/**
 * Service interface for managing permission parent-child relationships.
 * Used to determine parent permissions for a given permission.
 */
public interface IPermissionParentService {
    
    /**
     * Returns all parent permissions for a given permission.
     * For example, the parent of CREATE_ROOM is ROOM_MANAGEMENT.
     * 
     * @param permission The permission to find parents for
     * @return A list of parent permission names
     */
    List<String> getAllParentPermissions(String permission);
} 