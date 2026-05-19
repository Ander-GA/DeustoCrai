package es.deusto.spq.deustocrai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "valoracion")
public class Valoracion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @ManyToOne
    @JoinColumn(name = "recurso_id", nullable = false)
    private AbstractRecurso recurso;

    @Column(nullable = false)
    private int puntuacion; 

    @Column(length = 500)
    private String comentario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    public Valoracion() {}

    public Valoracion(User usuario, AbstractRecurso recurso, int puntuacion, String comentario) {
        this.usuario = usuario;
        this.recurso = recurso;
        this.puntuacion = puntuacion;
        this.comentario = comentario;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public AbstractRecurso getRecurso() { return recurso; }
    public void setRecurso(AbstractRecurso recurso) { this.recurso = recurso; }
    public int getPuntuacion() { return puntuacion; }
    public void setPuntuacion(int puntuacion) { this.puntuacion = puntuacion; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}