package com.fibno.srinis.milkmanager.model;

import java.util.Map;

public class Years
{
    private Map<String, Months> months;
    private int year;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Map<String, Months> getMonths() {
        return months;
    }

    public void setMonths(Map<String, Months> months) {
        this.months = months;
    }

    @Override
    public String toString() {
        return "Years{" +
                "months=" + months +
                '}';
    }
}
