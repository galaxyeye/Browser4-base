package ai.platon.pulsar.rest.openapi.controller

import ai.platon.pulsar.rest.openapi.dto.ScreenshotResponse
import ai.platon.pulsar.rest.openapi.service.SessionManager
import ai.platon.pulsar.rest.openapi.store.InMemoryStore
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controller for screenshot operations.
 */
@RestController
@CrossOrigin
@RequestMapping(
    "/session/{sessionId}",
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
@ConditionalOnBean(SessionManager::class)
class ScreenshotController(
    private val sessionManager: SessionManager,
    private val store: InMemoryStore
) {
    private val logger = LoggerFactory.getLogger(ScreenshotController::class.java)

    /**
     * Takes a screenshot of the current page.
     */
    @GetMapping("/screenshot")
    suspend fun takeScreenshot(
        @PathVariable sessionId: String,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} taking screenshot", sessionId)
        ControllerUtils.addRequestId(response)

        val managed = sessionManager.getSession(sessionId)
            ?: return ControllerUtils.notFound("session not found", "No active session with id $sessionId")

        return try {
            val screenshot = managed.withLock {
                driver.captureScreenshot(fullPage = false)
            }
            ResponseEntity.ok(ScreenshotResponse(value = screenshot))
        } catch (e: Exception) {
            logger.error("Screenshot failed | sessionId={} | {}", sessionId, e.message)
            ControllerUtils.errorResponse("webdriver error", e.message ?: "Failed to take screenshot")
        }
    }

    /**
     * Takes a screenshot of a specific element.
     */
    @GetMapping("/element/{elementId}/screenshot")
    suspend fun takeElementScreenshot(
        @PathVariable sessionId: String,
        @PathVariable elementId: String,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} taking screenshot of element: {}", sessionId, elementId)
        ControllerUtils.addRequestId(response)

        val managed = sessionManager.getSession(sessionId)
            ?: return ControllerUtils.notFound("session not found", "No active session with id $sessionId")

        val element = store.getElement(sessionId, elementId)
            ?: return ControllerUtils.notFound("no such element", "Element with id $elementId not found")

        return try {
            val screenshot = managed.withLock {
                driver.captureScreenshot(element.selector)
            }
            ResponseEntity.ok(ScreenshotResponse(value = screenshot))
        } catch (e: Exception) {
            logger.error("Element screenshot failed | sessionId={} elementId={} | {}", sessionId, elementId, e.message)
            ControllerUtils.errorResponse("webdriver error", e.message ?: "Failed to take element screenshot")
        }
    }
}
