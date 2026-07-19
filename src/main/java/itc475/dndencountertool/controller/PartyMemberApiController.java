package itc475.dndencountertool.controller;

import itc475.dndencountertool.domain.Campaign;
import itc475.dndencountertool.domain.PartyMember;
import itc475.dndencountertool.domain.User;
import itc475.dndencountertool.mapper.CampaignRepository;
import itc475.dndencountertool.mapper.PartyMemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class PartyMemberApiController {

    private final PartyMemberRepository partyMemberRepository;
    private final CampaignRepository campaignRepository;

    public PartyMemberApiController(PartyMemberRepository partyMemberRepository,
                                    CampaignRepository campaignRepository) {
        this.partyMemberRepository = partyMemberRepository;
        this.campaignRepository = campaignRepository;
    }

    @PostMapping("/campaigns/{campaignId}/party-members")
    public PartyMemberResponse createPartyMember(@PathVariable Long campaignId,
                                                 @AuthenticationPrincipal User user,
                                                 @RequestBody PartyMemberRequest request) {
        Campaign campaign = getOwnedCampaignOrThrow(campaignId, user);

        PartyMember member = new PartyMember(request.name(), campaign);
        partyMemberRepository.save(member);

        return new PartyMemberResponse(member.getId(), member.getName(), member.isActive());
    }

    @DeleteMapping("/party-members/{id}")
    public ResponseEntity<Void> deletePartyMember(@PathVariable Long id, @AuthenticationPrincipal User user) {
        PartyMember member = partyMemberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!member.getCampaign().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        partyMemberRepository.delete(member);
        return ResponseEntity.noContent().build();
    }

    record PartyMemberRequest(String name) {}
    record PartyMemberResponse(Long id, String name, boolean active) {}

    private Campaign getOwnedCampaignOrThrow(Long campaignId, User user) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!campaign.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return campaign;
    }
}