package com.example.stephen.fatcat;

public class FundingSourcesItem {

    private String fundId;
    private String name;

    public FundingSourcesItem(String fundId, String name) {
        this.fundId = fundId;
        this.name = name;
    }

    public String getFundId() {
        return fundId;
    }

    public String getName() {
        return name;
    }

}
