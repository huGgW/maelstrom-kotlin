package org.huggw.maelstrom.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.huggw.maelstrom.exception.MaelstromException

@Serializable
@SerialName("error")
data class ErrorBody(
    val code: Int,
    val text: String,
    override val inReplyTo: Long,
) : MessageBody {
    override val msgId = null

    constructor(error: MaelstromException, inReplyTo: Long): this(
        inReplyTo = inReplyTo,
        code = error.code,
        text = error.text,
    )
}
