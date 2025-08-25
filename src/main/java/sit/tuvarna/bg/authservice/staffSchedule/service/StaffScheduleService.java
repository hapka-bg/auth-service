package sit.tuvarna.bg.authservice.staffSchedule.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.tuvarna.bg.authservice.staffSchedule.repository.StaffScheduleRepository;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class StaffScheduleService {

    private final StaffScheduleRepository staffScheduleRepository;

    @Autowired
    public StaffScheduleService(StaffScheduleRepository staffScheduleRepository) {
        this.staffScheduleRepository = staffScheduleRepository;
    }


    public Long getActiveUsers() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        return staffScheduleRepository.countDistinctUserByShiftDayAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(today,now,now);
    }
}
