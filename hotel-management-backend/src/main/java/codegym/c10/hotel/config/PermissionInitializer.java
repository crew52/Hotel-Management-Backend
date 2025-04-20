package codegym.c10.hotel.config;

import codegym.c10.hotel.entity.Permission;
import codegym.c10.hotel.entity.Role;
import codegym.c10.hotel.repository.PermissionRepository;
import codegym.c10.hotel.repository.RoleRepository;
import codegym.c10.hotel.security.PermissionConstants;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Automatically initializes all permissions defined in PermissionConstants
 * and creates default roles with appropriate permissions.
 */
@Component
public class PermissionInitializer {

    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private RoleRepository roleRepository;

    /**
     * Creates all permissions defined in PermissionConstants if they don't exist,
     * and sets up default roles with appropriate permissions.
     */
    @PostConstruct
    @Transactional
    public void initializePermissions() {
        // Create all permissions defined in PermissionConstants
        Map<String, Permission> permissionMap = createAllPermissions();
        
        // Create default roles if they don't exist
        createDefaultRoles(permissionMap);
    }
    
    /**
     * Creates all permissions defined in PermissionConstants.
     * 
     * @return A map of permission name to Permission object
     */
    private Map<String, Permission> createAllPermissions() {
        Map<String, Permission> permissionMap = new HashMap<>();
        
        // Use reflection to get all constants from PermissionConstants
        Field[] fields = PermissionConstants.class.getDeclaredFields();
        
        for (Field field : fields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && 
                java.lang.reflect.Modifier.isFinal(field.getModifiers()) &&
                field.getType() == String.class) {
                
                try {
                    String permissionName = (String) field.get(null);
                    
                    // Check if permission already exists
                    Optional<Permission> existingPermission = permissionRepository.findByName(permissionName);
                    
                    if (existingPermission.isPresent()) {
                        permissionMap.put(permissionName, existingPermission.get());
                    } else {
                        // Create new permission
                        Permission permission = new Permission();
                        permission.setName(permissionName);
                        Permission savedPermission = permissionRepository.save(permission);
                        permissionMap.put(permissionName, savedPermission);
                    }
                } catch (IllegalAccessException e) {
                    // Log the error
                    System.err.println("Error accessing permission constant: " + e.getMessage());
                }
            }
        }
        
        return permissionMap;
    }
    
    /**
     * Creates default roles with appropriate permissions.
     * 
     * @param permissionMap Map of permission name to Permission object
     */
    private void createDefaultRoles(Map<String, Permission> permissionMap) {
        // Define default roles and their permissions
        
        // 1. ADMIN role - has all permissions
        createRoleIfNotExists("ROLE_ADMIN", new HashSet<>(permissionMap.values()));
        
        // 2. MANAGER role - has management permissions but not system admin
        Set<Permission> managerPermissions = new HashSet<>();
        addPermissionsWithPrefix(managerPermissions, permissionMap, "MANAGEMENT");
        // Add all CRUD permissions
        addPermissionsWithPrefix(managerPermissions, permissionMap, "CREATE_");
        addPermissionsWithPrefix(managerPermissions, permissionMap, "VIEW_");
        addPermissionsWithPrefix(managerPermissions, permissionMap, "UPDATE_");
        addPermissionsWithPrefix(managerPermissions, permissionMap, "DELETE_");
        // Remove system admin permission
        managerPermissions.remove(permissionMap.get(PermissionConstants.SYSTEM_ADMIN));
        createRoleIfNotExists("ROLE_MANAGER", managerPermissions);
        
        // 3. RECEPTIONIST role - can manage rooms but not users/permissions
        Set<Permission> receptionistPermissions = new HashSet<>();
        // Add room management
        receptionistPermissions.add(permissionMap.get(PermissionConstants.ROOM_MANAGEMENT));
        receptionistPermissions.add(permissionMap.get(PermissionConstants.VIEW_ROOM));
        receptionistPermissions.add(permissionMap.get(PermissionConstants.CREATE_ROOM));
        receptionistPermissions.add(permissionMap.get(PermissionConstants.UPDATE_ROOM));
        // Add room category viewing
        receptionistPermissions.add(permissionMap.get(PermissionConstants.VIEW_ROOM_CATEGORY));
        // Add employee viewing
        receptionistPermissions.add(permissionMap.get(PermissionConstants.VIEW_EMPLOYEE));
        createRoleIfNotExists("ROLE_RECEPTIONIST", receptionistPermissions);
        
        // 4. VIEWER role - can only view data
        Set<Permission> viewerPermissions = new HashSet<>();
        addPermissionsWithPrefix(viewerPermissions, permissionMap, "VIEW_");
        createRoleIfNotExists("ROLE_VIEWER", viewerPermissions);
    }
    
    /**
     * Helper method to add permissions that have a specific prefix to a set.
     */
    private void addPermissionsWithPrefix(Set<Permission> targetSet, Map<String, Permission> permissionMap, String prefix) {
        for (Map.Entry<String, Permission> entry : permissionMap.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                targetSet.add(entry.getValue());
            }
        }
    }
    
    /**
     * Creates a role if it doesn't exist.
     * 
     * @param roleName The name of the role
     * @param permissions The permissions to assign to the role
     */
    private void createRoleIfNotExists(String roleName, Set<Permission> permissions) {
        Role existingRole = roleRepository.findByName(roleName);
        
        if (existingRole == null) {
            Role role = new Role();
            role.setName(roleName);
            role.setPermissions(permissions);
            roleRepository.save(role);
        }
    }
} 