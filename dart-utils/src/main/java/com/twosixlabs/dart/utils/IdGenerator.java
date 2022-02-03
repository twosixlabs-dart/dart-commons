package com.twosixlabs.dart.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IdGenerator {

    public static String getMd5Hash( byte[] bytes ) {
        try {
            return padStart( new BigInteger( 1, MessageDigest.getInstance( "MD5" ).digest( bytes ) ).toString( 16 ), 32 );
        } catch ( NoSuchAlgorithmException e ) {
            throw new RuntimeException( "Something is seriously wrong, there is no MD5 hashing algorithm in the JVM", e );
        }
    }

    private static String padStart( String original, int desiredLength ) {
        return String.format( "%1$" + desiredLength + "s", original ).replace( ' ', '0' );
    }
}