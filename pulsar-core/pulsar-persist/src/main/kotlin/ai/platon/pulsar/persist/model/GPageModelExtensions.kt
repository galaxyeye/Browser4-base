package ai.platon.pulsar.persist.model

import ai.platon.pulsar.persist.PersistUtils.u8
import ai.platon.pulsar.persist.WebPage
import ai.platon.pulsar.persist.gora.generated.GPageModel

fun newGPageModel(): GPageModel = GPageModel.newBuilder().build()

val WebPage.gPageModel: GPageModel?
    get() = (this as? GoraWebPage)?.unbox()?.pageModel

fun WebPage.ensureGPageModel(): GPageModel {
    val goraPage = this as? GoraWebPage
        ?: error("Page model storage is only supported for GoraWebPage, got ${this::class.qualifiedName}")
    return goraPage.unbox().pageModel ?: newGPageModel().also { goraPage.unbox().pageModel = it }
}

@get:Synchronized
val GPageModel.unboxedFieldGroups get() = fieldGroups

@get:Synchronized
val GPageModel.boxedFieldGroups get() = unboxedFieldGroups.map { FieldGroup.box(it) }

@get:Synchronized
val GPageModel.numGroups get() = unboxedFieldGroups.size

@get:Synchronized
val GPageModel.numFields get() = unboxedFieldGroups.sumOf { it.fields.size }

@get:Synchronized
val GPageModel.numNonNullFields get() = unboxedFieldGroups.sumOf { it.fields.count { value -> value.value != null } }

@get:Synchronized
val GPageModel.numNonBlankFields get() = unboxedFieldGroups.sumOf { it.fields.count { value -> !value.value.isNullOrBlank() } }

@get:Synchronized
val GPageModel.isEmpty: Boolean get() = unboxedFieldGroups.isEmpty()

@get:Synchronized
val GPageModel.isNotEmpty: Boolean get() = !isEmpty

@Synchronized
fun GPageModel.firstOrNull(): FieldGroup? = unboxedFieldGroups.firstOrNull()?.let { FieldGroup.box(it) }

@Synchronized
fun GPageModel.lastOrNull(): FieldGroup? = unboxedFieldGroups.lastOrNull()?.let { FieldGroup.box(it) }

@Synchronized
operator fun GPageModel.get(index: Int): FieldGroup? = unboxedFieldGroups.getOrNull(index)?.let { FieldGroup.box(it) }

@Synchronized
fun GPageModel.getValue(index: Int, name: String) = unboxedFieldGroups.getOrNull(index)?.let { FieldGroup.box(it) }?.get(name)

@Synchronized
fun GPageModel.findGroup(groupId: Int): FieldGroup? {
    val gFieldGroup = unboxedFieldGroups.firstOrNull { it.id == groupId.toLong() }
    return gFieldGroup?.let { FieldGroup.box(it) }
}

@Synchronized
fun GPageModel.findValue(groupId: Int, name: String): String? = findGroup(groupId)?.get(name)

@Synchronized
fun GPageModel.add(fieldGroup: FieldGroup) {
    unboxedFieldGroups.add(fieldGroup.unbox())
    setDirty()
}

@Synchronized
fun GPageModel.add(index: Int, fieldGroup: FieldGroup) {
    unboxedFieldGroups.add(index, fieldGroup.unbox())
    setDirty()
}

@Synchronized
fun GPageModel.put(groupId: Int, name: String, value: String): Pair<FieldGroup, CharSequence?> {
    val group = findGroup(groupId)
    val parentId = group?.parentId?.toInt() ?: 0
    val groupName = group?.name ?: ""
    return put0(groupId, parentId, groupName, name, value)
}

@Synchronized
fun GPageModel.emplace(groupId: Int, fields: Map<String, String?>): FieldGroup {
    return emplace(groupId, 0, "", fields)
}

@Synchronized
fun GPageModel.emplace(groupId: Int, groupName: String, fields: Map<String, String?>): FieldGroup {
    return emplace(groupId, 0, groupName, fields)
}

@Synchronized
fun GPageModel.emplace(groupId: Int, parentId: Int, groupName: String, fields: Map<String, String?>): FieldGroup {
    return emplace0(groupId, parentId, groupName, fields)
}

@Synchronized
fun GPageModel.remove(groupId: Int) {
    unboxedFieldGroups.removeIf { it.id == groupId.toLong() }
    setDirty()
}

@Synchronized
fun GPageModel.remove(groupId: Int, key: String): String? {
    val gFieldGroup = findRawById(groupId) ?: return null
    val oldValue = gFieldGroup.fields.remove(u8(key)) ?: return null

    gFieldGroup.setDirty()
    setDirty()

    return oldValue.toString()
}

@Synchronized
fun GPageModel.clear() {
    unboxedFieldGroups.clear()
    setDirty()
}

@Synchronized
fun GPageModel.deepCopy(): GPageModel = GPageModel.newBuilder(this).build()

private fun GPageModel.findRawById(groupId: Int) = unboxedFieldGroups.firstOrNull { it.id == groupId.toLong() }

@Synchronized
private fun GPageModel.emplace0(groupId: Int, parentId: Int, groupName: String, fields: Map<String, String?>): FieldGroup {
    var gFieldGroup = unboxedFieldGroups.firstOrNull { it.id == groupId.toLong() }
    if (gFieldGroup == null) {
        gFieldGroup = FieldGroup.newGFieldGroup(groupId, groupName, parentId)
        unboxedFieldGroups.add(gFieldGroup)
    }

    gFieldGroup.fields.clear()
    fields.entries.associateTo(gFieldGroup.fields) { u8(it.key) to it.value }

    gFieldGroup.setDirty()
    setDirty()

    return FieldGroup.box(gFieldGroup)
}

@Synchronized
private fun GPageModel.put0(
    groupId: Int,
    parentId: Int,
    groupName: String,
    name: String,
    value: String
): Pair<FieldGroup, CharSequence?> {
    var gFieldGroup = unboxedFieldGroups.firstOrNull { it.id == groupId.toLong() }
    if (gFieldGroup == null) {
        gFieldGroup = FieldGroup.newGFieldGroup(groupId, groupName, parentId)
        unboxedFieldGroups.add(gFieldGroup)
    }

    val u8key = u8(name)
    val oldValue = gFieldGroup.fields.put(u8key, value)
    gFieldGroup.setDirty()
    setDirty()

    return FieldGroup.box(gFieldGroup) to oldValue
}
