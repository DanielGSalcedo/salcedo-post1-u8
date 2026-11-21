package com.example.auditoria.usecase.impl;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.domain.valueobject.PlanRemediacion;
import com.example.auditoria.usecase.IniciarRemediacionUseCase;
import com.example.auditoria.usecase.exception.HallazgoNotFoundException;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;
import com.example.auditoria.usecase.port.HistorialAuditoriaPort;

import java.time.LocalDate;

public class IniciarRemediacionService implements IniciarRemediacionUseCase {

    private final HallazgoRepositoryPort repositorio;
    private final HistorialAuditoriaPort historial;

    public IniciarRemediacionService(HallazgoRepositoryPort repositorio, HistorialAuditoriaPort historial) {
        this.repositorio = repositorio;
        this.historial = historial;
    }

    @Override
    public void ejecutar(HallazgoId id, String responsable, LocalDate fechaLimite, String notas) {
        HallazgoAuditoria hallazgo = repositorio.buscarPorId(id)
                .orElseThrow(() -> new HallazgoNotFoundException(id));
        EstadoHallazgo anterior = hallazgo.iniciarRemediacion(
                new PlanRemediacion(responsable, fechaLimite, notas));
        repositorio.guardar(hallazgo);
        historial.registrar(id, anterior, EstadoHallazgo.EN_REMEDIACION, "Inicio de remediacion");
    }
}
