package itc475.dndencountertool.controller;

import itc475.dndencountertool.domain.Combatant;
import itc475.dndencountertool.domain.Status;
import itc475.dndencountertool.domain.User;
import itc475.dndencountertool.mapper.CombatantRepository;
import itc475.dndencountertool.mapper.StatusRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class StatusApiController {

    private final StatusRepository statusRepository;
    private final CombatantRepository combatantRepository;

    public StatusApiController(StatusRepository statusRepository, CombatantRepository combatantRepository) {
        this.statusRepository = statusRepository;
        this.combatantRepository = combatantRepository;
    }

    @PostMapping("/combatants/{combatantId}/statuses")
    public StatusResponse addStatus(@PathVariable Long combatantId,
                                    @AuthenticationPrincipal User user,
                                    @RequestBody StatusRequest request) {
        Combatant combatant = getOwnedCombatantOrThrow(combatantId, user);

        Status status = new Status(request.status(), combatant);
        statusRepository.save(status);

        return new StatusResponse(status.getId(), status.getStatus());
    }

    @DeleteMapping("/statuses/{statusId}")
    public ResponseEntity<Void> deleteStatus(@PathVariable Long statusId, @AuthenticationPrincipal User user) {
        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!status.getCombatant().getEncounter().getCampaign().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        statusRepository.delete(status);
        return ResponseEntity.noContent().build();
    }

    record StatusRequest(String status) {}
    record StatusResponse(Long id, String status) {}

    private Combatant getOwnedCombatantOrThrow(Long combatantId, User user) {
        Combatant combatant = combatantRepository.findById(combatantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!combatant.getEncounter().getCampaign().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return combatant;
    }
}