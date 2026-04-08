package org.ac.cst8277.Joyce.Samuel.user_service.tokens;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tokens")
public class Token {
	private static final int EXPIRE_TIME = 15;
	
	@Id
	@Column(name = "token_id")
	private String tokenID;
	@Column(name = "user_id")
	private int userId;
	@Column(name = "expiry")
	private LocalDateTime expiry;
	
	public Token(int user_id) {
		this.userId = user_id;
		tokenID = UUID.randomUUID().toString();
		expiry = LocalDateTime.now().plusMinutes(EXPIRE_TIME);
	}
	
	public Token() {
		// Required
	}

	
	public String getTokenId() {
		return tokenID;
	}

	public void setTokenId(String token_id) {
		this.tokenID = token_id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int user_id) {
		this.userId = user_id;
	}

	public LocalDateTime getExpiry() {
		return expiry;
	}

	public void setExpiry(LocalDateTime expiry) {
		this.expiry = expiry;
	}
}
