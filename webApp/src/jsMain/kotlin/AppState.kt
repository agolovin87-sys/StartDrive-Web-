import com.example.startdrive.shared.model.User
import firebase.ChatMessage
import firebase.DrivingSession
import firebase.InstructorOpenWindow
import firebase.BalanceHistoryEntry

/** Вопрос билета ПДД (как в Android PddQuestion). */
data class PddQuestion(
    val id: String,
    val title: String,
    val ticketNumber: String,
    val ticketCategory: String,
    val image: String?,
    val question: String,
    val answers: List<PddAnswer>,
    val correctAnswer: String,
    val answerTip: String,
    val topic: List<String>,
)

/** Вариант ответа на вопрос ПДД. */
data class PddAnswer(val answerText: String, val isCorrect: Boolean)

/** Секция дорожных знаков (как в Android PddRepository.SignsSection). */
data class PddSignsSection(val name: String, val items: List<PddSignItem>)
data class PddSignItem(val number: String, val title: String, val imagePath: String, val description: String)

/** Секция разметки (как в Android PddRepository.MarkupSection). */
data class PddMarkupSection(val name: String, val items: List<PddMarkupItem>)
data class PddMarkupItem(val number: String, val imagePath: String, val description: String)

/** Элемент штрафов (как в Android PddRepository.PenaltyItem). */
data class PddPenaltyItem(val articlePart: String, val text: String, val penalty: String)

/** Вопросы по теме (для категории «Вопросы по разделам»). */
data class PddTopicSection(val name: String, val questions: List<PddQuestion>)

enum class AppScreen {
    Login,
    Register,
    PendingApproval,
    ProfileNotFound,
    Admin,
    Instructor,
    Cadet,
}

