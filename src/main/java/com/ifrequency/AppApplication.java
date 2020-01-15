package com.ifrequency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppApplication {

    public static void main(String[] args) {
        setPhantomJS();
        SpringApplication.run(AppApplication.class, args);
    }

    private static void setPhantomJS() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("windows")) {
            System.setProperty("phantomjs.binary.path", "lib/PhantomJS-2.1.1-win64x/phantomjs.exe");
        } else {
            System.setProperty("phantomjs.binary.path", "lib/phantomjs-2.1.1-linux-x86_64/bin/phantomjs");
        }
    }

}
