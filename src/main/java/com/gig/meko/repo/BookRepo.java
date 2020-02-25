package com.gig.meko.repo;

import com.gig.meko.entity.Book;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.List;

/**
 * @author spp
 */
public interface BookRepo extends JpaRepositoryImplementation<Book, Integer> {
    List<Book> findByAuthor_Id(Integer authorId);

    List<Book> findByAuthor_FirstName(String firstName);

    List<Book> findByNameLikeAndAuthor_Id(String name, Integer authorId);

    List<Book> findByNameLike(String name);
}
