package com.ifrequency.model;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class User {

    String schoolName;
    String name;
    String password;
    String captcha;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate sinceWhen;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate untilWhen;
}
