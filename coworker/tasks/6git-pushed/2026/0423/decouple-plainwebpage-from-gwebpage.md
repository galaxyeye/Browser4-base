# Remove the dependency of PlainWebPage on GWebPage

PlainWebPage is designed to be a data class that holds information about a web page, such as its URL, content type, and content length.
However, it currently relies on GWebPage, which is a more complex class that includes additional functionality and dependencies.

We should refactor PlainWebPage to remove its dependency on GWebPage and instead use appropriate native data types for the related cost variables.
This will simplify the design of PlainWebPage and make it more focused on its core purpose of holding web page information.

Remove the dependency of PlainWebPage on GWebPage, and use appropriate native data for the
related cost variables. For example:

```kotlin
override var contentType
    get() = if (page.contentType == null) "" else page.contentType.toString()
    set(value) {
        page.contentType = value.trim().lowercase(Locale.getDefault())
    }
```

=>

```kotlin
override var lastContentLength: Long
```

The final target is to make PlainWebPage a simple data class that easy to use, test and persist.
