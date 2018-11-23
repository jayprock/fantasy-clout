package com.bitbus.fantasyclout.projection;

import java.time.LocalDate;
import java.time.Month;

public final class SeasonYearEvaluator {

    private SeasonYearEvaluator() {}

    public static int getFootballSeasonYear(LocalDate date) {
        return (date.getMonth() == Month.JANUARY || date.getMonth() == Month.FEBRUARY) //
                ? date.getYear() - 1
                : date.getYear();
    }
}
