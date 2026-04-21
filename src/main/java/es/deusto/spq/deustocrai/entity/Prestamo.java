package es.deusto.spq.deustocrai.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "prestamo")
public class Prestamo {

    public enum EstadoPrestamo {
        PENDIENTE_ENTREGA,
        ENTREGADO,
        DEVUELTO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User usuario;

    // Esta es la "cuerda". Permitimos que sea null en un futuro si borramos el libro.
    @ManyToOne
    @JoinColumn(name = "recurso_id", nullable = true) 
    private AbstractRecurso recurso;

    // --- NUEVO: El Snapshot / Foto del texto ---
    @Column(nullable = false)
    private String nombreRecursoHistorico; 

    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucionPrevista;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPrestamo estado;

    public Prestamo() {}

    public Prestamo(User usuario, AbstractRecurso recurso) {
        this.usuario = usuario;
        this.recurso = recurso;
        
        // Guardamos el texto en el momento de crear el préstamo
        // Asumo que AbstractRecurso tiene el método getTitulo()
        this.nombreRecursoHistorico = recurso.getTitulo(); 
        
        this.fechaPrestamo = LocalDate.now();
        this.fechaDevolucionPrevista = LocalDate.now().plusDays(7);
        this.estado = EstadoPrestamo.PENDIENTE_ENTREGA;
    }

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public User getUsuario() { return usuario; }
    
    public AbstractRecurso getRecurso() { return recurso; }
    public void setRecurso(AbstractRecurso recurso) { this.recurso = recurso; } // Importante añadir el setter
    
    public String getNombreRecursoHistorico() { return nombreRecursoHistorico; }
    public void setNombreRecursoHistorico(String nombreRecursoHistorico) { this.nombreRecursoHistorico = nombreRecursoHistorico; }

    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public LocalDate getFechaDevolucionPrevista() { return fechaDevolucionPrevista; }
    public EstadoPrestamo getEstado() { return estado; }
    public void setEstado(EstadoPrestamo estado) { this.estado = estado; }
}