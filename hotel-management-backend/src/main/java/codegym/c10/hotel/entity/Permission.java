package codegym.c10.hotel.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;
import java.util.Set;

@EqualsAndHashCode(callSuper = true, exclude = {"roles"}) // Loại trừ roles
@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Permission name must not be blank")
    @Size(max = 100, message = "Permission name must be less than or equal to 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles;
}

