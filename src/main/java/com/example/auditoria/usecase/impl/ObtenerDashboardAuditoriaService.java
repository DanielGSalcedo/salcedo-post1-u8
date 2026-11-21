package com.example.auditoria.usecase.impl;

import com.example.auditoria.usecase.ObtenerDashboardAuditoriaUseCase;
import com.example.auditoria.usecase.port.DashboardAuditoriaView;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;

public class ObtenerDashboardAuditoriaService implements ObtenerDashboardAuditoriaUseCase {

    private final HallazgoRepositoryPort repositorio;

    public ObtenerDashboardAuditoriaService(HallazgoRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public DashboardAuditoriaView ejecutar() {
        return new DashboardAuditoriaView(
                repositorio.contarPorSeveridad(),
                repositorio.contarPorEstado(),
                repositorio.promedioDiasCierrePorArea());
    }
}
