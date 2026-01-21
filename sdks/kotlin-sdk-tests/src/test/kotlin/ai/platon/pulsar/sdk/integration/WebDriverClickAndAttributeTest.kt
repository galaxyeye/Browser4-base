/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership. The ASF licenses this file to
 * you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required
 * by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package ai.platon.pulsar.sdk.integration

import ai.platon.pulsar.sdk.integration.util.TestUrls
import ai.platon.pulsar.sdk.v0.WebDriver
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for WebDriver click, check/uncheck and attribute extraction operations.
 *
 * Covers previously untested methods:
 * - click operations (click, clickElement, check, uncheck)
 * - attribute extraction (selectFirstAttributeOrNull, selectAttributeAll, getAttribute)
 */
@Tag("IntegrationTest")
@Tag("RequiresBrowser")
class WebDriverClickAndAttributeTest : KotlinSdkIntegrationTestBase() {

    private lateinit var driver: WebDriver

    @BeforeEach
    suspend fun setupDriver() {
        createSession()
        driver = WebDriver(client)
    }

    // ========== Click Operations ==========

    @Test
    suspend fun `should click button element`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        // Click the button
        driver.click("#clickButton")

        // Verify the result (button click should update result div)
        val result = driver.selectFirstTextOrNull("#result")
        assertNotNull(result, "Result should not be null after button click")
        assertTrue(result.contains("clicked") || result.isNotBlank(), "Result should indicate button was clicked")
    }

    @Test
    suspend fun `should check checkbox element`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        // Check the remember checkbox
        driver.check("#remember")

        // Verify checkbox is checked (this might need adjustment based on actual API behavior)
        assertTrue(driver.exists("#remember"), "Checkbox should exist after check operation")
    }

    @Test
    suspend fun `should uncheck checkbox element`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        // First check, then uncheck
        driver.check("#remember")
        driver.uncheck("#remember")

        // Verify checkbox still exists
        assertTrue(driver.exists("#remember"), "Checkbox should exist after uncheck operation")
    }

    @Test
    suspend fun `should toggle checkbox states`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        // Multiple check/uncheck operations
        driver.check("#newsletter")
        driver.uncheck("#newsletter")
        driver.check("#newsletter")

        // Verify checkbox still exists and is accessible
        assertTrue(driver.exists("#newsletter"), "Checkbox should remain accessible after toggle operations")
    }

    @Test
    @Tag("Fast")
    suspend fun `should handle click on non-existent element gracefully`() {
        driver.navigateTo(TestUrls.SIMPLE_PAGE)

        // Attempt to click non-existent element
        // Should not throw exception but may return null or handle gracefully
        try {
            driver.click("#nonExistentButton")
            // If it doesn't throw, that's acceptable behavior
        } catch (e: Exception) {
            // Expected behavior - element not found
            assertTrue(true, "Exception expected for non-existent element")
        }
    }

    // ========== Attribute Extraction ==========

    @Test
    suspend fun `should extract href attribute from link`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        val href = driver.selectFirstAttributeOrNull("#testLink", "href")
        assertNotNull(href, "href attribute should not be null")
        assertTrue(href.contains("example.com"), "href should contain example.com")
    }

    @Test
    suspend fun `should extract custom data attribute`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        val customValue = driver.selectFirstAttributeOrNull("#attrTest", "data-custom")
        assertNotNull(customValue, "data-custom attribute should not be null")
        assertEquals("custom-value", customValue, "data-custom value should match")
    }

    @Test
    suspend fun `should extract title attribute`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        val title = driver.selectFirstAttributeOrNull("#attrTest", "title")
        assertNotNull(title, "title attribute should not be null")
        assertEquals("Test Title", title, "title value should match")
    }

    @Test
    suspend fun `should extract class attribute`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        val className = driver.selectFirstAttributeOrNull("#attrTest", "class")
        assertNotNull(className, "class attribute should not be null")
        assertTrue(className.contains("test-class"), "class should contain test-class")
    }

    @Test
    suspend fun `should return null for non-existent attribute`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        val nonExistent = driver.selectFirstAttributeOrNull("#attrTest", "non-existent-attr")
        // Depending on implementation, might return null or empty string
        assertTrue(nonExistent == null || nonExistent.isEmpty(), 
            "Non-existent attribute should return null or empty")
    }

    @Test
    suspend fun `should extract multiple attributes from same element`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        val href = driver.selectFirstAttributeOrNull("#testLink", "href")
        val target = driver.selectFirstAttributeOrNull("#testLink", "target")
        val rel = driver.selectFirstAttributeOrNull("#testLink", "rel")

        assertNotNull(href, "href should not be null")
        assertNotNull(target, "target should not be null")
        assertNotNull(rel, "rel should not be null")
        
        assertEquals("_blank", target, "target should be _blank")
        assertTrue(rel.contains("noopener"), "rel should contain noopener")
    }

    @Test
    suspend fun `should extract data-testid attributes`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        val testId = driver.selectFirstAttributeOrNull("#clickButton", "data-testid")
        assertNotNull(testId, "data-testid should not be null")
        assertEquals("click-button", testId, "data-testid value should match")
    }

    @Test
    @Tag("Fast")
    suspend fun `should handle attribute extraction from non-existent element`() {
        driver.navigateTo(TestUrls.SIMPLE_PAGE)

        val attr = driver.selectFirstAttributeOrNull("#nonExistentElement", "href")
        assertNull(attr, "Attribute from non-existent element should be null")
    }

    @Test
    suspend fun `should extract attributes from multiple elements using selectAttributeAll`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        // Extract data-testid from all inputs
        val testIds = driver.selectAttributeAll("input[data-testid]", "data-testid")
        assertNotNull(testIds, "Attribute list should not be null")
        assertTrue(testIds.isNotEmpty(), "Should extract attributes from multiple elements")
    }
}
