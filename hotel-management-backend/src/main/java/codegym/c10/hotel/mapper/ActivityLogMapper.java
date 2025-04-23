package codegym.c10.hotel.mapper;

import codegym.c10.hotel.dto.ActivityLogDTO;
import codegym.c10.hotel.entity.ActivityLog;
import codegym.c10.hotel.entity.Employee;
import codegym.c10.hotel.entity.User;

public class ActivityLogMapper {
    public static ActivityLogDTO toDTO(ActivityLog log) {
        User user = log.getUser();
        String fullName = null;

        // Nếu dùng Spring Data, bạn có thể đảm bảo Employee được load qua User
        if (user != null && user.getId() != null) {
            Employee employee = user.getEmployee(); // Cần thêm mapping 2 chiều nếu chưa có
            fullName = (employee != null) ? employee.getFullName() : null;
        }

        assert user != null;
        return ActivityLogDTO.builder()
                .id(log.getId())
                .fullName(fullName)
                .userId(user.getId())
                .username(user.getUsername())
                .action(toFriendlyAction(log.getAction()))
                .timestamp(log.getTimestamp())
                .description(toFriendlyDescription(log.getAction(), log.getDescription()))
                .build();
    }

    private static String toFriendlyAction(String action) {
        if (action == null) return "";
        switch (action.toUpperCase()) {
            case "CREATE": return "Tạo mới";
            case "UPDATE": return "Cập nhật";
            case "DELETE": return "Xóa";
            case "LOGIN": return "Đăng nhập";
            case "LOGOUT": return "Đăng xuất";
            case "REGISTER": return "Đăng ký tài khoản";
            case "BOOK": return "Đặt phòng";
            default: return action;
        }
    }

    private static String toFriendlyDescription(String action, String description) {
        if (description == null || description.trim().isEmpty()
                || description.startsWith("Executed method:")) {
            // Nếu description là dạng Executed method: <Class>.<Method>(<Object>[id=<id>])
            if (description != null && description.startsWith("Executed method:")) {
                // Parse chuỗi để lấy thông tin class, method, id
                try {
                    String pattern = "Executed method: ";
                    String desc = description.substring(pattern.length()).trim();
                    // VD: UserService.update(User[id=1])
                    int dotIdx = desc.indexOf('.');
                    int parenIdx = desc.indexOf('(');
                    int bracketIdx = desc.indexOf("[id=");
                    int bracketEndIdx = desc.indexOf("]", bracketIdx);
                    String className = dotIdx > 0 ? desc.substring(0, dotIdx) : "";
                    String methodName = (dotIdx > 0 && parenIdx > dotIdx) ? desc.substring(dotIdx + 1, parenIdx) : "";
                    String objectType = (parenIdx > 0 && bracketIdx > parenIdx) ? desc.substring(parenIdx + 1, bracketIdx) : "";
                    String objectId = (bracketIdx > 0 && bracketEndIdx > bracketIdx) ? desc.substring(bracketIdx + 4, bracketEndIdx) : "";

                    // Sinh mô tả chi tiết hơn
                    switch (action.toUpperCase()) {
                        case "CREATE":
                            return String.format("Tạo mới %s (ID: %s)", objectType, objectId);
                        case "UPDATE":
                            return String.format("Cập nhật %s (ID: %s)", objectType, objectId);
                        case "DELETE":
                            return String.format("Xóa %s (ID: %s)", objectType, objectId);
                        default:
                            return String.format("%s %s (ID: %s)", toFriendlyAction(action), objectType, objectId);
                    }
                } catch (Exception e) {
                    // Nếu parse lỗi thì fallback về mô tả đơn giản
                }
            }
            switch (action.toUpperCase()) {
                case "CREATE": return "Đã tạo mới một bản ghi.";
                case "UPDATE": return "Đã cập nhật thông tin.";
                case "DELETE": return "Đã xóa một bản ghi.";
                case "LOGIN": return "Người dùng đã đăng nhập hệ thống.";
                case "LOGOUT": return "Người dùng đã đăng xuất hệ thống.";
                case "REGISTER": return "Người dùng đã đăng ký tài khoản.";
                case "BOOK": return "Người dùng đã đặt phòng.";
                default: return "Đã thực hiện hành động: " + action;
            }
        }
        return description;
    }
}