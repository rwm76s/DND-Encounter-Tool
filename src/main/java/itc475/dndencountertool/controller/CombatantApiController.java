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
public class CombatantApiController {

    private final CombatantRepository combatantRepository;
    private final EncounterRepository encounterRepository;

    public CombatantApiController(CombatantRepository combatantRepository,
                                  EncounterRepository encounterRepository) {
        this.combatantRepository = combatantRepository;
        this.encounterRepository = encounterRepository;
    }

    @PutMapping("/combatants/{combatantId}")
    public CombatantResponse updateCombatant(@PathVariable Long combatantId,
                                             @AuthenticationPrincipal User user,
                                             @RequestBody CombatantUpdateRequest request) {
        Combatant combatant = getOwnedCombatantOrThrow(combatantId, user);

        combatant.setInitiative(request.initiative());
        if (!combatant.isPlayer()) {
            combatant.setHp(request.hp());
            combatant.setMaxHp(request.maxHp());
            combatant.setAc(request.ac());
        }
        combatant.setIncapacitated(request.incapacitated());

        combatantRepository.save(combatant);

        return new CombatantResponse(
                combatant.getId(),
                combatant.getInitiative(),
                combatant.getHp(),
                combatant.getMaxHp(),
                combatant.getAc(),
                combatant.isIncapacitated()
        );
    }

    @PostMapping("/encounters/{encounterId}/combatants")
    public CombatantFullResponse createCombatant(@PathVariable Long encounterId,
                                                 @AuthenticationPrincipal User user,
                                                 @RequestBody NewCombatantRequest request) {
        Encounter encounter = getOwnedEncounterOrThrow(encounterId, user);

        Combatant combatant = new Combatant(request.name(), false, encounter);
        combatant.setInitiative(request.initiative());
        combatant.setHp(request.hp());
        combatant.setMaxHp(request.maxHp());
        combatant.setAc(request.ac());

        combatantRepository.save(combatant);

        return new CombatantFullResponse(
                combatant.getId(),
                combatant.getName(),
                combatant.isPlayer(),
                combatant.getInitiative(),
                combatant.getHp(),
                combatant.getMaxHp(),
                combatant.getAc(),
                combatant.isIncapacitated()
        );
    }

    @DeleteMapping("/combatants/{combatantId}")
    public ResponseEntity<Void> deleteCombatant(@PathVariable Long combatantId, @AuthenticationPrincipal User user) {
        Combatant combatant = getOwnedCombatantOrThrow(combatantId, user);
        combatantRepository.delete(combatant);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/combatants/{combatantId}/notes")
    public NoteResponse updateNote(@PathVariable Long combatantId,
                                   @AuthenticationPrincipal User user,
                                   @RequestBody NoteRequest request) {
        Combatant combatant = getOwnedCombatantOrThrow(combatantId, user);
        combatant.setNotes(request.notes());
        combatantRepository.save(combatant);
        return new NoteResponse(combatant.getId(), combatant.getNotes());
    }

    @DeleteMapping("/combatants/{combatantId}/notes")
    public ResponseEntity<Void> deleteNote(@PathVariable Long combatantId, @AuthenticationPrincipal User user) {
        Combatant combatant = getOwnedCombatantOrThrow(combatantId, user);
        combatant.setNotes(null);
        combatantRepository.save(combatant);
        return ResponseEntity.noContent().build();
    }

    record NoteRequest(String notes) {}
    record NoteResponse(Long id, String notes) {}

    record CombatantUpdateRequest(Integer initiative, Integer hp, Integer maxHp, Integer ac, boolean incapacitated) {}
    record CombatantResponse(Long id, Integer initiative, Integer hp, Integer maxHp, Integer ac, boolean incapacitated) {}
    record NewCombatantRequest(String name, Integer initiative, Integer hp, Integer maxHp, Integer ac) {}
    record CombatantFullResponse(Long id, String name, boolean player, Integer initiative, Integer hp, Integer maxHp, Integer ac, boolean incapacitated) {}

    private Combatant getOwnedCombatantOrThrow(Long combatantId, User user) {
        Combatant combatant = combatantRepository.findById(combatantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!combatant.getEncounter().getCampaign().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return combatant;
    }

    private Encounter getOwnedEncounterOrThrow(Long encounterId, User user) {
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!encounter.getCampaign().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return encounter;
    }
}