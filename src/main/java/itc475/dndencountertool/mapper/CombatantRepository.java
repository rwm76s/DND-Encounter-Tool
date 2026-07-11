package itc475.dndencountertool.mapper;

import itc475.dndencountertool.domain.Combatant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Field mapping done within class definition
public interface CombatantRepository extends JpaRepository<Combatant, Long> {
    List<Combatant> findByEncounterId(Long encounterId);
    List<Combatant> findByEncounterIdOrderByInitiativeDesc(Long encounterId);
}
