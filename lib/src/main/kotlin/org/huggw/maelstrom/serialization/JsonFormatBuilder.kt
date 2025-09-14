package org.huggw.maelstrom.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.serializer
import org.huggw.maelstrom.message.MessageBody
import org.huggw.maelstrom.message.ErrorBody
import org.huggw.maelstrom.message.InitBody
import org.huggw.maelstrom.message.InitOkBody
import kotlin.reflect.KClass

internal class JsonFormatBuilder internal constructor() {
    private val serializersModules = mutableListOf<SerializersModule>()

    @OptIn(InternalSerializationApi::class)
    internal fun <T : MessageBody> messageBodyClass(bodyClass: KClass<T>) {
        serializersModules.add(
            SerializersModule {
                polymorphic(MessageBody::class) {
                    subclass(bodyClass, bodyClass.serializer())
                }
            },
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    internal fun build() =
        Json {
            namingStrategy = JsonNamingStrategy.SnakeCase
            classDiscriminator = "type"
            classDiscriminatorMode = ClassDiscriminatorMode.POLYMORPHIC

            serializersModule =
                serializersModules.fold(
                    SerializersModule {
                        polymorphic(MessageBody::class) {
                            subclass(InitBody::class)
                            subclass(InitOkBody::class)
                            subclass(ErrorBody::class)
                        }
                    },
                ) { acc, module -> acc + module }
        }
}
