package itc475.dndencountertool.controller;

import itc475.dndencountertool.domain.*;
import itc475.dndencountertool.mapper.CampaignRepository;
import itc475.dndencountertool.mapper.CombatantRepository;
import itc475.dndencountertool.mapper.EncounterRepository;
import itc475.dndencountertool.mapper.StatusRepository;
import itc475.dndencountertool.mapper.MonsterTemplateRepository;
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
    private final StatusRepository statusRepository;
    private final MonsterTemplateRepository monsterTemplateRepository;
    private final EncounterService encounterService;

    public EncounterController(EncounterRepository encounterRepository,
                               CampaignRepository campaignRepository,
                               CombatantRepository combatantRepository,
                               StatusRepository statusRepository,
                               MonsterTemplateRepository monsterTemplateRepository,
                               EncounterService encounterService) {
        this.encounterRepository = encounterRepository;
        this.campaignRepository = campaignRepository;
        this.combatantRepository = combatantRepository;
        this.statusRepository = statusRepository;
        this.monsterTemplateRepository = monsterTemplateRepository;
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
        model.addAttribute("combatants", combatantRepository.findByEncounterIdOrderByInitiativeDesc(id));
        model.addAttribute("monsterTemplates", monsterTemplateRepository.findByCampaignIdOrderByNameAsc(encounter.getCampaign().getId()));
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

    @GetMapping("/encounters/{encounterId}/combatants/new")
    public String newCombatantForm(@PathVariable Long encounterId,
                                   @AuthenticationPrincipal User user,
                                   Model model) {
        Encounter encounter = getOwnedEncounterOrThrow(encounterId, user);
        model.addAttribute("encounter", encounter);
        return "new-combatant";
    }

    @PostMapping("/encounters/{encounterId}/combatants")
    public String createCombatant(@PathVariable Long encounterId,
                                  @AuthenticationPrincipal User user,
                                  @RequestParam String name) {
        Encounter encounter = getOwnedEncounterOrThrow(encounterId, user);

        Combatant combatant = new Combatant(name, false, encounter);
        combatantRepository.save(combatant);

        return "redirect:/encounters/" + encounterId;
    }

    private Encounter getOwnedEncounterOrThrow(Long encounterId, User user) {
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!encounter.getCampaign().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return encounter;
    }

    @PostMapping("/encounters/{id}/toggle-complete")
    public String toggleEncounterComplete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Encounter encounter = getOwnedEncounterOrThrow(id, user);
        encounter.setComplete(!encounter.isComplete());
        encounterRepository.save(encounter);
        return "redirect:/encounters/" + id;
    }

    @PostMapping("/encounters/{encounterId}/combatants/{combatantId}")
    public String updateCombatant(@PathVariable Long encounterId,
                                  @PathVariable Long combatantId,
                                  @AuthenticationPrincipal User user,
                                  @RequestParam(required = false) Integer initiative,
                                  @RequestParam(required = false) Integer hp,
                                  @RequestParam(required = false) Integer maxHp,
                                  @RequestParam(required = false) boolean incapacitated) {
        Encounter encounter = getOwnedEncounterOrThrow(encounterId, user);

        Combatant combatant = combatantRepository.findById(combatantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!combatant.getEncounter().getId().equals(encounter.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        combatant.setInitiative(initiative);
        if (!combatant.isPlayer()) {
            combatant.setHp(hp);
            combatant.setMaxHp(maxHp);
        }
        combatant.setIncapacitated(incapacitated);

        combatantRepository.save(combatant);

        return "redirect:/encounters/" + encounterId;
    }

    @PostMapping("/combatants/{combatantId}/statuses")
    public String addStatus(@PathVariable Long combatantId,
                            @AuthenticationPrincipal User user,
                            @RequestParam String status) {
        Combatant combatant = getOwnedCombatantOrThrow(combatantId, user);

        Status newStatus = new Status(status, combatant);
        statusRepository.save(newStatus);

        return "redirect:/encounters/" + combatant.getEncounter().getId();
    }

    @PostMapping("/statuses/{statusId}/delete")
    public String deleteStatus(@PathVariable Long statusId, @AuthenticationPrincipal User user) {
        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Combatant combatant = status.getCombatant();
        if (!combatant.getEncounter().getCampaign().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Long encounterId = combatant.getEncounter().getId();
        statusRepository.delete(status);

        return "redirect:/encounters/" + encounterId;
    }

    private Combatant getOwnedCombatantOrThrow(Long combatantId, User user) {
        Combatant combatant = combatantRepository.findById(combatantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!combatant.getEncounter().getCampaign().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return combatant;
    }
}
