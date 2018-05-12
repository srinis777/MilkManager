package com.fibno.srinis.milkmanager.model;

public class MilkManagerPOJO
{
    private Accounts accounts;

    public Accounts getAccounts ()
    {
        return accounts;
    }

    public void setAccounts (Accounts accounts)
    {
        this.accounts = accounts;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [accounts = "+accounts+"]";
    }
}
