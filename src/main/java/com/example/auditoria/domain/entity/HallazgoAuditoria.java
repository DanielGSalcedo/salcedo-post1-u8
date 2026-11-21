package com.example.auditoria.domain.entity;

import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.domain.valueobject.PlanRemediacion;
import com.example.auditoria.domain.valueobject.Severidad;
import com.example.auditoria.domain.valueobject.TransicionInvalidaException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Aggregate Root del hallazgo de auditoria. El estado solo puede cambiar a
 * traves de sus metodos de dominio, que aplican la maquina de estados.
 */
public class HallazgoAuditoria {

    private final HallazgoId id;
    private final String titulo;
    private final String descripcion;
    private final String areaResponsable;
    private final Severidad severidad;
    private final LocalDate fechaDeteccion;
    private EstadoHallazgo estado;
    private PlanRemediacion planRemediacion;
    private LocalDate fechaCierre;

    public HallazgoAuditoria(HallazgoId id, String titulo, String descripcion,
                             String areaResponsable, Severidad severidad, LocalDate fechaDeteccion) {
        this.id = Objects.requireNonNull(id, "El id es obligatorio");
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El titulo es obligatorio");
        }
        if (areaResponsable == null || areaResponsable.isBlank()) {
            throw new IllegalArgumentException("El area responsable es obligatoria");
        }
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.areaResponsable = areaResponsable;
        this.severidad = Objects.requireNonNull(severidad, "La severidad es obligatoria");
        this.fechaDeteccion = Objects.requireNonNull(fechaDeteccion, "La fecha de deteccion es obligatoria");
        this.estado = EstadoHallazgo.ABIERTO;
    }

    /**
     * Reconstituye el agregado desde persistencia sin ejecutar transiciones ni
     * generar eventos/bitacora. Valida que el estado y sus datos sean coherentes.
     */
    public static HallazgoAuditoria reconstituir(HallazgoId id, String titulo, String descripcion,
                                                 String areaResponsable, Severidad severidad,
                                                 LocalDate fechaDeteccion, EstadoHallazgo estado,
                                                 PlanRemediacion planRemediacion, LocalDate fechaCierre) {
        HallazgoAuditoria hallazgo = new HallazgoAuditoria(
                id, titulo, descripcion, areaResponsable, severidad, fechaDeteccion);
        Objects.requireNonNull(estado, "El estado es obligatorio");

        if (estado == EstadoHallazgo.ABIERTO && planRemediacion != null) {
            throw new IllegalArgumentException("Un hallazgo ABIERTO no puede tener plan de remediacion");
        }
        if (estado != EstadoHallazgo.ABIERTO && planRemediacion == null) {
            throw new IllegalArgumentException("El estado " + estado + " requiere un plan de remediacion");
        }
        if (estado == EstadoHallazgo.CERRADO && fechaCierre == null) {
            throw new IllegalArgumentException("Un hallazgo CERRADO requiere fecha de cierre");
        }
        if (estado != EstadoHallazgo.CERRADO && fechaCierre != null) {
            throw new IllegalArgumentException("Solo un hallazgo CERRADO puede tener fecha de cierre");
        }

        hallazgo.estado = estado;
        hallazgo.planRemediacion = planRemediacion;
        hallazgo.fechaCierre = fechaCierre;
        return hallazgo;
    }

    public EstadoHallazgo iniciarRemediacion(PlanRemediacion plan) {
        Objects.requireNonNull(plan, "El plan de remediacion es obligatorio");
        EstadoHallazgo anterior = transicionar(EstadoHallazgo.EN_REMEDIACION);
        this.planRemediacion = plan;
        return anterior;
    }

    public EstadoHallazgo cerrar() {
        if (planRemediacion == null) {
            throw new IllegalStateException("No se puede cerrar un hallazgo sin plan de remediacion");
        }
        EstadoHallazgo anterior = transicionar(EstadoHallazgo.CERRADO);
        this.fechaCierre = LocalDate.now();
        return anterior;
    }

    public EstadoHallazgo reabrir() {
        EstadoHallazgo anterior = transicionar(EstadoHallazgo.REABIERTO);
        this.fechaCierre = null;
        return anterior;
    }

    private EstadoHallazgo transicionar(EstadoHallazgo destino) {
        if (!estado.puedeTransicionarA(destino)) {
            throw new TransicionInvalidaException(estado, destino);
        }
        EstadoHallazgo anterior = this.estado;
        this.estado = destino;
        return anterior;
    }

    public HallazgoId getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getAreaResponsable() {
        return areaResponsable;
    }

    public Severidad getSeveridad() {
        return severidad;
    }

    public LocalDate getFechaDeteccion() {
        return fechaDeteccion;
    }

    public EstadoHallazgo getEstado() {
        return estado;
    }

    public PlanRemediacion getPlanRemediacion() {
        return planRemediacion;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }
}
