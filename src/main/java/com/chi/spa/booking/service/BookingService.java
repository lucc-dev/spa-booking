package com.chi.spa.booking.service;

import com.chi.spa.booking.dto.BookingRequest;
import com.chi.spa.booking.exception.BusinessException;
import com.chi.spa.booking.model.Booking;
import com.chi.spa.booking.model.Customer;
import com.chi.spa.booking.model.ServiceItem;
import com.chi.spa.booking.repository.BookingRepository;
import com.chi.spa.booking.repository.ServiceItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final CustomerService customerService;

    @CacheEvict(value = "bookings", key = "'all'")
    public Booking createBooking(BookingRequest request) {

        ServiceItem serviceItem = serviceItemRepository.findById(request.getServiceItemId())
                .orElse(null);

        if (serviceItem == null) {
            throw new BusinessException("預約失敗：找不到指定的服務項目（療程 ID 錯誤）。");
        }

        validateNotPastDate(request.getBookingDate());

        LocalTime requestedTime = request.getBookingTime();

        if (!isValidSlot(requestedTime)) {
            throw new BusinessException("預約失敗：請選擇正確的預約時段（10:00, 12:00, 14:00, 16:00, 18:00）。");
        }

        long existingCount = bookingRepository.countByBookingDateAndBookingTime(
                request.getBookingDate(),
                request.getBookingTime()
        );

        if (existingCount >= 2) {
            throw new BusinessException("預約失敗：該時段預約已滿（上限 2 人），請選擇其他時段。");
        }

        Customer customer = customerService.findOrCreateCustomer(
                request.getCustomerName(),
                request.getCustomerPhone(),
                request.getCustomerEmail()
        );

        Booking booking = Booking.fromDto(request, serviceItem, customer);
        return bookingRepository.save(booking);
    }

    @CacheEvict(value = {"bookings", "booking"}, allEntries = true)
    public Booking updateBooking(Long id, Long newServiceItemId, LocalDate newDate, LocalTime newTime) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("找不到預約編號 #" + id + "，無法修改。"));

        if (newServiceItemId != null) {
            ServiceItem serviceItem = serviceItemRepository.findById(newServiceItemId)
                    .orElseThrow(() -> new BusinessException("找不到指定的服務項目（療程 ID 錯誤）。"));
            booking.setServiceItem(serviceItem);
        }

        if (newDate != null || newTime != null) {
            LocalDate targetDate = newDate != null ? newDate : booking.getBookingDate();
            LocalTime targetTime = newTime != null ? newTime : booking.getBookingTime();

            validateNotPastDate(targetDate);

            if (!isValidSlot(targetTime)) {
                throw new BusinessException("修改失敗：請選擇正確的預約時段（10:00, 12:00, 14:00, 16:00, 18:00）。");
            }

            long existingCount = bookingRepository.countByBookingDateAndBookingTime(targetDate, targetTime);
            boolean sameSlot = targetDate.equals(booking.getBookingDate()) && targetTime.equals(booking.getBookingTime());
            long effectiveCount = sameSlot ? existingCount - 1 : existingCount;

            if (effectiveCount >= 2) {
                throw new BusinessException("修改失敗：目標時段預約已滿（上限 2 人），請選擇其他時段。");
            }

            booking.setBookingDate(targetDate);
            booking.setBookingTime(targetTime);
        }

        return bookingRepository.save(booking);
    }

    public int getRemainingSlots(LocalDate date, LocalTime time) {
        long existingCount = bookingRepository.countByBookingDateAndBookingTime(date, time);
        return (int) Math.max(0, 2 - existingCount);
    }

    // 查詢某位顧客的所有歷史預約
    public List<Booking> getBookingsByCustomerId(Long customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    private void validateNotPastDate(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new BusinessException("預約失敗：無法預約已經過去的日期，請選擇今天或之後的日期。");
        }
    }

    private boolean isValidSlot(LocalTime time) {
        return time.equals(LocalTime.of(10, 0)) ||
                time.equals(LocalTime.of(12, 0)) ||
                time.equals(LocalTime.of(14, 0)) ||
                time.equals(LocalTime.of(16, 0)) ||
                time.equals(LocalTime.of(18, 0));
    }

    @Cacheable(value = "bookings", key = "'all'")
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Cacheable(value = "booking", key = "#id")
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id).orElse(null);
    }

    @CacheEvict(value = {"bookings", "booking"}, allEntries = true)
    public boolean deleteBooking(Long id) {
        if (bookingRepository.existsById(id)) {
            bookingRepository.deleteById(id);
            return true;
        }
        return false;
    }
}