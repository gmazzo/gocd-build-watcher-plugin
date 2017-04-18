package com.github.gmazzo.utils;

import java.io.InputStream;
import java.util.Scanner;

public final class IOUtils {

    public static String readStream(InputStream stream) {
        return new Scanner(stream).useDelimiter("\\Z").next();
    }

}
