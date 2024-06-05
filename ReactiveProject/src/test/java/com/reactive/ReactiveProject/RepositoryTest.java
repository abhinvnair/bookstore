package com.reactive.ReactiveProject;

import com.reactive.ReactiveProject.entities.Book;
import com.reactive.ReactiveProject.repositories.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class RepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    public void findMethodTest()
    {
        Flux<Book> nameMono = bookRepository.findByAuthor("Narender");
        StepVerifier.create(nameMono)
                .expectNextCount(1)
                .verifyComplete();
    }
    @Test
    public void queryMethodsTest()
    {
        bookRepository.getAllBooksByAuthor("Advanced spring stuff","Narender")
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }


}
