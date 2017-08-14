package com.epam.lstrsum.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@Document(collection = Subscription.SUBSCRIPTION_COLLECTION_NAME)
public class Subscription {
    public final static String SUBSCRIPTION_COLLECTION_NAME = "Subscription";

    @Id
    private String subscriptionId;
    @DBRef
    private User userId;
    @DBRef
    @Indexed
    private List<Question> questionIds;
}
