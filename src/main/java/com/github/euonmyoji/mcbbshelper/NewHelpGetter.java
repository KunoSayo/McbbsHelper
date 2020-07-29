package com.github.euonmyoji.mcbbshelper;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.euonmyoji.mcbbshelper.Main.print;

/**
 * @author yinyangshi
 */
public class NewHelpGetter implements Runnable {
    private static final Pattern PATTERN = Pattern.compile(".*(?<url>thread-[0-9]+-[0-9]+-[0-9]+\\.html).*\n*");


    @Override
    public void run() {
        LinkedList<String> list = new LinkedList<>();
        while (Main.running) {
            try {
                final String urlPrefix = "https://www.mcbbs.net/";
                URL url = new URL("https://www.mcbbs.net/forum.php?mod=guide&view=sofa&mobile=no");
                while (Main.running) {
                    URLConnection con = url.openConnection();
                    con.setReadTimeout(5000);
                    con.setConnectTimeout(5000);
                    boolean found = false;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                        String[] lines = reader.lines().toArray(String[]::new);
                        for (int i = 0; i < lines.length; ++i) {
                            String s = lines[i];
                            if (print) {
                                System.out.println(i + ":" + s);
                            }
                            if (s.contains("target=\"_blank\" class=\"xst\" >")) {
                                s = s.trim().replaceAll("(target=\"_blank\" class=\"xst\" >)|(<a href=)", "");
                                Matcher matcher = PATTERN.matcher(s);
                                if (matcher.matches()) {
                                    for (int j = 0; j < 6; ++j) {
                                        if (lines[i + j].contains("悬赏") || lines[i + j].contains("金粒")) {
                                            if (!list.contains(s)) {
                                                if (!found) {
                                                    System.out.println();
                                                    found = true;
                                                }
                                                list.addLast(s);
                                                while (list.size() > 70) {
                                                    list.removeFirst();
                                                }
                                                if (Main.state) {
                                                    try {
                                                        Desktop.getDesktop().browse(new URI(urlPrefix + matcher.group("url")));
                                                    } catch (IOException | URISyntaxException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                System.out.println(LocalTime.now() + " " + s.replace(matcher.group("url"), urlPrefix + matcher.group("url")));
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Thread.sleep(2500);
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
