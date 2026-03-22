package ai.platon.pulsar.agentic.tools.builtin

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class BrowserTabToolExecutorTest {

    private lateinit var executor: BrowserTabToolExecutor

    @BeforeEach
    fun setUp() {
        executor = BrowserTabToolExecutor()
    }

    @Test
    @DisplayName("help() returns Unix-style overview with USAGE section")
    fun helpReturnsUnixStyleOverviewWithUsageSection() {
        val help = executor.help()

        assertNotNull(help)
        assertTrue(help.isNotBlank())
        assertTrue(help.contains("USAGE"))
        assertTrue(help.contains("browser4-cli tab"))
    }

    @Test
    @DisplayName("help() contains COMMANDS section with categories")
    fun helpContainsCommandsSectionWithCategories() {
        val help = executor.help()

        assertTrue(help.contains("COMMANDS"))
        assertTrue(help.contains("Navigation"))
        assertTrue(help.contains("Interaction"))
        assertTrue(help.contains("Scrolling"))
        assertTrue(help.contains("Keyboard"))
    }

    @Test
    @DisplayName("help() lists core commands")
    fun helpListsCoreCommands() {
        val help = executor.help()

        assertTrue(help.contains("open"))
        assertTrue(help.contains("navigate"))
        assertTrue(help.contains("click"))
        assertTrue(help.contains("fill"))
        assertTrue(help.contains("screenshot"))
        assertTrue(help.contains("scrollDown"))
    }

    @Test
    @DisplayName("help() contains EXAMPLES section with SKILL.md-style commands")
    fun helpContainsExamplesSectionWithSkillStyleCommands() {
        val help = executor.help()

        assertTrue(help.contains("EXAMPLES"))
        assertTrue(help.contains("browser4-cli tab open https://example.com"))
        assertTrue(help.contains("browser4-cli tab click e3"))
        assertTrue(help.contains("browser4-cli tab screenshot"))
        assertTrue(help.contains("browser4-cli tab scrollDown"))
        assertTrue(help.contains("browser4-cli tab keyDown Shift"))
    }

    @Test
    @DisplayName("help() contains hint to get per-command help")
    fun helpContainsHintForPerCommandHelp() {
        val help = executor.help()

        assertTrue(help.contains("system.help"))
    }

    @Test
    @DisplayName("help(method) returns Unix-style USAGE for click")
    fun helpMethodReturnsUnixStyleUsageForClick() {
        val help = executor.help("click")

        assertNotNull(help)
        assertTrue(help.contains("USAGE"))
        assertTrue(help.contains("browser4-cli tab click"))
    }

    @Test
    @DisplayName("help(method) includes DESCRIPTION for known method")
    fun helpMethodIncludesDescriptionForKnownMethod() {
        val help = executor.help("navigate")

        assertTrue(help.contains("DESCRIPTION"))
    }

    @Test
    @DisplayName("help(method) includes OPTIONS section for methods with arguments")
    fun helpMethodIncludesOptionsSectionForMethodsWithArguments() {
        val help = executor.help("fill")

        assertTrue(help.contains("OPTIONS"))
        assertTrue(help.contains("--selector="))
        assertTrue(help.contains("--text="))
    }

    @Test
    @DisplayName("help(method) marks required vs optional arguments")
    fun helpMethodMarksRequiredVsOptionalArguments() {
        val help = executor.help("scrollDown")

        // scrollDown has an optional 'count' argument with a default
        assertTrue(help.contains("optional"))
    }

    @Test
    @DisplayName("help(method) for method with no arguments has no OPTIONS section")
    fun helpMethodForMethodWithNoArgumentsHasNoOptionsSection() {
        val help = executor.help("reload")

        assertTrue(help.contains("USAGE"))
        assertFalse(help.contains("OPTIONS"))
    }

    @Test
    @DisplayName("help(method) returns informative message for unknown method")
    fun helpMethodReturnsInformativeMessageForUnknownMethod() {
        val help = executor.help("nonExistentMethod")

        assertTrue(help.isNotBlank())
        assertTrue(help.contains("nonExistentMethod"))
    }
}
