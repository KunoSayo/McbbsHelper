package com.github.euonmyoji.mcbbshelper;

import sun.security.provider.MD5;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.zip.CRC32;

import static com.github.euonmyoji.mcbbshelper.Main.scanner;

/**
 * @author yinyangshi
 */
class ImageGetter {
    private static final byte[] DEFAULT_BYTES;

    static {
        byte[] tmp = null;
        try (InputStream in = Files.newInputStream(Paths.get("DEFAULT.jpg"));) {
            tmp = new byte[in.available()];
            System.out.println("read default total:" + in.read(tmp) + " left: " + in.available());

        } catch (IOException e) {
            e.printStackTrace();
        }
        DEFAULT_BYTES = tmp;
    }

    ImageGetter() {
        System.out.println("请输入开始UID");
        int start = scanner.nextInt();
        System.out.println("请输入结束UID");
        int end = scanner.nextInt();
        System.out.println("请输入每个线程爬一面等待时间(毫秒)");
        int delay = scanner.nextInt();
        IntStream intStream = IntStream.range(start, end + 1);
        try {
            Files.createDirectories(Paths.get("F:\\CACHE\\images"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(() -> intStream.forEach(uid -> {
            String msg = "";
            System.out.println("start " + uid);
            try {
                URL url = new URL(String.format("https://www.mcbbs.net/uc_server/avatar.php?uid=%d&size=small", uid));
                try (InputStream in = url.openStream()) {
                    byte[] temp = new byte[1024 * 1024];
                    int offset = 0;
                    int len;
                    while ((len = in.read(temp, offset, in.available() == 0 ? 1 : in.available())) != -1) {
                        offset += len;
                    }
                    System.out.println(offset);
                    if (offset == DEFAULT_BYTES.length && Arrays.equals(DEFAULT_BYTES, Arrays.copyOf(temp, offset))) {
                        msg = "default image ";
                    } else {
                        OutputStream out = Files.newOutputStream(Paths.get("F:/CACHE/images/uid" + uid + ".jpg"));
                        out.write(temp, 0, offset);
                        out.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(msg + "end " + uid);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        })).start();

    }
}
