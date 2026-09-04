# Graph Validation

NodeForge includes a validation engine (`GraphValidator`) to audit graph structural integrity, verify port compatibility, and enforce custom business logic before evaluation or persistence.

---

## 1. Running Graph Validation

To validate a graph, invoke `GraphValidator.validate(Graph)`:

```java
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.validation.GraphValidator;
import net.minex.nodeforge.core.validation.ValidationError;
import net.minex.nodeforge.core.validation.ValidationSeverity;

List<ValidationError> errors = GraphValidator.validate(graph);

for (ValidationError err : errors) {
    System.out.println("[" + err.severity() + "] " + err.message() + " (Node: " + err.nodeId() + ")");
}
```

---

## 2. Standard Structural Checks

`GraphValidator` automatically enforces the following baseline invariants:

1. **Dangling Connection Endpoints**: Ensures every connection references valid nodes and ports that currently exist in the graph.
2. **Directional Matching**: Ensures connections link an `OUTPUT` port to an `INPUT` port.
3. **Type Compatibility**: Validates that connection endpoints satisfy `TypeCompatibilityEngine` rules.
4. **Template Conformance**: If a node references a registered `NodeDefinition`, the validator checks that all mandatory ports defined by the template are present.

---

## 3. Defining Custom Validation Rules

Downstream mods can define custom domain rules on their `NodeDefinition` templates via `NodeValidationRule`:

```java
import net.minex.nodeforge.api.registry.NodeDefinition;
import net.minex.nodeforge.core.validation.ValidationError;
import net.minex.nodeforge.core.validation.ValidationSeverity;

NodeDefinition divisionNode = NodeDefinition.builder("math:divide")
        .displayName("Divide")
        .inputPort("numerator", "Numerator", BuiltinPortTypes.DOUBLE)
        .inputPort("denominator", "Denominator", BuiltinPortTypes.DOUBLE)
        .outputPort("quotient", "Quotient", BuiltinPortTypes.DOUBLE)
        // Add custom validation rule: Warn if denominator is hardcoded to zero
        .validationRule((node, graph) -> {
            List<ValidationError> issues = new ArrayList<>();
            Object denomProp = node.getProperty("denominator_constant");
            
            if (denomProp instanceof Double d && d == 0.0) {
                issues.add(ValidationError.nodeError(
                        ValidationSeverity.ERROR,
                        "Division by zero: Denominator property is set to 0.0",
                        node.id()
                ));
            }
            return issues;
        })
        .build();
```

### 3.1 Fault Isolation in Validation
If a custom `NodeValidationRule` throws an unhandled runtime exception:
- The exception is safely intercepted.
- It is converted into a `ValidationError` with `ValidationSeverity.ERROR`.
- The remaining nodes and connections continue to be validated without crashing the validation pass.

---

## 4. Next Steps

- Learn how validated graphs are saved and loaded in the [Resource Persistence Guide](persistence.md).
- Learn how validation feedback is displayed visually in the [Visual Editor Guide](visual-editor.md).
