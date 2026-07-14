package itc475.dndencountertool.controller;

import itc475.dndencountertool.domain.Combatant;
import itc475.dndencountertool.domain.Encounter;
import itc475.dndencountertool.domain.User;
import itc475.dndencountertool.mapper.CombatantRepository;
import itc475.dndencountertool.mapper.EncounterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class EncounterApiController {

    private final EncounterRepository encounterRepository;
    private final CombatantRepository combatantRepository;

    public EncounterApiController(EncounterRepository encounterRepository,
                                  CombatantRepository combatantRepository) {
        this.encounterRepository = encounterRepository;
        this.combatantRepository = combatantRepository;
    }

    @PutMapping("/encounters/{encounterId}/current-turn")
    public ResponseEntity<Void> updateCurrentTurn(@PathVariable Long encounterId,
                                                  @AuthenticationPrincipal User user,
                                                  @RequestBody CurrentTurnRequest request) {
        Encounter encounter = getOwnedEncounterOrThrow(encounterId, user);

        if (request.combatantId() == null) {
            encounter.setCurrentTurn(null);
        } else {
            Combatant combatant = combatantRepository.findById(request.combatantId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            if (!combatant.getEncounter().getId().equals(encounter.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            encounter.setCurrentTurn(combatant);
        }

        encounterRepository.save(encounter);
        return ResponseEntity.noContent().build();
    }

    record CurrentTurnRequest(Long combatantId) {}

    private Encounter getOwnedEncounterOrThrow(Long encounterId, User user) {
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!encounter.getCampaign().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return encounter;
    }
}