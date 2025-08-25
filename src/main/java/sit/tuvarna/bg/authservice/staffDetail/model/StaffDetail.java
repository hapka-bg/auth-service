package sit.tuvarna.bg.authservice.staffDetail.model;

import jakarta.persistence.*;
import lombok.Data;
import sit.tuvarna.bg.authservice.user.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@Table(name = "staff_details")
public class StaffDetail {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private BigDecimal salary;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "hired_date")
    private LocalDate hiredDate;

    private String notes;
}
