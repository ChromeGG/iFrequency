package com.ifrequency.service.aggregate;

import com.ifrequency.model.Subject;

import java.util.List;

public interface SubjectsAggregateService {

    void goToUserFrequency();

    void goToStartMonth(int differenceOfMonths);

    List<Subject> aggregateData(int monthsDifference);

    int countDifferenceBetweenMonth();
}
