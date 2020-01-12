package com.ifrequency.factory;

import com.ifrequency.service.browser.BrowserDataSupplier;
import com.ifrequency.service.browser.PhantomBrowser;

public class BrowserFactory {


    public static BrowserDataSupplier createNewBrowser() {
        return new PhantomBrowser();
    }
}
