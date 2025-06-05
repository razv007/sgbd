package com.arhiva_digitala.digital_archive_api.dto;

import javax.validation.constraints.NotBlank;

public class FriendRequestPayloadDto {
    @NotBlank(message = "Numele utilizatorului destinatar nu poate fi gol.")
    private String receiverUsername;

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }
}
