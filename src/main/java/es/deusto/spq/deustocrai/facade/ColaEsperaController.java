package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.entity.ColaEspera;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.service.ColaEsperaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import es.deusto.spq.deustocrai.dao.ColaEsperaRepository;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/cola-espera")
public class ColaEsperaController {

    private final ColaEsperaService colaEsperaService;
    private final AuthService authService;

    @Autowired
    private ColaEsperaRepository colaEsperaRepository;

    public ColaEsperaController(ColaEsperaService colaEsperaService, AuthService authService) {
        this.colaEsperaService = colaEsperaService;
        this.authService = authService;
    }

    

    @Operation(
            summary = "Apuntarse a la cola de espera",
            description = "Permite a un usuario apuntarse a la cola de espera de un libro o material que no está disponible."
    )
    @PostMapping("/recurso/{recursoId}")
    public ResponseEntity<?> apuntarseACola(
            @Parameter(in = ParameterIn.HEADER, name = "Token-Auth", required = true)
            @RequestHeader(value = "Token-Auth", required = false) String tokenSwagger,
            @RequestHeader(value = "Authorization", required = false) String tokenReal,
            @PathVariable Long recursoId
    ) {
        String token = (tokenReal != null && !tokenReal.isEmpty()) ? tokenReal : tokenSwagger;

        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No se ha recibido el token.");
        }

        User usuario = authService.getEmpleadoByToken(token);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o sesión caducada.");
        }

        try {
            ColaEspera cola = colaEsperaService.apuntarseACola(usuario, recursoId);
            return ResponseEntity.status(HttpStatus.CREATED).body(cola);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @Operation(
            summary = "Consultar cola activa de un recurso",
            description = "Devuelve la cola de espera activa de un libro o material."
    )
    @GetMapping("/recurso/{recursoId}")
    public ResponseEntity<?> obtenerCola(@PathVariable Long recursoId) {
        return ResponseEntity.ok(colaEsperaService.obtenerColaActiva(recursoId));
    }

    @DeleteMapping("/{colaId}")
    public ResponseEntity<?> salirDeLaCola(@PathVariable Long colaId) {

        Optional<ColaEspera> colaOpt = colaEsperaRepository.findById(colaId);

        if (colaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        colaEsperaRepository.deleteById(colaId);

        return ResponseEntity.ok("Has salido de la cola correctamente");
    }

    @GetMapping("/recurso/{recursoId}/posicion")
    public ResponseEntity<?> obtenerPosicion(
            @RequestHeader(value = "Token-Auth", required = false) String tokenSwagger,
            @RequestHeader(value = "Authorization", required = false) String tokenReal,
            @PathVariable Long recursoId
    ) {

        String token = (tokenReal != null && !tokenReal.isEmpty())
                ? tokenReal
                : tokenSwagger;

        User usuario = authService.getEmpleadoByToken(token);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token inválido");
        }

        int posicion = colaEsperaService.obtenerPosicion(
                recursoId,
                usuario.getId()
        );

        return ResponseEntity.ok(posicion);
    }
}