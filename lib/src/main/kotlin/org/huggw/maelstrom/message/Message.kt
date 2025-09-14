package org.huggw.maelstrom.message

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message<B : MessageBody>(
    val src: String,
    val dst: String,
    val body: B,
)

@Polymorphic
interface MessageBody {
    val msgId: Long?
    val inReplyTo: Long?
}
