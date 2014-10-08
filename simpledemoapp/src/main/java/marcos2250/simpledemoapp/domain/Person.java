package marcos2250.simpledemoapp.domain;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.joda.time.LocalDate;

@Entity
@Table(name = "PER_PERSON")
public class Person {

    private Long id;

    private String firstname;

    private String lastname;

    private LocalDate birthDate;

    private String cellphone;

    private List<Project> projects;

    @Id
    @GeneratedValue
    @Column(name = "PER_ID")
    public Long getId() {
        return id;
    }

    @Column(name = "PER_ST_FIRST_NAME")
    public String getFirstname() {
        return firstname;
    }

    @Column(name = "PER_ST_LAST_NAME")
    public String getLastname() {
        return lastname;
    }

    @Column(name = "PER_DT_BIRTH")
    public LocalDate getBirthDate() {
        return birthDate;
    }

    @Column(name = "PER_NM_CELLPHONE")
    public String getCellphone() {
        return cellphone;
    }

    @ManyToMany(mappedBy = "participants")
    public List<Project> getProjects() {
        return projects;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

}
