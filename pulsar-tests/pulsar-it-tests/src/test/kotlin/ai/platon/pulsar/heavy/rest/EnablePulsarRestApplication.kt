package ai.platon.pulsar.heavy.rest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ImportResource

@SpringBootApplication
@ComponentScan("ai.platon.pulsar.rest")
@ImportResource("classpath:rest-beans/app-context.xml")
class EnablePulsarRestApplication
