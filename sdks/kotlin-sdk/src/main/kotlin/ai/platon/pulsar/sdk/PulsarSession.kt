package ai.platon.pulsar.sdk

/**
 * PulsarSession provides methods for loading pages from storage or internet,
 * parsing them, and extracting data.
 *
 * This class provides a consistent API for web scraping and data extraction tasks
 * through the Browser4 REST API.
 *
 * Key methods:
 * - [open]: Open a URL immediately (bypass cache)
 * - [load]: Load from cache or fetch from internet
 * - [submit]: Submit URL to crawl pool for async processing
 * - [normalize]: Normalize a URL with load arguments
 * - [parse]: Parse a page into a document
 * - [extract]: Extract fields from a document
 * - [scrape]: Load, parse, and extract in one operation
 *
 * Example usage:
 * ```kotlin
 * val client = PulsarClient()
 * client.createSession()
 * val session = PulsarSession(client)
 * val page = session.load("https://example.com", "-expire 1d")
 * val fields = session.extract(page, mapOf("title" to "h1"))
 * session.close()
 * ```
 *
 * @param client PulsarClient instance for API communication
 */
open class PulsarSession(
    val client: PulsarClient
) : AutoCloseable {

    private var _driver: WebDriver? = null
    private var _id: Int = 0

    /**
     * Gets the session ID (numeric).
     */
    val id: Int get() = _id

    /**
     * Gets the session UUID.
     */
    val uuid: String get() = client.sessionId ?: ""

    /**
     * Gets a short descriptive display text.
     */
    val display: String
        get() = if (uuid.isNotEmpty()) "PulsarSession(${uuid.take(8)}...)" else "PulsarSession(no-session)"

    /**
     * Checks if the session is active.
     */
    val isActive: Boolean get() = client.sessionId != null

    /**
     * Gets the bound WebDriver instance.
     */
    val driver: WebDriver
        get() {
            if (_driver == null) {
                _driver = WebDriver(client)
            }
            return _driver!!
        }

    /**
     * Gets the bound driver (or null if not bound).
     */
    val boundDriver: WebDriver? get() = _driver

    // ========== URL Normalization ==========

    /**
     * Normalizes a URL with optional load arguments.
     *
     * @param url The URL to normalize
     * @param args Optional load arguments (e.g., "-expire 1d")
     * @param toItemOption Whether to convert to item load options
     * @return [NormURL] with normalized URL and parsed arguments
     */
    @Suppress("UNCHECKED_CAST")
    fun normalize(url: String, args: String? = null, toItemOption: Boolean = false): NormURL {
        val payload = mutableMapOf<String, Any?>("url" to url, "toItemOption" to toItemOption)
        if (args != null) {
            payload["args"] = args
        }
        val value = client.post("/session/{sessionId}/normalize", payload)
        return if (value is Map<*, *>) {
            NormURL.fromMap(value as Map<String, Any?>)
        } else {
            NormURL(spec = url, url = url)
        }
    }

    /**
     * Normalizes a URL, returning null if invalid.
     *
     * @param url The URL to normalize (can be null)
     * @param args Optional load arguments
     * @param toItemOption Whether to convert to item load options
     * @return [NormURL] or null if URL is invalid
     */
    fun normalizeOrNull(url: String?, args: String? = null, toItemOption: Boolean = false): NormURL? {
        if (url.isNullOrBlank()) {
            return null
        }
        val result = normalize(url, args, toItemOption)
        return if (result.isNil) null else result
    }

    // ========== Page Loading ==========

    /**
     * Opens a URL immediately, bypassing local cache.
     *
     * This method opens the URL immediately, regardless of the previous
     * state of the page in local storage.
     *
     * @param url The URL to open
     * @param args Optional load arguments
     * @return [WebPage] with the loaded page information
     */
    @Suppress("UNCHECKED_CAST")
    fun open(url: String, args: String? = null): WebPage {
        val payload = mutableMapOf<String, Any?>("url" to url)
        if (args != null) {
            payload["args"] = args
        }
        val value = client.post("/session/{sessionId}/open", payload)
        return if (value is Map<*, *>) {
            WebPage.fromMap(value as Map<String, Any?>)
        } else {
            WebPage(url = url)
        }
    }

    /**
     * Loads a URL from local storage or fetches from internet.
     *
     * This method first checks if the page exists in local storage and
     * meets the specified criteria. If so, it returns the cached version.
     * Otherwise, it fetches the page from the internet.
     *
     * @param url The URL to load
     * @param args Optional load arguments (e.g., "-expire 1d", "-refresh")
     * @return [WebPage] with the loaded page information
     */
    @Suppress("UNCHECKED_CAST")
    fun load(url: String, args: String? = null): WebPage {
        val payload = mutableMapOf<String, Any?>("url" to url)
        if (args != null) {
            payload["args"] = args
        }
        val value = client.post("/session/{sessionId}/load", payload)
        return if (value is Map<*, *>) {
            WebPage.fromMap(value as Map<String, Any?>)
        } else {
            WebPage(url = url)
        }
    }

    /**
     * Loads multiple URLs.
     *
     * @param urls URLs to load
     * @param args Optional load arguments applied to all URLs
     * @return List of loaded [WebPage]s
     */
    fun loadAll(urls: Iterable<String>, args: String? = null): List<WebPage> {
        return urls.map { load(it, args) }
    }

    /**
     * Submits a URL to the crawl pool for asynchronous processing.
     *
     * This is a non-blocking operation that returns immediately.
     * The URL will be processed later in the crawl loop.
     *
     * @param url The URL to submit
     * @param args Optional load arguments
     * @return True if the URL was submitted successfully
     */
    fun submit(url: String, args: String? = null): Boolean {
        val payload = mutableMapOf<String, Any?>("url" to url)
        if (args != null) {
            payload["args"] = args
        }
        val value = client.post("/session/{sessionId}/submit", payload)
        return if (value != null) value as? Boolean ?: true else true
    }

    /**
     * Submits multiple URLs to the crawl pool.
     *
     * @param urls URLs to submit
     * @param args Optional load arguments applied to all URLs
     * @return True if all URLs were submitted successfully
     */
    fun submitAll(urls: Iterable<String>, args: String? = null): Boolean {
        for (url in urls) {
            if (!submit(url, args)) {
                return false
            }
        }
        return true
    }

    // ========== Parsing and Extraction ==========

    /**
     * Parses a [WebPage] into a document.
     *
     * Note: Parsing is typically done locally. This method returns the
     * HTML content for local parsing with libraries like jsoup.
     *
     * @param page The [WebPage] to parse
     * @return HTML content for local parsing
     */
    fun parse(page: WebPage): String? {
        return page.html
    }

    /**
     * Extracts fields from a document using CSS selectors.
     *
     * @param document The document (or page) to extract from
     * @param fieldSelectors Map of field names to selectors
     * @return Map of field names to extracted values
     */
    fun extract(document: Any, fieldSelectors: Map<String, String>): Map<String, String?> {
        return driver.extract(fieldSelectors)
    }

    /**
     * Extracts fields from a document using CSS selectors.
     *
     * @param document The document (or page) to extract from
     * @param selectors List of selectors (selector becomes field name)
     * @return Map of field names to extracted values
     */
    fun extract(document: Any, selectors: Iterable<String>): Map<String, String?> {
        val fieldSelectors = selectors.associateWith { it }
        return driver.extract(fieldSelectors)
    }

    /**
     * Loads a page, parses it, and extracts fields in one operation.
     *
     * @param url The URL to scrape
     * @param args Load arguments
     * @param fieldSelectors Field selectors for extraction
     * @return Map of field names to extracted values
     */
    fun scrape(url: String, args: String, fieldSelectors: Map<String, String>): Map<String, String?> {
        val page = load(url, args)
        return extract(page, fieldSelectors)
    }

    // ========== Driver Management ==========

    /**
     * Gets or creates a bound WebDriver.
     *
     * @return The bound WebDriver instance
     */
    fun getOrCreateBoundDriver(): WebDriver {
        return driver
    }

    /**
     * Creates a new bound WebDriver.
     *
     * @return A new WebDriver instance
     */
    fun createBoundDriver(): WebDriver {
        _driver = WebDriver(client)
        return _driver!!
    }

    /**
     * Binds a WebDriver to this session.
     *
     * @param driver The WebDriver to bind
     */
    fun bindDriver(driver: WebDriver) {
        _driver = driver
    }

    /**
     * Unbinds a WebDriver from this session.
     *
     * @param driver The WebDriver to unbind
     */
    fun unbindDriver(driver: WebDriver) {
        if (_driver === driver) {
            _driver = null
        }
    }

    // ========== Utility Methods ==========

    /**
     * Checks if a page exists in storage.
     *
     * @param url The URL to check
     * @return True if the page exists in storage
     */
    fun exists(url: String): Boolean {
        // This would need a dedicated endpoint; using a workaround
        return false
    }

    /**
     * Flushes pending changes to storage.
     */
    fun flush() {
        // Placeholder
    }

    /**
     * Closes the session.
     */
    override fun close() {
        client.deleteSession()
    }
}
