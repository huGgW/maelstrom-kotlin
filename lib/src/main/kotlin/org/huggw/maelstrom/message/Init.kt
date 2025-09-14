package org.huggw.maelstrom.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("init")
data class InitBody(
    val nodeId: String,
    val nodeIds: List<String>,
    override val msgId: Long,
) : MessageBody {
    override val inReplyTo = null
}

@Serializable
@SerialName("init_ok")
data class InitOkBody(
    override val inReplyTo: Long,
) : MessageBody {
    override val msgId = null
}
