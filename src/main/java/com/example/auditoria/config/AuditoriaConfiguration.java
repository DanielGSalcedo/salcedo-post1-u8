package com.example.auditoria.config;

import com.example.auditoria.usecase.CerrarHallazgoUseCase;
import com.example.auditoria.usecase.ConsultarHallazgoUseCase;
import com.example.auditoria.usecase.ConsultarHistorialUseCase;
import com.example.auditoria.usecase.IniciarRemediacionUseCase;
import com.example.auditoria.usecase.ObtenerDashboardAuditoriaUseCase;
import com.example.auditoria.usecase.ReabrirHallazgoUseCase;
import com.example.auditoria.usecase.RegistrarHallazgoUseCase;
import com.example.auditoria.usecase.impl.CerrarHallazgoService;
import com.example.auditoria.usecase.impl.ConsultarHallazgoService;
import com.example.auditoria.usecase.impl.ConsultarHistorialService;
import com.example.auditoria.usecase.impl.IniciarRemediacionService;
import com.example.auditoria.usecase.impl.ObtenerDashboardAuditoriaService;
import com.example.auditoria.usecase.impl.ReabrirHallazgoService;
import com.example.auditoria.usecase.impl.RegistrarHallazgoService;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;
import com.example.auditoria.usecase.port.HistorialAuditoriaPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditoriaConfiguration {

    @Bean
    public RegistrarHallazgoUseCase registrarHallazgoUseCase(HallazgoRepositoryPort repositorio) {
        return new RegistrarHallazgoService(repositorio);
    }

    @Bean
    public IniciarRemediacionUseCase iniciarRemediacionUseCase(HallazgoRepositoryPort repositorio,
                                                               HistorialAuditoriaPort historial) {
        return new IniciarRemediacionService(repositorio, historial);
    }

    @Bean
    public CerrarHallazgoUseCase cerrarHallazgoUseCase(HallazgoRepositoryPort repositorio,
                                                       HistorialAuditoriaPort historial) {
        return new CerrarHallazgoService(repositorio, historial);
    }

    @Bean
    public ReabrirHallazgoUseCase reabrirHallazgoUseCase(HallazgoRepositoryPort repositorio,
                                                         HistorialAuditoriaPort historial) {
        return new ReabrirHallazgoService(repositorio, historial);
    }

    @Bean
    public ConsultarHallazgoUseCase consultarHallazgoUseCase(HallazgoRepositoryPort repositorio) {
        return new ConsultarHallazgoService(repositorio);
    }

    @Bean
    public ConsultarHistorialUseCase consultarHistorialUseCase(HistorialAuditoriaPort historial) {
        return new ConsultarHistorialService(historial);
    }

    @Bean
    public ObtenerDashboardAuditoriaUseCase obtenerDashboardAuditoriaUseCase(
            HallazgoRepositoryPort repositorio) {
        return new ObtenerDashboardAuditoriaService(repositorio);
    }
}
