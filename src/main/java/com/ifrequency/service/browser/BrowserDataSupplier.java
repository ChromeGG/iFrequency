package com.ifrequency.service.browser;

import com.ifrequency.model.Subject;
import com.ifrequency.model.User;

import java.util.List;

public interface BrowserDataSupplier {

    void setupNewConnection();

    String getCaptchaBase64();


    boolean logIn();

    List<Subject> getSubjectsList();

    void setUser(User user);

    void kill();
}
