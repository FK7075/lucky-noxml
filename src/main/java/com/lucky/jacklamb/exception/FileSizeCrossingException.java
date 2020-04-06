package com.lucky.jacklamb.exception;

/**
 * @Author jackfu
 * @Date 2020/4/6 10:19 下午
 * @Version 1.0
 **/


public class FileSizeCrossingException extends RuntimeException {

    public FileSizeCrossingException(String message) {
        super(message);
    }
}
