package es.deusto.spq.deustocrai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resultado_partido")
public class ResultadoPartido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Vinculamos el resultado a la reserva original
    @OneToOne
    @JoinColumn(name = "reserva_instalacion_id", nullable = false)
    private ReservaInstalacion reserva;

    @ManyToOne
    @JoinColumn(name = "ganador_id")
    private User ganador; // Quien ganó el partido

    @Column(nullable = false)
    private int puntosLocal; // Puntos/Goles del creador de la reserva

    @Column(nullable = false)
    private int puntosVisitante; // Puntos/Goles del rival

    private String comentarios;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public ResultadoPartido() {}

    public Long getId() { return id; }
    public ReservaInstalacion getReserva() { return reserva; }
    public void setReserva(ReservaInstalacion reserva) { this.reserva = reserva; }
    public User getGanador() { return ganador; }
    public void setGanador(User ganador) { this.ganador = ganador; }
    public int getPuntosLocal() { return puntosLocal; }
    public void setPuntosLocal(int puntosLocal) { this.puntosLocal = puntosLocal; }
    public int getPuntosVisitante() { return puntosVisitante; }
    public void setPuntosVisitante(int puntosVisitante) { this.puntosVisitante = puntosVisitante; }
    public String getComentarios() { return comentarios; }
    public void setComentarios(String comentarios) { this.comentarios = comentarios; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}