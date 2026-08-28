package itc475.dndencountertool.controller;

import itc475.dndencountertool.domain.*;
import itc475.dndencountertool.mapper.CombatantRepository;
import itc475.dndencountertool.mapper.EncounterRepository;
import itc475.dndencountertool.mapper.MonsterTemplateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class EncounterController {

    private final EncounterRepository encounterRepository;
    private final CombatantRepository combatantRepository;
    private final MonsterTemplateRepository monsterTemplateRepository;

    public EncounterController(EncounterRepository encounterRepository,
                               CombatantRepository combatantRepository,
                               MonsterTemplateRepository monsterTemplateRepository) {
        this.encounterRepository = encounterRepository;
        this.combatantRepository = combatantRepository;
        this.monsterTemplateRepository = monsterTemplateRepository;
    }

    @GetMapping("/encounters/{id}")
    public String viewEncounter(@PathVariable Long id,
                                @AuthenticationPrincipal User user,
                                Model model) {
        Encounter encounter = encounterRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!encounter.getCampaign().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        model.addAttribute("encounter", encounter);
        model.addAttribute("combatants", combatantRepository.findByEncounterIdOrderByInitiativeDesc(id));
        model.addAttribute("monsterTemplates", monsterTemplateRepository.findByCampaignIdOrderByNameAsc(encounter.getCampaign().getId()));
        return "encounter-details";
    }

    private Encounter getOwnedEncounterOrThrow(Long encounterId, User user) {
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!encounter.getCampaign().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return encounter;
    }

    // Convert this to a RESTful endpoint later
    @PostMapping("/encounters/{id}/toggle-complete")
    public String toggleEncounterComplete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Encounter encounter = getOwnedEncounterOrThrow(id, user);
        encounter.setComplete(!encounter.isComplete());
        encounterRepository.save(encounter);
        return "redirect:/encounters/" + id;
    }
}
