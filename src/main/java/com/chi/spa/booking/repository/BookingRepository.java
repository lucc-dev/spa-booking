package com.chi.spa.booking.repository;

import com.chi.spa.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByCustomerEmail(String customerEmail);
    boolean existsByCustomerPhone(String customerPhone);

    long countByBookingDateAndBookingTime(LocalDate bookingDate, LocalTime bookingTime);

    // 查詢某位顧客的所有預約紀錄
    List<Booking> findByCustomerId(Long customerId);
}
