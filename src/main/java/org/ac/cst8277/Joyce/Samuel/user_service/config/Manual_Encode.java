package org.ac.cst8277.Joyce.Samuel.user_service.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Manual_Encode {
	
	// Used to manually seed an admin user to the database 
	public static void main(String[] args) {
		System.out.println(new BCryptPasswordEncoder().encode("password"));

	}

}
