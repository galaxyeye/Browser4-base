package ai.platon.pulsar.common.browser.fingerprint

import java.time.Instant

/**
 * Report of detected fingerprint drift.
 *
 * @property drifts List of parameter changes detected
 * @property timestamp When the drift was detected
 * @property hasDrift Whether any drift was detected
 */
data class DriftReport(
    val drifts: List<String> = emptyList(),
    val timestamp: Instant = Instant.now()
) {
    val hasDrift: Boolean get() = drifts.isNotEmpty()
    
    /**
     * Get a human-readable summary of the drift report.
     */
    fun summary(): String {
        return if (hasDrift) {
            "Fingerprint drift detected (${drifts.size} changes)"
        } else {
            "No fingerprint drift detected"
        }
    }
    
    override fun toString(): String {
        val sb = StringBuilder(summary())
        if (hasDrift) {
            sb.append("\nChanges detected:")
            drifts.forEach { sb.append("\n  - $it") }
        }
        return sb.toString()
    }
}

/**
 * Detects drift (changes) in browser fingerprints.
 *
 * Fingerprint drift occurs when parameters change between sessions or over time,
 * which can indicate fingerprint instability or compromise. This detector compares
 * fingerprints to identify any differences.
 */
class FingerprintDriftDetector {
    
    /**
     * Detect drift between two fingerprints.
     *
     * Compares all parameters between the original and current fingerprints
     * and reports any differences.
     *
     * @param original The original/expected fingerprint
     * @param current The current fingerprint to check
     * @return DriftReport containing detected changes
     */
    fun detectDrift(original: Fingerprint, current: Fingerprint): DriftReport {
        val drifts = mutableListOf<String>()
        
        // Compare basic parameters
        if (original.browserType != current.browserType) {
            drifts.add("Browser type changed: ${original.browserType} → ${current.browserType}")
        }
        
        if (original.userAgent != current.userAgent) {
            drifts.add("User agent changed")
        }
        
        if (original.proxyURI != current.proxyURI) {
            drifts.add("Proxy changed: ${original.proxyURI} → ${current.proxyURI}")
        }
        
        // Compare screen parameters
        compareScreenParameters(original, current, drifts)
        
        // Compare viewport parameters
        compareViewportParameters(original, current, drifts)
        
        // Compare geo-time parameters
        compareGeoTimeParameters(original, current, drifts)
        
        // Compare hardware parameters
        compareHardwareParameters(original, current, drifts)
        
        // Compare WebGL parameters
        compareWebGLParameters(original, current, drifts)
        
        // Compare canvas parameters
        compareCanvasParameters(original, current, drifts)
        
        return DriftReport(drifts)
    }
    
    private fun compareScreenParameters(
        original: Fingerprint,
        current: Fingerprint,
        drifts: MutableList<String>
    ) {
        val orig = original.screenParameters
        val curr = current.screenParameters
        
        if (orig == null && curr != null) {
            drifts.add("Screen parameters added")
            return
        }
        if (orig != null && curr == null) {
            drifts.add("Screen parameters removed")
            return
        }
        if (orig == null || curr == null) return
        
        if (orig.width != curr.width || orig.height != curr.height) {
            drifts.add("Screen resolution changed: ${orig.width}x${orig.height} → ${curr.width}x${curr.height}")
        }
        if (orig.devicePixelRatio != curr.devicePixelRatio) {
            drifts.add("Device pixel ratio changed: ${orig.devicePixelRatio} → ${curr.devicePixelRatio}")
        }
        if (orig.colorDepth != curr.colorDepth) {
            drifts.add("Color depth changed: ${orig.colorDepth} → ${curr.colorDepth}")
        }
    }
    
    private fun compareViewportParameters(
        original: Fingerprint,
        current: Fingerprint,
        drifts: MutableList<String>
    ) {
        val orig = original.viewportParameters
        val curr = current.viewportParameters
        
        if (orig == null && curr != null) {
            drifts.add("Viewport parameters added")
            return
        }
        if (orig != null && curr == null) {
            drifts.add("Viewport parameters removed")
            return
        }
        if (orig == null || curr == null) return
        
        if (orig.width != curr.width || orig.height != curr.height) {
            drifts.add("Viewport size changed: ${orig.width}x${orig.height} → ${curr.width}x${curr.height}")
        }
        if (orig.isMobile != curr.isMobile) {
            drifts.add("Mobile mode changed: ${orig.isMobile} → ${curr.isMobile}")
        }
    }
    
    private fun compareGeoTimeParameters(
        original: Fingerprint,
        current: Fingerprint,
        drifts: MutableList<String>
    ) {
        val orig = original.geoTimeParameters
        val curr = current.geoTimeParameters
        
        if (orig == null && curr != null) {
            drifts.add("Geo-time parameters added")
            return
        }
        if (orig != null && curr == null) {
            drifts.add("Geo-time parameters removed")
            return
        }
        if (orig == null || curr == null) return
        
        if (orig.timezone != curr.timezone) {
            drifts.add("Timezone changed: ${orig.timezone} → ${curr.timezone}")
        }
        if (orig.locale != curr.locale) {
            drifts.add("Locale changed: ${orig.locale} → ${curr.locale}")
        }
        if (orig.languages != curr.languages) {
            drifts.add("Languages changed")
        }
    }
    
    private fun compareHardwareParameters(
        original: Fingerprint,
        current: Fingerprint,
        drifts: MutableList<String>
    ) {
        val orig = original.hardwareParameters
        val curr = current.hardwareParameters
        
        if (orig == null && curr != null) {
            drifts.add("Hardware parameters added")
            return
        }
        if (orig != null && curr == null) {
            drifts.add("Hardware parameters removed")
            return
        }
        if (orig == null || curr == null) return
        
        if (orig.hardwareConcurrency != curr.hardwareConcurrency) {
            drifts.add("Hardware concurrency changed: ${orig.hardwareConcurrency} → ${curr.hardwareConcurrency}")
        }
        if (orig.platform != curr.platform) {
            drifts.add("Platform changed: ${orig.platform} → ${curr.platform}")
        }
        if (orig.vendor != curr.vendor) {
            drifts.add("Vendor changed: ${orig.vendor} → ${curr.vendor}")
        }
    }
    
    private fun compareWebGLParameters(
        original: Fingerprint,
        current: Fingerprint,
        drifts: MutableList<String>
    ) {
        val orig = original.webGLParameters
        val curr = current.webGLParameters
        
        if (orig == null && curr != null) {
            drifts.add("WebGL parameters added")
            return
        }
        if (orig != null && curr == null) {
            drifts.add("WebGL parameters removed")
            return
        }
        if (orig == null || curr == null) return
        
        if (orig.vendor != curr.vendor) {
            drifts.add("WebGL vendor changed: ${orig.vendor} → ${curr.vendor}")
        }
        if (orig.renderer != curr.renderer) {
            drifts.add("WebGL renderer changed: ${orig.renderer} → ${curr.renderer}")
        }
    }
    
    private fun compareCanvasParameters(
        original: Fingerprint,
        current: Fingerprint,
        drifts: MutableList<String>
    ) {
        val orig = original.canvasParameters
        val curr = current.canvasParameters
        
        if (orig == null && curr != null) {
            drifts.add("Canvas parameters added")
            return
        }
        if (orig != null && curr == null) {
            drifts.add("Canvas parameters removed")
            return
        }
        if (orig == null || curr == null) return
        
        if (orig.fingerprintSeed != curr.fingerprintSeed) {
            drifts.add("Canvas fingerprint seed changed")
        }
    }
}
