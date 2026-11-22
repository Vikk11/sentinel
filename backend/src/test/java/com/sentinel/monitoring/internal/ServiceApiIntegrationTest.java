package com.sentinel.monitoring.internal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private HttpResponse<String> send(
            String method,
            String path,
            String body
    ) throws Exception {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .header("Content-Type", "application/json");

        if (body == null) {
            builder.method(
                    method,
                    HttpRequest.BodyPublishers.noBody()
            );
        } else {
            builder.method(
                    method,
                    HttpRequest.BodyPublishers.ofString(body)
            );
        }

        return client.send(
                builder.build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private HttpResponse<String> post(String body) throws Exception {
        return send("POST", "", body);
    }

    private HttpResponse<String> put(
            String id,
            String body
    ) throws Exception {
        return send("PUT", "/" + id, body);
    }

    private HttpResponse<String> get(String id) throws Exception {
        return send("GET", "/" + id, null);
    }

    private HttpResponse<String> delete(String id) throws Exception {
        return send("DELETE", "/" + id, null);
    }

    private String uniqueUrl() {
        return "https://" + UUID.randomUUID() + ".example.com";
    }

    private String createBody(
            String name,
            String url
    ) {
        return """
                {
                  "name": "%s",
                  "url": "%s",
                  "checkIntervalSeconds": 60,
                  "enabled": true
                }
                """.formatted(name, url);
    }

    private String replaceBody(
            String name,
            String url,
            int interval,
            boolean enabled
    ) {
        return """
                {
                  "name": "%s",
                  "url": "%s",
                  "checkIntervalSeconds": %d,
                  "enabled": %s
                }
                """.formatted(name, url, interval, enabled);
    }

    private String extractString(
            String body,
            String field
    ) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(field)
                        + "\"\\s*:\\s*\"([^\"]+)\""
        );

        Matcher matcher = pattern.matcher(body);

        assertTrue(
                matcher.find(),
                "Expected JSON field: " + field
        );

        return matcher.group(1);
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

    @Test
    void putReplacesFieldsAndPreservesIdAndCreatedAt() throws Exception {
        String originalUrl = uniqueUrl();

        HttpResponse<String> created = post(
                createBody("Original", originalUrl)
        );

        assertEquals(201, created.statusCode());

        String id = extractString(created.body(), "id");
        String createdAt = extractString(created.body(), "createdAt");

        HttpResponse<String> updated = put(
                id,
                replaceBody(
                        "Updated",
                        uniqueUrl(),
                        300,
                        false
                )
        );

        assertEquals(200, updated.statusCode());

        assertEquals(id, extractString(updated.body(), "id"));
        assertEquals(
                createdAt,
                extractString(updated.body(), "createdAt")
        );

        assertTrue(updated.body().contains("\"name\":\"Updated\""));
        assertTrue(updated.body().contains("\"checkIntervalSeconds\":300"));
        assertTrue(updated.body().contains("\"enabled\":false"));
    }

    @Test
    void putMissingEnabledReturns400() throws Exception {
        HttpResponse<String> response = put(
                UUID.randomUUID().toString(),
                """
                {
                  "name": "Test",
                  "url": "https://example.com",
                  "checkIntervalSeconds": 60
                }
                """
        );

        assertEquals(400, response.statusCode());

        assertProblemContentType(response);

        assertTrue(response.body().contains("enabled"));
    }

    @Test
    void putMissingServiceReturns404() throws Exception {
        HttpResponse<String> response = put(
                UUID.randomUUID().toString(),
                replaceBody(
                        "Test",
                        uniqueUrl(),
                        60,
                        true
                )
        );

        assertEquals(404, response.statusCode());

        assertProblemContentType(response);

        assertTrue(
                response.body().contains(
                        "The requested service could not be found."
                )
        );
    }

    @Test
    void deleteReturns204Then404() throws Exception {
        HttpResponse<String> created = post(
                createBody("Delete Me", uniqueUrl())
        );

        assertEquals(201, created.statusCode());

        String id = extractString(created.body(), "id");

        HttpResponse<String> deleted = delete(id);

        assertEquals(204, deleted.statusCode());
        assertTrue(deleted.body().isEmpty());

        HttpResponse<String> secondDelete = delete(id);

        assertEquals(404, secondDelete.statusCode());
        assertProblemContentType(secondDelete);

        HttpResponse<String> getAfterDelete = get(id);

        assertEquals(404, getAfterDelete.statusCode());
    }

    @Test
    void duplicatePostReturns409WithoutUrlLeak() throws Exception {
        String url = uniqueUrl();

        assertEquals(
                201,
                post(createBody("First", url)).statusCode()
        );

        HttpResponse<String> duplicate = post(
                createBody("Second", url)
        );

        assertEquals(409, duplicate.statusCode());

        assertProblemContentType(duplicate);

        assertFalse(duplicate.body().contains(url));

        assertTrue(
                duplicate.body().contains(
                        "A service with this URL already exists."
                )
        );
    }

    @Test
    void duplicatePutReturns409() throws Exception {
        String urlA = uniqueUrl();
        String urlB = uniqueUrl();

        HttpResponse<String> createdA = post(
                createBody("A", urlA)
        );

        HttpResponse<String> createdB = post(
                createBody("B", urlB)
        );

        String idB = extractString(createdB.body(), "id");

        HttpResponse<String> response = put(
                idB,
                replaceBody("B", urlA, 60, true)
        );

        assertEquals(409, response.statusCode());

        assertProblemContentType(response);

        assertFalse(response.body().contains(urlA));
    }

    @Test
    void putKeepingItsOwnUrlDoesNotConflict() throws Exception {
        String url = uniqueUrl();

        HttpResponse<String> created = post(
                createBody("Original", url)
        );

        String id = extractString(created.body(), "id");

        HttpResponse<String> updated = put(
                id,
                replaceBody("Updated", url, 120, false)
        );

        assertEquals(200, updated.statusCode());
        assertTrue(updated.body().contains("\"name\":\"Updated\""));
    }

    @Test
    void caseInsensitiveDuplicateReturns409() throws Exception {
        UUID uuid = UUID.randomUUID();

        HttpResponse<String> first = post(
                createBody("First", "HTTPS://" + uuid + ".Example.COM")
        );

        assertEquals(201, first.statusCode());

        HttpResponse<String> second = post(
                createBody("Second", "https://" + uuid + ".example.com/")
        );

        assertEquals(409, second.statusCode());
    }

    @Test
    void malformedJsonReturns400ProblemDetails() throws Exception {
        HttpResponse<String> response = post(
                "{\"name\":"
        );

        assertEquals(400, response.statusCode());

        assertProblemContentType(response);

        assertFalse(response.body().contains("JsonParseException"));
        assertFalse(response.body().contains("at com.fasterxml"));
    }

    @Test
    void invalidUuidReturns400WithoutEchoingInput() throws Exception {
        HttpResponse<String> response = get("not-a-uuid");

        assertEquals(400, response.statusCode());
        assertProblemContentType(response);

        assertTrue(response.body().contains("\"detail\":\"A request parameter has an invalid format.\""));
        assertFalse(response.body().contains("Failed to convert"));
    }

    private void assertProblemContentType(
            HttpResponse<String> response
    ) {
        assertTrue(
                response.headers()
                        .firstValue("Content-Type")
                        .orElse("")
                        .contains("application/problem+json"),
                "Expected application/problem+json"
        );
    }
}