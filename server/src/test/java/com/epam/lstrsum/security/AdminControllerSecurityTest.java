package com.epam.lstrsum.security;

import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.QuestionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_QUESTION_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.NON_EXISTING_QUESTION_ID;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@ActiveProfiles({"test", "controller-security-test"})
public class AdminControllerSecurityTest {
    private static final String QUESTION_DELETE_URL_WITH_EXISTING_QUESTION_ID = "/admin/question/" + EXISTING_QUESTION_ID;
    private static final String QUESTION_DELETE_URL_WITH_NON_EXISTING_QUESTION_ID = "/admin/question/" + NON_EXISTING_QUESTION_ID;
    private static final String USER_LIST_GET_URL = "/admin/user/list";
    private static final String FORCE_USER_SYNCHRONIZATION_PUT_URL = "/admin/user/synchronize";

    private MockMvc mockMvc;

    @Autowired
    QuestionService questionService;

    @Autowired
    AnswerService answerService;

    @Autowired
    private WebApplicationContext context;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = {"SIMPLE_USER", "ACTUATOR"})
    public void deleteQuestionByUserWithMultipleRoles() throws Exception {
        mockMvc
                .perform(delete(QUESTION_DELETE_URL_WITH_EXISTING_QUESTION_ID).with(csrf()))
                .andExpect(authenticated().withRoles("SIMPLE_USER", "ACTUATOR"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"SIMPLE_USER", "ACTUATOR"})
    public void getListOfUsersWithMultipleRoles() throws Exception {
        mockMvc
                .perform(get(USER_LIST_GET_URL).with(csrf()))
                .andExpect(authenticated().withRoles("SIMPLE_USER", "ACTUATOR"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"SIMPLE_USER", "ACTUATOR"})
    public void forceUserSynchronizationWithMultipleRoles() throws Exception {
        mockMvc
                .perform(put(FORCE_USER_SYNCHRONIZATION_PUT_URL).with(csrf()))
                .andExpect(authenticated().withRoles("SIMPLE_USER", "ACTUATOR"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EXTENDED_USER")
    public void deleteQuestionByUserWithExtendedRole() throws Exception {
        mockMvc
                .perform(delete(QUESTION_DELETE_URL_WITH_EXISTING_QUESTION_ID).with(csrf()))
                .andExpect(authenticated().withRoles("EXTENDED_USER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EXTENDED_USER")
    public void getListOfUsersWithExtendedRole() throws Exception {
        mockMvc
                .perform(get(USER_LIST_GET_URL).with(csrf()))
                .andExpect(authenticated().withRoles("EXTENDED_USER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EXTENDED_USER")
    public void forceUserSynchronizationWithExtendedRole() throws Exception {
        mockMvc
                .perform(put(FORCE_USER_SYNCHRONIZATION_PUT_URL).with(csrf()))
                .andExpect(authenticated().withRoles("EXTENDED_USER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "NOT_ALLOWED_USER")
    public void deleteQuestionByUserWithNotAllowedRole() throws Exception {
        mockMvc
                .perform(delete(QUESTION_DELETE_URL_WITH_EXISTING_QUESTION_ID).with(csrf()))
                .andExpect(authenticated().withRoles("NOT_ALLOWED_USER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "NOT_ALLOWED_USER")
    public void getListOfUsersWithNotAllowedRole() throws Exception {
        mockMvc
                .perform(get(USER_LIST_GET_URL).with(csrf()))
                .andExpect(authenticated().withRoles("NOT_ALLOWED_USER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "NOT_ALLOWED_USER")
    public void forceUserSynchronizationWithNotAllowedRole() throws Exception {
        mockMvc
                .perform(put(FORCE_USER_SYNCHRONIZATION_PUT_URL).with(csrf()))
                .andExpect(authenticated().withRoles("NOT_ALLOWED_USER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void deleteQuestionByAdminWithExistingQuestionId() throws Exception {
        when(questionService.contains(anyString())).thenReturn(true);

        mockMvc
                .perform(delete(QUESTION_DELETE_URL_WITH_EXISTING_QUESTION_ID).with(csrf()))
                .andExpect(authenticated().withRoles("ADMIN"))
                .andExpect(status().isNoContent());

        verify(questionService, times(1)).contains(anyString());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void deleteQuestionByAdminWithNonExistingQuestionId() throws Exception {
        when(questionService.contains(anyString())).thenReturn(false);

        mockMvc
                .perform(delete(QUESTION_DELETE_URL_WITH_NON_EXISTING_QUESTION_ID).with(csrf()))
                .andExpect(authenticated().withRoles("ADMIN"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getListOfUsersByAdmin() throws Exception {
        mockMvc
                .perform(get(USER_LIST_GET_URL).with(csrf()))
                .andExpect(authenticated().withRoles("ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void forceUserSynchronizationByAdmin() throws Exception {
        mockMvc
                .perform(put(FORCE_USER_SYNCHRONIZATION_PUT_URL).with(csrf()))
                .andExpect(authenticated().withRoles("ADMIN"))
                .andExpect(status().isNoContent());
    }
}