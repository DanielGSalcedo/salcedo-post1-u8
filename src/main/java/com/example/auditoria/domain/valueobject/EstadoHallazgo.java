package com.example.auditoria.domain.valueobject;

public enum EstadoHallazgo {
    ABIERTO, EN_REMEDIACION, CERRADO, REABIERTO;

    public boolean puedeTransicionarA(EstadoHallazgo destino) {
        if (destino == null) {
            return false;
        }
        return switch (this) {
            case ABIERTO -> destino == EN_REMEDIACION;
            case EN_REMEDIACION -> destino == CERRADO;
            case CERRADO -> destino == REABIERTO;
            case REABIERTO -> destino == EN_REMEDIACION;
        };
    }
}
