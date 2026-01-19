package com.emrecelen.rateproducer.api.error;

import com.emrecelen.rateproducer.exception.InvalidRateException;
import com.emrecelen.rateproducer.exception.PairNotActiveException;
import com.emrecelen.rateproducer.monitoring.metrics.RateProducerMetrics;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final RateProducerMetrics rateProducerMetrics;

    public GlobalExceptionHandler(RateProducerMetrics rateProducerMetrics) {
        this.rateProducerMetrics = rateProducerMetrics;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        rateProducerMetrics.rejected();
        List<ApiErrorResponse.FieldErrorItem> items = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toItem)
                .toList();

        ApiErrorResponse body = new ApiErrorResponse(
                "VALIDATION_ERROR",
                "Request validation failed",
                Instant.now(),
                items
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(PairNotActiveException.class)
    public ResponseEntity<ApiErrorResponse> handlePairNotActive(
            PairNotActiveException ex
    ) {
        rateProducerMetrics.rejected();
        ApiErrorResponse body = new ApiErrorResponse(
                "PAIR_NOT_AVAILABLE",
                "Requested pair is currently not available",
                Instant.now(),
                null
        );
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }

    @ExceptionHandler(InvalidRateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRate(InvalidRateException ex) {
        ApiErrorResponse body = new ApiErrorResponse(
                "INVALID_RATE",
                "Request validation failed",
                Instant.now(),
                List.of(
                        new ApiErrorResponse.FieldErrorItem(
                                "bid",
                                ex.getMessage(),
                                ex.getBid()
                        )
                )

        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private ApiErrorResponse.FieldErrorItem toItem(FieldError fe) {
        return new ApiErrorResponse.FieldErrorItem(
                fe.getField(),
                fe.getDefaultMessage(),
                fe.getRejectedValue()
        );
    }
}
