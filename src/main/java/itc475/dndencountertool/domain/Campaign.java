package itc475.dndencountertool.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

// Mapping SQL table columns to class attributes
@Entity
@Table(name = "campaigns")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private boolean complete = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    // Ensures that encounters and party members
    // do not incidentally end up being null
    @OneToMany(mappedBy = "campaign")
    private List<Encounter> encounters = new ArrayList<>();

    @OneToMany(mappedBy = "campaign")
    private List<PartyMember> partyMembers = new ArrayList<>();

    public Campaign() {
    }

    public Campaign(String name, User user) {
        this.name = name;
        this.user = user;
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

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Encounter> getEncounters() {
        return encounters;
    }

    public void setEncounters(List<Encounter> encounters) {
        this.encounters = encounters;
    }

    public List<PartyMember> getPartyMembers() {
        return partyMembers;
    }

    public void setPartyMembers(List<PartyMember> partyMembers) {
        this.partyMembers = partyMembers;
    }
}