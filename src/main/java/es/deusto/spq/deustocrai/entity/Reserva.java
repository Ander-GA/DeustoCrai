package es.deusto.spq.deustocrai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User usuario;

    @ManyToOne
    @JoinColumn(name = "aula_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Aula aula;

    @Column(nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(nullable = false)
    private LocalDateTime fechaHoraFin;
    
    @Column(nullable = false)
    private boolean devuelta = false;

    // NUEVO: Para saber si ya le hemos avisado de que su reserva empieza pronto
    @Column(nullable = false)
    private boolean avisoEnviado = false; 

    public Reserva() {}

    public Reserva(User usuario, Aula aula, LocalDateTime inicio, LocalDateTime fin) {
        this.usuario = usuario;
        this.aula = aula;
        this.fechaHoraInicio = inicio;
        this.fechaHoraFin = fin;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public Aula getAula() { return aula; }
    public void setAula(Aula aula) { this.aula = aula; }
    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }
    public LocalDateTime getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }
    
    public boolean isDevuelta() { return devuelta; }
    public void setDevuelta(boolean devuelta) { this.devuelta = devuelta; }

    public boolean isAvisoEnviado() { return avisoEnviado; }
    public void setAvisoEnviado(boolean avisoEnviado) { this.avisoEnviado = avisoEnviado; }
}