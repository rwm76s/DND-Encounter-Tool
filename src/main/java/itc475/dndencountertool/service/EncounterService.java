package itc475.dndencountertool.service;

import itc475.dndencountertool.domain.Campaign;
import itc475.dndencountertool.domain.Combatant;
import itc475.dndencountertool.domain.Encounter;
import itc475.dndencountertool.domain.PartyMember;
import itc475.dndencountertool.mapper.CombatantRepository;
import itc475.dndencountertool.mapper.EncounterRepository;
import itc475.dndencountertool.mapper.PartyMemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EncounterService {

    private final EncounterRepository encounterRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final CombatantRepository combatantRepository;

    public EncounterService(EncounterRepository encounterRepository,
                            PartyMemberRepository partyMemberRepository,
                            CombatantRepository combatantRepository) {
        this.encounterRepository = encounterRepository;
        this.partyMemberRepository = partyMemberRepository;
        this.combatantRepository = combatantRepository;
    }

    public Encounter createEncounter(String name, Campaign campaign) {
        Encounter encounter = new Encounter(name, campaign);
        encounterRepository.save(encounter);

        List<PartyMember> activeMembers =
                partyMemberRepository.findByCampaignIdAndActiveTrue(campaign.getId());

        for (PartyMember member : activeMembers) {
            Combatant combatant = new Combatant(member.getName(), true, encounter);
            combatantRepository.save(combatant);
        }

        return encounter;
    }
}
