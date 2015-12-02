package org.openlmis.core.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonFileReader {
    public static String readJson(Class clazz, String fileName) {
        InputStream in = clazz.getClassLoader().getResourceAsStream("data/" + fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuffer sb = new StringBuffer();
        try {
            String line = reader.readLine();
            while (line != null) {
                sb.append(line);
                line = reader.readLine();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
