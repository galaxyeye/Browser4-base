package ai.platon.pulsar.common.code

import ai.platon.pulsar.common.code.ProjectUtils.isInJar
import ai.platon.pulsar.common.printlnPro
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ProjectUtilsTest {

    @BeforeEach
    fun setUp() {
        // Skip tests if running in a JAR environment
        Assumptions.assumeFalse(isInJar(), "Tests are skipped when running in a JAR environment")
    }

    @Test
    fun testFindProjectRootDirFromCurrentDir() {
        // Assuming the current working directory is the project root
        val projectRootDir = ProjectUtils.findProjectRootDir()
        assertNotNull(projectRootDir)
        assertTrue(Files.exists(projectRootDir!!.resolve("VERSION")))
    }

    @Test
    fun testFindProjectRootDirFromStartDir(@TempDir tempDir: Path) {
        // Create a mock project structure
        val versionFile = tempDir.resolve("VERSION")
        Files.createFile(versionFile)

        val subdirectory = tempDir.resolve("subdirectory")
        Files.createDirectory(subdirectory)

        val projectRootDir = ProjectUtils.findProjectRootDir(subdirectory)
        assertNotNull(projectRootDir) { "Project root directory should be found" }
        assertEquals(tempDir, projectRootDir)
    }

    @Test
    fun testWalkToFindFiles(@TempDir tempDir: Path) {
        // Create a mock file
        val targetFile = tempDir.resolve("testFile.txt")
        Files.createFile(targetFile)

        val foundFiles = ProjectUtils.walkToFindFiles("testFile.txt", tempDir)
        assertEquals(foundFiles.size, 1)
        assertEquals(targetFile, foundFiles.first())
    }

    @Test
    fun testFindFiles() {
        printlnPro("Project root dir:")
        printlnPro(ProjectUtils.findProjectRootDir())
        printlnPro("Current dir:")
        printlnPro(Paths.get(".").toAbsolutePath().normalize())
        val foundFiles = ProjectUtils.findFiles("pulsar-core", "WebDriver.kt")
        assertEquals(foundFiles.size, 1)
        assertEquals("WebDriver.kt", foundFiles.first().fileName?.toString())
        assertEquals("PulsarSession.kt", ProjectUtils.findFiles("pulsar-core", "PulsarSession.kt").first().fileName?.toString())
    }

    @Test
    fun testFindFilesNotFound(@TempDir tempDir: Path) {
        // Create a mock project structure
        val versionFile = tempDir.resolve("VERSION")
        Files.createFile(versionFile)

        val foundFiles = ProjectUtils.findFiles("nonExistentFile.txt")
        assertTrue { foundFiles.isEmpty() }
    }
}
