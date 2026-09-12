package com.hackathon.databeats.churninsight.infra.exception;

import com.hackathon.databeats.churninsight.domain.exception.PredictionException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Erros de Validação (@Valid, @NotNull, etc) -> 400 Bad Request
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Erro de validação: {}", message);

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    // 2. Erros de Negócio da Predição (PredictionException) -> 422 Unprocessable Entity
    @ExceptionHandler(PredictionException.class)
    public ResponseEntity<ApiErrorResponse> handlePredictionException(
            PredictionException ex,
            HttpServletRequest request
    ) {
        log.warn("Erro de negócio na predição: {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    // 3. Erros Técnicos do Modelo (ModelInferenceException) -> 500 Internal Server Error
    @ExceptionHandler(ModelInferenceException.class)
    public ResponseEntity<ApiErrorResponse> handleModelInference(
            ModelInferenceException ex,
            HttpServletRequest request
    ) {
        log.error("Erro técnico no modelo ONNX: {}", ex.getMessage(), ex);

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Falha na inferência do modelo: " + ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // 4. Argumentos Ilegais (Ex: Arquivo inválido no Batch) -> 400 Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.warn("Argumento inválido: {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    // 5. JSON Malformado (Ex: Enviar String num campo Integer) -> 400 Bad Request
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleJsonError(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.warn("JSON inválido recebido: {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Formato de JSON inválido ou campos mal formatados.",
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    // 6. Fallback Genérico (Qualquer outra coisa) -> 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        // Loga o erro completo no console para debug
        log.error("Erro interno não tratado: ", ex);

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocorreu um erro interno inesperado. Consulte os logs.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}