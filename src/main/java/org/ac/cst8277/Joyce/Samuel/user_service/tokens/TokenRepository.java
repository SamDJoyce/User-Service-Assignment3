package org.ac.cst8277.Joyce.Samuel.user_service.tokens;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, String>{
	Optional<Token> findByTokenId(String tokenId);
}
