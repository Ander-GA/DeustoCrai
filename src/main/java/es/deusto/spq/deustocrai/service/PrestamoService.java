package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.dao.ColaEsperaRepository;
import es.deusto.spq.deustocrai.dao.LibroRepository;
import es.deusto.spq.deustocrai.dao.MaterialRepository;
import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.entity.ColaEspera;
import es.deusto.spq.deustocrai.entity.Libro;
import es.deusto.spq.deustocrai.entity.Material;
import es.deusto.spq.deustocrai.entity.Prestamo;
import es.deusto.spq.deustocrai.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PrestamoService {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private LibroRepository libroRepository;
    
    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ColaEsperaService colaEsperaService;

    @Autowired
    private ColaEsperaRepository colaEsperaRepository;

    public List<Prestamo> obtenerPrestamosPorUsuario(User usuario) {
        List<Prestamo> todosLosPrestamos = prestamoRepository.findByUsuarioId(usuario.getId());
        
        return todosLosPrestamos.stream()
                .filter(prestamo -> prestamo.getEstado() != Prestamo.EstadoPrestamo.DEVUELTO)
                .toList();
    }
    
    public List<Prestamo> obtenerHistorialPorUsuario(User usuario) {
        return prestamoRepository.findByUsuarioIdAndEstado(
                usuario.getId(),
                Prestamo.EstadoPrestamo.DEVUELTO
        );
    }

    public List<Prestamo> obtenerTodosLosPrestamos() {
        return prestamoRepository.findAll();
    }

    public Prestamo realizarPrestamo(User usuario, Long libroId) {
        Optional<Libro> libroOpt = libroRepository.findById(libroId);
        
        if (libroOpt.isPresent() && libroOpt.get().isDisponible()) {
            Libro libro = libroOpt.get();
            libro.setDisponible(false); 
            libroRepository.save(libro); 
            
            Prestamo prestamo = new Prestamo(usuario, libro);
            return prestamoRepository.save(prestamo);
        }

        return null; 
    }

    public boolean devolverPrestamo(User usuario, Long prestamoId) {
        Optional<Prestamo> prestamoOpt = prestamoRepository.findById(prestamoId);
        
        if (prestamoOpt.isPresent()) {
            Prestamo prestamo = prestamoOpt.get();
            
            if (prestamo.getUsuario().getId().equals(usuario.getId()) &&
                    prestamo.getEstado() != Prestamo.EstadoPrestamo.DEVUELTO) {

                if (prestamo.getRecurso() instanceof Libro) {
                    Libro libro = (Libro) prestamo.getRecurso();
                    colaEsperaService.asignarPrimerUsuarioSiExiste(libro);

                } else if (prestamo.getRecurso() instanceof Material) {
                    Material material = (Material) prestamo.getRecurso();

                    // El material tecnológico queda pendiente de control de calidad.
                    // No vuelve automáticamente al catálogo disponible.
                    material.setDisponible(false);
                    materialRepository.save(material);
                }

                prestamo.setFechaDevolucionReal(LocalDate.now());
                prestamo.setEstado(Prestamo.EstadoPrestamo.DEVUELTO);
                prestamoRepository.save(prestamo);

                return true;
            }
        }

        return false;
    }

    public boolean cambiarEstadoPrestamo(Long prestamoId, Prestamo.EstadoPrestamo nuevoEstado) {
        Optional<Prestamo> prestamoOpt = prestamoRepository.findById(prestamoId);
        
        if (prestamoOpt.isPresent()) {
            Prestamo prestamo = prestamoOpt.get();
            prestamo.setEstado(nuevoEstado);
            
            if (nuevoEstado == Prestamo.EstadoPrestamo.DEVUELTO) {
                prestamo.setFechaDevolucionReal(LocalDate.now());

                if (prestamo.getRecurso() instanceof Libro) {
                    Libro libro = (Libro) prestamo.getRecurso();
                    colaEsperaService.asignarPrimerUsuarioSiExiste(libro);

                } else if (prestamo.getRecurso() instanceof Material) {
                    Material material = (Material) prestamo.getRecurso();

                    // El material tecnológico queda pendiente de control de calidad.
                    material.setDisponible(false);
                    materialRepository.save(material);
                }
            }
            
            prestamoRepository.save(prestamo);
            return true;
        }

        return false;
    }

    public Prestamo realizarPrestamoMaterial(User usuario, Long materialId) {
        Optional<Material> materialOpt = materialRepository.findById(materialId);
        
        if (materialOpt.isPresent() && materialOpt.get().isDisponible()) {
            Material material = materialOpt.get();
            material.setDisponible(false); 
            materialRepository.save(material); 
            
            Prestamo prestamo = new Prestamo(usuario, material);
            return prestamoRepository.save(prestamo);
        }

        return null; 
    }
    
    public List<Prestamo> obtenerPrestamosLibrosActivos() {
        return prestamoRepository.findLibrosPrestadosActivos();
    }

    public List<Prestamo> obtenerPrestamosMaterialesActivos() {
        return prestamoRepository.findMaterialesPrestadosActivos();
    }

    public Map<String, Integer> obtenerEstadisticasUsuario(User usuario) {
        List<Prestamo> prestamos = prestamoRepository.findByUsuarioId(usuario.getId());
        
        int total = prestamos.size();
        int activos = 0;
        int aTiempo = 0;
        int conRetraso = 0;

        for (Prestamo p : prestamos) {
            if (p.getEstado() != Prestamo.EstadoPrestamo.DEVUELTO) {
                activos++;
            } else {
                if (p.getFechaDevolucionReal() != null &&
                        p.getFechaDevolucionReal().isAfter(p.getFechaDevolucionPrevista())) {
                    conRetraso++;
                } else {
                    aTiempo++; 
                }
            }
        }

        Map<String, Integer> estadisticas = new HashMap<>();
        estadisticas.put("totalPrestamos", total);
        estadisticas.put("prestamosActivos", activos);
        estadisticas.put("devueltosATiempo", aTiempo);
        estadisticas.put("devueltosConRetraso", conRetraso);

        return estadisticas;
    }

    @Transactional
    public Prestamo renovarPrestamo(Long prestamoId, User usuarioAutenticado) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new IllegalArgumentException("Préstamo no encontrado"));

        if (!prestamo.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new IllegalStateException("No tienes permiso para renovar este préstamo");
        }

        if (prestamo.getEstado() == Prestamo.EstadoPrestamo.DEVUELTO) {
            throw new IllegalStateException("Solo se pueden renovar préstamos activos");
        }
        
        if (prestamo.getFechaDevolucionPrevista().isBefore(LocalDate.now())) {
            throw new IllegalStateException("El préstamo ya está vencido, debes devolverlo y pagar penalización si aplica.");
        }

        List<ColaEspera> cola = colaEsperaRepository.findByRecursoIdAndEstadoOrderByFechaEntradaAsc(
                prestamo.getRecurso().getId(), 
                ColaEspera.EstadoCola.ACTIVA
        );

        if (!cola.isEmpty()) {
            throw new IllegalStateException("No puedes renovar el material porque hay usuarios en lista de espera");
        }

        prestamo.setFechaDevolucionPrevista(prestamo.getFechaDevolucionPrevista().plusDays(7));
        return prestamoRepository.save(prestamo);
    }
}