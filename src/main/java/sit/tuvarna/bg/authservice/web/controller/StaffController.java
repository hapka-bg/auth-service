package sit.tuvarna.bg.authservice.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.tuvarna.bg.authservice.staffSchedule.service.StaffScheduleService;
import sit.tuvarna.bg.authservice.user.service.AuthService;
import sit.tuvarna.bg.authservice.web.dto.AllUsersResponse;
import sit.tuvarna.bg.authservice.web.dto.UserDetailsForOnlineOrders;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/staff")
public class StaffController {

    private final StaffScheduleService staffScheduleService;
    private final AuthService authService;

    @Autowired
    public StaffController(StaffScheduleService staffScheduleService, AuthService authService) {
        this.staffScheduleService = staffScheduleService;
        this.authService = authService;
    }

    @GetMapping("/active")
    public ResponseEntity<Long> activeStaff() {
        return ResponseEntity.ok(staffScheduleService.getActiveUsers());
    }

    @GetMapping("/all")
    public ResponseEntity<List<AllUsersResponse>> allStaff() {
        return ResponseEntity.ok(authService.getAllUsers());
    }


    @GetMapping("/names")
    public ResponseEntity<List<String>> getAllStaffNames(@RequestParam List<UUID> ids) {
        List<String> names = authService.getNames(ids);
        return ResponseEntity.ok(names);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailsForOnlineOrders> getPhoneNumberAndAddress(@PathVariable UUID id) {
        UserDetailsForOnlineOrders phoneNumberAndAddress = authService.getPhoneNumberAndAddress(id);
        return ResponseEntity.ok(phoneNumberAndAddress);
    }

}