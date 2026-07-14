package itc475.dndencountertool.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

// Mapping SQL table columns to class attributes
@Entity
@Table(name = "combatants")
public class Combatant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String name;

    private Integer initiative;

    private Integer hp;

    @Column(name = "max_hp")
    private Integer maxHp;

    @Column(nullable = false)
    private boolean incapacitated = false;

    private boolean player;

    @ManyToOne
    @JoinColumn(name = "encounter_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Encounter encounter;

    @OneToMany(mappedBy = "combatant")
    private List<Status> statuses = new ArrayList<>();

    public Combatant() {
    }

    public Combatant(String name, boolean player, Encounter encounter) {
        this.name = name;
        this.player = player;
        this.encounter = encounter;
    }

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

    public Integer getInitiative() {
        return initiative;
    }

    public void setInitiative(Integer initiative) {
        this.initiative = initiative;
    }

    public Integer getHp() {
        return hp;
    }

    public void setHp(Integer hp) {
        this.hp = hp;
    }

    public Integer getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(Integer maxHp) {
        this.maxHp = maxHp;
    }

    public boolean isIncapacitated() {
        return incapacitated;
    }

    public void setIncapacitated(boolean incapacitated) {
        this.incapacitated = incapacitated;
    }

    public boolean isPlayer() {
        return player;
    }

    public void setPlayer(boolean player) {
        this.player = player;
    }

    public Encounter getEncounter() {
        return encounter;
    }

    public void setEncounter(Encounter encounter) {
        this.encounter = encounter;
    }

    public List<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<Status> statuses) {
        this.statuses = statuses;
    }
}