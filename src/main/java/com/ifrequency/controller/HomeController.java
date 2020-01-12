package com.ifrequency.controller;

import com.ifrequency.error.ERROR_TYPE;
import com.ifrequency.error.ErrorMessage;
import com.ifrequency.factory.BrowserFactory;
import com.ifrequency.model.Subject;
import com.ifrequency.model.User;
import com.ifrequency.service.aggregate.GoToFrequencyException;
import com.ifrequency.service.browser.BrowserDataSupplier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;


@Controller
@RequestMapping("/ifrekwencja")
public class HomeController {

    private BrowserDataSupplier browserDataSupplier = BrowserFactory.createNewBrowser();

    @GetMapping
    public String getHomePage(Model model) {
        browserDataSupplier.setupNewConnection();
        String captchaInBase64 = browserDataSupplier.getCaptchaBase64();

        model.addAttribute("captchaImage", captchaInBase64);
        model.addAttribute("user", new User());
        return "index";
    }

    @PostMapping()
    public String getUserAndCreateStatistic(@ModelAttribute User user, Model model) {
        browserDataSupplier.setUser(user);
        boolean loginSuccess = browserDataSupplier.logIn();

        if (!loginSuccess) {
            throwViewWithException(model, ERROR_TYPE.LOGIN);
            return "error-site";
        }

        List<Subject> subjects;

        try {
            subjects = browserDataSupplier.getSubjectsList();
        } catch (GoToFrequencyException exception) {
            throwViewWithException(model, ERROR_TYPE.GO_TO_FREQUENCY);
            return "error-site";
        }

        model.addAttribute("user", user);
        model.addAttribute("subjectsList", subjects);
        model.addAttribute("subject", new Subject());
        endConnection();
        return "statistic";
    }

    private void throwViewWithException(Model model, ERROR_TYPE error_type) {
        model.addAttribute("errorMessage", new ErrorMessage(error_type));
        endConnection();
    }

    private void endConnection() {
        browserDataSupplier.kill();
        browserDataSupplier = BrowserFactory.createNewBrowser();
    }


}
