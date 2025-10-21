package com.baidu.common;

public class ApiResponse<T> {
    private boolean success;
    private int code; // 0 表示成功
    private String message;
    private T data;

    public ApiResponse() {}

    public ApiResponse(boolean success, int code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(true, 0, "OK", null);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, 0, "OK", data);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, -1, message, null);
    }

    public static <T> ApiResponse<T> of(boolean success, int code, String message, T data) {
        return new ApiResponse<>(success, code, message, data);
    }
}

