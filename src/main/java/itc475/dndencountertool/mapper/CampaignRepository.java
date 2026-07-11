package itc475.dndencountertool.mapper;

import itc475.dndencountertool.domain.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Field mapping done within class definition
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByUserId(Long userId);
    List<Campaign> findByUserIdOrderByCompleteAscNameAsc(Long userId);
}
