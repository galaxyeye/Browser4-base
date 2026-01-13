package ai.platon.pulsar.skeleton.crawl

/**
 * The global event handlers.
 * */
object GlobalEventHandlers {
    /**
     * The page event handlers.
     *
     * The calling order rule:
     *
     * The more specific handlers has the opportunity to override the result of more general handlers.
     * */
    var pageEventHandlers: PageEventHandlers? = null

    /**
     * The server-side event handlers for broadcasting events to external listeners.
     *
     * When set, events from page event handlers will be forwarded to this handler,
     * which can broadcast them to clients via SSE or other mechanisms.
     * */
    var serverSideEventHandlers: ServerSideEventHandlers? = null
}
