package com.example.message.model;

public class Request {

    private String requester, requestee;

    public Request() {
    }

    public Request(String requester, String requestee) {
        this.requester = requester;
        this.requestee = requestee;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public String getRequestee() {
        return requestee;
    }

    public void setRequestee(String requestee) {
        this.requestee = requestee;
    }
}
