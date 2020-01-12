package com.ifrequency.service.browser;

import com.ifrequency.model.Subject;
import com.ifrequency.model.User;
import com.ifrequency.service.aggregate.JSoupAggregate;
import com.ifrequency.service.aggregate.SubjectsAggregateService;
import lombok.Data;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
public class PhantomBrowser implements BrowserDataSupplier {

    private PhantomJSDriver driver = new PhantomJSDriver();
    User user;

    @Override
    public void setupNewConnection() {
        ((JavascriptExecutor) driver).executeScript("window.open('about:blank','_blank');");
        driver.get("https://iuczniowie.progman.pl/idziennik");
        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    @Override
    public String getCaptchaBase64() {
        waitForPageLoaded();

        byte[] arrScreen = driver.getScreenshotAs(OutputType.BYTES);
        BufferedImage imageScreen = null;

        try {
            imageScreen = ImageIO.read(new ByteArrayInputStream(arrScreen));
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedImage imgCap = getCaptchaImage(imageScreen);


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream b64 = new Base64OutputStream(baos);
        try {
            ImageIO.write(imgCap, "png", b64);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toString(StandardCharsets.UTF_8);
    }

    @Override
    public boolean logIn() {
        WebElement schoolName = driver.findElementById("NazwaSzkoly");
        WebElement userName = driver.findElementById("UserName");
        WebElement password = driver.findElementById("Password");
        WebElement captcha = driver.findElementById("captcha");
        WebElement btnLogin = driver.findElementByClassName("btnLogin");


        schoolName.sendKeys(user.getSchoolName());
        userName.sendKeys(user.getName());
        password.sendKeys(user.getPassword());
        captcha.sendKeys(user.getCaptcha());

        btnLogin.click();

        waitForPageLoaded();

        try {
            driver.findElementById("spanErrorMessage");
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public List<Subject> getSubjectsList() {
        SubjectsAggregateService aggregate = new JSoupAggregate(driver, user);

        aggregate.goToUserFrequency();
        int monthsDifference = aggregate.countDifferenceBetweenMonth();
        aggregate.goToStartMonth(monthsDifference);

        List<Subject> subjects = aggregate.aggregateData(monthsDifference);

        return subjects;
    }

    private BufferedImage getCaptchaImage(BufferedImage imageScreen) {
        WebElement cap = driver.findElementById("imgCaptcha");

        Dimension capDimension = cap.getSize();
        Point capLocation = cap.getLocation();

        return imageScreen.getSubimage(capLocation.x, capLocation.y, capDimension.width, capDimension.height);
    }

    private void makeSS() {
        TakesScreenshot ts = driver;
        File source = ts.getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(source, new File("screen.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Screenshot created");
    }

    @Override
    public void kill() {
        driver.close();
    }

    private void waitForPageLoaded() {
        ExpectedCondition<Boolean> expectation = new
                ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        return ((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete");
                    }
                };
        try {
            Thread.sleep(1000);
            WebDriverWait wait = new WebDriverWait(driver, 30);
            wait.until(expectation);
        } catch (Throwable error) {
            Assert.fail("Timeout waiting for Page Load Request to complete.");
        }
    }
}
