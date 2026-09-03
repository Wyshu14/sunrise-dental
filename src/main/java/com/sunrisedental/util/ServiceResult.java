package com.sunrisedental.util;

import java.util.Collections;
import java.util.List;

/**
 * Generic success/failure wrapper returned by the service layer, so the
 * REST handlers and the console client both have one consistent shape to
 * check (isSuccess()) instead of relying on exceptions for expected
 * validation failures.
 */
public class ServiceResult<T> {

    private final boolean success;
    private final T data;
    private final List<String> errors;

    private ServiceResult(boolean success, T data, List<String> errors) {
        this.success = success;
        this.data = data;
        this.errors = errors;
    }

    public static <T> ServiceResult<T> ok(T data) {
        return new ServiceResult<>(true, data, Collections.emptyList());
    }

    public static <T> ServiceResult<T> fail(List<String> errors) {
        return new ServiceResult<>(false, null, errors);
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public List<String> getErrors() { return errors; }
}
