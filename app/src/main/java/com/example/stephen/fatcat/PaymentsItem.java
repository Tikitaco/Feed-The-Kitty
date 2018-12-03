package com.example.stephen.fatcat;

public class PaymentsItem {

    private String paymentId;
    private String amount;
    private String status;

    public PaymentsItem(String paymentId, String amount) {
        this.paymentId = paymentId;
        this.amount = amount;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
