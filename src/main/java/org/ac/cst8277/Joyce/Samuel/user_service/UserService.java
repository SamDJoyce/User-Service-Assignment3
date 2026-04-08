package org.ac.cst8277.Joyce.Samuel.user_service;

import java.time.LocalDateTime;

import org.ac.cst8277.Joyce.Samuel.user_service.tokens.Token;
import org.ac.cst8277.Joyce.Samuel.user_service.tokens.TokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class UserService {
	private final UserRepository  userRepo;
	private final TokenRepository tokenRepo;
	private final PasswordEncoder passEncoder;
	
	public UserService(	UserRepository userRepo, 
						TokenRepository tokenRepo, 
						PasswordEncoder passEncoder) {
		this.userRepo = userRepo;
		this.tokenRepo = tokenRepo;
		this.passEncoder = passEncoder;
	}
	
	public Mono<String> login(String username, String password){
		return Mono.fromCallable(() -> {
			// Retrieve the user by user name
			User user = userRepo.findByUserName(username)
					.orElseThrow(() -> new RuntimeException("User not found"));
			// Compare password to stored hash
			Boolean matches = passEncoder.matches(password, user.getPassHash());
			if (!matches) {
				throw new RuntimeException("Invalid Password");
			}
			// If it matches dispense a token to the user
			return generateToken(user.getUserId());
		});
	}
	
	public Mono<String> oAuthLogin(String username, String email) {
	    return Mono.fromCallable(() -> {
	        User user = userRepo.findByEmail(email)
	                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credential mismatch"));

	        return generateToken(user.getUserId());
	    }).subscribeOn(Schedulers.boundedElastic());
	}
	
	public Mono<Integer> getUserIdFromToken(String token) {
	    return validateToken(token);
	}
	
	public Mono<Integer> validateToken(String token) {
	    return Mono.fromCallable(() -> {
	        Token t = tokenRepo.findByTokenId(token)
	        		 .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

	        if (t.getExpiry().isBefore(LocalDateTime.now())) {
	            throw new RuntimeException("Token expired");
	        }

	        return t.getUserId();
	    }).subscribeOn(Schedulers.boundedElastic());
	}
	
	public Flux<User> getAllUsers(String token) {
	    return valid(token)
	        .flatMapMany(isValid -> {
	            if (!isValid) {
	                return Flux.error(new RuntimeException("Invalid token"));
	            }

	            return Mono.fromCallable(userRepo::findAll)
	                       .subscribeOn(Schedulers.boundedElastic())
	                       .flatMapMany(Flux::fromIterable);
	        });
	}
	
	public Mono<ApiResponse> addUser(String userName, String email, String password) {
	    return Mono.fromCallable(() -> {
	        if (userRepo.findByEmail(email).isPresent()) {
	            return new ApiResponse("Email already exists");
	        }

	        if (userRepo.findByUserName(userName).isPresent()) {
	            return new ApiResponse("Username already exists");
	        }
	        // Hash password before saving
	        String hashed = passEncoder.encode(password);

	        User user = new User.Builder()
	                .setUserName(userName)
	                .setEmail(email)
	                .setPassHash(hashed)
	                .build();
	        // Save user
	        userRepo.save(user);

	        return new ApiResponse("User successfully created");
	    }).subscribeOn(Schedulers.boundedElastic());
	}
	
	public Flux<Role> getAllRoles(String token) {
	    return valid(token)
	        .flatMapMany(isValid -> {
	            if (!isValid) {
	                return Flux.error(new RuntimeException("Invalid token"));
	            }

	            return Mono.fromCallable(userRepo::findAll)
	                    .subscribeOn(Schedulers.boundedElastic())
	                    .flatMapMany(Flux::fromIterable)
	                    .flatMap(user -> Flux.fromIterable(user.getRoles()))
	                    .distinct();
	        });
	}
	
	// Get all user roles
	public Flux<UserRolesDTO> getAllUserRoles(String token) {
	    return valid(token)
	        .flatMapMany(isValid -> {
	            if (!isValid) {
	                return Flux.error(new RuntimeException("Invalid token"));
	            }

	            return Mono.fromCallable(userRepo::findAllUserRoles)
	                    .subscribeOn(Schedulers.boundedElastic())
	                    .flatMapMany(Flux::fromIterable);
	        });
	}
	
	public String generateToken(int userId) {
		Token token  = new Token(userId);
		tokenRepo.save(token);
		return token.getTokenId();
	}
	
	private Mono<Boolean> valid(String token) {
	    return validateToken(token)
	            .map(userId -> userId != 0);
	}
}
