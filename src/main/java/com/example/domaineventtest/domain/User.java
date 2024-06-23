package com.example.domaineventtest.domain;

import com.example.domaineventtest.domain.config.Events;
import com.example.domaineventtest.domain.event.UserChangedNameEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.data.domain.AbstractAggregateRoot;

@Entity
public class User extends AbstractAggregateRoot<User> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    protected User() {
    }

    public User(String name) {
        this.name = name;
    }

    public void changeName(String name) {
        this.name = name;
        registerEvent(new UserChangedNameEvent("AbstractAggregateRoot", this.name));
        Events.raise(new UserChangedNameEvent("Events.raise", this.name));
    }
}
