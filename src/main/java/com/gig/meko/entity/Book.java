package com.gig.meko.entity;

import javax.persistence.*;

/**
 * @author spp
 */
@Entity
public class Book {
    public Book() {
    }

    public Book(String name, Integer pageCount, Author author) {
        this.name = name;
        this.pageCount = pageCount;
        this.author = author;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private Integer pageCount;
    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }
}
