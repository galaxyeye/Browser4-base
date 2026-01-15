# Kotlin SDK Design & Implementation — Review

## Goals

1. Improve the existing Kotlin SDK based on user feedback and evolving requirements.
2. Align the SDK's usage patterns with Browser4's native API.
3. Refer to Browser4 native API examples in the pulsar-examples module.
4. Provide SDK capabilities similar to the FusedActs example that uses Browser4's native API.
   - examples/browser4-examples/src/main/kotlin/ai/platon/pulsar/examples/fuse/FusedActs.kt

## Issues to Address

1. Use ai.platon.pulsar:pulsar-jsoup to parse HTML content (e.g., for PulsarSession.extract())
   - Load the page to obtain HTML, then parse it with Jsoup.
2. Add chat() to PulsarSession to match Browser4's chat API.
3. Implement a client-side PerceptiveAgent matching Browser4's PerceptiveAgent
   - Implement all PerceptiveAgent methods.
   - Support stateHistory and processTrace.
4. Reserve extension points for PageEventHandlers for future support
   - For example: PulsarSession.open(url: String, eventHandlers: PageEventHandlers)
