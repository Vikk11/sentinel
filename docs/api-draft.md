# Sentinel API Draft

## Overview

This document defines the initial REST API contract for Sentinel.

The API allows authenticated users to manage monitored services and retrieve their health-check history.

All endpoints use the `/v1` prefix.

## 1. Services

Services represent websites, APIs, or other endpoints that Sentinel monitors.

### Endpoint Summary

| Method | Endpoint | Purpose | Success |
|---|---|---|---|
| POST | `/v1/services` | Create a monitored service | 201 Created |
| GET | `/v1/services` | List monitored services | 200 OK |
| GET | `/v1/services/{id}` | Get a specific service | 200 OK |
| PUT | `/v1/services/{id}` | Replace a service configuration | 200 OK |
| PATCH | `/v1/services/{id}` | Partially update a service | 200 OK |
| DELETE | `/v1/services/{id}` | Delete a service | 204 No Content |

### 1.1 Create a Service

**POST** `/v1/services`

Creates a new monitored service for the authenticated tenant.

Request body:

```json
{
  "name": "Shop API",
  "url": "https://api.shop.com/health",
  "checkIntervalSeconds": 60,
  "enabled": true
}
```

Success response: `201 Created`

Returns the created service as a `ServiceResponse` object.

The response should also include a `Location` header pointing to the new resource.

Possible errors:

- `400 Bad Request` – Invalid request body or missing required fields.
- `401 Unauthorized` – Authentication is missing or invalid.
- `409 Conflict` – A service with the same URL already exists for the tenant.
- `403 Forbidden` – The authenticated user does not have permission to perform this operation (for example, a viewer attempting to modify a service).

### 1.2 List Services

**GET** `/v1/services`

Returns monitored services belonging to the authenticated tenant.

Query parameters:

| Parameter | Required | Description |
|---|---|---|
| `cursor` | No | Opaque cursor identifying where the next page starts. |
| `limit` | No | Number of records to return. Default: 50. Maximum: 100. |

Example request:

`GET /v1/services?limit=50`

Success response: `200 OK`

Returns a paginated collection:

```json
{
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Shop API",
      "url": "https://api.shop.com/health",
      "checkIntervalSeconds": 60,
      "enabled": true,
      "createdAt": "2026-11-12T10:00:00Z",
      "updatedAt": "2026-11-12T10:00:00Z"
    }
  ],
  "nextCursor": "opaque-cursor-token",
  "hasMore": true
}
```

Pagination and sorting rules
- Default limit is 50.
- Maximum limit is 100.
- Limits greater than 100 return 400 Bad Request.
- Results are ordered by createdAt descending, with UUID as a unique tie-breaker.
- The cursor represents both createdAt and the service UUID.
- Cursors are opaque to clients. Clients must not decode, modify, or construct them.
- The server validates every cursor before using it.
- Cursors are treated as untrusted input and must never be directly inserted into database queries.
- Database queries use validated, parameterized values.
- If no cursor is provided, the newest records are returned.
- If no more records exist, nextCursor is null and hasMore is false.

Possible errors:

- `401 Unauthorized` – Authentication is missing or invalid.
- `403 Forbidden` – The authenticated user does not have permission to view services.

### 1.3 Get a Service

**GET** `/v1/services/{id}`

Returns details about a specific monitored service.

Success response: `200 OK`

Returns a single `ServiceResponse` object.

Possible errors:

- `401 Unauthorized` – Authentication is missing or invalid.
- `404 Not Found` – The service does not exist or does not belong to the authenticated tenant.

### 1.4 Replace a Service

**PUT** `/v1/services/{id}`

Replaces the editable configuration of an existing service.

Request body:

```json
{
  "name": "Shop API Production",
  "url": "https://api.shop.com/health",
  "checkIntervalSeconds": 30,
  "enabled": true
}
```

All editable fields must be provided, including:
- `name`
- `url`
- `checkIntervalSeconds`
- `enabled`

- `enabled` is required and must be a Boolean. Omitting it returns `400 Bad Request`.

Success response: `200 OK`

Returns the updated `ServiceResponse` object.

Possible errors:

