package org.ac.cst8277.Joyce.Samuel.user_service;

import org.ac.cst8277.Joyce.Samuel.user_service.tokens.ValidTokenResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // Login
//    @PostMapping("/login")
//    public Mono<String> login(@RequestParam String username) {
//        return service.oAuthLogin(username);
//    }

    // Validate token
    @GetMapping("/validate")
    public Mono<ValidTokenResponse> validate(@RequestParam String token) {
        return service.validateToken(token);
    }
    
    // Get all users
    @GetMapping
    public Flux<User> getAllUsers(@RequestParam String token){
    	return service.getAllUsers(token);
    }
    
    // Get all Roles
    @GetMapping("/roles")
    public Flux<Role> getAllRoles(@RequestParam String token){
    	return service.getAllRoles(token);
    }
    
    // Get all User Roles
    @GetMapping("/user-roles")
    public Flux<UserRolesDTO> getAllUserRoles(@RequestParam String token){
    	return service.getAllUserRoles(token);
    }
    
    // Create a new User
    @PostMapping("/new-user")
    public Mono<ApiResponse> createNewUser(	
    		@RequestParam String userName, 
    		@RequestParam String email, 
    		@RequestParam String password){
    	return service.addUser(userName, email, password);
    }
}