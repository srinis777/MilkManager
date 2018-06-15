package com.fibno.srinis.milkmanager.model;

import java.util.List;
import java.util.Map;

public class MilkAccount {
    private Map<String, Years> years;
    private String account;

    public List<String> getUnsettledMonths() {
        return unsettledMonths;
    }

    public void setUnsettledMonths(List<String> unsettledMonths) {
        this.unsettledMonths = unsettledMonths;
    }

    private List<String> unsettledMonths;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Map<String, Years> getYears() {
        return years;
    }

    public void setYears(Map<String, Years> years) {
        this.years = years;
    }

    @Override
    public String toString() {
        return "MilkAccount{" +
                "years=" + years +
                ", account='" + account + '\'' +
                ", unsettledMonths=" + unsettledMonths +
                '}';
    }
}
