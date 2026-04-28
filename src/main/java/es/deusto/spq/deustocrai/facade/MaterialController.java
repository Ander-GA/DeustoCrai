package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.dao.MaterialRepository;
import es.deusto.spq.deustocrai.entity.Material;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional; 

@RestController
@RequestMapping("/api/materiales")
public class MaterialController {

    @Autowired
    private MaterialRepository materialRepository;

    // 1. Listar todos los materiales 

    @GetMapping
    public List<Material> listar(@RequestParam(required = false) String buscar) {
        if (buscar != null && !buscar.isEmpty()) {
        return materialRepository.findByTituloContainingIgnoreCase(buscar);
        }
        return materialRepository.findAll();
    }

    // 2. Obtener detalles de uno solo (Lo usa detalle-material.html)
    @GetMapping("/{id}")
    public ResponseEntity<Material> obtenerDetallesMaterial(@PathVariable("id") Long id) {
        Optional<Material> material = materialRepository.findById(id);
        if (material.isPresent()) {
            return ResponseEntity.ok(material.get()); 
        } else {
            return ResponseEntity.notFound().build(); 
        }
    }

    // 3. Crear material (Opcional, para el administrador)
    @PostMapping
    public ResponseEntity<Material> crearMaterial(@RequestBody Material material) {
        Material nuevo = materialRepository.save(material);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }
}