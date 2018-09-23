package com.fibno.srinis.milkmanager.model;
import java.util.List;
import java.util.Map;
public class Supplier {
    private Map<String, MilkAccount> accountsMap;
    private List<String> accounts;

    public void setAccounts(List<String> accounts) {
        this.accounts = accounts;
    }

    public Map<String, MilkAccount> getAccounts() {
        return accountsMap;
    }

    @Override
    public String toString() {
        return "Supplier{" +
                "accountsMap=" + accountsMap +
                ", accounts=" + accounts +
                '}';
    }

    public void setAccounts(Map<String, MilkAccount> accountsMap) {
        this.accountsMap = accountsMap;
    }
}
