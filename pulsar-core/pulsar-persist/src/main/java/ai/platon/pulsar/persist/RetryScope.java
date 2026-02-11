package ai.platon.pulsar.persist;

/**
 * Retry scope indicates in which scope or subsystem to perform the retrying.
 */
public enum RetryScope {
    /**
     * Retry in crawl schedule scope
     * */
    CRAWL,
    /**
     * Retry in fetch protocol scope, ignored in browser emulation mode
     * */
    PROTOCOL,
    /**
     * Change the privacy context and retry
     * */
    PRIVACY,
}
