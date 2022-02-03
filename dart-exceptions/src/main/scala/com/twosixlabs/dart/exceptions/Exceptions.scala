package com.twosixlabs.dart.exceptions

import java.io.{PrintWriter, StringWriter}

object Exceptions {

    def getStackTraceText( t : Throwable ) : String = {
        val sw = new StringWriter
        t.printStackTrace( new PrintWriter( sw ) )
        sw.toString
    }
}