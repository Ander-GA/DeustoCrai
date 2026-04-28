package es.deusto.spq.deustocrai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.deusto.spq.deustocrai.dao.UserRepository;
import es.deusto.spq.deustocrai.entity.User;
import org.junit.jupiter.api.Tag;

@Tag("Unitario")
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private User usuarioMock;

    @BeforeEach
    void setUp() {
        usuarioMock = mock(User.class);
        // HEMOS ELIMINADO EL STUB INNECESARIO DE getEmail() AQUÍ
    }

    @Test
    @DisplayName("login: Debería devolver un token cuando las credenciales son correctas")
    void testLoginExitoso() {
        when(userRepository.findByEmail("admin@deusto.es")).thenReturn(Optional.of(usuarioMock));
        when(usuarioMock.checkPassword("passwordCorrecta")).thenReturn(true); 

        Optional<String> token = authService.login("admin@deusto.es", "passwordCorrecta");

        assertTrue(token.isPresent(), "El token debería haberse generado");
    }

    @Test
    @DisplayName("login: Debería devolver Optional vacio si la contraseña es incorrecta")
    void testLoginFallido() {
        when(userRepository.findByEmail("admin@deusto.es")).thenReturn(Optional.of(usuarioMock));
        when(usuarioMock.checkPassword("passwordIncorrecta")).thenReturn(false); 

        Optional<String> token = authService.login("admin@deusto.es", "passwordIncorrecta");

        assertFalse(token.isPresent(), "No debería devolver token con contraseña incorrecta");
    }
}