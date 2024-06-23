package com.example.domaineventtest.application;

import com.example.domaineventtest.domain.User;
import com.example.domaineventtest.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void createUser(String name) {
        User user = new User(name);

        userRepository.save(user);
    }

    public void changeUserName(Long id, String name) {
        User user = userRepository.findById(id)
                .orElseThrow();

        user.changeName(name);

        /**
         * https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/domain/AbstractAggregateRoot.html
         * save() 또는 delete()를 호출해야만 이벤트가 발행됨.
         */
//        userRepository.save(user);
    }

}
