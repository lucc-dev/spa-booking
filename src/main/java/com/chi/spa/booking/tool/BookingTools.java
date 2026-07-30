package com.chi.spa.booking.tool;

import com.chi.spa.booking.dto.BookingRequest;
import com.chi.spa.booking.exception.BusinessException;
import com.chi.spa.booking.model.Booking;
import com.chi.spa.booking.model.ServiceItem;
import com.chi.spa.booking.service.BookingService;
import com.chi.spa.booking.service.ServiceItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookingTools {

    private final BookingService bookingService;
    private final ServiceItemService serviceItemService;

    @Tool(description = "查詢目前所有可預約的 SPA 服務項目，包含每個項目的 ID、名稱、時長與價格。當顧客想預約但你不確定療程的正確 ID 時，一定要先呼叫這個工具查詢。")
    public String listServiceItems() {
        List<ServiceItem> items = serviceItemService.getAllServiceItems();
        if (items.isEmpty()) {
            return "目前系統中沒有任何可預約的服務項目。";
        }
        return items.stream()
                .map(i -> "ID:%d｜%s｜%d分鐘｜%d元".formatted(i.getId(), i.getName(), i.getDurationMinutes(), i.getPrice()))
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "查詢某個日期與時段目前還剩下幾個可預約名額（上限 2 人）。當顧客表明想預約的人數與時段（不管有沒有先講療程項目）時，一定要先呼叫這個工具確認名額是否足夠，再繼續詢問其他資訊。")
    public String checkAvailability(
            @ToolParam(description = "查詢日期，格式為 yyyy-MM-dd") String bookingDate,
            @ToolParam(description = "查詢時間，格式為 HH:mm，只能是 10:00、12:00、14:00、16:00、18:00 其中之一") String bookingTime
    ) {
        int remaining = bookingService.getRemainingSlots(
                LocalDate.parse(bookingDate),
                LocalTime.parse(bookingTime)
        );
        return "%s %s 這個時段目前剩餘 %d 個名額（上限 2 人）。".formatted(bookingDate, bookingTime, remaining);
    }

    @Tool(description = "為顧客建立一筆正式的 SPA 預約紀錄，會實際寫入資料庫並自動檢查該時段是否已額滿（每個時段上限 2 人）。只有在顧客明確提供姓名、預約日期、預約時間、與正確的服務項目 ID 之後才呼叫這個工具，不要自行捏造任何欄位。")
    public String createBooking(
            @ToolParam(description = "顧客姓名") String customerName,
            @ToolParam(description = "顧客電話，若顧客沒有提供可傳空字串") String customerPhone,
            @ToolParam(description = "顧客Email，若顧客沒有提供可傳空字串") String customerEmail,
            @ToolParam(description = "預約日期，格式為 yyyy-MM-dd，例如 2026-08-01") String bookingDate,
            @ToolParam(description = "預約時間，只能是 10:00、12:00、14:00、16:00、18:00 其中之一，格式為 HH:mm") String bookingTime,
            @ToolParam(description = "服務項目 ID，必須先呼叫 listServiceItems 查詢取得正確的 ID") Long serviceItemId
    ) {
        BookingRequest request = new BookingRequest();
        request.setCustomerName(customerName);
        request.setCustomerPhone(customerPhone);
        request.setCustomerEmail(customerEmail);
        request.setBookingDate(LocalDate.parse(bookingDate));
        request.setBookingTime(LocalTime.parse(bookingTime));
        request.setServiceItemId(serviceItemId);

        try {
            Booking booking = bookingService.createBooking(request);
            return "預約成功！預約編號 #%d，%s 已於 %s %s 成功登記。"
                    .formatted(booking.getId(), booking.getCustomerName(), booking.getBookingDate(), booking.getBookingTime());
        } catch (BusinessException e) {
            return "預約失敗：" + e.getMessage();
        }
    }

    @Tool(description = "查詢某個日期與時段目前已有哪些預約紀錄，包含每筆預約的 ID、顧客姓名、電話與療程。在顧客想取消或修改預約前，一定要先呼叫這個工具找到正確的預約 ID，不可以自己猜測或編造 ID。")
    public String listBookingsByDateTime(
            @ToolParam(description = "查詢日期，格式為 yyyy-MM-dd") String bookingDate,
            @ToolParam(description = "查詢時間，格式為 HH:mm") String bookingTime
    ) {
        LocalDate date = LocalDate.parse(bookingDate);
        LocalTime time = LocalTime.parse(bookingTime);

        List<Booking> matched = bookingService.getAllBookings().stream()
                .filter(b -> b.getBookingDate().equals(date) && b.getBookingTime().equals(time))
                .toList();

        if (matched.isEmpty()) {
            return "%s %s 這個時段目前沒有任何預約紀錄。".formatted(bookingDate, bookingTime);
        }

        return matched.stream()
                .map(b -> "預約ID:%d｜姓名:%s｜電話:%s｜療程:%s"
                        .formatted(b.getId(), b.getCustomerName(), b.getCustomerPhone(), b.getServiceItem().getName()))
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "修改一筆已存在的預約，可以只改療程項目、只改日期時間、或兩者都改。呼叫前必須先透過查詢工具確認正確的預約編號 ID，絕對不要自行猜測 ID。修改日期時間時會自動檢查目標時段名額是否足夠（上限 2 人）。不需要修改的欄位請傳 null。")
    public String updateBooking(
            @ToolParam(description = "要修改的預約編號 ID") Long bookingId,
            @ToolParam(description = "新的服務項目 ID，若不修改療程請傳 null") Long newServiceItemId,
            @ToolParam(description = "新的預約日期，格式 yyyy-MM-dd，若不修改日期請傳 null") String newBookingDate,
            @ToolParam(description = "新的預約時間，格式 HH:mm，若不修改時間請傳 null") String newBookingTime
    ) {
        try {
            LocalDate date = newBookingDate != null ? LocalDate.parse(newBookingDate) : null;
            LocalTime time = newBookingTime != null ? LocalTime.parse(newBookingTime) : null;

            Booking updated = bookingService.updateBooking(bookingId, newServiceItemId, date, time);
            return "修改成功！預約編號 #%d 目前資訊：%s ／ %s %s ／ 療程：%s"
                    .formatted(updated.getId(), updated.getCustomerName(),
                            updated.getBookingDate(), updated.getBookingTime(), updated.getServiceItem().getName());
        } catch (BusinessException e) {
            return "修改失敗：" + e.getMessage();
        }
    }

    @Tool(description = "取消一筆已存在的預約，需要提供正確的預約編號 ID。呼叫前必須先透過查詢工具確認 ID 是正確的，絕對不要自行猜測 ID。")
    public String cancelBooking(@ToolParam(description = "要取消的預約編號 ID") Long bookingId) {
        boolean deleted = bookingService.deleteBooking(bookingId);
        return deleted
                ? "預約編號 #%d 已成功取消。".formatted(bookingId)
                : "找不到預約編號 #%d，取消失敗，請確認編號是否正確。".formatted(bookingId);
    }
}