package com.mtislab.core.data.logging
import co.touchlab.kermit.Logger
import com.mtislab.core.domain.logging.CelvoLogger


object KermitLogger: CelvoLogger {

    override fun debug(message: String) {
        Logger.d(message)
    }

    override fun info(message: String) {
        Logger.i(message)
    }

    override fun warn(message: String) {
        Logger.w(message)
    }

    override fun error(message: String, throwable: Throwable?) {
        Logger.e(message, throwable)
    }
}