package demo.app.web;

import demo.app.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
public class UserJController {

    private final UserJDao userDao;
    private final AvatarJDao avatarDao;

    @Autowired
    public UserJController(UserJDao userDao, AvatarJDao avatarDao) {
        this.userDao = userDao;
        this.avatarDao = avatarDao;
    }

    @GetMapping("/usersj/{user-id}")
    @ResponseBody
    public Flux<User> getUser(@PathVariable("user-id") Long id) {
        return userDao.findById(id).flux();
    }

    @PatchMapping("/usersj/{user-id}/sync-avatar")
    @ResponseBody
    public Flux<User> syncAvatar(@PathVariable("user-id") Long id) {
        return userDao.findById(id)
                .flatMap(user -> avatarDao.randomAvatar()
                        .flatMap(avatar ->
                                userDao.updateUser(UserBuilder
                                        .from(user)
                                        .withAvatarUrl(avatar.getUrl())
                                        .build()))

                ).flux();
    }


    private static class UserBuilder {
        private User newUser;

        private UserBuilder(User user) {
            this.newUser = user;
        }

        static UserBuilder from(User user) {
            User newUser = new User(user.getId(), user.getFirstName(), user.getLastName(), user.getAvatarUrl());
            return new UserBuilder(newUser);
        }

        public UserBuilder withAvatarUrl(String url) {
            newUser = new User(newUser.getId(), newUser.getFirstName(), newUser.getLastName(), url);
            return this;
        }

        public User build() {
            return newUser;
        }
    }
}

