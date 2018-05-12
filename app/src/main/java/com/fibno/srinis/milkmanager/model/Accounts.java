package com.fibno.srinis.milkmanager.model;

public class Accounts
{
    private Years[] years;

    public Years[] getYears ()
    {
        return years;
    }

    public void setYears (Years[] years)
    {
        this.years = years;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [years = "+years+"]";
    }
}
