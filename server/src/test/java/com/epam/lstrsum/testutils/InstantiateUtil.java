package com.epam.lstrsum.testutils;

import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.dto.attachment.AttachmentAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.dto.question.QuestionWithAnswersCountDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.dto.user.telescope.TelescopeDataDto;
import com.epam.lstrsum.dto.user.telescope.TelescopeEmployeeEntityDto;
import com.epam.lstrsum.dto.user.telescope.TelescopeProfileDto;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Attachment;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.model.Vote;
import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.api.EnhancedRandom;
import lombok.val;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.lstrsum.utils.FunctionalUtil.getList;
import static java.util.Collections.singletonList;

public class InstantiateUtil {
    public static final String SOME_USER_EMAIL = "John_Doe@epam.com";
    public static final String SOME_NOT_USER_EMAIL = "email@test.com";
    public static final String NON_EXISTING_USER_ID = "1123";
    public static final String EXISTING_USER_ID = "1u";
    public static final String EXISTING_QUESTION_ID = "1u_1r";
    public static final String EXISTING_USER_EMAIL = "Ernest_Hemingway@epam.com";
    public static final String EXISTING_ANSWER_ID = "1u_1r_1a";
    public static final String NON_EXISTING_QUESTION_ID = "notExist";
    private static final EnhancedRandom random = EnhancedRandomBuilder.aNewEnhancedRandomBuilder()
            .stringLengthRange(5, 50)
            .build();
    private static SecureRandom SECURE_RANDOM = new SecureRandom();

    public static Subscription someSubscription() {
        return random.nextObject(Subscription.class);
    }

    public static User someUser() {
        return random.nextObject(User.class);
    }

    public static User someActiveUser() {
        return someUserWithActive(true);
    }

    public static User someNotActiveUser() {
        return someUserWithActive(false);
    }

    public static User someUserWithActive(boolean active) {
        val someUser = someUser();
        someUser.setIsActive(active);
        return someUser;
    }

    public static AttachmentAllFieldsDto someAttachmentAllFieldsDto() {
        return random.nextObject(AttachmentAllFieldsDto.class);
    }

    public static TelescopeEmployeeEntityDto someTelescopeEmployeeEntityDtoWithEmail(String email) {
        return TelescopeEmployeeEntityDto.builder()
                .data(someTelescopeDataDtoWithEmail(email))
                .build();
    }

    public static TelescopeEmployeeEntityDto someTelescopeEmployeeEntityDto() {
        return TelescopeEmployeeEntityDto.builder()
                .data(someTelescopeDataDto())
                .build();
    }

    private static TelescopeDataDto someTelescopeDataDtoWithEmail(String email) {
        return TelescopeDataDto.builder()
                ._e3sId(someString())
                .email(singletonList(email))
                .fullName(someStrings())
                .firstName(someString())
                .lastName(someString())
                .displayName(someString())
                .primarySkill(someString())
                .primaryTitle(someString())
                .manager(someString())
                .profile(Collections.singletonMap(someString(), singletonList(someTelescopeProfileDto())))
                .photo(someStrings())
                .unitPath(someString())
                .build();
    }

    public static TelescopeDataDto someTelescopeDataDto() {
        return TelescopeDataDto.builder()
                ._e3sId(someString())
                .email(getList(InstantiateUtil::someStringLowerCase))
                .fullName(someStrings())
                .firstName(someString())
                .lastName(someString())
                .displayName(someString())
                .primarySkill(someString())
                .primaryTitle(someString())
                .manager(someString())
                .profile(Collections.singletonMap(someString(), singletonList(someTelescopeProfileDto())))
                .photo(someStrings())
                .unitPath(someString())
                .build();
    }

    private static TelescopeProfileDto someTelescopeProfileDto() {
        return random.nextObject(TelescopeProfileDto.class);
    }

    public static UserBaseDto someUserBaseDto() {
        return random.nextObject(UserBaseDto.class);
    }

    public static Answer someAnswer() {
        return random.nextObject(Answer.class);
    }

    public static List<Vote> someVotes() {
        return getList(InstantiateUtil::someVote);
    }

    public static AnswerPostDto someAnswerPostDto() {
        return random.nextObject(AnswerPostDto.class);
    }

    public static AnswerBaseDto someAnswerBaseDto() {
        return random.nextObject(AnswerBaseDto.class);
    }

    public static Question someQuestion() {
        return random.nextObject(Question.class);
    }

    public static QuestionAllFieldsDto someQuestionAllFieldsDto() {
        return random.nextObject(QuestionAllFieldsDto.class);
    }

    public static QuestionPostDto someQuestionPostDto() {
        return random.nextObject(QuestionPostDto.class);
    }

    public static QuestionBaseDto someQuestionBaseDto() {
        return random.nextObject(QuestionBaseDto.class);
    }

    public static QuestionAppearanceDto someQuestionAppearanceDto() {
        return random.nextObject(QuestionAppearanceDto.class);
    }

    public static QuestionWithAnswersCountDto someQuestionWithAnswersCountDto() {
        return random.nextObject(QuestionWithAnswersCountDto.class);
    }

    public static Attachment someAttachment() {
        return random.nextObject(Attachment.class);
    }

    public static Vote someVote() {
        return random.nextObject(Vote.class);
    }

    public static List<TelescopeEmployeeEntityDto> someTelescopeEmployeeEntityDtosWithEmails(String... emails) {
        return Arrays.stream(emails)
                .map(email -> TelescopeDataDto.builder()
                        ._e3sId(someString())
                        .displayName(someString())
                        .email(singletonList(email.toLowerCase()))
                        .firstName(someString())
                        .lastName(someString())
                        .fullName(someStrings())
                        .manager(someString())
                        .photo(someStrings())
                        .primarySkill(someString())
                        .primaryTitle(someString())
                        .build()
                )
                .map(data -> new TelescopeEmployeeEntityDto(data, someStrings()))
                .collect(Collectors.toList());
    }

    public static List<TelescopeEmployeeEntityDto> someTelescopeEmployeeEntityDtos() {
        return getList(InstantiateUtil::someTelescopeEmployeeEntityDto);
    }

    public static List<UserRoleType> someRoles() {
        return singletonList(someRole());
    }

    private static UserRoleType someRole() {
        final UserRoleType[] values = UserRoleType.values();
        return values[SECURE_RANDOM.nextInt(values.length)];
    }

    public static HttpEntity someEntity() {
        return new ByteArrayEntity(someString().getBytes());
    }

    public static String someString() {
        return random.nextObject(String.class);
    }

    public static String someStringLowerCase() {
        return new BigInteger(130, SECURE_RANDOM).toString(32);
    }

    public static List<String> someStrings() {
        return getList(InstantiateUtil::someString);
    }

    public static String[] someArrayStrings() {
        return getList(InstantiateUtil::someString).toArray(new String[0]);
    }

    public static long someLong() {
        return SECURE_RANDOM.nextLong();
    }

    public static int someInt() {
        return SECURE_RANDOM.nextInt();
    }
}
