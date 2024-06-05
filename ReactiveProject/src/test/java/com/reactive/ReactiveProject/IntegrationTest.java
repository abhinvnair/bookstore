package com.reactive.ReactiveProject;

import com.reactive.ReactiveProject.entities.Book;
import com.reactive.ReactiveProject.repositories.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class IntegrationTest {

    static {
        System.setProperty("testcontainers.ryuk.container.image", "testcontainers/ryuk:0.3.3");
    }

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("boot_work1")
            .withUsername("postgres")
            .withPassword("mysecretpassword")
            .withReuse(true);

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll().block();
    }

    @Test
    @DisplayName("CreateBook")
    void createBookTest() {
        Book book = new Book(17 ,"Test Book", "Test Description", "Test Publisher", "Test Author");

        webTestClient.post().uri("/books")
                .body(Mono.just(book), Book.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Book.class)
                .value(createdBook -> {
                    assert createdBook.getBookId() != 0;
                    assert createdBook.getName().equals("Test Book");
                });
    }

    @Test
    @DisplayName("GetAllBooks")
    void getAllBooksTest() {
        Book book1 = new Book(1, "Book 1", "Description 1", "Publisher 1", "Author 1");
        Book book2 = new Book(2, "Book 2", "Description 2", "Publisher 2", "Author 2");

        bookRepository.save(book1).block();
        bookRepository.save(book2).block();

        webTestClient.get().uri("/books")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Book.class)
                .hasSize(2)
                .value(books -> {
                    assert books.stream().anyMatch(book -> book.getName().equals("Book 1"));
                    assert books.stream().anyMatch(book -> book.getName().equals("Book 2"));
                });
    }

    @Test
    @DisplayName("GetBookById")
    void getBookByIdTest() {
        Book book = new Book(1, "Test Book", "Test Description", "Test Publisher", "Test Author");

        book = bookRepository.save(book).block();

        final Book savedBook = book;

        webTestClient.get().uri("/books/{id}", savedBook.getBookId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Book.class)
                .value(fetchedBook -> {
                    assert fetchedBook.getBookId() == savedBook.getBookId();
                    assert fetchedBook.getName().equals("Test Book");
                });
    }

    @Test
    @DisplayName("UpdateBook")
    void updateBookTest() {
        Book book = new Book(7, "Spring Masterclass stuff part 7", "this is spring boot stuff for advanced", "BBB Publisher", "Ratin Sankpal");

        book = bookRepository.save(book).block();

        final Book savedBook = book;

        Book updatedBook = new Book(savedBook.getBookId(), "Updated Book", "Updated Description", "Updated Publisher", "Updated Author");

        webTestClient.put().uri("/books/{id}", savedBook.getBookId())
                .body(Mono.just(updatedBook), Book.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Book.class)
                .value(fetchedBook -> {
                    assert fetchedBook.getBookId() == savedBook.getBookId();
                    assert fetchedBook.getName().equals("Updated Book");
                });
    }
}

