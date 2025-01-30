package fi.book.org;

import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import lombok.Getter;

@Getter
@Service
public class ApplicationPortListener {

    private int serverPort;

    @EventListener
    public void onApplicationEvent(final ServletWebServerInitializedEvent event) {
        serverPort = event.getWebServer().getPort();
    }

}
