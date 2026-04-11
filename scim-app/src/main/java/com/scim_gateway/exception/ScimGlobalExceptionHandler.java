package com.scim_gateway.exception;

import com.scim_gateway.model.scim.ScimError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
public class ScimGlobalExceptionHandler {
    
    @ExceptionHandler(ScimResourceNotFoundException.class)
    public ResponseEntity<ScimError> handleNotFound(ScimResourceNotFoundException ex) {
        ScimError error = new ScimError();
        error.setDetail(ex.getMessage());
        error.setStatus(String.valueOf(ex.getStatusCode()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(ScimAlreadyExistsException.class)
    public ResponseEntity<ScimError> handleConflict(ScimAlreadyExistsException ex) {
        ScimError error = new ScimError();
        error.setDetail(ex.getMessage());
        error.setStatus(String.valueOf(ex.getStatusCode()));
        error.setScimType(ex.getScimType());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(ScimInvalidFilterException.class)
    public ResponseEntity<ScimError> handleInvalidFilter(ScimInvalidFilterException ex) {
        ScimError error = new ScimError();
        error.setDetail(ex.getMessage());
        error.setStatus(String.valueOf(ex.getStatusCode()));
        error.setScimType(ex.getScimType());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(ScimException.class)
    public ResponseEntity<ScimError> handleScimException(ScimException ex) {
        ScimError error = new ScimError();
        error.setDetail(ex.getMessage());
        error.setStatus(String.valueOf(ex.getStatusCode()));
        if (ex.getScimType() != null) {
            error.setScimType(ex.getScimType());
        }
        return ResponseEntity.status(HttpStatus.valueOf(ex.getStatusCode())).body(error);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ScimError> handleRuntimeException(RuntimeException ex) {
        ScimError error = new ScimError();
        error.setDetail(ex.getMessage());
        error.setStatus("500");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ScimError> handleGenericException(Exception ex) {
        ScimError error = new ScimError();
        error.setDetail("An unexpected error occurred: " + ex.getMessage());
        error.setStatus("500");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
