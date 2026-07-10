package itc475.dndencountertool.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "statuses")
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 25)
    private String status;

    @ManyToOne
    @JoinColumn(name = "combatant_id")
    private Combatant combatant;

    public Status() {
    }

    public Status(String status, Combatant combatant) {
        this.status = status;
        this.combatant = combatant;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Combatant getCombatant() {
        return combatant;
    }

    public void setCombatant(Combatant combatant) {
        this.combatant = combatant;
    }
}