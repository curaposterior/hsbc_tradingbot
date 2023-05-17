package com.example.model.rest;

public sealed interface RestResponse {

    record BadRequest(String message) implements RestResponse {
    }

    record Unauthorized(String message) implements RestResponse {
    }

    record Unknown(int statusCode, String message) implements RestResponse {
    }

    record Failed(java.lang.Exception exception) implements RestResponse {
    }
}
