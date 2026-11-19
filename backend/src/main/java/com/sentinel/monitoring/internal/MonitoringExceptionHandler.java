package com.sentinel.monitoring.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.sentinel.monitoring")
@Order(Ordered.HIGHEST_PRECEDENCE)
class MonitoringExceptionHandler{
    private static final Logger log = LoggerFactory.getLogger(MonitoringExceptionHandler.class);

    @ExceptionHandler(ServiceNotFoundException.class)
    ResponseEntity<ProblemDetail> handleServiceNotFound(ServiceNotFoundException ex){
        log.info("Requested monitored service was not found.");

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "The requested service could not be found."
        );

        problem.setTitle("Resource Not Found");

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(InvalidServiceUrlException.class)
    ResponseEntity<ProblemDetail> handleInvalidServiceUrl(InvalidServiceUrlException ex){
        log.info("Rejected invalid service URL");

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "The provided service URL is invalid."
        );

        problem.setTitle("Invalid Request");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}