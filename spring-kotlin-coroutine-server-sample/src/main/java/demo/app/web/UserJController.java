package demo.app.web;

import demo.app.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
public class UserJController {

    private final UserJDao userDao;
    private final AwatarJDao awatarDao;

    @Autowired
    public UserJController(UserJDao userDao, AwatarJDao awatarDao) {
        this.userDao = userDao;
        this.awatarDao = awatarDao;
    }

    @GetMapping("/usersj/{user-id}")
    @ResponseBody
    public Flux<User> getUser(@PathVariable("user-id") Long id) {
        return userDao.findById(id).flux();
    }

    @PatchMapping("/usersj/{user-id}/sync-awatar")
    @ResponseBody
    public Flux<User> syncAwatar(@PathVariable("user-id") Long id) {
        return userDao.findById(id)
                .flatMap(user -> awatarDao.randomAwatar()
                        .flatMap(awatar ->
                                userDao.updateUser(UserBuilder
                                        .from(user)
                                        .withAwatarUrl(awatar.getUrl())
                                        .build()))

                ).flux();
    }


    private static class UserBuilder {
        private User newUser;

        private UserBuilder(User user) {
            this.newUser = user;
        }

        static UserBuilder from(User user) {
            User newUser = new User(user.getId(), user.getFirstName(), user.getLastName(), user.getAwatarUrl());
            return new UserBuilder(newUser);
        }

        public UserBuilder withAwatarUrl(String url) {
            newUser = new User(newUser.getId(), newUser.getFirstName(), newUser.getLastName(), url);
            return this;
        }

        public User build() {
            return newUser;
        }
    }
}

