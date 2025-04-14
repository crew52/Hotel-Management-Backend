package codegym.c10.hotel.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username must not be blank")
    @Size(max = 50, message = "Username must be at most 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, max = 255, message = "Password must be between 6 and 255 characters")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must be at most 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotNull(message = "isLocked field must not be null")
    @Column(name = "is_locked")
    private Boolean isLocked = false;

    @NotEmpty(message = "User must have at least one role")
    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<@NotNull Role> roles;
}
