package itc475.dndencountertool.controller;

import itc475.dndencountertool.domain.Campaign;
import itc475.dndencountertool.domain.Encounter;
import itc475.dndencountertool.domain.User;
import itc475.dndencountertool.mapper.CampaignRepository;
import itc475.dndencountertool.mapper.CombatantRepository;
import itc475.dndencountertool.mapper.EncounterRepository;
import itc475.dndencountertool.service.EncounterService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class EncounterController {

    private final EncounterRepository encounterRepository;
    private final CampaignRepository campaignRepository;
    private final CombatantRepository combatantRepository;
    private final EncounterService encounterService;

    public EncounterController(EncounterRepository encounterRepository,
                               CampaignRepository campaignRepository,
                               CombatantRepository combatantRepository,
                               EncounterService encounterService) {
        this.encounterRepository = encounterRepository;
        this.campaignRepository = campaignRepository;
        this.combatantRepository = combatantRepository;
        this.encounterService = encounterService;
    }

    @GetMapping("/campaigns/{campaignId}/encounters/new")
    public String newEncounterForm(@PathVariable Long campaignId,
                                   @AuthenticationPrincipal User user,
                                   Model model) {
        Campaign campaign = getOwnedCampaignOrThrow(campaignId, user);
        model.addAttribute("campaign", campaign);
        return "new-encounter";
    }

    @PostMapping("/campaigns/{campaignId}/encounters")
    public String createEncounter(@PathVariable Long campaignId,
                                  @AuthenticationPrincipal User user,
                                  @RequestParam String name) {
        Campaign campaign = getOwnedCampaignOrThrow(campaignId, user);
        Encounter encounter = encounterService.createEncounter(name, campaign);
        return "redirect:/encounters/" + encounter.getId();
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
        model.addAttribute("combatants", combatantRepository.findByEncounterId(id));
        return "encounter-details";
    }

    private Campaign getOwnedCampaignOrThrow(Long campaignId, User user) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!campaign.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return campaign;
    }
}
