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
import org.junit.jupiter.api.Tag;

import es.deusto.spq.deustocrai.dao.UserRepository;
import es.deusto.spq.deustocrai.dto.CreateUserDTO;
import es.deusto.spq.deustocrai.entity.User;

@Tag("Unitario")
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private User mockUser;
    private CreateUserDTO createUserDTO;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@deusto.es");
        mockUser.setNombre("Test");
        mockUser.setRole(User.Role.ESTUDIANTE);
        mockUser.setPassword("1234"); 
        
        createUserDTO = new CreateUserDTO();
        createUserDTO.setEmail("nuevo@deusto.es");
        createUserDTO.setPassword("1234");
        createUserDTO.setNombre("Nuevo");
        createUserDTO.setApellidos("Usuario");
        createUserDTO.setRole(CreateUserDTO.Role.ESTUDIANTE);
    }

    @Test
    @DisplayName("login: Retorna un token si el usuario existe y la contraseña es correcta")
    void testLoginSuccess() {
        // Usamos spy para simular el método checkPassword del User
        User spyUser = spy(mockUser);
        when(spyUser.checkPassword("1234")).thenReturn(true);
        when(userRepository.findByEmail("test@deusto.es")).thenReturn(Optional.of(spyUser));

        Optional<String> token = authService.login("test@deusto.es", "1234");

        assertTrue(token.isPresent());
        assertNotNull(token.get());
    }

    @Test
    @DisplayName("login: Retorna Optional.empty si el usuario no existe")
    void testLoginUserNotFound() {
        when(userRepository.findByEmail("noexiste@deusto.es")).thenReturn(Optional.empty());

        Optional<String> token = authService.login("noexiste@deusto.es", "1234");

        assertTrue(token.isEmpty());
    }

    @Test
    @DisplayName("login: Retorna Optional.empty si la contraseña es incorrecta")
    void testLoginWrongPassword() {
        User spyUser = spy(mockUser);
        when(spyUser.checkPassword("mala")).thenReturn(false);
        when(userRepository.findByEmail("test@deusto.es")).thenReturn(Optional.of(spyUser));

        Optional<String> token = authService.login("test@deusto.es", "mala");

        assertTrue(token.isEmpty());
    }

    @Test
    @DisplayName("register: Registra usuario si el email no existe")
    void testRegisterSuccess() {
        when(userRepository.findByEmail(createUserDTO.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        Optional<User> registrado = authService.register(createUserDTO);

        assertTrue(registrado.isPresent());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("register: Falla si el email ya existe")
    void testRegisterDuplicado() {
        when(userRepository.findByEmail(createUserDTO.getEmail())).thenReturn(Optional.of(mockUser));

        Optional<User> registrado = authService.register(createUserDTO);

        assertTrue(registrado.isEmpty());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Flujo completo de Sesión: Login, getEmpleadoByToken y Logout")
    void testTokenStoreFlow() {
        // 1. Hacemos login para obtener un token simulado
        User spyUser = spy(mockUser);
        when(spyUser.checkPassword("1234")).thenReturn(true);
        when(userRepository.findByEmail("test@deusto.es")).thenReturn(Optional.of(spyUser));
        
        Optional<String> tokenOpt = authService.login("test@deusto.es", "1234");
        assertTrue(tokenOpt.isPresent());
        String token = tokenOpt.get();

        // 2. Verificamos que getEmpleadoByToken devuelve el usuario correcto
        User userRecuperado = authService.getEmpleadoByToken(token);
        assertNotNull(userRecuperado);
        assertEquals("test@deusto.es", userRecuperado.getEmail());

        // 3. Verificamos que el logout funciona y borra el token
        Optional<Boolean> logoutResult = authService.logout(token);
        assertTrue(logoutResult.isPresent());
        assertTrue(logoutResult.get());

        // 4. Verificamos que el token ya no existe en el sistema
        assertNull(authService.getEmpleadoByToken(token));
        
        // 5. Verificamos que falla si intentamos desloguear un token inventado
        Optional<Boolean> badLogout = authService.logout("token-invalido");
        assertTrue(badLogout.isEmpty());
    }

    @Test
    @DisplayName("getEmpleadoByEmail: Llama al repositorio para buscar")
    void testGetEmpleadoByEmail() {
        when(userRepository.findByEmail("test@deusto.es")).thenReturn(Optional.of(mockUser));

        Optional<User> resultado = authService.getEmpleadoByEmail("test@deusto.es");

        assertTrue(resultado.isPresent());
        assertEquals("test@deusto.es", resultado.get().getEmail());
    }
}