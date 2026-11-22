package com.sentinel.monitoring.internal;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/v1/services")
class ServiceController {

    private final ServiceService service;

    ServiceController(ServiceService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<ServiceResponse> create(@Valid @RequestBody CreateServiceRequest request) {
        ServiceResponse response = service.create(request);

        return ResponseEntity
                .created(URI.create("/v1/services/" + response.id()))
                .body(response);
    }

    @GetMapping("/{id}")
    ResponseEntity<ServiceResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    ResponseEntity<ServiceResponse> replace(@PathVariable UUID id, @Valid @RequestBody ReplaceServiceRequest request) {
        return ResponseEntity.ok(service.replace(id, request));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}