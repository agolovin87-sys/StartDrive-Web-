package com.example.startdrive.data.repository

import android.content.Context
import com.example.startdrive.data.model.PddAnswer
import com.example.startdrive.data.model.PddQuestion
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader

data class PddCategory(val id: String, val title: String)

/**
 * Загрузка экзаменационных билетов ПДД из assets (папка pdd из pdd_russia-master).
 * Категории: A_B (категории A,B), C_D (категории C,D).
 */
object PddRepository {
    private const val ASSET_BASE = "pdd"
    private const val TICKETS_DIR = "$ASSET_BASE/tickets"
    private const val IMAGES_BASE = "$ASSET_BASE/images"

    private const val SIGNS_DIR = "$ASSET_BASE/signs"
    private const val MARKUP_DIR = "$ASSET_BASE/markup"
    private const val PENALTIES_DIR = "$ASSET_BASE/penalties"

    /** Категории: билеты AB, CD, вопросы по разделам, знаки, разметка, штрафы */
    fun getCategories(): List<PddCategory> = listOf(
        PddCategory(id = "A_B", title = "Категория AB"),
        PddCategory(id = "C_D", title = "Категория CD"),
        PddCategory(id = "by_topic", title = "Вопросы по разделам"),
        PddCategory(id = "signs", title = "Дорожные знаки"),
        PddCategory(id = "markup", title = "Дорожная разметка"),
        PddCategory(id = "penalties", title = "Штрафы"),
    )

    /** Номера билетов 1..40 для выбранной категории (только A_B, C_D) */
    fun getTicketNumbers(categoryId: String): List<String> =
        if (categoryId in listOf("A_B", "C_D")) (1..40).map { "Билет $it" } else emptyList()

    /** Секция вопросов по теме (раздел). Используется для категории «Вопросы по разделам». */
    data class TopicSection(val name: String, val questions: List<PddQuestion>)

    /**
     * Загрузить все вопросы по разделам (темам) для категории A_B или C_D.
     * Читает все 40 билетов, объединяет вопросы и группирует по первой теме.
     */
    fun loadQuestionsByTopic(context: Context, categoryId: String): List<TopicSection> {
        val allQuestions = mutableListOf<PddQuestion>()
        val ticketNumbers = (1..40).map { "Билет $it" }
        for (ticketName in ticketNumbers) {
            allQuestions.addAll(loadTicket(context, categoryId, ticketName))
        }
        val order = mutableListOf<String>()
        val byTopic = mutableMapOf<String, MutableList<PddQuestion>>()
        for (q in allQuestions) {
            val topic = q.topic.firstOrNull()?.takeIf { it.isNotBlank() } ?: "Прочее"
            if (topic !in order) order.add(topic)
            byTopic.getOrPut(topic) { mutableListOf() }.add(q)
        }
        return order.map { name -> TopicSection(name = name, questions = byTopic[name] ?: emptyList()) }
    }

