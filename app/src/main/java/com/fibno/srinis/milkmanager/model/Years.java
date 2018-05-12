package com.fibno.srinis.milkmanager.model;

public class Years
{
    private Months[] months;

    private String year;

    public Months[] getMonths ()
    {
        return months;
    }

    public void setMonths (Months[] months)
    {
        this.months = months;
    }

    public String getYear ()
    {
        return year;
    }

    public void setYear (String year)
    {
        this.year = year;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [months = "+months+", year = "+year+"]";
    }
}
