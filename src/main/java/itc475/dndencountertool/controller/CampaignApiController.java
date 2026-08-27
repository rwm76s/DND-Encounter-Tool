package itc475.dndencountertool.controller;

import itc475.dndencountertool.domain.Campaign;
import itc475.dndencountertool.domain.User;
import itc475.dndencountertool.mapper.CampaignRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class CampaignApiController {

    private final CampaignRepository campaignRepository;

    public CampaignApiController(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    // Used in campaigns.js to create a campaign
    @PostMapping("/campaigns")
    public CampaignResponse createCampaign(@AuthenticationPrincipal User user, @RequestBody CampaignRequest request) {
        Campaign campaign = new Campaign(request.name(), user);
        campaignRepository.save(campaign);
        return new CampaignResponse(campaign.getId(), campaign.getName(), campaign.isComplete());
    }

    record CampaignRequest(String name) {}
    record CampaignResponse(Long id, String name, boolean complete) {}

    // Used in campaign-details.js to delete a campaign
    @DeleteMapping("/campaigns/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!campaign.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        campaignRepository.delete(campaign);
        return ResponseEntity.noContent().build();
    }
}
