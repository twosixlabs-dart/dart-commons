package com.twosixlabs.dart.utils

import scala.util.{Failure, Try}

object RetryHelper {

    def retry[ T ]( numRetries : Int, pauseMillis : Long = 0 )( fn : => Try[ T ] ) : Try[ T ] = {
        fn match {
            case Failure( e ) => {
                Thread.sleep( pauseMillis )
                if ( numRetries > 1 ) {
                    retry( numRetries - 1, pauseMillis )( fn )
                } else Failure( e )
            }
            case fn => fn
        }
    }
}
