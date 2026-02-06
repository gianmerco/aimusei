# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a lightweight Java commons library (`com.leonardo.aiservice:common`) that provides DTOs for AI gateway request/response communication. It's designed for text transformation based on accessibility contexts (disabilities).

## Build Commands

```bash
# Build the project
mvn clean compile

# Package the JAR
mvn clean package

# Deploy to Azure Artifacts repository
mvn clean deploy
```

Note: Deployment requires Maven settings with Azure DevOps authentication configured.

## Architecture

### Request/Response Hierarchy

```
AbstractRequest (abstract)
    └─> AIRequest (concrete, builder pattern)
        - contains: input (String), contexts (List<Context>)

AbstractResponse (abstract)
    └─> AIResponse (concrete, builder pattern)
        - contains: outputs (Map<Context, Output<?>>)
```

### Key Classes

All classes are in package `com.leonardo.aiservice`:

- **AbstractRequest/AIRequest**: Request DTOs. AIRequest carries an input text and a list of Context values indicating which transformations to apply.
- **AbstractResponse/AIResponse**: Response DTOs. AIResponse maps each Context to its corresponding Output.
- **Context**: Enum of accessibility contexts: `DISLESSIA`, `DISCALCULIA`, `ADHD`, `EASY_TO_READ`, `CAA`, `NONE`. Includes `fromString()` for case-insensitive parsing.
- **Output<T>**: Generic wrapper for context-specific output values. Type varies by context (e.g., `List<String>` for CAA images as base64, `String` for text transformations).

### Design Patterns

- **Builder Pattern**: Both AIRequest and AIResponse use inner Builder classes for construction
- **Immutability**: Objects have final fields and no setters; create via Builder, then immutable
- **Generics**: Output<T> allows type-safe context-specific data

## Project Configuration

- **Java Version**: 21
- **Dependencies**: None (intentionally lightweight)
- **Repository**: Azure Artifacts at `ai-solution-repository`
- **CI/CD**: Azure Pipelines (builds on main branch, runs `mvn clean deploy`)

## Language Notes

Documentation and comments are primarily in Italian. Key terms:
- Dislessia = Dyslexia
- Discalculia = Dyscalculia
- CAA = Comunicazione Aumentativa e Alternativa (Augmentative and Alternative Communication)
