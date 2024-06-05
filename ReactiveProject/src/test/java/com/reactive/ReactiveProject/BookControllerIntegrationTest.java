package com.reactive.ReactiveProject;

import com.reactive.ReactiveProject.entities.Book;
import com.reactive.ReactiveProject.repositories.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class BookControllerIntegrationTest {

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

    @MockBean
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        when(bookRepository.deleteAll()).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Create book")
    void  createBookTest() {
        Book book = new Book(1, "Test Book", "Test Description", "Test Publisher", "Test Author");

        when(bookRepository.save(any(Book.class))).thenReturn(Mono.just(book));

        webTestClient.post().uri("/books")
                .body(Mono.just(book), Book.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Book.class)
                .value(createdBook -> {
                    assert createdBook.getName().equals("Test Book");
                });
    }

    @Test
    @DisplayName("Get All Books")
    void getAllBooksTest() {
        Book book1 = new Book(1, "Book 1", "Description 1", "Publisher 1", "Author 1");
        Book book2 = new Book(2, "Book 2", "Description 2", "Publisher 2", "Author 2");

        when(bookRepository.findAll()).thenReturn(Flux.just(book1, book2));

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
    @DisplayName("Get book by ID")
    void getBookByIdTest() {
        Book book = new Book(1, "Test Book", "Test Description", "Test Publisher", "Test Author");

        when(bookRepository.findById(1)).thenReturn(Mono.just(book));

        webTestClient.get().uri("/books/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Book.class)
                .value(fetchedBook -> {
                    assert fetchedBook.getBookId() == 1;
                    assert fetchedBook.getName().equals("Test Book");
                });
    }

    @Test
    @DisplayName("Update Book")
    void updateBookTest() {
        Book book = new Book(1, "Test Book", "Test Description", "Test Publisher", "Test Author");

        when(bookRepository.findById(1)).thenReturn(Mono.just(book));
        when(bookRepository.save(any(Book.class))).thenReturn(Mono.just(book));

        Book updatedBook = new Book(1, "Updated Book", "Updated Description", "Updated Publisher", "Updated Author");

        webTestClient.put().uri("/books/{id}", 1)
                .body(Mono.just(updatedBook), Book.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Book.class)
                .value(fetchedBook -> {
                    assert fetchedBook.getBookId() == 1;
                    assert fetchedBook.getName().equals("Updated Book");
                });
    }
    @Test
    @DisplayName("Delete Book")
    void deleteBookTest() {
        Book book = new Book(21, "Test Book", "Test Description", "Test Publisher", "Test Author");

        when(bookRepository.findById(21)).thenReturn(Mono.just(book));
        when(bookRepository.delete(any(Book.class))).thenReturn(Mono.empty());

        // Check if the book exists before deleting
        webTestClient.get().uri("/books/{id}", 21)
                .exchange()
                .expectStatus().isOk();

        webTestClient.delete().uri("/books/{id}", 21)
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

    }
    @Test
    @DisplayName("Create Book with Invalid Data")
    void createBookWithInvalidDataTest() {
        Book book = new Book(1, "", "Test Description", "Test Publisher", "Test Author");

        webTestClient.post().uri("/books")
                .body(Mono.just(book), Book.class)
                .exchange()
                .expectStatus().isBadRequest();
    }
    @Test
    @DisplayName("Create multiple books concurrently")
    void createBooksConcurrentlyTest() {
        Book book1 = new Book(1, "Concurrent Book 1", "Description 1", "Publisher 1", "Author 1");
        Book book2 = new Book(2, "Concurrent Book 2", "Description 2", "Publisher 2", "Author 2");

        when(bookRepository.save(any(Book.class))).thenReturn(Mono.just(book1)).thenReturn(Mono.just(book2));

        webTestClient.post().uri("/books")
                .body(Mono.just(book1), Book.class)
                .exchange()
                .expectStatus().isOk();

        webTestClient.post().uri("/books")
                .body(Mono.just(book2), Book.class)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get().uri("/books")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Book.class)
                .hasSize(2)
                .value(books -> {
                    assert books.stream().anyMatch(book -> book.getName().equals("Concurrent Book 1"));
                    assert books.stream().anyMatch(book -> book.getName().equals("Concurrent Book 2"));
                });
    }
    @Test
    @DisplayName("Update book partially")
    void updateBookPartiallyTest() {
        Book book = new Book(1, "Partial Update Book", "Description", "Publisher", "Author");

        when(bookRepository.findById(1)).thenReturn(Mono.just(book));
        when(bookRepository.save(any(Book.class))).thenReturn(Mono.just(book));

        Book partialUpdateBook = new Book();
        partialUpdateBook.setName("Partially Updated Book");

        webTestClient.patch().uri("/books/{id}", 1)
                .body(Mono.just(partialUpdateBook), Book.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Book.class)
                .value(updatedBook -> {
                    assert updatedBook.getBookId() == 1;
                    assert updatedBook.getName().equals("Partially Updated Book");
                    assert updatedBook.getDescription().equals("Description");
                    assert updatedBook.getPublisher().equals("Publisher");
                    assert updatedBook.getAuthor().equals("Author");
                });
    }
    @Test
    @DisplayName("Find Books by Author")
    void findBooksByAuthorTest() {
        Book book1 = new Book(1, "Book 1", "Description 1", "Publisher 1", "Author 1");
        Book book2 = new Book(2, "Book 2", "Description 2", "Publisher 2", "Author 1");

        when(bookRepository.findByAuthor("Author 1")).thenReturn(Flux.just(book1, book2));

        webTestClient.get().uri("/books/author/{author}", "Author 1")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Book.class)
                .hasSize(2)
                .value(books -> {
                    assert books.stream().allMatch(book -> book.getAuthor().equals("Author 1"));
                });
    }
    @Test
    @DisplayName("Find Books by Name and Author")
    void findBooksByNameAndAuthorTest() {
        Book book1 = new Book(1, "Book Name", "Description 1", "Publisher 1", "Author 1");
        Book book2 = new Book(2, "Book Name", "Description 2", "Publisher 2", "Author 1");

        when(bookRepository.findByNameAndAuthor("Book Name", "Author 1")).thenReturn(Flux.just(book1, book2));

        webTestClient.get().uri("/books/search?name={name}&author={author}", "Book Name", "Author 1")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Book.class)
                .hasSize(2)
                .value(books -> {
                    assert books.stream().allMatch(book -> book.getName().equals("Book Name") && book.getAuthor().equals("Author 1"));
                });
    }

}

