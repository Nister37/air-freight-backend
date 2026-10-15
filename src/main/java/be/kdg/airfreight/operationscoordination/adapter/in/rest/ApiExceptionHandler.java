package be.kdg.airfreight.operationscoordination.adapter.in.rest;

import be.kdg.airfreight.operationscoordination.LoadPlanningUnavailableException;
import be.kdg.airfreight.flightoperations.FlightNotFoundException;
import be.kdg.airfreight.flightoperations.DelayNotFoundException;
import org.slf4j.*;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import be.kdg.airfreight.loadplanning.LoadNotFoundException;

@RestControllerAdvice
class ApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> invalid(IllegalArgumentException e) { return error(HttpStatus.BAD_REQUEST, e.getMessage()); }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> malformed(HttpMessageNotReadableException e) { return error(HttpStatus.BAD_REQUEST, "Invalid JSON or field value"); }
    @ExceptionHandler(FlightNotFoundException.class)
    ResponseEntity<ApiError> missingFlight(FlightNotFoundException e) { return error(HttpStatus.NOT_FOUND, e.getMessage()); }
    @ExceptionHandler(LoadNotFoundException.class)
    ResponseEntity<ApiError> loadNotFound(LoadNotFoundException e) { return error(HttpStatus.NOT_FOUND, e.getMessage()); }
    @ExceptionHandler(DelayNotFoundException.class)
    ResponseEntity<ApiError> missingDelay(DelayNotFoundException e) { return error(HttpStatus.NOT_FOUND, e.getMessage()); }
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ApiError> refused(IllegalStateException e) { return error(HttpStatus.CONFLICT, e.getMessage()); }
    @ExceptionHandler(LoadPlanningUnavailableException.class)
    ResponseEntity<ApiError> loadUnavailable(LoadPlanningUnavailableException e) { return error(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage()); }
    @ExceptionHandler({OptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    ResponseEntity<ApiError> concurrent(DataAccessException e) { return error(HttpStatus.CONFLICT, "Data changed concurrently; reload and retry"); }
    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<ApiError> storageFailure(DataAccessException e) {
        LOGGER.error("Database operation failed", e);
        return error(HttpStatus.SERVICE_UNAVAILABLE, "Database unavailable; the change was not accepted");
    }
    private ResponseEntity<ApiError> error(HttpStatus status, String detail) {
        LOGGER.warn("Request refused ({}): {}", status.value(), detail);
        return ResponseEntity.status(status).body(new ApiError(detail));
    }

    record ApiError(String detail) { }
}
