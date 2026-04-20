package es.deusto.spq.deustocrai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "libro")
public class Libro extends AbstractRecurso {

    // El ISBN no puede ser nulo y además debe ser único (no puede haber dos libros con el mismo ISBN en el catálogo)
    @Column(nullable = false, unique = true)
    private String isbn;

    // El autor no puede dejarse en blanco
    @Column(nullable = false)
    private String autor;

    public Libro() {}

    public Libro(String titulo, String isbn, String autor) {
        super(titulo); 
        this.isbn = isbn;
        this.autor = autor;
    }

    // Getters y Setters
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
}