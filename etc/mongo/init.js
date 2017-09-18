/*
* for running scripts use
*  mongo <host>:<port> mongo.init/init.js
*/

function randomString(length, chars) {
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

function randomEpamEmail() {
    const userNameMinLength = 2
    const userNameMaxLength = 15
    const epamDomainName = '@epam.com'
    result = ''
    result += randomString(randomInt(userNameMinLength, userNameMaxLength), EMAIL_CHARS)
    result += '_';
    result += randomString(randomInt(userNameMinLength, userNameMaxLength), EMAIL_CHARS)
    result += epamDomainName;
    return result
}

/*ATTENTION!!!!
* This number response for how many documents will in db after script
*/
const USERS_AMOUNT_MIN = 50
const USERS_AMOUNT_MAX = 2000
const QUESTIONS_AMOUNT_MIN = 500
const QUESTIONS_AMOUNT_MAX = 1000
const ANSWER_ON_QUESTION_AMOUNT_MIN = 0
const ANSWER_ON_QUESTION_AMOUNT_MAX = 20
const ATTACHMENT_ON_QUESTION_CHAR_AMOUNT_MIN = 102400 / 2  //102400 byte -> 100kb, assume that UTF-16 -> 2 bytes per chars
const ATTACHMENT_ON_QUESTION_CHAR_AMOUNT_MAX = 16777216 / 2 // 16777216 byte -> 16mb, assume that UTF-16 -> 2 bytes per chars
/**/

const USER_COLLECTION_NAME = "User"
const ATTACHMENT_COLLECTION_NAME = "Attachment"
const QUESTION_COLLECTION_NAME = "Question"
const ANSWER_COLLECTION_NAME = "Answer"
const SUBSCRIPTION_COLLECTION_NAME = "Subscription"
const LITTLE_STRING_LENGTH = 50
const MAXIMUM_TAGS_AMOUNT = 20
const MINIMUM_TAGS_AMOUNT = 5
const MINIMUM_TEXT_SIZE = 100
const MAXIMUM_TEXT_SIZE = 2000
const ALL_ROLES = ["ROLE_ADMIN", "ROLE_SIMPLE_USER", "ROLE_EXTENDED_USER", "ROLE_NOT_ALLOWED_USER"]
const GENERAL_CHARS = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ      '
const EMAIL_CHARS = 'abcdefghijklmnopqrstuvwxyz'

collectionNames = db.getCollectionNames()

//clear collections
for (let i = 0; i < collectionNames.length; ++i) {
    db.getCollection(collectionNames[i]).remove({})
}

//create Users
for (let i = 0; i < USERS_AMOUNT_MAX; ++i) {
    newUser = {}
    newUser._class = "com.epam.lstrsum.model.User";
    newUser.firstName = randomString(LITTLE_STRING_LENGTH, GENERAL_CHARS)
    newUser.lastName = randomString(LITTLE_STRING_LENGTH, GENERAL_CHARS)
    newUser.email = randomEpamEmail()
    newUser.isActive = true
    newUser.roles = []
    newUser.createdAt = randomISODate()
    roleAmount = randomInt(0, ALL_ROLES.length)

    for (let j = 0; j < roleAmount; ++j) {
        newUser.roles.push(ALL_ROLES[randomInt(0, ALL_ROLES.length)])
    }

    db.getCollection(USER_COLLECTION_NAME).insert(newUser)
}

db.User.insert({
    "_class": "com.epam.lstrsum.model.User",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john_doe@epam.com",
    "roles": [
        "ROLE_EXTENDED_USER"
    ],
    "createdAt": ISODate("2017-09-04T10:49:52.265Z"),
    "isActive": true
})

db.getCollection(USER_COLLECTION_NAME).createIndex({"email": 1}, {"unique": true});
allUsers = db.getCollection(USER_COLLECTION_NAME).find().toArray()

//create Attachments
for (let i = 0; i < Math.floor(QUESTIONS_AMOUNT_MAX / 10); ++i) {
    newAttachment = {}
    newAttachment._class = "com.epam.lstrsum.model.Attachment";
    newAttachment.name = randomString(LITTLE_STRING_LENGTH, GENERAL_CHARS)
    newAttachment.type = randomString(LITTLE_STRING_LENGTH, GENERAL_CHARS)
    newAttachment.data = randomString(randomInt(ATTACHMENT_ON_QUESTION_CHAR_AMOUNT_MIN, ATTACHMENT_ON_QUESTION_CHAR_AMOUNT_MAX), GENERAL_CHARS)

    db.getCollection(ATTACHMENT_COLLECTION_NAME).insert(newAttachment)
}

allAttachments = db.getCollection(ATTACHMENT_COLLECTION_NAME).find({}).toArray()

//create Questions
for (let i = 0; i < QUESTIONS_AMOUNT_MAX; ++i) {
    newQuestion = {}
    newQuestion._class = "com.epam.lstrsum.model.Question";
    newQuestion.title = randomString(LITTLE_STRING_LENGTH, GENERAL_CHARS)
    newQuestion.tags = []
    newQuestion.createdAt = randomISODate()
    newQuestion.deadLine = randomISODate()

    tagsAmount = randomInt(MINIMUM_TAGS_AMOUNT, MAXIMUM_TAGS_AMOUNT)
    for (let j = 0; j < tagsAmount; ++j) {
        newQuestion.tags.push(randomString(LITTLE_STRING_LENGTH, GENERAL_CHARS))
    }

    newQuestion.text = randomString(randomInt(MINIMUM_TEXT_SIZE, MAXIMUM_TEXT_SIZE), GENERAL_CHARS)
    newQuestion.authorId = DBRef(USER_COLLECTION_NAME, allUsers[randomInt(0, allUsers.length)]._id)

    newQuestion.allowedSubs = []
    subsAmount = randomInt(0, allUsers.length)
    for (let j = 0; j < subsAmount; ++j) {
        newQuestion.allowedSubs.push(DBRef(USER_COLLECTION_NAME, allUsers[randomInt(0, allUsers.length)]._id))
    }

    newQuestion.attachmentIds = []
    if (i % 10 == 0) {
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
    for (let j = 0; j < randomInt(ANSWER_ON_QUESTION_AMOUNT_MIN, ANSWER_ON_QUESTION_AMOUNT_MAX); ++j) {
        newAnswer = {}
        newAnswer._class = "com.epam.lstrsum.model.Answer";
        newAnswer.questionId = DBRef(QUESTION_COLLECTION_NAME, question._id)
        newAnswer.text = randomString(randomInt(MINIMUM_TEXT_SIZE, MAXIMUM_TEXT_SIZE), GENERAL_CHARS)
        newAnswer.createdAt = randomISODate()

        newAnswer.authorId = DBRef(USER_COLLECTION_NAME, allUsers[randomInt(0, allUsers.length)]._id)
        newAnswer.votes = []

        db.getCollection(ANSWER_COLLECTION_NAME).insert(newAnswer)
    }
})

db.getCollection(ANSWER_COLLECTION_NAME).createIndex({"questionId": 1});
allAnswers = db.getCollection(ANSWER_COLLECTION_NAME).find().toArray()

//create Subscriptions
for (let i = 0; i < USERS_AMOUNT_MAX; ++i) {
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
