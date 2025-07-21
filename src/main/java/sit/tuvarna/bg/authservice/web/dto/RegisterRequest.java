package sit.tuvarna.bg.authservice.web.dto;

public record RegisterRequest(String firstName,
                              String lastName,
                              String phone,
                              String email,
                              String password,
                              String city,
                              String street,
                              String turnstileToken
) {}


