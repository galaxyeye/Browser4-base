package ai.platon.pulsar.persist

import ai.platon.pulsar.common.config.VolatileConfig
import ai.platon.pulsar.persist.model.GoraWebPage
import ai.platon.pulsar.persist.model.emplace
import ai.platon.pulsar.persist.model.ensureGPageModel
import ai.platon.pulsar.persist.model.findGroup
import ai.platon.pulsar.persist.model.findValue
import ai.platon.pulsar.persist.model.put
import ai.platon.pulsar.persist.model.remove
import ai.platon.pulsar.test.TestUrls
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TestGPageModel {

    private val baseUrl = TestUrls.PRODUCT_DETAIL_URL
    private val conf = VolatileConfig()
    private val groupId = 43853791

    @Test
    fun testEmplace() {
        val page = GoraWebPage.newWebPage(baseUrl, conf)
        val pageModel = page.ensureGPageModel()

        pageModel.emplace(groupId, "", mapOf("a" to "1", "b" to "2"))
        assertTrue { pageModel.isDirty }
        assertTrue { pageModel.fieldGroups[0].isDirty }

        assertEquals("1", pageModel.findGroup(groupId)?.get("a"))

        pageModel.emplace(groupId, "", mapOf("c" to "3", "d" to "4"))
        assertTrue { pageModel.isDirty }
        assertTrue { pageModel.fieldGroups[0].isDirty }

        assertEquals("4", pageModel.findGroup(groupId)?.get("d"))
    }

    @Test
    fun testAccess() {
        val page = GoraWebPage.newWebPage(baseUrl, conf)
        val pageModel = page.ensureGPageModel()

        assertTrue { !pageModel.isDirty }
        assertNotEquals("1", pageModel.findValue(1, "a"))

        pageModel.put(1, "a", "1")
        assertTrue { pageModel.isDirty }
        assertTrue { pageModel.fieldGroups[0].isDirty }
        assertEquals("1", pageModel.findValue(1, "a"))

        pageModel.remove(1, "a")
        assertTrue { pageModel.isDirty }
        assertTrue { pageModel.fieldGroups[0].isDirty }
        assertNotEquals("1", pageModel.findValue(1, "a"))
    }
}
