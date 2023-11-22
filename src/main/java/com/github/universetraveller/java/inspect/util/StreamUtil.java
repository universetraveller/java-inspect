package com.github.universetraveller.java.inspect.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamUtil {
    public static void readFromStream(StringBuffer builder, InputStream inputStream) throws IOException{
        BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
        while (true){
		/*
            try{
                if(inputStream.available() <= 0)
                    break;
            }catch(IOException e){
                break;
            }
		*/
            if(!input.ready())
                break;
            int ret = input.read();
            if(ret == -1)
                break;
            builder.append((char)ret);
        }
        input.close();
    }
}
