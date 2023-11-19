package com.github.universetraveller.java.inspect.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamUtil {
    public static void readFromStream(StringBuilder builder, InputStream inputStream) throws IOException{
        BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
        String getLine = null;
        while ((getLine = input.readLine()) != null){
            builder.append(getLine).append("\n");
        }
        input.close();
    }
}
