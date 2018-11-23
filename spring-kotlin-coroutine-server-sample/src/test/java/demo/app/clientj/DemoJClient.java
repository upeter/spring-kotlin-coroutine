package demo.app.clientj;

import demo.app.domain.Avatar;
import org.springframework.web.reactive.function.client.WebClient;

public class DemoJClient {

    public static void main(String [] args) {
        final Avatar avatar = WebClient
                .create("http://localhost:8081")
                .get()
                .uri("/avatar")
                .retrieve()
                .bodyToFlux(Avatar.class).blockFirst();
        System.out.println(avatar);
    }
}
