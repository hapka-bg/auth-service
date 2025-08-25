package sit.tuvarna.bg.authservice.staffDetail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.tuvarna.bg.authservice.staffDetail.model.StaffDetail;

import java.util.UUID;

@Repository
public interface StaffDetailRepository extends JpaRepository<StaffDetail, UUID> {
}
