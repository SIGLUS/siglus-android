package org.openlmis.core.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonFileReader {
    public static String readJson(Class clazz, String fileName) {
        InputStream in = clazz.getClassLoader().getResourceAsStream("data/" + fileName);
        return readFromDataDirToString(in);
    }

    public static String readJson(Class clazz, String dir, String fileName) {
        InputStream inputStream = clazz.getClassLoader().getResourceAsStream(dir + "/" + fileName);
        return readFromDataDirToString(inputStream);
    }

    private static String readFromDataDirToString(InputStream in) {
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

    public static String readString(Class clazz, String fileName) {
        InputStream in = clazz.getClassLoader().getResourceAsStream("String/" + fileName);
        return readFromDataDirToString(in);
    }
}
