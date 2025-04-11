package codegym.c10.hotel.entity;

import codegym.c10.hotel.eNum.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(length = 15, unique = true)
    private String phone;

    @Column(name = "id_card", length = 20, unique = true)
    private String idCard;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String position;

    @Column(length = 100)
    private String department;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(columnDefinition = "TEXT")
    private String note;

}
