package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.dao.ControlCalidadRepository;
import es.deusto.spq.deustocrai.dao.MaterialRepository;
import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.entity.Aviso;
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
    private final AvisoService avisoService;

    public ControlCalidadService(
            ControlCalidadRepository controlCalidadRepository,
            MaterialRepository materialRepository,
            PrestamoRepository prestamoRepository,
            AvisoService avisoService
    ) {
        this.controlCalidadRepository = controlCalidadRepository;
        this.materialRepository = materialRepository;
        this.prestamoRepository = prestamoRepository;
        this.avisoService = avisoService;
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

            avisoService.crearAviso(
                    bibliotecario,
                    Aviso.TipoAviso.RECORDATORIO_DEVOLUCION,
                    "Incidencia en control de calidad",
                    "El material '" + material.getTitulo() + "' ha sido marcado como " + estado +
                            ". Observaciones: " + observaciones
            );
        }

        materialRepository.save(material);

        return controlCalidadRepository.save(control);
    }
}