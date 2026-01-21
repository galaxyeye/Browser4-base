package ai.platon.pulsar.persist.metadata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiMetadataTest {

    @Test
    void testNamesIsDefensiveCopy() {
        MultiMetadata m = new MultiMetadata();
        m.put("k1", "v1");
        m.put("k2", "v2");

        var names = m.names();
        assertTrue(names.contains("k1"));
        assertTrue(names.contains("k2"));

        names.remove("k1");
        // should not affect underlying multimap
        assertTrue(m.has("k1"));
    }

    @Test
    void testGetBooleanDefaultOverloads() {
        MultiMetadata m = new MultiMetadata();

        assertTrue(m.getBoolean("missing", true));
        assertFalse(m.getBoolean("missing", Boolean.FALSE));

        m.put("flag", "true");
        assertTrue(m.getBoolean("flag", false));
    }

    @Test
    void testCopyIsDeepForValues() {
        MultiMetadata m = new MultiMetadata();
        m.put("k", "v1");
        MultiMetadata c = m.copy();

        c.put("k", "v2");

        assertEquals("v1", m.get("k"));
        assertEquals(2, c.getValues("k").size());
        assertEquals(1, m.getValues("k").size());
    }

    @Test
    void testRemoveReturnsRemovedCount() {
        MultiMetadata m = new MultiMetadata();
        m.put("k", "v1");
        m.put("k", "v2");

        assertEquals(2, m.remove("k"));
        assertFalse(m.has("k"));
    }

    @Test
    void testEqualsAndHashCode() {
        MultiMetadata a = new MultiMetadata();
        MultiMetadata b = new MultiMetadata();

        a.put("k", "v");
        b.put("k", "v");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
