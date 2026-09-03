package com.sunrisedental.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** The hand-rolled JSON layer (see Json.java's class comment for why it exists) needs its own tests since it has no external test coverage from a library. */
class JsonTest {

    @Test
    void writesAndParsesASimpleObject() {
        Map<String, Object> obj = Json.obj("username", "reception1", "attempts", 3.0, "active", true);
        String text = Json.write(obj);
        Map<String, Object> parsed = Json.parseObject(text);

        assertEquals("reception1", parsed.get("username"));
        assertEquals(3.0, parsed.get("attempts"));
        assertEquals(true, parsed.get("active"));
    }

    @Test
    void escapesSpecialCharactersInStrings() {
        Map<String, Object> obj = Json.obj("note", "Patient said \"ouch\"\nand left");
        String text = Json.write(obj);
        Map<String, Object> parsed = Json.parseObject(text);
        assertEquals("Patient said \"ouch\"\nand left", parsed.get("note"));
    }

    @Test
    void handlesNestedArraysAndObjects() {
        Map<String, Object> obj = Json.obj("errors", List.of("Contact number is required.", "Patient name is required."));
        String text = Json.write(obj);
        Map<String, Object> parsed = Json.parseObject(text);

        @SuppressWarnings("unchecked")
        List<Object> errors = (List<Object>) parsed.get("errors");
        assertEquals(2, errors.size());
        assertEquals("Patient name is required.", errors.get(1));
    }

    @Test
    void nullValueRoundTrips() {
        Map<String, Object> obj = Json.obj("appointment", null);
        Map<String, Object> parsed = Json.parseObject(Json.write(obj));
        assertTrue(parsed.containsKey("appointment"));
        assertNull(parsed.get("appointment"));
    }
}
