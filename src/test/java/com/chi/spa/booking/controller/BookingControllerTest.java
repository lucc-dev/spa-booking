package com.chi.spa.booking.controller;

import com.chi.spa.booking.SpaBookingApplication;
import com.chi.spa.booking.dto.BookingRequest;
import com.chi.spa.booking.model.Booking;
import com.chi.spa.booking.model.ServiceItem;
import com.chi.spa.booking.repository.BookingRepository;
import com.chi.spa.booking.repository.ServiceItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest(classes = SpaBookingApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Transactional
class BookingControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ServiceItemRepository serviceItemRepository;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context).build();
        bookingRepository.deleteAll();
        serviceItemRepository.deleteAll();
    }

    @Test
    void createBooking_returnsBadRequestWhenContactMissing() throws Exception {
        ServiceItem serviceItem = createServiceItem("Test Massage A");
        BookingRequest request = bookingRequest(serviceItem.getId(), null, null, LocalTime.of(10, 0));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("欄位錯誤：電話或電子郵件必須填寫一項。"));
    }

    @Test
    void createBooking_returnsOkWhenSaved() throws Exception {
        ServiceItem serviceItem = createServiceItem("Test Massage B");
        BookingRequest request = bookingRequest(serviceItem.getId(), "amy@example.com", "0912345678", LocalTime.of(10, 0));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Amy"))
                .andExpect(jsonPath("$.serviceItem.id").value(serviceItem.getId().intValue()))
                .andExpect(jsonPath("$.customerEmail").value("amy@example.com"));
    }

    @Test
    void createBooking_returnsBadRequestWhenBusinessExceptionRaised() throws Exception {
        ServiceItem serviceItem = createServiceItem("Test Massage C");
        BookingRequest request = bookingRequest(serviceItem.getId(), "amy@example.com", "0912345678", LocalTime.of(11, 0));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("預約失敗：請選擇正確的預約時段（10:00, 12:00, 14:00, 16:00, 18:00）。"));
    }

    @Test
    void getAllBookings_returnsOk() throws Exception {
        ServiceItem serviceItem = createServiceItem("Test Massage D");
        bookingRepository.save(booking("Amy", "amy@example.com", "0912345678",
                LocalDate.of(2026, 7, 18), LocalTime.of(10, 0), serviceItem));

        mockMvc.perform(get("/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerName").value("Amy"));
    }

    @Test
    void getBookingById_returnsNotFoundWhenMissing() throws Exception {
        mockMvc.perform(get("/bookings/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBooking_returnsNoContent() throws Exception {
        ServiceItem serviceItem = createServiceItem("Test Massage E");
        Booking saved = bookingRepository.save(booking("Amy", "amy@example.com", "0912345678",
                LocalDate.of(2026, 7, 18), LocalTime.of(10, 0), serviceItem));

        mockMvc.perform(delete("/bookings/{id}", saved.getId()))
                .andExpect(status().isNoContent());
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

    private BookingRequest bookingRequest(Long serviceItemId, String email, String phone, LocalTime time) {
        BookingRequest request = new BookingRequest();
        request.setCustomerName("Amy");
        request.setCustomerEmail(email);
        request.setCustomerPhone(phone);
        request.setBookingDate(LocalDate.of(2026, 7, 18));
        request.setBookingTime(time);
        request.setServiceItemId(serviceItemId);
        return request;
    }
}
