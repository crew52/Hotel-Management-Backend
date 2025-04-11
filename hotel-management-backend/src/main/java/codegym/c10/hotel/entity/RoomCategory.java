package codegym.c10.hotel.entity;

import codegym.c10.hotel.eNum.ExtraFeeType;
import codegym.c10.hotel.eNum.RoomCategoryStatus;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "room_category_code", nullable = false, unique = true, length = 20)
    private String roomCategoryCode;

    @Column(name = "room_category_name", nullable = false, length = 100)
    private String roomCategoryName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "hourly_price", precision = 10, scale = 2)
    private BigDecimal hourlyPrice;

    @Column(name = "daily_price", precision = 10, scale = 2)
    private BigDecimal dailyPrice;

    @Column(name = "overnight_price", precision = 10, scale = 2)
    private BigDecimal overnightPrice;

    @Column(name = "early_checkin_fee", precision = 10, scale = 2)
    private BigDecimal earlyCheckinFee;

    @Column(name = "late_checkout_fee", precision = 10, scale = 2)
    private BigDecimal lateCheckoutFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "extra_fee_type", length = 10)
    private ExtraFeeType extraFeeType = ExtraFeeType.FIXED;

    @Column(name = "default_extra_fee", precision = 10, scale = 2)
    private BigDecimal defaultExtraFee = BigDecimal.ZERO;

    @Column(name = "apply_to_all_categories")
    private Boolean applyToAllCategories = false;

    @Column(name = "standard_adult_capacity")
    private Integer standardAdultCapacity = 1;

    @Column(name = "standard_child_capacity")
    private Integer standardChildCapacity = 0;

    @Column(name = "max_adult_capacity")
    private Integer maxAdultCapacity = 2;

    @Column(name = "max_child_capacity")
    private Integer maxChildCapacity = 1;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private RoomCategoryStatus status = RoomCategoryStatus.ACTIVE;

    @Column(name = "img_url", length = 255)
    private String imgUrl;
}