package itc475.dndencountertool.controller;

import itc475.dndencountertool.domain.Combatant;
import itc475.dndencountertool.domain.User;
import itc475.dndencountertool.mapper.CombatantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class CombatantApiController {

    private final CombatantRepository combatantRepository;

    public CombatantApiController(CombatantRepository combatantRepository) {
        this.combatantRepository = combatantRepository;
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
        }
        combatant.setIncapacitated(request.incapacitated());

        combatantRepository.save(combatant);

        return new CombatantResponse(
                combatant.getId(),
                combatant.getInitiative(),
                combatant.getHp(),
                combatant.getMaxHp(),
                combatant.isIncapacitated()
        );
    }

    record CombatantUpdateRequest(Integer initiative, Integer hp, Integer maxHp, boolean incapacitated) {}
    record CombatantResponse(Long id, Integer initiative, Integer hp, Integer maxHp, boolean incapacitated) {}

    private Combatant getOwnedCombatantOrThrow(Long combatantId, User user) {
        Combatant combatant = combatantRepository.findById(combatantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!combatant.getEncounter().getCampaign().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return combatant;
    }
}