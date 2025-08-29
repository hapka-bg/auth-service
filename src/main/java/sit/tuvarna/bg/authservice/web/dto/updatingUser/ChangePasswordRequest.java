package sit.tuvarna.bg.authservice.web.dto.updatingUser;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
    private String confirmNewPassword;
}
