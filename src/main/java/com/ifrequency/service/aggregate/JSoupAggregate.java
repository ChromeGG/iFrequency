package com.ifrequency.service.aggregate;

import com.ifrequency.model.Day;
import com.ifrequency.model.Lesson;
import com.ifrequency.model.Subject;
import com.ifrequency.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class JSoupAggregate implements SubjectsAggregateService {

    private PhantomJSDriver driver;
    private User user;

    @Override
    public void goToUserFrequency() {
        WebElement attendanceButton = driver.findElementByLinkText("Obecności");
        attendanceButton.click();

        waitForPageLoaded();

        List<WebElement> viewsButtons = driver.findElementsByClassName("label");
        WebElement monthlyViewButton = viewsButtons.get(1);
        monthlyViewButton.click();

        waitForPageLoaded();
    }

    @Override
    public void goToStartMonth(int differenceOfMonths) {

        try {
            while (differenceOfMonths > 0) {
                driver.findElementByClassName("prevButton").click();
                differenceOfMonths--;
                waitForPageLoaded();
            }
        } catch (Exception e) {
            throw new GoToFrequencyException();
        }

    }

    @Override
    public List<Subject> aggregateData(int monthsDifference) {
        List<Element> htmlDays;

        int startDay = user.getSinceWhen().getDayOfMonth();
        int endDay = user.getUntilWhen().getDayOfMonth();

        if (monthsDifference == 0) {
            htmlDays = parseMonth(startDay, endDay);
        } else if (monthsDifference == 1) {
            htmlDays = parseMonth(startDay, 31);
            clickNextMonthButton();
            htmlDays.addAll(parseMonth(1, endDay));
        } else {
            htmlDays = parseMonth(startDay, 31);
            monthsDifference--;

            clickNextMonthButton();

            while (monthsDifference >= 0) {
                htmlDays.addAll(parseMonth(1, 31));
                clickNextMonthButton();
                monthsDifference--;
            }
            clickNextMonthButton();

            htmlDays.addAll(parseMonth(1, endDay));
        }

        List<Day> selectedDays = parseElementsToDays(htmlDays);
        Set<Subject> subjectsNames = initSubjects(selectedDays);
        List<Subject> completeSubjectList = assignFrequencyToSubjects(subjectsNames, selectedDays);

        addFrequency(completeSubjectList);

        return completeSubjectList;
    }

    private void clickNextMonthButton() {
        driver.findElementByClassName("nextButton").click();
        waitForPageLoaded();
    }

    private void addFrequency(List<Subject> completeSubjectList) {
        for (Subject subject : completeSubjectList) {
            double positiveHours = subject.getPresent() + subject.getExemptPresent();
            double negativeHours = subject.getAbsent() + subject.getAbsentExcused() + subject.getExemption();
            double allMatterHours = positiveHours + negativeHours;
            double frequency;
            double formattedFrequency;

            if (negativeHours == 0) {
                frequency = 100;
            } else if (positiveHours == 0) {
                frequency = 0;
            } else {
                frequency = positiveHours * 100 / allMatterHours;
            }
            formattedFrequency = Double.parseDouble(String.format("%.2f", frequency));

            subject.setFrequency(formattedFrequency);
        }
    }


    private List<Day> parseElementsToDays(List<Element> elementDays) {
        List<Day> days = new ArrayList<>();

        for (Element elementDay : elementDays) {
            List<Lesson> lessons = new ArrayList<>();
            Day day = new Day();


            Elements htmlLessons = elementDay.children();

            for (Element htmlLesson : htmlLessons) {
                Optional<Lesson> lesson = parseHtmlLesson(htmlLesson);
                lesson.ifPresent(lessons::add);
            }
            day.setLessons(lessons);
            days.add(day);
        }
        return days;
    }

    private Set<Subject> initSubjects(List<Day> days) {
        Set<Subject> subjectSet = new HashSet<>();

        for (Day day : days) {
            List<Lesson> lessons = day.getLessons();
            for (Lesson lesion : lessons) {
                String lessonName = lesion.getName();
                Subject subject = new Subject(lessonName);
                subjectSet.add(subject);
            }
        }
        return subjectSet;
    }

    private Optional<Lesson> parseHtmlLesson(Element htmlLesson) {
        String classAttributes = htmlLesson.attr("class");

        if (classAttributes.contains("dzienMiesiaca") || classAttributes.contains("okienko") || htmlLesson.text().contains("Ferie")) {
            return Optional.empty();
        } else {
            String lessonName = htmlLesson.text().substring(4);

            int attendanceCategory = Integer.parseInt(classAttributes.replaceAll("[^0-9]", ""));
            Lesson lesson = new Lesson(lessonName, attendanceCategory);
            return Optional.of(lesson);
        }
    }

    private List<Element> parseMonth(int startDay, int endDay) {
        String source = driver.getPageSource();
        Document document = Jsoup.parse(source);

        Elements allDays = document.getElementsByClass("dzienMiesiaca");
        List<Element> days = new ArrayList<>();

        for (Element htmlDay : allDays) {
            int dayNumber = getDayNumber(htmlDay);
            if (dayNumber >= startDay && dayNumber <= endDay) {
                days.add(htmlDay);
            }
        }

        return days;
    }

    private int getDayNumber(Element day) {
        String id;
        id = day.id();

        int dayNumber = 0;
        if (id.length() > 1) {
            dayNumber = Integer.parseInt(id.replaceAll("[^0-9]", ""));

        }
        return dayNumber;
    }

    @Override
    public int countDifferenceBetweenMonth() {
        LocalDate today = LocalDate.now();
        int differenceBetweenMonths = (int) ChronoUnit.MONTHS.between(
                user.getSinceWhen().withDayOfMonth(1),
                today.withDayOfMonth(1));

        if (differenceBetweenMonths > 50) {
            throw new GoToFrequencyException();
        }
        return differenceBetweenMonths;
    }

    private List<Subject> assignFrequencyToSubjects(Set<Subject> subjects, List<Day> selectedDays) {
        List<Subject> completeSubjectsList = new ArrayList<>(subjects);
        Map<String, Subject> map = subjects.stream().collect(Collectors.toMap(Subject::getName, e -> e));

        for (Day day : selectedDays) {
            List<Lesson> lessons = day.getLessons();

            for (Lesson lesson : lessons) {
                String nameLesson = lesson.getName();
                int category = lesson.getCategory();

                Subject subject = map.get(nameLesson);

                switch (category) {
                    case 0:
                        subject.setPresent(subject.getPresent() + 1);
                        break;
                    case 1:
                        subject.setAbsentExcused(subject.getAbsentExcused() + 1);
                        break;
                    case 2:
                        subject.setBelated(subject.getBelated() + 1);
                        break;
                    case 3:
                        subject.setAbsent(subject.getAbsent() + 1);
                        break;
                    case 4:
                        subject.setExemption(subject.getExemption() + 1);
                        break;
                    case 5:
                        subject.setNotHappen(subject.getNotHappen() + 1);
                        break;
                    case 9:
                        subject.setExemptPresent(subject.getExemptPresent() + 1);
                        break;
                    default:
                        System.err.println("Unknown category: " + category);
                }
            }

        }

        return completeSubjectsList;
    }

    private void waitForPageLoaded() {
        ExpectedCondition<Boolean> expectation = driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete");
        try {
            Thread.sleep(1000);
            WebDriverWait wait = new WebDriverWait(driver, 30);
            wait.until(expectation);
        } catch (Throwable error) {
            Assert.fail("Timeout waiting for Page Load Request to complete.");
        }
    }
}
