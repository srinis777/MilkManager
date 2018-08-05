package com.fibno.srinis.milkmanager.model;
import java.util.Map;
public class Supplier {
    private Map<String, MilkAccount> accounts;

    public Map<String, MilkAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(Map<String, MilkAccount> accounts) {
        this.accounts = accounts;
    }
}
