package com.reactive.ReactiveProject.controller;
import com.reactive.ReactiveProject.entities.Book;
import com.reactive.ReactiveProject.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/books")
public class   BookController {

    @Autowired
    private BookService bookService;

    //    create
    @PostMapping
    public Mono<Book> create(@RequestBody Book book) {
        return bookService.create(book);
    }

    //    get all books
    @GetMapping
    public Flux<Book> getAll() {
        return bookService.getAll();
    }

    //    get single book
    @GetMapping("/{bid}")
    public Mono<Book> get(@PathVariable("bid") int bookId) {
        return bookService.get(bookId);
    }

    //    update
    @PutMapping("/{bookId}")
    public Mono<Book> update(@RequestBody Book book, @PathVariable int bookId) {
        return bookService.update(book, bookId);
    }

    //    delete
    @DeleteMapping("/{bookId}")
    public Mono<Void> delete(@PathVariable int bookId) {
        return bookService.delete(bookId);
    }

    @GetMapping("/author/{author}")
    public Flux<Book> findByAuthor(@PathVariable String author) {
        return bookService.findByAuthor(author);
    }
    @GetMapping("/search")
    public Flux<Book> findByNameAndAuthor(@RequestParam String name, @RequestParam String author) {
        return bookService.findByNameAndAuthor(name, author);
    }
    @PatchMapping("/{bookId}")
    public Mono<ResponseEntity<Book>> partialUpdate(@RequestBody Book book, @PathVariable int bookId) {
        return bookService.partialUpdate(book, bookId)
                .map(updatedBook -> ResponseEntity.ok(updatedBook))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


}