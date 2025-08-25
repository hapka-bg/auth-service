package sit.tuvarna.bg.authservice.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class OrderSummary {
    private UUID orderId;
    private UUID userId;
    private LocalDateTime orderDate;
    private BigDecimal total;
    private String orderStatus;

    private String name;
}
