package com.fibno.srinis.milkmanager.model;

public class Months
{
    private Days[] days;

    private String month;

    public Days[] getDays ()
    {
        return days;
    }

    public void setDays (Days[] days)
    {
        this.days = days;
    }

    public String getMonth ()
    {
        return month;
    }

    public void setMonth (String month)
    {
        this.month = month;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [days = "+days+", month = "+month+"]";
    }
}
