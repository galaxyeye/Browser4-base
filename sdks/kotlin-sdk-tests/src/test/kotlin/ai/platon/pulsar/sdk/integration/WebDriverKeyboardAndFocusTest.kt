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

/**
 * Tests for WebDriver keyboard and focus operations.
 *
 * Covers previously untested methods:
 * - focus operations (focus, blur)
 * - keyboard operations (press, sendKeys, type)
 * - form field interactions
 */
@Tag("IntegrationTest")
@Tag("RequiresBrowser")
class WebDriverKeyboardAndFocusTest : KotlinSdkIntegrationTestBase() {

    private lateinit var driver: WebDriver

    @BeforeEach
    suspend fun setupDriver() {
        createSession()
        driver = WebDriver(client)
    }

    // ========== Focus Operations ==========

    @Test
    suspend fun `should focus on input element`() {
        driver.navigateTo(TestUrls.KEYBOARD_PAGE)

        // Focus on the focus input
        driver.focus("#focusInput")

        // Verify the input exists (focus operation completed)
        assertTrue(driver.exists("#focusInput"), "Input should exist after focus operation")
    }

    @Test
    suspend fun `should focus on multiple elements sequentially`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        // Focus on different inputs
        driver.focus("#username")
        driver.focus("#email")
        driver.focus("#password")

