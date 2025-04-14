package codegym.c10.hotel.entity;

import codegym.c10.hotel.eNum.Gender;
import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

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

    @NotNull(message = "User must be associated with employee")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank(message = "Full name must not be blank")
    @Size(max = 100, message = "Full name must be at most 100 characters")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @NotNull(message = "Gender is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Column(nullable = false)
    private LocalDate dob;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10–15 digits")
    @Column(length = 15, unique = true)
    private String phone;

    @Pattern(regexp = "^[A-Z0-9]{5,20}$", message = "ID card must be 5–20 characters, letters and digits only")
    @Column(name = "id_card", length = 20, unique = true)
    private String idCard;

    @Size(max = 255, message = "Address must be at most 255 characters")
    @Column(length = 255)
    private String address;

    @Size(max = 100, message = "Position must be at most 100 characters")
    @Column(length = 100)
    private String position;

    @Size(max = 100, message = "Department must be at most 100 characters")
    @Column(length = 100)
    private String department;

    @NotNull(message = "Start date is required")
    @PastOrPresent(message = "Start date cannot be in the future")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Size(max = 65535, message = "Note is too long") // Optional: validate TEXT if needed
    @Column(columnDefinition = "TEXT")
    private String note;
}
