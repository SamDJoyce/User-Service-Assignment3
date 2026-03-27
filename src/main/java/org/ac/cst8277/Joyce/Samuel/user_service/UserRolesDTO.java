package org.ac.cst8277.Joyce.Samuel.user_service;

public class UserRolesDTO {
    private String userName;
    private String roleName;

    public UserRolesDTO(String userName, String roleName) {
        this.userName = userName;
        this.roleName = roleName;
    }

    public String getUserName() { 
    	return userName; 
    }
    public String getRoleName() { 
    	return roleName; 
    }
}
