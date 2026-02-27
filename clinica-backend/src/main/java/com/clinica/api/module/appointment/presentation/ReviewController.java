package com.clinica.api.module.appointment.presentation;

import com.clinica.api.module.appointment.application.dto.CreateReviewRequest;
import com.clinica.api.module.appointment.application.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Endpoint Protegido: Solo pacientes pueden dejar Reviews.
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<Void> submitReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request) {
        reviewService.submitReview(userDetails.getUsername(), request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
