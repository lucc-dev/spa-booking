package com.chi.spa.booking.repository;

import com.chi.spa.booking.SpaBookingApplication;
import com.chi.spa.booking.model.Booking;
import com.chi.spa.booking.model.ServiceItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SpaBookingApplication.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Transactional
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ServiceItemRepository serviceItemRepository;

    @Test
    void saveAndDerivedQueries_work() {
        ServiceItem serviceItem = serviceItemRepository.save(ServiceItem.builder()
                .name("Test Massage S")
                .description("Full body massage")
                .durationMinutes(60)
                .price(1200)
                .build());

        LocalDate date = LocalDate.of(2026, 7, 18);
        LocalTime time = LocalTime.of(10, 0);

        bookingRepository.save(booking("Amy", "amy@example.com", "0912345678", date, time, serviceItem));
        bookingRepository.save(booking("Bella", "bella@example.com", "0922333444", date, time, serviceItem));

        assertThat(bookingRepository.existsByCustomerEmail("amy@example.com")).isTrue();
        assertThat(bookingRepository.existsByCustomerPhone("0912345678")).isTrue();
        assertThat(bookingRepository.countByBookingDateAndBookingTime(date, time)).isEqualTo(2L);
    }

    private Booking booking(String name, String email, String phone, LocalDate date, LocalTime time, ServiceItem serviceItem) {
        return Booking.builder()
                .customerName(name)
                .customerEmail(email)
                .customerPhone(phone)
                .bookingDate(date)
                .bookingTime(time)
                .serviceItem(serviceItem)
                .build();
    }
}
