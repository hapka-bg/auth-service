package sit.tuvarna.bg.authservice.web.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class WaiterOrders {
    private UUID waiterId;
    private Long ordersCount;
    private String name;
}
