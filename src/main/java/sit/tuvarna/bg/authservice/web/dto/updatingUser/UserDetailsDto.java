package sit.tuvarna.bg.authservice.web.dto.updatingUser;

import lombok.Data;

@Data
public class UserDetailsDto {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String city;
}

