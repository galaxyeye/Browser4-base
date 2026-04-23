package ai.platon.pulsar.persist.model

import ai.platon.pulsar.common.config.VolatileConfig
import ai.platon.pulsar.persist.ParseStatus
import ai.platon.pulsar.persist.ProtocolStatus
import ai.platon.pulsar.persist.model.GoraWebPage
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class PlainWebPageTest {

    @Test
    fun `native content fields stay in sync`() {
        val page = PlainWebPage.newWebPage("https://example.com")

        page.contentType = "text/html"
        page.setStringContent("hello")

        assertEquals("text/html", page.contentType)
        assertEquals("hello", page.contentAsString)
        assertEquals(5, page.contentLength)
        assertEquals(5, page.originalContentLength)
        assertEquals(5, page.persistedContentLength)
        assertEquals(5, page.aveContentLength)

        page.setByteArrayContent("welcome".toByteArray())

        assertEquals(5, page.lastContentLength)
        assertEquals(7, page.contentLength)
        assertEquals(6, page.aveContentLength)
    }

    @Test
    fun `snapshot copy detaches from source webpage`() {
        val source = GoraWebPage.newWebPage("https://example.com", VolatileConfig.UNSAFE)
        source.contentType = " TEXT/HTML "
        source.originalContentLength = 5
        source.setStringContent("hello")
        source.metadata["source"] = "crawler"
        source.headers.put("x-test", "yes")
        source.protocolStatus = ProtocolStatus.STATUS_SUCCESS
        source.parseStatus = ParseStatus(ParseStatus.SUCCESS, ParseStatus.SC_OK)

        val plain = PlainWebPage.from(source)

        source.contentType = "application/json"
        source.metadata["source"] = "mutated"
        source.setStringContent("changed")

        assertEquals("text/html", plain.contentType)
        assertEquals("crawler", plain.metadata["source"])
        assertEquals("yes", plain.headers["x-test"])
        assertEquals(ProtocolStatus.STATUS_SUCCESS.name, plain.protocolStatus?.name)
        assertEquals("success/ok", plain.parseStatus?.name)
        assertContentEquals("hello".toByteArray(), assertNotNull(plain.content))
    }

    @Test
    fun `plain webpage keeps storage types out of its state`() {
        val fieldTypes = PlainWebPage::class.java.declaredFields.map { it.type.name }

        assertFalse(fieldTypes.any { it.contains("GWebPage") })
    }
}
