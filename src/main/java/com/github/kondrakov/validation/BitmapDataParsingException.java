package com.github.kondrakov.validation;

import java.io.IOException;

public class BitmapDataParsingException extends IOException {
    public BitmapDataParsingException() {
    }

    public BitmapDataParsingException(String message) {
        super(message);
    }
}