package sit.tuvarna.bg.authservice.staffSchedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.tuvarna.bg.authservice.staffSchedule.model.StaffSchedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Repository
public interface StaffScheduleRepository extends JpaRepository<StaffSchedule, UUID> {

    Long countDistinctUserByShiftDayAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            LocalDate shiftDay,
            LocalTime currentTime1,
            LocalTime currentTime2
    );

}
