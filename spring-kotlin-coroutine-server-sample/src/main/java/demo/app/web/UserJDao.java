package demo.app.web;

import com.firefly.reactive.adapter.db.ReactiveSQLClient;
import com.firefly.reactive.adapter.db.ReactiveSQLConnection;
import com.firefly.utils.function.Func1;
import demo.app.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UserJDao {
    private ReactiveSQLClient sqlClient;

    @Autowired
    public UserJDao(ReactiveSQLClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    public Mono<User> findById(Long id) {
        return exec(c -> c.queryById(id, User.class));
    }

    public Mono<User> updateUser(User user) {
        return exec(c -> c.updateObject(user)).flatMap(i -> Mono.just(user));
    }

    private <T> Mono<T> exec(Func1<ReactiveSQLConnection, Mono<T>> func1) {
        return sqlClient.newTransaction(func1);
    }


}
