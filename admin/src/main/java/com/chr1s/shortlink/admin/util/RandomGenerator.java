package com.chr1s.shortlink.admin.util;

import java.util.Random;

public class RandomGenerator {
    // 定义所有可能出现的字符
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    // 定义生成字符串的长度
    private static final int LENGTH = 6;

    // 随机数生成器
    private static final Random random = new Random();

    /**
     * 生成一个包含数字和英文字母的随机字符串。
     * @return 返回生成的随机字符串。
     */
    public static String generateRandomString() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

}
