package com.gigaspaces.quality.dashboard.server.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utils {

    public static Properties loadPropertiesFromClasspath(String classpath) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpath);
        Properties props = new Properties();
        try {
            props.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }
    
}
