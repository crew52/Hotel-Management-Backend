package codegym.c10.hotel.security;

/**
 * Defines all application permissions using a standardized naming convention.
 * Format: ACTION_OBJECT (e.g., CREATE_ROOM, VIEW_EMPLOYEE)
 */
public final class PermissionConstants {
    // Prevent instantiation
    private PermissionConstants() {}
    
    // Room permissions
    public static final String CREATE_ROOM = "CREATE_ROOM";
    public static final String VIEW_ROOM = "VIEW_ROOM";
    public static final String UPDATE_ROOM = "UPDATE_ROOM";
    public static final String DELETE_ROOM = "DELETE_ROOM";
    public static final String ROOM_MANAGEMENT = "ROOM_MANAGEMENT"; // Super permission for all room operations
    
    // Room Category permissions
    public static final String CREATE_ROOM_CATEGORY = "CREATE_ROOM_CATEGORY";
    public static final String VIEW_ROOM_CATEGORY = "VIEW_ROOM_CATEGORY";
    public static final String UPDATE_ROOM_CATEGORY = "UPDATE_ROOM_CATEGORY";
    public static final String DELETE_ROOM_CATEGORY = "DELETE_ROOM_CATEGORY";
    public static final String ROOM_CATEGORY_MANAGEMENT = "ROOM_CATEGORY_MANAGEMENT";
    
    // Employee permissions
    public static final String CREATE_EMPLOYEE = "CREATE_EMPLOYEE";
    public static final String VIEW_EMPLOYEE = "VIEW_EMPLOYEE";
    public static final String UPDATE_EMPLOYEE = "UPDATE_EMPLOYEE";
    public static final String DELETE_EMPLOYEE = "DELETE_EMPLOYEE";
    public static final String EMPLOYEE_MANAGEMENT = "EMPLOYEE_MANAGEMENT";
    
    // User permissions
    public static final String CREATE_USER = "CREATE_USER";
    public static final String VIEW_USER = "VIEW_USER";
    public static final String UPDATE_USER = "UPDATE_USER";
    public static final String DELETE_USER = "DELETE_USER";
    public static final String USER_MANAGEMENT = "USER_MANAGEMENT";
    
    // Role and Permission management
    public static final String VIEW_ROLE = "VIEW_ROLE";
    public static final String CREATE_ROLE = "CREATE_ROLE";
    public static final String UPDATE_ROLE = "UPDATE_ROLE";
    public static final String DELETE_ROLE = "DELETE_ROLE";
    public static final String ROLE_MANAGEMENT = "ROLE_MANAGEMENT";
    
    public static final String VIEW_PERMISSION = "VIEW_PERMISSION";
    public static final String CREATE_PERMISSION = "CREATE_PERMISSION";
    public static final String UPDATE_PERMISSION = "UPDATE_PERMISSION";
    public static final String DELETE_PERMISSION = "DELETE_PERMISSION";
    public static final String PERMISSION_MANAGEMENT = "PERMISSION_MANAGEMENT";
    
    // System admin permissions
    public static final String VIEW_ACTIVITY_LOG = "VIEW_ACTIVITY_LOG";
    public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN"; // Super admin permission
} 