package ai.platon.pulsar.common.browser.fingerprint

import ai.platon.pulsar.common.browser.BrowserType

/**
 * Basic fingerprint generator that provides minimal browser fingerprints.
 *
 * This is the default generator used when the professional fingerprinting plugin is not
 * available or not enabled. It generates fingerprints with core parameters (browserType,
 * userAgent, screen, viewport) but does not include advanced parameters such as WebGL,
 * canvas, media devices, or hardware details.
 */
class BasicFingerprintGenerator : FingerprintGenerator {

    override val name: String = "basic"

    override fun generate(
        browserType: BrowserType,
        preset: DevicePreset
    ): Fingerprint {
        return Fingerprint(browserType)
    }

    override fun generateRandom(
        browserType: BrowserType, platform: Platform
    ): Fingerprint {
        return Fingerprint(browserType)
    }

    /**
     * Device preset types for fingerprint generation.
     */
    enum class DevicePreset {
        DESKTOP_WINDOWS,
        LAPTOP_WINDOWS,
        MACBOOK_PRO_13,
        MACBOOK_AIR,
        DESKTOP_LINUX,
        LAPTOP_LINUX
    }

    /**
     * Platform types.
     */
    enum class Platform {
        WINDOWS,
        MAC,
        LINUX
    }

    companion object {
        /**
         * Default Chrome version used for user agent generation.
         * Shared across all fingerprint generator implementations.
         */
        const val DEFAULT_CHROME_VERSION = "120.0.0.0"
    }
}
