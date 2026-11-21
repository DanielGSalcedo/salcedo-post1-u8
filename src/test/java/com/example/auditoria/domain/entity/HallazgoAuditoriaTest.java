package com.example.auditoria.domain.entity;

import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.domain.valueobject.PlanRemediacion;
import com.example.auditoria.domain.valueobject.Severidad;
import com.example.auditoria.domain.valueobject.TransicionInvalidaException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HallazgoAuditoriaTest {

    private HallazgoAuditoria hallazgoAbierto() {
        return new HallazgoAuditoria(
                HallazgoId.nuevo(),
                "Contrasenas por defecto",
                "El servidor QA usa credenciales del fabricante",
                "Infraestructura",
                Severidad.ALTA,
                LocalDate.of(2026, 8, 1));
    }

    private PlanRemediacion plan() {
        return new PlanRemediacion("Equipo de Infraestructura", LocalDate.of(2026, 8, 20), "Rotar credenciales");
    }

    @Test
    void unHallazgoNuevoNaceAbierto() {
        assertThat(hallazgoAbierto().getEstado()).isEqualTo(EstadoHallazgo.ABIERTO);
    }

    @Test
    void recorreElCicloDeVidaCompletoYDevuelveElEstadoAnterior() {
        HallazgoAuditoria hallazgo = hallazgoAbierto();

        assertThat(hallazgo.iniciarRemediacion(plan())).isEqualTo(EstadoHallazgo.ABIERTO);
        assertThat(hallazgo.getEstado()).isEqualTo(EstadoHallazgo.EN_REMEDIACION);

        assertThat(hallazgo.cerrar()).isEqualTo(EstadoHallazgo.EN_REMEDIACION);
        assertThat(hallazgo.getEstado()).isEqualTo(EstadoHallazgo.CERRADO);
        assertThat(hallazgo.getFechaCierre()).isEqualTo(LocalDate.now());

        assertThat(hallazgo.reabrir()).isEqualTo(EstadoHallazgo.CERRADO);
        assertThat(hallazgo.getEstado()).isEqualTo(EstadoHallazgo.REABIERTO);
        assertThat(hallazgo.getFechaCierre()).isNull();

        assertThat(hallazgo.iniciarRemediacion(plan())).isEqualTo(EstadoHallazgo.REABIERTO);
        assertThat(hallazgo.getEstado()).isEqualTo(EstadoHallazgo.EN_REMEDIACION);
    }

    @Test
    void noSePuedeCerrarSinPlanDeRemediacion() {
        HallazgoAuditoria hallazgo = hallazgoAbierto();

        assertThatThrownBy(hallazgo::cerrar)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sin plan");
    }

    @Test
    void noSePuedeCerrarUnHallazgoQueNuncaEntroEnRemediacion() {
        HallazgoAuditoria hallazgo = hallazgoAbierto();
        hallazgo.iniciarRemediacion(plan());

        assertThatThrownBy(() -> hallazgo.iniciarRemediacion(plan()))
                .isInstanceOf(TransicionInvalidaException.class);
    }

    @Test
    void noSePuedeReabrirUnHallazgoAbierto() {
        assertThatThrownBy(() -> hallazgoAbierto().reabrir())
                .isInstanceOf(TransicionInvalidaException.class)
                .hasMessageContaining("ABIERTO")
                .hasMessageContaining("REABIERTO");
    }

    @Test
    void noSePuedeIniciarRemediacionDosVecesSeguidas() {
        HallazgoAuditoria hallazgo = hallazgoAbierto();
        hallazgo.iniciarRemediacion(plan());

        assertThatThrownBy(() -> hallazgo.iniciarRemediacion(plan()))
                .isInstanceOf(TransicionInvalidaException.class);
    }

    @Test
    void reconstituyeUnHallazgoCerradoSinPerderSuFechaDeCierre() {
        LocalDate fechaCierre = LocalDate.of(2026, 8, 15);

        HallazgoAuditoria hallazgo = HallazgoAuditoria.reconstituir(
                HallazgoId.nuevo(), "Titulo", "Descripcion", "Infraestructura", Severidad.ALTA,
                LocalDate.of(2026, 8, 1), EstadoHallazgo.CERRADO, plan(), fechaCierre);

        assertThat(hallazgo.getEstado()).isEqualTo(EstadoHallazgo.CERRADO);
        assertThat(hallazgo.getFechaCierre()).isEqualTo(fechaCierre);
    }

    @Test
    void reconstituirRechazaEstadosInconsistentes() {
        assertThatThrownBy(() -> HallazgoAuditoria.reconstituir(
                HallazgoId.nuevo(), "Titulo", "Descripcion", "Infraestructura", Severidad.ALTA,
                LocalDate.of(2026, 8, 1), EstadoHallazgo.CERRADO, plan(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha de cierre");

        assertThatThrownBy(() -> HallazgoAuditoria.reconstituir(
                HallazgoId.nuevo(), "Titulo", "Descripcion", "Infraestructura", Severidad.ALTA,
                LocalDate.of(2026, 8, 1), EstadoHallazgo.EN_REMEDIACION, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("plan de remediacion");
    }
}
