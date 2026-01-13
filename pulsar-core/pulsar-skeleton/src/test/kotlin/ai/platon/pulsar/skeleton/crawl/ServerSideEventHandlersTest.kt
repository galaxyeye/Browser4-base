package ai.platon.pulsar.skeleton.crawl

import ai.platon.pulsar.common.config.ImmutableConfig
import ai.platon.pulsar.persist.WebPage
import ai.platon.pulsar.persist.model.GoraWebPage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant

class ServerSideEventHandlersTest {
    private val conf = ImmutableConfig()

    @Test
    fun `test ServerSideEvent creation`() {
        val event = ServerSideEvent(
            eventType = "onWillLoad",
            eventPhase = "crawl",
            url = "https://example.com",
            message = "Loading page"
        )

        assertEquals("onWillLoad", event.eventType)
        assertEquals("crawl", event.eventPhase)
        assertEquals("https://example.com", event.url)
        assertEquals("Loading page", event.message)
        assertTrue(event.timestamp <= Instant.now())
    }

    @Test
    fun `test DefaultServerSideEventHandlers emits crawl events`() = runBlocking {
        val handlers = DefaultServerSideEventHandlers()
        val events = mutableListOf<ServerSideEvent>()

        // Collect events in the background
        val job = launch {
            handlers.eventFlow.take(2).toList().let { events.addAll(it) }
        }

        // Emit some events
        delay(100)
        handlers.onCrawlEvent("onWillLoad", "https://example.com")
        handlers.onCrawlEvent("onLoaded", "https://example.com", "Page loaded successfully")
        delay(100)

        job.join()

        assertEquals(2, events.size)
        assertEquals("onWillLoad", events[0].eventType)
        assertEquals("crawl", events[0].eventPhase)
        assertEquals("https://example.com", events[0].url)

        assertEquals("onLoaded", events[1].eventType)
        assertEquals("crawl", events[1].eventPhase)
        assertEquals("Page loaded successfully", events[1].message)
    }

    @Test
    fun `test DefaultServerSideEventHandlers emits load events`() = runBlocking {
        val handlers = DefaultServerSideEventHandlers()
        val events = mutableListOf<ServerSideEvent>()
        val page = GoraWebPage.newWebPage("https://example.com", conf.toVolatileConfig())

        // Collect events in the background
        val job = launch {
            handlers.eventFlow.take(2).toList().let { events.addAll(it) }
        }

        // Emit some events
        delay(100)
        handlers.onLoadEvent("onWillFetch", page)
        handlers.onLoadEvent("onFetched", page, "Fetch completed")
        delay(100)

        job.join()

        assertEquals(2, events.size)
        assertEquals("onWillFetch", events[0].eventType)
        assertEquals("load", events[0].eventPhase)
        assertEquals("https://example.com", events[0].url)

        assertEquals("onFetched", events[1].eventType)
        assertEquals("load", events[1].eventPhase)
        assertEquals("Fetch completed", events[1].message)
    }

    @Test
    fun `test DefaultServerSideEventHandlers emits browse events`() = runBlocking {
        val handlers = DefaultServerSideEventHandlers()
        val events = mutableListOf<ServerSideEvent>()
        val page = GoraWebPage.newWebPage("https://example.com", conf.toVolatileConfig())

        // Collect events in the background
        val job = launch {
            handlers.eventFlow.take(2).toList().let { events.addAll(it) }
        }

        // Emit some events
        delay(100)
        handlers.onBrowseEvent("onBrowserLaunched", page)
        handlers.onBrowseEvent("onNavigated", page, "Navigation completed")
        delay(100)

        job.join()

        assertEquals(2, events.size)
        assertEquals("onBrowserLaunched", events[0].eventType)
        assertEquals("browse", events[0].eventPhase)
        assertEquals("https://example.com", events[0].url)

        assertEquals("onNavigated", events[1].eventType)
        assertEquals("browse", events[1].eventPhase)
        assertEquals("Navigation completed", events[1].message)
    }

    @Test
    fun `test DefaultServerSideEventHandlers emits generic events`() = runBlocking {
        val handlers = DefaultServerSideEventHandlers()
        val events = mutableListOf<ServerSideEvent>()

        // Collect events in the background
        val job = launch {
            handlers.eventFlow.take(1).toList().let { events.addAll(it) }
        }

        // Emit a generic event
        delay(100)
        handlers.onEvent(
            eventType = "customEvent",
            eventPhase = "custom",
            url = "https://example.com",
            message = "Custom event message",
            metadata = mapOf("key" to "value")
        )
        delay(100)

        job.join()

        assertEquals(1, events.size)
        assertEquals("customEvent", events[0].eventType)
        assertEquals("custom", events[0].eventPhase)
        assertEquals("https://example.com", events[0].url)
        assertEquals("Custom event message", events[0].message)
        assertEquals("value", events[0].metadata["key"])
    }

    @Test
    fun `test event flow buffering`() = runBlocking {
        val handlers = DefaultServerSideEventHandlers()

        // Emit events before any collector is attached
        handlers.onCrawlEvent("event1", "https://example.com")
        handlers.onCrawlEvent("event2", "https://example.com")
        handlers.onCrawlEvent("event3", "https://example.com")

        delay(100)

        // Start collecting - should not receive old events (replay = 0)
        val events = handlers.eventFlow.take(2).toList()

        // Emit new events while collecting
        handlers.onCrawlEvent("event4", "https://example.com")
        handlers.onCrawlEvent("event5", "https://example.com")

        // Should only get the 2 new events
        assertEquals(2, events.size)
        assertEquals("event4", events[0].eventType)
        assertEquals("event5", events[1].eventType)
    }
}
