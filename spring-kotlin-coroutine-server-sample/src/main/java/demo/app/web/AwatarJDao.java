package demo.app.web;

import demo.app.domain.Awatar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AwatarJDao {
    private WebClient webClient;

    public AwatarJDao() {
        webClient = WebClient
                .create("http://localhost:8081");
    }

    public Mono<Awatar> randomAwatar() {
        return
                webClient.get()
                        .uri("/awatar")
                        .retrieve()
                        .bodyToMono(Awatar.class);
    }
}
