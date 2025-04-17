SET @current_user_id = 4; -- user_id của nhân viên đang cập nhật
UPDATE Rooms
SET status = 'IN_USE'
WHERE id = 1;


select * from activity_logs;