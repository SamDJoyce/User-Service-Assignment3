package org.ac.cst8277.Joyce.Samuel.user_service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * @author sjoyce
 *
 */
public interface UserRepository extends JpaRepository<User, Integer>{
	Optional<User> findByUserName(String userName);
	Optional<User> findByEmail(String email);
	@Query("""
		    SELECT new org.ac.cst8277.Joyce.Samuel.user_service.UserRolesDTO(u.userName, r.roleName)
		    FROM User u
		    JOIN u.roles r
		""")
	List<UserRolesDTO> findAllUserRoles();
}
