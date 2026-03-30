package org.ac.cst8277.Joyce.Samuel.user_service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class UserService {
	private final UserRepository  repo;
	private final PasswordEncoder passEncoder;
	private Map<String, Integer>  generatedTokens;
	
	public UserService(UserRepository repo, PasswordEncoder passEncoder) {
		this.repo = repo;
		this.passEncoder = passEncoder;
		this.generatedTokens = new HashMap<>();
	}
	
	public Mono<String> login(String username, String password){
		return Mono.fromCallable(() -> {
			// Retrieve the user by user name
			User user = repo.findByUserName(username)
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
	
	public Mono<Integer> getUserIdFromToken(String token) {
	    return Mono.fromCallable(() -> {
	        Integer userId = generatedTokens.get(token);
	        if (userId == null) {
	            throw new RuntimeException("Invalid token");
	        }
	        return userId;
	    });
	}
	
	public Mono<Integer> validateToken(String token) {
		if (generatedTokens.containsKey(token)) {
			return Mono.just(generatedTokens.get(token));
		}
	    return Mono.just(0);
	}
	
	public Flux<User> getAllUsers(String token) {
	    return valid(token)
	        .flatMapMany(isValid -> {
	            if (!isValid) {
	                return Flux.error(new RuntimeException("Invalid token"));
	            }

	            return Mono.fromCallable(repo::findAll)
	                       .subscribeOn(Schedulers.boundedElastic())
	                       .flatMapMany(Flux::fromIterable);
	        });
	}
	
	public Mono<ApiResponse> addUser(String userName, String email, String password) {
	    return Mono.fromCallable(() -> {
	        if (repo.findByEmail(email).isPresent()) {
	            return new ApiResponse("Email already exists");
	        }

	        if (repo.findByUserName(userName).isPresent()) {
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
	        repo.save(user);

	        return new ApiResponse("User successfully created");
	    }).subscribeOn(Schedulers.boundedElastic());
	}
	
	public Flux<Role> getAllRoles(String token) {
	    return valid(token)
	        .flatMapMany(isValid -> {
	            if (!isValid) {
	                return Flux.error(new RuntimeException("Invalid token"));
	            }

	            return Mono.fromCallable(repo::findAll)
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

	            return Mono.fromCallable(repo::findAllUserRoles)
	                    .subscribeOn(Schedulers.boundedElastic())
	                    .flatMapMany(Flux::fromIterable);
	        });
	}
	
	private String generateToken(int userId) {
		String token;
		do {
			token = UUID.randomUUID().toString();
		} while (generatedTokens.containsKey(token));
		generatedTokens.put(token, userId);
		return token;
	}
	
	private Mono<Boolean> valid(String token) {
	    return validateToken(token)
	            .map(userId -> userId != 0);
	}
}
