package org.ac.cst8277.Joyce.Samuel.user_service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.ac.cst8277.Joyce.Samuel.user_service.tokens.Token;
import org.ac.cst8277.Joyce.Samuel.user_service.tokens.TokenRepository;
import org.ac.cst8277.Joyce.Samuel.user_service.tokens.ValidTokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class UserService {
	private static final String NOT_FOUND = "Account not found";
	private static final String INVALID_TOKEN = "Invalid token";
	private final UserRepository  userRepo;
	private final TokenRepository tokenRepo;
	private final PasswordEncoder passEncoder;
	
	public UserService(	UserRepository userRepo, 
						TokenRepository tokenRepo, 
						PasswordEncoder passEncoder) {
		this.userRepo    = userRepo;
		this.tokenRepo   = tokenRepo;
		this.passEncoder = passEncoder;
	}
	
//	public Mono<String> login(String username, String password){
//		return Mono.fromCallable(() -> {
//			// Retrieve the user by user name
//			User user = userRepo.findByUserNameIgnoreCase(username)
//					.orElseThrow(() -> new RuntimeException("User not found"));
//			// Compare password to stored hash
//			Boolean matches = passEncoder.matches(password, user.getPassHash());
//			if (!matches) {
//				throw new RuntimeException("Invalid Password");
//			}
//			// If it matches dispense a token to the user
//			return generateToken(user.getUserId());
//		});
//	}
	
//	public Mono<String> oAuthLogin(String username) {
//	    return Mono.fromCallable(() -> {
//	        User user = userRepo.findByUserNameIgnoreCase(username)
//	                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credential mismatch"));
//	        Optional<Token> existingToken = tokenRepo.findByUserId(user.getUserId());
//	        Token token;
//	        // Check if the user already has a token in the system
//	        if (existingToken.isPresent()) {
//	        	// Remove the old token
//	        	token = existingToken.get();
//	            tokenRepo.delete(token);
//	        }
//        	// Create a new token
//        	token = generateToken(user.getUserId());
//	        // Dispense token id
//	        return token.getTokenId();
//	    }).subscribeOn(Schedulers.boundedElastic());
//	}
	public String handleOAuthLogin(String githubUsername) {

	    // Find user
	    User user = userRepo.findByUserNameIgnoreCase(githubUsername)
	        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, NOT_FOUND));

	    // Check existing token
	    Optional<Token> existingToken = tokenRepo.findByUserId(user.getUserId());

	    Token token;

	    if (existingToken.isPresent()) {
	        token = existingToken.get();

	        // Update expiry if token already present
	        token.setExpiry(LocalDateTime.now().plusMinutes(15));

	    } else {
	        token = new Token(user.getUserId());
	    }

	    tokenRepo.save(token);

	    return token.getTokenId();
	}
	
	public Mono<ValidTokenResponse> validateToken(String tokenId) {
	    return Mono.fromCallable(() -> {
	        Token t = tokenRepo.findByTokenId(tokenId)
	        		 .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_TOKEN));
	        
	        if (isExpired(t)) {
	            tokenRepo.delete(t);
	        	throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_TOKEN);
	        }
	        User u = userRepo.findById(t.getUserId()).get();
	        return new ValidTokenResponse(t.getUserId(), t.getExpiry(), u.getRoles());
	    }).subscribeOn(Schedulers.boundedElastic());
	}
	
	public Flux<User> getAllUsers(String token) {
	    return isValid(token)
	        .flatMapMany(isValid -> {
	            if (!isValid) {
	                return Flux.error(new RuntimeException(INVALID_TOKEN));
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

	        if (userRepo.findByUserNameIgnoreCase(userName).isPresent()) {
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
	    return isValid(token)
	        .flatMapMany(isValid -> {
	            if (!isValid) {
	                return Flux.error(new RuntimeException(INVALID_TOKEN));
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
	    return isValid(token)
	        .flatMapMany(isValid -> {
	            if (!isValid) {
	                return Flux.error(new RuntimeException(INVALID_TOKEN));
	            }

	            return Mono.fromCallable(userRepo::findAllUserRoles)
	                    .subscribeOn(Schedulers.boundedElastic())
	                    .flatMapMany(Flux::fromIterable);
	        });
	}
	
	private Token generateToken(int userId) {
		Token token  = new Token(userId);
		tokenRepo.save(token);
		return token;
	}
	
	private Mono<Boolean> isValid(String token) {
	    return validateToken(token)
	            .map(res -> true)
	            .onErrorReturn(false);
	}
	
	private Boolean isExpired(Token token) {
		return token.getExpiry().isBefore(LocalDateTime.now());
	}
}
