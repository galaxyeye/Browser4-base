
package ai.platon.pulsar.skeleton.crawl.schedule

import ai.platon.pulsar.common.config.ImmutableConfig
import ai.platon.pulsar.skeleton.common.message.MiscMessageMessageWriter

class DefaultFetchSchedule(
        conf: ImmutableConfig,
        messageWriter: MiscMessageMessageWriter? = null
) : AbstractFetchSchedule(conf, messageWriter)
