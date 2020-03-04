package com.github.euonmyoji.mcbbshelper;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 * @author yinyangshi
 */
public class Main {
    static volatile boolean running = true;
    static volatile boolean state = false;
    static volatile boolean print = false;
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        help();
        new Thread(Main::getCommand).start();
    }

    private static void getCommand() {
        while (running && scanner.hasNextLine()) {
            String s = scanner.nextLine();
            switch (s) {
                case "1": {
                    new UserDataGetter().run();
                    break;
                }
                case "2": {
                    new SignDataGetter().run();
                }
                case "3": {
                    new DailyTaskGetter();
                    break;
                }
                case "help": {
                    System.out.println("输入1爬用户 输入2爬签到 输入3爬日常任务1|2页面   输入6爬头像  任意时刻有效(大概)");
                    break;
                }
                case "stop": {
                    running = false;
                    break;
                }
                case "check": {
                    System.out.println("是否自动打开:" + state + " print:" + print);
                    break;
                }
                case "on": {
                    state = true;
                    break;
                }
                case "print": {
                    print = !print;
                    break;
                }
                case "off": {
                    state = false;
                    break;
                }
                case "water": {
                    new WaterChecker();
                    break;
                }
                case "map": {
                    new McbbsMapNetease();
                    break;
                }
                case "4":
                case "getNewHelp": {
                    System.out.println("开始爬最新问答 from 导读");
                    new Thread(new NewHelpGetter()).start();
                    break;
                }
                case "5": {
                    try {
                        Desktop.getDesktop().browse(new URI("https://www.mcbbs.net/thread-941408-1-1.html"));
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }

                    break;
                }
                case "6": {
                    new ImageGetter();
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private static void help() {
        System.out.println("输入1爬用户 输入2爬签到 输入3爬日常任务1|2页面   输入6爬头像  任意时刻有效(大概)");
        System.out.println("输入map来检测uid和名字是否在网易存在对应用户");

    }
}
