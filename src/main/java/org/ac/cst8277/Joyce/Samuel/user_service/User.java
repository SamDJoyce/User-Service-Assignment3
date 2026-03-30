package org.ac.cst8277.Joyce.Samuel.user_service;


import java.util.Set;

import jakarta.persistence.*;

/**
 * 
 * 
 * @author sjoyce
 *
 */
@Entity
@Table(name = "users")
public class User {
	// Fields
	@Id
	@GeneratedValue( strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private int userId;
	@Column(name = "user_name")
	private String userName;
	@Column(name = "email")
	private String email;
	@Column(name = "pass_hash")
	private String passHash;
	
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
    	    name = "user_roles",
    	    joinColumns = @JoinColumn(name = "user_id"),
    	    inverseJoinColumns = @JoinColumn(name = "role_id")
    	)
	private Set<Role>	  roles;
	
	// Getters and Setters
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	
	public Boolean addRole(Role role) {
		// Check if the user already has the role
		if (hasRole(role)) {
			return false;
		}
		roles.add(role);
		return true;
	}
	
	public Boolean removeRole(Role role) {
		// Check if the user already has the role
		if (!hasRole(role)) {
			return false;
		}
		roles.remove(role);
		return true;
	}
	
	public Boolean hasRole(Role role) {
		return roles.contains(role);
	}
	
	public void setPassHash(String passHash) {
		this.passHash = passHash;
	}
	
	public String getPassHash() {
		return this.passHash;
	}

	public User() {
	}
	
	/**
	 * @author sjoyce
	 *
	 */
	public static class Builder {
		private int 		  userId;
		private String 		  userName;
		private String 		  email;
		private String		  passHash;
		private Set<Role>	  roles;
		
		public Builder setUserId(int userId) {
			this.userId = userId;
			return this;
		}
		public Builder setUserName(String userName) {
			this.userName = userName;
			return this;
		}
		public Builder setEmail(String email) {
			this.email = email;
			return this;
		}
		public Builder setPassHash(String passHash) {
			this.passHash = passHash;
			return this;
		}
		public Builder setRoles(Set<Role> roles) {
			this.roles = roles;
			return this;
		}
		public User build() {
			User u = new User();
			
			u.userId 		= userId;
			u.userName 		= userName;
			u.email 		= email;
			u.passHash		= passHash;
			u.roles 		= roles;
			
			return u;
		}
	}
}
