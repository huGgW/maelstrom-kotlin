package org.huggw.maelstrom.server

import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.huggw.maelstrom.message.Message
import org.huggw.maelstrom.message.MessageBody
import kotlin.reflect.KClass

class MaelstromServer internal constructor(
    private val format: Json,
    private val handlerMap: Map<KClass<out MessageBody>, suspend (Message<out MessageBody>) -> Message<out MessageBody>>,
) {
}