package marcos2250.simpledemoapp.domain;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.joda.time.LocalDate;

@Entity
@Table(name = "PRJ_PROJECT")
public class Project {

    private Long id;

    private String name;
    private ProjectStatus status;

    private LocalDate beginning;
    private LocalDate ending;

    private List<Person> participants;

    @Id
    @GeneratedValue
    @Column(name = "PRJ_ID")
    public Long getId() {
        return id;
    }

    @Column(name = "PRJ_ST_NAME")
    public String getName() {
        return name;
    }

    @Column(name = "PRJ_DT_BEGIN")
    public LocalDate getBeginning() {
        return beginning;
    }

    @Column(name = "PRJ_DT_END")
    public LocalDate getEnding() {
        return ending;
    }

    @ManyToMany
    @JoinTable(name = "PAR_PARTICIPANTS", //
    joinColumns = @JoinColumn(name = "PRJ_ID"), //
    inverseJoinColumns = @JoinColumn(name = "PER_ID"))
    public List<Person> getParticipants() {
        return participants;
    }

    @Column(name = "PRJ_ST_STATUS")
    @Enumerated(EnumType.ORDINAL)
    public ProjectStatus getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public void setBeginning(LocalDate beginning) {
        this.beginning = beginning;
    }

    public void setEnding(LocalDate ending) {
        this.ending = ending;
    }

    public void setParticipants(List<Person> participants) {
        this.participants = participants;
    }

}
