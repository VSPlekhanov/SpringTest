/*
* for running scripts use
*  mongo <host>:<port>/ExperienceDataBase .etc/mongo/init-test-data.js
* init-test-data.js generates random data except users.
* It takes existing users and uses them instead of randomly generated users.
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

/*ATTENTION!!!!
* This number response for how many documents will in db after script
*/
const QUESTIONS_AMOUNT_MAX = 1000
const ANSWER_ON_QUESTION_AMOUNT_MIN = 0
const ANSWER_ON_QUESTION_AMOUNT_MAX = 20
/**/

const USER_COLLECTION_NAME = "User"
const ATTACHMENT_COLLECTION_NAME = "Attachment"
const QUESTION_COLLECTION_NAME = "Question"
const SUBSCRIPTION_COLLECTION_NAME = "Subscription"
const LITTLE_STRING_LENGTH = 50
const MAXIMUM_TAGS_AMOUNT = 20
const MINIMUM_TAGS_AMOUNT = 5
const MINIMUM_TEXT_SIZE = 100
const MAXIMUM_TEXT_SIZE = 2000
const GENERAL_CHARS = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ      '

collectionNames = db.getCollectionNames()

//clear Question, Answer and Subscription collections
db.getCollection(QUESTION_COLLECTION_NAME).remove({})
db.getCollection(SUBSCRIPTION_COLLECTION_NAME).remove({})

//if the index already exist, this will take no effect
db.getCollection(USER_COLLECTION_NAME).createIndex({"email": 1}, {"unique": true});
allUsers = db.getCollection(USER_COLLECTION_NAME).find().toArray()

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
    if (!newQuestion.allowedSubs.includes(newQuestion.authorId)) newQuestion.allowedSubs.push(newQuestion.authorId)

    newQuestion.attachmentIds = []

    // create some answers on a question
    newQuestion.answers = []
    for (let j = 0, n = randomInt(ANSWER_ON_QUESTION_AMOUNT_MIN, ANSWER_ON_QUESTION_AMOUNT_MAX); j < n; ++j) {
        newAnswer = {}
        newAnswer.answerId = ObjectId().str
        newAnswer.text = randomString(randomInt(MINIMUM_TEXT_SIZE, MAXIMUM_TEXT_SIZE), GENERAL_CHARS)
        newAnswer.createdAt = randomISODate()
        newAnswer.authorId = allUsers[randomInt(0, allUsers.length)]._id.str
        newAnswer.votes = []

        newQuestion.answers.push(newAnswer)
    }

    newQuestion.subscribers = [].concat(newQuestion.allowedSubs)

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