package es.deusto.spq.deustocrai.facade;

import java.util.Optional;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.deusto.spq.deustocrai.dto.CreateUserDTO;
import es.deusto.spq.deustocrai.dto.CredentialsDTO;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authorization Controller", description = "Login and logout operations")
public class AuthController {

    private AuthService authService;
    
	public AuthController(AuthService authService) {
		this.authService = authService;
	}
    
    // Login endpoint
    @Operation(
        summary = "Login to the system",
        description = "Allows a user to login by providing email and password. Returns a token if successful.",
        responses = {
            @ApiResponse(responseCode = "200", description = "OK: Login successful, returns a token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid credentials, login failed"),
        }
    )
    @PostMapping("/login")
    public ResponseEntity<String> login(
    		@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User's credentials", required = true)
    		@RequestBody CredentialsDTO credentials) {    	
        Optional<String> token = authService.login(credentials.getEmail(), credentials.getPassword());
        
    	if (token.isPresent()) {
    		return new ResponseEntity<>(token.get(), HttpStatus.OK);
    	} else {
    		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    	}
    }

    // Logout endpoint
    @Operation(
        summary = "Logout from the system",
        description = "Allows a user to logout by providing the authorization token.",
        responses = {
            @ApiResponse(responseCode = "204", description = "No Content: Logout successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid token, logout failed"),
        }
    )    
    @PostMapping("/logout")    
    public ResponseEntity<Void> logout(
    		@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Authorization token in plain text", required = true)
    		@RequestBody String token) {    	
        Optional<Boolean> result = authService.logout(token);
    	
        if (result.isPresent() && result.get()) {
        	return new ResponseEntity<>(HttpStatus.NO_CONTENT);	
        } else {
        	return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }        
    }
    @Operation(
    	    summary = "Registrar un nuevo usuario",
    	    description = "Crea un nuevo usuario en el sistema con el rol especificado (ESTUDIANTE, BIBLIOTECARIO o ADMIN)."
    	)
	@PostMapping("/register")
	public ResponseEntity<?> register(@Valid @RequestBody CreateUserDTO userDTO) { // Cambio de User a CreateUserDTO
	    // Llamada al servicio pasando el DTO
	    Optional<User> registeredUser = authService.register(userDTO);

	    if (registeredUser.isPresent()) {
	        return new ResponseEntity<>("Usuario registrado con éxito", HttpStatus.CREATED); //
	    } else {
	        return new ResponseEntity<>("El email ya está en uso", HttpStatus.BAD_REQUEST); //
	    }
	}
    
    @GetMapping("/me")
    @Operation(summary = "Obtener usuario actual", description = "Retorna los datos del usuario logueado usando su token")
    public ResponseEntity<User> getCurrentUser(@RequestHeader("Authorization") String token) {
        // Llamamos al método que ya tienes en tu AuthService
        User user = authService.getEmpleadoByToken(token);
        
        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}