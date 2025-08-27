package sit.tuvarna.bg.authservice.web.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddStaffRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String role;
    private BigDecimal salary;
}