    /** Загрузить вопросы билета по категории и номеру (например categoryId "A_B", ticketName "Билет 1"). */
    fun loadTicket(context: Context, categoryId: String, ticketName: String): List<PddQuestion> {
        val fileName = "$ticketName.json"
        val path = when (categoryId) {
            "A_B" -> "$TICKETS_DIR/$fileName"  // билеты AB в корне tickets/
            else -> "$TICKETS_DIR/$categoryId/$fileName"  // C_D в tickets/C_D/
        }
        return try {
            context.assets.open(path).use { stream ->
                val json = InputStreamReader(stream, Charsets.UTF_8).readText()
                parseTicketJson(json)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseTicketJson(json: String): List<PddQuestion> {
        val arr = JSONArray(json)
        val list = mutableListOf<PddQuestion>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val imageRel = obj.optString("image", "").trim()
            val imagePath = when {
                imageRel.isEmpty() || imageRel.endsWith("no_image.jpg") -> null
                imageRel.startsWith("./") -> IMAGES_BASE + imageRel.removePrefix("./images").replace("\\", "/")
                else -> "$IMAGES_BASE/$imageRel"
            }?.takeIf { it != IMAGES_BASE }
            val answersArr = obj.getJSONArray("answers")
            val answers = mutableListOf<PddAnswer>()
            for (j in 0 until answersArr.length()) {
                val a = answersArr.getJSONObject(j)
                answers.add(PddAnswer(
                    answerText = a.optString("answer_text", ""),
                    isCorrect = a.optBoolean("is_correct", false),
                ))
            }
            val topicArr = obj.optJSONArray("topic")
            val topic = if (topicArr != null) (0 until topicArr.length()).map { topicArr.getString(it) } else emptyList()
            list.add(PddQuestion(
                id = obj.optString("id", ""),
                title = obj.optString("title", ""),
                ticketNumber = obj.optString("ticket_number", ""),
                ticketCategory = obj.optString("ticket_category", ""),
                imagePath = imagePath,
                question = obj.optString("question", ""),
                answers = answers,
                correctAnswer = obj.optString("correct_answer", ""),
                answerTip = obj.optString("answer_tip", ""),
                topic = topic,
            ))
        }
        return list
    }

    /** Секция дорожных знаков: название + список знаков */
    data class SignsSection(val name: String, val items: List<PddSignItem>)
    data class PddSignItem(val number: String, val title: String, val imagePath: String, val description: String)

    fun loadSigns(context: Context): List<SignsSection> = try {
        context.assets.open("$SIGNS_DIR/signs.json").use { stream ->
            val json = InputStreamReader(stream, Charsets.UTF_8).readText()
            parseSignsJson(json)
        }
    } catch (e: Exception) {
        emptyList()
    }

    private fun parseSignsJson(json: String): List<SignsSection> {
        val root = JSONObject(json)
        val list = mutableListOf<SignsSection>()
        for (key in root.keys()) {
            val sectionName = key
            val sectionObj = root.getJSONObject(key)
            val items = mutableListOf<PddSignItem>()
            for (num in sectionObj.keys()) {
                val obj = sectionObj.getJSONObject(num)
                val imageRel = obj.optString("image", "").trim()
                val imagePath = when {
                    imageRel.isEmpty() -> ""
                    imageRel.startsWith("./") -> IMAGES_BASE + imageRel.removePrefix("./images").replace("\\", "/")
                    else -> "$IMAGES_BASE/$imageRel"
                }
                items.add(PddSignItem(
                    number = obj.optString("number", num),
                    title = obj.optString("title", ""),
                    imagePath = imagePath,
                    description = obj.optString("description", ""),
                ))
            }
            list.add(SignsSection(name = sectionName, items = items))
        }
        return list
    }

    /** Секция разметки: название + список элементов */
    data class MarkupSection(val name: String, val items: List<PddMarkupItem>)
    data class PddMarkupItem(val number: String, val imagePath: String, val description: String)

    fun loadMarkup(context: Context): List<MarkupSection> = try {
        context.assets.open("$MARKUP_DIR/markup.json").use { stream ->
            val json = InputStreamReader(stream, Charsets.UTF_8).readText()
            parseMarkupJson(json)
        }
    } catch (e: Exception) {
        emptyList()
    }

    private fun parseMarkupJson(json: String): List<MarkupSection> {
        val root = JSONObject(json)
        val list = mutableListOf<MarkupSection>()
        for (key in root.keys()) {
            val sectionName = key
            val sectionObj = root.getJSONObject(key)
            val items = mutableListOf<PddMarkupItem>()
            for (num in sectionObj.keys()) {
                val obj = sectionObj.getJSONObject(num)
                val imageRel = obj.optString("image", "").trim()
                val imagePath = when {
                    imageRel.isEmpty() -> ""
                    imageRel.startsWith("./") -> IMAGES_BASE + imageRel.removePrefix("./images").replace("\\", "/")
                    else -> "$IMAGES_BASE/$imageRel"
                }
                items.add(PddMarkupItem(
                    number = obj.optString("number", num),
                    imagePath = imagePath,
                    description = obj.optString("description", ""),
                ))
            }
            list.add(MarkupSection(name = sectionName, items = items))
        }
        return list
    }

    /** Элемент каталога штрафов КоАП/УК РФ */
    data class PenaltyItem(val articlePart: String, val text: String, val penalty: String)

    /** Загрузить список штрафов (файл penalties.json — по одной строке JSON на запись). */
    fun loadPenalties(context: Context): List<PenaltyItem> = try {
        context.assets.open("$PENALTIES_DIR/penalties.json").use { stream ->
            InputStreamReader(stream, Charsets.UTF_8).readText()
                .lineSequence()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    try {
                        val obj = JSONObject(line.trim())
                        PenaltyItem(
                            articlePart = obj.optString("article_part", ""),
                            text = obj.optString("text", ""),
                            penalty = obj.optString("penalty", ""),
                        )
                    } catch (_: Exception) { null }
                }
                .toList()
        }
    } catch (e: Exception) {
        emptyList()
    }
}
