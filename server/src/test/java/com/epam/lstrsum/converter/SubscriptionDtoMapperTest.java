package com.epam.lstrsum.converter;

import com.epam.lstrsum.InstantiateUtil;
import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.model.Subscription;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.epam.lstrsum.InstantiateUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SubscriptionDtoMapperTest extends SetUpDataBaseCollections {
    @Autowired
    private SubscriptionDtoMapper subscriptionDtoMapper;

    @Test
    public void modelToAllFieldsDto() throws Exception {
        Subscription subscription = someSubscription();
        UserBaseDto userId = someUserBaseDto();
        List<QuestionBaseDto> questionIds = initList(InstantiateUtil::someQuestionBaseDto, 2);

        assertThat(subscriptionDtoMapper.modelToAllFieldsDto(subscription, userId, questionIds))
                .satisfies(
                        subscriptionAllFieldsDto -> {
                            assertThat(subscriptionAllFieldsDto.getSubscriptionId()).isEqualTo(subscription.getSubscriptionId());
                            assertThat(subscriptionAllFieldsDto.getUserId()).isEqualTo(userId);
                            assertThat(subscriptionAllFieldsDto.getQuestionIds()).containsExactly(questionIds.get(0),questionIds.get(1));
                        }
                );
    }

}