package com.epam.lstrsum.security;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class EpamEmployeePrincipalTest {
    private static String SOME_EMAIL = "email";
    private static String SOME_UNIQUE_NAME = "unique_name";
    private static String SOME_USER_ID = "user_id";
    private static boolean DL_USER = true;

    public static void checkThatOfMapThrowException(Map<String, Object> epamEmployeePrincipalMap) {
        assertThatThrownBy(() -> EpamEmployeePrincipal.ofMap(epamEmployeePrincipalMap))
                .hasNoCause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageMatching(".*Not String value by a key : .*");
    }

    @Test
    public void EpamEmployeePrincipalOfMapSuccessful() throws Exception {
        Map<String, Object> epamEmployeePrincipalMap = new HashMap<>();

        epamEmployeePrincipalMap.put(EpamEmployeePrincipal.EMAIL, SOME_EMAIL);
        epamEmployeePrincipalMap.put(EpamEmployeePrincipal.UNIQUE_NAME, SOME_UNIQUE_NAME);
        epamEmployeePrincipalMap.put(EpamEmployeePrincipal.USER_ID, SOME_USER_ID);
        epamEmployeePrincipalMap.put(EpamEmployeePrincipal.DISTRIBUTION_LIST_USER, DL_USER);

        EpamEmployeePrincipal epamEmployeePrincipal = EpamEmployeePrincipal.ofMap(epamEmployeePrincipalMap);

        assertThat(epamEmployeePrincipal)
                .hasFieldOrPropertyWithValue("email", SOME_EMAIL)
                .hasFieldOrPropertyWithValue("displayName", SOME_UNIQUE_NAME)
                .hasFieldOrPropertyWithValue("userId", SOME_USER_ID)
                .hasFieldOrPropertyWithValue("userInDistributionList", DL_USER);
    }

    @Test
    public void EpamEmployeePrincipalOfMapNotBooleanValueByDistributionListKey() {
        Map<String, Object> epamEmployeePrincipalMap = new HashMap<>();

        epamEmployeePrincipalMap.put(EpamEmployeePrincipal.EMAIL, SOME_EMAIL);
        epamEmployeePrincipalMap.put(EpamEmployeePrincipal.UNIQUE_NAME, SOME_UNIQUE_NAME);
        epamEmployeePrincipalMap.put(EpamEmployeePrincipal.USER_ID, SOME_USER_ID);
        epamEmployeePrincipalMap.put(EpamEmployeePrincipal.DISTRIBUTION_LIST_USER, 1);

        assertThatThrownBy(() -> EpamEmployeePrincipal.ofMap(epamEmployeePrincipalMap))
                .hasNoCause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageMatching(".*Not boolean value by  a key : .*");
    }

    @Test
    public void EpamEmployeePrincipalOfMapNullUserId() {
        checkThatOfMapThrowException(emptyMap());
    }

    @Test
    public void EpamEmployeePrincipalOfMapNullUniqueName() {
        Map epamEmployeePrincipalMap = mock(Map.class);
        when(epamEmployeePrincipalMap.get(eq(EpamEmployeePrincipal.USER_ID))).thenReturn(SOME_USER_ID);

        checkThatOfMapThrowException(epamEmployeePrincipalMap);

        verify(epamEmployeePrincipalMap, times(1)).get(eq(EpamEmployeePrincipal.USER_ID));
        verify(epamEmployeePrincipalMap, times(1)).get(eq(EpamEmployeePrincipal.UNIQUE_NAME));
    }

    @Test
    public void EpamEmployeePrincipalOfMapNullEmail() {
        Map epamEmployeePrincipalMap = mock(Map.class);
        when(epamEmployeePrincipalMap.get(eq(EpamEmployeePrincipal.USER_ID))).thenReturn(SOME_USER_ID);
        when(epamEmployeePrincipalMap.get(eq(EpamEmployeePrincipal.UNIQUE_NAME))).thenReturn(SOME_UNIQUE_NAME);

        checkThatOfMapThrowException(epamEmployeePrincipalMap);

        verify(epamEmployeePrincipalMap, times(1)).get(eq(EpamEmployeePrincipal.USER_ID));
        verify(epamEmployeePrincipalMap, times(1)).get(eq(EpamEmployeePrincipal.UNIQUE_NAME));
        verify(epamEmployeePrincipalMap, times(1)).get(eq(EpamEmployeePrincipal.EMAIL));
    }

    @Test
    public void checkGetNameReturnDisplayName() {
        Map<String, Object> epamEmployeePrincipalMap = new HashMap<>();

        epamEmployeePrincipalMap.put(EpamEmployeePrincipal.EMAIL, SOME_EMAIL);
        epamEmployeePrincipalMap.put(EpamEmployeePrincipal.UNIQUE_NAME, SOME_UNIQUE_NAME);
        epamEmployeePrincipalMap.put(EpamEmployeePrincipal.USER_ID, SOME_USER_ID);

        EpamEmployeePrincipal epamEmployeePrincipal = EpamEmployeePrincipal.ofMap(epamEmployeePrincipalMap);

        assertEquals(epamEmployeePrincipal.getName(), SOME_UNIQUE_NAME);
    }
}