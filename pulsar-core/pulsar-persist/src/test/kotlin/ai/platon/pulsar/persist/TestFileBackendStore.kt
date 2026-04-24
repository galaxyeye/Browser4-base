package ai.platon.pulsar.persist

import ai.platon.pulsar.common.AppPaths
import ai.platon.pulsar.common.urls.URLUtils
import ai.platon.pulsar.persist.gora.FileBackendPageStore
import ai.platon.pulsar.test.TestUrls
import org.apache.commons.io.FileUtils
import java.nio.file.Files
import kotlin.test.*

class TestFileBackendStore {
    private val url = TestUrls.PRODUCT_DETAIL_URL
    private val persistDirectory = AppPaths.TEST_DIR.resolve("unittests/TestFileBackendStore")
    private val store = FileBackendPageStore(persistDirectory)
    private lateinit var page: WebPage

    @BeforeTest
    fun setup() {
        page = WebPageExt.newTestWebPage(url)
    }

    @AfterTest
    fun tearDown() {
        runCatching { FileUtils.deleteDirectory(persistDirectory.toFile()) }.onFailure { it.printStackTrace() }
    }

    @Test
    fun whenWritePage_ThenAvroFileExists() {
        val path = store.getPersistPath(url, ".avro")
        store.writeAvro(page)
        assertTrue { Files.exists(path) }

        val key = URLUtils.reverseUrl(url)
        val loadedPage = store.readAvro(key)
        assertNotNull(loadedPage)
    }

    @Test
    fun whenWritePage_ThenReadSuccess() {
        store.writeAvro(page)

        val key = URLUtils.reverseUrl(url)
        val loadedPage = store.readAvro(key)
        assertNotNull(loadedPage)
    }
}
