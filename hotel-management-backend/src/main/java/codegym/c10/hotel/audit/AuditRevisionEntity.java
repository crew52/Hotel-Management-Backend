package codegym.c10.hotel.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.io.Serializable;
import java.util.Date;

/**
 * Custom revision entity for Hibernate Envers auditing
 */
@Entity
@RevisionEntity(AuditRevisionListener.class)
@Table(name = "revinfo_custom")
@Data
@EqualsAndHashCode
public class AuditRevisionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @RevisionNumber
    private int id;

    @RevisionTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;
} 