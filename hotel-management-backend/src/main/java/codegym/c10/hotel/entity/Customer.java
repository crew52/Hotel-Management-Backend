package codegym.c10.hotel.entity;

import codegym.c10.hotel.eNum.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10,20}$", message = "Phone must be between 10 and 20 digits")
    @Column(name = "phone", nullable = false, unique = true, length = 20)
    private String phone;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    private LocalDate dob;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Column(length = 255)
    private String address;

    @Size(max = 20, message = "ID Card must not exceed 20 characters")
    @Column(name = "id_card", length = 20)
    private String idCard;

    @Size(max = 50, message = "Nationality must not exceed 50 characters")
    @Column(length = 50)
    private String nationality;

    @Column(columnDefinition = "TEXT")
    private String note;

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
