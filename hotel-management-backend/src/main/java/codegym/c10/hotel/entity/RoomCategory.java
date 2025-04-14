package codegym.c10.hotel.entity;

import codegym.c10.hotel.eNum.ExtraFeeType;
import codegym.c10.hotel.eNum.RoomCategoryStatus;
import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Entity
@Table(name = "room_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class RoomCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_category_id")
    private Long id;

    @NotBlank(message = "Room category code is required")
    @Size(max = 20, message = "Room category code must not exceed 20 characters")
    @Column(name = "room_category_code", nullable = false, unique = true, length = 20)
    private String roomCategoryCode;

    @NotBlank(message = "Room category name is required")
    @Size(max = 100, message = "Room category name must not exceed 100 characters")
    @Column(name = "room_category_name", nullable = false, length = 100)
    private String roomCategoryName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @DecimalMin(value = "0.00", inclusive = true, message = "Hourly price must be positive or zero")
    @Column(name = "hourly_price", precision = 10, scale = 2)
    private BigDecimal hourlyPrice;

    @DecimalMin(value = "0.00", inclusive = true, message = "Daily price must be positive or zero")
    @Column(name = "daily_price", precision = 10, scale = 2)
    private BigDecimal dailyPrice;

    @DecimalMin(value = "0.00", inclusive = true, message = "Overnight price must be positive or zero")
    @Column(name = "overnight_price", precision = 10, scale = 2)
    private BigDecimal overnightPrice;

    @DecimalMin(value = "0.00", inclusive = true, message = "Early check-in fee must be positive or zero")
    @Column(name = "early_checkin_fee", precision = 10, scale = 2)
    private BigDecimal earlyCheckinFee;

    @DecimalMin(value = "0.00", inclusive = true, message = "Late checkout fee must be positive or zero")
    @Column(name = "late_checkout_fee", precision = 10, scale = 2)
    private BigDecimal lateCheckoutFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "extra_fee_type", length = 10)
    private ExtraFeeType extraFeeType = ExtraFeeType.FIXED;

    @DecimalMin(value = "0.00", inclusive = true, message = "Default extra fee must be positive or zero")
    @Column(name = "default_extra_fee", precision = 10, scale = 2)
    private BigDecimal defaultExtraFee = BigDecimal.ZERO;

    @Column(name = "apply_to_all_categories")
    private Boolean applyToAllCategories = false;

    @Min(value = 0, message = "Standard adult capacity must be >= 0")
    @Column(name = "standard_adult_capacity")
    private Integer standardAdultCapacity = 1;

    @Min(value = 0, message = "Standard child capacity must be >= 0")
    @Column(name = "standard_child_capacity")
    private Integer standardChildCapacity = 0;

    @Min(value = 0, message = "Max adult capacity must be >= 0")
    @Column(name = "max_adult_capacity")
    private Integer maxAdultCapacity = 2;

    @Min(value = 0, message = "Max child capacity must be >= 0")
    @Column(name = "max_child_capacity")
    private Integer maxChildCapacity = 1;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE'")
    private RoomCategoryStatus status;

    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    @Column(name = "img_url", length = 255)
    private String imgUrl;
}
