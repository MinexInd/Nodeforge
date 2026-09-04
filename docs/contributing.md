# Contributing to NodeForge

Thank you for contributing to NodeForge. As a shared infrastructure library for the Minecraft modding ecosystem, NodeForge maintains high standards of architectural isolation, backward compatibility, and test coverage.

---

## Core Contribution Philosophy

> **NodeForge: Build Anything.**

Every pull request must adhere to our core principle: **NodeForge is domain-agnostic graph infrastructure.**
* **Never add game-specific logic**: Skill progression, quest objectives, dialogue states, and item registries belong in consumer mods, not in NodeForge core.
* **Preserve the client/common split**: Client-only rendering code must never leak into the common source set.
* **Uphold stability**: Public API contracts in `net.minex.nodeforge.api.*` are governed by Semantic Versioning 2.0.0.

---

## Repository Structure

```text
src/
├── main/java/net/minex/nodeforge/
│   ├── api/                   # Public common APIs (Contracts, Port types, Builders)
│   └── core/                  # Common implementation (Execution, Graph container, Reloader)
│
├── client/java/net/minex/nodeforge/
│   ├── client/api/            # Public client APIs (Theme presets, Renderers, Layers)
│   └── client/                # Client implementation (NodeEditorScreen, Canvas, OpenGL)
│
└── test/java/net/minex/nodeforge/
    ├── api/                   # API contract validation tests
    ├── core/                  # Graph, execution, stress, and serialization tests
    └── client/                # Headless client math and camera projection tests
```

---

## Development Environment Setup

### Prerequisites

* **Java Development Kit (JDK)**: Version 21 (Adoptium Temurin or Microsoft OpenJDK).
* **Git**: Modern git client.

### Building and Testing

Verify your local environment by running the test suite:

```bash
# Set JAVA_HOME if not configured globally
export JAVA_HOME="/path/to/jdk-21"

# Run tests
./gradlew test

# Generate Javadoc documentation
./gradlew javadoc

# Run full project build (includes checks and JAR assembly)
./gradlew build
```

---

## Architectural & Coding Invariants

### 1. Environment Isolation

| Source Set | Allowed Dependencies | Prohibited Dependencies |
| :--- | :--- | :--- |
| `src/main/java` | Common Fabric API, Minecraft Server, Java 21 stdlib | `net.minecraft.client.*`, `net.minex.nodeforge.client.*` |
| `src/client/java` | Minecraft Client, OpenGL/RenderSystem, Common NodeForge | None (Client only) |

Any PR that introduces a client import into `src/main/java` will fail automated continuous integration.

### 2. Defensive Programming & Null Safety

* Validate all public method parameters using `Objects.requireNonNull(param, "param must not be null")`.
* Reject blank string identifiers (`id.isBlank()`).
* Return immutable or defensive unmodifiable views for collections (`Collections.unmodifiableList()`, `Map.copyOf()`).

### 3. Execution Fault Isolation

When implementing or modifying execution engines:
* Never let arbitrary runtime exceptions bubble up and crash caller threads.
* Convert failures into structured `ExecutionResult.Failure` or `ExecutionSummary.failure()`.
* **Always rethrow `VirtualMachineError`** (`OutOfMemoryError`, `StackOverflowError`) to allow catastrophic JVM state to terminate appropriately.

---

## Testing Standards

Every functional change must include corresponding JUnit 5 test coverage:

1. **Unit Tests**: Test the happy path and edge-case behavior of individual components.
2. **Boundary Testing**: Verify behavior with empty graphs, zero steps, extreme floating-point coordinates ($10^{12}$), and null values.
3. **Concurrency**: Verify that concurrent reads and synchronized mutations do not deadlock or throw unexpected `ConcurrentModificationException`.

Run tests locally with Gradle before submitting changes:

```bash
./gradlew test --info
```

---

## Pull Request Checklist

Before submitting your pull request, verify that:

- [ ] `./gradlew test` passes all tests with zero failures.
- [ ] `./gradlew javadoc` completes with zero errors.
- [ ] `./gradlew build` succeeds.
- [ ] No client classes are referenced in `src/main/java`.
- [ ] All new public methods have comprehensive Javadoc comments.
- [ ] Documentation in `docs/` is updated if public APIs or behaviors changed.
- [ ] Zero emojis are used in code comments, commit messages, or documentation files.
