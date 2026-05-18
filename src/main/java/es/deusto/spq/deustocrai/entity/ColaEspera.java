package es.deusto.spq.deustocrai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cola_espera")
public class ColaEspera {

    public enum EstadoCola {
        ACTIVA,
        ASIGNADA,
        CANCELADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private User usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recurso_id")
    private AbstractRecurso recurso;

    @Column(nullable = false)
    private LocalDateTime fechaEntrada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCola estado;

    public ColaEspera() {
    }

    public ColaEspera(User usuario, AbstractRecurso recurso) {
        this.usuario = usuario;
        this.recurso = recurso;
        this.fechaEntrada = LocalDateTime.now();
        this.estado = EstadoCola.ACTIVA;
    }

    public Long getId() {
        return id;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public AbstractRecurso getRecurso() {
        return recurso;
    }

    public void setRecurso(AbstractRecurso recurso) {
        this.recurso = recurso;
    }

    public LocalDateTime getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(LocalDateTime fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public EstadoCola getEstado() {
        return estado;
    }

    public void setEstado(EstadoCola estado) {
        this.estado = estado;
    }
}