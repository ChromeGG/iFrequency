package com.ifrequency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppApplication {

    public static void main(String[] args) {
//        System.setProperty("phantomjs.binary.path", "lib/PhantomJS-2.1.1-win64x/phantomjs.exe");
        System.setProperty("phantomjs.binary.path", "lib/phantomjs-2.1.1-linux-x86_64/bin/phantomjs");
        SpringApplication.run(AppApplication.class, args);
    }

}
