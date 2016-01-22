package net.jawr.web.resource.bundle.generator;

import org.junit.Test;

import static org.junit.Assert.*;

public class GeneratorMappingHelperTest {

    @Test
    public void js() {
        final String resource = "path/to/foo.js";
        final GeneratorMappingHelper helper = new GeneratorMappingHelper(resource);
        assertEquals(resource, helper.getPath());
        assertNull(helper.getBracketsParam());
        assertNull(helper.getParenthesesParam());
    }

    @Test
    public void css() {
        final String resource = "path/to/foo.css";
        final GeneratorMappingHelper helper = new GeneratorMappingHelper(resource);
        assertEquals(resource, helper.getPath());
        assertNull(helper.getBracketsParam());
        assertNull(helper.getParenthesesParam());
    }

    @Test
    public void wildcard() {
        final String resource = "path/to/*";
        final GeneratorMappingHelper helper = new GeneratorMappingHelper(resource);
        assertEquals(resource, helper.getPath());
        assertNull(helper.getBracketsParam());
        assertNull(helper.getParenthesesParam());
    }

    @Test
    public void withParentheses() {
        final String resource = "path/to/foo(bar).js";
        final GeneratorMappingHelper helper = new GeneratorMappingHelper(resource);
        assertEquals("path/to/foo.js", helper.getPath());
        assertEquals("bar", helper.getParenthesesParam());
        assertNull(helper.getBracketsParam());
    }

    @Test
    public void withEmptyParentheses() {
        final String resource = "path/to/foo().js";
        final GeneratorMappingHelper helper = new GeneratorMappingHelper(resource);
        assertEquals("path/to/foo.js", helper.getPath());
        assertEquals("", helper.getParenthesesParam());
        assertNull(helper.getBracketsParam());
    }

    @Test
    public void withBrackets() {
        final String resource = "path/to/foo[bar].js";
        final GeneratorMappingHelper helper = new GeneratorMappingHelper(resource);
        assertEquals("path/to/foo.js", helper.getPath());
        assertEquals("bar", helper.getBracketsParam());
        assertNull(helper.getParenthesesParam());
    }

    @Test
    public void withEmptyBrackets() {
        final String resource = "path/to/foo[].js";
        final GeneratorMappingHelper helper = new GeneratorMappingHelper(resource);
        assertEquals("path/to/foo.js", helper.getPath());
        assertEquals("", helper.getBracketsParam());
        assertNull(helper.getParenthesesParam());
    }

}
