package sit.tuvarna.bg.authservice.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import sit.tuvarna.bg.authservice.staffDetail.repository.StaffDetailRepository;
import sit.tuvarna.bg.authservice.user.repository.UserRepository;


@Component
@RequiredArgsConstructor
public class TableSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StaffDetailRepository staffDetailRepository;

    @Override

    public void run(String... args) {



    }
}


