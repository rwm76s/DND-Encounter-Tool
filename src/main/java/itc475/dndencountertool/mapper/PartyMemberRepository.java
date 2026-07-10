package itc475.dndencountertool.mapper;

import itc475.dndencountertool.domain.PartyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Field mapping done within class definition
public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {
    List<PartyMember> findByCampaignId(Long campaignId);
    List<PartyMember> findByCampaignIdAndActiveTrue(Long campaignId);
}
