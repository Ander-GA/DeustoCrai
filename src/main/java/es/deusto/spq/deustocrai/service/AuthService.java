/**
 * This code is based on solutions provided by ChatGPT 4o and 
 * adapted using GitHub Copilot. It has been thoroughly reviewed 
 * and validated to ensure correctness and that it is free of errors.
 */
package es.deusto.spq.deustocrai.service;

import org.springframework.stereotype.Service;

import es.deusto.spq.deustocrai.dao.UserRepository;
import es.deusto.spq.deustocrai.entity.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

	private final UserRepository userRepository;

   
    // Storage to keep the session of the Empleados that are logged in
    private static Map<String, User> tokenStore = new HashMap<>(); 

    public AuthService(UserRepository userRepository) {
			this.userRepository = userRepository;
    }
    
    // Login method that checks if the Empleado exists in the database and validates the password
    public Optional<String> login(String email, String password) {
        Optional<User> empleado = userRepository.findByEmail(email);
        
        if(empleado.isPresent()&& empleado.get().checkPassword(password)) {
			String token = generateToken();  // Generate a random token for the session
			tokenStore.put(token, empleado.get());     // Store the token and associate it with the Empleado
			return Optional.of(token);
        	
        }else {
			return Optional.empty();
		}
    }
    
    // Logout method to remove the token from the session store
    public Optional<Boolean> logout(String token) {
        if (tokenStore.containsKey(token)) {
            tokenStore.remove(token);

            return Optional.of(true);
        } else {
            return Optional.empty();
        }
    }
    
    public Optional<User> register(User newUser) {
        // 1. Verificar si el email ya está registrado en la BD
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            return Optional.empty();
        }
        
        // 2. Guardar el nuevo usuario
        return Optional.of(userRepository.save(newUser));
    }
    
    // Method to get the Empleado based on the token
    public User getEmpleadoByToken(String token) {
        return tokenStore.get(token); 
    }
    
    // Method to get the Empleado based on the email
    public Optional<User> getEmpleadoByEmail(String email) {
		return userRepository.findByEmail(email);
	}

    // Synchronized method to guarantee unique token generation
    private static synchronized String generateToken() {
        return Long.toHexString(System.currentTimeMillis());
    }
}