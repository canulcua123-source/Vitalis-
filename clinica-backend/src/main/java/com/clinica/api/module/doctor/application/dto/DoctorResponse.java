package com.clinica.api.module.doctor.application.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class DoctorResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String specialty;
    private String location;
    private String bio;
    private String photoUrl;
    private Integer experienceYears;
    private BigDecimal consultationPrice;
    private BigDecimal rating;
    private Integer totalReviews;
}
