package ai.platon.pulsar.agentic.tools.executors

import ai.platon.pulsar.agentic.ToolCall
import ai.platon.pulsar.skeleton.crawl.fetch.driver.AbstractBrowser
import ai.platon.pulsar.skeleton.crawl.fetch.driver.AbstractWebDriver
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BrowserToolExecutorTest {

    private lateinit var browser: AbstractBrowser
    private lateinit var executor: BrowserToolExecutor

    @BeforeEach
    fun setUp() {
        browser = mockk(relaxed = true)
        executor = BrowserToolExecutor()
    }

    @Test
    fun `help returns available methods`() {
        val help = executor.help()
        
        assertNotNull(help)
        assertTrue(help.isNotBlank())
        assertTrue(help.contains("Switch to a specific browser tab"))
    }

    @Test
    fun `help for switchTab method returns detailed help`() {
        val help = executor.help("switchTab")
        
        assertNotNull(help)
        assertTrue(help.contains("Switch to a specific browser tab"))
        assertTrue(help.contains("switchTab"))
    }

    @Test
    fun `help for unknown method returns empty string`() {
        val help = executor.help("unknownMethod")
        
        assertEquals("", help)
    }

    @Test
    fun `switchTab calls correct browser method`() = runBlocking {
        val driver = mockk<AbstractWebDriver>(relaxed = true)
        every { browser.findDriverById(any()) } returns driver
        every { driver.bringToFront() } returns Unit
        every { driver.id } returns 1
        every { driver.guid } returns "test-guid"
        
        val tc = ToolCall(
            domain = "browser",
            method = "switchTab",
            arguments = mutableMapOf("tabId" to "1")
        )
        
        val result = executor.execute(tc, browser)
        
        assertNotNull(result.value)
        assertEquals(driver, result.value)
        verify { driver.bringToFront() }
    }

    @Test
    fun `switchTab with invalid tab returns exception`() = runBlocking {
        every { browser.findDriverById(any()) } returns null
        every { browser.drivers } returns mutableMapOf()
        
        val tc = ToolCall(
            domain = "browser",
            method = "switchTab",
            arguments = mutableMapOf("tabId" to "999")
        )
        
        val result = executor.execute(tc, browser)
        
        assertNotNull(result.exception)
        assertTrue(result.exception?.cause?.message?.contains("not found") == true)
    }

    @Test
    fun `domain property is browser`() {
        assertEquals("browser", executor.domain)
    }
}
