package demo.app.dbj;

import com.firefly.db.jdbc.JDBCClient;
import com.firefly.reactive.adapter.Reactor;
import com.firefly.reactive.adapter.db.ReactiveSQLClient;
import com.firefly.reactive.adapter.db.ReactiveSQLConnection;
import com.firefly.utils.function.Func1;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import demo.app.domain.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;

public class DBJTest {

    private ReactiveSQLClient sqlClient;

    public DBJTest() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:" + UUID.randomUUID().toString().replace(" - ", " "));
        config.setDriverClassName("org.h2.Driver");
        config.setAutoCommit(false);
        HikariDataSource ds = new HikariDataSource(config);
        sqlClient = Reactor.db.fromSQLClient(new JDBCClient(ds));
    }

    private <T> Mono<T> exec(Func1<ReactiveSQLConnection, Mono<T>> func1) {
        return sqlClient.newTransaction(func1);
    }

    @Before
    public void before() {
        exec(c -> c.update("drop schema if exists test")
                .flatMap(v -> c.update("create schema test"))
                .flatMap(v -> c.update("CREATE TABLE IF NOT EXISTS `test`.`user`(" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "pt_first_name VARCHAR(255), " +
                        "pt_last_name VARCHAR(255), " +
                        "pt_avatar_url VARCHAR(255))"))
                .flatMap(v -> {
                    Object[][] params = new Object[2][2];
                    for (int i = 0; i < 2; i++) {
                        params[i][0] = "John " + i;
                        params[i][1] = "Doe " + i;
                    }
                    String sql = "insert into `test`.`user`(pt_first_name, pt_last_name) values(?,?)";
                    return c.insertBatch(sql, params, (rs) ->
                            rs.stream()
                                    .map(r -> r.getInt(1))
                                    .collect(Collectors.toList()));
                })).doOnSuccess(System.out::println).block();
    }


    @Test
    public void testQueryById() {
        Mono<User> user = exec(c -> c.queryById(1, User.class));
        StepVerifier.create(user)
                .assertNext(u -> Assert.assertThat(u.getFirstName(), is("John 0")))
                .verifyComplete();
    }

    @Test
    public void testInsertUser() {
        User newUser = new User(null, "test insert", "test insert pwd", null);
        Mono<Long> newUserId = exec(c -> c.insertObject(newUser));
        StepVerifier.create(newUserId)
                .assertNext(i -> {
                    Assert.assertThat(i, is(3l));
                })
                .verifyComplete();
    }

    @Test
    public void testUpdateUser() {
        Mono<User> userMono = exec(c -> c.queryById(1, User.class));
        StepVerifier.create(userMono).assertNext(u -> Assert.assertThat(u.getId(), is(1L))).verifyComplete();
        User user = userMono.as(i -> i.block());
        user.setAwatarUrl("abc");

        Mono<Integer> row = exec(c -> c.updateObject(user));
        StepVerifier.create(row).expectNext(1).verifyComplete();

        Mono<User> userMono2 = exec(c -> c.queryById(1, User.class));
        StepVerifier.create(userMono2)
                .assertNext(u -> Assert.assertThat(u.getAwatarUrl(), is("abc")))
                .verifyComplete();

    }
}
