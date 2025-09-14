package org.huggw.maelstrom.server

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.json.Json
import org.huggw.maelstrom.exception.CrashMaelstromException
import org.huggw.maelstrom.exception.MaelstromException
import org.huggw.maelstrom.exception.NotSupportedMaelstromException
import org.huggw.maelstrom.message.ErrorBody
import org.huggw.maelstrom.message.Message
import org.huggw.maelstrom.message.MessageBody
import java.io.BufferedReader
import java.io.BufferedWriter
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KClass

class MaelstromServer internal constructor(
    private val format: Json,
    private val handlerMap: Map<KClass<out MessageBody>, suspend (Message<out MessageBody>) -> Message<out MessageBody>>,
) {
    private val reader: BufferedReader = System.`in`.bufferedReader()
    private val writer: BufferedWriter = System.`out`.bufferedWriter()

    lateinit var nodeId: String
    private set

    fun run() = runBlocking {
        val readCh = Channel<String>()
        val readProducer = launch {
            produceReadLine(readCh)
        }

        val writeCh = Channel<String>()
        val writeConsumer = launch {
            consumeWrite(writeCh)
        }

        val processor = launch {
            processJob(readCh, writeCh)
        }

        joinAll(readProducer, writeConsumer, processor)
    }

    private suspend fun produceReadLine(ch: SendChannel<String>) = withContext(Dispatchers.IO) {
        while (this.isActive) {
            if (!reader.ready()) {
                delay(1)
                continue
            }

            reader.readLine().let {
                ch.send(it)
            }
        }
    }

    private suspend fun consumeWrite(ch: ReceiveChannel<String>) = withContext(Dispatchers.IO) {
        for (str in ch) {
            writer.write(str)
            writer.flush()
        }
    }

    private suspend fun processJob(
        readCh: ReceiveChannel<String>,
        writeCh: SendChannel<String>,
    ) = coroutineScope {
        val processJobs = mutableListOf<Job>()

        for (line in readCh) {
            val job = launch {
                runCatching {
                    format.decodeFromString<Message<out MessageBody>>(line)
                }.mapCatching {
                    val handler = handlerMap[it.body::class]
                        ?: throw NotSupportedMaelstromException("Message body ${it.body::class} is not supported")

                    handler(it)
                }.recover {
                    when (it) {
                        is MaelstromException -> {
                            Message<ErrorBody>(
                                src = "todo",
                                dst = "todo",
                                body = ErrorBody(error = it, inReplyTo = -1)
                            )
                        }

                        else -> {
                            Message<ErrorBody>(
                                src = "todo",
                                dst = "todo",
                                body = ErrorBody(
                                    error = CrashMaelstromException(
                                        it.message ?: "Internal error"
                                    ),
                                    inReplyTo = -1
                                )
                            )
                        }
                    }
                }.mapCatching {
                    format.encodeToString(it)
                }.onSuccess {
                    writeCh.send(it)
                }.onFailure {
                    writeCh.send(
                        format.encodeToString(
                            Message(
                                src = "todo",
                                dst = "todo",
                                body = ErrorBody(
                                    error = CrashMaelstromException(
                                        it.message ?: "Internal error"
                                    ),
                                    inReplyTo = -1
                                )
                            )
                        )
                    )
                }
            }

            processJobs.add(job)
        }

        processJobs.joinAll()
    }
}