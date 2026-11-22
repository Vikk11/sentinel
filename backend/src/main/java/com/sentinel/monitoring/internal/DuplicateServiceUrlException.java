package com.sentinel.monitoring.internal;

class DuplicateServiceUrlException extends RuntimeException {
    DuplicateServiceUrlException() {
        super("Service URL is already duplicated.");
    }
}