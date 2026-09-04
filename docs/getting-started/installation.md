# Installation and Dependency Setup

This document details the exact requirements and procedure for installing NodeForge both as an end-user runtime mod and as a compiled dependency within a Fabric development environment.

---

## 1. System Requirements

Before integrating NodeForge, ensure your environment satisfies the following platform prerequisites:

| Component | Minimum Required Version | Notes |
| :--- | :--- | :--- |
| **Java Runtime (JDK / JRE)** | **Java 21** | NodeForge utilizes Java 21 language features including pattern matching for switch, record patterns, and sequenced collections. |
| **Minecraft** | **1.21.11** | Compatible with the Minecraft 1.21.11 release cycle. |
| **Fabric Loader** | **>= 0.19.3** | Required for modern split-environment source set separation and entrypoint resolution. |
| **Fabric API** | **0.141.6+1.21.11** | Required for resource reload listener integration and networking hooks. |

---

## 2. End-User Installation (Players & Server Administrators)

When operating a Minecraft client or dedicated server that includes mods built on NodeForge, NodeForge must be installed into the standard mods folder:

1. Download the production binary: `nodeforge-1.0.0.jar`.
2. Place the file into the runtime mods directory:
   - Client: `.minecraft/mods/nodeforge-1.0.0.jar`
   - Dedicated Server: `server_root/mods/nodeforge-1.0.0.jar`
3. Verify that Fabric Loader and Fabric API are also present in the `mods/` directory.

---

## 3. Developer Dependency Setup (Mod Developers)

Mod developers depend on NodeForge as a library through Gradle. Because NodeForge operates within Fabric Loom, it must be declared using `modImplementation` so that Loom appropriately remaps and links its symbols during compilation and test runtime.

### 3.1 Declaring the Repository

Add the repository hosting NodeForge artifacts to your project's `build.gradle`:

```groovy
repositories {
    mavenCentral()
    
    // NodeForge Official Maven
    maven {
        name = "NodeForge Maven"
        url = "https://maven.minex.net/releases"
    }

    // Alternatively, Modrinth Maven if pulling published releases
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
    }
}
```

### 3.2 Declaring the Dependency

In the `dependencies` block of `build.gradle`, add the `modImplementation` directive:

```groovy
dependencies {
    minecraft "com.mojang:minecraft:1.21.11"
    mappings "net.fabricmc:yarn:1.21.11+build.6:v2"
    modImplementation "net.fabricmc:fabric-loader:0.19.3"
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.141.6+1.21.11"

    // NodeForge Framework Dependency
    modImplementation "net.minex.nodeforge:nodeforge:1.0.0"

    // Optional: Jar-in-Jar (JiJ) packaging
    // If you wish to bundle NodeForge directly inside your mod's distribution jar,
    // uncomment the following line:
    // include "net.minex.nodeforge:nodeforge:1.0.0"
}
```

> [!NOTE]
> **Jar-in-Jar (JiJ) vs External Dependency**:
> Using `include` packages NodeForge directly inside your compiled JAR file, relieving end-users from downloading NodeForge separately. However, if multiple installed mods bundle differing versions of NodeForge, Fabric Loader will select the highest semver release. If you do not bundle it, declare NodeForge as an explicit dependency in your `fabric.mod.json`.

---

## 4. Local Development / Flat Directory Dependency

If you are developing a downstream mod concurrently against a local build of NodeForge without publishing to an external Maven server, you can link the compiled JAR directly via Gradle's `flatDir`:

```groovy
repositories {
    flatDir {
        dirs "libs"
    }
}

dependencies {
    modImplementation name: "nodeforge-1.0.0"
}
```

Place the compiled `nodeforge-1.0.0.jar` into the `libs/` directory located at your project root, then reload Gradle.

---

## 5. Next Steps

Once dependencies are resolved:
- Proceed to [Project Setup](project-setup.md) to configure your `fabric.mod.json` and split-environment structure.
- Read [Core Concepts](concepts.md) to understand the fundamental mechanics of graphs, nodes, and ports.
