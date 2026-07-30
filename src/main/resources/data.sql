INSERT IGNORE INTO service_items (name, description, duration_minutes, price)VALUES
('全身精油深層舒壓', '放鬆緊繃肌肉，改善疲勞感', 90, 1800),
('亮顏保濕臉部保養','多重保濕精華，使臉部重回光彩',90,1200),
('頭部放鬆課程','透過專業撥筋手法放鬆頭皮，享受頭部清爽輕盈感',60,1000);

INSERT IGNORE INTO customers (id, name, email, phone_number, gender) VALUES
(1, '陳小美', 'may@example.com', '0912345678', 'FEMALE'),
(2, '林大明', 'ming@example.com', '0987654321', 'MALE');

INSERT IGNORE INTO bookings (id, customer_name, customer_email, customer_phone, booking_date, booking_time, service_item_id, created_at) VALUES
(1, '陳小美', 'may@example.com', '0912345678', '2026-08-01', '14:00:00', 1, NOW()),
(2, '林大明', 'ming@example.com', '0987654321', '2026-08-02', '10:00:00', 3, NOW());