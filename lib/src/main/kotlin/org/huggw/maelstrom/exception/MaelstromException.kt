package org.huggw.maelstrom.exception

open class MaelstromException(
    val code: Int,
    val text: String,
) : Exception() {
    override val message: String
        get() = text
}

class TimeoutMaelstromException(text: String) : MaelstromException(0, text)
class NotSupportedMaelstromException(text: String) : MaelstromException(10, text)
class TemporarilyUnavailableMaelstromException(text: String) : MaelstromException(11, text)
class MalformedRequestMaelstromException(text: String) : MaelstromException(12, text)
class CrashMaelstromException(text: String) : MaelstromException(13, text)
class AbortMaelstromException(text: String) : MaelstromException(14, text)
class KeyDoesNotExistMaelstromException(text: String) : MaelstromException(20, text)
class KeyAlreadyExistsMaelstromException(text: String) : MaelstromException(21, text)
class PreconditionFailedMaelstromException(text: String) : MaelstromException(22, text)
class TxnConflictMaelstromException(text: String) : MaelstromException(30, text)
