package com.kingsley.client.models;

import java.util.Objects;

/**
 *  Transaction Class
 */

public class Transaction {
    private final int senderID;
    private final int receiverID;
    private final int amt;

    public Transaction(int senderID, int receiverID, int amt) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.amt = amt;
    }

    public int getSenderID() {
        return senderID;
    }

    public int getReceiverID() {
        return receiverID;
    }

    public int getAmt() {
        return amt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return senderID == that.senderID &&
                receiverID == that.receiverID &&
                amt == that.amt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderID, receiverID, amt);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "senderID=" + senderID +
                ", receiverID=" + receiverID +
                ", amt=" + amt +
                '}';
    }
}
