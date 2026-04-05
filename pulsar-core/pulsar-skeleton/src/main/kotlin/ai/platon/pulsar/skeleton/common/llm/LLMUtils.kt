package ai.platon.pulsar.skeleton.common.llm

import ai.platon.pulsar.common.ResourceLoader
import ai.platon.pulsar.common.code.ProjectUtils
import ai.platon.pulsar.common.code.ProjectUtils.CODE_MIRROR_DIR
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.notExists

object LLMUtils {

    @Throws(IOException::class)
    fun copySourceFileAsResource(moduleName: String, filename: String) {
        if (ProjectUtils.isInJar()) {
            return
        }

        if (ProjectUtils.findProjectRootDir() == null) {
            // we are not in a source code project
            return
        }

        val file = ProjectUtils.findFiles(moduleName, filename).firstOrNull() ?: throw FileNotFoundException(filename)
        ProjectUtils.copySourceFileAsCodeResource(file)
    }

    /**
     * Reads the content of a source file as a resource string.
     * */
    fun readSourceFileFromResource(moduleName: String, resource: String): String {
        copySourceFileAsResource(moduleName, resource)

        val resource = "$resource.txt"
        return ResourceLoader.readString("$CODE_MIRROR_DIR/$resource")
    }

    fun writeAsResource(fileName: String, content: String): Path? {
        val baseDir = ProjectUtils.findFiles(CODE_MIRROR_DIR).firstOrNull() ?: return null
        if (baseDir.notExists()) {
            return null
        }

        val path = baseDir.resolve(fileName)
        Files.writeString(path, content)
        return path
    }
}
