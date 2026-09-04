package net.minex.nodeforge.core.port;

import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.api.port.PortType;
import net.minex.nodeforge.api.port.PortTypeId;
import net.minex.nodeforge.api.port.PortTypeRegistry;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Engine responsible for evaluating connection compatibility between node ports.
 *
 * <h2>Compatibility Rules</h2>
 * <ol>
 *   <li><strong>Exact Match:</strong> Same {@link PortType} or identical {@code typeKey} strings are compatible.</li>
 *   <li><strong>Execution Isolation:</strong> Execution ports can only connect to execution ports. Data ports can never connect to execution ports.</li>
 *   <li><strong>Wildcard {@code ANY}:</strong> {@link BuiltinPortTypes#ANY} can connect to or from any data port.</li>
 *   <li><strong>Class Assignability:</strong> If target type's Java class is assignable from source type's Java class, they are compatible.</li>
 *   <li><strong>Implicit Numeric Widening:</strong> Standard numeric conversions are permitted (e.g. Integer → Long/Float/Double, Float → Double).</li>
 *   <li><strong>Custom Rules:</strong> Additional compatibility pairs can be registered via {@link #registerCustomCompatibility(PortTypeId, PortTypeId)} or {@link #registerSymmetricCompatibility(PortTypeId, PortTypeId)}.</li>
 * </ol>
 */
public final class TypeCompatibilityEngine {

    private static final Set<TypePair> CUSTOM_COMPATIBILITY = ConcurrentHashMap.newKeySet();

    private record TypePair(PortTypeId source, PortTypeId target) {}

    private TypeCompatibilityEngine() {
        // static utility engine
    }

    /**
     * Evaluates whether data from {@code sourcePort} can safely flow into {@code targetPort}.
     *
     * @param sourcePort the output port producing data
     * @param targetPort the input port consuming data
     * @return a {@link TypeCheckResult} indicating compatibility status and reason
     */
    public static TypeCheckResult checkCompatibility(Port sourcePort, Port targetPort) {
        return checkCompatibility(sourcePort, targetPort, PortTypeRegistry.getInstance());
    }

    /**
     * Evaluates whether data from {@code sourcePort} can safely flow into {@code targetPort}
     * using the specified {@link PortTypeRegistry}.
     *
     * @param sourcePort the output port producing data
     * @param targetPort the input port consuming data
     * @param registry   the registry used to look up port types
     * @return a {@link TypeCheckResult}
     */
    public static TypeCheckResult checkCompatibility(Port sourcePort, Port targetPort, PortTypeRegistry registry) {
        Objects.requireNonNull(sourcePort, "sourcePort must not be null");
        Objects.requireNonNull(targetPort, "targetPort must not be null");
        Objects.requireNonNull(registry, "registry must not be null");

        String srcKey = sourcePort.typeKey();
        String dstKey = targetPort.typeKey();

        // 1. Identical type key string -> immediate match
        if (srcKey.equals(dstKey)) {
            return TypeCheckResult.COMPATIBLE;
        }

        PortType<?> srcType = registry.get(srcKey);
        PortType<?> dstType = registry.get(dstKey);

        // If either type is not registered, fallback to string equality (which failed above)
        if (srcType == null || dstType == null) {
            return TypeCheckResult.incompatible("Unregistered port types are only compatible by exact string match: '"
                    + srcKey + "' vs '" + dstKey + "'");
        }

        // 2. Execution isolation
        if (srcType.isExecution() != dstType.isExecution()) {
            return TypeCheckResult.incompatible(
                    "Cannot connect execution flow port (" + srcKey + ") with data port (" + dstKey + ")");
        }

        // If both are execution types, and keys differed above, check custom rules
        if (srcType.isExecution() && dstType.isExecution()) {
            if (isCustomCompatible(srcType.id(), dstType.id())) {
                return TypeCheckResult.COMPATIBLE;
            }
            return TypeCheckResult.incompatible("Execution types do not match: '" + srcKey + "' vs '" + dstKey + "'");
        }

        // 3. Wildcard ANY compatibility
        if (BuiltinPortTypes.ANY.equals(srcType) || BuiltinPortTypes.ANY.equals(dstType)) {
            return TypeCheckResult.COMPATIBLE;
        }

        // 4. Class assignability (subclass -> superclass)
        if (dstType.typeClass().isAssignableFrom(srcType.typeClass())) {
            return TypeCheckResult.COMPATIBLE;
        }

        // 5. Implicit numeric widening
        if (isNumericWideningCompatible(srcType, dstType)) {
            return TypeCheckResult.COMPATIBLE;
        }

        // 6. Custom registered compatibility pairs
        if (isCustomCompatible(srcType.id(), dstType.id())) {
            return TypeCheckResult.COMPATIBLE;
        }

        return TypeCheckResult.incompatible("Port type '" + srcKey + "' is incompatible with target type '" + dstKey + "'");
    }

    private static boolean isNumericWideningCompatible(PortType<?> src, PortType<?> dst) {
        if (BuiltinPortTypes.INTEGER.equals(src)) {
            return BuiltinPortTypes.LONG.equals(dst)
                    || BuiltinPortTypes.FLOAT.equals(dst)
                    || BuiltinPortTypes.DOUBLE.equals(dst);
        }
        if (BuiltinPortTypes.LONG.equals(src)) {
            return BuiltinPortTypes.DOUBLE.equals(dst);
        }
        if (BuiltinPortTypes.FLOAT.equals(src)) {
            return BuiltinPortTypes.DOUBLE.equals(dst);
        }
        return false;
    }

    /**
     * Registers a directional compatibility rule allowing connections from {@code sourceTypeId} to {@code targetTypeId}.
     *
     * @param sourceTypeId source port type ID
     * @param targetTypeId target port type ID
     */
    public static void registerCustomCompatibility(PortTypeId sourceTypeId, PortTypeId targetTypeId) {
        Objects.requireNonNull(sourceTypeId, "sourceTypeId must not be null");
        Objects.requireNonNull(targetTypeId, "targetTypeId must not be null");
        CUSTOM_COMPATIBILITY.add(new TypePair(sourceTypeId, targetTypeId));
    }

    /**
     * Registers a symmetric compatibility rule allowing connections both from {@code typeA} to {@code typeB}
     * and from {@code typeB} to {@code typeA}.
     *
     * @param typeA first port type ID
     * @param typeB second port type ID
     */
    public static void registerSymmetricCompatibility(PortTypeId typeA, PortTypeId typeB) {
        registerCustomCompatibility(typeA, typeB);
        registerCustomCompatibility(typeB, typeA);
    }

    /**
     * Checks if a custom compatibility rule has been registered between the given types.
     */
    public static boolean isCustomCompatible(PortTypeId sourceTypeId, PortTypeId targetTypeId) {
        return CUSTOM_COMPATIBILITY.contains(new TypePair(sourceTypeId, targetTypeId));
    }

    /**
     * Clears all custom compatibility rules (useful for test isolation).
     */
    public static void clearCustomCompatibility() {
        CUSTOM_COMPATIBILITY.clear();
    }

    /**
     * Result of a type compatibility check.
     */
    public record TypeCheckResult(boolean isCompatible, String reason) {
        public static final TypeCheckResult COMPATIBLE = new TypeCheckResult(true, "Types are compatible");

        public static TypeCheckResult incompatible(String reason) {
            return new TypeCheckResult(false, reason);
        }
    }
}
