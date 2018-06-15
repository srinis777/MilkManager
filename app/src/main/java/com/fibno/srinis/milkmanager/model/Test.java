package com.fibno.srinis.milkmanager.model;

import java.util.Calendar;

public class Test {
    public static void main(String[] args) {
        Calendar unsettledCalendar = Calendar.getInstance();
        int unsettledMonthInInt = Integer.parseInt("4");
        unsettledCalendar.set(2018, unsettledMonthInInt, 1);
        int days = unsettledCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        System.out.println(unsettledCalendar.getTime() + ":" + days);
    }
}
