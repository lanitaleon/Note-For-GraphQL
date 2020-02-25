package com.gig.meko.entity;

import javax.persistence.*;

/**
 * @author spp
 */
@Entity
public class Author {
    enum AgeType {
        /**
         * young
         */
        YOUNG,
        /**
         * old
         */
        OLD,
        /**
         * infant
         */
        INFANT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String firstName;
    private String lastName;
    @Enumerated
    private AgeType age;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public AgeType getAge() {
        return age;
    }

    public void setAge(AgeType age) {
        this.age = age;
    }
}
