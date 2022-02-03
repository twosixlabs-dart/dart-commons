package com.twosixlabs.dart.test.utils

import java.net.InetAddress
import scala.util.Random

object NetUtils {

    /**
      *
      * For using in tests where you need to dynamically determine the hostname to use in the test environment
      *
      * @return
      */
    def getLocalhost( ) : String = InetAddress.getLocalHost.getHostAddress


    def randomPort( min : Int = 2000, max : Int = 20000 ) : Int = min + Random.nextInt( ( max - min ) + 1 )
}
