package codegym.c10.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * DTO chứa thông tin về một phiên bản (revision) của entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityRevisionDTO<T> {
    
    /**
     * Số phiên bản (revision number)
     */
    private int revisionNumber;
    
    /**
     * Thời gian diễn ra thay đổi
     */
    private Date revisionDate;
    
    /**
     * Tên người dùng thực hiện thay đổi
     */
    private String username;
    
    /**
     * ID người dùng thực hiện thay đổi
     */
    private Long userId;
    
    /**
     * Loại thay đổi (ADD, MOD, DEL)
     */
    private String revisionType;
    
    /**
     * Dữ liệu của entity tại phiên bản này
     */
    private T entity;
} 