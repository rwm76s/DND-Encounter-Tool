package itc475.dndencountertool.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

// Mapping SQL table columns to class attributes
@Entity
@Table(name = "monster_templates")
public class MonsterTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    private Integer hp;

    @Column(name = "max_hp")
    private Integer maxHp;

    private Integer ac;

    // Acts as a foreign key
    @ManyToOne
    @JoinColumn(name = "campaign_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Campaign campaign;

    public MonsterTemplate() {
    }

    public MonsterTemplate(String name, Integer hp, Integer maxHp, Integer ac, Campaign campaign) {
        this.name = name;
        this.hp = hp;
        this.maxHp = maxHp;
        this.ac = ac;
        this.campaign = campaign;
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
    public Integer getAc() {
        return ac;
    }
    public void setAc(Integer ac) {
        this.ac = ac;
    }
    public Campaign getCampaign() {
        return campaign;
    }
    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }
}