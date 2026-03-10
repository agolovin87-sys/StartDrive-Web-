import com.example.startdrive.shared.model.User
import firebase.ChatMessage
import firebase.DrivingSession
import firebase.InstructorOpenWindow
import firebase.BalanceHistoryEntry

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
    var recordingOpenWindows: List<InstructorOpenWindow> = emptyList(),
    var recordingSessions: List<DrivingSession> = emptyList(),
    var recordingLoading: Boolean = false,
    var historySessions: List<DrivingSession> = emptyList(),
    var historyBalance: List<BalanceHistoryEntry> = emptyList(),
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
)

var appState = AppState()

var onStateChanged: (() -> Unit)? = null

fun updateState(block: AppState.() -> Unit) {
    appState.block()
    onStateChanged?.invoke()
}
