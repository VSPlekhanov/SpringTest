package com.epam.lstrsum;

import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
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

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InstantiateUtil {
    private static SecureRandom SECURE_RANDOM = new SecureRandom();
    public static final String SOME_USER_EMAIL = "John_Doe@epam.com";

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
                .roles(Collections.singletonList(UserRoleType.ROLE_EXTENDED_USER))
                .email(someString())
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

    public static TelescopeEmployeeEntityDto someTelescopeEmployeeEntityDto() {
        return TelescopeEmployeeEntityDto.builder()
                .data(someTelescopeDataDto())
                .build();
    }

    public static TelescopeDataDto someTelescopeDataDto() {
        return TelescopeDataDto.builder()
                ._e3sId("8841d390-36fa-4173-8430-6849da54881a")
                .email(new String[]{"Ivan_Ivanov@epam.com"})
                .fullName(new String[]{"Ivanov Ivan Ivanovich"})
                .firstName("Ivan")
                .lastName("Ivanov")
                .displayName("Ivanov Ivan")
                .primarySkill("DevOps/Automation")
                .primaryTitle("Systems Engineer")
                .manager("Olga Petrova")
                .profile(Collections.singletonMap("UPSA", new TelescopeProfileDto[]{someTelescopeProfileDto()}))
                .photo(new String[]{"attachment:///upsa_profilePhoto.4060641410043119281_1.GIF_b8f369df-8a70-4d79-8413-120b31e39016"})
                .unitPath("Cloud & DevOps / NA / MX / DevOps")
                .build();
    }

    private static TelescopeProfileDto someTelescopeProfileDto() {
        return TelescopeProfileDto.builder()
                .origin("UPSA")
                .id("40607414003048743")
                .status("Active")
                .url("https://upsa.epam.com/workload/employeeView.do?employeeId=406074147648379594")
                .visibility("public")
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
                .tags(new String[]{someString(), someString()})
                .allowedSubs(Collections.emptyList())
                .authorId(someUser())
                .createdAt(Instant.now())
                .deadLine(Instant.now())
                .upVote(SECURE_RANDOM.nextInt())
                .build();
    }

    public static QuestionPostDto someQuestionPostDto() {
        return QuestionPostDto.builder()
                .allowedSubs(initList(InstantiateUtil::someString))
                .deadLine(someLong())
                .tags(initList(InstantiateUtil::someString).toArray(new String[0]))
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
                .tags(initList(InstantiateUtil::someString).toArray(new String[0]))
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

    public static <T> List<T> initList(Supplier<T> supplier) {
        return initList(supplier, SECURE_RANDOM.nextInt(10));
    }

    public static String someString() {
        return new BigInteger(130, SECURE_RANDOM).toString(32);
    }

    public static long someLong() {
        return SECURE_RANDOM.nextLong();
    }
}
