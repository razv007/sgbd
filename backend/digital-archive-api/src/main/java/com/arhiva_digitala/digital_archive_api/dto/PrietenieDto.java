package com.arhiva_digitala.digital_archive_api.dto;

import com.arhiva_digitala.digital_archive_api.model.StarePrietenie;
import java.time.LocalDateTime;

public class PrietenieDto {
    private Long id;
    private UserProfileDto utilizator1; // Inițiatorul (sender)
    private UserProfileDto utilizator2; // Destinatarul (receiver)
    private StarePrietenie stare;
    private LocalDateTime dataSolicitare;
    private LocalDateTime dataRaspuns;

    public PrietenieDto() {}

    public PrietenieDto(Long id, UserProfileDto utilizator1, UserProfileDto utilizator2, StarePrietenie stare, LocalDateTime dataSolicitare, LocalDateTime dataRaspuns) {
        this.id = id;
        this.utilizator1 = utilizator1;
        this.utilizator2 = utilizator2;
        this.stare = stare;
        this.dataSolicitare = dataSolicitare;
        this.dataRaspuns = dataRaspuns;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserProfileDto getUtilizator1() {
        return utilizator1;
    }

    public void setUtilizator1(UserProfileDto utilizator1) {
        this.utilizator1 = utilizator1;
    }

    public UserProfileDto getUtilizator2() {
        return utilizator2;
    }

    public void setUtilizator2(UserProfileDto utilizator2) {
        this.utilizator2 = utilizator2;
    }

    public StarePrietenie getStare() {
        return stare;
    }

    public void setStare(StarePrietenie stare) {
        this.stare = stare;
    }

    public LocalDateTime getDataSolicitare() {
        return dataSolicitare;
    }

    public void setDataSolicitare(LocalDateTime dataSolicitare) {
        this.dataSolicitare = dataSolicitare;
    }

    public LocalDateTime getDataRaspuns() {
        return dataRaspuns;
    }

    public void setDataRaspuns(LocalDateTime dataRaspuns) {
        this.dataRaspuns = dataRaspuns;
    }
}
