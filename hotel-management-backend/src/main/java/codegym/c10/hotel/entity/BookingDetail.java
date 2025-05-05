package codegym.c10.hotel.entity;

import codegym.c10.hotel.eNum.BookingDetailStatus;
import codegym.c10.hotel.eNum.RentType;
import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;
import codegym.c10.hotel.eNum.RoomStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Booking_Details")
public class BookingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_main"))
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_room"))
    private Room room;

    @NotNull
    @Column(name = "checkin_time")
    private LocalDateTime checkinTime;

    @NotNull
    @Column(name = "checkout_time")
    private LocalDateTime checkoutTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "rent_type")
    private RentType rentType = RentType.HOURLY;

    @Min(1)
    private int duration = 1;

    @Column(precision = 10, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private BookingDetailStatus status = BookingDetailStatus.BOOKED;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_status_in_booking")
    private RoomStatus roomStatus = RoomStatus.AVAILABLE;

    @Column(name = "adult_count", nullable = false)
    private Integer adultCount = 1;   // số lượng người lớn, mặc định 1

    @Column(name = "child_count", nullable = false)
    private Integer childCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
