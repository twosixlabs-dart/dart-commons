package com.twosixlabs.dart.utils

import com.twosixlabs.dart.test.base.StandardTestBase3x

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, OffsetDateTime, ZoneOffset}
import java.util.Date

class DatesAndTimesTestSuite extends StandardTestBase3x {

    "Dates and Time Utils" should "convert from epoch seconds to a LocalDate" in {
        val epochSec = 583632000L
        val expected = LocalDate.of( 1988, 6, 30 )
        val actual = DatesAndTimes.localDateFromEpochSec( epochSec )

        expected shouldBe actual

    }

    "Dates and Time Utils" should "convert from epoch millisecond to a LocalDate" in {
        val epochMilliSec = 1341014400000L
        val expected = LocalDate.of( 2012, 6, 30 )
        val actual = DatesAndTimes.localDateFromEpochMilliSec( epochMilliSec )

        expected shouldBe actual

    }

    "Dates and Time Utils" should "convert from a LocalDate to epoch seconds" in {
        val localDate = LocalDate.of( 1988, 6, 30 )
        val expected = 583632000
        val actual = DatesAndTimes.epochSecFromLocalDate( localDate )
        expected shouldBe actual
    }

    "Dates and Time Utils" should "convert epoch millis to a java.time.OffsetDateTime" in {
        // 12/07/2020 @ 13:15:00 UTC time
        val millis = 1607346900000L
        val expected = OffsetDateTime.of( 2020, 12, 7, 13, 15, 0, 0, ZoneOffset.UTC )
        val actual = DatesAndTimes.offsetDateTimeFromEpochMillis( millis )
        expected shouldBe actual
    }

    "Dates and Time Utils" should "convert a java.time.OffsetDateTime to epoch millis" in {
        val date = OffsetDateTime.of( 2020, 12, 7, 13, 15, 0, 0, ZoneOffset.UTC )

        // 12/07/2020 @ 13:15:00 UTC time
        val expected = 1607346900000L

        val actual = DatesAndTimes.epochMillisFromOffsetDateTime( date )
        actual shouldBe expected
    }

    "Dates and Time Utils" should "format a LocalDate into an ISO format date string" in {
        val date = LocalDate.of( 1988, 6, 30 )
        val expected = "1988-06-30"
        val actual = DatesAndTimes.toIsoLocalDateStr( date )
        expected shouldBe actual
    }

    "Dates and Time Utils" should "generate a LocalDate from an ISO format date string" in {
        val dateStr = "1988-06-30"
        val expected = LocalDate.of( 1988, 6, 30 )
        val actual = DatesAndTimes.fromIsoLocalDateStr( dateStr )
        expected shouldBe actual
    }

    "Dates and Time Utils" should "format an OffsetDateTime as an ISO format string with UTC offset" in {
        val dateTime : OffsetDateTime = OffsetDateTime.of( 2007, 12, 3, 10, 30, 0, 0, ZoneOffset.ofHours( 2 ) )
        val expected : String = "2007-12-03T08:30:00Z"
        val actual : String = DatesAndTimes.toIsoOffsetDateTimeStr( dateTime )
        expected shouldBe actual
    }

    "Dates and Time Utils" should "convert an ISO format string into an OffsetDateTime object with UTC offset" in {
        val dateTimeStr : String = "2007-12-03T10:30+02:00"
        val expected : OffsetDateTime = OffsetDateTime.of( 2007, 12, 3, 8, 30, 0, 0, ZoneOffset.UTC )
        val actual : OffsetDateTime = DatesAndTimes.fromIsoOffsetDateTimeStr( dateTimeStr )
        expected shouldBe actual
    }

    "Dates and Time Utils" should "generate an OffsetDateTime object of the present instant (UTC), and convert it into an ISO string and back" in {
        val timestamp : OffsetDateTime = DatesAndTimes.timeStamp
        val timestampStr = DatesAndTimes.toIsoOffsetDateTimeStr( timestamp )
        val converted = DatesAndTimes.fromIsoOffsetDateTimeStr( timestampStr )
        timestamp shouldBe converted
    }

    "Dates and Time Utils" should "convert an OffsetDateTime object to epoch seconds" in {
        val dateTime = OffsetDateTime.of( 2007, 12, 3, 10, 30, 0, 567000000, ZoneOffset.ofHours( 2 ) )
        val expected = 1196670600
        DatesAndTimes.epochSecFromOffsetDateTime( dateTime ) shouldBe expected
    }

    "Dates and Time Utils" should "convert epoch seconds to an OffsetDateTime object at UTC" in {
        val epochSeconds = 1196670600
        val expected = OffsetDateTime.of( 2007, 12, 3, 8, 30, 0, 0, ZoneOffset.ofHours( 0 ) )
        DatesAndTimes.offsetDateTimeFromEpochSec( epochSeconds.toLong ) shouldBe expected
    }

    "Dates and Time Utils" should "get midnight for a given calendar date" in {
        val expected = OffsetDateTime.parse( "2019-06-30T00:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME )
        val actual = DatesAndTimes.midnightOf( 6, 30, 2019 )

        actual shouldBe expected
    }

    "Dates and Time Utils" should "get the of the day for a given calendar date" in {
        val expected = OffsetDateTime.parse( "2019-06-30T23:59:59Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME )
        val actual = DatesAndTimes.endOfDay( 6, 30, 2019 )

        actual shouldBe expected
    }
}
