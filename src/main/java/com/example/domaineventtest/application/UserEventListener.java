package com.example.domaineventtest.application;

import com.example.domaineventtest.domain.event.UserChangedNameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserEventListener {
    Logger logger = LoggerFactory.getLogger(UserEventListener.class);

    @TransactionalEventListener
    public void onUserNameChanged(UserChangedNameEvent event) {
        logger.info("User name changed from: {}", event.from());
    }
}
