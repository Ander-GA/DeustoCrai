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

    @ManyToOne
    @JoinColumn(name = "recurso_id")
    private AbstractRecurso recurso;
    
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucionPrevista;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPrestamo estado;
    
    public Prestamo() {}

    public Prestamo(User usuario, AbstractRecurso recurso) {
        this.usuario = usuario;
        this.recurso = recurso;
        this.fechaPrestamo = LocalDate.now();
        this.fechaDevolucionPrevista = LocalDate.now().plusDays(7); // Por defecto 7 días
        this.estado = EstadoPrestamo.PENDIENTE_ENTREGA;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public User getUsuario() { return usuario; }
    public AbstractRecurso getRecurso() { return recurso; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public LocalDate getFechaDevolucionPrevista() { return fechaDevolucionPrevista; }
    public EstadoPrestamo getEstado() { return estado; }
    public void setEstado(EstadoPrestamo estado) { this.estado = estado; }
}