package ai.platon.browser4.driver.chrome

import ai.platon.browser4.driver.chrome.experimental.RemoteBrowserProtocol
import ai.platon.pulsar.common.AppContext

/**
 * Result of resolving a DOM node to a temporary or pre-existing runtime object id.
 *
 * @property objectId The runtime object id for the node.
 * @property shouldRelease Whether the caller should release the object after use.
 */
data class ResolvedNodeObjectId(
    val objectId: String,
    val shouldRelease: Boolean,
)

/**
 * Resolves a [NodeRef] into a runtime object id.
 *
 * If the node already carries an object id, it is reused and the caller should not release it.
 * Otherwise a temporary object id is resolved via CDP DOM APIs and must be released by the caller.
 */
suspend fun resolveNodeObjectId(devTools: RemoteDevTools, node: NodeRef): ResolvedNodeObjectId? {
    node.objectId?.let { return ResolvedNodeObjectId(it, false) }

    if (!AppContext.isActive || !devTools.isOpen) {
        return null
    }

    val remoteBrowserProtocol = RemoteBrowserProtocol(devTools)
    val objectId = when {
        node.nodeId > 0 -> remoteBrowserProtocol.resolveNodeByNodeId(node.nodeId).objectId
        node.backendNodeId > 0 -> remoteBrowserProtocol.resolveNodeByBackendNodeId(node.backendNodeId).objectId
        else -> null
    }

    return objectId?.let { ResolvedNodeObjectId(it, true) }
}

suspend fun resolveNodeObjectId(remoteBrowserProtocol: RemoteBrowserProtocol, node: NodeRef): ResolvedNodeObjectId? {
    val devTools = remoteBrowserProtocol.remoteDevToolsOrNull ?: return null
    return resolveNodeObjectId(devTools, node)
}

/**
 * Releases a temporary runtime object id previously returned by [resolveNodeObjectId].
 */
suspend fun releaseNodeObjectIfNeeded(devTools: RemoteDevTools, resolved: ResolvedNodeObjectId?) {
    if (resolved?.shouldRelease != true || !AppContext.isActive || !devTools.isOpen) {
        return
    }

    val remoteBrowserProtocol = RemoteBrowserProtocol(devTools)
    runCatching { remoteBrowserProtocol.releaseObject(resolved.objectId) }
}

suspend fun releaseNodeObjectIfNeeded(remoteBrowserProtocol: RemoteBrowserProtocol, resolved: ResolvedNodeObjectId?) {
    val devTools = remoteBrowserProtocol.remoteDevToolsOrNull ?: return
    releaseNodeObjectIfNeeded(devTools, resolved)
}

/**
 * Resolves a node to a runtime object id, executes [block], and releases temporary objects automatically.
 */
suspend inline fun <T> withNodeObjectId(
    devTools: RemoteDevTools,
    node: NodeRef,
    block: suspend (String) -> T,
): T? {
    val resolved = resolveNodeObjectId(devTools, node) ?: return null

    return try {
        block(resolved.objectId)
    } finally {
        releaseNodeObjectIfNeeded(devTools, resolved)
    }
}

suspend inline fun <T> withNodeObjectId(
    remoteBrowserProtocol: RemoteBrowserProtocol,
    node: NodeRef,
    block: suspend (String) -> T,
): T? {
    val devTools = remoteBrowserProtocol.remoteDevToolsOrNull ?: return null
    return withNodeObjectId(devTools, node, block)
}


