package org.huggw.maelstrom.logger

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

fun LOGGER(fn: () -> Unit): KLogger = KotlinLogging.logger(fn)