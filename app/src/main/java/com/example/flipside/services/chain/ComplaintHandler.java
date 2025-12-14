package com.example.flipside.services.chain;

import com.example.flipside.models.Complaint;

public abstract class ComplaintHandler {
    protected ComplaintHandler nextHandler;

    public void setNextHandler(ComplaintHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public abstract void handleComplaint(Complaint complaint);
}