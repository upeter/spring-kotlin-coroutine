package demo.app.clientj;

import demo.app.domain.Awatar;
import org.springframework.web.reactive.function.client.WebClient;

public class DemoJClient {

    public static void main(String [] args) {
        final Awatar awatar = WebClient
                .create("http://localhost:8081")
                .get()
                .uri("/awatar")
                .retrieve()
                .bodyToFlux(Awatar.class).blockFirst();
        System.out.println(awatar);
    }
}
