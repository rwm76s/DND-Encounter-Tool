package itc475.dndencountertool.controller;

import itc475.dndencountertool.domain.PartyMember;
import itc475.dndencountertool.domain.User;
import itc475.dndencountertool.mapper.PartyMemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class PartyMemberController {

    private final PartyMemberRepository partyMemberRepository;

    public PartyMemberController(PartyMemberRepository partyMemberRepository) {
        this.partyMemberRepository = partyMemberRepository;
    }

    // Convert this to RESTful endpoint so that this controller can be deleted
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