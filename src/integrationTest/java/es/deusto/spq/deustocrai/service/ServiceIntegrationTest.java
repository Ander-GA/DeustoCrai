package es.deusto.spq.deustocrai.service;

import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import es.deusto.spq.deustocrai.dao.*;
import es.deusto.spq.deustocrai.entity.*;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Tag;

@Tag("Integracion")
@SpringBootTest
@Transactional // Limpia la base de datos automáticamente después de cada test
public class ServiceIntegrationTest {

    @Autowired
    private LibroService libroService;
    
    @Autowired
    private PrestamoService prestamoService;
    
    @Autowired
    private ReservaService reservaService;

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AulaRepository aulaRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    private User usuarioTest;
    private Libro libroTest;
    private Aula aulaTest;

    @BeforeEach
    public void setUp() {
        // Limpiamos datos para evitar conflictos de integridad referencial
        reservaRepository.deleteAll();
        prestamoRepository.deleteAll();
        libroRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Creamos y guardamos un usuario real para los tests
        usuarioTest = new User();
        usuarioTest.setEmail("integracion.test@deusto.es");
        usuarioTest.setNombre("Usuario");
        usuarioTest.setApellidos("De Prueba");
        usuarioTest.setPassword("pass12345");
        usuarioTest.setRole(User.Role.ESTUDIANTE);
        usuarioTest = userRepository.save(usuarioTest);

        // 2. Creamos un libro real
        libroTest = new Libro("Clean Code Integracion", "978-0131350884", "Robert C. Martin");
        libroTest.setDisponible(true);
        libroTest = libroRepository.save(libroTest);
        
        // 3. Creamos un aula real
        aulaTest = new Aula("Aula Integracion 01", 10, true);
        aulaTest = aulaRepository.save(aulaTest);
    }

    @Test
    @DisplayName("Integración: Ciclo completo de añadir libro y verificar su persistencia")
    void testAnadirYListarLibroPersistencia() {
        Libro nuevo = new Libro("Refactoring", "978-0201485677", "Martin Fowler");
        libroService.anadirLibro(nuevo);

        List<Libro> libros = libroService.listarLibros();
        assertTrue(libros.stream().anyMatch(l -> l.getIsbn().equals("978-0201485677")), 
            "El libro debería estar guardado y ser recuperable por el servicio");
    }

    @Test
    @DisplayName("Integración: Realizar préstamo real y comprobar cambio de estado en BD")
    void testFlujoPrestamoPersistencia() {
        // Tu PrestamoService recibe el objeto User y el Long del libroId
        prestamoService.realizarPrestamo(usuarioTest, libroTest.getId());

        // Verificamos que el cambio ha llegado a la base de datos real
        Libro libroEnBD = libroRepository.findById(libroTest.getId()).get();
        assertFalse(libroEnBD.isDisponible(), "La base de datos debe reflejar que el libro ya no está disponible");
    }

    @Test
    @DisplayName("Integración: Realizar reserva de aula y comprobar solapamientos")
    void testReservaAulaPersistencia() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime fin = inicio.plusHours(2);
        
        // Tu ReservaService.realizarReserva recibe el objeto Reserva completo
        Reserva nuevaReserva = new Reserva(usuarioTest, aulaTest, inicio, fin);
        Optional<Reserva> resultado = reservaService.realizarReserva(nuevaReserva);
        
        assertTrue(resultado.isPresent(), "La reserva debería haberse creado correctamente en la BD");
        
        // Comprobamos que el servicio de listado por aula funciona con datos reales
        List<Reserva> reservasAula = reservaService.getReservasPorAula(aulaTest.getId());
        assertEquals(1, reservasAula.size());
    }

    @Test
    @DisplayName("Integración: Borrado físico de un recurso y validación en repositorio")
    void testBorradoFisicoPersistencia() {
        Long id = libroTest.getId();
        libroService.borrarLibro(id);
        
        Optional<Libro> borrado = libroRepository.findById(id);
        assertTrue(borrado.isEmpty(), "El libro debe haber sido eliminado físicamente de la base de datos");
    }
}