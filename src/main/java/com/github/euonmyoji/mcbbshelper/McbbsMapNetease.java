package com.github.euonmyoji.mcbbshelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;
import java.util.regex.Pattern;

import static com.github.euonmyoji.mcbbshelper.Main.scanner;

/**
 * @author yinyangshi
 */
class McbbsMapNetease {
    private static final Pattern NOT_FOUND = Pattern.compile(".*<title>提示信息.*</title>.*");

    public McbbsMapNetease() {
        System.out.println("从文件第几行开始读");
        int inputCurLine = scanner.nextInt() - 1;
        try {
            if (Files.notExists(Paths.get("mcbbsUserData.txt"))) {
                Files.createFile(Paths.get("mcbbsUserData.txt"));
            }
            if (Files.notExists(Paths.get("mcbbsMapNetease.txt"))) {
                Files.createFile(Paths.get("mcbbsMapNetease.txt"));
            }
            Scanner in = new Scanner(Paths.get("mcbbsUserData.txt"));
            for (int i = 0; i < inputCurLine; i++) {
                in.nextLine();
            }
            String urlPrefix = "https://mc.netease.com/home.php?mod=space&username=";
            new Thread(() -> {
                int curLine = inputCurLine;
                try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("mcbbsMapNetease.txt"), StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
                    String fileLine;
                    fileLoop:
                    while ((++curLine > 0) && in.hasNextLine() && (fileLine = in.nextLine()) != null) {
                        String[] data = fileLine.split("\t", 2);
                        System.out.println("reading file line:" + curLine + "    (UID:" + data[0] + "  , NAME:" + data[1] + ")             cur progress:" + (curLine / 263305.0));
                        String writePrefix = data[1] + "\t" + data[0] + "\t";
                        for (int i = 0; true; i++) {
                            try {
                                URL url = new URL(urlPrefix + URLEncoder.encode(data[1], "GBK"));
                                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), StandardCharsets.UTF_8))) {
                                    String urlLine;
                                    while ((urlLine = reader.readLine()) != null) {
                                        if (NOT_FOUND.matcher(urlLine).matches()) {
                                            writer.write(writePrefix + "可能不存在该网易用户！\n");
                                            System.out.println("end read line (可能不存在):" + curLine);
                                            writer.flush();
                                            continue fileLoop;
                                        }
                                    }
                                    writer.write(writePrefix + "可能存在\n");
                                    writer.flush();
                                }
                                break;
                            } catch (IOException e) {
                                writer.flush();
                                System.out.println("错误第" + (i + 1) + "次 5s后重试");
                                e.printStackTrace();
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        System.out.println("end read line(可能存在):" + curLine);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
