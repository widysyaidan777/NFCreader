package com.widy.appwidy.NFCUtils;

import android.util.Log;

import java.math.BigInteger;

public class Utils {

    public static String getHexString(byte[] buf)
    {
        Log.i("Utils","Converting Bytes to Binary String...");

        StringBuilder sb = new StringBuilder();

        for (byte b : buf)
            sb.append(String.format("%02X ", b));

        Log.w("Utils","Converted Byte string is:  "+sb.toString().trim());
        return sb.toString().trim();
    }
    public static byte[] binaryStringToByteArray(String s)
    {
        byte[] ret = new byte[(s.length()+8-1) / 8];

        BigInteger bigint = new BigInteger(s, 2);
        byte[] bigintbytes = bigint.toByteArray();

        if (bigintbytes.length > ret.length) {
            for (int i = 0; i < ret.length; i++) {
                ret[i] = bigintbytes[i+1];
            }
        }
        else {
            ret = bigintbytes;
        }
        return ret;
    }
    public static String getBinaryString(byte[] input)
    {
        Log.i("Utils","Converting Bytes to Binary String...");
        StringBuilder sb = new StringBuilder();

        for (byte c : input)
        {
            for (int n = 128; n > 0; n >>= 1)
            {
                String res = ((c & n) == 0) ? "0" : "1";
                sb.append(res);
            }
        }

        Log.w("Utils","Converted Byte string is:  "+sb.toString().trim());

        return sb.toString().trim();
    }
}