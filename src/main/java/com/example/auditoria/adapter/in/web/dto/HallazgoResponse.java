package com.example.auditoria.adapter.in.web.dto;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.Severidad;

import java.time.LocalDate;

public record HallazgoResponse(
        String id,
        String titulo,
        String descripcion,
        String areaResponsable,
        Severidad severidad,
        EstadoHallazgo estado,
        LocalDate fechaDeteccion,
        LocalDate fechaCierre,
        PlanRemediacionResponse planRemediacion) {

    public static HallazgoResponse from(HallazgoAuditoria hallazgo) {
        PlanRemediacionResponse plan = hallazgo.getPlanRemediacion() == null
                ? null
                : new PlanRemediacionResponse(
                        hallazgo.getPlanRemediacion().responsable(),
                        hallazgo.getPlanRemediacion().fechaLimite(),
                        hallazgo.getPlanRemediacion().notas());
        return new HallazgoResponse(
                hallazgo.getId().toString(),
                hallazgo.getTitulo(),
                hallazgo.getDescripcion(),
                hallazgo.getAreaResponsable(),
                hallazgo.getSeveridad(),
                hallazgo.getEstado(),
                hallazgo.getFechaDeteccion(),
                hallazgo.getFechaCierre(),
                plan);
    }

    public record PlanRemediacionResponse(String responsable, LocalDate fechaLimite, String notas) {
    }
}