- `400 Bad Request` – Invalid request body or missing required fields.
- `401 Unauthorized` – Authentication is missing or invalid.
- `404 Not Found` – Service does not exist within the authenticated tenant.
- `409 Conflict` – The updated URL conflicts with another service belonging to the same tenant.
- `403 Forbidden` – The authenticated user does not have permission to perform this operation (for example, a viewer attempting to modify a service).

### 1.5 Partially Update a Service

**PATCH** `/v1/services/{id}`

Updates only the fields included in the request body.

Request body example:

```json
{
  "name": "Shop API Production"
}
```

Supported fields:

- `name`
- `url`
- `checkIntervalSeconds`
- `enabled`

Fields not included remain unchanged.

An empty request body is rejected.

Success response: `200 OK`

Returns the updated `ServiceResponse` object.

Possible errors:

- `400 Bad Request` – Invalid field values or unsupported fields.
- `401 Unauthorized` – Authentication is missing or invalid.
- `404 Not Found` – Service does not exist within the authenticated tenant.
- `409 Conflict` – Updated URL conflicts with another service.
- `403 Forbidden` – The authenticated user does not have permission to perform this operation (for example, a viewer attempting to modify a service).

### 1.6 Delete a Service

**DELETE** `/v1/services/{id}`

Deletes a monitored service belonging to the authenticated tenant.

Success response: `204 No Content`

Returns no response body.

Possible errors:

- `401 Unauthorized` – Authentication is missing or invalid.
- `403 Forbidden` – The authenticated user does not have permission to delete services.
- `404 Not Found` – Service does not exist within the authenticated tenant.

---

## 2. Checks

Checks represent individual health-check executions performed by Sentinel.

Check history is read-only through this API. Clients cannot manually create, update, or delete check results.

### 2.1 Get Service Check History

**GET** `/v1/services/{id}/checks`

Returns the health-check history for a specific monitored service.

Query parameters:

| Parameter | Required | Description |
|---|---|---|
| `cursor` | No | Cursor identifying where the next page should start. |
| `limit` | No | Number of records to return. Default: 50. Maximum: 100. |

Example request:

`GET /v1/services/550e8400-e29b-41d4-a716-446655440000/checks?limit=50`

Example response:

```json
{
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "status": "SUCCESS",
      "statusCode": 200,
      "latencyMs": 145,
      "errorType": null,
      "checkedAt": "2026-11-12T10:30:00Z"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440002",
      "status": "FAILURE",
      "statusCode": null,
      "latencyMs": 5000,
      "errorType": "TIMEOUT",
      "checkedAt": "2026-11-12T10:29:00Z"
    }
  ],
  "nextCursor": "opaque-cursor-token",
  "hasMore": true
}
```

Check result fields:

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Unique check identifier. |
| `status` | String | SUCCESS or FAILURE. |
| `statusCode` | Integer or null | HTTP response status code. Null when no HTTP response was received. |
| `latencyMs` | Integer | Duration of the check in milliseconds. |
| `errorType` | String or null | Failure reason when applicable. Always included; null for successful checks. |
| `checkedAt` | DateTime | Time when the check was executed. |

Supported error types:

- `TIMEOUT`
- `DNS_FAILURE`
- `TLS_ERROR`
- `CONNECTION_REFUSED`
- `BLOCKED_DESTINATION`

For successful checks, errorType is null.
For failures without an HTTP response, statusCode is null.

Success response: `200 OK`

Returns a paginated collection of check records.

Pagination rules:

- Default limit is 50.
- Maximum limit is 100.
- Limits greater than 100 return `400 Bad Request`.
- Results are ordered by `checkedAt` descending, with UUID as a unique tie-breaker.
- The cursor represents both `checkedAt` and the check UUID.
- Cursors are opaque to clients. Clients must not decode, modify, or construct them.
- The server validates every cursor before using it.
- Cursors are treated as untrusted input and must never be directly inserted into database queries.
- Database queries use validated, parameterized values.
- If no cursor is provided, the newest records are returned.
- If no more records exist, `nextCursor` is null and `hasMore` is false.

Possible errors:

- `400 Bad Request` – Invalid cursor or limit outside the allowed range.
- `401 Unauthorized` – Authentication is missing or invalid.
- `404 Not Found` – Service does not exist within the authenticated tenant.
- `403 Forbidden` – The authenticated user does not have permission to view check history.

---

