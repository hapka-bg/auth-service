package sit.tuvarna.bg.authservice.utils;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TurnstileService {

    @Value("${turnstile.secret}")
    private String secret;

    private final RestTemplate rest = new RestTemplate();
    private static final String url = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    public boolean verify(String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String body = "secret=%s&response=%s".formatted(secret, token);
        String r = rest.postForObject(url, new HttpEntity<>(body, headers), String.class);
        return r != null && new JSONObject(r).getBoolean("success");
    }
}