package org.ac.cst8277.Joyce.Samuel.user_service.tokens;

import java.time.LocalDateTime;
import java.util.Set;

import org.ac.cst8277.Joyce.Samuel.user_service.Role;

public class ValidTokenResponse {
	private final int userId;
	private final LocalDateTime expiry;
	private final Set<Role> roles;
	
	public ValidTokenResponse(	int userId,
								LocalDateTime expiry,
								Set<Role> roles) {
		this.userId = userId;
		this.expiry = expiry;
		this.roles = roles;
	}

	public int getUserId() {
		return userId;
	}

	public LocalDateTime getExpiry() {
		return expiry;
	}

	public Set<Role> getRoles() {
		return roles;
	}
}
