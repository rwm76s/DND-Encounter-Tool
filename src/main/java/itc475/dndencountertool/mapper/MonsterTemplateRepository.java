package itc475.dndencountertool.mapper;

import itc475.dndencountertool.domain.MonsterTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonsterTemplateRepository extends JpaRepository<MonsterTemplate, Long> {
    List<MonsterTemplate> findByCampaignIdOrderByNameAsc(Long campaignId);
}