data class AppState(
    var screen: AppScreen = AppScreen.Login,
    var user: User? = null,
    var error: String? = null,
    var loading: Boolean = false,
    var networkError: String? = null,
    var selectedTabIndex: Int = 0,
    var chatContacts: List<User> = emptyList(),
    var chatContactOnlineIds: Set<String> = emptySet(),
    var chatContactsLoading: Boolean = false,
    var selectedChatContactId: String? = null,
    var chatMessages: List<ChatMessage> = emptyList(),
    var chatUnreadCounts: Map<String, Int> = emptyMap(),
    /** Показывать аватары других пользователей в чате (настройка из Firebase: app_config/chat_show_other_avatars). */
    var chatShowOtherAvatars: Boolean = true,
    var recordingOpenWindows: List<InstructorOpenWindow> = emptyList(),
    var recordingSessions: List<DrivingSession> = emptyList(),
    var recordingLoading: Boolean = false,
    var historySessions: List<DrivingSession> = emptyList(),
    var historyBalance: List<BalanceHistoryEntry> = emptyList(),
    var historyUsers: List<User> = emptyList(),
    var historyLoading: Boolean = false,
    var balanceAdminHistory: List<BalanceHistoryEntry> = emptyList(),
    var balanceAdminUsers: List<User> = emptyList(),
    var balanceAdminLoading: Boolean = false,
    var balanceAdminSelectedUserId: String? = null,
    var adminHomeUsers: List<User> = emptyList(),
    var adminHomeLoading: Boolean = false,
    var adminNewbiesSectionOpen: Boolean = true,
    var adminInstructorsSectionOpen: Boolean = true,
    var adminCadetsSectionOpen: Boolean = true,
    var balanceHistorySectionOpen: Boolean = false,
    var instructorMyCadetsSectionOpen: Boolean = true,
    var adminAssignInstructorId: String? = null,
    var adminAssignCadetId: String? = null,
    var adminInstructorCadetsModalId: String? = null,
    var cadetInstructor: User? = null,
    var instructorCadets: List<User> = emptyList(),
    var chatVoiceRecording: Boolean = false,
    var chatVoiceRecordStartMs: Double = 0.0,
    var chatVoiceRecordElapsedSec: Int = 0,
    var chatPlayingVoiceId: String? = null,
    var chatPlayingVoiceCurrentMs: Int = 0,
    var pddCategoryId: String? = null,
    var pddTicketName: String? = null,
    var pddQuestions: List<PddQuestion> = emptyList(),
    var pddCurrentIndex: Int = 0,
    var pddUserSelections: Map<Int, Int> = emptyMap(),
    var pddFinished: Boolean = false,
    var pddLoading: Boolean = false,
    var pddSignsSections: List<PddSignsSection> = emptyList(),
    var pddSelectedSign: PddSignItem? = null, // выбранный знак для просмотра по отдельности
    var pddSelectedSignSectionIndex: Int = -1,
    var pddSelectedSignItemIndex: Int = -1,
    var pddScrollToSignDetail: Boolean = false, // после перехода «к следующему знаку» прокрутить к началу пояснения
    var pddMarkupSections: List<PddMarkupSection> = emptyList(),
    var pddPenalties: List<PddPenaltyItem> = emptyList(),
    var pddByTopicSections: List<PddTopicSection> = emptyList(),
    var pddTicketsBundle: dynamic = null, // { A_B: { "1": [...], ... }, C_D: { "1": [...], ... } } — загружается один раз
    var pddStatsVersion: Int = 0, // увеличиваем при обнулении статистики для перерисовки списка
    var pddResetConfirmCategory: String? = null, // показывать модальное подтверждение обнуления (без localhost в тексте)
    // Экзамен как в ГИБДД
    var pddExamMode: Boolean = false,
    var pddExamCategoryForBundle: String? = null, // A_B или C_D для бандла
    var pddExamStartTimeMs: Double? = null,
    var pddExamBlockIndices: List<Int> = emptyList(), // блок 0..3 для каждого из 20 вопросов
    var pddExamPhase: String = "main", // main | additional | result
    var pddExamAdditionalQuestions: List<PddQuestion> = emptyList(),
    var pddExamAdditionalBlockIndices: List<Int> = emptyList(),
    var pddExamAdditionalCurrentIndex: Int = 0,
    var pddExamAdditionalUserSelections: Map<Int, Int> = emptyMap(),
    var pddExamAdditionalStartTimeMs: Double? = null,
    var pddExamAdditionalDurationSec: Int = 300, // 5 мин на 5 вопросов, 10 мин на 10
    var pddExamResultPass: Boolean? = null,
    /** Список уведомлений: дата, время, текст (сохраняются при действиях). */
    var notifications: List<AppNotification> = emptyList(),
    /** Сколько уведомлений было при последнем входе во вкладку — счётчик бейджа обнуляется при входе. */
    var notificationsReadCount: Int = 0,
    /** Открыт ли полноэкранный экран уведомлений по кнопке в шапке. */
    var notificationsViewOpen: Boolean = false,
    /** Количество пунктов «Запись» (сессии/окна) при последнем входе во вкладку — бейдж вкладки обнуляется при входе. */
    var recordingTabBadgeBaseline: Int = 0,
    /** Показать ли окно настроек звуковых уведомлений (при первом запуске курсанта). */
    var showSoundSettingsModal: Boolean = false,
    /** Пользователь дал согласие на воспроизведение звука (нажал «Разрешить звук» или «Включить» в настройках). */
    var soundAudioUnlocked: Boolean = false,
    /** До какого времени (мс) кнопка «Опаздываю» неактивна после выбора задержки. */
    var instructorRunningLateUntilMs: Long = 0L,
)

/** Одно уведомление: дата, время, текст. */
data class AppNotification(
    val dateTimeMs: Long,
    val text: String,
)

var appState = AppState()

var onStateChanged: (() -> Unit)? = null

fun updateState(block: AppState.() -> Unit) {
    appState.block()
    onStateChanged?.invoke()
}
