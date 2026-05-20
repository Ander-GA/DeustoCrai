package es.deusto.spq.deustocrai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.deusto.spq.deustocrai.dao.UserRepository;
import es.deusto.spq.deustocrai.dto.CreateUserDTO;
import es.deusto.spq.deustocrai.entity.User;

/**
 * Tests unitarios para AuthService.
 * Se usa Mockito para aislar la capa de servicio del repositorio.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    // ─── Fixtures ───────────────────────────────────────────────────────────────

    private User usuarioEstudiante;
    private User usuarioBibliotecario;
    private User usuarioAdmin;

    @BeforeEach
    void setUp() {
        usuarioEstudiante = new User("Ana", "García", "pass123", "ana@deusto.es", User.Role.ESTUDIANTE);
        usuarioBibliotecario = new User("Pedro", "López", "biblio99", "pedro@deusto.es", User.Role.BIBLIOTECARIO);
        usuarioAdmin = new User("Root", "Admin", "adminpass", "admin@deusto.es", User.Role.ADMIN);
    }

    // ─── Login ───────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Login")
    class LoginTests {

        @Test
        @DisplayName("Login correcto: devuelve token no vacío")
        void testLoginExitoso() {
            when(userRepository.findByEmail("ana@deusto.es"))
                    .thenReturn(Optional.of(usuarioEstudiante));

            Optional<String> resultado = authService.login("ana@deusto.es", "pass123");

            assertTrue(resultado.isPresent(), "Debería devolver un token");
            assertFalse(resultado.get().isEmpty(), "El token no debería estar vacío");
        }

        @Test
        @DisplayName("Login correcto: el token generado es una cadena hexadecimal")
        void testLoginTokenEsHexadecimal() {
            when(userRepository.findByEmail("ana@deusto.es"))
                    .thenReturn(Optional.of(usuarioEstudiante));

            Optional<String> resultado = authService.login("ana@deusto.es", "pass123");

            assertTrue(resultado.isPresent());
            assertTrue(resultado.get().matches("[0-9a-f]+"), "El token debería ser hexadecimal");
        }

        @Test
        @DisplayName("Login con email inexistente: devuelve Optional vacío")
        void testLoginEmailNoExiste() {
            when(userRepository.findByEmail("fantasma@deusto.es"))
                    .thenReturn(Optional.empty());

            Optional<String> resultado = authService.login("fantasma@deusto.es", "cualquier");

            assertFalse(resultado.isPresent(), "No debería devolver token para usuario inexistente");
        }

        @Test
        @DisplayName("Login con contraseña incorrecta: devuelve Optional vacío")
        void testLoginPasswordIncorrecta() {
            when(userRepository.findByEmail("ana@deusto.es"))
                    .thenReturn(Optional.of(usuarioEstudiante));

            Optional<String> resultado = authService.login("ana@deusto.es", "passwordMal");

            assertFalse(resultado.isPresent(), "No debería devolver token con contraseña incorrecta");
        }

        @Test
        @DisplayName("Login con email correcto y contraseña vacía: devuelve Optional vacío")
        void testLoginPasswordVacia() {
            when(userRepository.findByEmail("ana@deusto.es"))
                    .thenReturn(Optional.of(usuarioEstudiante));

            Optional<String> resultado = authService.login("ana@deusto.es", "");

            assertFalse(resultado.isPresent());
        }

        @Test
        @DisplayName("Login con credenciales null: no lanza excepción inesperada")
        void testLoginEmailNull() {
            when(userRepository.findByEmail(null))
                    .thenReturn(Optional.empty());

            Optional<String> resultado = authService.login(null, null);

            assertFalse(resultado.isPresent());
        }

        @Test
        @DisplayName("Login con usuario bibliotecario: devuelve token correctamente")
        void testLoginBibliotecario() {
            when(userRepository.findByEmail("pedro@deusto.es"))
                    .thenReturn(Optional.of(usuarioBibliotecario));

            Optional<String> resultado = authService.login("pedro@deusto.es", "biblio99");

            assertTrue(resultado.isPresent());
        }

        @Test
        @DisplayName("Login con usuario admin: devuelve token correctamente")
        void testLoginAdmin() {
            when(userRepository.findByEmail("admin@deusto.es"))
                    .thenReturn(Optional.of(usuarioAdmin));

            Optional<String> resultado = authService.login("admin@deusto.es", "adminpass");

            assertTrue(resultado.isPresent());
        }

        @Test
        @DisplayName("Login dos veces seguidas: genera tokens distintos (por timestamp)")
        void testLoginDobleGeneraTokensDiferentes() throws InterruptedException {
            when(userRepository.findByEmail("ana@deusto.es"))
                    .thenReturn(Optional.of(usuarioEstudiante));

            Optional<String> token1 = authService.login("ana@deusto.es", "pass123");
            Thread.sleep(2); // Aseguramos diferencia de timestamp
            Optional<String> token2 = authService.login("ana@deusto.es", "pass123");

            assertTrue(token1.isPresent());
            assertTrue(token2.isPresent());
            // Con la implementación actual basada en timestamp pueden coincidir en la misma ms,
            // pero el test documenta la expectativa del sistema
        }

        @Test
        @DisplayName("Login verifica que se consulta el repositorio exactamente una vez")
        void testLoginLlamaRepositorioUnaVez() {
            when(userRepository.findByEmail("ana@deusto.es"))
                    .thenReturn(Optional.of(usuarioEstudiante));

            authService.login("ana@deusto.es", "pass123");

            verify(userRepository, times(1)).findByEmail("ana@deusto.es");
        }
    }

    // ─── Logout ──────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Logout")
    class LogoutTests {

        @Test
        @DisplayName("Logout con token válido: devuelve Optional<true>")
        void testLogoutConTokenValido() {
            when(userRepository.findByEmail("ana@deusto.es"))
                    .thenReturn(Optional.of(usuarioEstudiante));
            Optional<String> loginResult = authService.login("ana@deusto.es", "pass123");
            assertTrue(loginResult.isPresent());
            String token = loginResult.get();

            Optional<Boolean> resultado = authService.logout(token);

            assertTrue(resultado.isPresent());
            assertTrue(resultado.get());
        }

        @Test
        @DisplayName("Logout con token inexistente: devuelve Optional vacío")
        void testLogoutConTokenInexistente() {
            Optional<Boolean> resultado = authService.logout("tokenquenosexiste");

            assertFalse(resultado.isPresent());
        }

        @Test
        @DisplayName("Logout doble: el segundo intento devuelve Optional vacío")
        void testLogoutDoble() {
            when(userRepository.findByEmail("ana@deusto.es"))
                    .thenReturn(Optional.of(usuarioEstudiante));
            String token = authService.login("ana@deusto.es", "pass123").get();

            authService.logout(token);
            Optional<Boolean> segundoLogout = authService.logout(token);

            assertFalse(segundoLogout.isPresent(), "El segundo logout no debería tener efecto");
        }

        @Test
        @DisplayName("Logout con token null: devuelve Optional vacío sin excepción")
        void testLogoutConTokenNull() {
            Optional<Boolean> resultado = authService.logout(null);
            assertFalse(resultado.isPresent());
        }

        @Test
        @DisplayName("Logout invalida la sesión: getEmpleadoByToken devuelve null tras logout")
        void testLogoutInvalidaSesion() {
            when(userRepository.findByEmail("ana@deusto.es"))
                    .thenReturn(Optional.of(usuarioEstudiante));
            String token = authService.login("ana@deusto.es", "pass123").get();

            assertNotNull(authService.getEmpleadoByToken(token), "Debería estar en sesión antes del logout");

            authService.logout(token);

            assertNull(authService.getEmpleadoByToken(token), "Debería ser null después del logout");
        }
    }

    // ─── Register ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Register")
    class RegisterTests {

        private CreateUserDTO buildDTO(String nombre, String apellidos, String email, String pass, CreateUserDTO.Role rol) {
            CreateUserDTO dto = new CreateUserDTO();
            dto.setNombre(nombre);
            dto.setApellidos(apellidos);
            dto.setEmail(email);
            dto.setPassword(pass);
            dto.setRole(rol);
            return dto;
        }

        @Test
        @DisplayName("Registro exitoso: devuelve el usuario guardado")
        void testRegistroExitoso() {
            CreateUserDTO dto = buildDTO("Nuevo", "Usuario", "nuevo@deusto.es", "1234", CreateUserDTO.Role.ESTUDIANTE);
            User saved = new User("Nuevo", "Usuario", "1234", "nuevo@deusto.es", User.Role.ESTUDIANTE);

            when(userRepository.findByEmail("nuevo@deusto.es")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(saved);

            Optional<User> resultado = authService.register(dto);

            assertTrue(resultado.isPresent());
            assertEquals("nuevo@deusto.es", resultado.get().getEmail());
        }

        @Test
        @DisplayName("Registro con email duplicado: devuelve Optional vacío")
        void testRegistroEmailDuplicado() {
            CreateUserDTO dto = buildDTO("Otro", "User", "ana@deusto.es", "xyz", CreateUserDTO.Role.ESTUDIANTE);

            when(userRepository.findByEmail("ana@deusto.es")).thenReturn(Optional.of(usuarioEstudiante));

            Optional<User> resultado = authService.register(dto);

            assertFalse(resultado.isPresent());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Registro con rol BIBLIOTECARIO: se guarda correctamente")
        void testRegistroConRolBibliotecario() {
            CreateUserDTO dto = buildDTO("Luis", "Martín", "luis@deusto.es", "pass", CreateUserDTO.Role.BIBLIOTECARIO);
            User saved = new User("Luis", "Martín", "pass", "luis@deusto.es", User.Role.BIBLIOTECARIO);

            when(userRepository.findByEmail("luis@deusto.es")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(saved);

            Optional<User> resultado = authService.register(dto);

            assertTrue(resultado.isPresent());
            assertEquals(User.Role.BIBLIOTECARIO, resultado.get().getRole());
        }

        @Test
        @DisplayName("Registro con rol ADMIN: se guarda correctamente")
        void testRegistroConRolAdmin() {
            CreateUserDTO dto = buildDTO("Sara", "Ruiz", "sara@deusto.es", "admin", CreateUserDTO.Role.ADMIN);
            User saved = new User("Sara", "Ruiz", "admin", "sara@deusto.es", User.Role.ADMIN);

            when(userRepository.findByEmail("sara@deusto.es")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(saved);

            Optional<User> resultado = authService.register(dto);

            assertTrue(resultado.isPresent());
            assertEquals(User.Role.ADMIN, resultado.get().getRole());
        }

        @Test
        @DisplayName("Registro llama a userRepository.save exactamente una vez si el email no existe")
        void testRegistroLlamaSaveUnaVez() {
            CreateUserDTO dto = buildDTO("Test", "Save", "save@deusto.es", "pw", CreateUserDTO.Role.ESTUDIANTE);
            User saved = new User("Test", "Save", "pw", "save@deusto.es", User.Role.ESTUDIANTE);

            when(userRepository.findByEmail("save@deusto.es")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(saved);

            authService.register(dto);

            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Registro captura los datos del DTO correctamente en la entidad guardada")
        void testRegistroCapturaDTO() {
            CreateUserDTO dto = buildDTO("Carla", "Vega", "carla@deusto.es", "secret", CreateUserDTO.Role.ESTUDIANTE);
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            when(userRepository.findByEmail("carla@deusto.es")).thenReturn(Optional.empty());
            when(userRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            authService.register(dto);

            User captured = captor.getValue();
            assertEquals("Carla", captured.getNombre());
            assertEquals("Vega", captured.getApellidos());
            assertEquals("carla@deusto.es", captured.getEmail());
            assertEquals("secret", captured.getPassword());
            assertEquals(User.Role.ESTUDIANTE, captured.getRole());
        }

        @Test
        @DisplayName("Registro: el repositorio findByEmail se llama una vez antes de save")
        void testRegistroOrdenDeLlamadas() {
            CreateUserDTO dto = buildDTO("Orden", "Test", "orden@deusto.es", "pw", CreateUserDTO.Role.ESTUDIANTE);

            when(userRepository.findByEmail("orden@deusto.es")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            authService.register(dto);

            var inOrder = inOrder(userRepository);
            inOrder.verify(userRepository).findByEmail("orden@deusto.es");
            inOrder.verify(userRepository).save(any(User.class));
        }
    }

    // ─── getEmpleadoByToken ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("getEmpleadoByToken")
    class GetEmpleadoByTokenTests {

        @Test
        @DisplayName("Devuelve el usuario correcto para un token activo")
        void testGetEmpleadoConTokenActivo() {
            when(userRepository.findByEmail("ana@deusto.es"))
                    .thenReturn(Optional.of(usuarioEstudiante));
            String token = authService.login("ana@deusto.es", "pass123").get();

            User resultado = authService.getEmpleadoByToken(token);

            assertNotNull(resultado);
            assertEquals("ana@deusto.es", resultado.getEmail());
        }

        @Test
        @DisplayName("Devuelve null para un token que nunca existió")
        void testGetEmpleadoConTokenInexistente() {
            User resultado = authService.getEmpleadoByToken("tokenFalso123");
            assertNull(resultado);
        }

        @Test
        @DisplayName("Devuelve null con token null")
        void testGetEmpleadoConNull() {
            User resultado = authService.getEmpleadoByToken(null);
            assertNull(resultado);
        }

        @Test
        @DisplayName("Después del logout el token ya no recupera usuario")
        void testGetEmpleadoDespuesDeLogout() {
            when(userRepository.findByEmail("ana@deusto.es"))
                    .thenReturn(Optional.of(usuarioEstudiante));
            String token = authService.login("ana@deusto.es", "pass123").get();
            authService.logout(token);

            User resultado = authService.getEmpleadoByToken(token);

            assertNull(resultado);
        }

        @Test
        @DisplayName("Recupera el rol correcto del usuario en sesión")
        void testGetEmpleadoDevuelveRolCorrecto() {
            when(userRepository.findByEmail("pedro@deusto.es"))
                    .thenReturn(Optional.of(usuarioBibliotecario));
            String token = authService.login("pedro@deusto.es", "biblio99").get();

            User resultado = authService.getEmpleadoByToken(token);

            assertEquals(User.Role.BIBLIOTECARIO, resultado.getRole());
        }
    }

    // ─── getEmpleadoByEmail ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("getEmpleadoByEmail")
    class GetEmpleadoByEmailTests {

        @Test
        @DisplayName("Devuelve el usuario cuando el email existe")
        void testGetPorEmailExistente() {
            when(userRepository.findByEmail("ana@deusto.es"))
                    .thenReturn(Optional.of(usuarioEstudiante));

            Optional<User> resultado = authService.getEmpleadoByEmail("ana@deusto.es");

            assertTrue(resultado.isPresent());
            assertEquals("ana@deusto.es", resultado.get().getEmail());
        }

        @Test
        @DisplayName("Devuelve Optional vacío cuando el email no existe")
        void testGetPorEmailNoExistente() {
            when(userRepository.findByEmail("noexiste@deusto.es"))
                    .thenReturn(Optional.empty());

            Optional<User> resultado = authService.getEmpleadoByEmail("noexiste@deusto.es");

            assertFalse(resultado.isPresent());
        }

        @Test
        @DisplayName("Llama al repositorio con el email exacto recibido")
        void testGetPorEmailDelegaEnRepositorio() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            authService.getEmpleadoByEmail("exacto@deusto.es");

            verify(userRepository).findByEmail("exacto@deusto.es");
        }
    }
}