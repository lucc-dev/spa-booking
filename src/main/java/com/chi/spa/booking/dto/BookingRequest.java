package com.chi.spa.booking.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingRequest {
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private Long serviceItemId;
}