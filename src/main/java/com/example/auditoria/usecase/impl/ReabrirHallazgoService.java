package com.example.auditoria.usecase.impl;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.usecase.ReabrirHallazgoUseCase;
import com.example.auditoria.usecase.exception.HallazgoNotFoundException;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;
import com.example.auditoria.usecase.port.HistorialAuditoriaPort;

public class ReabrirHallazgoService implements ReabrirHallazgoUseCase {

    private final HallazgoRepositoryPort repositorio;
    private final HistorialAuditoriaPort historial;

    public ReabrirHallazgoService(HallazgoRepositoryPort repositorio, HistorialAuditoriaPort historial) {
        this.repositorio = repositorio;
        this.historial = historial;
    }

    @Override
    public void ejecutar(HallazgoId id, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de reapertura es obligatorio");
        }
        HallazgoAuditoria hallazgo = repositorio.buscarPorId(id)
                .orElseThrow(() -> new HallazgoNotFoundException(id));
        EstadoHallazgo anterior = hallazgo.reabrir();
        repositorio.guardar(hallazgo);
        historial.registrar(id, anterior, EstadoHallazgo.REABIERTO, motivo);
    }
}
