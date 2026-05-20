package es.deusto.spq.deustocrai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa el resultado final de un partido disputado en las instalaciones del CRAI.
 * Almacena la información detallada dependiendo de si es un partido de Fútbol Sala o de Pádel.
 */
@Entity
@Table(name = "resultado_partido")
public class ResultadoPartido {

    /**
     * Identificador único autogenerado para cada resultado en la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relación uno a uno con la reserva original de la instalación deportiva.
     * No puede ser nulo porque todo resultado pertenece a una reserva.
     */
    @OneToOne
    @JoinColumn(name = "reserva_instalacion_id", nullable = false)
    private ReservaInstalacion reserva;

    /**
     * Usuario que ha resultado ganador del encuentro.
     * Puede ser nulo en caso de empate (común en fútbol sala).
     */
    @ManyToOne
    @JoinColumn(name = "ganador_id")
    private User ganador;

    /**
     * Tipo de deporte que se ha jugado.
     * Se espera que sea "FUTBOL" o "PADEL".
     */
    @Column(nullable = false, length = 50)
    private String deporte;

    /**
     * Detalle específico de los juegos en caso de que el deporte sea Pádel.
     * Ejemplo de formato: "6-4, 3-6, 7-6".
     */
    @Column(name = "detalle_resultado", length = 255)
    private String detalleResultado;

    /**
     * Puntos obtenidos por el usuario creador de la reserva (Local).
     * Representa Goles en fútbol o Sets ganados en pádel.
     */
    @Column(name = "puntos_local", nullable = false)
    private int puntosLocal;

    /**
     * Puntos obtenidos por el rival (Visitante).
     * Representa Goles en fútbol o Sets ganados en pádel.
     */
    @Column(name = "puntos_visitante", nullable = false)
    private int puntosVisitante;

    /**
     * Comentarios adicionales sobre el partido (lesiones, incidencias, etc.).
     */
    @Column(length = 1000)
    private String comentarios;

    /**
     * Fecha y hora exacta en la que se subió el resultado al sistema.
     */
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    /**
     * Constructor por defecto requerido por JPA/Hibernate.
     */
    public ResultadoPartido() {
        // Hibernate lo necesita vacío
    }

    /**
     * Constructor completo para instanciar un resultado con todos los datos de golpe.
     */
    public ResultadoPartido(ReservaInstalacion reserva, User ganador, String deporte, String detalleResultado, int puntosLocal, int puntosVisitante, String comentarios) {
        this.reserva = reserva;
        this.ganador = ganador;
        this.deporte = deporte;
        this.detalleResultado = detalleResultado;
        this.puntosLocal = puntosLocal;
        this.puntosVisitante = puntosVisitante;
        this.comentarios = comentarios;
        this.fechaRegistro = LocalDateTime.now();
    }

    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReservaInstalacion getReserva() {
        return reserva;
    }

    public void setReserva(ReservaInstalacion reserva) {
        this.reserva = reserva;
    }

    public User getGanador() {
        return ganador;
    }

    public void setGanador(User ganador) {
        this.ganador = ganador;
    }

    public String getDeporte() {
        return deporte;
    }

    public void setDeporte(String deporte) {
        this.deporte = deporte;
    }

    public String getDetalleResultado() {
        return detalleResultado;
    }

    public void setDetalleResultado(String detalleResultado) {
        this.detalleResultado = detalleResultado;
    }

    public int getPuntosLocal() {
        return puntosLocal;
    }

    public void setPuntosLocal(int puntosLocal) {
        this.puntosLocal = puntosLocal;
    }

    public int getPuntosVisitante() {
        return puntosVisitante;
    }

    public void setPuntosVisitante(int puntosVisitante) {
        this.puntosVisitante = puntosVisitante;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    // ==========================================
    // MÉTODOS SOBRESCRITOS DE OBJECT
    // ==========================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResultadoPartido que = (ResultadoPartido) o;
        return puntosLocal == que.puntosLocal && 
               puntosVisitante == que.puntosVisitante && 
               Objects.equals(id, que.id) && 
               Objects.equals(reserva, que.reserva) && 
               Objects.equals(deporte, que.deporte);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reserva, deporte, puntosLocal, puntosVisitante);
    }

    @Override
    public String toString() {
        return "ResultadoPartido{" +
                "id=" + id +
                ", deporte='" + deporte + '\'' +
                ", puntosLocal=" + puntosLocal +
                ", puntosVisitante=" + puntosVisitante +
                ", detalleResultado='" + detalleResultado + '\'' +
                ", fechaRegistro=" + fechaRegistro +
                '}';
    }
}