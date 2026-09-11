package com.chi.spa.booking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "service_items")
// implements Serializable：
// 因為 Booking 裡面包含 ServiceItem 關聯物件，當整個 Booking 存入 Redis 時，裡面的 ServiceItem 也必須支援序列化，Redis 才能完整儲存。
public class ServiceItem implements Serializable {

    // serialVersionUID：
    // 序列化版本識別碼，確保跨系統或重啟後，Redis 內的 ServiceItem 資料能被正常解析。
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private Integer price;
}