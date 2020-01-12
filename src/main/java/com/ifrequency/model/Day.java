package com.ifrequency.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Day {

    int DayOfMonth;
    List<Lesson> lessons;

}
