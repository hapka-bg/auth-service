package sit.tuvarna.bg.authservice.web.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AllUsersResponse {
    private String name;
    private String role;
    private String email;
    private String salary;
}
