package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.entity.Valoracion;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.service.ValoracionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/valoraciones")
public class ValoracionController {

    @Autowired
    private ValoracionService valoracionService;

    @Autowired
    private AuthService authService;

    @Operation(summary = "Obtener reseñas de un recurso", description = "Endpoint público para leer los comentarios y estrellas de un material.")
    @GetMapping("/recurso/{recursoId}")
    public ResponseEntity<List<Valoracion>> obtenerResenas(@PathVariable("recursoId") Long recursoId) {
        return ResponseEntity.ok(valoracionService.obtenerResenasDeRecurso(recursoId));
    }

    @Operation(summary = "Dejar una reseña", description = "Valora de 1 a 5 estrellas un material que ya has devuelto.")
    @PostMapping("/recurso/{recursoId}")
    public ResponseEntity<?> publicarResena(
            @Parameter(in = ParameterIn.HEADER, name = "Token-Auth", required = true)
            @RequestHeader(value = "Token-Auth", required = false) String tokenSwagger,
            @RequestHeader(value = "Authorization", required = false) String tokenReal,
            @PathVariable("recursoId") Long recursoId,
            @RequestParam("puntuacion") int puntuacion,
            @RequestParam(value = "comentario", required = false) String comentario) {

        String token = (tokenReal != null && !tokenReal.isEmpty()) ? tokenReal : tokenSwagger;

        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"No se ha recibido el token.\"}");
        }

        User user = authService.getEmpleadoByToken(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"Token inválido.\"}");
        }

        try {
            Valoracion nuevaValoracion = valoracionService.dejarResena(user, recursoId, puntuacion, comentario);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaValoracion);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}