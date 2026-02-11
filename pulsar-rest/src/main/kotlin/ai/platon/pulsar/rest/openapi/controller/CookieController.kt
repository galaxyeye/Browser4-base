package ai.platon.pulsar.rest.openapi.controller

import ai.platon.pulsar.rest.openapi.dto.*
import ai.platon.pulsar.rest.openapi.service.SessionManager
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controller for cookie operations.
 */
@RestController
@CrossOrigin
@RequestMapping(
    "/session/{sessionId}",
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
@ConditionalOnBean(SessionManager::class)
class CookieController(
    private val sessionManager: SessionManager
) {
    private val logger = LoggerFactory.getLogger(CookieController::class.java)

    /**
     * Gets all cookies.
     */
    @GetMapping("/cookie")
    suspend fun getAllCookies(
        @PathVariable sessionId: String,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} getting all cookies", sessionId)
        ControllerUtils.addRequestId(response)

        val managed = sessionManager.getSession(sessionId)
            ?: return ControllerUtils.notFound("session not found", "No active session with id $sessionId")

        return try {
            val cookies = managed.withLock {
                driver.getCookies().map { cookieMap ->
                    Cookie(
                        name = cookieMap["name"] as? String ?: "",
                        value = cookieMap["value"] as? String ?: "",
                        domain = cookieMap["domain"] as? String,
                        path = cookieMap["path"] as? String,
                        expires = (cookieMap["expires"] as? Number)?.toLong(),
                        httpOnly = cookieMap["httpOnly"] as? Boolean,
                        secure = cookieMap["secure"] as? Boolean,
                        sameSite = cookieMap["sameSite"] as? String
                    )
                }
            }
            ResponseEntity.ok(CookiesResponse(value = cookies))
        } catch (e: Exception) {
            logger.error("Get cookies failed | sessionId={} | {}", sessionId, e.message)
            ControllerUtils.errorResponse("webdriver error", e.message ?: "Failed to get cookies")
        }
    }

    /**
     * Adds a cookie.
     */
    @PostMapping("/cookie", consumes = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun addCookie(
        @PathVariable sessionId: String,
        @RequestBody request: AddCookieRequest,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} adding cookie: {}", sessionId, request.cookie.name)
        ControllerUtils.addRequestId(response)

        val managed = sessionManager.getSession(sessionId)
            ?: return ControllerUtils.notFound("session not found", "No active session with id $sessionId")

        return try {
            managed.withLock {
                // Use JavaScript to add cookie
                val cookie = request.cookie
                val cookieString = buildString {
                    append("${cookie.name}=${cookie.value}")
                    cookie.domain?.let { append("; domain=$it") }
                    cookie.path?.let { append("; path=$it") }
                    cookie.expires?.let { append("; expires=${java.util.Date(it)}") }
                    if (cookie.httpOnly == true) append("; HttpOnly")
                    if (cookie.secure == true) append("; Secure")
                    cookie.sameSite?.let { append("; SameSite=$it") }
                }
                driver.evaluate("document.cookie = '${cookieString.replace("'", "\\'")}'")
            }
            ResponseEntity.ok(WebDriverResponse<Any?>(value = null))
        } catch (e: Exception) {
            logger.error("Add cookie failed | sessionId={} | {}", sessionId, e.message)
            ControllerUtils.errorResponse("webdriver error", e.message ?: "Failed to add cookie")
        }
    }

    /**
     * Deletes all cookies.
     */
    @DeleteMapping("/cookie")
    suspend fun deleteAllCookies(
        @PathVariable sessionId: String,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} deleting all cookies", sessionId)
        ControllerUtils.addRequestId(response)

        val managed = sessionManager.getSession(sessionId)
            ?: return ControllerUtils.notFound("session not found", "No active session with id $sessionId")

        return try {
            managed.withLock {
                driver.clearBrowserCookies()
            }
            ResponseEntity.ok(WebDriverResponse<Any?>(value = null))
        } catch (e: Exception) {
            logger.error("Delete all cookies failed | sessionId={} | {}", sessionId, e.message)
            ControllerUtils.errorResponse("webdriver error", e.message ?: "Failed to delete all cookies")
        }
    }

    /**
     * Deletes a cookie by name.
     */
    @DeleteMapping("/cookie/{name}")
    suspend fun deleteCookie(
        @PathVariable sessionId: String,
        @PathVariable name: String,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.debug("Session {} deleting cookie: {}", sessionId, name)
        ControllerUtils.addRequestId(response)

        val managed = sessionManager.getSession(sessionId)
            ?: return ControllerUtils.notFound("session not found", "No active session with id $sessionId")

        return try {
            managed.withLock {
                driver.deleteCookies(name)
            }
            ResponseEntity.ok(WebDriverResponse<Any?>(value = null))
        } catch (e: Exception) {
            logger.error("Delete cookie failed | sessionId={} name={} | {}", sessionId, name, e.message)
            ControllerUtils.errorResponse("webdriver error", e.message ?: "Failed to delete cookie")
        }
    }
}
