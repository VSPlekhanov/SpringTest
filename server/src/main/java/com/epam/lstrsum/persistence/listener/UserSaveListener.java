package com.epam.lstrsum.persistence.listener;

import com.epam.lstrsum.model.User;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;

import static com.epam.lstrsum.enums.UserRoleType.ROLE_EXTENDED_USER;
import static com.epam.lstrsum.enums.UserRoleType.ROLE_SIMPLE_USER;

@Component
@Slf4j
public class UserSaveListener extends AbstractMongoEventListener<User> {

    @Override
    public void onBeforeSave(BeforeSaveEvent<User> event) {
        if (event.getSource().getRoles().containsAll(Sets.immutableEnumSet(ROLE_EXTENDED_USER, ROLE_SIMPLE_USER))) {
            log.error("Cannot save a user with roles : " + ROLE_EXTENDED_USER + " and " + ROLE_SIMPLE_USER + " at the same time");
            throw new DataIntegrityViolationException(
                    "Cannot save a user with roles : " + ROLE_EXTENDED_USER + " and " + ROLE_SIMPLE_USER + " at the same time");
        }
    }
}
