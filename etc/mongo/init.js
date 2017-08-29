/*
* for running scripts use
*  mongo <host>:<port> mongo.init/init.js
*/

conn = new Mongo()
/*
Here is database name to init
*/
db = conn.getDB("ExperienceDataBase")

function randomString(length) {
    const chars = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'
    result = ''
    for (let i = length; i > 0; --i) {
        result += chars[Math.floor(Math.random() * chars.length)]
    }
    return result
}

function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min)) + min
}

function randomISODate() {
    return ISODate(new Date(randomInt(1990, 2015), randomInt(1, 10), randomInt(1, 10), randomInt(1, 10), randomInt(1, 10)).toISOString())
}

/*ATTENTION!!!!
* This number response for how many documents will in db after script
*/
const N = 50
/**/

const USER_COLLECTION_NAME = "User"
const ATTACHMENT_COLLECTION_NAME = "Attachment"
const QUESTION_COLLECTION_NAME = "Question"
const ANSWER_COLLECTION_NAME = "Answer"
const SUBSCRIPTION_COLLECTION_NAME = "Subscription"
const LITTLE_STRING_LENGTH = 50
const ATTACHMENT_SIZE = 10000
const MAXIMUM_TAGS_AMOUNT = 20
const MINIMUM_TAGS_AMOUNT = 5
const MINIMUM_TEXT_SIZE = 100
const MAXIMUM_TEXT_SIZE = 300
const ALL_ROLES = ["ADMIN", "SIMPLE_USER", "EXTENDED_USER", "NOT_ALLOWED_USER"]

collectionNames = db.getCollectionNames()

//clear collections
for (let i = 0; i < collectionNames.length; ++i) {
    db.getCollection(collectionNames[i]).remove({})
}

//create Users
for (let i = 0; i < N; ++i) {
    newUser = {}
    newUser._class = "com.epam.lstrsum.model.User";
    newUser.firstName = randomString(LITTLE_STRING_LENGTH)
    newUser.lastName = randomString(LITTLE_STRING_LENGTH)
    newUser.email = randomString(LITTLE_STRING_LENGTH)
    newUser.isActive = true
    newUser.roles = []
    newUser.createdAt = randomISODate()
    roleAmount = randomInt(0, ALL_ROLES.length)

    for (let j = 0; j < roleAmount; ++j) {
        newUser.roles.push(ALL_ROLES[randomInt(0, ALL_ROLES.length)])
    }

    db.getCollection(USER_COLLECTION_NAME).insert(newUser)
}

db.getCollection(USER_COLLECTION_NAME).createIndex({"email": 1}, {"unique": true});
allUsers = db.getCollection(USER_COLLECTION_NAME).find().toArray()

//create Attachments
for (let i = 0; i < N; ++i) {
    newAttachment = {}
    newAttachment._class = "com.epam.lstrsum.model.Attachment";
    newAttachment.name = randomString(LITTLE_STRING_LENGTH)
    newAttachment.type = randomString(LITTLE_STRING_LENGTH)
    newAttachment.data = randomString(ATTACHMENT_SIZE)

    db.getCollection(ATTACHMENT_COLLECTION_NAME).insert(newAttachment)
}

allAttachments = db.getCollection(ATTACHMENT_COLLECTION_NAME).find({}).toArray()

//create Questions
for (let i = 0; i < N; ++i) {
    newQuestion = {}
    newQuestion._class = "com.epam.lstrsum.model.Question";
    newQuestion.title = randomString(LITTLE_STRING_LENGTH)
    newQuestion.tags = []
    newQuestion.createdAt = randomISODate()
    newQuestion.deadLine = randomISODate()

    tagsAmount = randomInt(MINIMUM_TAGS_AMOUNT, MAXIMUM_TAGS_AMOUNT)
    for (let j = 0; j < tagsAmount; ++j) {
        newQuestion.tags.push(randomString(LITTLE_STRING_LENGTH))
    }

    newQuestion.text = randomString(randomInt(MINIMUM_TEXT_SIZE, MAXIMUM_TEXT_SIZE))
    newQuestion.authorId = DBRef(USER_COLLECTION_NAME, allUsers[randomInt(0, allUsers.length)]._id)

    newQuestion.allowedSubs = []
    subsAmount = randomInt(0, allUsers.length)
    for (let j = 0; j < subsAmount; ++j) {
        newQuestion.allowedSubs.push(DBRef(USER_COLLECTION_NAME, allUsers[randomInt(0, allUsers.length)]._id))
    }

    newQuestion.attachmentIds = []
    attachAmount = randomInt(0, allAttachments.length)
    for (let j = 0; j < attachAmount; ++j) {
        newQuestion.attachmentIds.push(allAttachments[randomInt(0, allAttachments.length)]._id.str)
    }

    db.getCollection(QUESTION_COLLECTION_NAME).insert(newQuestion)
}


db.getCollection(QUESTION_COLLECTION_NAME).createIndex({"title": 1, "authorId": 1}, {"unique": true});
db.getCollection(QUESTION_COLLECTION_NAME).createIndex({"_fts": "text", "_ftsx": 1},
    {
        "name": "Question_TextIndex",
        "weights": {
            "text": 1
        }
    });
allQuestions = db.getCollection(QUESTION_COLLECTION_NAME).find({}).toArray()

//create Answers
allQuestions.forEach(function (question, i, arr) {
    for (let j = 0; j < N; ++j) {
        newAnswer = {}
        newAnswer._class = "com.epam.lstrsum.model.Answer";
        newAnswer.questionId = DBRef(QUESTION_COLLECTION_NAME, question._id)
        newAnswer.text = randomString(randomInt(MINIMUM_TEXT_SIZE, MAXIMUM_TEXT_SIZE))
        newAnswer.createdAt = randomISODate()

        newAnswer.authorId = DBRef(USER_COLLECTION_NAME, allUsers[randomInt(0, allUsers.length)]._id)
        newAnswer.upVote = randomInt(0, N)

        db.getCollection(ANSWER_COLLECTION_NAME).insert(newAnswer)
    }
})

db.getCollection(ANSWER_COLLECTION_NAME).createIndex({"questionId": 1});
allAnswers = db.getCollection(ANSWER_COLLECTION_NAME).find().toArray()

//create Subscriptions
for (let i = 0; i < N; ++i) {
    newSubscription = {}
    newSubscription._class = "com.epam.lstrsum.model.Subscription";
    newSubscription.userId = DBRef(USER_COLLECTION_NAME, allUsers[randomInt(0, allUsers.length)]._id)
    newSubscription.questionIds = []

    db.getCollection(SUBSCRIPTION_COLLECTION_NAME).insert(newSubscription)
}

db.getCollection(SUBSCRIPTION_COLLECTION_NAME).createIndex({"questionIds": 1});
allSubs = db.getCollection(SUBSCRIPTION_COLLECTION_NAME).find({}).toArray()

allQuestions.forEach(function (question, i, arr) {
    currentAllowedSubs = question.allowedSubs
    subscriptionAmountForCurrentQuestion = randomInt(0, currentAllowedSubs.length)

    for (let j = 0; j < subscriptionAmountForCurrentQuestion; ++j) {
        userForSubscribe = currentAllowedSubs[randomInt(0, currentAllowedSubs.length)]

        db.getCollection(SUBSCRIPTION_COLLECTION_NAME).findAndModify({
            query: {userId: userForSubscribe},
            update: {$addToSet: {questionIds: DBRef(QUESTION_COLLECTION_NAME, question._id)}}
        })
    }
})
