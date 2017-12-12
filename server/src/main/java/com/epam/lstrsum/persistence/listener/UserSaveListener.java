package com.epam.lstrsum.persistence.listener;

import com.epam.lstrsum.model.User;
import com.epam.lstrsum.utils.MessagesHelper;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

import static com.epam.lstrsum.enums.UserRoleType.ROLE_EXTENDED_USER;
import static com.epam.lstrsum.enums.UserRoleType.ROLE_SIMPLE_USER;

@Component
@Slf4j
public class UserSaveListener extends AbstractMongoEventListener<User> {

    @Autowired
    private MessagesHelper messagesHelper;

    @Override
    public void onBeforeSave(BeforeSaveEvent<User> event) {
        if (event.getSource().getRoles().containsAll(Sets.immutableEnumSet(ROLE_EXTENDED_USER, ROLE_SIMPLE_USER))) {
            log.error("Cannot save a user with roles : " + ROLE_EXTENDED_USER + " and " + ROLE_SIMPLE_USER + " at the same time");
            throw new DataIntegrityViolationException(
                    MessageFormat.format(messagesHelper.get("validation.persistence.cannot-save-user-with-many-roles"),
                            ROLE_EXTENDED_USER, ROLE_SIMPLE_USER));
        }
    }
}
