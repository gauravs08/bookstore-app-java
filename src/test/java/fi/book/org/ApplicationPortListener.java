package fi.book.org;

import org.springframework.boot.web.reactive.context.ReactiveWebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import lombok.Getter;

@Getter
@Service
public class ApplicationPortListener {

    private int serverPort;

    @EventListener
    public void onApplicationEvent(final ReactiveWebServerInitializedEvent event) {
        serverPort = event.getWebServer().getPort();
    }

}
