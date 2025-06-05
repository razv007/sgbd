package com.arhiva_digitala.digital_archive_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class FriendshipRequestAlreadyExistsException extends RuntimeException {
    public FriendshipRequestAlreadyExistsException(String message) {
        super(message);
    }
}
