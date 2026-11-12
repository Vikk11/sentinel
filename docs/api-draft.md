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
  "checkIntervalSeconds": 60
}
```

Success response: `201 Created`

Returns the created service as a `ServiceResponse` object.

The response should also include a `Location` header pointing to the new resource.

Possible errors:

- `400 Bad Request` – Invalid request body or missing required fields.
- `401 Unauthorized` – Authentication is missing or invalid.
- `409 Conflict` – A service with the same URL already exists for the tenant.

### 1.2 List Services

**GET** `/v1/services`

Returns all monitored services belonging to the authenticated tenant.

Success response: `200 OK`

Returns an array of `ServiceResponse` objects.

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
  "checkIntervalSeconds": 30
}
```

All editable fields must be provided.

Success response: `200 OK`

Returns the updated `ServiceResponse` object.

Possible errors:

- `400 Bad Request` – Invalid request body or missing required fields.
- `401 Unauthorized` – Authentication is missing or invalid.
- `404 Not Found` – Service does not exist within the authenticated tenant.
- `409 Conflict` – The updated URL conflicts with another service belonging to the same tenant.

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

Fields not included remain unchanged.

An empty request body is rejected.

Success response: `200 OK`

Returns the updated `ServiceResponse` object.

Possible errors:

- `400 Bad Request` – Invalid field values or unsupported fields.
- `401 Unauthorized` – Authentication is missing or invalid.
- `404 Not Found` – Service does not exist within the authenticated tenant.
- `409 Conflict` – Updated URL conflicts with another service.

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

`GET /v1/services/123/checks?limit=50`

Example response:

```json
{
  "data": [
    {
      "id": "check-001",
      "status": "SUCCESS",
      "statusCode": 200,
      "latencyMs": 145,
      "checkedAt": "2026-11-12T10:30:00Z"
    },
    {
      "id": "check-002",
      "status": "FAILURE",
      "statusCode": 503,
      "latencyMs": 2100,
      "checkedAt": "2026-11-12T10:29:00Z"
    }
  ],
  "nextCursor": "eyJjaGVja2VkQXQiOiIyMDI2LTExLTEyVDEwOjI5OjAwWiJ9",
  "hasMore": true
}
```

Success response: `200 OK`

Returns a paginated collection of check records.

Pagination rules:

- Default limit is 50.
- Maximum limit is 100.
- Requests with a limit greater than 100 return `400 Bad Request`.
- If no cursor is provided, the newest records are returned.
- Results are ordered by `checkedAt` descending, with a unique ID as a tie-breaker.
- The next cursor is returned when more records are available.
- If no more records exist, `nextCursor` is `null` and `hasMore` is `false`.

Possible errors:

- `400 Bad Request` – Invalid cursor or limit outside the allowed range.
- `401 Unauthorized` – Authentication is missing or invalid.
- `404 Not Found` – Service does not exist within the authenticated tenant.

---

## 3. DTO Definitions

DTOs (Data Transfer Objects) define the data exchanged between the client and the API.

They expose only the fields clients need and prevent internal database or tenant information from leaking.

### 3.1 CreateServiceRequest

Used when creating a monitored service.

```json
{
  "name": "Shop API",
  "url": "https://api.shop.com/health",
  "checkIntervalSeconds": 60
}
```

Fields:

| Field | Type | Description |
|---|---|---|
| `name` | String | Display name of the monitored service. Required. |
| `url` | String | URL Sentinel should monitor. Required. |
| `checkIntervalSeconds` | Integer | Interval between health checks. Required. |

Validation:

- `name` must not be blank.
- `url` must be a valid HTTP or HTTPS URL.
- `checkIntervalSeconds` must be within the supported range (30–3600 seconds).

### 3.2 ServiceResponse

Returned when retrieving or modifying a monitored service.

```json
{
  "id": "service-001",
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
| `id` | String | Unique service identifier. |
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

Errors follow the Problem Details format defined in RFC 9457.

Example:

```json
{
  "type": "about:blank",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "The requested service could not be found.",
  "instance": "/v1/services/123"
}
```

The API uses standard HTTP status codes to communicate success and failure.

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
| DELETE | Yes | Repeated deletion leaves the resource deleted. |

---

## 5. Future Considerations

The following resources are planned for future API versions or extensions:

- `/v1/incidents` – Incident management and history.
- `/v1/incidents/{id}/updates` – Incident timeline entries.
- `/v1/alert-rules` – Configurable alerting rules.
- `/v1/api-keys` – API key management.
- `/v1/status-pages` – Public service status pages.

These endpoints are outside the scope of this initial API draft.
