package org.huggw.maelstrom.message

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

typealias NodeId = String

@Serializable
data class Message<B : MessageBody>(
    val src: NodeId? = null,
    val dst: NodeId? = null,
    val body: B,
)

@Polymorphic
interface MessageBody {
    val msgId: Long?
    val inReplyTo: Long?
}
