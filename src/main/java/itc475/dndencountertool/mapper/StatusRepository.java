package itc475.dndencountertool.mapper;

import itc475.dndencountertool.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Field mapping done within class definition
public interface StatusRepository extends JpaRepository<Status, Long> {
    List<Status> findByCombatantId(Long combatantId);
}
