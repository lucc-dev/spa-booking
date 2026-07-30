package com.chi.spa.booking.model;

import com.chi.spa.booking.dto.BookingRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "bookings")
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 增加 implements Serializable：
// 讓 Booking 物件可以被「序列化」轉為二進位資料。
// 當使用 Spring Cache / Redis 將預約紀錄進行快取時，Redis 必須透過此介面才能順利寫入與讀取物件，避免 NotSerializableException。
public class Booking implements Serializable {

    // 增加 serialVersionUID：
    // 序列化版本控制器。確保物件在寫入 Redis 與未來從 Redis 讀出時，Java 能核對版本一致性，避免類別修改後反序列化失敗。
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = true)
    private String customerEmail;

    @Column(nullable = true)
    private String customerPhone;

    // 關聯到正式的顧客資料表，nullable = true 是為了相容舊資料
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = true)
    private Customer customer;

    @Column(nullable = false)
    private LocalDate bookingDate;

    @Column(nullable = false)
    private LocalTime bookingTime;

    // 建立與 ServiceItem 的多對一關聯
    @ManyToOne
    @JoinColumn(name = "service_item_id", nullable = false)
    private ServiceItem serviceItem;

    private LocalDateTime createdAt;

    // 讓資料在寫入時，自動帶入當前的系統時間
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 多接收一個 Customer 參數，並在 builder 裡一併設定
    public static Booking fromDto(BookingRequest request, ServiceItem serviceItem, Customer customer) {
        return Booking.builder()
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
                .customer(customer)                         // 關聯到正式的顧客資料
                .bookingDate(request.getBookingDate())
                .bookingTime(request.getBookingTime())
                .serviceItem(serviceItem)                  // 將查到的完整 ServiceItem 物件與這筆預約做關聯綁定
                .build();                                  // 正式生成 Booking 實例
    }
}