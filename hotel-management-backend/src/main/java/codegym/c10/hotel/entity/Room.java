package codegym.c10.hotel.entity;

import codegym.c10.hotel.eNum.RoomStatus;
import jakarta.persistence.*;
import lombok.*;

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
    @Column(name = "room_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_room_type"))
    private RoomCategory roomCategory;

    @Column
    private Integer floor;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RoomStatus status = RoomStatus.AVAILABLE;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_clean")
    private Boolean isClean = true;

    @Column(name = "check_in_duration")
    private Integer checkInDuration = 0;

    @Column(name = "img_1")
    private String img1;

    @Column(name = "img_2")
    private String img2;

    @Column(name = "img_3")
    private String img3;

    @Column(name = "img_4")
    private String img4;
}