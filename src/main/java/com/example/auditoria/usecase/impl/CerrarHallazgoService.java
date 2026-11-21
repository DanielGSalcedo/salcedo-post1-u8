package com.example.auditoria.usecase.impl;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.usecase.CerrarHallazgoUseCase;
import com.example.auditoria.usecase.exception.HallazgoNotFoundException;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;
import com.example.auditoria.usecase.port.HistorialAuditoriaPort;

public class CerrarHallazgoService implements CerrarHallazgoUseCase {

    private final HallazgoRepositoryPort repositorio;
    private final HistorialAuditoriaPort historial;

    public CerrarHallazgoService(HallazgoRepositoryPort repositorio, HistorialAuditoriaPort historial) {
        this.repositorio = repositorio;
        this.historial = historial;
    }

    @Override
    public void ejecutar(HallazgoId id) {
        HallazgoAuditoria hallazgo = repositorio.buscarPorId(id)
                .orElseThrow(() -> new HallazgoNotFoundException(id));
        EstadoHallazgo anterior = hallazgo.cerrar();
        repositorio.guardar(hallazgo);
        historial.registrar(id, anterior, EstadoHallazgo.CERRADO, "Cierre de remediacion");
    }
}
