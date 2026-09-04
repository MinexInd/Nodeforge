# Your First Graph Tutorial

This tutorial guides you through building, connecting, executing, and visually inspecting your first NodeForge graph in under ten minutes.

---

## 1. Goal

We will construct a mathematical expression graph representing:

$$\text{result} = (A + B) \times C$$

Where $A = 10.0$, $B = 20.0$, and $C = 3.0$. The final expected output is $90.0$.

```text
[Input A: 10.0] ──► [Port a]
                    [Math Add] ──► [Sum: 30.0] ──► [Port a]
[Input B: 20.0] ──► [Port b]                       [Math Multiply] ──► [Result: 90.0]
                                                   [Port b]
[Input C: 3.0]  ─────────────────────────────────► 
```

---

## 2. Server/Common Code: Constructing and Executing the Graph

The following code is **100% server-safe** and can execute in common code, unit tests, or headless dedicated servers without graphics drivers.

```java
package com.example.mymod;

import net.minex.nodeforge.api.execution.ExecutionContext;
import net.minex.nodeforge.api.execution.ExecutionSummary;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.api.port.BuiltinPortTypes;
import net.minex.nodeforge.core.execution.GraphEvaluator;
import net.minex.nodeforge.core.graph.ConnectionResult;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.NodeId;
import net.minex.nodeforge.core.id.PortId;

public class FirstGraphExample {

    public static void main(String[] args) {
        // 1. Instantiate a new Graph container
        Graph graph = new Graph("arithmetic_demo");

        // 2. Create the Addition Node
        Node addNode = Node.builder(NodeId.of("add_node"), "math:add")
                .displayName("Add")
                .position(new Position(200.0, 100.0))
                .inputPort("a", "A", BuiltinPortTypes.DOUBLE)
                .inputPort("b", "B", BuiltinPortTypes.DOUBLE)
                .outputPort("sum", "Sum", BuiltinPortTypes.DOUBLE)
                .build();
        graph.addNode(addNode);

        // 3. Create the Multiplication Node
        Node multiplyNode = Node.builder(NodeId.of("mult_node"), "math:multiply")
                .displayName("Multiply")
                .position(new Position(420.0, 120.0))
                .inputPort("a", "A", BuiltinPortTypes.DOUBLE)
                .inputPort("b", "B", BuiltinPortTypes.DOUBLE)
                .outputPort("result", "Result", BuiltinPortTypes.DOUBLE)
                .build();
        graph.addNode(multiplyNode);

        // 4. Wire the Output of Addition into Input 'a' of Multiplication
        ConnectionResult wireResult = graph.connect(
                addNode.id(), PortId.of("sum"),
                multiplyNode.id(), PortId.of("a")
        );

        if (!wireResult.isSuccess()) {
            throw new IllegalStateException("Failed to wire nodes: " + wireResult.errorMessage());
        }

        // 5. Setup Execution Context and Feed Initial Values
        ExecutionContext context = new ExecutionContext();

        // Supply inputs for Addition: a = 10.0, b = 20.0
        context.setInputValue(addNode.id(), PortId.of("a"), 10.0);
        context.setInputValue(addNode.id(), PortId.of("b"), 20.0);

        // Supply input 'b' for Multiplication: b = 3.0
        context.setInputValue(multiplyNode.id(), PortId.of("b"), 3.0);

        // 6. Evaluate the Graph
        GraphEvaluator evaluator = new GraphEvaluator();
        ExecutionSummary summary = evaluator.evaluateDataFlow(graph, context);

        // 7. Verify Results
        if (summary.isSuccess()) {
            Double finalOutput = (Double) context.getOutputValue(multiplyNode.id(), PortId.of("result"));
            System.out.println("Execution succeeded in " + summary.durationMillis() + " ms");
            System.out.println("Steps executed: " + summary.stepsExecuted());
            System.out.println("Final Result: " + finalOutput); // Prints 90.0
        } else {
            System.err.println("Execution failed: " + summary.errorMessage().orElse("Unknown error"));
        }
    }
}
```

---

## 3. Client-Only Code: Opening the Visual Editor

If you want the player to see and interact with this graph on a graphical canvas, open the `NodeEditorScreen`.

> [!IMPORTANT]
> **Client-Only Code**:
> The following snippet must reside in your `src/client/java` source set. Never invoke `MinecraftClient` or `NodeEditorScreen` from common or server code.

```java
package com.example.mymod.client;

import com.example.mymod.FirstGraphExample;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minex.nodeforge.client.editor.NodeEditorScreen;
import net.minex.nodeforge.core.graph.Graph;

public class EditorLauncher {

    /**
     * Opens the NodeForge visual editor on the active Minecraft client.
     */
    public static void openEditor(Graph graph) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(new NodeEditorScreen(Text.literal("Expression Editor"), graph));
    }
}
```

When opened:
- The addition and multiplication nodes will appear at coordinates $(200, 100)$ and $(420, 120)$.
- A smooth cubic Bézier cable connects the `sum` socket to the `a` socket.
- You can pan the camera by dragging the background with Middle-Click or Left-Click.
- You can zoom by scrolling the mouse wheel.
- Press `T` to cycle visual color themes.
- Press `H` to toggle the diagnostics telemetry HUD.

---

## 4. Next Steps

- Explore the [Graph API Guide](../guides/graphs.md) to learn about querying, deleting, and serializing graphs.
- Learn how to define custom node archetypes in the [Custom Nodes Guide](../customization/custom-nodes.md).
