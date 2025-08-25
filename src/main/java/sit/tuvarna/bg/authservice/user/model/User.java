package sit.tuvarna.bg.authservice.user.model;

import jakarta.persistence.*;
import lombok.*;
import sit.tuvarna.bg.authservice.staffDetail.model.StaffDetail;
import sit.tuvarna.bg.authservice.staffSchedule.model.StaffSchedule;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = false)
    private String phoneNumber;

    private String street;

    private String city;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<StaffSchedule> staffSchedules;

    @OneToOne(mappedBy = "user",cascade = CascadeType.ALL,fetch =  FetchType.EAGER)
    private StaffDetail staffDetail;
}

