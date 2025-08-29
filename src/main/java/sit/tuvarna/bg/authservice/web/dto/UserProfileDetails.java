package sit.tuvarna.bg.authservice.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileDetails {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String city;
}
