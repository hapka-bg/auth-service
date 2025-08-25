package sit.tuvarna.bg.authservice.web.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserDetailsForOnlineOrders {
    private String phoneNumber;
    private String address;
}
