package com.github.euonmyoji.mcbbshelper;

import com.github.euonmyoji.mcbbshelper.util.Util;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yinyangshi
 */
public class WaterChecker implements Runnable {
    private static final String T_FORMATTER = "https://www.mcbbs.net/thread-%s-1-1.html";

    private static final String WATER_AREA = "https://www.mcbbs.net/forum.php?mod=forumdisplay&fid=52&filter=author&orderby=dateline&mobile=no";
    private Pattern tidPattern = Pattern.compile(".*id=\"normalthread_(?<tid>\\d+)\">.*");

    WaterChecker() {
        System.out.println("newing water checker");
        new Thread(this, "WaterChecker").start();
    }

    @Override
    public void run() {
        LinkedList<String> list = new LinkedList<>();
        if (Files.exists(Paths.get("waters"))) {
            try (BufferedReader reader = Files.newBufferedReader(Paths.get("waters"))) {
                reader.lines().forEach(list::addLast);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        while (Main.running) {
            try {
                while (Main.running) {
                    URL url = new URL(WATER_AREA);
                    HttpsURLConnection con = ((HttpsURLConnection) url.openConnection());
                    con.setDoInput(true);
                    con.setReadTimeout(5000);
                    con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
                    try (InputStreamReader reader = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
                        BufferedReader bufferedReader = new BufferedReader(reader);
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            if (Main.print) {
                                System.out.println(line);
                            }
                            Matcher matcher = tidPattern.matcher(line);
                            if (matcher.find()) {
                                String tidUrl = String.format(T_FORMATTER, matcher.group("tid"));
                                int index = list.indexOf(tidUrl);
                                if (index == -1) {
                                    String title = Util.getTitle(tidUrl);
                                    list.addLast(tidUrl);
                                    while (list.size() > 70) {
                                        list.removeFirst();
                                    }
                                    System.out.println(LocalTime.now() + " " + tidUrl + " " + title);
                                    if (Main.state) {
                                        try {
                                            Desktop.getDesktop().browse(new URI(tidUrl));
                                        } catch (IOException | URISyntaxException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                    if (index < 30 && list.size() == 70) {
                                        list.remove(index);
                                        list.addLast(tidUrl);
                                    }
                                }
                            }
                        }
                    }
                }
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        System.out.println("writing list to file");
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("waters"))) {
            for (String s : list) {
                writer.write(s + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
