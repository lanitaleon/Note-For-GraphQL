package com.gig.meko.util;

import com.gig.meko.entity.Author;
import com.gig.meko.entity.Book;
import com.gig.meko.repo.AuthorRepo;
import com.gig.meko.repo.BookRepo;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * resolver
 * @author spp
 */
@Component
public class GraphQLDataFetchers {
    private final static BookPublisher BOOK_PUBLISHER = new BookPublisher();

    @Resource
    private BookRepo bookRepo;
    @Resource
    private AuthorRepo authorRepo;

    public DataFetcher getBookById() {
        return dataFetchingEnvironment -> {
            Integer id = dataFetchingEnvironment.getArgument("id");
            return bookRepo.findById(id);
        };
    }

    public DataFetcher listBookByAuthor() {
        return dataFetchingEnvironment -> {
            Integer authorId = dataFetchingEnvironment.getArgument("authorId");
            return bookRepo.findByAuthor_Id(authorId);
        };
    }

    public DataFetcher listBookByAuthorName() {
        return dataFetchingEnvironment -> {
            String authorName = dataFetchingEnvironment.getArgument("authorName");
            return bookRepo.findByAuthor_FirstName(authorName);
        };
    }

    public DataFetcher listBookByName() {
        return dataFetchingEnvironment -> {
            String name = dataFetchingEnvironment.getArgument("name");
            return bookRepo.findByNameLike("%" + name + "%");
        };
    }

    public DataFetcher listBookByNameByAuthor() {
        return dataFetchingEnvironment -> {
            String name = dataFetchingEnvironment.getArgument("name");
            Integer author = dataFetchingEnvironment.getArgument("authorId");
            return bookRepo.findByNameLikeAndAuthor_Id("%" + name + "%", author);
        };
    }

    public DataFetcher getAuthor() {
        return dataFetchingEnvironment -> {
            Book book = dataFetchingEnvironment.getSource();
            return book.getAuthor();
        };
    }

    public DataFetcher getAuthorById() {
        return dataFetchingEnvironment -> {
            List<Book> bookList = dataFetchingEnvironment.getSource();
            Integer author = dataFetchingEnvironment.getArgument("authorId");
            return bookList.stream().filter(b -> b.getAuthor().getId().equals(author)).collect(Collectors.toList());
        };
    }

    public DataFetcher listBook() {
        return dataFetchingEnvironment -> bookRepo.findAll();
    }

    public DataFetcher addBook() {
        return dataFetchingEnvironment -> {
            Integer authorId = dataFetchingEnvironment.getArgument("authorId");
            Integer pageCount = dataFetchingEnvironment.getArgument("pageCount");
            String name = dataFetchingEnvironment.getArgument("name");
            Author author = authorRepo.findById(authorId).orElse(null);
            Book book = new Book(name, pageCount, author);
            bookRepo.save(book);
            return book;
        };
    }

    public DataFetcher updateBookName() {
        return dataFetchingEnvironment -> {
            Integer bookId = dataFetchingEnvironment.getArgument("id");
            String name = dataFetchingEnvironment.getArgument("name");
            Book book = bookRepo.findById(bookId).orElseThrow(()->new RuntimeException("book not exist"));
            book.setName(name);
            bookRepo.save(book);
            return book;
        };
    }

    public DataFetcher publishBook() {
        return dataFetchingEnvironment ->  {
            Integer bookId = dataFetchingEnvironment.getArgument("id");
            return BOOK_PUBLISHER.getPublisher(bookId);
        };
    }
}
