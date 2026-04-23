package ai.platon.pulsar.persist.model

import ai.platon.pulsar.common.DateTimes.doomsday
import ai.platon.pulsar.common.config.AppConstants
import ai.platon.pulsar.common.urls.URLUtils
import ai.platon.pulsar.persist.ParseStatus
import ai.platon.pulsar.persist.ProtocolStatus
import ai.platon.pulsar.persist.WebPage
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.LinkedHashMap

/**
 * A storage-agnostic snapshot of a web page that only uses plain Kotlin/JDK types.
 */
data class PlainWebPage(
    val url: String,
    var id: Long = -1,
    var href: String? = null,
    var location: String = url,
    var referrer: String? = null,
    var baseURI: String = "",
    var args: String = "",
    var pageTitle: String? = null,
    var contentTitle: String? = null,
    var pageText: String? = null,
    var contentText: String? = null,
    var contentType: String = "",
    var encoding: String? = null,
    var content: ByteArray? = null,
    var contentLength: Long = 0,
    var originalContentLength: Long = 0,
    var persistedContentLength: Long = 0,
    var lastContentLength: Long = 0,
    var aveContentLength: Long = 0,
    var createTime: Instant = Instant.EPOCH,
    var fetchTime: Instant = Instant.EPOCH,
    var prevFetchTime: Instant = Instant.EPOCH,
    var fetchInterval: Duration = Duration.ZERO,
    var modifiedTime: Instant = Instant.EPOCH,
    var prevModifiedTime: Instant = Instant.EPOCH,
    var fetchCount: Int = 0,
    var fetchRetries: Int = 0,
    var distance: Int = AppConstants.DISTANCE_INFINITE,
    var isResource: Boolean = false,
    var lastBrowser: String? = null,
    var pageCategory: String? = null,
    var htmlIntegrity: String? = null,
    var proxy: String? = null,
    var metadata: MutableMap<String, String> = linkedMapOf(),
    var headers: MutableMap<String, String> = linkedMapOf(),
    var protocolStatus: PlainProtocolStatus? = null,
    var parseStatus: PlainParseStatus? = null,
) {
    companion object {
        val NIL = newInternalPage(AppConstants.NIL_PAGE_URL, 0, "", "")

        fun newWebPage(url: String, href: String? = null): PlainWebPage {
            return PlainWebPage(
                url = url,
                href = href,
                location = url,
                createTime = Instant.now(),
                modifiedTime = Instant.now()
            )
        }

        fun newInternalPage(url: String, id: Long, title: String, content: String): PlainWebPage {
            return newWebPage(url).apply {
                this.id = id
                this.pageTitle = title
                this.fetchTime = doomsday
                this.fetchInterval = ChronoUnit.CENTURIES.duration
                this.distance = AppConstants.DISTANCE_INFINITE
                this.modifiedTime = Instant.EPOCH
                this.prevFetchTime = Instant.EPOCH
                setStringContent(content)
            }
        }

        fun from(page: WebPage): PlainWebPage {
            return PlainWebPage(
                id = page.id,
                url = page.url,
                href = page.href,
                location = page.location,
                referrer = page.referrer,
                baseURI = page.baseURI,
                args = page.args,
                pageTitle = page.pageTitle,
                contentTitle = page.contentTitle,
                pageText = page.pageText,
                contentText = page.contentText,
                contentType = page.contentType,
                encoding = page.encoding,
                content = page.content?.let { page.contentAsBytes.copyOf() },
                contentLength = page.contentLength,
                originalContentLength = page.originalContentLength,
                persistedContentLength = page.persistedContentLength,
                lastContentLength = page.lastContentLength,
                aveContentLength = page.aveContentLength,
                createTime = page.createTime,
                fetchTime = page.fetchTime,
                prevFetchTime = page.prevFetchTime,
                fetchInterval = page.fetchInterval,
                modifiedTime = page.modifiedTime,
                prevModifiedTime = page.prevModifiedTime,
                fetchCount = page.fetchCount,
                fetchRetries = page.fetchRetries,
                distance = page.distance,
                isResource = page.isResource,
                lastBrowser = page.lastBrowser.name,
                pageCategory = page.pageCategory.format(),
                htmlIntegrity = page.htmlIntegrity.name,
                proxy = page.proxy,
                metadata = LinkedHashMap(page.metadata.asStringMap()),
                headers = LinkedHashMap(page.headers.asStringMap()),
                protocolStatus = PlainProtocolStatus.from(page.protocolStatus),
                parseStatus = PlainParseStatus.from(page.parseStatus),
            )
        }
    }

    val key: String = URLUtils.reverseUrlOrEmpty(url)

    private var contentRevisionCount: Long = 0

    init {
        content = content?.copyOf()
        if (content != null) {
            val size = content!!.size.toLong()
            if (persistedContentLength == 0L) {
                persistedContentLength = size
            }
            if (originalContentLength == 0L) {
                originalContentLength = size
            }
            if (contentLength == 0L) {
                contentLength = originalContentLength
            }
            if (aveContentLength == 0L) {
                aveContentLength = contentLength
            }
            contentRevisionCount = 1
        }
    }

    val contentAsString: String
        get() = content?.toString(Charsets.UTF_8).orEmpty()

    fun setStringContent(value: String?) {
        setByteArrayContent(value?.toByteArray())
    }

    fun setByteArrayContent(value: ByteArray?) {
        if (value == null) {
            lastContentLength = contentLength
            content = null
            contentLength = 0
            originalContentLength = 0
            persistedContentLength = 0
            aveContentLength = 0
            contentRevisionCount = 0
            return
        }

        val bytes = value.copyOf()
        lastContentLength = contentLength
        content = bytes

        val length = bytes.size.toLong()
        persistedContentLength = length
        originalContentLength = length
        contentLength = length
        aveContentLength = if (contentRevisionCount == 0L) {
            contentLength
        } else {
            ((aveContentLength * contentRevisionCount) + contentLength) / (contentRevisionCount + 1)
        }
        contentRevisionCount++
    }
}

data class PlainProtocolStatus(
    var majorCode: Short,
    var minorCode: Int,
    var name: String,
    var args: MutableMap<String, String> = linkedMapOf(),
) {
    companion object {
        fun from(status: ProtocolStatus): PlainProtocolStatus {
            return PlainProtocolStatus(
                majorCode = status.majorCode,
                minorCode = status.minorCode,
                name = status.name,
                args = LinkedHashMap(status.args.mapKeys { it.key.toString() }.mapValues { it.value?.toString().orEmpty() })
            )
        }
    }
}

data class PlainParseStatus(
    var majorCode: Short,
    var minorCode: Int,
    var name: String,
    var args: MutableMap<String, String> = linkedMapOf(),
) {
    companion object {
        fun from(status: ParseStatus): PlainParseStatus {
            return PlainParseStatus(
                majorCode = status.majorCode,
                minorCode = status.minorCode,
                name = status.name,
                args = LinkedHashMap(status.args.mapKeys { it.key.toString() }.mapValues { it.value?.toString().orEmpty() })
            )
        }
    }
}
