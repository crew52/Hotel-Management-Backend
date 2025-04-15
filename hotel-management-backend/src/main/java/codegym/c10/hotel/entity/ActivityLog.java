package codegym.c10.hotel.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Action must not be blank")
    @Size(max = 100, message = "Action must be at most 100 characters")
    @Column(nullable = false, length = 100)
    private String action;

    @NotNull(message = "Timestamp must not be null")
    @PastOrPresent(message = "Timestamp must be in the past or present")
    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Size(max = 65535, message = "Description is too long") // Optional TEXT limit
    @Column(columnDefinition = "TEXT")
    private String description;
}