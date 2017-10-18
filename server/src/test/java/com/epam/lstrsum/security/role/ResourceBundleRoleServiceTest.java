package com.epam.lstrsum.security.role;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.enums.UserRoleType;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.util.ResourceBundle;

import static com.epam.lstrsum.enums.UserRoleType.ROLE_NOT_ALLOWED_USER;
import static com.epam.lstrsum.testutils.InstantiateUtil.someEpamEmployeePrincipal;
import static com.epam.lstrsum.testutils.InstantiateUtil.someUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

public class ResourceBundleRoleServiceTest extends SetUpDataBaseCollections {

    @Mock
    private ResourceBundle resourceBundle;

    @InjectMocks
    private ResourceBundleRoleService resourceBundleRoleService;

    @Before
    public void setUp() throws IOException {
        initMocks(this);
    }

    @Test
    public void getNotAllowedPrincipalRole() {
        assertThat(resourceBundleRoleService.getNotAllowedPrincipalRole()).isEqualTo(ROLE_NOT_ALLOWED_USER.name());
    }

    @Test
    public void getPrincipalRoles() {
        val user = someUser();
        val userRoles = user.getRoles().stream().map(UserRoleType::name).toArray(String[]::new);

        assertThat(resourceBundleRoleService.getPrincipalRoles(user)).isEqualTo(userRoles);
    }

    @Test
    public void getPrincipalRolesWithEpamEmployee() {
        val epamEmployeePrincipal = someEpamEmployeePrincipal();

        assertThat(resourceBundleRoleService.getPrincipalRoles(epamEmployeePrincipal))
                .isEqualTo(new String[]{ROLE_NOT_ALLOWED_USER.name()});
    }

}
