package com.example.domaineventtest.infra;

import com.example.domaineventtest.domain.User;
import org.springframework.data.repository.CrudRepository;

public interface UserJpaRepository extends CrudRepository<User, Long> {
}
