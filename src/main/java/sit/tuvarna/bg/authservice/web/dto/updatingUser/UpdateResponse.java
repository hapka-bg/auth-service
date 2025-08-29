package sit.tuvarna.bg.authservice.web.dto.updatingUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import sit.tuvarna.bg.authservice.user.model.User;

@Data
@AllArgsConstructor
public class UpdateResponse {

    private String token;
    private User user;
}
