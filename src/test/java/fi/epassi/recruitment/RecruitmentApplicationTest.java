package fi.epassi.recruitment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RecruitmentApplicationTest {

    @Test
    void applicationStartsSuccessfully() {
        // This test ensures that the Spring application context loads without issues.
        RecruitmentApplication.main(new String[] {});
    }
}