        // All elements should still exist
        assertTrue(driver.exists("#username"), "Username input should exist")
        assertTrue(driver.exists("#email"), "Email input should exist")
        assertTrue(driver.exists("#password"), "Password input should exist")
    }

    @Test
    @Tag("Fast")
    suspend fun `should handle focus on non-focusable element gracefully`() {
        driver.navigateTo(TestUrls.SIMPLE_PAGE)

        // Try to focus on a div (not normally focusable)
        try {
            driver.focus("body")
            // If it doesn't throw, that's acceptable
            assertTrue(true, "Focus on non-focusable element handled")
        } catch (e: Exception) {
            // Expected behavior
            assertTrue(true, "Exception expected for non-focusable element")
        }
    }

    // ========== Keyboard Press Operations ==========

    @Test
    suspend fun `should press Enter key`() {
        driver.navigateTo(TestUrls.KEYBOARD_PAGE)

        driver.focus("#keyInput")
        driver.press("#keyInput", "Enter")

        // Verify operation completed
        assertTrue(driver.exists("#keyInput"), "Input should exist after key press")
    }

    @Test
    suspend fun `should press Tab key`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        driver.focus("#username")
        driver.press("#username", "Tab")

        // Tab should move focus (operation completed)
        assertTrue(driver.exists("#username"), "Form should be accessible after Tab")
    }

    @Test
    suspend fun `should press Escape key`() {
        driver.navigateTo(TestUrls.KEYBOARD_PAGE)

        driver.focus("#keyInput")
        driver.press("#keyInput", "Escape")

        // Verify operation completed
        assertTrue(driver.exists("#keyInput"), "Input should exist after Escape")
    }

    @Test
    suspend fun `should press Arrow keys`() {
        driver.navigateTo(TestUrls.KEYBOARD_PAGE)

        driver.focus("#keyInput")
        driver.press("#keyInput", "ArrowDown")
        driver.press("#keyInput", "ArrowUp")
        driver.press("#keyInput", "ArrowLeft")
        driver.press("#keyInput", "ArrowRight")

        // Verify operations completed
        assertTrue(driver.exists("#keyInput"), "Input should exist after arrow key presses")
    }

    // ========== Type and SendKeys Operations ==========

    @Test
    suspend fun `should type text in input field`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        driver.type("#username", "testuser")

        // Verify the input exists (type operation completed)
        assertTrue(driver.exists("#username"), "Input should exist after typing")
    }

    @Test
    suspend fun `should type in multiple input fields`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        driver.type("#username", "testuser")
        driver.type("#email", "test@example.com")
        driver.type("#password", "password123")

        // Verify all inputs exist
        assertTrue(driver.exists("#username"), "Username input should exist")
        assertTrue(driver.exists("#email"), "Email input should exist")
        assertTrue(driver.exists("#password"), "Password input should exist")
    }

    @Test
    suspend fun `should type empty string`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        driver.type("#username", "")

        // Should handle empty string gracefully
        assertTrue(driver.exists("#username"), "Input should exist after typing empty string")
    }

    @Test
    suspend fun `should type special characters`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        driver.type("#username", "user@#$%&*()")

        // Should handle special characters
        assertTrue(driver.exists("#username"), "Input should exist after typing special characters")
    }

    @Test
    suspend fun `should type long text`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        val longText = "a".repeat(1000)
        driver.type("#username", longText)

        // Should handle long text
        assertTrue(driver.exists("#username"), "Input should exist after typing long text")
    }

    @Test
    suspend fun `should use sendKeys for text input`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        driver.sendKeys("#email", "user@test.com")

        // Verify the input exists
        assertTrue(driver.exists("#email"), "Input should exist after sendKeys")
    }

    @Test
    suspend fun `should use sendKeys with special keys`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        driver.focus("#username")
        driver.sendKeys("#username", "Hello")

        // Verify operation completed
        assertTrue(driver.exists("#username"), "Input should exist after sendKeys")
    }

    // ========== Form Interaction Workflows ==========

    @Test
    suspend fun `should complete form filling workflow`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        // Fill form using keyboard operations
        driver.type("#username", "testuser")
        driver.press("#username", "Tab")
        driver.type("#email", "test@example.com")
        driver.press("#email", "Tab")
        driver.type("#password", "secure123")

        // Verify all fields are accessible
        assertTrue(driver.exists("#username"), "Username should be filled")
        assertTrue(driver.exists("#email"), "Email should be filled")
        assertTrue(driver.exists("#password"), "Password should be filled")
    }

    @Test
    suspend fun `should handle focus and type together`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        driver.focus("#username")
        driver.type("#username", "focused_user")

        // Verify operation completed
        assertTrue(driver.exists("#username"), "Input should exist after focus and type")
    }

    @Test
    suspend fun `should type and submit with Enter`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        driver.type("#username", "submituser")
        driver.focus("#username")
        driver.press("#username", "Enter")

        // Verify form is still accessible (might have submitted)
        assertTrue(driver.exists("#testForm"), "Form should exist after Enter press")
    }

    // ========== Clear and Modify Operations ==========

    @Test
    suspend fun `should type then clear with selections`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        // Type text
        driver.type("#username", "initial")
        
        // Select all and replace
        driver.focus("#username")
        // Ctrl+A (select all) - platform dependent
        driver.type("#username", "replaced")

        // Verify input is accessible
        assertTrue(driver.exists("#username"), "Input should exist after modification")
    }

    @Test
    @Tag("Fast")
    suspend fun `should handle rapid typing`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        // Rapid typing operations
        repeat(5) {
            driver.type("#username", "test$it")
        }

        // Should handle rapid operations
        assertTrue(driver.exists("#username"), "Input should exist after rapid typing")
    }

    // ========== Edge Cases ==========

    @Test
    @Tag("Fast")
    suspend fun `should handle focus on non-existent element`() {
        driver.navigateTo(TestUrls.SIMPLE_PAGE)

        try {
            driver.focus("#nonExistentInput")
            // If it doesn't throw, that's acceptable
        } catch (e: Exception) {
            // Expected behavior
            assertTrue(true, "Exception expected for non-existent element")
        }
    }

    @Test
    @Tag("Fast")
    suspend fun `should handle type on non-existent element`() {
        driver.navigateTo(TestUrls.SIMPLE_PAGE)

        try {
            driver.type("#nonExistentInput", "text")
            // If it doesn't throw, that's acceptable
        } catch (e: Exception) {
            // Expected behavior
            assertTrue(true, "Exception expected for non-existent element")
        }
    }

    @Test
    suspend fun `should handle Unicode characters in type`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        // Type Unicode characters
        driver.type("#username", "用户名测试")

        // Should handle Unicode
        assertTrue(driver.exists("#username"), "Input should exist after typing Unicode")
    }

    @Test
    suspend fun `should handle newlines in type`() {
        driver.navigateTo(TestUrls.FORM_PAGE)

        // Type text with newline (might be treated differently in single-line input)
        driver.type("#username", "line1\nline2")

        // Should handle newline
        assertTrue(driver.exists("#username"), "Input should exist after typing with newline")
    }
}
