package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.exceptions.InvalidFormChangesException;

/**
 * Unit tests for {@link MapUtilities}.
 * Covers getDouble and getBoolean with various types, null handling, and edge cases.
 * @date 27/08/2026
 */
@DisplayName("MapUtilities Tests")
class MapUtilitiesTest {

    // ========== getDouble ==========

    @Test
    @DisplayName("getDouble should extract double from Number value")
    void getDouble_withNumber() {
        Map<String, Object> map = Map.of("key", 123.45);
        assertEquals(123.45, MapUtilities.getDouble(map, "key", 0.0));
    }

    @Test
    @DisplayName("getDouble should extract double from Integer value")
    void getDouble_withInteger() {
        Map<String, Object> map = Map.of("key", 100);
        assertEquals(100.0, MapUtilities.getDouble(map, "key", 0.0));
    }

    @Test
    @DisplayName("getDouble should parse double from String value")
    void getDouble_withString() {
        Map<String, Object> map = Map.of("key", "123.45");
        assertEquals(123.45, MapUtilities.getDouble(map, "key", 0.0));
    }

    @Test
    @DisplayName("getDouble should throw InvalidFormChangesException for unparseable String")
    void getDouble_withUnparseableString() {
        Map<String, Object> map = Map.of("key", "abc");
        assertThrows(InvalidFormChangesException.class,
                () -> MapUtilities.getDouble(map, "key", 0.0));
    }

    @Test
    @DisplayName("getDouble should return default for null map")
    void getDouble_nullMap() {
        assertEquals(99.0, MapUtilities.getDouble(null, "key", 99.0));
    }

    @Test
    @DisplayName("getDouble should return default for missing key")
    void getDouble_missingKey() {
        Map<String, Object> map = Map.of("other", 100);
        assertEquals(99.0, MapUtilities.getDouble(map, "key", 99.0));
    }

    @Test
    @DisplayName("getDouble should return default for null value")
    void getDouble_nullValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", null);
        assertEquals(99.0, MapUtilities.getDouble(map, "key", 99.0));
    }

    // ========== getBoolean ==========

    @Test
    @DisplayName("getBoolean should extract Boolean.TRUE")
    void getBoolean_withBoolean() {
        Map<String, Object> map = Map.of("key", Boolean.TRUE);
        assertTrue(MapUtilities.getBoolean(map, "key", false));
    }

    @Test
    @DisplayName("getBoolean should parse 'true' string")
    void getBoolean_withString_true() {
        Map<String, Object> map = Map.of("key", "true");
        assertTrue(MapUtilities.getBoolean(map, "key", false));
    }

    @Test
    @DisplayName("getBoolean should parse 'TRUE' string (case insensitive)")
    void getBoolean_withString_TRUE() {
        Map<String, Object> map = Map.of("key", "TRUE");
        assertTrue(MapUtilities.getBoolean(map, "key", false));
    }

    @Test
    @DisplayName("getBoolean should parse 'si' string")
    void getBoolean_withString_si() {
        Map<String, Object> map = Map.of("key", "si");
        assertTrue(MapUtilities.getBoolean(map, "key", false));
    }

    @Test
    @DisplayName("getBoolean should parse 'Si' string (case insensitive)")
    void getBoolean_withString_Si() {
        Map<String, Object> map = Map.of("key", "Si");
        assertTrue(MapUtilities.getBoolean(map, "key", false));
    }

    @Test
    @DisplayName("getBoolean should parse 'yes' string")
    void getBoolean_withString_yes() {
        Map<String, Object> map = Map.of("key", "yes");
        assertTrue(MapUtilities.getBoolean(map, "key", false));
    }

    @Test
    @DisplayName("getBoolean should return false for 'no' string")
    void getBoolean_withString_no() {
        Map<String, Object> map = Map.of("key", "no");
        assertFalse(MapUtilities.getBoolean(map, "key", true));
    }

    @Test
    @DisplayName("getBoolean should return default for null map")
    void getBoolean_nullMap() {
        assertTrue(MapUtilities.getBoolean(null, "key", true));
    }

    @Test
    @DisplayName("getBoolean should return default for null value")
    void getBoolean_nullValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", null);
        assertTrue(MapUtilities.getBoolean(map, "key", true));
    }

    @Test
    @DisplayName("getBoolean should return default for non-String/non-Boolean type")
    void getBoolean_otherType() {
        Map<String, Object> map = Map.of("key", 123);
        assertFalse(MapUtilities.getBoolean(map, "key", false));
    }

    // ========== Non-instantiability ==========

    @Test
    @DisplayName("Should not be instantiable - utility class")
    void shouldNotBeInstantiable() throws Exception {
        var constructor = MapUtilities.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(java.lang.reflect.InvocationTargetException.class, constructor::newInstance);
    }
}
