# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot starter library (`ai-service-spring-boot-starter`) that enables applications deployed in Kubernetes to communicate with an AI Gateway service. The library handles authentication via Kubernetes ServiceAccount tokens.

## Build Commands

```bash
# Build and install to local Maven repository
mvn clean install

# Compile only
mvn compile

# Run tests
mvn test
```

## Architecture

### Core Components

- **AiService** (interface) - Main entry point for consumers. Exposes `sendRequest(AiRequest)` method returning `AiResponse`
- **DefaultAiService** - Implementation that delegates to AiGatewayClient
- **AiGatewayClient** (`internal` package) - Handles HTTP communication with the AI Gateway using WebClient. Attaches SA-Token header for authentication
- **TokenManager** (`internal` package) - Requests time-bound (10 min), audience-scoped ("aigateway") tokens from Kubernetes Token API
- **AiServiceConfiguration** - Spring `@AutoConfiguration` that wires all beans together
- **AiServiceProperties** - Configuration properties record with prefix `aiservice`

### Authentication Flow

1. TokenManager requests a token from Kubernetes API for the configured ServiceAccount
2. Token is scoped to "aigateway" audience with 600 second expiration
3. AiGatewayClient attaches token in "SA-Token" header when calling AI Gateway
4. AI Gateway validates the token before processing

### Dependencies

- Requires `com.leonardo.aiservice:common` library (must be installed separately) - provides `AiRequest`, `AiResponse`, and `Context` types
- Uses Spring Boot 3.5.x with WebFlux
- Uses Kubernetes Java client for token management

## Configuration Properties

Properties use the `aiservice` prefix and can be set via application.properties or environment variables:

| Property | Env Variable | Default | Description |
|----------|--------------|---------|-------------|
| `aiservice.uri` | `AISERVICE_URI` | `http://aigateway-middleware.accessibilita.svc.cluster.local:80` | AI Gateway service URL |
| `aiservice.endpoint` | `AISERVICE_ENDPOINT` | `/aiservice/airequest` | AI Gateway endpoint path |
| `aiservice.k8s.namespace` | `AISERVICE_K8S_NAMESPACE` | - | Kubernetes namespace of the workload |
| `aiservice.k8s.service-account` | `AISERVICE_K8S_SERVICE_ACCOUNT` | - | ServiceAccount name for the workload |

## Kubernetes Requirements

The workload using this library must have a ClusterRole allowing token creation:
```yaml
rules:
- apiGroups: [""]
  resources: ["serviceaccounts/token"]
  verbs: ["create"]
```

## Notes for Claude
- When solving questions and answers that imply coding using external libraries and frameworks, always check Context7 for documentation check, and avoid using legacy code