package ai.platon.pulsar.common.browser

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

class BasicFingerprintGeneratorTest {

    private val generator = BasicFingerprintGenerator()

    @Test
    @DisplayName("test basic generator name is basic")
    fun testBasicGeneratorName() {
        assertEquals("basic", generator.name)
    }

    @Test
    @DisplayName("test basic generator produces fingerprint with core parameters")
    fun testBasicGeneratorProducesCoreParameters() {
        val fingerprint = generator.generate(
            BrowserType.PULSAR_CHROME,
            FingerprintGenerator.DevicePreset.DESKTOP_WINDOWS
        )

        assertNotNull(fingerprint.userAgent)
        assertTrue(fingerprint.userAgent!!.contains("Windows"))
        assertNotNull(fingerprint.screenParameters)
        assertNotNull(fingerprint.viewportParameters)
        assertEquals(1920, fingerprint.screenParameters?.width)
        assertEquals(1, fingerprint.version)
    }

    @Test
    @DisplayName("test basic generator omits advanced parameters")
    fun testBasicGeneratorOmitsAdvancedParameters() {
        val fingerprint = generator.generate(
            BrowserType.PULSAR_CHROME,
            FingerprintGenerator.DevicePreset.DESKTOP_WINDOWS
        )

        assertNull(fingerprint.geoTimeParameters)
        assertNull(fingerprint.hardwareParameters)
        assertNull(fingerprint.webGLParameters)
        assertNull(fingerprint.canvasParameters)
        assertNull(fingerprint.mediaParameters)
        assertNull(fingerprint.miscParameters)
    }

    @Test
    @DisplayName("test basic generator implements FingerprintGeneratorProvider")
    fun testBasicGeneratorImplementsProvider() {
        assertTrue(generator is FingerprintGeneratorProvider)
    }
}
