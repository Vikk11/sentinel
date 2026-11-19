package com.sentinel.monitoring.internal;

class InvalidServiceUrlException extends RuntimeException {
    InvalidServiceUrlException() {
        super("The provided service URL is invalid.");
    }
}