package ai.platon.pulsar.rest.openapi.controller

import ai.platon.pulsar.rest.openapi.dto.*
import ai.platon.pulsar.rest.openapi.service.SessionManager
import ai.platon.pulsar.rest.openapi.store.InMemoryStore
import ai.platon.pulsar.skeleton.crawl.fetch.driver.WebDriverException
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controller for element operations by element ID.
 */
@RestController
@CrossOrigin
@RequestMapping(
    "/session/{sessionId}",
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
@ConditionalOnBean(SessionManager::class)
class ElementController(
    private val sessionManager: SessionManager,
    private val store: InMemoryStore
) {
    private val logger = LoggerFactory.getLogger(ElementController::class.java)

    /**
     * Finds a single element using WebDriver locator strategy.
     */
    @PostMapping("/element", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun findElement(
        @PathVariable sessionId: String,
        @RequestBody request: FindElementRequest,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} finding element using {}: {}", sessionId, request.using, request.value)
        ControllerUtils.addRequestId(response)

        if (!sessionManager.sessionExists(sessionId)) {
            return ControllerUtils.notFound("session not found", "No active session with id $sessionId")
        }

        // Convert WebDriver strategy to selector
        val selector = convertToSelector(request.using, request.value)
        val element = store.getOrCreateElement(sessionId, selector, "css")

        return ResponseEntity.ok(ElementResponse(value = ElementRef(elementId = element.elementId)))
    }

    /**
     * Finds all elements using WebDriver locator strategy.
     */
    @PostMapping("/elements", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun findElements(
        @PathVariable sessionId: String,
        @RequestBody request: FindElementRequest,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} finding elements using {}: {}", sessionId, request.using, request.value)
        ControllerUtils.addRequestId(response)

        if (!sessionManager.sessionExists(sessionId)) {
            return ControllerUtils.notFound("session not found", "No active session with id $sessionId")
        }

        val selector = convertToSelector(request.using, request.value)
        val element = store.getOrCreateElement(sessionId, selector, "css")

        return ResponseEntity.ok(ElementsResponse(value = listOf(ElementRef(elementId = element.elementId))))
    }

    /**
     * Clicks an element by ID.
     */
    @PostMapping("/element/{elementId}/click")
    suspend fun clickElement(
        @PathVariable sessionId: String,
        @PathVariable elementId: String,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} clicking element: {}", sessionId, elementId)
        ControllerUtils.addRequestId(response)

        val managed = sessionManager.getSession(sessionId)
            ?: return ControllerUtils.notFound("session not found", "No active session with id $sessionId")

        val element = store.getElement(sessionId, elementId)
            ?: return ControllerUtils.notFound("no such element", "Element with id $elementId not found")

        return try {
            managed.withLock {
                driver.click(element.selector)
            }
            ResponseEntity.ok(WebDriverResponse<Any?>(value = null))
        } catch (e: WebDriverException) {
            logger.error("Element click failed | sessionId={} elementId={} | {}", sessionId, elementId, e.message)
            ControllerUtils.errorResponse("webdriver error", e.message ?: "WebDriver error")
        } catch (e: Exception) {
            logger.error("Element click failed | sessionId={} elementId={} | {}", sessionId, elementId, e.message, e)
            ControllerUtils.errorResponse("internal error", e.message ?: "Internal error")
        }
    }

    /**
     * Sends keys to an element by ID.
     */
    @PostMapping("/element/{elementId}/value", consumes = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun sendKeysToElement(
        @PathVariable sessionId: String,
        @PathVariable elementId: String,
        @RequestBody request: SendKeysRequest,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} sending keys to element: {}", sessionId, elementId)
        ControllerUtils.addRequestId(response)

        val managed = sessionManager.getSession(sessionId)
            ?: return ControllerUtils.notFound("session not found", "No active session with id $sessionId")

        val element = store.getElement(sessionId, elementId)
            ?: return ControllerUtils.notFound("no such element", "Element with id $elementId not found")

        return try {
            managed.withLock {
                driver.fill(element.selector, request.text)
            }
            ResponseEntity.ok(WebDriverResponse<Any?>(value = null))
        } catch (e: WebDriverException) {
            logger.error("Send keys failed | sessionId={} elementId={} | {}", sessionId, elementId, e.message)
            ControllerUtils.errorResponse("webdriver error", e.message ?: "WebDriver error")
        } catch (e: Exception) {
            logger.error("Send keys failed | sessionId={} elementId={} | {}", sessionId, elementId, e.message, e)
            ControllerUtils.errorResponse("internal error", e.message ?: "Internal error")
        }
    }

    /**
     * Gets an element attribute by name.
     */
    @GetMapping("/element/{elementId}/attribute/{name}")
    suspend fun getElementAttribute(
        @PathVariable sessionId: String,
        @PathVariable elementId: String,
        @PathVariable name: String,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} getting attribute {} from element: {}", sessionId, name, elementId)
        ControllerUtils.addRequestId(response)

        val managed = sessionManager.getSession(sessionId)
            ?: return ControllerUtils.notFound("session not found", "No active session with id $sessionId")

        val element = store.getElement(sessionId, elementId)
            ?: return ControllerUtils.notFound("no such element", "Element with id $elementId not found")

        return try {
            val value = managed.withLock {
                driver.selectFirstAttributeOrNull(element.selector, name)
            }
            ResponseEntity.ok(AttributeResponse(value = value ?: ""))
        } catch (e: WebDriverException) {
            logger.error(
                "Get attribute failed | sessionId={} elementId={} name={} | {}",
                sessionId,
                elementId,
                name,
                e.message
            )
            ControllerUtils.errorResponse("webdriver error", e.message ?: "WebDriver error")
        } catch (e: Exception) {
            logger.error(
                "Get attribute failed | sessionId={} elementId={} name={} | {}",
                sessionId,
                elementId,
                name,
                e.message,
                e
            )
            ControllerUtils.errorResponse("internal error", e.message ?: "Internal error")
        }
    }

    /**
     * Gets an element's text content.
     */
    @GetMapping("/element/{elementId}/text")
    suspend fun getElementText(
        @PathVariable sessionId: String,
        @PathVariable elementId: String,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} getting text from element: {}", sessionId, elementId)
        ControllerUtils.addRequestId(response)

        val managed = sessionManager.getSession(sessionId)
            ?: return ControllerUtils.notFound("session not found", "No active session with id $sessionId")

        val element = store.getElement(sessionId, elementId)
            ?: return ControllerUtils.notFound("no such element", "Element with id $elementId not found")

        return try {
            val text = managed.withLock {
                driver.selectFirstTextOrNull(element.selector) ?: ""
            }
            ResponseEntity.ok(TextResponse(value = text))
        } catch (e: WebDriverException) {
            logger.error("Get text failed | sessionId={} elementId={} | {}", sessionId, elementId, e.message)
            ControllerUtils.errorResponse("webdriver error", e.message ?: "WebDriver error")
        } catch (e: Exception) {
            logger.error("Get text failed | sessionId={} elementId={} | {}", sessionId, elementId, e.message, e)
            ControllerUtils.errorResponse("internal error", e.message ?: "Internal error")
        }
    }

    /**
     * Gets the active element (currently focused element).
     */
    @PostMapping("/element/active")
    suspend fun getActiveElement(
        @PathVariable sessionId: String,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} getting active element", sessionId)
        ControllerUtils.addRequestId(response)

        val managed = sessionManager.getSession(sessionId)
            ?: return ControllerUtils.notFound("session not found", "No active session with id $sessionId")

        return try {
            // Use JavaScript to get the active element's selector or tag
            val selector = managed.withLock {
                driver.evaluate("document.activeElement?.tagName?.toLowerCase() || 'body'") as? String ?: "body"
            }
            val element = store.getOrCreateElement(sessionId, selector, "css")
            ResponseEntity.ok(ElementResponse(value = ElementRef(elementId = element.elementId)))
        } catch (e: Exception) {
            logger.error("Get active element failed | sessionId={} | {}", sessionId, e.message)
            ControllerUtils.errorResponse("webdriver error", e.message ?: "Failed to get active element")
        }
    }

    /**
     * Finds a child element from a parent element.
     */
    @PostMapping("/element/{elementId}/element", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun findElementFromElement(
        @PathVariable sessionId: String,
        @PathVariable elementId: String,
        @RequestBody request: FindElementRequest,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} finding element from element {} using {}: {}", sessionId, elementId, request.using, request.value)
        ControllerUtils.addRequestId(response)

        if (!sessionManager.sessionExists(sessionId)) {
            return ControllerUtils.notFound("session not found", "No active session with id $sessionId")
        }

        val parentElement = store.getElement(sessionId, elementId)
            ?: return ControllerUtils.notFound("no such element", "Element with id $elementId not found")

        // Combine parent selector with child selector
        val childSelector = convertToSelector(request.using, request.value)
        val combinedSelector = "${parentElement.selector} ${childSelector}"
        val element = store.getOrCreateElement(sessionId, combinedSelector, "css")

        return ResponseEntity.ok(ElementResponse(value = ElementRef(elementId = element.elementId)))
    }

    /**
     * Finds all child elements from a parent element.
     */
    @PostMapping("/element/{elementId}/elements", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun findElementsFromElement(
        @PathVariable sessionId: String,
        @PathVariable elementId: String,
        @RequestBody request: FindElementRequest,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} finding elements from element {} using {}: {}", sessionId, elementId, request.using, request.value)
        ControllerUtils.addRequestId(response)

        if (!sessionManager.sessionExists(sessionId)) {
            return ControllerUtils.notFound("session not found", "No active session with id $sessionId")
        }

        val parentElement = store.getElement(sessionId, elementId)
            ?: return ControllerUtils.notFound("no such element", "Element with id $elementId not found")

        val childSelector = convertToSelector(request.using, request.value)
        val combinedSelector = "${parentElement.selector} ${childSelector}"
        val element = store.getOrCreateElement(sessionId, combinedSelector, "css")

        return ResponseEntity.ok(ElementsResponse(value = listOf(ElementRef(elementId = element.elementId))))
    }

    /**
     * Clears the value of an element (typically input or textarea).
     */
    @PostMapping("/element/{elementId}/clear")
    suspend fun clearElement(
        @PathVariable sessionId: String,
        @PathVariable elementId: String,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} clearing element: {}", sessionId, elementId)
        ControllerUtils.addRequestId(response)

        val managed = sessionManager.getSession(sessionId)
            ?: return ControllerUtils.notFound("session not found", "No active session with id $sessionId")

        val element = store.getElement(sessionId, elementId)
            ?: return ControllerUtils.notFound("no such element", "Element with id $elementId not found")

        return try {
            managed.withLock {
                driver.fill(element.selector, "")
            }
            ResponseEntity.ok(WebDriverResponse<Any?>(value = null))
        } catch (e: WebDriverException) {
            logger.error("Clear element failed | sessionId={} elementId={} | {}", sessionId, elementId, e.message)
            ControllerUtils.errorResponse("webdriver error", e.message ?: "WebDriver error")
        } catch (e: Exception) {
            logger.error("Clear element failed | sessionId={} elementId={} | {}", sessionId, elementId, e.message, e)
            ControllerUtils.errorResponse("internal error", e.message ?: "Internal error")
        }
    }

    /**
     * Checks if an element is selected (for checkbox/radio/option elements).
     */
    @GetMapping("/element/{elementId}/selected")
    suspend fun isElementSelected(
        @PathVariable sessionId: String,
        @PathVariable elementId: String,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} checking if element is selected: {}", sessionId, elementId)
        ControllerUtils.addRequestId(response)

        val managed = sessionManager.getSession(sessionId)
            ?: return ControllerUtils.notFound("session not found", "No active session with id $sessionId")

        val element = store.getElement(sessionId, elementId)
            ?: return ControllerUtils.notFound("no such element", "Element with id $elementId not found")

        return try {
            val isSelected = managed.withLock {
                driver.isChecked(element.selector)
            }
            ResponseEntity.ok(WebDriverResponse(value = isSelected))
        } catch (e: Exception) {
            logger.error("Is element selected failed | sessionId={} elementId={} | {}", sessionId, elementId, e.message)
            ControllerUtils.errorResponse("webdriver error", e.message ?: "Failed to check if element is selected")
        }
    }

    /**
     * Checks if an element is enabled.
     */
    @GetMapping("/element/{elementId}/enabled")
    suspend fun isElementEnabled(
        @PathVariable sessionId: String,
        @PathVariable elementId: String,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} checking if element is enabled: {}", sessionId, elementId)
        ControllerUtils.addRequestId(response)

        val managed = sessionManager.getSession(sessionId)
            ?: return ControllerUtils.notFound("session not found", "No active session with id $sessionId")

        val element = store.getElement(sessionId, elementId)
            ?: return ControllerUtils.notFound("no such element", "Element with id $elementId not found")

        return try {
            val isEnabled = managed.withLock {
                val disabled = driver.selectFirstAttributeOrNull(element.selector, "disabled")
                disabled == null
            }
            ResponseEntity.ok(WebDriverResponse(value = isEnabled))
        } catch (e: Exception) {
            logger.error("Is element enabled failed | sessionId={} elementId={} | {}", sessionId, elementId, e.message)
            ControllerUtils.errorResponse("webdriver error", e.message ?: "Failed to check if element is enabled")
        }
    }

    /**
     * Checks if an element is displayed (visible).
     */
    @GetMapping("/element/{elementId}/displayed")
    suspend fun isElementDisplayed(
        @PathVariable sessionId: String,
        @PathVariable elementId: String,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} checking if element is displayed: {}", sessionId, elementId)
        ControllerUtils.addRequestId(response)

        val managed = sessionManager.getSession(sessionId)
            ?: return ControllerUtils.notFound("session not found", "No active session with id $sessionId")

        val element = store.getElement(sessionId, elementId)
            ?: return ControllerUtils.notFound("no such element", "Element with id $elementId not found")

        return try {
            val isDisplayed = managed.withLock {
                driver.isVisible(element.selector)
            }
            ResponseEntity.ok(WebDriverResponse(value = isDisplayed))
        } catch (e: Exception) {
            logger.error("Is element displayed failed | sessionId={} elementId={} | {}", sessionId, elementId, e.message)
            ControllerUtils.errorResponse("webdriver error", e.message ?: "Failed to check if element is displayed")
        }
    }

    /**
     * Gets an element's tag name.
     */
    @GetMapping("/element/{elementId}/name")
    suspend fun getElementTagName(
        @PathVariable sessionId: String,
        @PathVariable elementId: String,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} getting tag name from element: {}", sessionId, elementId)
        ControllerUtils.addRequestId(response)

        val managed = sessionManager.getSession(sessionId)
            ?: return ControllerUtils.notFound("session not found", "No active session with id $sessionId")

        val element = store.getElement(sessionId, elementId)
            ?: return ControllerUtils.notFound("no such element", "Element with id $elementId not found")

        return try {
            val tagName = managed.withLock {
                driver.evaluate("document.querySelector('${element.selector}')?.tagName?.toLowerCase() || ''") as? String ?: ""
            }
            ResponseEntity.ok(WebDriverResponse(value = tagName))
        } catch (e: Exception) {
            logger.error("Get tag name failed | sessionId={} elementId={} | {}", sessionId, elementId, e.message)
            ControllerUtils.errorResponse("webdriver error", e.message ?: "Failed to get tag name")
        }
    }

    /**
     * Gets an element's bounding box (rect).
     */
    @GetMapping("/element/{elementId}/rect")
    suspend fun getElementRect(
        @PathVariable sessionId: String,
        @PathVariable elementId: String,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} getting rect from element: {}", sessionId, elementId)
        ControllerUtils.addRequestId(response)

        val managed = sessionManager.getSession(sessionId)
            ?: return ControllerUtils.notFound("session not found", "No active session with id $sessionId")

        val element = store.getElement(sessionId, elementId)
            ?: return ControllerUtils.notFound("no such element", "Element with id $elementId not found")

        return try {
            val rect = managed.withLock {
                driver.boundingBox(element.selector)
            }
            val rectMap = if (rect != null) {
                mapOf(
                    "x" to rect.x,
                    "y" to rect.y,
                    "width" to rect.width,
                    "height" to rect.height
                )
            } else {
                mapOf("x" to 0, "y" to 0, "width" to 0, "height" to 0)
            }
            ResponseEntity.ok(WebDriverResponse(value = rectMap))
        } catch (e: Exception) {
            logger.error("Get rect failed | sessionId={} elementId={} | {}", sessionId, elementId, e.message)
            ControllerUtils.errorResponse("webdriver error", e.message ?: "Failed to get element rect")
        }
    }

    /**
     * Gets an element's property value.
     */
    @GetMapping("/element/{elementId}/property/{name}")
    suspend fun getElementProperty(
        @PathVariable sessionId: String,
        @PathVariable elementId: String,
        @PathVariable name: String,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} getting property {} from element: {}", sessionId, name, elementId)
        ControllerUtils.addRequestId(response)

        val managed = sessionManager.getSession(sessionId)
            ?: return ControllerUtils.notFound("session not found", "No active session with id $sessionId")

        val element = store.getElement(sessionId, elementId)
            ?: return ControllerUtils.notFound("no such element", "Element with id $elementId not found")

        return try {
            val value = managed.withLock {
                driver.selectFirstPropertyValueOrNull(element.selector, name)
            }
            ResponseEntity.ok(WebDriverResponse(value = value ?: ""))
        } catch (e: Exception) {
            logger.error("Get property failed | sessionId={} elementId={} name={} | {}", sessionId, elementId, name, e.message)
            ControllerUtils.errorResponse("webdriver error", e.message ?: "Failed to get element property")
        }
    }

    /**
     * Gets an element's computed CSS value.
     */
    @GetMapping("/element/{elementId}/css/{propertyName}")
    suspend fun getElementCssValue(
        @PathVariable sessionId: String,
        @PathVariable elementId: String,
        @PathVariable propertyName: String,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} getting CSS property {} from element: {}", sessionId, propertyName, elementId)
        ControllerUtils.addRequestId(response)

        val managed = sessionManager.getSession(sessionId)
            ?: return ControllerUtils.notFound("session not found", "No active session with id $sessionId")

        val element = store.getElement(sessionId, elementId)
            ?: return ControllerUtils.notFound("no such element", "Element with id $elementId not found")

        return try {
            val value = managed.withLock {
                driver.evaluate(
                    "window.getComputedStyle(document.querySelector('${element.selector}'))?.getPropertyValue('$propertyName') || ''"
                ) as? String ?: ""
            }
            ResponseEntity.ok(WebDriverResponse(value = value))
        } catch (e: Exception) {
            logger.error("Get CSS value failed | sessionId={} elementId={} property={} | {}", sessionId, elementId, propertyName, e.message)
            ControllerUtils.errorResponse("webdriver error", e.message ?: "Failed to get CSS value")
        }
    }

    /**
     * Converts WebDriver locator strategy to CSS selector.
     */
    private fun convertToSelector(using: String, value: String): String {
        return when (using) {
            "css selector" -> value
            "xpath" -> value
            "id" -> "#$value"
            "name" -> "[name=\"$value\"]"
            "class name" -> ".$value"
            "tag name" -> value
            else -> value
        }
    }
}
