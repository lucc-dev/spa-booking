package com.chi.spa.booking.service;

import com.chi.spa.booking.SpaBookingApplication;
import com.chi.spa.booking.dto.BookingRequest;
import com.chi.spa.booking.exception.BusinessException;
import com.chi.spa.booking.model.Booking;
import com.chi.spa.booking.model.ServiceItem;
import com.chi.spa.booking.repository.BookingRepository;
import com.chi.spa.booking.repository.ServiceItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = SpaBookingApplication.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Transactional
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ServiceItemRepository serviceItemRepository;

    @Autowired
    private CacheManager cacheManager; // 1. 注入 CacheManager 來管理測試時的快取

    @BeforeEach
    void clearData() {
        // 清空資料庫
        bookingRepository.deleteAll();
        serviceItemRepository.deleteAll();

        // 2. 清空所有的 Spring Cache，防止前一個測試的快取殘留影響到後續測試
        for (String cacheName : cacheManager.getCacheNames()) {
            Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
        }
    }

    @Test
    void createBooking_savesBookingWhenAllRulesPass() {
        ServiceItem serviceItem = createServiceItem("Test Massage H");
        BookingRequest request = bookingRequest(serviceItem.getId(), LocalDate.of(2026, 7, 18), LocalTime.of(10, 0));

        Booking result = bookingService.createBooking(request);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getServiceItem().getId()).isEqualTo(serviceItem.getId());
        assertThat(bookingRepository.findById(result.getId())).isPresent();
    }

    @Test
    void createBooking_throwsWhenServiceItemMissing() {
        BookingRequest request = bookingRequest(999L, LocalDate.of(2026, 7, 18), LocalTime.of(10, 0));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("找不到指定的服務項目");
    }

    @Test
    void createBooking_throwsWhenTimeSlotInvalid() {
        ServiceItem serviceItem = createServiceItem("Test Massage I");
        BookingRequest request = bookingRequest(serviceItem.getId(), LocalDate.of(2026, 7, 18), LocalTime.of(11, 0));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("請選擇正確的預約時段");
    }

    @Test
    void createBooking_throwsWhenSlotIsFull() {
        ServiceItem serviceItem = createServiceItem("Test Massage J");
        LocalDate date = LocalDate.of(2026, 7, 18);
        LocalTime time = LocalTime.of(10, 0);
        bookingRepository.save(booking("Amy", "amy1@example.com", "0911111111", date, time, serviceItem));
        bookingRepository.save(booking("Bella", "bella2@example.com", "0922222222", date, time, serviceItem));
        BookingRequest request = bookingRequest(serviceItem.getId(), date, time);

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("該時段預約已滿");
    }

    @Test
    void getAllBookings_returnsAllBookings() {
        ServiceItem serviceItem = createServiceItem("Test Massage K");
        bookingRepository.save(booking("Amy", "amy@example.com", "0912345678",
                LocalDate.of(2026, 7, 18), LocalTime.of(10, 0), serviceItem));

        List<Booking> result = bookingService.getAllBookings();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerName()).isEqualTo("Amy");
    }

    @Test
    void getBookingById_returnsNullWhenMissing() {
        assertThat(bookingService.getBookingById(999L)).isNull();
    }

    @Test
    void deleteBooking_returnsTrueWhenExists() {
        ServiceItem serviceItem = createServiceItem("Test Massage L");
        Booking saved = bookingRepository.save(booking("Amy", "amy@example.com", "0912345678",
                LocalDate.of(2026, 7, 18), LocalTime.of(10, 0), serviceItem));

        boolean result = bookingService.deleteBooking(saved.getId());

        assertThat(result).isTrue();
        assertThat(bookingRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void deleteBooking_returnsFalseWhenMissing() {
        assertThat(bookingService.deleteBooking(999L)).isFalse();
    }

    private ServiceItem createServiceItem(String name) {
        return serviceItemRepository.save(ServiceItem.builder()
                .name(name)
                .description("Test service")
                .durationMinutes(60)
                .price(1200)
                .build());
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

    private BookingRequest bookingRequest(Long serviceItemId, LocalDate date, LocalTime time) {
        BookingRequest request = new BookingRequest();
        request.setCustomerName("Amy");
        request.setCustomerEmail("amy@example.com");
        request.setCustomerPhone("0912345678");
        request.setBookingDate(date);
        request.setBookingTime(time);
        request.setServiceItemId(serviceItemId);
        return request;
    }
}
