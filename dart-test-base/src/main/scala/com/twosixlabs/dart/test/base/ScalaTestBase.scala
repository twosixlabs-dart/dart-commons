package com.twosixlabs.dart.test.base

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.{MatchResult, Matcher}

@deprecated( "for 3.x development, use com.twosixlabs.dart.test.base.StandardTestBase3x" )
abstract class ScalaTestBase extends AnyFlatSpec with Matchers {

    class IncludesAllMatcher[ T ]( expected : Seq[ T ] ) extends Matcher[ Seq[ T ] ] {
        def apply( left : Seq[ T ] ) : MatchResult = {
            val contains : Boolean = expected.map( left.contains ).reduce( _ && _ )
            val diff = expected.diff( left )
            MatchResult( contains, s"The Sequence did not contain all of the expected items - ${diff}", s"The Seqs contain all the same elements" )
        }
    }

    def includeAll[ T ]( right : Seq[ T ] ) : Matcher[ Seq[ T ] ] = new IncludesAllMatcher( right )
}
