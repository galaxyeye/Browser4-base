package ai.platon.pulsar.rest

import ai.platon.pulsar.skeleton.TaskLoops
import ai.platon.pulsar.skeleton.workflow.common.GlobalCache
import ai.platon.pulsar.skeleton.workflow.common.GlobalCacheFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ImportResource

@SpringBootApplication
@ImportResource("classpath:rest-beans/app-context.xml")
class ApiApplication(
    val globalCache: GlobalCache,
    val globalCacheFactory: GlobalCacheFactory,
    val taskLoops: TaskLoops
)

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args) {
        addInitializers(PulsarContextInitializer())
        setAdditionalProfiles("rest", "private", "advanced")
        setLogStartupInfo(true)
    }
}
