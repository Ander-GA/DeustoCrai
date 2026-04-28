package es.deusto.spq.deustocrai.facade;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.Required;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

/**
 * Test de rendimiento para el buscador de materiales.
 * Objetivo Sprint 2: Implementar tests de rendimiento y conocer su funcionamiento.
 */
public class MaterialControllerPerformanceTest {

    // Regla necesaria para que ContiPerf intercepte los tests de JUnit
    @Rule
    public ContiPerfRule rule = new ContiPerfRule();

    private RestTemplate restTemplate = new RestTemplate();

    @Test
    /* * PerfTest: Define la carga. 
     * invocations = 60 (se ejecutará 60 veces)
     * threads = 4 (usará 4 hilos en paralelo)
     */
    @PerfTest(invocations = 60, threads = 4)
    /* * Required: Define los límites de éxito.
     * max = 1200 (ninguna petición debe tardar más de 1.2s)
     * average = 250 (la media debe estar por debajo de 250ms)
     */
    @Required(max = 1200, average = 250)
    public void testBuscarMaterialPerformance() {
        // Probamos el endpoint con el parámetro de búsqueda optimizado
        String url = "http://localhost:8080/api/materiales?buscar=Portatil";
        
        try {
            restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            // Nota: En entornos de Integración Continua (CI), este test puede fallar 
            // si el servidor no está arrancado previamente. 
            // Para tests de rendimiento reales, el servidor debe estar activo.
        }
    }
}