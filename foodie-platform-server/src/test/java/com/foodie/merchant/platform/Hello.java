package com.foodie.merchant.platform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * @Author: wanderer
 * @Date: 2026/1/15 15:57
 */

public class Hello {

    public static void main(String[] args) {

        Collection<String> coll = new ArrayList<>();
        coll.add("aaa");
        coll.add("bbb");
        coll.add("ccc");


        coll.forEach(s -> System.out.println(s));

    }
}
