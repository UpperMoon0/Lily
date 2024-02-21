package com.nhat.lily.models;

public interface IResponseHandler {
    String getResponse(String prompt) throws Exception;
}