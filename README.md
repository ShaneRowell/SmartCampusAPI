# Smart Campus API

A RESTful API built with JAX-RS (Jersey) and Grizzly HTTP server for managing campus rooms and sensors.

---

## Technology Stack

- Java 25
- JAX-RS (Jakarta RESTful Web Services)
- Jersey 3.1.3 (JAX-RS Implementation)
- Grizzly HTTP Server
- Jackson (JSON processing)
- Maven (Build tool)
- In-memory storage (ConcurrentHashMap)

---

## Project Structure

```
src/main/java/com/smartcampus/
├── Main.java                          # Entry point, server startup
├── AppConfig.java                     # JAX-RS application config
├── DataStore.java                     # In-memory data storage
├── model/
│   ├── Room.java
│   ├── Sensor.java
│   └── SensorReading.java
├── resource/
│   ├── DiscoveryResource.java         # GET /api/v1/discovery
│   ├── RoomResource.java              # /api/v1/rooms
│   ├── SensorResource.java            # /api/v1/sensors
│   └── SensorReadingResource.java     # /api/v1/sensors/{id}/readings
├── exception/
│   ├── RoomNotEmptyException.java
│   ├── RoomNotEmptyExceptionMapper.java
│   ├── LinkedResourceNotFoundException.java
│   ├── LinkedResourceNotFoundExceptionMapper.java
│   ├── SensorUnavailableException.java
│   ├── SensorUnavailableExceptionMapper.java
│   └── GlobalExceptionMapper.java
└── filter/
    └── LoggingFilter.java
```

---

## How to Build and Run

### Prerequisites
- JDK 25
- Apache NetBeans 28 or 29
- Maven (bundled with NetBeans)

### Steps

1. Clone the repository:
```
git clone https://github.com/ShaneRowell/SmartCampusAPI.git
```

2. Open the project in NetBeans:
   - File → Open Project → select the SmartCampusAPI folder

3. Build the project:
   - Right-click project → Clean and Build

4. Run the project:
   - Right-click project → Run

5. The server will start at:
```
http://localhost:8080/api/v1/
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/discovery | API metadata and resource links |
| GET | /api/v1/rooms | Get all rooms |
| POST | /api/v1/rooms | Create a new room |
| GET | /api/v1/rooms/{roomId} | Get a specific room |
| DELETE | /api/v1/rooms/{roomId} | Delete a room |
| GET | /api/v1/sensors | Get all sensors |
| GET | /api/v1/sensors?type=CO2 | Filter sensors by type |
| POST | /api/v1/sensors | Create a new sensor |
| GET | /api/v1/sensors/{sensorId} | Get a specific sensor |
| GET | /api/v1/sensors/{sensorId}/readings | Get readings for a sensor |
| POST | /api/v1/sensors/{sensorId}/readings | Add a reading to a sensor |

---

## Sample curl Commands

### 1. Get API Discovery
```bash
curl -X GET http://localhost:8080/api/v1/discovery
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"LIB-301\",\"name\":\"Library Quiet Study\",\"capacity\":50}"
```

### 3. Create a Sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"TEMP-001\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":22.5,\"roomId\":\"LIB-301\"}"
```

### 4. Filter Sensors by Type
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
```

### 5. Add a Sensor Reading
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":25.3}"
```

