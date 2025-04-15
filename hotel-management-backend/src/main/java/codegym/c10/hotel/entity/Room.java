package codegym.c10.hotel.entity;

import codegym.c10.hotel.eNum.RoomStatus;
import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Room category must not be null")
    @ManyToOne
    @JoinColumn(name = "room_category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_room_type"))
    private RoomCategory roomCategory;

    @Min(value = 0, message = "Floor number must be 0 or higher")
    @Column
    private Integer floor;

    @PastOrPresent(message = "Start date cannot be in the future")
    @Column(name = "start_date")
    private LocalDate startDate;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('AVAILABLE', 'MAINTENANCE', 'OCCUPIED') DEFAULT 'AVAILABLE'")
    private RoomStatus status;

    @Size(max = 1000, message = "Note must be less than 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String note;

    @NotNull(message = "Cleaning status must be specified")
    @Column(name = "is_clean")
    private Boolean isClean = true;

    @Min(value = 0, message = "Check-in duration must be 0 or more")
    @Column(name = "check_in_duration")
    private Integer checkInDuration = 0;

    @Size(max = 255, message = "Image path must be less than 255 characters")
    @Column(name = "img_1")
    private String img1;

    @Size(max = 255, message = "Image path must be less than 255 characters")
    @Column(name = "img_2")
    private String img2;

    @Size(max = 255, message = "Image path must be less than 255 characters")
    @Column(name = "img_3")
    private String img3;

    @Size(max = 255, message = "Image path must be less than 255 characters")
    @Column(name = "img_4")
    private String img4;
}
