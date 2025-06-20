package com.duck.cryptoroomdbtestapp

import com.duck.flexilogger.FlexiLog
import com.duck.flexilogger.LogType

object Log : FlexiLog() {
    override fun canLogToConsole(type: LogType): Boolean {
        return true
    }

    override fun shouldReport(type: LogType): Boolean {
        return false
    }

    override fun shouldReportException(tr: Throwable): Boolean {
        return false
    }

    override fun report(
        type: LogType,
        tag: String,
        msg: String
    ) {
        //no-op
    }

    override fun report(
        type: LogType,
        tag: String,
        msg: String,
        tr: Throwable
    ) {
        //no-op
    }
}