package itc475.dndencountertool.controller;

import itc475.dndencountertool.domain.Campaign;
import itc475.dndencountertool.domain.MonsterTemplate;
import itc475.dndencountertool.domain.User;
import itc475.dndencountertool.mapper.CampaignRepository;
import itc475.dndencountertool.mapper.MonsterTemplateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class MonsterTemplateApiController {

    private final MonsterTemplateRepository monsterTemplateRepository;
    private final CampaignRepository campaignRepository;

    public MonsterTemplateApiController(MonsterTemplateRepository monsterTemplateRepository,
                                        CampaignRepository campaignRepository) {
        this.monsterTemplateRepository = monsterTemplateRepository;
        this.campaignRepository = campaignRepository;
    }

    @PostMapping("/campaigns/{campaignId}/monster-templates")
    public TemplateResponse createTemplate(@PathVariable Long campaignId,
                                           @AuthenticationPrincipal User user,
                                           @RequestBody TemplateRequest request) {
        Campaign campaign = getOwnedCampaignOrThrow(campaignId, user);

        MonsterTemplate template = new MonsterTemplate(
                request.name(), request.hp(), request.maxHp(), request.ac(), campaign);
        monsterTemplateRepository.save(template);

        return toResponse(template);
    }

    @DeleteMapping("/monster-templates/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id, @AuthenticationPrincipal User user) {
        MonsterTemplate template = monsterTemplateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!template.getCampaign().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        monsterTemplateRepository.delete(template);
        return ResponseEntity.noContent().build();
    }

    private TemplateResponse toResponse(MonsterTemplate t) {
        return new TemplateResponse(t.getId(), t.getName(), t.getHp(), t.getMaxHp(), t.getAc());
    }

    record TemplateRequest(String name, Integer hp, Integer maxHp, Integer ac) {}
    record TemplateResponse(Long id, String name, Integer hp, Integer maxHp, Integer ac) {}

    private Campaign getOwnedCampaignOrThrow(Long campaignId, User user) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!campaign.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return campaign;
    }
}