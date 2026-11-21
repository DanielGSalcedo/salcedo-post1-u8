package com.example.auditoria.adapter.in.web;

import com.example.auditoria.adapter.in.web.dto.ErrorResponse;
import com.example.auditoria.domain.valueobject.TransicionInvalidaException;
import com.example.auditoria.usecase.exception.HallazgoNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HallazgoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse hallazgoNoEncontrado(HallazgoNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(TransicionInvalidaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse transicionInvalida(TransicionInvalidaException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse reglaDeNegocio(RuntimeException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validacion(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return new ErrorResponse(detalle.isBlank() ? "Solicitud invalida" : detalle);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse jsonInvalido(HttpMessageNotReadableException ex) {
        return new ErrorResponse("El cuerpo de la solicitud no es un JSON valido o contiene valores no permitidos");
    }
}
