package com.itheima.reggie;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@SpringBootTest
class ReggieTakeOutApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("hello");
    }

    @Test
    public void test1() {
//        Random random = new Random();
//        int num = -1;
//        while (num!=100){
//            num = random.nextInt(100);    // num>=0 && num<=100
//            System.out.println(num);
//        }
        String str = String.valueOf("null");
        System.out.println(str);
    }

    @Test
    public void test2() {
        //统计字符串中一个子字符串的出现次数
        String str = "12312String sda string ss dsadas2312String strinString00";
        int index = 0;
        int count = 0;
        while (str.indexOf("String", index) != -1) {
            count++;
            index = str.indexOf("String", index)+"String".length();
        }
        System.out.println(count);
    }

    @Test
    public void test3(){
        //统计字符串中一个字符的出现次数
        String str = "321ssdasasderwuoqixcnm,zcz,.xmklsdjaaAAfd";
        char[] chars = str.toCharArray();
        Character [] characters = new Character[chars.length];
        for (int i = 0; i < characters.length; i++) {
            characters[i] = chars[i];
        }
        System.out.println(Arrays.toString(characters));

        List<Character> arrayList = Arrays.asList(characters);
        System.out.println(arrayList);
    }
}
