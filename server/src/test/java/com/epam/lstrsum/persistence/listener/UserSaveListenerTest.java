package com.epam.lstrsum.persistence.listener;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.enums.UserRoleType;
import com.google.common.collect.Sets;
import lombok.val;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static com.epam.lstrsum.testutils.InstantiateUtil.someUserWithRoles;

public class UserSaveListenerTest extends SetUpDataBaseCollections {

    private static final String USER_COLLECTION_NAME = "User";

    @Test
    public void onBeforeSave() {
        val user = someUserWithRoles(Sets.immutableEnumSet(UserRoleType.ROLE_SIMPLE_USER));
        getMongoTemplate().save(user, USER_COLLECTION_NAME);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void onBeforeSaveWithException() {
        val user = someUserWithRoles(Sets.immutableEnumSet(UserRoleType.ROLE_EXTENDED_USER, UserRoleType.ROLE_SIMPLE_USER));
        getMongoTemplate().save(user, USER_COLLECTION_NAME);
    }
}
