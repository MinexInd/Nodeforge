package net.minex.nodeforge.api.registry;

import net.minecraft.util.Identifier;
import net.minex.nodeforge.core.graph.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraphRegistry & Concurrency")
class GraphRegistryTest {

    private GraphRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new GraphRegistry();
    }

    @Test
    @DisplayName("registers, queries, and unregisters graphs by string and Identifier")
    void registerAndQuery() {
        Graph g1 = new Graph("graph_1");
        Graph g2 = new Graph("graph_2");

        assertNull(registry.get("graph_1"));
        assertFalse(registry.has("graph_1"));

        registry.register("mod:g1", g1);
        Identifier g2Id = Identifier.of("mod", "g2");
        registry.register(g2Id, g2);

        assertEquals(2, registry.size());
        assertTrue(registry.has("mod:g1"));
        assertTrue(registry.has(g2Id));
        assertSame(g1, registry.get("mod:g1"));
        assertSame(g2, registry.get(g2Id));

        // Unregister
        assertSame(g1, registry.unregister("mod:g1"));
        assertEquals(1, registry.size());
        assertFalse(registry.has("mod:g1"));

        // Clear
        registry.clear();
        assertEquals(0, registry.size());
    }

    @Test
    @DisplayName("rejects null or blank identifiers")
    void rejectsInvalidIds() {
        Graph g = new Graph("valid");
        assertThrows(NullPointerException.class, () -> registry.register((String) null, g));
        assertThrows(NullPointerException.class, () -> registry.register((Identifier) null, g));
        assertThrows(NullPointerException.class, () -> registry.register("valid", null));
        assertThrows(IllegalArgumentException.class, () -> registry.register("   ", g));
    }

    @Test
    @DisplayName("thread-safe concurrent registration")
    void concurrentRegistration() throws InterruptedException {
        int threads = 20;
        int perThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger counter = new AtomicInteger();

        for (int t = 0; t < threads; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < perThread; i++) {
                        int idx = counter.incrementAndGet();
                        registry.register("graph_" + idx, new Graph("g" + idx));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();
        assertEquals(threads * perThread, registry.size());
    }
}
