package com.epam.lstrsum.persistence.listener;

import com.epam.lstrsum.SetUpDataBaseCollections;
import lombok.val;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.EnumSet;

import static com.epam.lstrsum.enums.UserRoleType.ROLE_EXTENDED_USER;
import static com.epam.lstrsum.enums.UserRoleType.ROLE_SIMPLE_USER;
import static com.epam.lstrsum.testutils.InstantiateUtil.someUserWithRoles;

public class UserSaveListenerTest extends SetUpDataBaseCollections {

    private static final String USER_COLLECTION_NAME = "User";

    @Test
    public void onBeforeSave() {
        val user = someUserWithRoles(EnumSet.of(ROLE_SIMPLE_USER));
        getMongoTemplate().save(user, USER_COLLECTION_NAME);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void onBeforeSaveWithException() {
        val user = someUserWithRoles(EnumSet.of(ROLE_EXTENDED_USER, ROLE_SIMPLE_USER));
        getMongoTemplate().save(user, USER_COLLECTION_NAME);
    }
}
