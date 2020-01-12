package com.ifrequency.error;

import lombok.Getter;

import java.util.List;

@Getter
public enum ERROR_TYPE {
    LOGIN("Login Error", List.of("Check if all fields are filled correctly")),
    GO_TO_FREQUENCY("Go to frequency Error", List.of("Try input other date range"));

    private String title;
    private List<String> solutions;


    ERROR_TYPE(String title, List<String> solutions) {
        this.title = title;
        this.solutions = solutions;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getSolutions() {
        return solutions;
    }

}