### 6. Get All Readings for a Sensor
```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

### 7. Delete a Room
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

---

## Error Handling

| Status Code | Scenario |
|-------------|----------|
| 409 Conflict | Deleting a room that still has sensors assigned |
| 422 Unprocessable Entity | Creating a sensor with a roomId that does not exist |
| 403 Forbidden | Posting a reading to a sensor under MAINTENANCE |
| 500 Internal Server Error | Any unexpected runtime error |

---

## Report — Answers to Coursework Questions

### Part 1 — Setup & Discovery

**Q: Explain the default lifecycle of a JAX-RS Resource class. Is a new instance created per request or is it a singleton?**

By default, JAX-RS creates a new instance of a resource class for every incoming HTTP request. This is known as the per-request lifecycle. Each request gets its own isolated object, which avoids concurrency issues between simultaneous requests. However, because a new instance is created each time, resource classes cannot store state between requests using instance variables. This is why a shared singleton DataStore was implemented using a static instance pattern with ConcurrentHashMap. ConcurrentHashMap ensures thread safety when multiple requests read or write to the in-memory data simultaneously, preventing race conditions and data corruption.

**Q: Why is HATEOAS considered a hallmark of advanced RESTful design? How does it benefit client developers?**

HATEOAS (Hypermedia as the Engine of Application State) means that API responses include links to related resources and available actions, rather than requiring clients to construct URLs manually. This makes the API self-documenting and self-navigable. Client developers benefit because they do not need to hardcode endpoint paths or consult external documentation to discover what actions are available. Instead, the API guides them dynamically through the available state transitions, making integrations more resilient to API changes and easier to maintain.

---

### Part 2 — Room Management

**Q: What are the implications of returning only IDs versus full room objects in a list response?**

Returning only IDs reduces the response payload size significantly, which improves network performance and reduces bandwidth consumption, especially when there are thousands of rooms. However, the client must then make additional requests for each room to retrieve its full details, which increases the number of round trips and can degrade performance. Returning full room objects in a single response is more convenient for the client and reduces round trips, but at the cost of larger payloads. The best approach depends on the use case — for dashboards displaying summaries, full objects are preferable, while for large datasets, IDs with pagination may be more efficient.

**Q: Is the DELETE operation idempotent in your implementation?**

Yes, the DELETE operation is idempotent in this implementation. Idempotency means that making the same request multiple times produces the same result as making it once. In this API, the first DELETE request on a valid room either removes it successfully (204 No Content) or rejects it if sensors are still assigned (409 Conflict). If the same DELETE request is sent again after the room has already been deleted, the API returns a 404 Not Found response. While the status codes differ between the first and subsequent calls, the server state remains consistent — the room is gone — which satisfies the principle of idempotency as defined in the HTTP specification.

---

### Part 3 — Sensor Operations

**Q: What happens if a client sends data in a format other than JSON to a @Consumes(APPLICATION_JSON) endpoint?**

If a client sends a request with a Content-Type other than application/json, such as text/plain or application/xml, JAX-RS will automatically reject the request before it even reaches the resource method. The framework returns an HTTP 415 Unsupported Media Type response, indicating that the server cannot process the content format provided. This is handled entirely by the JAX-RS runtime based on the @Consumes annotation, providing automatic input validation without any additional code in the resource class.

**Q: Why is the query parameter approach superior to path-based filtering for searching collections?**

Query parameters such as GET /api/v1/sensors?type=CO2 are semantically more appropriate for filtering because they represent optional, variable criteria applied to a collection rather than identifying a specific resource. Path parameters such as /api/v1/sensors/type/CO2 imply that "type/CO2" is a distinct resource in itself, which is semantically incorrect. Query parameters also support combining multiple filters easily, for example ?type=CO2&status=ACTIVE, without requiring changes to the URL structure. Additionally, query parameters are the established convention for filtering, searching, and pagination in RESTful API design, making the API more intuitive for developers.

---

### Part 4 — Sub-Resources

**Q: Discuss the architectural benefits of the Sub-Resource Locator pattern.**

The Sub-Resource Locator pattern improves API maintainability by separating concerns into dedicated classes. Instead of handling all nested paths in one large controller, each resource class has a single responsibility. SensorResource manages sensors, while SensorReadingResource manages readings exclusively. This makes the codebase easier to read, test, and extend. In large APIs with deep nesting, a single monolithic controller becomes difficult to manage and prone to errors. The sub-resource locator delegates routing to specialised classes, keeping each class focused and cohesive, which aligns with the Single Responsibility Principle in software design.

---

### Part 5 — Error Handling & Logging

**Q: Why is HTTP 422 more semantically accurate than 404 when a referenced resource is missing inside a valid payload?**

A 404 Not Found response implies that the requested URL or endpoint does not exist. However, when a client submits a valid JSON payload to a valid endpoint but references a non-existent resource inside that payload, such as an invalid roomId, the endpoint itself exists and was found successfully. The problem is with the content of the request body — a semantic validation failure rather than a missing route. HTTP 422 Unprocessable Entity is specifically designed for situations where the request is syntactically correct but semantically invalid, making it a more precise and informative status code in this context.

**Q: What are the cybersecurity risks of exposing Java stack traces to API consumers?**

Exposing raw Java stack traces to external clients presents several serious security risks. Stack traces reveal the internal package structure and class names of the application, giving attackers a map of the codebase. They can expose the versions of frameworks and libraries in use, allowing attackers to look up known vulnerabilities for those specific versions. Stack traces may also reveal database queries, file paths, or configuration details that can be exploited. Additionally, error messages within stack traces can confirm whether certain resources or users exist, aiding enumeration attacks. The GlobalExceptionMapper in this implementation prevents all of this by catching every unhandled exception and returning only a generic 500 response with no internal details.

**Q: Why is it better to use JAX-RS filters for logging rather than manually inserting Logger.info() in every method?**

Using JAX-RS filters for logging implements the cross-cutting concern of observability in a single, centralised place. If logging were added manually to every resource method, it would result in significant code duplication across the entire codebase, making it harder to maintain and modify. If the logging format needed to change, every method would need to be updated individually. Filters apply automatically to every request and response without modifying any resource class, keeping the resource code clean and focused on business logic. This approach follows the Separation of Concerns principle and makes the logging behaviour consistent and easy to manage.
