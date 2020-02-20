package com.github.euonmyoji.mcbbshelper;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.github.euonmyoji.mcbbshelper.Main.scanner;

/**
 * @author yinyangshi
 */
public class SignDataGetter implements Runnable {
    private Pattern namePattern = Pattern.compile(".*<.+>(?<name>.+)</a>.*");
    private Pattern valuePattern = Pattern.compile(".*<td>(?<value>.+)</td>.*");

    @Override
    public void run() {
        System.out.println("请输入开始页");
        int start = scanner.nextInt();
        System.out.println("请输入结束页");
        int end = scanner.nextInt();
        System.out.println("请输入cookie 用于登陆");
        scanner.nextLine();
        String cookie = scanner.nextLine();
        System.out.println("确认cookie:" + cookie);
        System.out.println("请输入爬一面等待时间(毫秒)");
        int delay = scanner.nextInt();
        Queue<SignData> queue = new ConcurrentLinkedQueue<>();
        new Thread(() -> {
            try (BufferedWriter out = Files.newBufferedWriter(Paths.get("result.txt"),
                    StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
                while (Main.running) {
                    SignData signData = queue.poll();
                    if (signData != null) {
                        out.write(signData + "\n");
                        while ((signData = queue.poll()) != null) {
                            out.write(signData + "\n");
                        }
                        out.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        IntStream.range(start, end + 1).parallel().forEach(page -> {
            try {
                Thread.sleep(delay);
                System.out.println("now get page:" + page);
                URL url = new URL(String.format("https://www.mcbbs.net/plugin.php?id=dc_signin&mobile=no&action=qdlist&page=%d", page));
//                HttpClient client = new HttpClient(url, null, 0);
                HttpsURLConnection con = ((HttpsURLConnection) url.openConnection());
                con.setDoInput(true);
//                con.setRequestProperty("Cookie",
//                        String.format("Set－Cookie: ZxYQ_8cea_auth=%s；Expires=DATE；Path=PATH；Domain=DOMAIN_NAME；SECURE",
//                                "4290UGO8I54yG0hKPiEiIU0vHpGUIGeRTTqu2UGIzyXTAL3r7z7GnH%2F5YE828ffnl%2FNP3XpYK5yPmb4BLQhAdlD5o4g"));
                con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
                con.setRequestProperty("Cookie", cookie);
                try (InputStreamReader reader = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
                    String[] lines = new BufferedReader(reader).lines().toArray(String[]::new);
                    for (int i = 0; i < lines.length; i++) {
                        String line = lines[i].trim();
                        if (line.contains("dt mtm")) {
                            //noinspection StatementWithEmptyBody
                            while (!lines[++i].contains("<tbody>")) ;
                            //noinspection StatementWithEmptyBody
                            while (!lines[++i].contains("</tr>")) ;
                            while (Main.running && !line.contains("pgs cl mtm")) {
                                while (!(line = lines[++i]).contains("</tr>")) {
                                    //skip empty
                                    SignData data = new SignData();
                                    Matcher matcher;
                                    while (!(matcher = namePattern.matcher(lines[++i])).find()) ;
                                    data.user = matcher.group("name");
                                    while (!(matcher = valuePattern.matcher(lines[++i])).find()) ;
                                    data.feel = matcher.group("value");
                                    while (!(matcher = valuePattern.matcher(lines[++i])).find()) ;
                                    data.reason = matcher.group("value");

                                    while (!(matcher = valuePattern.matcher(lines[++i])).find()) ;
                                    data.reward = matcher.group("value");

                                    while (!(matcher = valuePattern.matcher(lines[++i])).find()) ;
                                    data.time = matcher.group("value");
                                    queue.add(data);
                                }
                            }
                            break;
                        }
                    }
                }
                System.out.println("end page:" + page);
            } catch (ArrayIndexOutOfBoundsException ignore) {
                System.out.println("end page:" + page);
            } catch (Exception e) {
                System.out.println("(error)cur page:" + page);
                e.printStackTrace();
            }
        });
    }

    static class SignData {

        private String user;
        private String feel;
        private String reason;
        private String time;
        private String reward;

        @Override
        public String toString() {
            return String.format("%s\t\t%s\t\t%s\t\t%s\t\t%s", user, feel, reason, time, reward);
        }
    }

}
