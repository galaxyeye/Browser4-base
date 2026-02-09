package ai.platon.pulsar.common.browser.fingerprint

import ai.platon.pulsar.common.browser.BrowserType
import ai.platon.pulsar.common.serialize.json.pulsarObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

class FingerprintLoader(
    private val browserType: BrowserType,
    private val contextDir: Path
) {
    private val logger = LoggerFactory.getLogger(FingerprintLoader::class.java)

    // Fingerprint validator for loaded fingerprints
    private val fingerprintValidator by lazy {
        FingerprintValidator()
    }

    fun loadOrGenerateFingerprint(): Fingerprint {
        val path = contextDir.resolve("fingerprint.json")
        val fingerprint = if (Files.exists(path)) {
            // Load existing fingerprint
            try {
                val loaded = pulsarObjectMapper().readValue<Fingerprint>(path.toFile())
                    .also { it.source = path.toString() }

                // Validate loaded fingerprint
                val validationResult = fingerprintValidator.validate(loaded)
                if (!validationResult.isValid) {
                    logger.warn("Loaded fingerprint validation failed: ${validationResult.summary()}")
                    logger.warn("Validation errors: ${validationResult.errors.joinToString(", ")}")
                } else if (validationResult.hasWarnings) {
                    logger.debug("Loaded fingerprint has warnings: ${validationResult.warnings.joinToString(", ")}")
                }

                loaded
            } catch (e: Exception) {
                logger.warn("Failed to load fingerprint from $path, use the browser's real fingerprint", e)
                // generateAndSaveFingerprint(browserType, contextDir)
                Fingerprint(browserType)
            }
        } else {
            // No existing fingerprint, use the browser's real fingerprint
            Fingerprint(browserType)
        }

        // Ensure browser type is set
        fingerprint.browserType = browserType

        return fingerprint
    }
}
