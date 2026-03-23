package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.dao.MaterialRepository;
import es.deusto.spq.deustocrai.entity.Material;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/materiales")
public class MaterialController {

    @Autowired
    private MaterialRepository materialRepository;

    @PostMapping
    public ResponseEntity<Material> crearMaterial(@RequestBody Material material) {
        Material nuevo = materialRepository.save(material);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @GetMapping
    public List<Material> listar() {
        return materialRepository.findAll();
    }
}