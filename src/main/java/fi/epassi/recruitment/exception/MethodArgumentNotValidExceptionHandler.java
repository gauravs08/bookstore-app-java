package fi.epassi.recruitment.exception;

import org.springframework.stereotype.Component;
import org.zalando.problem.spring.web.advice.validation.MethodArgumentNotValidAdviceTrait;

@Component
public class MethodArgumentNotValidExceptionHandler implements MethodArgumentNotValidAdviceTrait {

}
