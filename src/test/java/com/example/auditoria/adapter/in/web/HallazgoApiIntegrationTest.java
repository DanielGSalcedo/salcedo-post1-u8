package com.example.auditoria.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HallazgoApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String registrarHallazgo(String titulo, String severidad, LocalDate fechaDeteccion)
            throws Exception {
        String body = """
                {
                  "titulo": "%s",
                  "descripcion": "Descripcion de prueba",
                  "areaResponsable": "Infraestructura",
                  "severidad": "%s",
                  "fechaDeteccion": "%s"
                }
                """.formatted(titulo, severidad, fechaDeteccion);

        MvcResult result = mockMvc.perform(post("/api/hallazgos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hallazgoId").isNotEmpty())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("hallazgoId").asText();
    }

    @Test
    void flujoCompletoDeLaParte1YParte2() throws Exception {
        String id = registrarHallazgo("Contrasenas por defecto", "ALTA", LocalDate.now().minusDays(10));

        mockMvc.perform(patch("/api/hallazgos/{id}/cerrar", id))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/hallazgos/{id}/iniciar-remediacion", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"responsable":"Equipo de Infraestructura",
                                 "fechaLimite":"2026-08-20",
                                 "notas":"Rotar credenciales"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_REMEDIACION"));

        mockMvc.perform(patch("/api/hallazgos/{id}/cerrar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERRADO"));

        mockMvc.perform(patch("/api/hallazgos/{id}/reabrir", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"motivo":"El hallazgo reaparecio en la auditoria externa"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REABIERTO"));

        mockMvc.perform(get("/api/hallazgos/{id}/historial", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].estadoAnterior").value("ABIERTO"))
                .andExpect(jsonPath("$[0].estadoNuevo").value("EN_REMEDIACION"))
                .andExpect(jsonPath("$[1].estadoNuevo").value("CERRADO"))
                .andExpect(jsonPath("$[2].estadoNuevo").value("REABIERTO"))
                .andExpect(jsonPath("$[2].motivo").value("El hallazgo reaparecio en la auditoria externa"));

        mockMvc.perform(get("/api/hallazgos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REABIERTO"))
                .andExpect(jsonPath("$.planRemediacion.responsable").value("Equipo de Infraestructura"));

        String cerrado = registrarHallazgo("Licencias vencidas", "MEDIA", LocalDate.now().minusDays(4));
        mockMvc.perform(patch("/api/hallazgos/{id}/iniciar-remediacion", cerrado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"responsable":"Equipo de Infraestructura","fechaLimite":"2026-12-31"}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/hallazgos/{id}/cerrar", cerrado))
                .andExpect(status().isOk());

        MvcResult dashboard = mockMvc.perform(get("/api/hallazgos/dashboard"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(dashboard.getResponse().getContentAsString());

        assertThat(json.get("porSeveridad").size()).isGreaterThanOrEqualTo(2);
        assertThat(json.get("porEstado").size()).isGreaterThanOrEqualTo(2);

        JsonNode promedios = json.get("promedioDiasCierrePorArea");
        assertThat(promedios.size()).isEqualTo(1);
        assertThat(promedios.get(0).get("categoria").asText()).isEqualTo("Infraestructura");
        assertThat(promedios.get(0).get("promedioDias").asDouble()).isEqualTo(4.0);

        mockMvc.perform(get("/api/hallazgos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/hallazgos/{id}", "no-es-un-uuid"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/hallazgos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"","areaResponsable":"","severidad":"ALTA","fechaDeteccion":"2026-08-01"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
