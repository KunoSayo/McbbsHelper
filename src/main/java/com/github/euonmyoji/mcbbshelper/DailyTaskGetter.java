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
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.euonmyoji.mcbbshelper.Main.scanner;

/**
 * @author yinyangshi
 */
public class DailyTaskGetter {
    private static final String TASK_ONE = "https://www.mcbbs.net/home.php?mod=task&do=parter&nomobile=1&id=39";
    private static final String TASK_TWO = "https://www.mcbbs.net/home.php?mod=task&do=parter&nomobile=1&id=22";
    private static final Pattern NAME_PATTERN = Pattern.compile(".*<.+查看详细资料.+>(?<name>.+)</a>.*");

    public DailyTaskGetter() {
        System.out.println("请输入cookie 用于登陆");
        String cookie = scanner.nextLine();
        System.out.println("确认cookie:" + cookie);
        System.out.println("请输入每个任务刷新一次等待时间(毫秒)");
        int delay = scanner.nextInt();
        Queue<String> task1Queue = new ConcurrentLinkedQueue<>();
        Queue<String> task2Queue = new ConcurrentLinkedQueue<>();
        new Thread(() -> {
            try (BufferedWriter out1 = Files.newBufferedWriter(Paths.get("task1.txt"),
                    StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                 BufferedWriter out2 = Files.newBufferedWriter(Paths.get("task2.txt"),
                         StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
                while (Main.running) {
                    String str = task1Queue.poll();
                    if (str != null) {
                        out1.write(str + "\n");
                        while ((str = task1Queue.poll()) != null) {
                            out1.write(str + "\n");
                        }
                        out1.flush();
                    }
                    str = task2Queue.poll();
                    if(str != null) {
                        out2.write(str + "\n");
                        while ((str = task2Queue.poll()) != null) {
                            out2.write(str + "\n");
                        }
                        out2.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            LinkedList<String> list = new LinkedList<>();
            while (Main.running) {
                try {
                    while (Main.running) {
                        URL url = new URL(TASK_ONE);
                        HttpsURLConnection con = ((HttpsURLConnection) url.openConnection());
                        con.setDoInput(true);
                        con.setRequestProperty("Cookie", cookie);
                        try (InputStreamReader reader = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
                            String[] lines = new BufferedReader(reader).lines().toArray(String[]::new);
                            Matcher matcher;
                            String name;
                            LocalDateTime now = LocalDateTime.now();
                            for (String line : lines) {
                                matcher = NAME_PATTERN.matcher(line);
                                if (matcher.find()) {
                                    name = matcher.group("name");
                                    if (!list.contains(name)) {
                                        list.addLast(name);
                                        while (list.size() > 70) {
                                            list.removeFirst();
                                        }
                                        System.out.println("[Task1]" + now + "\t" + name);
                                        task1Queue.add(now + "\t" + name);
                                    }
                                }
                            }
                        }
                        Thread.sleep(delay);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "task1").start();

        new Thread(() -> {
            LinkedList<String> list = new LinkedList<>();
            while (Main.running) {
                try {
                    while (Main.running) {
                        URL url = new URL(TASK_TWO);
                        HttpsURLConnection con = ((HttpsURLConnection) url.openConnection());
                        con.setDoInput(true);
                        con.setRequestProperty("Cookie", cookie);
                        try (InputStreamReader reader = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
                            String[] lines = new BufferedReader(reader).lines().toArray(String[]::new);
                            Matcher matcher;
                            String name;
                            LocalDateTime now = LocalDateTime.now();
                            for (String line : lines) {
                                matcher = NAME_PATTERN.matcher(line);
                                if (matcher.find()) {
                                    name = matcher.group("name");
                                    if (!list.contains(name)) {
                                        list.addLast(name);
                                        while (list.size() > 70) {
                                            list.removeFirst();
                                        }
                                        System.out.println("[Task2]" + now + "\t" + name);
                                        task2Queue.add(now + "\t" + name);
                                    }
                                }
                            }
                        }
                        Thread.sleep(delay);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "task2").start();
    }
}
