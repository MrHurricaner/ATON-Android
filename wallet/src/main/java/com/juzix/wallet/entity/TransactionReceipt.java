package com.juzix.wallet.entity;

public class TransactionReceipt {

    private int status;

    private String hash;

    public TransactionReceipt(int status, String hash) {
        this.status = status;
        this.hash = hash;
    }

    public TransactionReceipt() {
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
