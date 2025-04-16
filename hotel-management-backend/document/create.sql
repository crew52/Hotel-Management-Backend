USE hotel_management_db;

INSERT INTO Room_Category (
    code, name, description, hourly_price, daily_price, overnight_price, 
    early_checkin_fee, late_checkout_fee, extra_fee_type, default_extra_fee, 
    apply_to_all_categories, standard_adult_capacity, standard_child_capacity, 
    max_adult_capacity, max_child_capacity, status, img_url, deleted
) VALUES
('RC001', 'Standard Room', 'A standard room with basic amenities', 50.00, 150.00, 200.00, 
    10.00, 15.00, 'FIXED', 10.00, FALSE, 2, 1, 4, 2, 'ACTIVE', 'url_to_image_1.jpg', FALSE),
('RC002', 'Deluxe Room', 'A spacious room with luxury features', 75.00, 225.00, 300.00, 
    15.00, 20.00, 'PERCENTAGE', 5.00, TRUE, 3, 2, 5, 3, 'ACTIVE', 'url_to_image_2.jpg', FALSE),
('RC003', 'Suite Room', 'A luxurious suite with multiple rooms and a living area', 100.00, 300.00, 400.00, 
    20.00, 25.00, 'FIXED', 20.00, TRUE, 4, 3, 6, 4, 'INACTIVE', 'url_to_image_3.jpg', TRUE);

select * from Room_Category;

INSERT INTO Rooms (
    room_category_id, floor, start_date, status, note, is_clean, check_in_duration, 
    img_1, img_2, img_3, img_4, deleted
) VALUES
(1, 1, '2023-12-01', 'AVAILABLE', 'Near elevator', TRUE, 2, 'img1_1.jpg', 'img1_2.jpg', NULL, NULL, FALSE),
(1, 2, '2023-12-05', 'UPCOMING', 'Leaky faucet', FALSE, 0, 'img2_1.jpg', NULL, NULL, NULL, FALSE),
(1, 3, '2024-01-01', 'UPCOMING', NULL, TRUE, 3, NULL, NULL, NULL, NULL, FALSE),
(2, 1, '2024-01-15', 'IN_USE', 'Nice view', TRUE, 1, 'img4_1.jpg', 'img4_2.jpg', 'img4_3.jpg', NULL, FALSE),
(2, 4, '2024-02-01', 'CHECKOUT_SOON', 'Family room', TRUE, 5, 'img5_1.jpg', NULL, NULL, NULL, FALSE),
(2, 5, '2024-02-10', 'UPCOMING', 'Under renovation', FALSE, 0, NULL, NULL, NULL, NULL, FALSE),
(3, 6, '2024-03-01', 'AVAILABLE', 'VIP guest', TRUE, 0, 'img7_1.jpg', 'img7_2.jpg', NULL, NULL, FALSE),
(3, 6, '2024-03-02', 'CHECKOUT_SOON', NULL, TRUE, 4, 'img8_1.jpg', NULL, NULL, NULL, FALSE),
(1, 7, '2024-03-10', 'AVAILABLE', NULL, TRUE, 0, NULL, NULL, NULL, NULL, FALSE),
(2, 8, '2024-03-15', 'AVAILABLE', 'Recently cleaned', TRUE, 1, NULL, NULL, NULL, NULL, FALSE);

select * from Rooms;