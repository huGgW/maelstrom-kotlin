package org.huggw.maelstrom.server

import org.huggw.maelstrom.message.Message
import org.huggw.maelstrom.message.MessageBody
import org.huggw.maelstrom.serialization.JsonFormatBuilder
import kotlin.reflect.KClass

class MaelstromServerBuilder internal constructor() {
    private val jsonFormatBuilder = JsonFormatBuilder()

    private val handlerMap =
        mutableMapOf<KClass<out MessageBody>, suspend (Message<out MessageBody>) -> Message<out MessageBody>>()

    inline fun <reified T : MessageBody> handler(noinline fn: suspend (Message<T>) -> Message<out MessageBody>) {
        registerHandler(fn, T::class)
    }

    @PublishedApi
    internal fun <T : MessageBody> registerHandler(
        fn: suspend (Message<T>) -> Message<out MessageBody>,
        requestMessageBodyClass: KClass<T>
    ) {
        if (handlerMap.containsKey(requestMessageBodyClass)) {
            throw IllegalArgumentException("Handler for $requestMessageBodyClass is already registered")
        }

        @Suppress("UNCHECKED_CAST")
        handlerMap[requestMessageBodyClass] =
            fn as suspend (Message<out MessageBody>) -> Message<out MessageBody>

        jsonFormatBuilder.messageBodyClass(requestMessageBodyClass)
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
