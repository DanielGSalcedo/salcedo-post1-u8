package com.example.auditoria.adapter.in.web.dto;

import com.example.auditoria.domain.valueobject.Severidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegistrarHallazgoRequest(
        @NotBlank(message = "El titulo es obligatorio")
        @Size(max = 255, message = "El titulo no puede superar 255 caracteres")
        String titulo,

        @Size(max = 2000, message = "La descripcion no puede superar 2000 caracteres")
        String descripcion,

        @NotBlank(message = "El area responsable es obligatoria")
        String areaResponsable,

        @NotNull(message = "La severidad es obligatoria")
        Severidad severidad,

        @NotNull(message = "La fecha de deteccion es obligatoria")
        LocalDate fechaDeteccion) {
}
