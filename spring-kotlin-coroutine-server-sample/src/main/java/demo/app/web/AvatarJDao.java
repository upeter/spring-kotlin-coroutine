package demo.app.web;

import demo.app.domain.Avatar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AvatarJDao {
    private WebClient webClient;

    public AvatarJDao() {
        webClient = WebClient
                .create("http://localhost:8081");
    }

    public Mono<Avatar> randomAvatar() {
        return
                webClient.get()
                        .uri("/avatar")
                        .retrieve()
                        .bodyToMono(Avatar.class);
    }
}
