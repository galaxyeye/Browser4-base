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
import ai.platon.pulsar.sdk.v0.PulsarSession
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * PulsarSession integration tests.
 *
 * Tests session functionality including page loading, data extraction,
 * and document parsing.
 */
@Tag("Fast")
class PulsarSessionIntegrationTest : KotlinSdkIntegrationTestBase() {

    private lateinit var session: PulsarSession

    @BeforeEach
    suspend fun setupSession() {
        createSession()
        session = PulsarSession(client)
    }

    @Test
    @DisplayName("should verify session is active")
    fun testShouldVerifySessionIsActive() {
        assertTrue(session.isActive, "Session should be active")
        assertNotNull(session.uuid, "Session UUID should not be null")
        assertTrue(session.uuid.isNotBlank(), "Session UUID should not be blank")
    }

    @Test
    @DisplayName("should normalize URL")
    suspend fun testShouldNormalizeURL() {
        val url = "https://example.com"
        val normalized = session.normalize(url)

        assertNotNull(normalized, "Normalized URL should not be null")
        assertNotNull(normalized.url, "Normalized URL string should not be null")
        assertTrue(normalized.url.isNotBlank(), "Normalized URL should not be blank")
    }

    @Test
    @DisplayName("should normalize URL with arguments")
    suspend fun testShouldNormalizeURLWithArguments() {
        val url = "https://example.com"
        val args = "-expire 1d"
        val normalized = session.normalize(url, args)

        assertNotNull(normalized, "Normalized URL should not be null")
        assertNotNull(normalized.url, "Normalized URL string should not be null")
    }

    @Test
    @DisplayName("should load page")
    suspend fun testShouldLoadPage() {
        val url = TestUrls.SIMPLE_PAGE
        val page = session.load(url)

        assertNotNull(page, "Page should not be null")
        assertFalse(page.isNil, "Page should not be nil")
        assertTrue(page.url.isNotBlank(), "Page URL should not be blank")
    }

    @Test
    @DisplayName("should load page with arguments")
    suspend fun testShouldLoadPageWithArguments() {
        val url = TestUrls.SIMPLE_PAGE
        val args = "-expire 1d"
        val page = session.load(url, args)

        assertNotNull(page, "Page should not be null")
        assertFalse(page.isNil, "Page should not be nil")
    }

    @Test
    @DisplayName("should open page immediately")
    suspend fun testShouldOpenPageImmediately() {
        val url = TestUrls.SIMPLE_PAGE
        val page = session.open(url)

        assertNotNull(page, "Page should not be null")
        assertFalse(page.isNil, "Page should not be nil")
        assertTrue(page.url.isNotBlank(), "Page URL should not be blank")
    }

    @Test
    @DisplayName("should parse page")
    suspend fun testShouldParsePage() {
        val url = TestUrls.SIMPLE_PAGE
        val page = session.load(url)

        val document = session.parse(page)
        assertNotNull(document, "Parsed document should not be null")
    }

    @Test
    @DisplayName("should extract fields from page with selectors")
    suspend fun testShouldExtractFieldsFromPageWithSelectors() {
        val url = TestUrls.PRODUCT_DETAIL
        val page = session.load(url)
        val document = session.parse(page)

        assertNotNull(document, "Document should not be null")

        val selectors = mapOf(
            "title" to "#productTitle",
            "price" to ".a-price-whole"
        )

        val result = session.extract(document, selectors)

        assertNotNull(result, "Extracted result should not be null")
        assertTrue(result.containsKey("title"), "Should have title field")
        assertTrue(result.containsKey("price"), "Should have price field")
    }

    @Test
    @DisplayName("should scrape page with selectors")
    suspend fun testShouldScrapePageWithSelectors() {
        val url = TestUrls.PRODUCT_DETAIL
        val selectors = mapOf(
            "title" to "#productTitle",
            "price" to ".a-price-whole"
        )

        val result = session.scrape(url, "", selectors)

        assertNotNull(result, "Scrape result should not be null")
        assertTrue(result.containsKey("title"), "Should have title field")
        assertTrue(result.containsKey("price"), "Should have price field")
    }

    @Test
    @DisplayName("should scrape page with arguments and selectors")
    suspend fun testShouldScrapePageWithArgumentsAndSelectors() {
        val url = TestUrls.PRODUCT_DETAIL
        val args = "-expire 1d"
        val selectors = mapOf(
            "title" to "#productTitle"
        )

        val result = session.scrape(url, args, selectors)

        assertNotNull(result, "Scrape result should not be null")
        assertTrue(result.containsKey("title"), "Should have title field")
    }

    @Test
    @DisplayName("should load multiple pages")
    suspend fun testShouldLoadMultiplePages() {
        val urls = listOf(
            TestUrls.SIMPLE_PAGE,
            TestUrls.PRODUCT_DETAIL
        )

        val pages = session.loadAll(urls)

        assertNotNull(pages, "Pages should not be null")
        assertEquals(urls.size, pages.size, "Should load all pages")
        pages.forEach { page ->
            assertNotNull(page, "Each page should not be null")
            assertFalse(page.isNil, "Each page should not be nil")
        }
    }

    @Test
    @DisplayName("should load multiple pages with arguments")
    suspend fun testShouldLoadMultiplePagesWithArguments() {
        val urls = listOf(
            TestUrls.SIMPLE_PAGE,
            TestUrls.PRODUCT_LIST
        )
        val args = "-expire 1d"

        val pages = session.loadAll(urls, args)

        assertNotNull(pages, "Pages should not be null")
        assertEquals(urls.size, pages.size, "Should load all pages")
    }

    @Test
    @DisplayName("should submit URL for async processing")
    suspend fun testShouldSubmitURLForAsyncProcessing() {
        val url = TestUrls.SIMPLE_PAGE

        val result = session.submit(url)

        // Submit returns boolean indicating if submission was successful
        assertNotNull(result, "Submit result should not be null")
    }

    @Test
    @DisplayName("should submit multiple URLs")
    suspend fun testShouldSubmitMultipleURLs() {
        val urls = listOf(
            TestUrls.SIMPLE_PAGE,
            TestUrls.PRODUCT_LIST
        )

        val result = session.submitAll(urls)

        assertNotNull(result, "Submit result should not be null")
    }

    @Test
    @DisplayName("should access bound driver")
    suspend fun testShouldAccessBoundDriver() {
        // Access driver through session
        val driver = session.driver

        assertNotNull(driver, "Driver should not be null")
        assertEquals(client, driver.client, "Driver should use same client")
    }

    @Test
    @DisplayName("should handle page with nil status")
    suspend fun testShouldHandlePageWithNilStatus() {
        // Try to load a page that might not exist
        val url = TestUrls.MOCK_SERVER_BASE + "/nonexistent"
        val page = session.load(url)

        assertNotNull(page, "Page object should not be null even if page doesn't exist")
        // Page may be nil or not, depending on server behavior
    }
}
