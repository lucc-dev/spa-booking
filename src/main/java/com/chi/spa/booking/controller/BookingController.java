package com.chi.spa.booking.controller;

import com.chi.spa.booking.dto.BookingRequest;
import com.chi.spa.booking.exception.BusinessException;
import com.chi.spa.booking.model.Booking;
import com.chi.spa.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body("欄位錯誤：預約資料不能為空。");
        }

        if (isBlank(request.getCustomerName())) {
            return ResponseEntity.badRequest().body("欄位錯誤：姓名必須填寫。");
        }

        if (request.getBookingDate() == null) {
            return ResponseEntity.badRequest().body("欄位錯誤：預約日期必須填寫。");
        }

        if (request.getBookingTime() == null) {
            return ResponseEntity.badRequest().body("欄位錯誤：預約時段必須填寫。");
        }

        if (request.getServiceItemId() == null) {
            return ResponseEntity.badRequest().body("欄位錯誤：療程項目必須填寫。");
        }

        if (isBlank(request.getCustomerPhone()) && isBlank(request.getCustomerEmail())) {
            return ResponseEntity.badRequest().body("欄位錯誤：電話或電子郵件必須填寫一項。");
        }

        try {
            Booking savedBooking = bookingService.createBooking(request);
            if (savedBooking == null) {
                return ResponseEntity.badRequest().body("預約失敗，請檢查輸入資料。");
            }
            return ResponseEntity.ok(savedBooking);

        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            e.printStackTrace(); // 印出錯誤追蹤細節
            return ResponseEntity.internalServerError().body("系統發生未知錯誤，請稍後再試。");
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllBookings() {
        try {
            List<Booking> bookings = bookingService.getAllBookings();
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            e.printStackTrace(); // 關鍵：在 Console 印出異常 Log
            return ResponseEntity.internalServerError().body("查詢失敗：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        try {
            Booking booking = bookingService.getBookingById(id);
            if (booking == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("查詢預約失敗：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        try {
            boolean deleted = bookingService.deleteBooking(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("刪除預約失敗：" + e.getMessage());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
