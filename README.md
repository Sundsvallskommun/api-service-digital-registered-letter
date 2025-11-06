# Digital Registered Letter

_The service provides functionality to send digital registered letters with Kivra and stores information about what
letters have been sent, signed and expired._

## Getting Started

### Prerequisites

- **Java 21 or higher**
- **Maven**
- **MariaDB**
- **Git**

### Installation

1. **Clone the repository:**

   ```bash
   git clone https://github.com/Sundsvallskommun/api-service-digital-registered-letter.git
   cd api-service-digital-registered-letter
   ```
2. **Configure the application:**

   Before running the application, you need to set up configuration settings.
   See [Configuration](#configuration)

   **Note:** Ensure all required configurations are set; otherwise, the application may fail to start.

3. **Ensure dependent services are running:**
   If this microservice depends on other services, make sure they are up and accessible.
   See [Dependencies](#dependencies) for more details.

4. **Build and run the application:**

   ```bash
   mvn spring-boot:run
   ```

## API Documentation

Access the API documentation via:

- **Swagger UI:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Usage

### API Endpoints

See the [API Documentation](#api-documentation) for detailed information on available endpoints.

### Example Request

```bash
curl -X 'GET' 'https://localhost:8080/2281/letters?page=0&size=10'

```

## Dependencies

This microservice depends on the following services:

- **Kivra**
  - **Purpose:** Used for sending digital registered letters.
  - **Setup Instructions:** Ensure you have the necessary credentials and configuration for Kivra. Refer to
    the [Kivra API documentation](https://developer.kivra.se/) for more details.
- **Party**
  - **Purpose:** Used for translating between party id and legal id.
  - **Repository:** https://github.com/Sundsvallskommun/api-service-party
  - **Setup Instructions:** See documentation in the repository above for installation and configuration steps.
- **Messaging**
  - **Purpose:** Used to send messages when certificate issues are detected.
  - **Repository:** https://github.com/Sundsvallskommun/api-service-messaging
  - **Setup Instructions:** See documentation in the repository above for installation and configuration steps.
- **Templating**
  - **Purpose:** Used to render PDF templates.
  - **Repository:** https://github.com/Sundsvallskommun/api-service-templating
  - **Setup Instructions:** See documentation in the repository above for installation and configuration steps.

## Configuration

Configuration is crucial for the application to run successfully. Ensure all necessary settings are configured in
`application.yml`.

### Key Configuration Parameters

- **Server Port:**

  ```yaml
  server:
    port: 8080
  ```
- **Database Settings**

  ```yaml
  spring:
    datasource:
      username: <db_username>
      password: <db_password>
      url: jdbc:mariadb://<db_host>:<db_port>/<database>
    jpa:
      properties:
        jakarta:
          persistence:
            schema-generation:
              database:
                action: validate
  ```
- **External Service URLs:**

  ```yaml
  spring:
    security:
      oauth2:
        client:
          provider:
            party:
              token-uri: <token-uri-for-party>
          registration:
            party:
              client-id: <party-client-id>
              client-secret: <party-client-secret>
  integration:
    party:
      url: <party-url>
    kivra:
      api-url: <kivra-url>
      connect-timeout: <maximum-connection-duration in ISO8601-duration format>
      read-timeout: <maximum-read-duration in ISO8601-duration format>
      oauth2:
        token-url: <token-uri-for-kivra>
        client-id: <kivra-client-id>
        client-secret: <kivra-client-secret>
        authorization-grant-type: <grant-type>
  ```
- **Scheduler Settings:**

  ```yaml
  scheduler:
    update-letter-statuses:
      cron: <cron-expression>
      name: <name-of-the-job>
      shedlock-lock-at-most-for: <ISO8601-duration format>
      maximum-execution-time: <ISO8601-duration format>
  ```

### Database Initialization

The project is set up with [Flyway](https://github.com/flyway/flyway) for database migrations. Flyway is disabled by
default, so you will have to enable it to automatically populate the database schema upon application startup.

```yaml
spring:
  flyway:
    enabled: true
```

- **No additional setup is required** for database initialization, as long as the database connection settings are
  correctly configured.

### Additional Notes

- **Application Profiles:**

  Use Spring profiles (`dev`, `prod`, etc.) to manage different configurations for different environments.

- **Logging Configuration:**

  Adjust logging levels if necessary.

## Contributing

Contributions are welcome! Please
see [CONTRIBUTING.md](https://github.com/Sundsvallskommun/.github/blob/main/.github/CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the [MIT License](LICENSE).

## Status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-digital-registered-letter&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-digital-registered-letter)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-digital-registered-letter&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-digital-registered-letter)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-digital-registered-letter&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-digital-registered-letter)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-digital-registered-letter&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-digital-registered-letter)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-digital-registered-letter&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-digital-registered-letter)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-digital-registered-letter&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-digital-registered-letter)

## 

&copy; 2024 Sundsvalls kommun