## 3. DTO Definitions

DTOs (Data Transfer Objects) define the data exchanged between the client and the API.

They expose only the fields clients need and prevent internal database or tenant information from leaking.

All service IDs use UUID format.

Example: `550e8400-e29b-41d4-a716-446655440000`

IDs are generated by the server and are never accepted from clients
when creating a service.

### 3.1 CreateServiceRequest

Used when creating a monitored service.

```json
{
  "name": "Shop API",
  "url": "https://api.shop.com/health",
  "checkIntervalSeconds": 60,
  "enabled": true
}
```

Fields:

| Field | Type | Description |
|---|---|---|
| `name` | String | Display name of the monitored service. Required. |
| `url` | String | URL Sentinel should monitor. Required. |
| `checkIntervalSeconds` | Integer | Interval between health checks. Required. |
| `enabled`                   | Boolean | Whether monitoring is enabled. Optional; defaults to true.  |

Validation:

- `name` must not be blank.
- `url` must be a valid HTTP or HTTPS URL.
- `checkIntervalSeconds` must be within the supported range (30–3600 seconds).
- `enabled` is optional and defaults to true when omitted.

### 3.2 ServiceResponse

Returned when retrieving or modifying a monitored service.

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Shop API",
  "url": "https://api.shop.com/health",
  "checkIntervalSeconds": 60,
  "enabled": true,
  "createdAt": "2026-11-12T10:00:00Z",
  "updatedAt": "2026-11-12T10:15:00Z"
}
```

Fields:

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Unique service identifier. |
| `name` | String | Display name. |
| `url` | String | Monitored endpoint URL. |
| `checkIntervalSeconds` | Integer | Configured check interval. |
| `enabled` | Boolean | Whether monitoring is enabled. |
| `createdAt` | DateTime | Creation timestamp. |
| `updatedAt` | DateTime | Last modification timestamp. |

The response deliberately excludes internal fields such as `tenantId`, database-specific identifiers, and internal processing metadata.

---

## 4. Conventions

### Error Format

All API errors follow RFC 9457 (Problem Details for HTTP APIs).

Content-Type:

`application/problem+json`

Example:

```json
{
  "type": "about:blank",
  "title": "Validation Failed",
  "status": 400,
  "detail": "One or more request fields are invalid.",
  "instance": "/v1/services",
  "errors": [
    {
      "field": "url",
      "message": "URL must use HTTP or HTTPS."
    }
  ]
}
```

### URL Security Validation

Sentinel must protect against Server-Side Request Forgery (SSRF).

URLs must:

- Use HTTP or HTTPS.
- Not resolve to localhost or loopback addresses.
- Not resolve to private or internal IP ranges.
- Not resolve to cloud metadata addresses.
- Not access internal infrastructure or restricted network destinations.

These checks must be performed both when a service is created or updated
and immediately before each monitoring request.

DNS resolution must be validated to prevent DNS rebinding attacks.
Redirect destinations must also be validated before following redirects.

Requests to prohibited destinations must be rejected.

### Tenant Isolation

Tenant information is never accepted directly from request bodies or query parameters.

The tenant is determined from the authenticated user's security context (for example, their JWT claims).

Every service and check query must be scoped to the authenticated tenant.

If a user requests a service belonging to another tenant, the API returns `404 Not Found` rather than revealing that the resource exists.

### Idempotency

An operation is idempotent if repeating the same request produces the same intended resource state.

| Method | Idempotent | Reason |
|---|---|---|
| GET | Yes | Does not modify resources. |
| POST | No | Creates a new resource on each successful request. |
| PUT | Yes | Replaces the resource with the same supplied configuration. |
| PATCH | Depends on operation | Setting a field to a specific value is idempotent; relative operations may not be. |
| DELETE | Yes | Repeated deletion returns 404 on the second request, but the resource remains deleted, so the resulting state is identical. |

---

## 5. Future Considerations

The following resources are planned for future API versions or extensions:

- `/v1/incidents` – Incident management and history.
- `/v1/incidents/{id}/updates` – Incident timeline entries.
- `/v1/alert-rules` – Configurable alerting rules.
- `/v1/api-keys` – API key management.
- `/v1/status-pages` – Public service status pages.

These endpoints are outside the scope of this initial API draft.
