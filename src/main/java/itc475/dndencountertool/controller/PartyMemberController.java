package itc475.dndencountertool.controller;

import itc475.dndencountertool.domain.Campaign;
import itc475.dndencountertool.domain.PartyMember;
import itc475.dndencountertool.domain.User;
import itc475.dndencountertool.mapper.CampaignRepository;
import itc475.dndencountertool.mapper.PartyMemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class PartyMemberController {

    private final PartyMemberRepository partyMemberRepository;
    private final CampaignRepository campaignRepository;

    public PartyMemberController(PartyMemberRepository partyMemberRepository,
                                 CampaignRepository campaignRepository) {
        this.partyMemberRepository = partyMemberRepository;
        this.campaignRepository = campaignRepository;
    }

    @GetMapping("/campaigns/{campaignId}/party-members/new")
    public String newPartyMemberForm(@PathVariable Long campaignId,
                                     @AuthenticationPrincipal User user,
                                     Model model) {
        Campaign campaign = getOwnedCampaignOrThrow(campaignId, user);

        model.addAttribute("campaign", campaign);
        model.addAttribute("partyMember", new PartyMember());
        return "new-party-member";
    }

    @PostMapping("/campaigns/{campaignId}/party-members")
    public String createPartyMember(@PathVariable Long campaignId,
                                    @AuthenticationPrincipal User user,
                                    @ModelAttribute PartyMember partyMember) {
        Campaign campaign = getOwnedCampaignOrThrow(campaignId, user);

        partyMember.setCampaign(campaign);
        partyMemberRepository.save(partyMember);
        return "redirect:/campaigns/" + campaignId;
    }

    private Campaign getOwnedCampaignOrThrow(Long campaignId, User user) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!campaign.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return campaign;
    }

    @PostMapping("/party-members/{id}/toggle-active")
    public String togglePartyMemberActive(@PathVariable Long id, @AuthenticationPrincipal User user) {
        PartyMember member = partyMemberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!member.getCampaign().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        member.setActive(!member.isActive());
        partyMemberRepository.save(member);
        return "redirect:/campaigns/" + member.getCampaign().getId();
    }
}