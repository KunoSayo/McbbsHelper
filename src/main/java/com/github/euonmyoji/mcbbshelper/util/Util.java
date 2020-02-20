package com.github.euonmyoji.mcbbshelper.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yinyangshi
 */
public class Util {
    private static final Pattern TITLE_PATTERN = Pattern.compile("<title>(?<title>.*)</title>.*");

    public static String getTitle(String url) throws IOException {
        URL u = new URL(url);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(u.openConnection().getInputStream()))) {
            String line;
            while((line = reader.readLine()) != null) {
                Matcher matcher = TITLE_PATTERN.matcher(line);
                if(matcher.find()) {
                    return matcher.group("title");
                }
            }
        }
        return "{TITLE}";
    }
}
