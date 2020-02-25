package com.gig.meko.repo;

import com.gig.meko.entity.Author;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

/**
 * @author spp
 */
public interface AuthorRepo extends JpaRepositoryImplementation<Author, Integer> {
}
