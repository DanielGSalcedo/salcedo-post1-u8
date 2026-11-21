package com.example.auditoria.adapter.in.web;

import com.example.auditoria.adapter.in.web.dto.HallazgoResponse;
import com.example.auditoria.adapter.in.web.dto.IniciarRemediacionRequest;
import com.example.auditoria.adapter.in.web.dto.ReabrirRequest;
import com.example.auditoria.adapter.in.web.dto.RegistrarHallazgoRequest;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.usecase.CerrarHallazgoUseCase;
import com.example.auditoria.usecase.ConsultarHallazgoUseCase;
import com.example.auditoria.usecase.ConsultarHistorialUseCase;
import com.example.auditoria.usecase.IniciarRemediacionUseCase;
import com.example.auditoria.usecase.ObtenerDashboardAuditoriaUseCase;
import com.example.auditoria.usecase.ReabrirHallazgoUseCase;
import com.example.auditoria.usecase.RegistrarHallazgoUseCase;
import com.example.auditoria.usecase.port.CambioEstadoView;
import com.example.auditoria.usecase.port.DashboardAuditoriaView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hallazgos")
public class HallazgoController {

    private final RegistrarHallazgoUseCase registrarUseCase;
    private final IniciarRemediacionUseCase iniciarRemediacionUseCase;
    private final CerrarHallazgoUseCase cerrarUseCase;
    private final ReabrirHallazgoUseCase reabrirUseCase;
    private final ConsultarHallazgoUseCase consultarUseCase;
    private final ConsultarHistorialUseCase consultarHistorialUseCase;
    private final ObtenerDashboardAuditoriaUseCase dashboardUseCase;

    public HallazgoController(RegistrarHallazgoUseCase registrarUseCase,
                              IniciarRemediacionUseCase iniciarRemediacionUseCase,
                              CerrarHallazgoUseCase cerrarUseCase,
                              ReabrirHallazgoUseCase reabrirUseCase,
                              ConsultarHallazgoUseCase consultarUseCase,
                              ConsultarHistorialUseCase consultarHistorialUseCase,
                              ObtenerDashboardAuditoriaUseCase dashboardUseCase) {
        this.registrarUseCase = registrarUseCase;
        this.iniciarRemediacionUseCase = iniciarRemediacionUseCase;
        this.cerrarUseCase = cerrarUseCase;
        this.reabrirUseCase = reabrirUseCase;
        this.consultarUseCase = consultarUseCase;
        this.consultarHistorialUseCase = consultarHistorialUseCase;
        this.dashboardUseCase = dashboardUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> registrar(@Valid @RequestBody RegistrarHallazgoRequest request) {
        HallazgoId id = registrarUseCase.ejecutar(
                request.titulo(), request.descripcion(), request.areaResponsable(),
                request.severidad(), request.fechaDeteccion());
        return Map.of("hallazgoId", id.toString());
    }

    @PatchMapping("/{id}/iniciar-remediacion")
    @Transactional
    public Map<String, String> iniciarRemediacion(@PathVariable String id,
                                                  @Valid @RequestBody IniciarRemediacionRequest request) {
        iniciarRemediacionUseCase.ejecutar(
                HallazgoId.de(id), request.responsable(), request.fechaLimite(), request.notas());
        return Map.of("estado", "EN_REMEDIACION");
    }

    @PatchMapping("/{id}/cerrar")
    @Transactional
    public Map<String, String> cerrar(@PathVariable String id) {
        cerrarUseCase.ejecutar(HallazgoId.de(id));
        return Map.of("estado", "CERRADO");
    }

    @PatchMapping("/{id}/reabrir")
    @Transactional
    public Map<String, String> reabrir(@PathVariable String id,
                                       @Valid @RequestBody ReabrirRequest request) {
        reabrirUseCase.ejecutar(HallazgoId.de(id), request.motivo());
        return Map.of("estado", "REABIERTO");
    }

    @GetMapping("/dashboard")
    public DashboardAuditoriaView dashboard() {
        return dashboardUseCase.ejecutar();
    }

    @GetMapping("/{id}")
    public HallazgoResponse buscar(@PathVariable String id) {
        return HallazgoResponse.from(consultarUseCase.buscarPorId(HallazgoId.de(id)));
    }

    @GetMapping
    public List<HallazgoResponse> listar() {
        return consultarUseCase.listarTodos().stream()
                .map(HallazgoResponse::from)
                .toList();
    }

    @GetMapping("/{id}/historial")
    public List<CambioEstadoView> historial(@PathVariable String id) {
        return consultarHistorialUseCase.ejecutar(HallazgoId.de(id));
    }
}
