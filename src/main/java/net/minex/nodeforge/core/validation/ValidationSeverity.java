package net.minex.nodeforge.core.validation;

/**
 * The severity of a validation error.
 */
public enum ValidationSeverity {

    /** A critical problem that must be resolved. The graph is in an invalid state. */
    ERROR,

    /** A potential problem that should be reviewed but may be acceptable. */
    WARNING,

    /** An informational observation, not necessarily a problem. */
    INFO
}
