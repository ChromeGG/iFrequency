package com.ifrequency.error;

import lombok.Data;

import java.util.List;

@Data
public class ErrorMessage {

    String title;
    List<String> possibleSolutions;


    public ErrorMessage(ERROR_TYPE error) {
        this.title = error.getTitle();
        this.possibleSolutions = error.getSolutions();
    }
}
