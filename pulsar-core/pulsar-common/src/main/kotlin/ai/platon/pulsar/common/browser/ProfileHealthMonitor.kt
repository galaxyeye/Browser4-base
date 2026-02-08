package ai.platon.pulsar.common.browser

import java.nio.file.Path
import java.time.Instant

/**
 * Individual health check result.
 *
 * @property name Name of the health check
 * @property passed Whether the check passed
 * @property message Descriptive message about the check result
 */
data class HealthCheck(
    val name: String,
    val passed: Boolean,
    val message: String = ""
) {
    override fun toString(): String {
        val status = if (passed) "✓" else "✗"
        return "$status $name${if (message.isNotEmpty()) ": $message" else ""}"
    }
}

/**
 * Report of profile health check results.
 *
 * @property checks List of individual health check results
 * @property timestamp When the health check was performed
 */
data class HealthReport(
    val checks: List<HealthCheck> = emptyList(),
    val timestamp: Instant = Instant.now()
) {
    val isHealthy: Boolean get() = checks.all { it.passed }
    val failedChecks: List<HealthCheck> get() = checks.filter { !it.passed }
    
    /**
     * Get a human-readable summary of the health report.
     */
    fun summary(): String {
        return when {
            isHealthy -> "Profile is healthy (${checks.size}/${checks.size} checks passed)"
            else -> "Profile has issues (${failedChecks.size}/${checks.size} checks failed)"
        }
    }
    
    override fun toString(): String {
        val sb = StringBuilder(summary())
        if (checks.isNotEmpty()) {
            sb.append("\nChecks:")
            checks.forEach { sb.append("\n  $it") }
        }
        return sb.toString()
    }
}

/**
 * Monitors the health of browser profiles.
 *
 * Health checks include:
 * - Fingerprint integrity (all required parameters present)
 * - Fingerprint consistency (parameters logically consistent)
 * - Fingerprint stability (no unexpected drift)
 */
class ProfileHealthMonitor {
    
    private val validator = FingerprintValidator()
    
    /**
     * Check the health of a browser profile.
     *
     * Performs comprehensive health checks including integrity,
     * consistency, and validation.
     *
     * @param fingerprint The profile fingerprint to check
     * @param contextDir Optional context directory to check accessibility
     * @return HealthReport containing check results
     */
    fun checkHealth(fingerprint: Fingerprint, contextDir: Path? = null): HealthReport {
        val checks = mutableListOf<HealthCheck>()
        
        // Check fingerprint integrity
        checks.add(checkFingerprintIntegrity(fingerprint))
        
        // Check fingerprint consistency
        checks.add(checkFingerprintConsistency(fingerprint))
        
        // Check context directory if provided
        if (contextDir != null) {
            checks.add(checkContextDirectory(contextDir))
        }
        
        // Check fingerprint version
        checks.add(checkFingerprintVersion(fingerprint))
        
        return HealthReport(checks)
    }
    
    /**
     * Check if fingerprint has all essential parameters.
     */
    private fun checkFingerprintIntegrity(fingerprint: Fingerprint): HealthCheck {
        val missing = mutableListOf<String>()
        
        if (fingerprint.userAgent == null) {
            missing.add("userAgent")
        }
        if (fingerprint.screenParameters == null) {
            missing.add("screenParameters")
        }
        if (fingerprint.hardwareParameters == null) {
            missing.add("hardwareParameters")
        }
        if (fingerprint.geoTimeParameters == null) {
            missing.add("geoTimeParameters")
        }
        
        return if (missing.isEmpty()) {
            HealthCheck(
                name = "Fingerprint Integrity",
                passed = true,
                message = "All essential parameters present"
            )
        } else {
            HealthCheck(
                name = "Fingerprint Integrity",
                passed = false,
                message = "Missing parameters: ${missing.joinToString(", ")}"
            )
        }
    }
    
    /**
     * Check if fingerprint parameters are logically consistent.
     */
    private fun checkFingerprintConsistency(fingerprint: Fingerprint): HealthCheck {
        val result = validator.validate(fingerprint)
        
        return if (result.isValid) {
            HealthCheck(
                name = "Fingerprint Consistency",
                passed = true,
                message = if (result.hasWarnings) {
                    "${result.warnings.size} warnings"
                } else {
                    "All parameters consistent"
                }
            )
        } else {
            HealthCheck(
                name = "Fingerprint Consistency",
                passed = false,
                message = "${result.errors.size} errors: ${result.errors.take(2).joinToString(", ")}"
            )
        }
    }
    
    /**
     * Check if context directory is accessible.
     */
    private fun checkContextDirectory(contextDir: Path): HealthCheck {
        return try {
            val exists = java.nio.file.Files.exists(contextDir)
            val writable = if (exists) java.nio.file.Files.isWritable(contextDir) else false
            
            when {
                !exists -> HealthCheck(
                    name = "Context Directory",
                    passed = false,
                    message = "Directory does not exist: $contextDir"
                )
                !writable -> HealthCheck(
                    name = "Context Directory",
                    passed = false,
                    message = "Directory not writable: $contextDir"
                )
                else -> HealthCheck(
                    name = "Context Directory",
                    passed = true,
                    message = "Directory accessible"
                )
            }
        } catch (e: Exception) {
            HealthCheck(
                name = "Context Directory",
                passed = false,
                message = "Error checking directory: ${e.message}"
            )
        }
    }
    
    /**
     * Check if fingerprint version is supported.
     */
    private fun checkFingerprintVersion(fingerprint: Fingerprint): HealthCheck {
        val currentVersion = 1
        val fingerprintVersion = fingerprint.version
        
        return when {
            fingerprintVersion > currentVersion -> HealthCheck(
                name = "Fingerprint Version",
                passed = false,
                message = "Version $fingerprintVersion is newer than supported version $currentVersion"
            )
            fingerprintVersion < currentVersion -> HealthCheck(
                name = "Fingerprint Version",
                passed = true,
                message = "Version $fingerprintVersion (upgrade available to $currentVersion)"
            )
            else -> HealthCheck(
                name = "Fingerprint Version",
                passed = true,
                message = "Current version $currentVersion"
            )
        }
    }
}
