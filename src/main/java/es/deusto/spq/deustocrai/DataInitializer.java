package es.deusto.spq.deustocrai;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.deusto.spq.deustocrai.dao.AulaRepository;
import es.deusto.spq.deustocrai.dao.MaterialRepository;
import es.deusto.spq.deustocrai.dao.UserRepository; 
import es.deusto.spq.deustocrai.entity.Aula;
import es.deusto.spq.deustocrai.entity.Material;
import es.deusto.spq.deustocrai.entity.User; 
import es.deusto.spq.deustocrai.dao.LibroRepository;
import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.dao.ReservaRepository;
import es.deusto.spq.deustocrai.entity.Libro;

// IMPORTAMOS LOS NUEVOS DAO Y ENTIDADES
import es.deusto.spq.deustocrai.dao.InstalacionRepository; 
import es.deusto.spq.deustocrai.dao.ReservaInstalacionRepository; // <-- NUEVO
import es.deusto.spq.deustocrai.entity.InstalacionDeportiva;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initData(
            UserRepository userRepository,
            AulaRepository aulaRepository,
            MaterialRepository materialRepository,
            LibroRepository libroRepository,
            ReservaRepository reservaRepository,
            PrestamoRepository prestamoRepository,
            InstalacionRepository instalacionRepository,
            ReservaInstalacionRepository reservaInstalacionRepository) { // <-- LO INYECTAMOS AQUÍ

        return args -> {
            // 1. Limpiar datos previos en el orden correcto (primero los hijos, luego los padres)
            reservaInstalacionRepository.deleteAll(); // <-- BORRAMOS LAS RESERVAS DE PISTAS PRIMERO
            prestamoRepository.deleteAll();
            reservaRepository.deleteAll();
            userRepository.deleteAll();
            libroRepository.deleteAll();
            aulaRepository.deleteAll();
            materialRepository.deleteAll(); 
            instalacionRepository.deleteAll(); 

            // 2. Crear Usuarios
            User ander = new User();
            ander.setNombre("Ander");
            ander.setApellidos("Gonzalez Alonso");
            ander.setPassword("pass1");
            ander.setEmail("ander.gonzalez.a@opendeusto.es");
            ander.setRole(User.Role.ESTUDIANTE);

            User inigo = new User();
            inigo.setNombre("Iñigo");
            inigo.setApellidos("Melchisidor Urquijo");
            inigo.setPassword("pass2");
            inigo.setEmail("i.melchisidor@opendeusto.es");
            inigo.setRole(User.Role.ESTUDIANTE);

            User emilio = new User();
            emilio.setNombre("Emilio");
            emilio.setApellidos("Gil del Rio Perez");
            emilio.setPassword("pass3");
            emilio.setEmail("emilio.gildelrio@opendeusto.es");
            emilio.setRole(User.Role.ESTUDIANTE);

            User gaizka = new User();
            gaizka.setNombre("Gaizka");
            gaizka.setApellidos("Gredilla Yarritu");
            gaizka.setPassword("pass4");
            gaizka.setEmail("gaizka.gredilla@opendeusto.es");
            gaizka.setRole(User.Role.ESTUDIANTE);

            User jacqueline = new User();
            jacqueline.setNombre("Jacqueline");
            jacqueline.setApellidos("Furelos Parra");
            jacqueline.setPassword("pass5");
            jacqueline.setEmail("jacqueline.furelos@opendeusto.es");
            jacqueline.setRole(User.Role.ESTUDIANTE);
            
            User admin = new User();
            admin.setNombre("Admin");
            admin.setApellidos("Sistema DeustoCrai");
            admin.setPassword("1");
            admin.setEmail("1");
            admin.setRole(User.Role.ADMIN);

            User bibliotecario = new User();
            bibliotecario.setNombre("Biblio");
            bibliotecario.setApellidos("Tecario");
            bibliotecario.setPassword("biblio123");
            bibliotecario.setEmail("bibliotecario@deusto.es");
            bibliotecario.setRole(User.Role.BIBLIOTECARIO);

            userRepository.saveAll(List.of(ander, inigo, emilio, gaizka, jacqueline, admin, bibliotecario));
            logger.info("Usuarios de DeustoCrai guardados!");

            // 3. Crear Aulas (Salas del CRAI)
            Aula sala1 = new Aula();
            sala1.setNombre("Aula 01");
            sala1.setCapacidad(8);

            Aula sala2 = new Aula();
            sala2.setNombre("Aula 02");
            sala2.setCapacidad(4);

            Aula sala3 = new Aula();
            sala3.setNombre("Aula 03");
            sala3.setCapacidad(20);

            aulaRepository.saveAll(List.of(sala1, sala2, sala3));
            logger.info("Salas del CRAI guardadas!");

            // 4. Crear Materiales iniciales
            Material portatil = new Material("Portátil Dell Latitude", "SN-DELL-001", "Portatil");
            Material camara = new Material("Cámara Canon EOS", "SN-CAN-500", "Camara");
            
            materialRepository.saveAll(List.of(portatil, camara));
            logger.info("Materiales del CRAI guardados!");
            
            // 5. Crear Libros para el catálogo
            Libro libro1 = new Libro("Clean Code: A Handbook of Agile Software Craftsmanship", "978-0132350884", "Robert C. Martin");
            Libro libro2 = new Libro("Design Patterns: Elements of Reusable Object-Oriented Software", "978-0201633610", "Erich Gamma");
            Libro libro3 = new Libro("El Señor de los Anillos: La Comunidad del Anillo", "978-8445071409", "J.R.R. Tolkien");
            
            libroRepository.saveAll(List.of(libro1, libro2, libro3));
            logger.info("Libros del catálogo guardados!");
            
            // 6. CREAR INSTALACIONES DEPORTIVAS
            InstalacionDeportiva p1 = new InstalacionDeportiva("Pista Pádel 1", "PADEL");
            InstalacionDeportiva p2 = new InstalacionDeportiva("Pista Pádel 2", "PADEL");
            InstalacionDeportiva p3 = new InstalacionDeportiva("Pista Pádel 3", "PADEL");
            InstalacionDeportiva futbol = new InstalacionDeportiva("Campo de Fútbol", "FUTBOL");
            
            instalacionRepository.saveAll(List.of(p1, p2, p3, futbol));
            logger.info("Instalaciones deportivas guardadas!");

            logger.info(">> Inicialización de datos completada con éxito.");
        };
    }
}