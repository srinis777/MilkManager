package com.fibno.srinis.milkmanager.model;

import java.util.Map;

public class Months
{
    private Map<String, Integer> days;
    private int month;

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public Map<String, Integer> getDays() {
        return days;
    }

    public void setDays(Map<String, Integer> days) {
        this.days = days;
    }

    @Override
    public String toString() {
        return "Months{" +
                "days=" + days +
                '}';
    }
}
