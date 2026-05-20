package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.dao.ControlCalidadRepository;
import es.deusto.spq.deustocrai.dao.MaterialRepository;
import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.entity.ControlCalidad;
import es.deusto.spq.deustocrai.entity.ControlCalidad.EstadoControl;
import es.deusto.spq.deustocrai.entity.Material;
import es.deusto.spq.deustocrai.entity.Prestamo;
import es.deusto.spq.deustocrai.entity.User;
import org.springframework.stereotype.Service;

@Service
public class ControlCalidadService {

    private final ControlCalidadRepository controlCalidadRepository;
    private final MaterialRepository materialRepository;
    private final PrestamoRepository prestamoRepository;

    public ControlCalidadService(
            ControlCalidadRepository controlCalidadRepository,
            MaterialRepository materialRepository,
            PrestamoRepository prestamoRepository
    ) {
        this.controlCalidadRepository = controlCalidadRepository;
        this.materialRepository = materialRepository;
        this.prestamoRepository = prestamoRepository;
    }

    public ControlCalidad registrarControl(
            Long materialId,
            Long prestamoId,
            User bibliotecario,
            EstadoControl estado,
            String observaciones
    ) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Material no encontrado"));

        Prestamo prestamo = null;

        if (prestamoId != null) {
            prestamo = prestamoRepository.findById(prestamoId)
                    .orElseThrow(() -> new IllegalArgumentException("Préstamo no encontrado"));
        }

        ControlCalidad control = new ControlCalidad(
                material,
                prestamo,
                bibliotecario,
                estado,
                observaciones
        );

        if (estado == EstadoControl.APTO) {
            material.setDisponible(true);
        } else {
            material.setDisponible(false);
        }

        materialRepository.save(material);

        return controlCalidadRepository.save(control);
    }
}