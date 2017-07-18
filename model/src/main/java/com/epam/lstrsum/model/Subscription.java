package com.epam.lstrsum.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = Subscription.SUBSCRIPTION_COLLECTION_NAME)
public class Subscription {
    public final static String SUBSCRIPTION_COLLECTION_NAME = "subscription";

    @Id
    private String subscriptionId;
    @DBRef
    private User userId;
    @DBRef
    private List<Request> requestIds;
}
