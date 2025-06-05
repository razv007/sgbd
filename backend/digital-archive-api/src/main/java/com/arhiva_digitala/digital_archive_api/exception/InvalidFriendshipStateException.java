package com.arhiva_digitala.digital_archive_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidFriendshipStateException extends RuntimeException {
    public InvalidFriendshipStateException(String message) {
        super(message);
    }
}
