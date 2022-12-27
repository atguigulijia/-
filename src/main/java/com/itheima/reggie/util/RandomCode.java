package com.itheima.reggie.util;

/**
 * @author lijia
 * @create 2022-12-05 21:22
 */
public class RandomCode {
    /**
     * @description: 随机生成4位验证码
     * @param: no
     * @return: char[]
     */
    public static char[] generateCheckCode() {
        String chars = "0123456789abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        char[] rands = new char[4];
        for (int i = 0; i < 4; i++) {
            int rand = (int) (Math.random() * (10 + 26 * 2));
            rands[i] = chars.charAt(rand);
        }
        return rands;
    }

    public static void main(String[] args) {
        char[] chars = generateCheckCode();
        System.out.println(String.valueOf(chars));
    }
}
