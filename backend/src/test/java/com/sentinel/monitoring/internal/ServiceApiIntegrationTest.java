package com.sentinel.monitoring.internal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ServiceApiIntegrationTest {
    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    private String baseUrl() {
        return "http://localhost:" + port + "/v1/services";
    }

    private HttpResponse<String> post(String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private HttpResponse<String> get(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "/" + id))
                .GET()
                .build();

        return client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }

    @Test
    void nonexistentServiceReturnsProblemDetails404() throws Exception {
        HttpResponse<String> response = get("99999999-9999-9999-9999-999999999999");

        assertEquals(404, response.statusCode());

        assertTrue(
                response.headers()
                        .firstValue("Content-Type")
                        .orElse("")
                        .startsWith("application/problem+json")
        );

        assertTrue(response.body().contains("Resource Not Found"));
        assertTrue(response.body().contains(
                "The requested service could not be found."
        ));

        assertFalse(response.body().contains("Exception"));
        assertFalse(response.body().contains("at com.sentinel"));
    }

    @Test
    void invalidRequestReturnsValidationErrors() throws Exception {
        String body = """
                {
                    "name": "",
                    "url": "https://example.com"
                }
                """;

        HttpResponse<String> response = post(body);

        assertEquals(400, response.statusCode());

        assertTrue(
                response.headers()
                        .firstValue("Content-Type")
                        .orElse("")
                        .startsWith("application/problem+json")
        );

        assertTrue(response.body().contains("\"field\":\"name\""));
        assertTrue(response.body().contains(
                "\"field\":\"checkIntervalSeconds\""
        ));
    }

    @Test
    void successfulPostUsesCorrectApiFieldName() throws Exception {
        String body = """
                {
                    "name": "Example",
                    "url": "https://example.com",
                    "checkIntervalSeconds": 60
                }
                """;

        HttpResponse<String> response = post(body);

        assertEquals(201, response.statusCode());

        assertTrue(
                response.headers().firstValue("Location").isPresent()
        );

        assertTrue(
                response.headers()
                        .firstValue("Content-Type")
                        .orElse("")
                        .contains("application/json")
        );

        assertTrue(
                response.body().contains("\"checkIntervalSeconds\":60"),
                "Response must contain checkIntervalSeconds"
        );

        assertFalse(response.body().contains("tenantId"));
    }
}