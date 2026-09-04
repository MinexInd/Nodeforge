package net.minex.nodeforge.api.port;

/**
 * Standard built-in {@link PortType} definitions provided by NodeForge out-of-the-box.
 *
 * <p>These cover standard execution flow, core Java primitives, text, and generic wildcard data.
 * Additional domain-specific types (e.g. Minecraft entities, item stacks, block positions)
 * can be registered dynamically using {@link PortTypeRegistry}.
 */
public final class BuiltinPortTypes {

    private BuiltinPortTypes() {
        // static constants container
    }

    /**
     * Execution control flow port type.
     * Used for impulse / signal pins that trigger node execution (e.g. "Trigger", "Completed").
     */
    public static final PortType<Void> EXECUTION = PortType.execution(
            PortTypeId.of("nodeforge:execution"), "Execution", 0xFFEEEEEE);

    /** Boolean truth value port type ({@code true} or {@code false}). */
    public static final PortType<Boolean> BOOLEAN = PortType.builder(
                    PortTypeId.of("nodeforge:boolean"), Boolean.class)
            .displayName("Boolean")
            .color(0xFFE53935)
            .build();

    /** 32-bit signed integer numeric port type. */
    public static final PortType<Integer> INTEGER = PortType.builder(
                    PortTypeId.of("nodeforge:integer"), Integer.class)
            .displayName("Integer")
            .color(0xFF43A047)
            .build();

    /** 64-bit signed long integer numeric port type. */
    public static final PortType<Long> LONG = PortType.builder(
                    PortTypeId.of("nodeforge:long"), Long.class)
            .displayName("Long")
            .color(0xFF2E7D32)
            .build();

    /** 32-bit single-precision floating point numeric port type. */
    public static final PortType<Float> FLOAT = PortType.builder(
                    PortTypeId.of("nodeforge:float"), Float.class)
            .displayName("Float")
            .color(0xFF26C6DA)
            .build();

    /** 64-bit double-precision floating point numeric port type. */
    public static final PortType<Double> DOUBLE = PortType.builder(
                    PortTypeId.of("nodeforge:double"), Double.class)
            .displayName("Double")
            .color(0xFF1E88E5)
            .build();

    /** Textual string port type. */
    public static final PortType<String> STRING = PortType.builder(
                    PortTypeId.of("nodeforge:string"), String.class)
            .displayName("String")
            .color(0xFF8E24AA)
            .build();

    /** Wildcard / Generic object port type capable of connecting to or from any data type. */
    public static final PortType<Object> ANY = PortType.builder(
                    PortTypeId.of("nodeforge:any"), Object.class)
            .displayName("Any")
            .color(0xFFFFB300)
            .build();

    /**
     * Registers all standard built-in port types to the given registry.
     *
     * @param registry the target port type registry
     */
    public static void registerAll(PortTypeRegistry registry) {
        registry.register(EXECUTION);
        registry.register(BOOLEAN);
        registry.register(INTEGER);
        registry.register(LONG);
        registry.register(FLOAT);
        registry.register(DOUBLE);
        registry.register(STRING);
        registry.register(ANY);
    }
}
