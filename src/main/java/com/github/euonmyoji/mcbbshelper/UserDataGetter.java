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
public class UserDataGetter implements Runnable {
    private Pattern namePattern = Pattern.compile("<title>(?<name>.+)的个人资料.+");

    @Override
    public void run() {
        System.out.println("请输入开始UID");
        int start = scanner.nextInt();
        System.out.println("请输入结束UID");
        int end = scanner.nextInt();
        System.out.println("请输入每个线程爬一面等待时间(毫秒)");
        int delay = scanner.nextInt();
        System.out.println("是否并行(数量基本取决于cpu核心数)");
        boolean parallel = scanner.nextBoolean();
        System.out.println("start.");
        Queue<UserData> queue = new ConcurrentLinkedQueue<>();
        new Thread(() -> {
            try (BufferedWriter out = Files.newBufferedWriter(Paths.get("userData.txt"),
                    StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
                while (Main.running) {
                    UserData signData = queue.poll();
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
        IntStream intStream = IntStream.range(start, end + 1);
        if (parallel) {
            intStream = intStream.parallel();
        }
        intStream.forEach(uid -> {
            try {
                Thread.sleep(delay);

                System.out.println("now getting uid:" + uid);
                URL url = new URL(String.format("https://www.mcbbs.net/home.php?mod=space&nomobile=1&uid=%d", uid));
                HttpsURLConnection con = ((HttpsURLConnection) url.openConnection());
                con.setDoInput(true);
                con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
                try (InputStreamReader reader = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        Matcher matcher = namePattern.matcher(line);
                        if (matcher.find()) {
                            String name = matcher.group("name");
                            queue.add(new UserData(uid, name));
                            System.out.println("uid:" + uid + " name: " + name + " got.");
                            return;
                        }
                    }
                }
                System.out.println("tried to get uid:" + uid + " but found nothing.");
            } catch (Exception e) {
                System.out.println("(error)cur uid:" + uid);
                e.printStackTrace();
            }
        });
    }

    private static class UserData {
        private final long uid;
        private final String name;

        public UserData(long uid, String name) {
            this.uid = uid;
            this.name = name;
        }

        @Override
        public String toString() {
            return uid + "\t" + name;
        }
    }
}
