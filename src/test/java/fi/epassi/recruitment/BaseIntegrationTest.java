package fi.epassi.recruitment;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

//@Transactional
@Rollback
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ComponentScan(basePackages = "fi.epassi.recruitment")
@TestInstance(PER_CLASS)
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    protected ApplicationPortListener applicationPortListener;

    protected String getEndpointUrl(String path) {
        return "http://localhost:" + applicationPortListener.getServerPort() + path;
    }

}
