package es.deusto.spq.deustocrai.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.deusto.spq.deustocrai.entity.Libro;


@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
	List<Libro> findByTituloContainingIgnoreCase(String titulo);
}