package com.twosixlabs.dart.utils

import java.util.concurrent.TimeUnit.MILLISECONDS
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util._

object AsyncDecorators {

    private val DEFAULT_TIMEOUT_MS : Int = 2000

    implicit class DecoratedFuture[ T ]( operation : Future[ T ] ) {
        def synchronously( ) : Try[ T ] = synchronously( DEFAULT_TIMEOUT_MS )

        def synchronously( timeoutMs : Int ) : Try[ T ] = Try( Await.result( operation, Duration( timeoutMs, MILLISECONDS ) ) )
    }

}
