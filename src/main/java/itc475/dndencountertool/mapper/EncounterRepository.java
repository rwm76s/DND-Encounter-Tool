package itc475.dndencountertool.mapper;

import itc475.dndencountertool.domain.Encounter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Field mapping done within class definition
public interface EncounterRepository extends JpaRepository<Encounter, Long> {
    List<Encounter> findByCampaignId(Long campaignId);
}
