package com.sentinel.monitoring.internal;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/v1/services")
class ServiceController {
    private final ServiceService service;

    ServiceController(ServiceService service){
        this.service = service;
    }

    @PostMapping
    ResponseEntity<ServiceResponse> create(@Valid @RequestBody CreateServiceRequest request){
        ServiceResponse created = service.create(request);

        var location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(created);
    }

    @GetMapping("/{id}")
    ResponseEntity<ServiceResponse> getById(@PathVariable UUID id){
        return ResponseEntity.ok(service.getById(id));
    }
}