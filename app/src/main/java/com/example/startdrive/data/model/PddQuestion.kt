package com.example.startdrive.data.model

/**
 * Вопрос из экзаменационного билета ПДД.
 */
data class PddQuestion(
    val id: String,
    val title: String,
    val ticketNumber: String,
    val ticketCategory: String,
    val imagePath: String?, // путь в assets, например pdd/images/A_B/xxx.jpg
    val question: String,
    val answers: List<PddAnswer>,
    val correctAnswer: String,
    val answerTip: String,
    val topic: List<String>,
)

data class PddAnswer(
    val answerText: String,
    val isCorrect: Boolean,
)
