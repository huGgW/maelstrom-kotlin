package org.huggw.maelstrom.server

import org.huggw.maelstrom.message.Message
import org.huggw.maelstrom.message.MessageBody
import org.huggw.maelstrom.message.NodeId
import org.huggw.maelstrom.serialization.JsonFormatBuilder
import kotlin.reflect.KClass

class MaelstromServerBuilder internal constructor() {
    private val jsonFormatBuilder = JsonFormatBuilder()

    private val handlerMap =
        mutableMapOf<KClass<out MessageBody>, suspend (Message<out MessageBody>) -> Pair<Message<out MessageBody>>, List<NodeId>>()

    inline fun <reified T : MessageBody, reified S : MessageBody> handler(noinline fn: suspend (Message<T>) -> Pair<Message<S>, List<NodeId>>) {
        registerHandler(fn, T::class, S::class)
    }

    @PublishedApi
    internal fun <T : MessageBody, S : MessageBody> registerHandler(
        fn: suspend (Message<T>) -> Pair<Message<S>, List<NodeId>>,
        requestMessageBodyClass: KClass<T>,
        responseMessageBodyClass: KClass<S>,
    ) {
        if (handlerMap.containsKey(requestMessageBodyClass)) {
            throw IllegalArgumentException("Handler for $requestMessageBodyClass is already registered")
        }

        @Suppress("UNCHECKED_CAST")
        handlerMap[requestMessageBodyClass] =
            fn as suspend (Message<out MessageBody>) -> Message<out MessageBody>

        jsonFormatBuilder.messageBodyClass(requestMessageBodyClass)
        jsonFormatBuilder.messageBodyClass(responseMessageBodyClass)
    }

    internal fun build(): MaelstromServer =
        MaelstromServer(
            format = jsonFormatBuilder.build(),
            handlerMap = handlerMap,
        )
}

fun MaelstromServer(block: MaelstromServerBuilder.() -> Unit): MaelstromServer {
    val builder = MaelstromServerBuilder()
    builder.apply(block)
    return builder.build()
}
