package itc475.dndencountertool.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

// Mapping SQL table columns to class attributes
@Entity
@Table(name = "encounters")
public class Encounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private boolean complete = false;

    @ManyToOne
    @JoinColumn(name = "campaign_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Campaign campaign;

    @ManyToOne
    @JoinColumn(name = "current_turn_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Combatant currentTurn;

    // Acts as a foreign key
    @OneToMany(mappedBy = "encounter")
    private List<Combatant> combatants = new ArrayList<>();

    public Encounter() {
    }

    public Encounter(String name, Campaign campaign) {
        this.name = name;
        this.campaign = campaign;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public List<Combatant> getCombatants() {
        return combatants;
    }

    public void setCombatants(List<Combatant> combatants) {
        this.combatants = combatants;
    }

    public Combatant getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(Combatant currentTurn) {
        this.currentTurn = currentTurn;
    }
}