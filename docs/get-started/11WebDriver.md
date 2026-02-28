WebDriver
=

[Prev](10RPA.md) | [Home](1home.md) | [Next](12massive-crawling.md)

[WebDriver](/pulsar-skeleton/src/main/kotlin/ai/platon/pulsar/skeleton/crawl/fetch/driver/WebDriver.kt) defines a concise interface to visit and manipulate webpages. The webpage is rendered to a Document Object Model (DOM) in a real browser, and the interface provides methods to control the browser, select text content and attributes of Elements, and interact with the webpage.

All actions and behaviors are optimized to mimic real people as closely as possible, such as scrolling, clicking, typing text, dragging and dropping, etc.

## Key Capabilities

The methods in this interface fall into several categories:

### 1. Browser Control
- `.navigate(url)`: Load a new web page.
- `.goBack()`, `.goForward()`, `.reload()`: Navigation history control.
- `.stop()`, `.pause()`: Lifecycle management.

### 2. Page Interaction
- `.click(selector)`: Click an element.
- `.type(selector, text)`: Type text into an input field.
- `.scrollDown()`, `.scrollTo(selector)`: Scroll the page or to an element.
- `.mouseWheelDown()`: Simulate mouse wheel scrolling.
- `.dragAndDrop()`: Perform drag and drop operations.

### 3. Data Extraction & State Checking
- `.textContent(selector)`: Get text content of the document or an element.
- `.extract(fields)`: Extract multiple fields using CSS selectors.
- `.pageSource()`: Obtain the web page source code.
- `.exists(selector)`, `.isVisible(selector)`: Check element state.
- `.screenshot()`: Capture screenshots of the page or elements.

### 4. Advanced & AI Features
- `.evaluate(expression)`: Execute JavaScript in the browser context.
- `.chat(prompt, selector)`: Interact with an AI model using element context.
- `.nanoDOMTree()`: Retrieve a lightweight DOM structure for AI analysis.

## Example

```kotlin
class WebDriverDemo(private val session: PulsarSession) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val searchBoxSelector = ".form input[type=text]"
    private val searchBoxSubmit = ".form input[type=submit]"

    val fieldSelectors = mutableMapOf(
        "title" to "#productTitle",
        "reviews" to "#acrCustomerReviewText",
        "prodDetails" to "#prodDetails"
    )

    fun options(args: String): LoadOptions {
        val options = session.options(args)

        val be = options.eventHandlers.browseEventHandlers

        be.onDocumentFullyLoaded.addLast { page, driver ->
            fieldSelectors.values.forEach { interact1(it, driver) }
        }

        be.onDocumentFullyLoaded.addLast { page, driver ->
            interact2(driver)
        }

        be.onDidInteract.addLast { page, driver ->
            logger.info("Did the interaction")
        }

        return options
    }

    private suspend fun interact1(selector: String, driver: WebDriver) {
        if (driver.exists(selector)) {
            println("click $selector ...")
            driver.click(selector)

            println("select first text by $selector ...")
            var text = driver.selectFirstTextOrNull(selector) ?: "no-text"
            text = text.substring(1, 4)

            println("type $text in $searchBoxSelector ...")
            driver.type(searchBoxSelector, text)
        }
    }

    private suspend fun interact2(driver: WebDriver) {
        val selector = "#productTitle"

        println("bring the page to front ...")
        driver.bringToFront()

        println("scroll to the bottom of the page ...")
        driver.scrollToBottom()
        println("bounding box of body: " + driver.boundingBox("body"))

        println("scroll to the middle of the page ...")
        driver.scrollToMiddle(0.5f)

        println("click $selector ...")
        driver.click(selector)

        println("query text of $selector ...")
        var text = driver.selectFirstTextOrNull(selector) ?: "no-text"
        text = text.substring(1, 4)
        println("type `$text` in $searchBoxSelector")
        driver.type(searchBoxSelector, text)

        println("capture screenshot over $selector ...")
        driver.screenshot(selector)

        println("evaluate 1 + 1 ...")
        val result = driver.evaluate("1 + 1")
        require(result is Int)
        println("evaluate 1 + 1 returns $result")

        println("wheel down for 5 times ...")
        driver.mouseWheelDown(5, delayMillis = 2000)

        println("scroll to top ...")
        driver.mouseWheelDown(5, delayMillis = 2000)
        driver.scrollToTop()

        println("search ...")
        text = "Vincent Willem van Gogh"
        println("type `$text` in $searchBoxSelector")
        driver.type(searchBoxSelector, text)
        driver.click(searchBoxSubmit)
        val url = driver.currentUrl()
        println("the page navigated to $url")
    }
}
```

Complete code: [kotlin](/examples/browser4-examples/src/main/kotlin/ai/platon/pulsar/manual/_8_WebDriver.kt), [Chinese mirror](https://gitee.com/platonai_galaxyeye/browser4/blob/1.10.x/examples/browser4-examples/src/main/kotlin/ai/platon/pulsar/manual/_8_WebDriver.kt).

------

[Prev](10RPA.md) | [Home](1home.md) | [Next](12massive-crawling.md)
