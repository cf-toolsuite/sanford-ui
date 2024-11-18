package org.cftoolsuite.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
public class AppPropertiesTest {

    @Autowired
    private AppProperties appProperties;

    @Test
    void shouldLoadAllFileExtensionPatterns() {
        assertNotNull(appProperties.supportedFileFilters());
        assertEquals(11, appProperties.supportedFileFilters().size()); // Reduced from 14 to 11 after collapsing

        // Verify specific patterns exist
        assertTrue(appProperties.supportedFileFilters().containsKey("pdf"));
        assertTrue(appProperties.supportedFileFilters().containsKey("doc")); // Now handles both doc and docx
        assertFalse(appProperties.supportedFileFilters().containsKey("docx")); // Should no longer exist as separate key
    }

    @Test
    void shouldMatchCollapsedExtensions() {
        Map<String, String> filters = appProperties.supportedFileFilters();

        // Test HTML extensions
        assertTrue("page.html".matches(filters.get("html")));
        assertTrue("page.htm".matches(filters.get("html")));
        assertTrue("page.HTML".matches(filters.get("html"))); // Case insensitive
        assertTrue("page.HTM".matches(filters.get("html")));

        // Test DOC extensions
        assertTrue("document.doc".matches(filters.get("doc")));
        assertTrue("document.docx".matches(filters.get("doc")));
        assertTrue("document.DOC".matches(filters.get("doc")));
        assertTrue("document.DOCX".matches(filters.get("doc")));

        // Test PPT extensions
        assertTrue("presentation.ppt".matches(filters.get("ppt")));
        assertTrue("presentation.pptx".matches(filters.get("ppt")));
        assertTrue("presentation.PPT".matches(filters.get("ppt")));
        assertTrue("presentation.PPTX".matches(filters.get("ppt")));
    }

    @Test
    void shouldMatchNonCollapsedExtensions() {
        Map<String, String> filters = appProperties.supportedFileFilters();

        // Test regular extensions
        assertTrue("test.pdf".matches(filters.get("pdf")));
        assertTrue("data.CSV".matches(filters.get("csv")));
        assertTrue("notes.MD".matches(filters.get("md")));
        assertTrue("data.JSON".matches(filters.get("json")));
    }

    @Test
    void shouldNotMatchInvalidFileExtensions() {
        Map<String, String> filters = appProperties.supportedFileFilters();

        // Test invalid patterns for collapsed extensions
        assertFalse("test.htmlx".matches(filters.get("html")));
        assertFalse("test.htmx".matches(filters.get("html")));
        assertFalse("test.docs".matches(filters.get("doc")));
        assertFalse("test.docxx".matches(filters.get("doc")));
        assertFalse("test.ppts".matches(filters.get("ppt")));
        assertFalse("test.pptxx".matches(filters.get("ppt")));

        // Test other invalid patterns
        assertFalse("testpdf".matches(filters.get("pdf"))); // Missing dot
        assertFalse("test.txt.pdf".matches(filters.get("txt"))); // Wrong extension position
    }

    @Test
    void shouldHandleEdgeCases() {
        Map<String, String> filters = appProperties.supportedFileFilters();

        // Edge cases
        assertFalse(".html".matches(filters.get("html"))); // Just extension
        assertFalse(".doc".matches(filters.get("doc"))); // Just extension
        assertFalse("".matches(filters.get("ppt"))); // Empty string
        assertTrue("file.with.multiple.dots.docx".matches(filters.get("doc"))); // Multiple dots
        assertTrue("UPPERCASE.PPTX".matches(filters.get("ppt"))); // All caps
        assertTrue("mixed.HtMl".matches(filters.get("html"))); // Mixed case
    }
}
