package com.epam.lstrsum.testutils;

import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
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
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;

public class InstantiateUtil {
    public static final String SOME_USER_EMAIL = "John_Doe@epam.com";
    public static final String SOME_NOT_USER_EMAIL = "email@test.com";
    public static final String NON_EXISTING_USER_ID = "1123";
    public static final String EXISTING_USER_ID = "1u";
    private static SecureRandom SECURE_RANDOM = new SecureRandom();

    public static Subscription someSubscription() {
        return Subscription.builder()
                .questionIds(initList(InstantiateUtil::someQuestion))
                .userId(someUser())
                .subscriptionId(someString())
                .build();
    }

    public static User someUser() {
        return User.builder()
                .userId(someString())
                .firstName(someString())
                .lastName(someString())
                .isActive(true)
                .createdAt(Instant.now())
                .roles(Collections.singletonList(UserRoleType.EXTENDED_USER))
                .email(someString())
                .build();
    }

    public static TelescopeEmployeeEntityDto someTelescopeEmployeeEntityDto() {
        return TelescopeEmployeeEntityDto.builder()
                .data(someTelescopeDataDto())
                .build();
    }

    public static TelescopeDataDto someTelescopeDataDto() {
        return TelescopeDataDto.builder()
                ._e3sId(someString())
                .email(someStrings())
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
        return TelescopeProfileDto.builder()
                .origin(someString())
                .id(someString())
                .status(someString())
                .url(someString())
                .visibility(someString())
                .build();
    }

    public static UserBaseDto someUserBaseDto() {
        return UserBaseDto.builder()
                .email(someString())
                .firstName(someString())
                .lastName(someString())
                .userId(someString())
                .build();
    }

    public static Answer someAnswer() {
        return Answer.builder()
                .answerId(someString())
                .authorId(someUser())
                .questionId(someQuestion())
                .createdAt(Instant.now())
                .text(someString())
                .upVote(SECURE_RANDOM.nextInt())
                .build();
    }

    public static AnswerPostDto someAnswerPostDto() {
        return AnswerPostDto.builder()
                .questionId(someString())
                .text(someString())
                .build();
    }

    public static AnswerBaseDto someAnswerBaseDto() {
        return AnswerBaseDto.builder()
                .authorId(someUserBaseDto())
                .createdAt(Instant.now())
                .text(someString())
                .upVote(SECURE_RANDOM.nextInt())
                .build();
    }

    public static Question someQuestion() {
        return Question.builder()
                .questionId(someString())
                .title(someString())
                .tags(someArrayString())
                .allowedSubs(Collections.emptyList())
                .authorId(someUser())
                .createdAt(Instant.now())
                .deadLine(Instant.now())
                .upVote(SECURE_RANDOM.nextInt())
                .build();
    }

    public static QuestionAllFieldsDto someQuestionAllFieldsDto() {
        return new QuestionAllFieldsDto(
                someString(), someString(), someArrayString(),
                Instant.now(), Instant.now(), someUserBaseDto(), SECURE_RANDOM.nextInt(),
                initList(InstantiateUtil::someUserBaseDto), someString()
        );
    }

    public static QuestionPostDto someQuestionPostDto() {
        return QuestionPostDto.builder()
                .allowedSubs(initList(InstantiateUtil::someString))
                .deadLine(someLong())
                .tags(someArrayString())
                .text(someString())
                .title(someString())
                .build();
    }

    public static QuestionBaseDto someQuestionBaseDto() {
        return QuestionBaseDto.builder()
                .author(someUserBaseDto())
                .createdAt(Instant.now())
                .deadLine(Instant.now())
                .questionId(someString())
                .tags(someArrayString())
                .title(someString())
                .upVote(SECURE_RANDOM.nextInt())
                .build();
    }

    public static Attachment someAttachment() {
        return Attachment.builder()
                .id(someString())
                .name(someString())
                .data(someString().getBytes())
                .type(someString())
                .build();
    }

    public static Vote someVote() {
        return Vote.builder()
                .answerId(someAnswer())
                .userId(someUser())
                .voteId(someString())
                .isRevoked(true)
                .createdAt(Instant.now())
                .build();
    }

    public static <T> List<T> initList(Supplier<T> supplier, int limit) {
        return Stream.generate(supplier)
                .limit(limit)
                .collect(Collectors.toList());
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
        return initList(InstantiateUtil::someTelescopeEmployeeEntityDto);
    }

    public static List<UserRoleType> someRoles() {
        return singletonList(someRole());
    }

    public static UserRoleType someRole() {
        final UserRoleType[] values = UserRoleType.values();
        return values[SECURE_RANDOM.nextInt(values.length)];
    }

    public static HttpEntity someEntity() {
        return new ByteArrayEntity(someString().getBytes());
    }

    public static <T> List<T> initList(Supplier<T> supplier) {
        return initList(supplier, SECURE_RANDOM.nextInt(10));
    }

    public static String someString() {
        return new BigInteger(130, SECURE_RANDOM).toString(32);
    }

    public static List<String> someStrings() {
        return initList(InstantiateUtil::someString);
    }

    public static String[] someArrayString() {
        return initList(InstantiateUtil::someString).toArray(new String[0]);
    }

    public static long someLong() {
        return SECURE_RANDOM.nextLong();
    }

    public static int someInt() {
        return SECURE_RANDOM.nextInt();
    }
}
