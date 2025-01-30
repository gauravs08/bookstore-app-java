package fi.book.org;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BookApplicationTest {

    @Test
    void applicationStartsSuccessfully() {
        BookApplication.main(new String[]{});
    }
}
