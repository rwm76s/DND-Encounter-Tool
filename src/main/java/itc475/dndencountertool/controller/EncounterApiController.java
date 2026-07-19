package itc475.dndencountertool.controller;

import itc475.dndencountertool.domain.Campaign;
import itc475.dndencountertool.domain.Combatant;
import itc475.dndencountertool.domain.Encounter;
import itc475.dndencountertool.domain.User;
import itc475.dndencountertool.mapper.CampaignRepository;
import itc475.dndencountertool.mapper.CombatantRepository;
import itc475.dndencountertool.mapper.EncounterRepository;
import itc475.dndencountertool.service.EncounterService;
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
    private final CampaignRepository campaignRepository;
    private final EncounterService encounterService;

    public EncounterApiController(EncounterRepository encounterRepository,
                                  CombatantRepository combatantRepository,
                                  CampaignRepository campaignRepository,
                                  EncounterService encounterService) {
        this.encounterRepository = encounterRepository;
        this.combatantRepository = combatantRepository;
        this.campaignRepository = campaignRepository;
        this.encounterService = encounterService;
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

    @PostMapping("/campaigns/{campaignId}/encounters")
    public EncounterResponse createEncounter(@PathVariable Long campaignId,
                                             @AuthenticationPrincipal User user,
                                             @RequestBody EncounterCreateRequest request) {
        Campaign campaign = getOwnedCampaignOrThrow(campaignId, user);
        Encounter encounter = encounterService.createEncounter(request.name(), campaign);

        return new EncounterResponse(encounter.getId(), encounter.getName(), encounter.isComplete());
    }

    @DeleteMapping("/encounters/{id}")
    public ResponseEntity<Void> deleteEncounter(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Encounter encounter = getOwnedEncounterOrThrow(id, user);
        encounterRepository.delete(encounter);
        return ResponseEntity.noContent().build();
    }

    record EncounterCreateRequest(String name) {}
    record EncounterResponse(Long id, String name, boolean complete) {}

    private Campaign getOwnedCampaignOrThrow(Long campaignId, User user) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!campaign.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return campaign;
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