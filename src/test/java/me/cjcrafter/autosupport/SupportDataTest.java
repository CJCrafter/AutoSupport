package me.cjcrafter.autosupport;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SupportDataTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void test_defaults() {
        Map json = new HashMap();
        json.put("Keys", new ArrayList<>());
        SupportData data = new SupportData(json);

        assertEquals(Activator.QUESTION, data.getActivator());
        assertFalse(data.isOnlyUnverified());
        assertTrue(data.isDeleteAfterAnswer());
        assertEquals(1, data.getKeyThreshold());
        assertNull(data.getQuestion());
        assertNull(data.getAnswer());
        assertNull(data.getMedia());
        assertNull(data.getButton());
    }
}
