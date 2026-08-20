package com.librasys.book.controller;

import com.librasys.book.model.Book;
import com.librasys.book.repository.BookRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // GET /api/books - list all books with optional filters
    @GetMapping
    public List<Book> getAllBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category) {

        if (title == null && author == null && category == null) {
            return bookRepository.findAll();
        }

        // For MongoDB, we filter using derived queries and combine results
        List<Book> results = bookRepository.findAll();

        if (title != null) {
            results = results.stream()
                    .filter(b -> b.getTitle().toLowerCase().contains(title.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (author != null) {
            results = results.stream()
                    .filter(b -> b.getAuthor().toLowerCase().contains(author.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (category != null) {
            results = results.stream()
                    .filter(b -> b.getCategory() != null &&
                            b.getCategory().toLowerCase().contains(category.toLowerCase()))
                    .collect(Collectors.toList());
        }
        return results;
    }

    // GET /api/books/{id} - get one book
    @GetMapping("/{id}")
    public Book getBookById(@PathVariable String id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Book not found with id: " + id));
    }

    // POST /api/books - create a book
    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) {
        Book saved = bookRepository.save(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT /api/books/{id} - update a book
    @PutMapping("/{id}")
    public Book updateBook(@PathVariable String id, @Valid @RequestBody Book bookDetails) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Book not found with id: " + id));

        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setIsbn(bookDetails.getIsbn());
        book.setCategory(bookDetails.getCategory());
        book.setCopiesAvailable(bookDetails.getCopiesAvailable());
        book.setTotalCopies(bookDetails.getTotalCopies());

        return bookRepository.save(book);
    }

    // DELETE /api/books/{id} - delete a book
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/books/{id}/decrement - decrement copiesAvailable by 1
    @PutMapping("/{id}/decrement")
    public Book decrementCopies(@PathVariable String id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Book not found with id: " + id));

        if (book.getCopiesAvailable() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No copies available for book: " + book.getTitle());
        }

        book.setCopiesAvailable(book.getCopiesAvailable() - 1);
        return bookRepository.save(book);
    }

    // PUT /api/books/{id}/increment - increment copiesAvailable by 1
    @PutMapping("/{id}/increment")
    public Book incrementCopies(@PathVariable String id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Book not found with id: " + id));

        book.setCopiesAvailable(book.getCopiesAvailable() + 1);
        return bookRepository.save(book);
    }
}
