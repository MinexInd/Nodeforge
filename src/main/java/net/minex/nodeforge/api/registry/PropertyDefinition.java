package net.minex.nodeforge.api.registry;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Defines a strongly-typed configuration property on a {@link NodeDefinition}.
 *
 * <p>Properties allow nodes to hold editable parameters (such as a constant value,
 * operation mode, duration, or threshold) stored directly inside the node's metadata.
 *
 * @param <T> the Java type of the property value
 */
public final class PropertyDefinition<T> {

    private final String key;
    private final Class<T> valueClass;
    private final String displayName;
    private final String description;
    private final T defaultValue;
    private final Predicate<T> validator;

    private PropertyDefinition(String key, Class<T> valueClass, String displayName,
                               String description, T defaultValue, Predicate<T> validator) {
        this.key = Objects.requireNonNull(key, "property key must not be null");
        if (key.isBlank()) {
            throw new IllegalArgumentException("property key must not be blank");
        }
        this.valueClass = Objects.requireNonNull(valueClass, "valueClass must not be null");
        this.displayName = Objects.requireNonNull(displayName, "displayName must not be null");
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        this.description = description == null ? "" : description;
        this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue must not be null");
        this.validator = validator != null ? validator : v -> true;

        if (!this.validator.test(defaultValue)) {
            throw new IllegalArgumentException("Default value '" + defaultValue + "' fails property validation");
        }
    }

    /** Returns the unique metadata key for this property. */
    public String key() {
        return key;
    }

    /** Returns the Java class of the property value. */
    public Class<T> valueClass() {
        return valueClass;
    }

    /** Returns the human-readable display name. */
    public String displayName() {
        return displayName;
    }

    /** Returns the property description. */
    public String description() {
        return description;
    }

    /** Returns the default value for this property. */
    public T defaultValue() {
        return defaultValue;
    }

    /**
     * Checks if a value satisfies this property's validation rules.
     *
     * @param value the candidate value
     * @return {@code true} if valid
     */
    public boolean isValid(T value) {
        if (value == null) return false;
        return validator.test(value);
    }

    // ========== Static Factories ==========

    /** Creates a boolean toggle property. */
    public static PropertyDefinition<Boolean> booleanProperty(String key, String displayName, boolean defaultValue) {
        return new PropertyDefinition<>(key, Boolean.class, displayName, "", defaultValue, null);
    }

    /** Creates an integer numeric property. */
    public static PropertyDefinition<Integer> intProperty(String key, String displayName, int defaultValue) {
        return new PropertyDefinition<>(key, Integer.class, displayName, "", defaultValue, null);
    }

    /** Creates a bounded integer numeric property within [min, max]. */
    public static PropertyDefinition<Integer> intProperty(String key, String displayName, int defaultValue, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min (" + min + ") must not be greater than max (" + max + ")");
        }
        return new PropertyDefinition<>(key, Integer.class, displayName, "", defaultValue, v -> v >= min && v <= max);
    }

    /** Creates a double-precision floating point property. */
    public static PropertyDefinition<Double> doubleProperty(String key, String displayName, double defaultValue) {
        return new PropertyDefinition<>(key, Double.class, displayName, "", defaultValue, Double::isFinite);
    }

    /** Creates a bounded double property within [min, max]. */
    public static PropertyDefinition<Double> doubleProperty(String key, String displayName, double defaultValue, double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("min (" + min + ") must not be greater than max (" + max + ")");
        }
        return new PropertyDefinition<>(key, Double.class, displayName, "", defaultValue, v -> Double.isFinite(v) && v >= min && v <= max);
    }

    /** Creates a text string property. */
    public static PropertyDefinition<String> stringProperty(String key, String displayName, String defaultValue) {
        return new PropertyDefinition<>(key, String.class, displayName, "", defaultValue, null);
    }

    /** Creates an enumeration dropdown property. */
    public static <E extends Enum<E>> PropertyDefinition<E> enumProperty(String key, String displayName, Class<E> enumClass, E defaultValue) {
        return new PropertyDefinition<>(key, enumClass, displayName, "", defaultValue, null);
    }

    /** Creates a custom property with a validator predicate. */
    public static <T> PropertyDefinition<T> custom(String key, Class<T> valueClass, String displayName,
                                                   T defaultValue, Predicate<T> validator) {
        return new PropertyDefinition<>(key, valueClass, displayName, "", defaultValue, validator);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyDefinition<?> that)) return false;
        return key.equals(that.key) && valueClass.equals(that.valueClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, valueClass);
    }

    @Override
    public String toString() {
        return "PropertyDefinition[" + key + " (" + valueClass.getSimpleName() + ") default=" + defaultValue + "]";
    }
}
