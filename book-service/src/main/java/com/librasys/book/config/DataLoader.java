package com.librasys.book.config;

import com.librasys.book.model.ApiKey;
import com.librasys.book.model.Book;
import com.librasys.book.repository.ApiKeyRepository;
import com.librasys.book.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initData(BookRepository bookRepository, ApiKeyRepository apiKeyRepository) {
        return args -> {
            if (bookRepository.count() == 0) {
                bookRepository.save(new Book("Clean Code", "Robert C. Martin",
                        "978-0132350884", "Software Engineering", 3, 5));
                bookRepository.save(new Book("Design Patterns", "Erich Gamma",
                        "978-0201633610", "Software Engineering", 2, 3));
                bookRepository.save(new Book("The Pragmatic Programmer", "David Thomas",
                        "978-0135957059", "Software Engineering", 1, 2));
                bookRepository.save(new Book("Introduction to Algorithms", "Thomas H. Cormen",
                        "978-0262033848", "Computer Science", 4, 6));
                bookRepository.save(new Book("Artificial Intelligence: A Modern Approach",
                        "Stuart Russell", "978-0134610993", "Artificial Intelligence", 2, 4));

                System.out.println(">>> 5 sample books loaded into the catalog.");
            }

            if (apiKeyRepository.count() == 0) {
                apiKeyRepository.save(new ApiKey(
                        "book-service-key-2026",
                        "book-service",
                        "Direct Access & Gateway Authorization Key for Book Service (Student 2)",
                        "ACTIVE"
                ));
                System.out.println(">>> Seeded API Key into MongoDB 'api_keys' collection for Book Service.");
            }
        };
    }
}
