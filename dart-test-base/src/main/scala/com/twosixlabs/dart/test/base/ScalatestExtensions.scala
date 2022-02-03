package com.twosixlabs.dart.test.base

import org.scalatest.matchers.{MatchResult, Matcher}

trait ScalatestExtensions {

    class IncludesAllMatcher[ T ]( expected : Seq[ T ] ) extends Matcher[ Seq[ T ] ] {
        def apply( left : Seq[ T ] ) : MatchResult = {
            val contains : Boolean = expected.map( left.contains ).reduce( _ && _ )
            val diff = expected.diff( left )
            MatchResult( contains, s"The Sequence did not contain all of the expected items - ${diff}", s"The Seqs contain all the same elements" )
        }
    }

    def includeAll[ T ]( right : Seq[ T ] ) : Matcher[ Seq[ T ] ] = new IncludesAllMatcher( right )

}
