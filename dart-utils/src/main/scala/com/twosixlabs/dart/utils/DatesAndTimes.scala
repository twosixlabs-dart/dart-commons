package com.twosixlabs.dart.utils

import java.lang.{Integer => JInt, Long => JLong}
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, LocalDateTime, OffsetDateTime, ZoneId, ZoneOffset}

object DatesAndTimes {

    val ISO_LOCAL_DATE_PATTERN : String = "yyyy-MM-dd"
    val ISO_OFFSET_DATE_TIME_PATTERN : String = "yyyy-MM-dd'T'HH:mm:ssX"

    /**
      *
      * Create an LocalDate from Epoch seconds
      *
      * NOTE - all times are UTC
      *
      * @param epochSeconds
      * @return
      */
    def localDateFromEpochSec( epochSeconds : JLong ) : LocalDate = {
        if ( epochSeconds != null ) Instant.ofEpochSecond( epochSeconds ).atZone( ZoneOffset.UTC ).toLocalDate
        else null
    }

    /**
      *
      * Create an LocalDate from Epoch seconds
      *
      * NOTE - all times are UTC
      *
      * @param epochMilliSeconds
      * @return
      */
    def localDateFromEpochMilliSec( epochMilliSeconds : JLong ) : LocalDate = {
        if ( epochMilliSeconds != null ) Instant.ofEpochMilli( epochMilliSeconds ).atZone( ZoneOffset.UTC ).toLocalDate
        else null
    }

    /**
      *
      * Print out the epoch seconds from midnight of the given LocalDate
      *
      * NOTE - all times are UTC
      *
      * @param date
      * @return
      */
    def epochSecFromLocalDate( date : LocalDate ) : JLong = {
        if ( date != null ) date.atStartOfDay.toEpochSecond( ZoneOffset.UTC ) else null
    }

    /**
      *
      * Convert from an ISO Local Date to a ISO Local date formatted string
      * (https://www.iso.org/iso-8601-date-and-time-format.html)
      *
      * @param date
      * @return
      */
    def toIsoLocalDateStr( date : LocalDate ) : String = {
        if ( date != null ) DateTimeFormatter.ISO_LOCAL_DATE.format( date )
        else null
    }

    /**
      *
      * Convert from an iso local date formatted string to a java.time.LocalDate
      * (https://www.iso.org/iso-8601-date-and-time-format.html)
      *
      * @param dateStr
      * @return
      */
    def fromIsoLocalDateStr( dateStr : String ) : LocalDate = {
        if ( dateStr != null ) LocalDate.parse( dateStr, DateTimeFormatter.ISO_LOCAL_DATE ).atStartOfDay( ZoneOffset.UTC ).toLocalDate
        else null
    }

    /**
      * Get the current UTC (GMT) date/time in a java.time.OffsetDateTime object
      */
    def timeStamp : OffsetDateTime = OffsetDateTime.now( ZoneOffset.UTC ).truncatedTo( ChronoUnit.MILLIS )

    /**
      * Converts an OffsetDateTime to epoch seconds
      *
      * @param dateTime
      * @return
      */
    def epochSecFromOffsetDateTime( dateTime : OffsetDateTime ) : JLong = {
        if ( dateTime != null ) dateTime.toEpochSecond()
        else null
    }

    /**
      * Converts an OffsetDateTime to epoch millis
      *
      * @param dateTime
      * @return
      */
    def epochMillisFromOffsetDateTime( dateTime : OffsetDateTime ) : JLong = {
        if ( dateTime != null ) dateTime.toInstant.toEpochMilli()
        else null
    }

    /**
      * Converts epoch seconds to an OffsetDateTime at UTC
      *
      * @param epochSeconds
      * @return
      */
    def offsetDateTimeFromEpochSec( epochSeconds : JLong ) : OffsetDateTime = {
        if ( epochSeconds != null ) OffsetDateTime.ofInstant( Instant.ofEpochSecond( epochSeconds ), ZoneOffset.UTC ).truncatedTo( ChronoUnit.MILLIS )
        else null
    }

    /**
      * Converts epoch millis to an OffsetDateTime at UTC
      *
      * @param epochSeconds
      * @return
      */
    def offsetDateTimeFromEpochMillis( millis : JLong ) : OffsetDateTime = {
        if ( millis != null ) OffsetDateTime.ofInstant( Instant.ofEpochMilli( millis ), ZoneOffset.UTC ).truncatedTo( ChronoUnit.MILLIS )
        else null
    }

    /**
      *
      * Convert from an OffsetDateTime to an ISO Offset Date Time formatted string at GMT time
      *
      * @param dateTime
      * @return
      */
    def toIsoOffsetDateTimeStr( dateTime : OffsetDateTime ) : String = {
        if ( dateTime != null ) DateTimeFormatter.ISO_OFFSET_DATE_TIME.format( dateTime.withOffsetSameInstant( ZoneOffset.UTC ) )
        else null
    }

    /**
      *
      * Convert from an ISO Offset Date Time formatted string to an OffsetDateTime object with UTC zone offset
      *
      * @param dateTimeStr
      * @return
      */
    def fromIsoOffsetDateTimeStr( dateTimeStr : String ) : OffsetDateTime = {
        if ( dateTimeStr != null ) {
            OffsetDateTime.parse( dateTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME ).withOffsetSameInstant( ZoneOffset.UTC )
        } else null
    }

    /**
      *
      * Returns an offset date time for midnight of a given calendar date
      *
      * @param month
      * @param day
      * @param year
      * @return
      */
    def midnightOf( month : JInt, day : JInt, year : JInt ) : OffsetDateTime = {
        if ( month != null && day != null && year != null ) {
            val date : LocalDateTime = LocalDate.of( year, month, day ).atStartOfDay()
            val zone = ZoneId.of( "UTC" )
            val zoneOffset = zone.getRules.getOffset( date )
            OffsetDateTime.of( date, zoneOffset )
        } else null
    }

    /**
      *
      * Returns an offset date time for the very end of that calendar day
      *
      * @param month
      * @param day
      * @param year
      * @return
      */
    def endOfDay( month : JInt, day : JInt, year : JInt ) : OffsetDateTime = {
        if ( month != null && day != null && year != null ) {
            val date : LocalDateTime = LocalDate.of( year, month, day ).plusDays( 1 ).atStartOfDay().minusSeconds( 1 )
            val zone = ZoneId.of( "UTC" )
            val zoneOffset = zone.getRules.getOffset( date )
            OffsetDateTime.of( date, zoneOffset )
        } else null
    }

}
