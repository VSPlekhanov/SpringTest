package com.epam.lstrsum.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class CustomMultipartResolverTest {

    private CustomMultipartResolver resolver;

    private String restrictions;

    @Parameterized.Parameters
    public static Collection<String> getRestrictions() {
        return Arrays.asList(
                "exe", "exe,com", "com,exe", "com,exe,pdf",
                " exe ", " exe, com ", "com , exe ", "com , exe , pdf",
                "exe, batt", "abat, exe"
        );
    }

    public CustomMultipartResolverTest(String restrictString) {
        this.restrictions = restrictString;
    }


    @Before
    public void setUp() throws Exception {
        resolver = new CustomMultipartResolver();
        resolver.setRestrictions(restrictions);
    }

    @Test
    public void isRestrictedShouldRestrictInUsualCase() throws Exception {
        assertTrue(restrictions, resolver.isRestricted("exe"));
    }

    @Test
    public void isRestrictedShouldNotRestrictInUsualCase() throws Exception {
        assertFalse(restrictions, resolver.isRestricted("bat"));
    }


}