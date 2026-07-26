package itc475.dndencountertool.controller;

import itc475.dndencountertool.domain.Campaign;
import itc475.dndencountertool.domain.User;
import itc475.dndencountertool.mapper.CampaignRepository;
import itc475.dndencountertool.mapper.PartyMemberRepository;
import itc475.dndencountertool.mapper.EncounterRepository;
import itc475.dndencountertool.mapper.MonsterTemplateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class CampaignController {

    private final CampaignRepository campaignRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final EncounterRepository encounterRepository;
    private final MonsterTemplateRepository monsterTemplateRepository;

    public CampaignController(CampaignRepository campaignRepository,
                              PartyMemberRepository partyMemberRepository,
                              EncounterRepository encounterRepository,
                              MonsterTemplateRepository monsterTemplateRepository) {
        this.campaignRepository = campaignRepository;
        this.partyMemberRepository = partyMemberRepository;
        this.encounterRepository = encounterRepository;
        this.monsterTemplateRepository = monsterTemplateRepository;
    }

    @GetMapping("/campaigns")
    public String listCampaigns(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("campaigns", campaignRepository.findByUserIdOrderByCompleteAscNameAsc(user.getId()));
        return "campaigns";
    }

    @GetMapping("/campaigns/new")
    public String newCampaignForm(Model model) {
        model.addAttribute("campaign", new Campaign());
        return "createCampaign";
    }

    @PostMapping("/campaigns")
    public String createCampaign(@AuthenticationPrincipal User user, @ModelAttribute Campaign campaign) {
        campaign.setUser(user);
        campaignRepository.save(campaign);
        return "redirect:/campaigns";
    }

    @GetMapping("/campaigns/{id}")
    public String viewCampaign(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!campaign.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        model.addAttribute("campaign", campaign);
        model.addAttribute("partyMembers", partyMemberRepository.findByCampaignId(id));
        model.addAttribute("encounters", encounterRepository.findByCampaignIdOrderByCompleteAscNameAsc(id));
        model.addAttribute("monsterTemplates", monsterTemplateRepository.findByCampaignIdOrderByNameAsc(id));

        return "campaign-details";
    }

    private Campaign getOwnedCampaignOrThrow(Long campaignId, User user) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!campaign.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return campaign;
    }

    @PostMapping("/campaigns/{id}/toggle-complete")
    public String toggleCampaignComplete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Campaign campaign = getOwnedCampaignOrThrow(id, user);
        campaign.setComplete(!campaign.isComplete());
        campaignRepository.save(campaign);
        return "redirect:/campaigns/" + id;
    }
}
