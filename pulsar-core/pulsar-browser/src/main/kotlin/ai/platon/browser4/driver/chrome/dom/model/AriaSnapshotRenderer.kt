package ai.platon.browser4.driver.chrome.dom.model

import java.util.Locale

object AriaSnapshotRenderer {
    fun render(root: OptimizedDOMTreeNode): String {
        return AriaSnapshotFormatting.render(toRenderChildren(root))
    }

    private fun toRenderChildren(node: OptimizedDOMTreeNode): List<AriaSnapshotFormatting.RenderChild> {
        val original = node.originalNode
        if (isTextNode(original)) {
            return AriaSnapshotFormatting.normalizeText(original.nodeValue)
                ?.let { listOf(AriaSnapshotFormatting.RenderChild.Text(it)) }
                ?: emptyList()
        }

        val accessibleName = accessibleName(node)
        val children = node.children
            .flatMap { child -> toRenderChildren(child) }
            .let { AriaSnapshotFormatting.normalizeChildren(it, accessibleName) }

        val role = role(node) ?: return children
        val props = renderProps(node, role, accessibleName)
        val ref = original.backendNodeId.takeIf { it != null && it > 0 }?.let { "e$it" }

        if (children.isEmpty() && props.isEmpty() && ref == null && accessibleName.isNullOrEmpty()) {
            return emptyList()
        }

        return listOf(
            AriaSnapshotFormatting.RenderChild.Node(
                AriaSnapshotFormatting.RenderNode(
                    role = role,
                    name = accessibleName,
                    checked = AriaSnapshotFormatting.triState(rawState(node, "checked", "aria-checked")),
                    disabled = AriaSnapshotFormatting.booleanAttribute(rawState(node, "disabled", "aria-disabled")),
                    expanded = AriaSnapshotFormatting.booleanAttribute(rawState(node, "expanded", "aria-expanded")),
                    level = level(node),
                    pressed = AriaSnapshotFormatting.triState(rawState(node, "pressed", "aria-pressed")),
                    selected = AriaSnapshotFormatting.booleanAttribute(rawState(node, "selected", "aria-selected")),
                    ref = ref,
                    cursorPointer = node.originalNode.isInteractable == true || node.interactiveIndex != null,
                    props = props,
                    children = children
                )
            )
        )
    }

    private fun renderProps(
        node: OptimizedDOMTreeNode,
        role: String,
        accessibleName: String?
    ): LinkedHashMap<String, String> {
        val attributes = node.originalNode.attributes
        val axProperties = axProperties(node)
        val props = linkedMapOf<String, String>()

        if (role == "link") {
            val url = attributes["href"]?.takeIf { it.isNotBlank() }
                ?: axProperties["url"]?.takeIf { it.isNotBlank() }
            if (url != null) {
                props["url"] = url
            }
        }

        if (role == "textbox") {
            val placeholder = attributes["placeholder"] ?: attributes["aria-placeholder"]
            if (!placeholder.isNullOrBlank() && placeholder != accessibleName) {
                props["placeholder"] = placeholder
            }
        }

        AriaSnapshotFormatting.normalizeText(node.originalNode.axNode?.description)
            ?.takeIf { it != accessibleName }
            ?.let { props["description"] = it }

        val consumedProperties = setOf("checked", "disabled", "expanded", "level", "pressed", "selected", "url")
        axProperties.forEach { (name, value) ->
            if (name !in consumedProperties) {
                props.putIfAbsent(name, value)
            }
        }

        return props
    }

    private fun accessibleName(node: OptimizedDOMTreeNode): String? {
        val original = node.originalNode
        val role = role(node)
        return AriaSnapshotFormatting.normalizeText(
            original.axNode?.name
                ?: original.attributes["aria-label"]
                ?: original.attributes["title"]
                ?: if (role == "img") original.attributes["alt"] else null
        )
    }

    private fun level(node: OptimizedDOMTreeNode): String? {
        return rawState(node, "level", "aria-level")?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun role(node: OptimizedDOMTreeNode): String? {
        val role = node.originalNode.axNode?.role?.trim()
            ?: node.originalNode.attributes["role"]?.trim()
        return when {
            role.isNullOrEmpty() -> if (isTextNode(node.originalNode)) null else "generic"
            role.equals("none", ignoreCase = true) || role.equals("presentation", ignoreCase = true) -> null
            else -> role
        }
    }

    private fun rawState(node: OptimizedDOMTreeNode, propertyName: String, attributeName: String): String? {
        val attributes = node.originalNode.attributes
        return axProperties(node)[propertyName] ?: attributes[propertyName] ?: attributes[attributeName]
    }

    private fun axProperties(node: OptimizedDOMTreeNode): LinkedHashMap<String, String> {
        val properties = linkedMapOf<String, String>()
        node.originalNode.axNode?.properties.orEmpty().forEach { property ->
            val name = property.name.trim().lowercase(Locale.ROOT)
            val value = normalizePropertyValue(property.value) ?: return@forEach
            properties.putIfAbsent(name, value)
        }
        return properties
    }

    private fun normalizePropertyValue(value: Any?): String? {
        return when (value) {
            null -> null
            is Boolean -> value.toString().lowercase(Locale.ROOT)
            is String -> AriaSnapshotFormatting.normalizeText(value)
            else -> value.toString().trim().takeIf { it.isNotEmpty() }
        }
    }

    private fun isTextNode(node: MergedDOMTreeNode): Boolean {
        val nodeName = node.nodeName.trim().lowercase(Locale.ROOT)
        return nodeName == "#text" || nodeName == "text"
    }
}
