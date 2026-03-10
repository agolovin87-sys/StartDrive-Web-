(function (factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports', './kotlin-kotlin-stdlib.js', './StartDrive-shared.js'], factory);
  else if (typeof exports === 'object')
    factory(module.exports, require('./kotlin-kotlin-stdlib.js'), require('./StartDrive-shared.js'));
  else {
    if (typeof globalThis['kotlin-kotlin-stdlib'] === 'undefined') {
      throw new Error("Error loading module 'StartDrive:webApp'. Its dependency 'kotlin-kotlin-stdlib' was not found. Please, check whether 'kotlin-kotlin-stdlib' is loaded prior to 'StartDrive:webApp'.");
    }
    if (typeof globalThis['StartDrive-shared'] === 'undefined') {
      throw new Error("Error loading module 'StartDrive:webApp'. Its dependency 'StartDrive-shared' was not found. Please, check whether 'StartDrive-shared' is loaded prior to 'StartDrive:webApp'.");
    }
    globalThis['StartDrive:webApp'] = factory(typeof globalThis['StartDrive:webApp'] === 'undefined' ? {} : globalThis['StartDrive:webApp'], globalThis['kotlin-kotlin-stdlib'], globalThis['StartDrive-shared']);
  }
}(function (_, kotlin_kotlin, kotlin_StartDrive_shared) {
  'use strict';
  //region block: imports
  var imul = Math.imul;
  var THROW_IAE = kotlin_kotlin.$_$.c8;
  var enumEntries = kotlin_kotlin.$_$.m5;
  var Unit_getInstance = kotlin_kotlin.$_$.e3;
  var Enum = kotlin_kotlin.$_$.w7;
  var protoOf = kotlin_kotlin.$_$.w6;
  var initMetadataForClass = kotlin_kotlin.$_$.y5;
  var VOID = kotlin_kotlin.$_$.e;
  var emptyList = kotlin_kotlin.$_$.l4;
  var emptySet = kotlin_kotlin.$_$.n4;
  var toString = kotlin_kotlin.$_$.s8;
  var toString_0 = kotlin_kotlin.$_$.z6;
  var getStringHashCode = kotlin_kotlin.$_$.w5;
  var getBooleanHashCode = kotlin_kotlin.$_$.u5;
  var hashCode = kotlin_kotlin.$_$.x5;
  var THROW_CCE = kotlin_kotlin.$_$.b8;
  var equals = kotlin_kotlin.$_$.t5;
  var trimIndent = kotlin_kotlin.$_$.p7;
  var joinToString = kotlin_kotlin.$_$.s4;
  var ArrayList_init_$Create$ = kotlin_kotlin.$_$.h;
  var sortedWith = kotlin_kotlin.$_$.b5;
  var listOf = kotlin_kotlin.$_$.w4;
  var Long = kotlin_kotlin.$_$.y7;
  var _Char___init__impl__6a9atx = kotlin_kotlin.$_$.b1;
  var padStart = kotlin_kotlin.$_$.j7;
  var isCharSequence = kotlin_kotlin.$_$.g6;
  var trim = kotlin_kotlin.$_$.q7;
  var split = kotlin_kotlin.$_$.k7;
  var isBlank = kotlin_kotlin.$_$.i7;
  var drop = kotlin_kotlin.$_$.k4;
  var collectionSizeOrDefault = kotlin_kotlin.$_$.x3;
  var ArrayList_init_$Create$_0 = kotlin_kotlin.$_$.g;
  var firstOrNull = kotlin_kotlin.$_$.h7;
  var toString_1 = kotlin_kotlin.$_$.d1;
  var Char = kotlin_kotlin.$_$.u7;
  var take = kotlin_kotlin.$_$.d5;
  var LinkedHashMap_init_$Create$ = kotlin_kotlin.$_$.p;
  var substringBefore = kotlin_kotlin.$_$.m7;
  var SharedFactory_getInstance = kotlin_StartDrive_shared.$_$.b;
  var coerceIn = kotlin_kotlin.$_$.b7;
  var checkIndexOverflow = kotlin_kotlin.$_$.w3;
  var Pair = kotlin_kotlin.$_$.z7;
  var noWhenBranchMatchedException = kotlin_kotlin.$_$.r8;
  var FunctionAdapter = kotlin_kotlin.$_$.n5;
  var isInterface = kotlin_kotlin.$_$.k6;
  var Comparator = kotlin_kotlin.$_$.v7;
  var charSequenceLength = kotlin_kotlin.$_$.s5;
  var toIntOrNull = kotlin_kotlin.$_$.o7;
  var toMutableSet = kotlin_kotlin.$_$.j5;
  var ensureNotNull = kotlin_kotlin.$_$.o8;
  var compareValues = kotlin_kotlin.$_$.l5;
  var Collection = kotlin_kotlin.$_$.g3;
  var checkCountOverflow = kotlin_kotlin.$_$.v3;
  var take_0 = kotlin_kotlin.$_$.n7;
  var substringAfter = kotlin_kotlin.$_$.l7;
  var numberToLong = kotlin_kotlin.$_$.u6;
  var sorted = kotlin_kotlin.$_$.c5;
  var to = kotlin_kotlin.$_$.t8;
  var json = kotlin_kotlin.$_$.p6;
  var isNumber = kotlin_kotlin.$_$.m6;
  var sortWith = kotlin_kotlin.$_$.a5;
  var toLong = kotlin_kotlin.$_$.y6;
  var until = kotlin_kotlin.$_$.d7;
  var numberToInt = kotlin_kotlin.$_$.t6;
  var coerceAtLeast = kotlin_kotlin.$_$.a7;
  var isArray = kotlin_kotlin.$_$.c6;
  var User = kotlin_StartDrive_shared.$_$.a;
  var toList = kotlin_kotlin.$_$.g5;
  var toMutableList = kotlin_kotlin.$_$.i5;
  var copyToArray = kotlin_kotlin.$_$.i4;
  var listOfNotNull = kotlin_kotlin.$_$.t4;
  var plus = kotlin_kotlin.$_$.y4;
  var listOfNotNull_0 = kotlin_kotlin.$_$.u4;
  var distinct = kotlin_kotlin.$_$.j4;
  //endregion
  //region block: pre-declaration
  initMetadataForClass(AppScreen, 'AppScreen', VOID, Enum);
  initMetadataForClass(AppState, 'AppState', AppState);
  initMetadataForClass(sam$kotlin_Comparator$0, 'sam$kotlin_Comparator$0', VOID, VOID, [Comparator, FunctionAdapter]);
  initMetadataForClass(sam$kotlin_Comparator$0_0, 'sam$kotlin_Comparator$0', VOID, VOID, [Comparator, FunctionAdapter]);
  initMetadataForClass(sam$kotlin_Comparator$0_1, 'sam$kotlin_Comparator$0', VOID, VOID, [Comparator, FunctionAdapter]);
  initMetadataForClass(sam$kotlin_Comparator$0_2, 'sam$kotlin_Comparator$0', VOID, VOID, [Comparator, FunctionAdapter]);
  initMetadataForClass(ChatMessage, 'ChatMessage', ChatMessage);
  initMetadataForClass(sam$kotlin_Comparator$0_3, 'sam$kotlin_Comparator$0', VOID, VOID, [Comparator, FunctionAdapter]);
  initMetadataForClass(DrivingSession, 'DrivingSession', DrivingSession);
  initMetadataForClass(InstructorOpenWindow, 'InstructorOpenWindow', InstructorOpenWindow);
  initMetadataForClass(BalanceHistoryEntry, 'BalanceHistoryEntry', BalanceHistoryEntry);
  initMetadataForClass(sam$kotlin_Comparator$0_4, 'sam$kotlin_Comparator$0', VOID, VOID, [Comparator, FunctionAdapter]);
  //endregion
  function set_appState(_set____db54di) {
    _init_properties_AppState_kt__bzmygg();
    appState = _set____db54di;
  }
  function get_appState() {
    _init_properties_AppState_kt__bzmygg();
    return appState;
  }
  var appState;
  function set_onStateChanged(_set____db54di) {
    _init_properties_AppState_kt__bzmygg();
    onStateChanged = _set____db54di;
  }
  function get_onStateChanged() {
    _init_properties_AppState_kt__bzmygg();
    return onStateChanged;
  }
  var onStateChanged;
  var AppScreen_Login_instance;
  var AppScreen_Register_instance;
  var AppScreen_PendingApproval_instance;
  var AppScreen_ProfileNotFound_instance;
  var AppScreen_Admin_instance;
  var AppScreen_Instructor_instance;
  var AppScreen_Cadet_instance;
  function values() {
    return [AppScreen_Login_getInstance(), AppScreen_Register_getInstance(), AppScreen_PendingApproval_getInstance(), AppScreen_ProfileNotFound_getInstance(), AppScreen_Admin_getInstance(), AppScreen_Instructor_getInstance(), AppScreen_Cadet_getInstance()];
  }
  function valueOf(value) {
    switch (value) {
      case 'Login':
        return AppScreen_Login_getInstance();
      case 'Register':
        return AppScreen_Register_getInstance();
      case 'PendingApproval':
        return AppScreen_PendingApproval_getInstance();
      case 'ProfileNotFound':
        return AppScreen_ProfileNotFound_getInstance();
      case 'Admin':
        return AppScreen_Admin_getInstance();
      case 'Instructor':
        return AppScreen_Instructor_getInstance();
      case 'Cadet':
        return AppScreen_Cadet_getInstance();
      default:
        AppScreen_initEntries();
        THROW_IAE('No enum constant value.');
        break;
    }
  }
  function get_entries() {
    if ($ENTRIES == null)
      $ENTRIES = enumEntries(values());
    return $ENTRIES;
  }
  var AppScreen_entriesInitialized;
  function AppScreen_initEntries() {
    if (AppScreen_entriesInitialized)
      return Unit_getInstance();
    AppScreen_entriesInitialized = true;
    AppScreen_Login_instance = new AppScreen('Login', 0);
    AppScreen_Register_instance = new AppScreen('Register', 1);
    AppScreen_PendingApproval_instance = new AppScreen('PendingApproval', 2);
    AppScreen_ProfileNotFound_instance = new AppScreen('ProfileNotFound', 3);
    AppScreen_Admin_instance = new AppScreen('Admin', 4);
    AppScreen_Instructor_instance = new AppScreen('Instructor', 5);
    AppScreen_Cadet_instance = new AppScreen('Cadet', 6);
  }
  var $ENTRIES;
  function AppScreen(name, ordinal) {
    Enum.call(this, name, ordinal);
  }
  function AppState(screen, user, error, loading, networkError, selectedTabIndex, chatContacts, chatContactOnlineIds, chatContactsLoading, selectedChatContactId, chatMessages, recordingOpenWindows, recordingSessions, recordingLoading, historySessions, historyBalance, historyLoading, balanceAdminHistory, balanceAdminUsers, balanceAdminLoading, balanceAdminSelectedUserId, adminHomeUsers, adminHomeLoading, adminNewbiesSectionOpen, adminInstructorsSectionOpen, adminCadetsSectionOpen, balanceHistorySectionOpen, adminAssignInstructorId, adminAssignCadetId, adminInstructorCadetsModalId, cadetInstructor, instructorCadets) {
    screen = screen === VOID ? AppScreen_Login_getInstance() : screen;
    user = user === VOID ? null : user;
    error = error === VOID ? null : error;
    loading = loading === VOID ? false : loading;
    networkError = networkError === VOID ? null : networkError;
    selectedTabIndex = selectedTabIndex === VOID ? 0 : selectedTabIndex;
    chatContacts = chatContacts === VOID ? emptyList() : chatContacts;
    chatContactOnlineIds = chatContactOnlineIds === VOID ? emptySet() : chatContactOnlineIds;
    chatContactsLoading = chatContactsLoading === VOID ? false : chatContactsLoading;
    selectedChatContactId = selectedChatContactId === VOID ? null : selectedChatContactId;
    chatMessages = chatMessages === VOID ? emptyList() : chatMessages;
    recordingOpenWindows = recordingOpenWindows === VOID ? emptyList() : recordingOpenWindows;
    recordingSessions = recordingSessions === VOID ? emptyList() : recordingSessions;
    recordingLoading = recordingLoading === VOID ? false : recordingLoading;
    historySessions = historySessions === VOID ? emptyList() : historySessions;
    historyBalance = historyBalance === VOID ? emptyList() : historyBalance;
    historyLoading = historyLoading === VOID ? false : historyLoading;
    balanceAdminHistory = balanceAdminHistory === VOID ? emptyList() : balanceAdminHistory;
    balanceAdminUsers = balanceAdminUsers === VOID ? emptyList() : balanceAdminUsers;
    balanceAdminLoading = balanceAdminLoading === VOID ? false : balanceAdminLoading;
    balanceAdminSelectedUserId = balanceAdminSelectedUserId === VOID ? null : balanceAdminSelectedUserId;
    adminHomeUsers = adminHomeUsers === VOID ? emptyList() : adminHomeUsers;
    adminHomeLoading = adminHomeLoading === VOID ? false : adminHomeLoading;
    adminNewbiesSectionOpen = adminNewbiesSectionOpen === VOID ? true : adminNewbiesSectionOpen;
    adminInstructorsSectionOpen = adminInstructorsSectionOpen === VOID ? true : adminInstructorsSectionOpen;
    adminCadetsSectionOpen = adminCadetsSectionOpen === VOID ? true : adminCadetsSectionOpen;
    balanceHistorySectionOpen = balanceHistorySectionOpen === VOID ? false : balanceHistorySectionOpen;
    adminAssignInstructorId = adminAssignInstructorId === VOID ? null : adminAssignInstructorId;
    adminAssignCadetId = adminAssignCadetId === VOID ? null : adminAssignCadetId;
    adminInstructorCadetsModalId = adminInstructorCadetsModalId === VOID ? null : adminInstructorCadetsModalId;
    cadetInstructor = cadetInstructor === VOID ? null : cadetInstructor;
    instructorCadets = instructorCadets === VOID ? emptyList() : instructorCadets;
    this.screen_1 = screen;
    this.user_1 = user;
    this.error_1 = error;
    this.loading_1 = loading;
    this.networkError_1 = networkError;
    this.selectedTabIndex_1 = selectedTabIndex;
    this.chatContacts_1 = chatContacts;
    this.chatContactOnlineIds_1 = chatContactOnlineIds;
    this.chatContactsLoading_1 = chatContactsLoading;
    this.selectedChatContactId_1 = selectedChatContactId;
    this.chatMessages_1 = chatMessages;
    this.recordingOpenWindows_1 = recordingOpenWindows;
    this.recordingSessions_1 = recordingSessions;
    this.recordingLoading_1 = recordingLoading;
    this.historySessions_1 = historySessions;
    this.historyBalance_1 = historyBalance;
    this.historyLoading_1 = historyLoading;
    this.balanceAdminHistory_1 = balanceAdminHistory;
    this.balanceAdminUsers_1 = balanceAdminUsers;
    this.balanceAdminLoading_1 = balanceAdminLoading;
    this.balanceAdminSelectedUserId_1 = balanceAdminSelectedUserId;
    this.adminHomeUsers_1 = adminHomeUsers;
    this.adminHomeLoading_1 = adminHomeLoading;
    this.adminNewbiesSectionOpen_1 = adminNewbiesSectionOpen;
    this.adminInstructorsSectionOpen_1 = adminInstructorsSectionOpen;
    this.adminCadetsSectionOpen_1 = adminCadetsSectionOpen;
    this.balanceHistorySectionOpen_1 = balanceHistorySectionOpen;
    this.adminAssignInstructorId_1 = adminAssignInstructorId;
    this.adminAssignCadetId_1 = adminAssignCadetId;
    this.adminInstructorCadetsModalId_1 = adminInstructorCadetsModalId;
    this.cadetInstructor_1 = cadetInstructor;
    this.instructorCadets_1 = instructorCadets;
  }
  protoOf(AppState).set_screen_91y0x9_k$ = function (_set____db54di) {
    this.screen_1 = _set____db54di;
  };
  protoOf(AppState).get_screen_jed7jp_k$ = function () {
    return this.screen_1;
  };
  protoOf(AppState).set_user_gqmzbm_k$ = function (_set____db54di) {
    this.user_1 = _set____db54di;
  };
  protoOf(AppState).get_user_wovspg_k$ = function () {
    return this.user_1;
  };
  protoOf(AppState).set_error_5ld8to_k$ = function (_set____db54di) {
    this.error_1 = _set____db54di;
  };
  protoOf(AppState).get_error_iqzvfj_k$ = function () {
    return this.error_1;
  };
  protoOf(AppState).set_loading_7od76y_k$ = function (_set____db54di) {
    this.loading_1 = _set____db54di;
  };
  protoOf(AppState).get_loading_6tzj9v_k$ = function () {
    return this.loading_1;
  };
  protoOf(AppState).set_networkError_6qqylc_k$ = function (_set____db54di) {
    this.networkError_1 = _set____db54di;
  };
  protoOf(AppState).get_networkError_3tn6pv_k$ = function () {
    return this.networkError_1;
  };
  protoOf(AppState).set_selectedTabIndex_84igtv_k$ = function (_set____db54di) {
    this.selectedTabIndex_1 = _set____db54di;
  };
  protoOf(AppState).get_selectedTabIndex_3g78ox_k$ = function () {
    return this.selectedTabIndex_1;
  };
  protoOf(AppState).set_chatContacts_40tbaf_k$ = function (_set____db54di) {
    this.chatContacts_1 = _set____db54di;
  };
  protoOf(AppState).get_chatContacts_b21mhg_k$ = function () {
    return this.chatContacts_1;
  };
  protoOf(AppState).set_chatContactOnlineIds_1muzq_k$ = function (_set____db54di) {
    this.chatContactOnlineIds_1 = _set____db54di;
  };
  protoOf(AppState).get_chatContactOnlineIds_e5rmjq_k$ = function () {
    return this.chatContactOnlineIds_1;
  };
  protoOf(AppState).set_chatContactsLoading_kr0hj5_k$ = function (_set____db54di) {
    this.chatContactsLoading_1 = _set____db54di;
  };
  protoOf(AppState).get_chatContactsLoading_r2se4o_k$ = function () {
    return this.chatContactsLoading_1;
  };
  protoOf(AppState).set_selectedChatContactId_74xer0_k$ = function (_set____db54di) {
    this.selectedChatContactId_1 = _set____db54di;
  };
  protoOf(AppState).get_selectedChatContactId_qi8d29_k$ = function () {
    return this.selectedChatContactId_1;
  };
  protoOf(AppState).set_chatMessages_b9arcw_k$ = function (_set____db54di) {
    this.chatMessages_1 = _set____db54di;
  };
  protoOf(AppState).get_chatMessages_csrswd_k$ = function () {
    return this.chatMessages_1;
  };
  protoOf(AppState).set_recordingOpenWindows_boom0c_k$ = function (_set____db54di) {
    this.recordingOpenWindows_1 = _set____db54di;
  };
  protoOf(AppState).get_recordingOpenWindows_ecepd_k$ = function () {
    return this.recordingOpenWindows_1;
  };
  protoOf(AppState).set_recordingSessions_nfqq7s_k$ = function (_set____db54di) {
    this.recordingSessions_1 = _set____db54di;
  };
  protoOf(AppState).get_recordingSessions_veidxh_k$ = function () {
    return this.recordingSessions_1;
  };
  protoOf(AppState).set_recordingLoading_87pylv_k$ = function (_set____db54di) {
    this.recordingLoading_1 = _set____db54di;
  };
  protoOf(AppState).get_recordingLoading_h2yjak_k$ = function () {
    return this.recordingLoading_1;
  };
  protoOf(AppState).set_historySessions_497v17_k$ = function (_set____db54di) {
    this.historySessions_1 = _set____db54di;
  };
  protoOf(AppState).get_historySessions_72jo14_k$ = function () {
    return this.historySessions_1;
  };
  protoOf(AppState).set_historyBalance_xu4jsp_k$ = function (_set____db54di) {
    this.historyBalance_1 = _set____db54di;
  };
  protoOf(AppState).get_historyBalance_nnmchd_k$ = function () {
    return this.historyBalance_1;
  };
  protoOf(AppState).set_historyLoading_b45h4g_k$ = function (_set____db54di) {
    this.historyLoading_1 = _set____db54di;
  };
  protoOf(AppState).get_historyLoading_ytzv9t_k$ = function () {
    return this.historyLoading_1;
  };
  protoOf(AppState).set_balanceAdminHistory_5kfiug_k$ = function (_set____db54di) {
    this.balanceAdminHistory_1 = _set____db54di;
  };
  protoOf(AppState).get_balanceAdminHistory_wo1fzc_k$ = function () {
    return this.balanceAdminHistory_1;
  };
  protoOf(AppState).set_balanceAdminUsers_8is91l_k$ = function (_set____db54di) {
    this.balanceAdminUsers_1 = _set____db54di;
  };
  protoOf(AppState).get_balanceAdminUsers_ico6ks_k$ = function () {
    return this.balanceAdminUsers_1;
  };
  protoOf(AppState).set_balanceAdminLoading_w4lmmh_k$ = function (_set____db54di) {
    this.balanceAdminLoading_1 = _set____db54di;
  };
  protoOf(AppState).get_balanceAdminLoading_slms8w_k$ = function () {
    return this.balanceAdminLoading_1;
  };
  protoOf(AppState).set_balanceAdminSelectedUserId_tuhz6u_k$ = function (_set____db54di) {
    this.balanceAdminSelectedUserId_1 = _set____db54di;
  };
  protoOf(AppState).get_balanceAdminSelectedUserId_4lrwub_k$ = function () {
    return this.balanceAdminSelectedUserId_1;
  };
  protoOf(AppState).set_adminHomeUsers_48iycq_k$ = function (_set____db54di) {
    this.adminHomeUsers_1 = _set____db54di;
  };
  protoOf(AppState).get_adminHomeUsers_yujv1f_k$ = function () {
    return this.adminHomeUsers_1;
  };
  protoOf(AppState).set_adminHomeLoading_891u22_k$ = function (_set____db54di) {
    this.adminHomeLoading_1 = _set____db54di;
  };
  protoOf(AppState).get_adminHomeLoading_t0xi21_k$ = function () {
    return this.adminHomeLoading_1;
  };
  protoOf(AppState).set_adminNewbiesSectionOpen_h0xy2x_k$ = function (_set____db54di) {
    this.adminNewbiesSectionOpen_1 = _set____db54di;
  };
  protoOf(AppState).get_adminNewbiesSectionOpen_l2t6yo_k$ = function () {
    return this.adminNewbiesSectionOpen_1;
  };
  protoOf(AppState).set_adminInstructorsSectionOpen_ns1zpy_k$ = function (_set____db54di) {
    this.adminInstructorsSectionOpen_1 = _set____db54di;
  };
  protoOf(AppState).get_adminInstructorsSectionOpen_frw1fj_k$ = function () {
    return this.adminInstructorsSectionOpen_1;
  };
  protoOf(AppState).set_adminCadetsSectionOpen_fepkwa_k$ = function (_set____db54di) {
    this.adminCadetsSectionOpen_1 = _set____db54di;
  };
  protoOf(AppState).get_adminCadetsSectionOpen_e2mf91_k$ = function () {
    return this.adminCadetsSectionOpen_1;
  };
  protoOf(AppState).set_balanceHistorySectionOpen_9pgeu3_k$ = function (_set____db54di) {
    this.balanceHistorySectionOpen_1 = _set____db54di;
  };
  protoOf(AppState).get_balanceHistorySectionOpen_z8lwya_k$ = function () {
    return this.balanceHistorySectionOpen_1;
  };
  protoOf(AppState).set_adminAssignInstructorId_hlct9e_k$ = function (_set____db54di) {
    this.adminAssignInstructorId_1 = _set____db54di;
  };
  protoOf(AppState).get_adminAssignInstructorId_yfdvdf_k$ = function () {
    return this.adminAssignInstructorId_1;
  };
  protoOf(AppState).set_adminAssignCadetId_ihf1g8_k$ = function (_set____db54di) {
    this.adminAssignCadetId_1 = _set____db54di;
  };
  protoOf(AppState).get_adminAssignCadetId_fdosjf_k$ = function () {
    return this.adminAssignCadetId_1;
  };
  protoOf(AppState).set_adminInstructorCadetsModalId_iqbhi4_k$ = function (_set____db54di) {
    this.adminInstructorCadetsModalId_1 = _set____db54di;
  };
  protoOf(AppState).get_adminInstructorCadetsModalId_38kovt_k$ = function () {
    return this.adminInstructorCadetsModalId_1;
  };
  protoOf(AppState).set_cadetInstructor_tunjhh_k$ = function (_set____db54di) {
    this.cadetInstructor_1 = _set____db54di;
  };
  protoOf(AppState).get_cadetInstructor_oyqk5l_k$ = function () {
    return this.cadetInstructor_1;
  };
  protoOf(AppState).set_instructorCadets_zfaiwn_k$ = function (_set____db54di) {
    this.instructorCadets_1 = _set____db54di;
  };
  protoOf(AppState).get_instructorCadets_elsrtw_k$ = function () {
    return this.instructorCadets_1;
  };
  protoOf(AppState).component1_7eebsc_k$ = function () {
    return this.screen_1;
  };
  protoOf(AppState).component2_7eebsb_k$ = function () {
    return this.user_1;
  };
  protoOf(AppState).component3_7eebsa_k$ = function () {
    return this.error_1;
  };
  protoOf(AppState).component4_7eebs9_k$ = function () {
    return this.loading_1;
  };
  protoOf(AppState).component5_7eebs8_k$ = function () {
    return this.networkError_1;
  };
  protoOf(AppState).component6_7eebs7_k$ = function () {
    return this.selectedTabIndex_1;
  };
  protoOf(AppState).component7_7eebs6_k$ = function () {
    return this.chatContacts_1;
  };
  protoOf(AppState).component8_7eebs5_k$ = function () {
    return this.chatContactOnlineIds_1;
  };
  protoOf(AppState).component9_7eebs4_k$ = function () {
    return this.chatContactsLoading_1;
  };
  protoOf(AppState).component10_gazzfo_k$ = function () {
    return this.selectedChatContactId_1;
  };
  protoOf(AppState).component11_gazzfn_k$ = function () {
    return this.chatMessages_1;
  };
  protoOf(AppState).component12_gazzfm_k$ = function () {
    return this.recordingOpenWindows_1;
  };
  protoOf(AppState).component13_gazzfl_k$ = function () {
    return this.recordingSessions_1;
  };
  protoOf(AppState).component14_gazzfk_k$ = function () {
    return this.recordingLoading_1;
  };
  protoOf(AppState).component15_gazzfj_k$ = function () {
    return this.historySessions_1;
  };
  protoOf(AppState).component16_gazzfi_k$ = function () {
    return this.historyBalance_1;
  };
  protoOf(AppState).component17_gazzfh_k$ = function () {
    return this.historyLoading_1;
  };
  protoOf(AppState).component18_gazzfg_k$ = function () {
    return this.balanceAdminHistory_1;
  };
  protoOf(AppState).component19_gazzff_k$ = function () {
    return this.balanceAdminUsers_1;
  };
  protoOf(AppState).component20_gazzet_k$ = function () {
    return this.balanceAdminLoading_1;
  };
  protoOf(AppState).component21_gazzes_k$ = function () {
    return this.balanceAdminSelectedUserId_1;
  };
  protoOf(AppState).component22_gazzer_k$ = function () {
    return this.adminHomeUsers_1;
  };
  protoOf(AppState).component23_gazzeq_k$ = function () {
    return this.adminHomeLoading_1;
  };
  protoOf(AppState).component24_gazzep_k$ = function () {
    return this.adminNewbiesSectionOpen_1;
  };
  protoOf(AppState).component25_gazzeo_k$ = function () {
    return this.adminInstructorsSectionOpen_1;
  };
  protoOf(AppState).component26_gazzen_k$ = function () {
    return this.adminCadetsSectionOpen_1;
  };
  protoOf(AppState).component27_gazzem_k$ = function () {
    return this.balanceHistorySectionOpen_1;
  };
  protoOf(AppState).component28_gazzel_k$ = function () {
    return this.adminAssignInstructorId_1;
  };
  protoOf(AppState).component29_gazzek_k$ = function () {
    return this.adminAssignCadetId_1;
  };
  protoOf(AppState).component30_gazzdy_k$ = function () {
    return this.adminInstructorCadetsModalId_1;
  };
  protoOf(AppState).component31_gazzdx_k$ = function () {
    return this.cadetInstructor_1;
  };
  protoOf(AppState).component32_gazzdw_k$ = function () {
    return this.instructorCadets_1;
  };
  protoOf(AppState).copy_wbahco_k$ = function (screen, user, error, loading, networkError, selectedTabIndex, chatContacts, chatContactOnlineIds, chatContactsLoading, selectedChatContactId, chatMessages, recordingOpenWindows, recordingSessions, recordingLoading, historySessions, historyBalance, historyLoading, balanceAdminHistory, balanceAdminUsers, balanceAdminLoading, balanceAdminSelectedUserId, adminHomeUsers, adminHomeLoading, adminNewbiesSectionOpen, adminInstructorsSectionOpen, adminCadetsSectionOpen, balanceHistorySectionOpen, adminAssignInstructorId, adminAssignCadetId, adminInstructorCadetsModalId, cadetInstructor, instructorCadets) {
    return new AppState(screen, user, error, loading, networkError, selectedTabIndex, chatContacts, chatContactOnlineIds, chatContactsLoading, selectedChatContactId, chatMessages, recordingOpenWindows, recordingSessions, recordingLoading, historySessions, historyBalance, historyLoading, balanceAdminHistory, balanceAdminUsers, balanceAdminLoading, balanceAdminSelectedUserId, adminHomeUsers, adminHomeLoading, adminNewbiesSectionOpen, adminInstructorsSectionOpen, adminCadetsSectionOpen, balanceHistorySectionOpen, adminAssignInstructorId, adminAssignCadetId, adminInstructorCadetsModalId, cadetInstructor, instructorCadets);
  };
  protoOf(AppState).copy$default_z3a01d_k$ = function (screen, user, error, loading, networkError, selectedTabIndex, chatContacts, chatContactOnlineIds, chatContactsLoading, selectedChatContactId, chatMessages, recordingOpenWindows, recordingSessions, recordingLoading, historySessions, historyBalance, historyLoading, balanceAdminHistory, balanceAdminUsers, balanceAdminLoading, balanceAdminSelectedUserId, adminHomeUsers, adminHomeLoading, adminNewbiesSectionOpen, adminInstructorsSectionOpen, adminCadetsSectionOpen, balanceHistorySectionOpen, adminAssignInstructorId, adminAssignCadetId, adminInstructorCadetsModalId, cadetInstructor, instructorCadets, $super) {
    screen = screen === VOID ? this.screen_1 : screen;
    user = user === VOID ? this.user_1 : user;
    error = error === VOID ? this.error_1 : error;
    loading = loading === VOID ? this.loading_1 : loading;
    networkError = networkError === VOID ? this.networkError_1 : networkError;
    selectedTabIndex = selectedTabIndex === VOID ? this.selectedTabIndex_1 : selectedTabIndex;
    chatContacts = chatContacts === VOID ? this.chatContacts_1 : chatContacts;
    chatContactOnlineIds = chatContactOnlineIds === VOID ? this.chatContactOnlineIds_1 : chatContactOnlineIds;
    chatContactsLoading = chatContactsLoading === VOID ? this.chatContactsLoading_1 : chatContactsLoading;
    selectedChatContactId = selectedChatContactId === VOID ? this.selectedChatContactId_1 : selectedChatContactId;
    chatMessages = chatMessages === VOID ? this.chatMessages_1 : chatMessages;
    recordingOpenWindows = recordingOpenWindows === VOID ? this.recordingOpenWindows_1 : recordingOpenWindows;
    recordingSessions = recordingSessions === VOID ? this.recordingSessions_1 : recordingSessions;
    recordingLoading = recordingLoading === VOID ? this.recordingLoading_1 : recordingLoading;
    historySessions = historySessions === VOID ? this.historySessions_1 : historySessions;
    historyBalance = historyBalance === VOID ? this.historyBalance_1 : historyBalance;
    historyLoading = historyLoading === VOID ? this.historyLoading_1 : historyLoading;
    balanceAdminHistory = balanceAdminHistory === VOID ? this.balanceAdminHistory_1 : balanceAdminHistory;
    balanceAdminUsers = balanceAdminUsers === VOID ? this.balanceAdminUsers_1 : balanceAdminUsers;
    balanceAdminLoading = balanceAdminLoading === VOID ? this.balanceAdminLoading_1 : balanceAdminLoading;
    balanceAdminSelectedUserId = balanceAdminSelectedUserId === VOID ? this.balanceAdminSelectedUserId_1 : balanceAdminSelectedUserId;
    adminHomeUsers = adminHomeUsers === VOID ? this.adminHomeUsers_1 : adminHomeUsers;
    adminHomeLoading = adminHomeLoading === VOID ? this.adminHomeLoading_1 : adminHomeLoading;
    adminNewbiesSectionOpen = adminNewbiesSectionOpen === VOID ? this.adminNewbiesSectionOpen_1 : adminNewbiesSectionOpen;
    adminInstructorsSectionOpen = adminInstructorsSectionOpen === VOID ? this.adminInstructorsSectionOpen_1 : adminInstructorsSectionOpen;
    adminCadetsSectionOpen = adminCadetsSectionOpen === VOID ? this.adminCadetsSectionOpen_1 : adminCadetsSectionOpen;
    balanceHistorySectionOpen = balanceHistorySectionOpen === VOID ? this.balanceHistorySectionOpen_1 : balanceHistorySectionOpen;
    adminAssignInstructorId = adminAssignInstructorId === VOID ? this.adminAssignInstructorId_1 : adminAssignInstructorId;
    adminAssignCadetId = adminAssignCadetId === VOID ? this.adminAssignCadetId_1 : adminAssignCadetId;
    adminInstructorCadetsModalId = adminInstructorCadetsModalId === VOID ? this.adminInstructorCadetsModalId_1 : adminInstructorCadetsModalId;
    cadetInstructor = cadetInstructor === VOID ? this.cadetInstructor_1 : cadetInstructor;
    instructorCadets = instructorCadets === VOID ? this.instructorCadets_1 : instructorCadets;
    return $super === VOID ? this.copy_wbahco_k$(screen, user, error, loading, networkError, selectedTabIndex, chatContacts, chatContactOnlineIds, chatContactsLoading, selectedChatContactId, chatMessages, recordingOpenWindows, recordingSessions, recordingLoading, historySessions, historyBalance, historyLoading, balanceAdminHistory, balanceAdminUsers, balanceAdminLoading, balanceAdminSelectedUserId, adminHomeUsers, adminHomeLoading, adminNewbiesSectionOpen, adminInstructorsSectionOpen, adminCadetsSectionOpen, balanceHistorySectionOpen, adminAssignInstructorId, adminAssignCadetId, adminInstructorCadetsModalId, cadetInstructor, instructorCadets) : $super.copy_wbahco_k$.call(this, screen, user, error, loading, networkError, selectedTabIndex, chatContacts, chatContactOnlineIds, chatContactsLoading, selectedChatContactId, chatMessages, recordingOpenWindows, recordingSessions, recordingLoading, historySessions, historyBalance, historyLoading, balanceAdminHistory, balanceAdminUsers, balanceAdminLoading, balanceAdminSelectedUserId, adminHomeUsers, adminHomeLoading, adminNewbiesSectionOpen, adminInstructorsSectionOpen, adminCadetsSectionOpen, balanceHistorySectionOpen, adminAssignInstructorId, adminAssignCadetId, adminInstructorCadetsModalId, cadetInstructor, instructorCadets);
  };
  protoOf(AppState).toString = function () {
    return 'AppState(screen=' + this.screen_1.toString() + ', user=' + toString(this.user_1) + ', error=' + this.error_1 + ', loading=' + this.loading_1 + ', networkError=' + this.networkError_1 + ', selectedTabIndex=' + this.selectedTabIndex_1 + ', chatContacts=' + toString_0(this.chatContacts_1) + ', chatContactOnlineIds=' + toString_0(this.chatContactOnlineIds_1) + ', chatContactsLoading=' + this.chatContactsLoading_1 + ', selectedChatContactId=' + this.selectedChatContactId_1 + ', chatMessages=' + toString_0(this.chatMessages_1) + ', recordingOpenWindows=' + toString_0(this.recordingOpenWindows_1) + ', recordingSessions=' + toString_0(this.recordingSessions_1) + ', recordingLoading=' + this.recordingLoading_1 + ', historySessions=' + toString_0(this.historySessions_1) + ', historyBalance=' + toString_0(this.historyBalance_1) + ', historyLoading=' + this.historyLoading_1 + ', balanceAdminHistory=' + toString_0(this.balanceAdminHistory_1) + ', balanceAdminUsers=' + toString_0(this.balanceAdminUsers_1) + ', balanceAdminLoading=' + this.balanceAdminLoading_1 + ', balanceAdminSelectedUserId=' + this.balanceAdminSelectedUserId_1 + ', adminHomeUsers=' + toString_0(this.adminHomeUsers_1) + ', adminHomeLoading=' + this.adminHomeLoading_1 + ', adminNewbiesSectionOpen=' + this.adminNewbiesSectionOpen_1 + ', adminInstructorsSectionOpen=' + this.adminInstructorsSectionOpen_1 + ', adminCadetsSectionOpen=' + this.adminCadetsSectionOpen_1 + ', balanceHistorySectionOpen=' + this.balanceHistorySectionOpen_1 + ', adminAssignInstructorId=' + this.adminAssignInstructorId_1 + ', adminAssignCadetId=' + this.adminAssignCadetId_1 + ', adminInstructorCadetsModalId=' + this.adminInstructorCadetsModalId_1 + ', cadetInstructor=' + toString(this.cadetInstructor_1) + ', instructorCadets=' + toString_0(this.instructorCadets_1) + ')';
  };
  protoOf(AppState).hashCode = function () {
    var result = this.screen_1.hashCode();
    result = imul(result, 31) + (this.user_1 == null ? 0 : this.user_1.hashCode()) | 0;
    result = imul(result, 31) + (this.error_1 == null ? 0 : getStringHashCode(this.error_1)) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.loading_1) | 0;
    result = imul(result, 31) + (this.networkError_1 == null ? 0 : getStringHashCode(this.networkError_1)) | 0;
    result = imul(result, 31) + this.selectedTabIndex_1 | 0;
    result = imul(result, 31) + hashCode(this.chatContacts_1) | 0;
    result = imul(result, 31) + hashCode(this.chatContactOnlineIds_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.chatContactsLoading_1) | 0;
    result = imul(result, 31) + (this.selectedChatContactId_1 == null ? 0 : getStringHashCode(this.selectedChatContactId_1)) | 0;
    result = imul(result, 31) + hashCode(this.chatMessages_1) | 0;
    result = imul(result, 31) + hashCode(this.recordingOpenWindows_1) | 0;
    result = imul(result, 31) + hashCode(this.recordingSessions_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.recordingLoading_1) | 0;
    result = imul(result, 31) + hashCode(this.historySessions_1) | 0;
    result = imul(result, 31) + hashCode(this.historyBalance_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.historyLoading_1) | 0;
    result = imul(result, 31) + hashCode(this.balanceAdminHistory_1) | 0;
    result = imul(result, 31) + hashCode(this.balanceAdminUsers_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.balanceAdminLoading_1) | 0;
    result = imul(result, 31) + (this.balanceAdminSelectedUserId_1 == null ? 0 : getStringHashCode(this.balanceAdminSelectedUserId_1)) | 0;
    result = imul(result, 31) + hashCode(this.adminHomeUsers_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.adminHomeLoading_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.adminNewbiesSectionOpen_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.adminInstructorsSectionOpen_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.adminCadetsSectionOpen_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.balanceHistorySectionOpen_1) | 0;
    result = imul(result, 31) + (this.adminAssignInstructorId_1 == null ? 0 : getStringHashCode(this.adminAssignInstructorId_1)) | 0;
    result = imul(result, 31) + (this.adminAssignCadetId_1 == null ? 0 : getStringHashCode(this.adminAssignCadetId_1)) | 0;
    result = imul(result, 31) + (this.adminInstructorCadetsModalId_1 == null ? 0 : getStringHashCode(this.adminInstructorCadetsModalId_1)) | 0;
    result = imul(result, 31) + (this.cadetInstructor_1 == null ? 0 : this.cadetInstructor_1.hashCode()) | 0;
    result = imul(result, 31) + hashCode(this.instructorCadets_1) | 0;
    return result;
  };
  protoOf(AppState).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof AppState))
      return false;
    var tmp0_other_with_cast = other instanceof AppState ? other : THROW_CCE();
    if (!this.screen_1.equals(tmp0_other_with_cast.screen_1))
      return false;
    if (!equals(this.user_1, tmp0_other_with_cast.user_1))
      return false;
    if (!(this.error_1 == tmp0_other_with_cast.error_1))
      return false;
    if (!(this.loading_1 === tmp0_other_with_cast.loading_1))
      return false;
    if (!(this.networkError_1 == tmp0_other_with_cast.networkError_1))
      return false;
    if (!(this.selectedTabIndex_1 === tmp0_other_with_cast.selectedTabIndex_1))
      return false;
    if (!equals(this.chatContacts_1, tmp0_other_with_cast.chatContacts_1))
      return false;
    if (!equals(this.chatContactOnlineIds_1, tmp0_other_with_cast.chatContactOnlineIds_1))
      return false;
    if (!(this.chatContactsLoading_1 === tmp0_other_with_cast.chatContactsLoading_1))
      return false;
    if (!(this.selectedChatContactId_1 == tmp0_other_with_cast.selectedChatContactId_1))
      return false;
    if (!equals(this.chatMessages_1, tmp0_other_with_cast.chatMessages_1))
      return false;
    if (!equals(this.recordingOpenWindows_1, tmp0_other_with_cast.recordingOpenWindows_1))
      return false;
    if (!equals(this.recordingSessions_1, tmp0_other_with_cast.recordingSessions_1))
      return false;
    if (!(this.recordingLoading_1 === tmp0_other_with_cast.recordingLoading_1))
      return false;
    if (!equals(this.historySessions_1, tmp0_other_with_cast.historySessions_1))
      return false;
    if (!equals(this.historyBalance_1, tmp0_other_with_cast.historyBalance_1))
      return false;
    if (!(this.historyLoading_1 === tmp0_other_with_cast.historyLoading_1))
      return false;
    if (!equals(this.balanceAdminHistory_1, tmp0_other_with_cast.balanceAdminHistory_1))
      return false;
    if (!equals(this.balanceAdminUsers_1, tmp0_other_with_cast.balanceAdminUsers_1))
      return false;
    if (!(this.balanceAdminLoading_1 === tmp0_other_with_cast.balanceAdminLoading_1))
      return false;
    if (!(this.balanceAdminSelectedUserId_1 == tmp0_other_with_cast.balanceAdminSelectedUserId_1))
      return false;
    if (!equals(this.adminHomeUsers_1, tmp0_other_with_cast.adminHomeUsers_1))
      return false;
    if (!(this.adminHomeLoading_1 === tmp0_other_with_cast.adminHomeLoading_1))
      return false;
    if (!(this.adminNewbiesSectionOpen_1 === tmp0_other_with_cast.adminNewbiesSectionOpen_1))
      return false;
    if (!(this.adminInstructorsSectionOpen_1 === tmp0_other_with_cast.adminInstructorsSectionOpen_1))
      return false;
    if (!(this.adminCadetsSectionOpen_1 === tmp0_other_with_cast.adminCadetsSectionOpen_1))
      return false;
    if (!(this.balanceHistorySectionOpen_1 === tmp0_other_with_cast.balanceHistorySectionOpen_1))
      return false;
    if (!(this.adminAssignInstructorId_1 == tmp0_other_with_cast.adminAssignInstructorId_1))
      return false;
    if (!(this.adminAssignCadetId_1 == tmp0_other_with_cast.adminAssignCadetId_1))
      return false;
    if (!(this.adminInstructorCadetsModalId_1 == tmp0_other_with_cast.adminInstructorCadetsModalId_1))
      return false;
    if (!equals(this.cadetInstructor_1, tmp0_other_with_cast.cadetInstructor_1))
      return false;
    if (!equals(this.instructorCadets_1, tmp0_other_with_cast.instructorCadets_1))
      return false;
    return true;
  };
  function updateState(block) {
    _init_properties_AppState_kt__bzmygg();
    block(get_appState());
    var tmp0_safe_receiver = get_onStateChanged();
    if (tmp0_safe_receiver == null)
      null;
    else
      tmp0_safe_receiver();
  }
  function AppScreen_Login_getInstance() {
    AppScreen_initEntries();
    return AppScreen_Login_instance;
  }
  function AppScreen_Register_getInstance() {
    AppScreen_initEntries();
    return AppScreen_Register_instance;
  }
  function AppScreen_PendingApproval_getInstance() {
    AppScreen_initEntries();
    return AppScreen_PendingApproval_instance;
  }
  function AppScreen_ProfileNotFound_getInstance() {
    AppScreen_initEntries();
    return AppScreen_ProfileNotFound_instance;
  }
  function AppScreen_Admin_getInstance() {
    AppScreen_initEntries();
    return AppScreen_Admin_instance;
  }
  function AppScreen_Instructor_getInstance() {
    AppScreen_initEntries();
    return AppScreen_Instructor_instance;
  }
  function AppScreen_Cadet_getInstance() {
    AppScreen_initEntries();
    return AppScreen_Cadet_instance;
  }
  var properties_initialized_AppState_kt_v71icy;
  function _init_properties_AppState_kt__bzmygg() {
    if (!properties_initialized_AppState_kt_v71icy) {
      properties_initialized_AppState_kt_v71icy = true;
      appState = new AppState();
      onStateChanged = null;
    }
  }
  function set_presenceUnsubscribes(_set____db54di) {
    _init_properties_Main_kt__xi25uv();
    presenceUnsubscribes = _set____db54di;
  }
  function get_presenceUnsubscribes() {
    _init_properties_Main_kt__xi25uv();
    return presenceUnsubscribes;
  }
  var presenceUnsubscribes;
  function get_iconPhoneSvg() {
    _init_properties_Main_kt__xi25uv();
    return iconPhoneSvg;
  }
  var iconPhoneSvg;
  function get_iconChatSvg() {
    _init_properties_Main_kt__xi25uv();
    return iconChatSvg;
  }
  var iconChatSvg;
  function get_iconUserPlusSvg() {
    _init_properties_Main_kt__xi25uv();
    return iconUserPlusSvg;
  }
  var iconUserPlusSvg;
  function get_iconPowerSvg() {
    _init_properties_Main_kt__xi25uv();
    return iconPowerSvg;
  }
  var iconPowerSvg;
  function get_iconTrashSvg() {
    _init_properties_Main_kt__xi25uv();
    return iconTrashSvg;
  }
  var iconTrashSvg;
  function get_iconUnlinkSvg() {
    _init_properties_Main_kt__xi25uv();
    return iconUnlinkSvg;
  }
  var iconUnlinkSvg;
  function get_iconUserSvg() {
    _init_properties_Main_kt__xi25uv();
    return iconUserSvg;
  }
  var iconUserSvg;
  function get_iconPhoneLabelSvg() {
    _init_properties_Main_kt__xi25uv();
    return iconPhoneLabelSvg;
  }
  var iconPhoneLabelSvg;
  function get_iconEmailLabelSvg() {
    _init_properties_Main_kt__xi25uv();
    return iconEmailLabelSvg;
  }
  var iconEmailLabelSvg;
  function get_iconTicketSvg() {
    _init_properties_Main_kt__xi25uv();
    return iconTicketSvg;
  }
  var iconTicketSvg;
  function get_iconInstructorSvg() {
    _init_properties_Main_kt__xi25uv();
    return iconInstructorSvg;
  }
  var iconInstructorSvg;
  function get_iconSelectSvg() {
    _init_properties_Main_kt__xi25uv();
    return iconSelectSvg;
  }
  var iconSelectSvg;
  function get_iconCreditSvg() {
    _init_properties_Main_kt__xi25uv();
    return iconCreditSvg;
  }
  var iconCreditSvg;
  function get_iconDebitSvg() {
    _init_properties_Main_kt__xi25uv();
    return iconDebitSvg;
  }
  var iconDebitSvg;
  function get_iconSetSvg() {
    _init_properties_Main_kt__xi25uv();
    return iconSetSvg;
  }
  var iconSetSvg;
  function get_iconResetSvg() {
    _init_properties_Main_kt__xi25uv();
    return iconResetSvg;
  }
  var iconResetSvg;
  function subscribeChatPresence(contactIds) {
    _init_properties_Main_kt__xi25uv();
    // Inline function 'kotlin.collections.forEach' call
    var _iterator__ex2g4s = get_presenceUnsubscribes().iterator_jk1svi_k$();
    while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
      var element = _iterator__ex2g4s.next_20eer_k$();
      element();
    }
    get_presenceUnsubscribes().clear_j9egeb_k$();
    updateState(subscribeChatPresence$lambda);
    // Inline function 'kotlin.collections.forEach' call
    var _iterator__ex2g4s_0 = contactIds.iterator_jk1svi_k$();
    while (_iterator__ex2g4s_0.hasNext_bitz1p_k$()) {
      var element_0 = _iterator__ex2g4s_0.next_20eer_k$();
      var unsub = subscribePresence(element_0, subscribeChatPresence$lambda_0(element_0));
      get_presenceUnsubscribes().add_utx5q5_k$(unsub);
    }
  }
  function main() {
    _init_properties_Main_kt__xi25uv();
    var tmp = window;
    var tmp_0 = main$lambda;
    tmp.onload = typeof tmp_0 === 'function' ? tmp_0 : THROW_CCE();
  }
  function renderLogin(error, loading) {
    _init_properties_Main_kt__xi25uv();
    var err = !(error == null) ? '<p class="sd-error">' + error + '<\/p>' : '';
    var btn = loading ? '\u0412\u0445\u043E\u0434\u2026' : '\u0412\u043E\u0439\u0442\u0438';
    return trimIndent('\n        <header class="sd-header">\n            <h1>StartDrive<\/h1>\n            <p>\u0412\u0445\u043E\u0434 \u0432 \u0432\u0435\u0431-\u0432\u0435\u0440\u0441\u0438\u044E<\/p>\n        <\/header>\n        <main class="sd-content">\n            <div class="sd-card sd-login-card">\n                <h2>\u0412\u0445\u043E\u0434<\/h2>\n                ' + err + '\n                <label>Email<\/label>\n                <input type="email" id="sd-email" class="sd-input" placeholder="email@example.com" />\n                <label>\u041F\u0430\u0440\u043E\u043B\u044C<\/label>\n                <input type="password" id="sd-password" class="sd-input" placeholder="\u041F\u0430\u0440\u043E\u043B\u044C" />\n                <label class="sd-checkbox"><input type="checkbox" id="sd-stay" checked /> \u041E\u0441\u0442\u0430\u0432\u0430\u0442\u044C\u0441\u044F \u0432 \u043F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u0438<\/label>\n                <button type="button" id="sd-btn-signin" class="sd-btn sd-btn-primary" ' + (loading ? 'disabled' : '') + '>' + btn + '<\/button>\n                <button type="button" id="sd-btn-register" class="sd-btn sd-btn-secondary">\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F<\/button>\n            <\/div>\n        <\/main>\n    ');
  }
  function renderRegister(error, loading) {
    _init_properties_Main_kt__xi25uv();
    var err = !(error == null) ? '<p class="sd-error">' + error + '<\/p>' : '';
    var btn = loading ? '\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F\u2026' : '\u0417\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u0442\u044C\u0441\u044F';
    return trimIndent('\n        <header class="sd-header">\n            <h1>StartDrive<\/h1>\n            <p>\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F<\/p>\n        <\/header>\n        <main class="sd-content">\n            <div class="sd-card sd-login-card">\n                <h2>\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F<\/h2>\n                ' + err + '\n                <label>\u0424\u0418\u041E<\/label>\n                <input type="text" id="sd-fullName" class="sd-input" placeholder="\u0418\u0432\u0430\u043D\u043E\u0432 \u0418\u0432\u0430\u043D \u0418\u0432\u0430\u043D\u043E\u0432\u0438\u0447" />\n                <label>Email<\/label>\n                <input type="email" id="sd-reg-email" class="sd-input" placeholder="email@example.com" />\n                <label>\u0422\u0435\u043B\u0435\u0444\u043E\u043D<\/label>\n                <input type="tel" id="sd-phone" class="sd-input" placeholder="+7 \u2026" />\n                <label>\u041F\u0430\u0440\u043E\u043B\u044C<\/label>\n                <input type="password" id="sd-reg-password" class="sd-input" placeholder="\u041F\u0430\u0440\u043E\u043B\u044C" />\n                <label>\u0420\u043E\u043B\u044C<\/label>\n                <select id="sd-role" class="sd-input">\n                    <option value="cadet">\u041A\u0443\u0440\u0441\u0430\u043D\u0442<\/option>\n                    <option value="instructor">\u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440<\/option>\n                    <option value="admin">\u0410\u0434\u043C\u0438\u043D\u0438\u0441\u0442\u0440\u0430\u0442\u043E\u0440<\/option>\n                <\/select>\n                <button type="button" id="sd-btn-do-register" class="sd-btn sd-btn-primary" ' + (loading ? 'disabled' : '') + '>' + btn + '<\/button>\n                <button type="button" id="sd-btn-back" class="sd-btn sd-btn-secondary">\u041D\u0430\u0437\u0430\u0434<\/button>\n            <\/div>\n        <\/main>\n    ');
  }
  function renderPendingApproval() {
    _init_properties_Main_kt__xi25uv();
    return '<header class="sd-header">\n    <h1>StartDrive<\/h1>\n    <p>\u041E\u0436\u0438\u0434\u0430\u043D\u0438\u0435 \u043F\u043E\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0435\u043D\u0438\u044F<\/p>\n<\/header>\n<main class="sd-content">\n    <div class="sd-card">\n        <h2>\u041E\u0436\u0438\u0434\u0430\u043D\u0438\u0435 \u043F\u043E\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0435\u043D\u0438\u044F<\/h2>\n        <p>\u0412\u0430\u0448\u0430 \u0437\u0430\u044F\u0432\u043A\u0430 \u043D\u0430 \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044E \u043E\u0442\u043F\u0440\u0430\u0432\u043B\u0435\u043D\u0430. \u0410\u0434\u043C\u0438\u043D\u0438\u0441\u0442\u0440\u0430\u0442\u043E\u0440 \u0430\u043A\u0442\u0438\u0432\u0438\u0440\u0443\u0435\u0442 \u0432\u0430\u0448 \u0430\u043A\u043A\u0430\u0443\u043D\u0442. \u041F\u043E\u0441\u043B\u0435 \u0430\u043A\u0442\u0438\u0432\u0430\u0446\u0438\u0438 \u0432\u044B \u0441\u043C\u043E\u0436\u0435\u0442\u0435 \u0432\u043E\u0439\u0442\u0438.<\/p>\n        <button type="button" id="sd-btn-check" class="sd-btn sd-btn-primary">\u041F\u0440\u043E\u0432\u0435\u0440\u0438\u0442\u044C \u0441\u043D\u043E\u0432\u0430<\/button>\n        <button type="button" id="sd-btn-signout-pending" class="sd-btn sd-btn-secondary">\u0412\u044B\u0439\u0442\u0438<\/button>\n    <\/div>\n<\/main>';
  }
  function renderProfileNotFound(message) {
    _init_properties_Main_kt__xi25uv();
    return trimIndent('\n    <header class="sd-header">\n        <h1>StartDrive<\/h1>\n        <p>\u041F\u0440\u043E\u0444\u0438\u043B\u044C \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D<\/p>\n    <\/header>\n    <main class="sd-content">\n        <div class="sd-card">\n            <h2>\u041D\u0435 \u0443\u0434\u0430\u043B\u043E\u0441\u044C \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044C \u043F\u0440\u043E\u0444\u0438\u043B\u044C<\/h2>\n            <p class="sd-error">' + message + '<\/p>\n            <p>\u0415\u0441\u043B\u0438 \u0432\u044B \u0432\u0445\u043E\u0434\u0438\u0442\u0435 \u0441 \u0442\u0435\u043C\u0438 \u0436\u0435 \u0434\u0430\u043D\u043D\u044B\u043C\u0438, \u0447\u0442\u043E \u0438 \u0432 \u043F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u0438 \u2014 \u0443\u0431\u0435\u0434\u0438\u0442\u0435\u0441\u044C, \u0447\u0442\u043E \u0432 Firebase Console \u0432 Firestore \u0435\u0441\u0442\u044C \u0434\u043E\u043A\u0443\u043C\u0435\u043D\u0442 \u0432 \u043A\u043E\u043B\u043B\u0435\u043A\u0446\u0438\u0438 <strong>users<\/strong> \u0441 id \u0432\u0430\u0448\u0435\u0433\u043E \u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u0435\u043B\u044F (UID \u0438\u0437 Authentication).<\/p>\n            <button type="button" id="sd-btn-signout-profile-not-found" class="sd-btn sd-btn-primary">\u0412\u044B\u0439\u0442\u0438<\/button>\n        <\/div>\n    <\/main>\n');
  }
  function renderChatTabContent(currentUser) {
    _init_properties_Main_kt__xi25uv();
    var contactId = get_appState().get_selectedChatContactId_qi8d29_k$();
    var contacts = get_appState().get_chatContacts_b21mhg_k$();
    var loading = get_appState().get_chatContactsLoading_r2se4o_k$();
    var messages = get_appState().get_chatMessages_csrswd_k$();
    var myId = currentUser.get_id_kntnx8_k$();
    if (!(contactId == null)) {
      // Inline function 'kotlin.collections.find' call
      var tmp$ret$1;
      $l$block: {
        // Inline function 'kotlin.collections.firstOrNull' call
        var _iterator__ex2g4s = contacts.iterator_jk1svi_k$();
        while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
          var element = _iterator__ex2g4s.next_20eer_k$();
          if (element.get_id_kntnx8_k$() === contactId) {
            tmp$ret$1 = element;
            break $l$block;
          }
        }
        tmp$ret$1 = null;
      }
      var tmp0_elvis_lhs = tmp$ret$1;
      var tmp;
      if (tmp0_elvis_lhs == null) {
        return '<p class="sd-error">\u041A\u043E\u043D\u0442\u0430\u043A\u0442 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D.<\/p>';
      } else {
        tmp = tmp0_elvis_lhs;
      }
      var contact = tmp;
      var iconCheck = '<svg class="sd-msg-check" xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/><\/svg>';
      var msgsHtml = joinToString(messages, '', VOID, VOID, VOID, VOID, renderChatTabContent$lambda(myId, iconCheck));
      return trimIndent('\n            <div class="sd-chat-tab">\n            <div class="sd-chat-conversation">\n                <div class="sd-chat-header">\n                    <button type="button" id="sd-chat-back" class="sd-btn sd-btn-secondary sd-chat-back-btn" title="\u041D\u0430\u0437\u0430\u0434" aria-label="\u041D\u0430\u0437\u0430\u0434">\u2190<\/button>\n                    <span class="sd-chat-contact-name">' + escapeHtml(contact.get_fullName_9skygt_k$()) + '<\/span>\n                <\/div>\n                <div class="sd-chat-messages" id="sd-chat-messages">' + msgsHtml + '<\/div>\n                <div class="sd-chat-input-row">\n                    <input type="text" id="sd-chat-input" class="sd-input" placeholder="\u0421\u043E\u043E\u0431\u0449\u0435\u043D\u0438\u0435..." maxlength="2000" />\n                    <button type="button" id="sd-chat-send" class="sd-btn sd-btn-primary">\u041E\u0442\u043F\u0440\u0430\u0432\u0438\u0442\u044C<\/button>\n                <\/div>\n            <\/div>\n            <\/div>\n        ');
    }
    var loadingLine = loading ? '<p class="sd-loading-text">\u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430 \u043A\u043E\u043D\u0442\u0430\u043A\u0442\u043E\u0432\u2026 <button type="button" id="sd-chat-stop-loading" class="sd-btn sd-btn-small sd-btn-secondary">\u041F\u043E\u043A\u0430\u0437\u0430\u0442\u044C \u043F\u0443\u0441\u0442\u043E<\/button><\/p>' : '';
    var refreshBtn = '<button type="button" id="sd-chat-refresh" class="sd-btn sd-btn-small sd-btn-secondary">\u041E\u0431\u043D\u043E\u0432\u0438\u0442\u044C \u0441\u043F\u0438\u0441\u043E\u043A \u043A\u043E\u043D\u0442\u0430\u043A\u0442\u043E\u0432<\/button>';
    // Inline function 'kotlin.collections.filter' call
    // Inline function 'kotlin.collections.filterTo' call
    var destination = ArrayList_init_$Create$();
    var _iterator__ex2g4s_0 = contacts.iterator_jk1svi_k$();
    while (_iterator__ex2g4s_0.hasNext_bitz1p_k$()) {
      var element_0 = _iterator__ex2g4s_0.next_20eer_k$();
      if (element_0.get_role_wotsxr_k$() === 'instructor') {
        destination.add_utx5q5_k$(element_0);
      }
    }
    // Inline function 'kotlin.collections.sortedBy' call
    // Inline function 'kotlin.comparisons.compareBy' call
    var tmp_0 = renderChatTabContent$lambda_0;
    var tmp$ret$6 = new sam$kotlin_Comparator$0(tmp_0);
    var instructors = sortedWith(destination, tmp$ret$6);
    // Inline function 'kotlin.collections.filter' call
    // Inline function 'kotlin.collections.filterTo' call
    var destination_0 = ArrayList_init_$Create$();
    var _iterator__ex2g4s_1 = contacts.iterator_jk1svi_k$();
    while (_iterator__ex2g4s_1.hasNext_bitz1p_k$()) {
      var element_1 = _iterator__ex2g4s_1.next_20eer_k$();
      if (element_1.get_role_wotsxr_k$() === 'cadet') {
        destination_0.add_utx5q5_k$(element_1);
      }
    }
    // Inline function 'kotlin.collections.sortedBy' call
    // Inline function 'kotlin.comparisons.compareBy' call
    var tmp_1 = renderChatTabContent$lambda_1;
    var tmp$ret$11 = new sam$kotlin_Comparator$0(tmp_1);
    var cadets = sortedWith(destination_0, tmp$ret$11);
    // Inline function 'kotlin.collections.filter' call
    // Inline function 'kotlin.collections.filterTo' call
    var destination_1 = ArrayList_init_$Create$();
    var _iterator__ex2g4s_2 = contacts.iterator_jk1svi_k$();
    while (_iterator__ex2g4s_2.hasNext_bitz1p_k$()) {
      var element_2 = _iterator__ex2g4s_2.next_20eer_k$();
      if (!listOf(['instructor', 'cadet']).contains_aljjnj_k$(element_2.get_role_wotsxr_k$())) {
        destination_1.add_utx5q5_k$(element_2);
      }
    }
    // Inline function 'kotlin.collections.sortedBy' call
    // Inline function 'kotlin.comparisons.compareBy' call
    var tmp_2 = renderChatTabContent$lambda_2;
    var tmp$ret$16 = new sam$kotlin_Comparator$0(tmp_2);
    var others = sortedWith(destination_1, tmp$ret$16);
    var tmp_3;
    if (instructors.isEmpty_y1axqb_k$()) {
      tmp_3 = '';
    } else {
      var tmp_4 = instructors.get_size_woubt6_k$();
      tmp_3 = '<div class="sd-chat-contacts-group"><h4 class="sd-chat-contacts-group-title">\u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440\u044B (' + tmp_4 + ')<\/h4><div class="sd-chat-contacts">' + joinToString(instructors, '', VOID, VOID, VOID, VOID, renderChatTabContent$lambda_3) + '<\/div><\/div>';
    }
    var instructorsSection = tmp_3;
    var tmp_5;
    if (cadets.isEmpty_y1axqb_k$()) {
      tmp_5 = '';
    } else {
      var tmp_6 = cadets.get_size_woubt6_k$();
      tmp_5 = '<div class="sd-chat-contacts-group"><h4 class="sd-chat-contacts-group-title">\u041A\u0443\u0440\u0441\u0430\u043D\u0442\u044B (' + tmp_6 + ')<\/h4><div class="sd-chat-contacts">' + joinToString(cadets, '', VOID, VOID, VOID, VOID, renderChatTabContent$lambda_4) + '<\/div><\/div>';
    }
    var cadetsSection = tmp_5;
    var tmp_7;
    if (others.isEmpty_y1axqb_k$()) {
      tmp_7 = '';
    } else {
      var tmp_8 = others.get_size_woubt6_k$();
      tmp_7 = '<div class="sd-chat-contacts-group"><h4 class="sd-chat-contacts-group-title">\u0414\u0440\u0443\u0433\u0438\u0435 (' + tmp_8 + ')<\/h4><div class="sd-chat-contacts">' + joinToString(others, '', VOID, VOID, VOID, VOID, renderChatTabContent$lambda_5) + '<\/div><\/div>';
    }
    var othersSection = tmp_7;
    var contactsBlock = contacts.isEmpty_y1axqb_k$() && !loading ? '<p class="sd-chat-empty-hint">\u041D\u0435\u0442 \u043A\u043E\u043D\u0442\u0430\u043A\u0442\u043E\u0432 \u0434\u043B\u044F \u0447\u0430\u0442\u0430. \u0410\u0434\u043C\u0438\u043D: \u0432\u0441\u0435 \u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u0435\u043B\u0438 \u043A\u0440\u043E\u043C\u0435 \u0430\u0434\u043C\u0438\u043D\u043E\u0432. \u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440: \u0430\u0434\u043C\u0438\u043D + \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D\u043D\u044B\u0435 \u043A\u0443\u0440\u0441\u0430\u043D\u0442\u044B. \u041A\u0443\u0440\u0441\u0430\u043D\u0442: \u0430\u0434\u043C\u0438\u043D + \u0432\u0430\u0448 \u0438\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440.<\/p>' : instructorsSection + cadetsSection + othersSection;
    return '<div class="sd-chat-tab"><h2 class="sd-chat-title">\u0427\u0430\u0442<\/h2>' + loadingLine + '<p>' + refreshBtn + '<\/p>' + contactsBlock + '<\/div>';
  }
  function escapeHtml(_this__u8e3s4) {
    _init_properties_Main_kt__xi25uv();
    return _this__u8e3s4;
  }
  function formatMessageDateTime(timestampMs) {
    _init_properties_Main_kt__xi25uv();
    if (timestampMs.compareTo_9jj042_k$(new Long(0, 0)) <= 0)
      return '';
    // Inline function 'kotlin.js.unsafeCast' call
    var d = function (ts) {
      return new Date(ts);
    }(timestampMs);
    var tmp = d.getDate();
    var day = padStart(((!(tmp == null) ? typeof tmp === 'number' : false) ? tmp : THROW_CCE()).toString(), 2, _Char___init__impl__6a9atx(48));
    var tmp_0 = d.getMonth();
    var month = padStart((((!(tmp_0 == null) ? typeof tmp_0 === 'number' : false) ? tmp_0 : THROW_CCE()) + 1 | 0).toString(), 2, _Char___init__impl__6a9atx(48));
    var tmp_1 = d.getFullYear();
    var year = (!(tmp_1 == null) ? typeof tmp_1 === 'number' : false) ? tmp_1 : THROW_CCE();
    var tmp_2 = d.getHours();
    var hours = padStart(((!(tmp_2 == null) ? typeof tmp_2 === 'number' : false) ? tmp_2 : THROW_CCE()).toString(), 2, _Char___init__impl__6a9atx(48));
    var tmp_3 = d.getMinutes();
    var minutes = padStart(((!(tmp_3 == null) ? typeof tmp_3 === 'number' : false) ? tmp_3 : THROW_CCE()).toString(), 2, _Char___init__impl__6a9atx(48));
    return day + '.' + month + '.' + year + ' ' + hours + ':' + minutes;
  }
  function avatarColorForId(id) {
    _init_properties_Main_kt__xi25uv();
    var h = (getStringHashCode(id) & 2147483647) % 360 | 0;
    var s = 55;
    var l = 42;
    return 'hsl(' + h + ',' + s + '%,' + l + '%)';
  }
  function formatShortName(fullName) {
    _init_properties_Main_kt__xi25uv();
    // Inline function 'kotlin.text.trim' call
    var tmp$ret$0 = toString_0(trim(isCharSequence(fullName) ? fullName : THROW_CCE()));
    // Inline function 'kotlin.collections.filter' call
    var tmp0 = split(tmp$ret$0, [' ']);
    // Inline function 'kotlin.collections.filterTo' call
    var destination = ArrayList_init_$Create$();
    var _iterator__ex2g4s = tmp0.iterator_jk1svi_k$();
    while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
      var element = _iterator__ex2g4s.next_20eer_k$();
      // Inline function 'kotlin.text.isNotBlank' call
      if (!isBlank(element)) {
        destination.add_utx5q5_k$(element);
      }
    }
    var parts = destination;
    if (parts.isEmpty_y1axqb_k$())
      return '\u2014';
    if (parts.get_size_woubt6_k$() === 1)
      return parts.get_c1px32_k$(0);
    var surname = parts.get_c1px32_k$(0);
    // Inline function 'kotlin.collections.map' call
    var this_0 = drop(parts, 1);
    // Inline function 'kotlin.collections.mapTo' call
    var destination_0 = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_0, 10));
    var _iterator__ex2g4s_0 = this_0.iterator_jk1svi_k$();
    while (_iterator__ex2g4s_0.hasNext_bitz1p_k$()) {
      var item = _iterator__ex2g4s_0.next_20eer_k$();
      var tmp0_safe_receiver = firstOrNull(item);
      var tmp;
      var tmp_0 = tmp0_safe_receiver;
      if ((tmp_0 == null ? null : new Char(tmp_0)) == null) {
        tmp = null;
      } else {
        // Inline function 'kotlin.text.uppercase' call
        // Inline function 'kotlin.js.asDynamic' call
        // Inline function 'kotlin.js.unsafeCast' call
        tmp = toString_1(tmp0_safe_receiver).toUpperCase();
      }
      var tmp1_safe_receiver = tmp;
      var tmp2_elvis_lhs = tmp1_safe_receiver == null ? null : tmp1_safe_receiver + '.';
      var tmp$ret$8 = tmp2_elvis_lhs == null ? '' : tmp2_elvis_lhs;
      destination_0.add_utx5q5_k$(tmp$ret$8);
    }
    var initials = joinToString(destination_0, ' ');
    return surname + ' ' + initials;
  }
  function formatDateTime(ms) {
    _init_properties_Main_kt__xi25uv();
    if (ms == null || ms.compareTo_9jj042_k$(new Long(0, 0)) <= 0)
      return '\u2014';
    var d = new Date(ms);
    // Inline function 'kotlin.js.unsafeCast' call
    return d.toLocaleString('ru-RU');
  }
  function formatDateTimeEkaterinburg(ms) {
    _init_properties_Main_kt__xi25uv();
    if (ms == null || ms.compareTo_9jj042_k$(new Long(0, 0)) <= 0)
      return '\u2014';
    var d = new Date(ms);
    // Inline function 'kotlin.js.unsafeCast' call
    return d.toLocaleString('ru-RU', {timeZone: 'Asia/Yekaterinburg', dateStyle: 'short', timeStyle: 'short'});
  }
  function renderAdminHomeContent() {
    _init_properties_Main_kt__xi25uv();
    var loading = get_appState().get_adminHomeLoading_t0xi21_k$();
    var allUsers = get_appState().get_adminHomeUsers_yujv1f_k$();
    // Inline function 'kotlin.collections.filter' call
    // Inline function 'kotlin.collections.filterTo' call
    var destination = ArrayList_init_$Create$();
    var _iterator__ex2g4s = allUsers.iterator_jk1svi_k$();
    while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
      var element = _iterator__ex2g4s.next_20eer_k$();
      if ((element.get_role_wotsxr_k$() === 'instructor' || element.get_role_wotsxr_k$() === 'cadet') && !element.get_isActive_quafmh_k$()) {
        destination.add_utx5q5_k$(element);
      }
    }
    // Inline function 'kotlin.collections.sortedBy' call
    // Inline function 'kotlin.comparisons.compareBy' call
    var tmp = renderAdminHomeContent$lambda;
    var tmp$ret$3 = new sam$kotlin_Comparator$0_0(tmp);
    var newbies = sortedWith(destination, tmp$ret$3);
    // Inline function 'kotlin.collections.filter' call
    // Inline function 'kotlin.collections.filterTo' call
    var destination_0 = ArrayList_init_$Create$();
    var _iterator__ex2g4s_0 = allUsers.iterator_jk1svi_k$();
    while (_iterator__ex2g4s_0.hasNext_bitz1p_k$()) {
      var element_0 = _iterator__ex2g4s_0.next_20eer_k$();
      if (element_0.get_role_wotsxr_k$() === 'instructor' && element_0.get_isActive_quafmh_k$()) {
        destination_0.add_utx5q5_k$(element_0);
      }
    }
    // Inline function 'kotlin.collections.sortedBy' call
    // Inline function 'kotlin.comparisons.compareBy' call
    var tmp_0 = renderAdminHomeContent$lambda_0;
    var tmp$ret$8 = new sam$kotlin_Comparator$0_0(tmp_0);
    var instructors = sortedWith(destination_0, tmp$ret$8);
    // Inline function 'kotlin.collections.filter' call
    // Inline function 'kotlin.collections.filterTo' call
    var destination_1 = ArrayList_init_$Create$();
    var _iterator__ex2g4s_1 = allUsers.iterator_jk1svi_k$();
    while (_iterator__ex2g4s_1.hasNext_bitz1p_k$()) {
      var element_1 = _iterator__ex2g4s_1.next_20eer_k$();
      if (element_1.get_role_wotsxr_k$() === 'cadet' && element_1.get_isActive_quafmh_k$()) {
        destination_1.add_utx5q5_k$(element_1);
      }
    }
    // Inline function 'kotlin.collections.sortedBy' call
    // Inline function 'kotlin.comparisons.compareBy' call
    var tmp_1 = renderAdminHomeContent$lambda_1;
    var tmp$ret$13 = new sam$kotlin_Comparator$0_0(tmp_1);
    var cadets = sortedWith(destination_1, tmp$ret$13);
    var emptyLoadBtn = !loading && allUsers.isEmpty_y1axqb_k$() ? '<p>\u0421\u043F\u0438\u0441\u043E\u043A \u043F\u0443\u0441\u0442. <button type="button" id="sd-admin-home-load" class="sd-btn sd-btn-primary">\u0417\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044C<\/button><\/p>' : '';
    var topSlotContent = emptyLoadBtn;
    var topSlot = '<div class="sd-admin-home-top-slot">' + topSlotContent + '<\/div>';
    var newbiesCards = joinToString(newbies, '', VOID, VOID, VOID, VOID, renderAdminHomeContent$lambda_2);
    var newbiesOpen = get_appState().get_adminNewbiesSectionOpen_l2t6yo_k$() ? ' open' : '';
    var newbiesContent = newbies.isEmpty_y1axqb_k$() ? '<p class="sd-muted">\u041D\u0435\u0442 \u043D\u043E\u0432\u044B\u0445 \u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u0435\u043B\u0435\u0439. \u041F\u043E\u0441\u043B\u0435 \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u0438 \u043E\u043D\u0438 \u043F\u043E\u044F\u0432\u044F\u0442\u0441\u044F \u0437\u0434\u0435\u0441\u044C; \u043F\u043E\u0441\u043B\u0435 \u0430\u043A\u0442\u0438\u0432\u0430\u0446\u0438\u0438 \u043F\u0435\u0440\u0435\u0439\u0434\u0443\u0442 \u0432 \u0440\u0430\u0437\u0434\u0435\u043B \xAB\u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440\u044B\xBB \u0438\u043B\u0438 \xAB\u041A\u0443\u0440\u0441\u0430\u043D\u0442\u044B\xBB \u043F\u043E \u0432\u044B\u0431\u0440\u0430\u043D\u043D\u043E\u0439 \u0440\u043E\u043B\u0438.<\/p>' : newbiesCards;
    var newbiesBlock = '<details class="sd-block sd-details-block" data-admin-section="newbies"' + newbiesOpen + '><summary class="sd-block-title">\u0412\u043D\u043E\u0432\u044C \u043F\u0440\u0438\u043D\u044F\u0442\u044B\u0435 (' + newbies.get_size_woubt6_k$() + ')<\/summary><div class="sd-admin-cards">' + newbiesContent + '<\/div><\/details>';
    var assignInstructorId = get_appState().get_adminAssignInstructorId_yfdvdf_k$();
    var assignCadetId = get_appState().get_adminAssignCadetId_fdosjf_k$();
    var tmp_2;
    if (!(assignInstructorId == null)) {
      // Inline function 'kotlin.collections.find' call
      var tmp$ret$16;
      $l$block: {
        // Inline function 'kotlin.collections.firstOrNull' call
        var _iterator__ex2g4s_2 = instructors.iterator_jk1svi_k$();
        while (_iterator__ex2g4s_2.hasNext_bitz1p_k$()) {
          var element_2 = _iterator__ex2g4s_2.next_20eer_k$();
          if (element_2.get_id_kntnx8_k$() === assignInstructorId) {
            tmp$ret$16 = element_2;
            break $l$block;
          }
        }
        tmp$ret$16 = null;
      }
      var inst = tmp$ret$16;
      var tmp_3;
      if (inst == null) {
        tmp_3 = null;
      } else {
        // Inline function 'kotlin.let' call
        tmp_3 = escapeHtml(formatShortName(inst.get_fullName_9skygt_k$()));
      }
      var tmp1_elvis_lhs = tmp_3;
      var instShortName = tmp1_elvis_lhs == null ? '\u2014' : tmp1_elvis_lhs;
      // Inline function 'kotlin.collections.filter' call
      // Inline function 'kotlin.collections.filterTo' call
      var destination_2 = ArrayList_init_$Create$();
      var _iterator__ex2g4s_3 = cadets.iterator_jk1svi_k$();
      while (_iterator__ex2g4s_3.hasNext_bitz1p_k$()) {
        var element_3 = _iterator__ex2g4s_3.next_20eer_k$();
        if (element_3.get_assignedInstructorId_laxw6p_k$() == assignInstructorId) {
          destination_2.add_utx5q5_k$(element_3);
        }
      }
      // Inline function 'kotlin.collections.sortedBy' call
      // Inline function 'kotlin.comparisons.compareBy' call
      var tmp_4 = renderAdminHomeContent$lambda_3;
      var tmp$ret$23 = new sam$kotlin_Comparator$0_0(tmp_4);
      var currentCadets = sortedWith(destination_2, tmp$ret$23);
      var currentSectionRows = joinToString(currentCadets, '', VOID, VOID, VOID, VOID, renderAdminHomeContent$lambda_4);
      var currentInstructorSection = '<div class="sd-assign-section sd-assign-section-current"><h4 class="sd-assign-section-title">\u041A\u0443\u0440\u0441\u0430\u043D\u0442\u044B \u0438\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440\u0430 (\u0443\u0436\u0435 \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D\u044B): ' + instShortName + ' (' + currentCadets.get_size_woubt6_k$() + ')<\/h4><div class="sd-assign-section-list">' + (currentCadets.isEmpty_y1axqb_k$() ? '<p class="sd-muted">\u041D\u0435\u0442 \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D\u043D\u044B\u0445 \u043A\u0443\u0440\u0441\u0430\u043D\u0442\u043E\u0432.<\/p>' : currentSectionRows) + '<\/div><\/div>';
      // Inline function 'kotlin.collections.filter' call
      // Inline function 'kotlin.collections.filterTo' call
      var destination_3 = ArrayList_init_$Create$();
      var _iterator__ex2g4s_4 = cadets.iterator_jk1svi_k$();
      while (_iterator__ex2g4s_4.hasNext_bitz1p_k$()) {
        var element_4 = _iterator__ex2g4s_4.next_20eer_k$();
        if (element_4.get_assignedInstructorId_laxw6p_k$() == null) {
          destination_3.add_utx5q5_k$(element_4);
        }
      }
      // Inline function 'kotlin.collections.sortedBy' call
      // Inline function 'kotlin.comparisons.compareBy' call
      var tmp_5 = renderAdminHomeContent$lambda_5;
      var tmp$ret$28 = new sam$kotlin_Comparator$0_0(tmp_5);
      var unassigned = sortedWith(destination_3, tmp$ret$28);
      // Inline function 'kotlin.collections.filter' call
      // Inline function 'kotlin.collections.filterTo' call
      var destination_4 = ArrayList_init_$Create$();
      var _iterator__ex2g4s_5 = instructors.iterator_jk1svi_k$();
      while (_iterator__ex2g4s_5.hasNext_bitz1p_k$()) {
        var element_5 = _iterator__ex2g4s_5.next_20eer_k$();
        if (!(element_5.get_id_kntnx8_k$() === assignInstructorId)) {
          destination_4.add_utx5q5_k$(element_5);
        }
      }
      // Inline function 'kotlin.collections.sortedBy' call
      // Inline function 'kotlin.comparisons.compareBy' call
      var tmp_6 = renderAdminHomeContent$lambda_6;
      var tmp$ret$33 = new sam$kotlin_Comparator$0_0(tmp_6);
      var otherInstructors = sortedWith(destination_4, tmp$ret$33);
      var newbiesRows = joinToString(unassigned, '', VOID, VOID, VOID, VOID, renderAdminHomeContent$lambda_7(assignInstructorId));
      var newbiesSection = '<div class="sd-assign-section"><h4 class="sd-assign-section-title">\u0412\u043D\u043E\u0432\u044C \u043F\u0440\u0438\u043D\u044F\u0442\u044B\u0435 (' + unassigned.get_size_woubt6_k$() + ')<\/h4><div class="sd-assign-section-list">' + (unassigned.isEmpty_y1axqb_k$() ? '<p class="sd-muted">\u041D\u0435\u0442 \u043A\u0443\u0440\u0441\u0430\u043D\u0442\u043E\u0432 \u0431\u0435\u0437 \u0438\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440\u0430.<\/p>' : newbiesRows) + '<\/div><\/div>';
      var instructorsSections = joinToString(otherInstructors, '', VOID, VOID, VOID, VOID, renderAdminHomeContent$lambda_8(cadets, assignInstructorId));
      tmp_2 = '<div class="sd-assign-panel" id="sd-assign-panel"><h3 class="sd-assign-panel-title">\u041D\u0430\u0437\u043D\u0430\u0447\u0438\u0442\u044C \u043A\u0443\u0440\u0441\u0430\u043D\u0442\u0430 \u0438\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440\u0443: ' + instShortName + '<\/h3>' + currentInstructorSection + newbiesSection + instructorsSections + '<p class="sd-assign-panel-actions"><button type="button" id="sd-admin-assign-cancel" class="sd-btn sd-assign-close-btn">\u0417\u0430\u043A\u0440\u044B\u0442\u044C<\/button><\/p><\/div>';
    } else if (!(assignCadetId == null)) {
      // Inline function 'kotlin.collections.find' call
      var tmp$ret$36;
      $l$block_0: {
        // Inline function 'kotlin.collections.firstOrNull' call
        var _iterator__ex2g4s_6 = cadets.iterator_jk1svi_k$();
        while (_iterator__ex2g4s_6.hasNext_bitz1p_k$()) {
          var element_6 = _iterator__ex2g4s_6.next_20eer_k$();
          if (element_6.get_id_kntnx8_k$() === assignCadetId) {
            tmp$ret$36 = element_6;
            break $l$block_0;
          }
        }
        tmp$ret$36 = null;
      }
      var cadet = tmp$ret$36;
      var tmp_7;
      if (cadet == null) {
        tmp_7 = null;
      } else {
        // Inline function 'kotlin.let' call
        tmp_7 = escapeHtml(formatShortName(cadet.get_fullName_9skygt_k$()));
      }
      var tmp3_elvis_lhs = tmp_7;
      var cadetShortName = tmp3_elvis_lhs == null ? '\u2014' : tmp3_elvis_lhs;
      var instRows = joinToString(instructors, '', VOID, VOID, VOID, VOID, renderAdminHomeContent$lambda_9(assignCadetId));
      tmp_2 = '<div class="sd-assign-panel" id="sd-assign-panel"><h3 class="sd-assign-panel-title">\u041D\u0430\u0437\u043D\u0430\u0447\u0438\u0442\u044C \u043A\u0443\u0440\u0441\u0430\u043D\u0442\u0430: ' + cadetShortName + '<\/h3><div class="sd-assign-section"><h4 class="sd-assign-section-title">\u0412\u044B\u0431\u0435\u0440\u0438\u0442\u0435 \u0438\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440\u0430<\/h4><div class="sd-assign-section-list">' + instRows + '<\/div><\/div><p class="sd-assign-panel-actions"><button type="button" id="sd-admin-assign-cancel" class="sd-btn sd-assign-close-btn">\u0417\u0430\u043A\u0440\u044B\u0442\u044C<\/button><\/p><\/div>';
    } else {
      tmp_2 = '';
    }
    var assignBlock = tmp_2;
    var instCards = joinToString(instructors, '', VOID, VOID, VOID, VOID, renderAdminHomeContent$lambda_10(cadets));
    var cadetCards = joinToString(cadets, '', VOID, VOID, VOID, VOID, renderAdminHomeContent$lambda_11(instructors));
    var instOpen = get_appState().get_adminInstructorsSectionOpen_frw1fj_k$() ? ' open' : '';
    var cadetOpen = get_appState().get_adminCadetsSectionOpen_e2mf91_k$() ? ' open' : '';
    var modalId = get_appState().get_adminInstructorCadetsModalId_38kovt_k$();
    var tmp_8;
    if (!(modalId == null)) {
      // Inline function 'kotlin.collections.find' call
      var tmp$ret$41;
      $l$block_1: {
        // Inline function 'kotlin.collections.firstOrNull' call
        var _iterator__ex2g4s_7 = instructors.iterator_jk1svi_k$();
        while (_iterator__ex2g4s_7.hasNext_bitz1p_k$()) {
          var element_7 = _iterator__ex2g4s_7.next_20eer_k$();
          if (element_7.get_id_kntnx8_k$() === modalId) {
            tmp$ret$41 = element_7;
            break $l$block_1;
          }
        }
        tmp$ret$41 = null;
      }
      var inst_0 = tmp$ret$41;
      // Inline function 'kotlin.collections.filter' call
      // Inline function 'kotlin.collections.filterTo' call
      var destination_5 = ArrayList_init_$Create$();
      var _iterator__ex2g4s_8 = cadets.iterator_jk1svi_k$();
      while (_iterator__ex2g4s_8.hasNext_bitz1p_k$()) {
        var element_8 = _iterator__ex2g4s_8.next_20eer_k$();
        if (element_8.get_assignedInstructorId_laxw6p_k$() == modalId) {
          destination_5.add_utx5q5_k$(element_8);
        }
      }
      // Inline function 'kotlin.collections.sortedBy' call
      // Inline function 'kotlin.comparisons.compareBy' call
      var tmp_9 = renderAdminHomeContent$lambda_12;
      var tmp$ret$46 = new sam$kotlin_Comparator$0_0(tmp_9);
      var modalCadets = sortedWith(destination_5, tmp$ret$46);
      var tmp5_elvis_lhs = inst_0 == null ? null : inst_0.get_fullName_9skygt_k$();
      var instName = escapeHtml(tmp5_elvis_lhs == null ? '\u2014' : tmp5_elvis_lhs);
      var listItems = joinToString(modalCadets, '', VOID, VOID, VOID, VOID, renderAdminHomeContent$lambda_13);
      tmp_8 = '<div class="sd-modal-overlay" id="sd-admin-cadets-modal-overlay"><div class="sd-modal sd-admin-cadets-modal"><h3 class="sd-modal-title">\u041A\u0443\u0440\u0441\u0430\u043D\u0442\u044B \u0438\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440\u0430: ' + instName + '<\/h3><ul class="sd-instructor-cadets-list">' + listItems + '<\/ul><p class="sd-modal-actions"><button type="button" id="sd-admin-cadets-modal-close" class="sd-btn sd-assign-close-btn">\u0417\u0430\u043A\u0440\u044B\u0442\u044C<\/button><\/p><\/div><\/div>';
    } else {
      tmp_8 = '';
    }
    var cadetsModalHtml = tmp_8;
    return '<h2>\u0413\u043B\u0430\u0432\u043D\u0430\u044F<\/h2>' + topSlot + newbiesBlock + '<details class="sd-block sd-details-block" data-admin-section="instructors"' + instOpen + '><summary class="sd-block-title">\u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440\u044B (' + instructors.get_size_woubt6_k$() + ')<\/summary><div class="sd-admin-cards">' + instCards + '<\/div><\/details>' + assignBlock + '<details class="sd-block sd-details-block" data-admin-section="cadets"' + cadetOpen + '><summary class="sd-block-title">\u041A\u0443\u0440\u0441\u0430\u043D\u0442\u044B (' + cadets.get_size_woubt6_k$() + ')<\/summary><div class="sd-admin-cards">' + cadetCards + '<\/div><\/details>' + cadetsModalHtml;
  }
  function renderInstructorHomeContent(user, version) {
    _init_properties_Main_kt__xi25uv();
    var loading = get_appState().get_recordingLoading_h2yjak_k$();
    // Inline function 'kotlin.collections.sortedBy' call
    var this_0 = get_appState().get_instructorCadets_elsrtw_k$();
    // Inline function 'kotlin.comparisons.compareBy' call
    var tmp = renderInstructorHomeContent$lambda;
    var tmp$ret$0 = new sam$kotlin_Comparator$0_1(tmp);
    var cadets = sortedWith(this_0, tmp$ret$0);
    // Inline function 'kotlin.collections.filter' call
    var tmp0 = get_appState().get_recordingSessions_veidxh_k$();
    // Inline function 'kotlin.collections.filterTo' call
    var destination = ArrayList_init_$Create$();
    var _iterator__ex2g4s = tmp0.iterator_jk1svi_k$();
    while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
      var element = _iterator__ex2g4s.next_20eer_k$();
      if (element.get_status_jnf6d7_k$() === 'scheduled' || element.get_status_jnf6d7_k$() === 'inProgress') {
        destination.add_utx5q5_k$(element);
      }
    }
    var sessions = take(destination, 20);
    var allSessions = get_appState().get_recordingSessions_veidxh_k$();
    var loadingLine = loading ? '<p class="sd-loading-text">\u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430\u2026<\/p>' : '';
    var tmp_0;
    if (cadets.isEmpty_y1axqb_k$()) {
      tmp_0 = '<p class="sd-muted">\u041D\u0435\u0442 \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D\u043D\u044B\u0445 \u043A\u0443\u0440\u0441\u0430\u043D\u0442\u043E\u0432<\/p>';
    } else {
      tmp_0 = joinToString(cadets, '', VOID, VOID, VOID, VOID, renderInstructorHomeContent$lambda_0(allSessions));
    }
    var cadetsListHtml = tmp_0;
    var sessList = joinToString(sessions, '', VOID, VOID, VOID, VOID, renderInstructorHomeContent$lambda_1);
    var iconPerson = '<span class="sd-profile-icon" aria-hidden="true"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/><\/svg><\/span>';
    var iconEmail = '<span class="sd-profile-icon" aria-hidden="true"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/><\/svg><\/span>';
    var iconPhone = '<span class="sd-profile-icon" aria-hidden="true"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z"/><\/svg><\/span>';
    var iconBadge = '<span class="sd-profile-icon" aria-hidden="true"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M20 7h-5V4c0-1.1-.9-2-2-2h-2C9.9 2 9 2.9 9 4v3H4c-1.1 0-2 .9-2 2v11c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V9c0-1.1-.9-2-2-2zm-9-3h2v3h-2V4zm9 16H4V9h5c0 1.1.9 2 2 2h2c1.1 0 2-.9 2-2h5v11zm-9-4l2 2 4-4"/><\/svg><\/span>';
    var iconTicket = '<span class="sd-profile-icon" aria-hidden="true"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M22 10V6c0-1.1-.9-2-2-2H4c-1.1 0-1.99.9-1.99 2v4c1.1 0 1.99.9 1.99 2s-.89 2-2 2v4c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2v-4c-1.1 0-2-.9-2-2s.9-2 2-2zm-2-1.46c-1.19.69-2 1.99-2 3.46s.81 2.77 2 3.46V18H4v-2.54c1.19-.69 2-1.99 2-3.46 0-1.48-.8-2.77-1.99-3.46L4 6h16v2.54z"/><\/svg><\/span>';
    // Inline function 'kotlin.text.ifBlank' call
    var this_1 = user.get_fullName_9skygt_k$();
    var tmp_1;
    if (isBlank(this_1)) {
      tmp_1 = '\u2014';
    } else {
      tmp_1 = this_1;
    }
    var tmp$ret$6 = tmp_1;
    var tmp_2 = escapeHtml(tmp$ret$6);
    // Inline function 'kotlin.text.ifBlank' call
    var this_2 = user.get_email_iqwbqr_k$();
    var tmp_3;
    if (isBlank(this_2)) {
      tmp_3 = '\u2014';
    } else {
      tmp_3 = this_2;
    }
    var tmp$ret$8 = tmp_3;
    var tmp_4 = escapeHtml(tmp$ret$8);
    // Inline function 'kotlin.text.ifBlank' call
    var this_3 = user.get_phone_iwv5tx_k$();
    var tmp_5;
    if (isBlank(this_3)) {
      tmp_5 = '\u2014';
    } else {
      tmp_5 = this_3;
    }
    var tmp$ret$10 = tmp_5;
    var profileCard = '\n        <div class="sd-profile-card">\n            <div class="sd-profile-card-bg"><\/div>\n            <div class="sd-profile-card-overlay"><\/div>\n            <div class="sd-profile-card-shimmer" aria-hidden="true"><\/div>\n            <div class="sd-profile-card-inner">\n                <h3 class="sd-profile-card-title">' + iconPerson + ' \u041F\u0440\u043E\u0444\u0438\u043B\u044C \u0438\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440\u0430<\/h3>\n                <div class="sd-profile-row">' + iconPerson + '<span class="sd-profile-label">\u0424\u0418\u041E:<\/span><span class="sd-profile-value">' + tmp_2 + '<\/span><\/div>\n                <div class="sd-profile-row">' + iconEmail + '<span class="sd-profile-label">Email:<\/span><span class="sd-profile-value">' + tmp_4 + '<\/span><\/div>\n                <div class="sd-profile-row">' + iconPhone + '<span class="sd-profile-label">\u0422\u0435\u043B.:<\/span><span class="sd-profile-value">' + escapeHtml(tmp$ret$10) + '<\/span><\/div>\n                <div class="sd-profile-row">' + iconBadge + '<span class="sd-profile-label">\u0420\u043E\u043B\u044C:<\/span><span class="sd-profile-value">\u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440<\/span><\/div>\n                <div class="sd-profile-row sd-profile-row-balance">' + iconTicket + '<span class="sd-profile-label">\u0411\u0430\u043B\u0430\u043D\u0441 \u0442\u0430\u043B\u043E\u043D\u043E\u0432:<\/span><span class="sd-profile-value sd-balance-badge">' + user.get_balance_4cdzil_k$() + '<\/span><\/div>\n            <\/div>\n        <\/div>';
    return '<h2>\u0413\u043B\u0430\u0432\u043D\u0430\u044F<\/h2>\n        ' + profileCard + '\n        <div class="sd-block"><h3 class="sd-block-title">\u041C\u043E\u0438 \u043A\u0443\u0440\u0441\u0430\u043D\u0442\u044B (' + cadets.get_size_woubt6_k$() + ')<\/h3><div class="sd-cadet-cards">' + cadetsListHtml + '<\/div><\/div>\n        ' + loadingLine + '\n        <div class="sd-block"><h3 class="sd-block-title">\u041C\u043E\u0439 \u0433\u0440\u0430\u0444\u0438\u043A<\/h3><div class="sd-list">' + sessList + '<\/div><\/div>\n        <p class="sd-version">\u0412\u0435\u0440\u0441\u0438\u044F: ' + version + '<\/p>';
  }
  function renderCadetHomeContent(user, version) {
    _init_properties_Main_kt__xi25uv();
    var inst = get_appState().get_cadetInstructor_oyqk5l_k$();
    var instText = !(inst == null) ? escapeHtml(inst.get_fullName_9skygt_k$()) : !(user.get_assignedInstructorId_laxw6p_k$() == null) ? '\u0437\u0430\u0433\u0440\u0443\u0437\u043A\u0430\u2026' : '\u043D\u0435 \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D';
    var loading = get_appState().get_recordingLoading_h2yjak_k$();
    // Inline function 'kotlin.collections.filter' call
    var tmp0 = get_appState().get_recordingSessions_veidxh_k$();
    // Inline function 'kotlin.collections.filterTo' call
    var destination = ArrayList_init_$Create$();
    var _iterator__ex2g4s = tmp0.iterator_jk1svi_k$();
    while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
      var element = _iterator__ex2g4s.next_20eer_k$();
      if (element.get_status_jnf6d7_k$() === 'scheduled') {
        destination.add_utx5q5_k$(element);
      }
    }
    var sessions = take(destination, 20);
    var loadingLine = loading ? '<p class="sd-loading-text">\u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430\u2026<\/p>' : '';
    var sessList = joinToString(sessions, '', VOID, VOID, VOID, VOID, renderCadetHomeContent$lambda);
    return '<h2>\u0413\u043B\u0430\u0432\u043D\u0430\u044F<\/h2>\n        <div class="sd-home-card"><strong>\u0411\u0430\u043B\u0430\u043D\u0441:<\/strong> ' + user.get_balance_4cdzil_k$() + ' \u0442\u0430\u043B\u043E\u043D\u043E\u0432.<\/div>\n        <div class="sd-home-card"><strong>\u041C\u043E\u0439 \u0438\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440:<\/strong> ' + instText + '<\/div>\n        ' + loadingLine + '\n        <div class="sd-block"><h3 class="sd-block-title">\u041C\u043E\u0451 \u0432\u043E\u0436\u0434\u0435\u043D\u0438\u0435<\/h3><div class="sd-list">' + sessList + '<\/div><\/div>\n        <p class="sd-version">\u0412\u0435\u0440\u0441\u0438\u044F: ' + version + '<\/p>';
  }
  function renderRecordingTabContent(user) {
    _init_properties_Main_kt__xi25uv();
    var loading = get_appState().get_recordingLoading_h2yjak_k$();
    var windows = get_appState().get_recordingOpenWindows_ecepd_k$();
    var sessions = get_appState().get_recordingSessions_veidxh_k$();
    var loadingLine = loading ? '<p class="sd-loading-text">\u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430\u2026 <button type="button" id="sd-stop-loading" class="sd-btn sd-btn-small sd-btn-secondary">\u041F\u043E\u043A\u0430\u0437\u0430\u0442\u044C \u043F\u0443\u0441\u0442\u043E<\/button><\/p>' : '';
    var tmp;
    switch (user.get_role_wotsxr_k$()) {
      case 'instructor':
        var list = joinToString(windows, '', VOID, VOID, VOID, VOID, renderRecordingTabContent$lambda);
        // Inline function 'kotlin.collections.filter' call

        // Inline function 'kotlin.collections.filterTo' call

        var destination = ArrayList_init_$Create$();
        var _iterator__ex2g4s = sessions.iterator_jk1svi_k$();
        while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
          var element = _iterator__ex2g4s.next_20eer_k$();
          if (element.get_status_jnf6d7_k$() === 'scheduled' || element.get_status_jnf6d7_k$() === 'inProgress') {
            destination.add_utx5q5_k$(element);
          }
        }

        var tmp_0 = take(destination, 20);
        var sessionsList = joinToString(tmp_0, '', VOID, VOID, VOID, VOID, renderRecordingTabContent$lambda_0);
        tmp = '<h2>\u0417\u0430\u043F\u0438\u0441\u044C<\/h2>' + loadingLine + '\n               <div class="sd-recording-section"><h3>\u0421\u0432\u043E\u0431\u043E\u0434\u043D\u044B\u0435 \u043E\u043A\u043D\u0430<\/h3><div class="sd-list">' + list + '<\/div><\/div>\n               <div class="sd-recording-section"><h3>\u0414\u043E\u0431\u0430\u0432\u0438\u0442\u044C \u043E\u043A\u043D\u043E<\/h3><p><input type="datetime-local" id="sd-new-window-dt" class="sd-input" /> <button type="button" id="sd-add-window" class="sd-btn sd-btn-primary">\u0414\u043E\u0431\u0430\u0432\u0438\u0442\u044C<\/button><\/p><\/div>\n               <div class="sd-recording-section"><h3>\u0411\u043B\u0438\u0436\u0430\u0439\u0448\u0438\u0435 \u0437\u0430\u043D\u044F\u0442\u0438\u044F<\/h3><div class="sd-list">' + sessionsList + '<\/div><\/div>';
        break;
      case 'cadet':
        var slotsHtml = joinToString(windows, '', VOID, VOID, VOID, VOID, renderRecordingTabContent$lambda_1);
        // Inline function 'kotlin.collections.filter' call

        // Inline function 'kotlin.collections.filterTo' call

        var destination_0 = ArrayList_init_$Create$();
        var _iterator__ex2g4s_0 = sessions.iterator_jk1svi_k$();
        while (_iterator__ex2g4s_0.hasNext_bitz1p_k$()) {
          var element_0 = _iterator__ex2g4s_0.next_20eer_k$();
          if (element_0.get_status_jnf6d7_k$() === 'scheduled') {
            destination_0.add_utx5q5_k$(element_0);
          }
        }

        var tmp_1 = take(destination_0, 10);
        var myRecords = joinToString(tmp_1, '', VOID, VOID, VOID, VOID, renderRecordingTabContent$lambda_2);
        tmp = '<h2>\u0417\u0430\u043F\u0438\u0441\u044C \u043D\u0430 \u0432\u043E\u0436\u0434\u0435\u043D\u0438\u0435<\/h2>' + loadingLine + '\n               <div class="sd-recording-section"><h3>\u0421\u0432\u043E\u0431\u043E\u0434\u043D\u044B\u0435 \u0441\u043B\u043E\u0442\u044B<\/h3><div class="sd-list">' + slotsHtml + '<\/div><\/div>\n               <div class="sd-recording-section"><h3>\u041C\u043E\u0438 \u0437\u0430\u043F\u0438\u0441\u0438<\/h3><div class="sd-list">' + myRecords + '<\/div><\/div>';
        break;
      default:
        tmp = '<h2>\u0417\u0430\u043F\u0438\u0441\u044C<\/h2><p>\u0414\u043E\u0441\u0442\u0443\u043F\u043D\u043E \u0438\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440\u0443 \u0438 \u043A\u0443\u0440\u0441\u0430\u043D\u0442\u0443.<\/p>';
        break;
    }
    return tmp;
  }
  function renderHistoryTabContent(user) {
    _init_properties_Main_kt__xi25uv();
    var loadingLine = get_appState().get_historyLoading_ytzv9t_k$() ? '<p class="sd-loading-text">\u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430\u2026 <button type="button" id="sd-stop-history-loading" class="sd-btn sd-btn-small sd-btn-secondary">\u041F\u043E\u043A\u0430\u0437\u0430\u0442\u044C \u043F\u0443\u0441\u0442\u043E<\/button><\/p>' : '';
    var sessions = take(get_appState().get_historySessions_72jo14_k$(), 30);
    var balance = take(get_appState().get_historyBalance_nnmchd_k$(), 50);
    var sessionsHtml = joinToString(sessions, '', VOID, VOID, VOID, VOID, renderHistoryTabContent$lambda);
    if (user.get_role_wotsxr_k$() === 'admin') {
      var users = get_appState().get_balanceAdminUsers_ico6ks_k$();
      var balanceHtml = joinToString(balance, '', VOID, VOID, VOID, VOID, renderHistoryTabContent$lambda_0(users));
      return '<h2>\u0418\u0441\u0442\u043E\u0440\u0438\u044F<\/h2>' + loadingLine + '\n            <div class="sd-block"><h3 class="sd-block-title">\u0417\u0430\u0447\u0438\u0441\u043B\u0435\u043D\u0438\u044F \u0438 \u0441\u043F\u0438\u0441\u0430\u043D\u0438\u044F (' + balance.get_size_woubt6_k$() + ')<\/h3><div class="sd-list">' + balanceHtml + '<\/div><\/div>\n            <div class="sd-block"><h3 class="sd-block-title">\u0412\u043E\u0436\u0434\u0435\u043D\u0438\u0435<\/h3><p>\u0417\u0430\u0432\u0435\u0440\u0448\u0451\u043D\u043D\u044B\u0435 \u0438 \u043E\u0442\u043C\u0435\u043D\u0451\u043D\u043D\u044B\u0435 \u0432\u043E\u0436\u0434\u0435\u043D\u0438\u044F \u2014 \u0432 \u043F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u0438.<\/p><\/div>\n            <div class="sd-block"><h3 class="sd-block-title">\u0427\u0430\u0442<\/h3><p>\u041F\u0440\u043E\u0441\u043C\u043E\u0442\u0440 \u043F\u0435\u0440\u0435\u043F\u0438\u0441\u043A\u0438 \u2014 \u0432\u044B\u0431\u0440\u0430\u0442\u044C \u043A\u043E\u043D\u0442\u0430\u043A\u0442 \u0432\u043E \u0432\u043A\u043B\u0430\u0434\u043A\u0435 \u0427\u0430\u0442.<\/p><\/div>';
    }
    var balanceHtml_0 = joinToString(balance, '', VOID, VOID, VOID, VOID, renderHistoryTabContent$lambda_1);
    return '<h2>\u0418\u0441\u0442\u043E\u0440\u0438\u044F<\/h2>' + loadingLine + '<div class="sd-block"><h3 class="sd-block-title">\u0417\u0430\u043D\u044F\u0442\u0438\u044F (' + sessions.get_size_woubt6_k$() + ')<\/h3><div class="sd-list">' + sessionsHtml + '<\/div><\/div><div class="sd-block"><h3 class="sd-block-title">\u0411\u0430\u043B\u0430\u043D\u0441 (' + balance.get_size_woubt6_k$() + ')<\/h3><div class="sd-list">' + balanceHtml_0 + '<\/div><\/div>';
  }
  function renderSettingsTabContent(user) {
    _init_properties_Main_kt__xi25uv();
    return '<h2>\u041D\u0430\u0441\u0442\u0440\u043E\u0439\u043A\u0438<\/h2>\n       <label>\u0424\u0418\u041E<\/label><input type="text" id="sd-settings-fullName" class="sd-input" value="' + escapeHtml(user.get_fullName_9skygt_k$()) + '" />\n       <label>\u0422\u0435\u043B\u0435\u0444\u043E\u043D<\/label><input type="tel" id="sd-settings-phone" class="sd-input" value="' + escapeHtml(user.get_phone_iwv5tx_k$()) + '" />\n       <button type="button" id="sd-settings-save" class="sd-btn sd-btn-primary">\u0421\u043E\u0445\u0440\u0430\u043D\u0438\u0442\u044C \u043F\u0440\u043E\u0444\u0438\u043B\u044C<\/button>\n       <p style="margin-top:16px">\u0421\u043C\u0435\u043D\u0438\u0442\u044C \u043F\u0430\u0440\u043E\u043B\u044C:<\/p>\n       <label>\u041D\u043E\u0432\u044B\u0439 \u043F\u0430\u0440\u043E\u043B\u044C<\/label><input type="password" id="sd-settings-newpassword" class="sd-input" placeholder="\u043C\u0438\u043D. 6 \u0441\u0438\u043C\u0432\u043E\u043B\u043E\u0432" />\n       <button type="button" id="sd-settings-password" class="sd-btn sd-btn-secondary">\u0421\u043C\u0435\u043D\u0438\u0442\u044C \u043F\u0430\u0440\u043E\u043B\u044C<\/button>';
  }
  function renderBalanceTabContent(user) {
    _init_properties_Main_kt__xi25uv();
    if (!(user.get_role_wotsxr_k$() === 'admin'))
      return '<h2>\u0411\u0430\u043B\u0430\u043D\u0441<\/h2><p>\u0412\u0430\u0448 \u0431\u0430\u043B\u0430\u043D\u0441: ' + user.get_balance_4cdzil_k$() + ' \u0442\u0430\u043B\u043E\u043D\u043E\u0432.<\/p>';
    var loadingLine = get_appState().get_balanceAdminLoading_slms8w_k$() ? '<p class="sd-loading-text">\u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430\u2026 <button type="button" id="sd-stop-balance-loading" class="sd-btn sd-btn-small sd-btn-secondary">\u041F\u043E\u043A\u0430\u0437\u0430\u0442\u044C \u043F\u0443\u0441\u0442\u043E<\/button><\/p>' : '';
    var users = get_appState().get_balanceAdminUsers_ico6ks_k$();
    var emptyBalanceBtn = !get_appState().get_balanceAdminLoading_slms8w_k$() && users.isEmpty_y1axqb_k$() ? '<p>\u0421\u043F\u0438\u0441\u043E\u043A \u043F\u0443\u0441\u0442. <button type="button" id="sd-balance-load" class="sd-btn sd-btn-primary">\u0417\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044C<\/button><\/p>' : '';
    var selectedId = get_appState().get_balanceAdminSelectedUserId_4lrwub_k$();
    // Inline function 'kotlin.collections.find' call
    var tmp$ret$1;
    $l$block: {
      // Inline function 'kotlin.collections.firstOrNull' call
      var _iterator__ex2g4s = users.iterator_jk1svi_k$();
      while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
        var element = _iterator__ex2g4s.next_20eer_k$();
        if (element.get_id_kntnx8_k$() === selectedId) {
          tmp$ret$1 = element;
          break $l$block;
        }
      }
      tmp$ret$1 = null;
    }
    var selectedUser = tmp$ret$1;
    // Inline function 'kotlin.collections.filter' call
    // Inline function 'kotlin.collections.filterTo' call
    var destination = ArrayList_init_$Create$();
    var _iterator__ex2g4s_0 = users.iterator_jk1svi_k$();
    while (_iterator__ex2g4s_0.hasNext_bitz1p_k$()) {
      var element_0 = _iterator__ex2g4s_0.next_20eer_k$();
      if (element_0.get_role_wotsxr_k$() === 'instructor') {
        destination.add_utx5q5_k$(element_0);
      }
    }
    var instructors = destination;
    // Inline function 'kotlin.collections.filter' call
    // Inline function 'kotlin.collections.filterTo' call
    var destination_0 = ArrayList_init_$Create$();
    var _iterator__ex2g4s_1 = users.iterator_jk1svi_k$();
    while (_iterator__ex2g4s_1.hasNext_bitz1p_k$()) {
      var element_1 = _iterator__ex2g4s_1.next_20eer_k$();
      if (element_1.get_role_wotsxr_k$() === 'cadet') {
        destination_0.add_utx5q5_k$(element_1);
      }
    }
    var cadets = destination_0;
    var balanceCardHtml = renderBalanceTabContent$lambda;
    var instRows = joinToString(instructors, '', VOID, VOID, VOID, VOID, renderBalanceTabContent$lambda_0(balanceCardHtml));
    var cadetRows = joinToString(cadets, '', VOID, VOID, VOID, VOID, renderBalanceTabContent$lambda_1(balanceCardHtml));
    var selectedBlock = !(selectedUser == null) ? '\n        <div class="sd-block" id="sd-balance-selected-block">\n            <h3 class="sd-block-title">\u0412\u044B\u0431\u0440\u0430\u043D<\/h3>\n            <p><strong>' + escapeHtml(selectedUser.get_fullName_9skygt_k$()) + '<\/strong> (' + selectedUser.get_role_wotsxr_k$() + '). \u0411\u0430\u043B\u0430\u043D\u0441: ' + selectedUser.get_balance_4cdzil_k$() + ' \u0442\u0430\u043B\u043E\u043D\u043E\u0432.<\/p>\n            <label>\u041A\u043E\u043B\u0438\u0447\u0435\u0441\u0442\u0432\u043E \u0442\u0430\u043B\u043E\u043D\u043E\u0432<\/label><input type="number" id="sd-balance-amount" class="sd-input" value="0" min="0" />\n            <div class="sd-balance-selected-actions">\n                <button type="button" id="sd-balance-credit" class="sd-btn sd-balance-btn sd-balance-btn-credit">' + get_iconCreditSvg() + ' \u0417\u0430\u0447\u0438\u0441\u043B\u0438\u0442\u044C (+N)<\/button>\n                <button type="button" id="sd-balance-debit" class="sd-btn sd-balance-btn sd-balance-btn-debit">' + get_iconDebitSvg() + ' \u0421\u043F\u0438\u0441\u0430\u0442\u044C (\u2212N)<\/button>\n                <button type="button" id="sd-balance-set" class="sd-btn sd-balance-btn sd-balance-btn-set">' + get_iconSetSvg() + ' \u0418\u0437\u043C\u0435\u043D\u0438\u0442\u044C \u043D\u0430 (= N)<\/button>\n                <button type="button" id="sd-balance-clear-selection" class="sd-btn sd-balance-btn sd-balance-btn-clear">' + get_iconResetSvg() + ' \u0421\u0431\u0440\u043E\u0441\u0438\u0442\u044C \u0432\u044B\u0431\u043E\u0440<\/button>\n            <\/div>\n        <\/div>\n    ' : '';
    var history = take(get_appState().get_balanceAdminHistory_wo1fzc_k$(), 50);
    var typeLabel = renderBalanceTabContent$lambda_2;
    // Inline function 'kotlin.collections.sortedByDescending' call
    // Inline function 'kotlin.comparisons.compareByDescending' call
    var tmp = renderBalanceTabContent$lambda_3;
    var tmp$ret$9 = new sam$kotlin_Comparator$0_2(tmp);
    var sortedHistory = sortedWith(history, tmp$ret$9);
    // Inline function 'kotlin.collections.groupBy' call
    // Inline function 'kotlin.collections.groupByTo' call
    var destination_1 = LinkedHashMap_init_$Create$();
    var _iterator__ex2g4s_2 = sortedHistory.iterator_jk1svi_k$();
    while (_iterator__ex2g4s_2.hasNext_bitz1p_k$()) {
      var element_2 = _iterator__ex2g4s_2.next_20eer_k$();
      var key = substringBefore(formatDateTimeEkaterinburg(element_2.get_timestampMillis_cbfaxf_k$()), ', ');
      // Inline function 'kotlin.collections.getOrPut' call
      var value = destination_1.get_wei43m_k$(key);
      var tmp_0;
      if (value == null) {
        var answer = ArrayList_init_$Create$();
        destination_1.put_4fpzoq_k$(key, answer);
        tmp_0 = answer;
      } else {
        tmp_0 = value;
      }
      var list = tmp_0;
      list.add_utx5q5_k$(element_2);
    }
    var byDate = destination_1;
    var tmp_1 = byDate.get_entries_p20ztl_k$();
    var historyRows = joinToString(tmp_1, '', VOID, VOID, VOID, VOID, renderBalanceTabContent$lambda_4(users, typeLabel));
    var historyEmptyMsg = history.isEmpty_y1axqb_k$() ? '<p class="sd-muted sd-balance-history-empty">\u041D\u0435\u0442 \u0437\u0430\u043F\u0438\u0441\u0435\u0439. \u0412\u044B\u043F\u043E\u043B\u043D\u0438\u0442\u0435 \u0437\u0430\u0447\u0438\u0441\u043B\u0435\u043D\u0438\u0435 \u0438\u043B\u0438 \u0441\u043F\u0438\u0441\u0430\u043D\u0438\u0435 \u043F\u043E \u0432\u044B\u0431\u0440\u0430\u043D\u043D\u043E\u043C\u0443 \u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u0435\u043B\u044E \u2014 \u043E\u043F\u0435\u0440\u0430\u0446\u0438\u0438 \u043F\u043E\u044F\u0432\u044F\u0442\u0441\u044F \u0437\u0434\u0435\u0441\u044C.<\/p>' : '';
    var historyOpen = get_appState().get_balanceHistorySectionOpen_z8lwya_k$() ? ' open' : '';
    var historyDetailsContent = history.isEmpty_y1axqb_k$() ? historyEmptyMsg : '<div class="sd-balance-history-by-date">' + historyRows + '<\/div>';
    var historyBlock = '<details class="sd-block sd-details-block" data-balance-section="history"' + historyOpen + '><summary class="sd-block-title">\u0418\u0441\u0442\u043E\u0440\u0438\u044F \u043E\u043F\u0435\u0440\u0430\u0446\u0438\u0439 (' + history.get_size_woubt6_k$() + ')<\/summary>' + historyDetailsContent + '<\/details>';
    return '<h2>\u0411\u0430\u043B\u0430\u043D\u0441<\/h2>' + loadingLine + emptyBalanceBtn + '\n        <div class="sd-block" id="sd-balance-instructors-block"><h3 class="sd-block-title">\u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440\u044B (' + instructors.get_size_woubt6_k$() + ')<\/h3><div id="sd-balance-instructors-list" class="sd-balance-cards">' + instRows + '<\/div><\/div>\n        <div class="sd-block" id="sd-balance-cadets-block"><h3 class="sd-block-title">\u041A\u0443\u0440\u0441\u0430\u043D\u0442\u044B (' + cadets.get_size_woubt6_k$() + ')<\/h3><div id="sd-balance-cadets-list" class="sd-balance-cards">' + cadetRows + '<\/div><\/div>\n        ' + selectedBlock + '\n        ' + historyBlock;
  }
  function getPanelTabButtonsAndContent(user, tabs) {
    _init_properties_Main_kt__xi25uv();
    var appInfo = SharedFactory_getInstance().getAppInfoRepository_n0p3kz_k$().getAppInfo_40fhh5_k$();
    var selected = coerceIn(get_appState().get_selectedTabIndex_3g78ox_k$(), 0, tabs.get_size_woubt6_k$() - 1 | 0);
    // Inline function 'kotlin.collections.mapIndexed' call
    // Inline function 'kotlin.collections.mapIndexedTo' call
    var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(tabs, 10));
    var index = 0;
    var _iterator__ex2g4s = tabs.iterator_jk1svi_k$();
    while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
      var item = _iterator__ex2g4s.next_20eer_k$();
      var _unary__edvuaz = index;
      index = _unary__edvuaz + 1 | 0;
      var i = checkIndexOverflow(_unary__edvuaz);
      var cls = i === selected ? 'sd-tab sd-active' : 'sd-tab';
      var tmp$ret$0 = '<button type="button" class="' + cls + '" data-tab="' + i + '">' + item + '<\/button>';
      destination.add_utx5q5_k$(tmp$ret$0);
    }
    var tabButtons = joinToString(destination, '');
    var tabName = tabs.get_c1px32_k$(selected);
    var tmp;
    switch (tabName) {
      case '\u0413\u043B\u0430\u0432\u043D\u0430\u044F':
        switch (user.get_role_wotsxr_k$()) {
          case 'admin':
            tmp = renderAdminHomeContent();
            break;
          case 'instructor':
            tmp = renderInstructorHomeContent(user, appInfo.get_version_72w4j3_k$());
            break;
          case 'cadet':
            tmp = renderCadetHomeContent(user, appInfo.get_version_72w4j3_k$());
            break;
          default:
            tmp = '<h2>' + tabName + '<\/h2><p>\u0411\u0430\u043B\u0430\u043D\u0441: ' + user.get_balance_4cdzil_k$() + ' \u0442\u0430\u043B\u043E\u043D\u043E\u0432.<\/p><p>\u0412\u0435\u0440\u0441\u0438\u044F: ' + appInfo.get_version_72w4j3_k$() + '<\/p>';
            break;
        }

        break;
      case '\u0427\u0430\u0442':
        tmp = renderChatTabContent(user);
        break;
      case '\u0411\u0430\u043B\u0430\u043D\u0441':
        tmp = renderBalanceTabContent(user);
        break;
      case '\u0417\u0430\u043F\u0438\u0441\u044C':
      case '\u0417\u0430\u043F\u0438\u0441\u044C \u043D\u0430 \u0432\u043E\u0436\u0434\u0435\u043D\u0438\u0435':
        tmp = renderRecordingTabContent(user);
        break;
      case '\u0418\u0441\u0442\u043E\u0440\u0438\u044F':
        tmp = renderHistoryTabContent(user);
        break;
      case '\u0411\u0438\u043B\u0435\u0442\u044B':
        tmp = '<h2>\u0411\u0438\u043B\u0435\u0442\u044B \u041F\u0414\u0414<\/h2><div class="sd-tickets-content"><p>\u0411\u0438\u043B\u0435\u0442\u044B \u041F\u0414\u0414 \u2014 \u0432 \u043F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u0438: \u0442\u0435\u043C\u044B, \u0437\u043D\u0430\u043A\u0438, \u0440\u0430\u0437\u043C\u0435\u0442\u043A\u0430, \u0448\u0442\u0440\u0430\u0444\u044B, \u0431\u0438\u043B\u0435\u0442\u044B.<\/p><p><a href="https://play.google.com/store/apps/details?id=com.example.startdrive" target="_blank" rel="noopener">\u0421\u043A\u0430\u0447\u0430\u0442\u044C \u043F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u0435 StartDrive (Google Play)<\/a><\/p><p><a href="https://pdd.ru/" target="_blank" rel="noopener">\u041F\u0414\u0414 \u0420\u0424 \u043D\u0430 pdd.ru<\/a><\/p><\/div>';
        break;
      case '\u041F\u0414\u0414':
        tmp = '<h2>' + tabName + '<\/h2><p>\u041F\u0440\u0430\u0432\u0438\u043B\u0430 \u0434\u043E\u0440\u043E\u0436\u043D\u043E\u0433\u043E \u0434\u0432\u0438\u0436\u0435\u043D\u0438\u044F \u2014 \u043F\u043E\u043B\u043D\u044B\u0439 \u0444\u0443\u043D\u043A\u0446\u0438\u043E\u043D\u0430\u043B \u0432 \u043F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u0438.<\/p><p><a href="https://play.google.com/store/apps/details?id=com.example.startdrive" target="_blank" rel="noopener">\u041F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u0435 StartDrive<\/a> \xB7 <a href="https://pdd.ru/" target="_blank" rel="noopener">\u041F\u0414\u0414 \u0420\u0424 (pdd.ru)<\/a><\/p>';
        break;
      case '\u041D\u0430\u0441\u0442\u0440\u043E\u0439\u043A\u0438':
        tmp = renderSettingsTabContent(user);
        break;
      default:
        tmp = '<h2>' + tabName + '<\/h2><p>\u0420\u0430\u0437\u0434\u0435\u043B \u0432 \u0440\u0430\u0437\u0440\u0430\u0431\u043E\u0442\u043A\u0435.<\/p>';
        break;
    }
    var tabContent = tmp;
    return new Pair(tabButtons, tabContent);
  }
  function renderPanel(user, roleTitle, tabs) {
    _init_properties_Main_kt__xi25uv();
    var _destruct__k2r9zo = getPanelTabButtonsAndContent(user, tabs);
    var tabButtons = _destruct__k2r9zo.component1_7eebsc_k$();
    var tabContent = _destruct__k2r9zo.component2_7eebsb_k$();
    return trimIndent('\n        <header class="sd-header sd-panel-header">\n            <h1>StartDrive \xB7 ' + roleTitle + '<\/h1>\n            <p>' + user.get_fullName_9skygt_k$() + ' \xB7 ' + user.get_email_iqwbqr_k$() + '<\/p>\n            <button type="button" id="sd-btn-signout" class="sd-btn sd-btn-signout">\u0412\u044B\u0439\u0442\u0438<\/button>\n        <\/header>\n        <nav class="sd-tabs">' + tabButtons + '<\/nav>\n        <main class="sd-content">\n            <div class="sd-card" id="sd-card">\n                ' + tabContent + '\n            <\/div>\n        <\/main>\n    ');
  }
  function setupPanelClickDelegation(root) {
    _init_properties_Main_kt__xi25uv();
    root.addEventListener('click', setupPanelClickDelegation$lambda, true);
  }
  function attachListeners(root) {
    _init_properties_Main_kt__xi25uv();
    var tmp0_safe_receiver = document.getElementById('sd-dismiss-network-error');
    if (tmp0_safe_receiver == null)
      null;
    else {
      tmp0_safe_receiver.addEventListener('click', attachListeners$lambda);
    }
    var tmp1_safe_receiver = document.getElementById('sd-stop-loading');
    if (tmp1_safe_receiver == null)
      null;
    else {
      tmp1_safe_receiver.addEventListener('click', attachListeners$lambda_0);
    }
    var tmp2_safe_receiver = document.getElementById('sd-stop-history-loading');
    if (tmp2_safe_receiver == null)
      null;
    else {
      tmp2_safe_receiver.addEventListener('click', attachListeners$lambda_1);
    }
    var tmp3_safe_receiver = document.getElementById('sd-stop-balance-loading');
    if (tmp3_safe_receiver == null)
      null;
    else {
      tmp3_safe_receiver.addEventListener('click', attachListeners$lambda_2);
    }
    var tmp4_safe_receiver = document.getElementById('sd-chat-stop-loading');
    if (tmp4_safe_receiver == null)
      null;
    else {
      tmp4_safe_receiver.addEventListener('click', attachListeners$lambda_3);
    }
    var tmp5_safe_receiver = document.getElementById('sd-chat-refresh');
    if (tmp5_safe_receiver == null)
      null;
    else {
      tmp5_safe_receiver.addEventListener('click', attachListeners$lambda_4);
    }
    var tmp6_safe_receiver = document.getElementById('sd-admin-home-load');
    if (tmp6_safe_receiver == null)
      null;
    else {
      tmp6_safe_receiver.addEventListener('click', attachListeners$lambda_5);
    }
    var tmp7_safe_receiver = document.getElementById('sd-balance-load');
    if (tmp7_safe_receiver == null)
      null;
    else {
      tmp7_safe_receiver.addEventListener('click', attachListeners$lambda_6);
    }
    var tmp8_safe_receiver = document.getElementById('sd-admin-assign-cancel');
    if (tmp8_safe_receiver == null)
      null;
    else {
      tmp8_safe_receiver.addEventListener('click', attachListeners$lambda_7);
    }
    switch (get_appState().get_screen_jed7jp_k$().get_ordinal_ip24qg_k$()) {
      case 0:
        var tmp10_safe_receiver = document.getElementById('sd-btn-signin');
        if (tmp10_safe_receiver == null)
          null;
        else {
          tmp10_safe_receiver.addEventListener('click', attachListeners$lambda_8);
        }

        var tmp11_safe_receiver = document.getElementById('sd-btn-register');
        if (tmp11_safe_receiver == null)
          null;
        else {
          tmp11_safe_receiver.addEventListener('click', attachListeners$lambda_9);
        }

        break;
      case 1:
        var tmp12_safe_receiver = document.getElementById('sd-btn-do-register');
        if (tmp12_safe_receiver == null)
          null;
        else {
          tmp12_safe_receiver.addEventListener('click', attachListeners$lambda_10);
        }

        var tmp13_safe_receiver = document.getElementById('sd-btn-back');
        if (tmp13_safe_receiver == null)
          null;
        else {
          tmp13_safe_receiver.addEventListener('click', attachListeners$lambda_11);
        }

        break;
      case 2:
        var tmp14_safe_receiver = document.getElementById('sd-btn-check');
        if (tmp14_safe_receiver == null)
          null;
        else {
          tmp14_safe_receiver.addEventListener('click', attachListeners$lambda_12);
        }

        var tmp15_safe_receiver = document.getElementById('sd-btn-signout-pending');
        if (tmp15_safe_receiver == null)
          null;
        else {
          tmp15_safe_receiver.addEventListener('click', attachListeners$lambda_13);
        }

        break;
      case 3:
        var tmp16_safe_receiver = document.getElementById('sd-btn-signout-profile-not-found');
        if (tmp16_safe_receiver == null)
          null;
        else {
          tmp16_safe_receiver.addEventListener('click', attachListeners$lambda_14);
        }

        break;
      case 4:
      case 5:
      case 6:
        var tmp17_safe_receiver = get_appState().get_user_wovspg_k$();
        var uid = tmp17_safe_receiver == null ? null : tmp17_safe_receiver.get_id_kntnx8_k$();
        if (!(get_appState().get_selectedTabIndex_3g78ox_k$() === 2)) {
          unsubscribeChat();
        }

        var tmp = window;
        tmp.setTimeout(attachListeners$lambda_15(uid), 0);
        var u = get_appState().get_user_wovspg_k$();
        if (u == null)
          null;
        else {
          // Inline function 'kotlin.let' call
          var tmp0_safe_receiver_0 = document.getElementById('sd-btn-signout');
          if (tmp0_safe_receiver_0 == null)
            null;
          else {
            tmp0_safe_receiver_0.addEventListener('click', attachListeners$lambda_16);
          }
          var tmp1_safe_receiver_0 = document.getElementById('sd-chat-back');
          if (tmp1_safe_receiver_0 == null)
            null;
          else {
            tmp1_safe_receiver_0.addEventListener('click', attachListeners$lambda_17);
          }
          var tmp_0 = document.getElementById('sd-chat-input');
          var chatInput = tmp_0 instanceof HTMLInputElement ? tmp_0 : null;
          var tmp2_safe_receiver_0 = document.getElementById('sd-chat-send');
          if (tmp2_safe_receiver_0 == null)
            null;
          else {
            tmp2_safe_receiver_0.addEventListener('click', attachListeners$lambda_18(chatInput, uid));
          }
          if (chatInput == null)
            null;
          else {
            chatInput.addEventListener('keypress', attachListeners$lambda_19(chatInput, uid));
          }
          var tmp4_safe_receiver_0 = document.getElementById('sd-add-window');
          if (tmp4_safe_receiver_0 == null)
            null;
          else {
            tmp4_safe_receiver_0.addEventListener('click', attachListeners$lambda_20(u));
          }
          var delNodes = root.querySelectorAll('.sd-btn-delete[data-window-id]');
          var inductionVariable = 0;
          var last = delNodes.length;
          if (inductionVariable < last)
            $l$loop: do {
              var k = inductionVariable;
              inductionVariable = inductionVariable + 1 | 0;
              var tmp_1 = delNodes.item(k);
              var tmp5_elvis_lhs = tmp_1 instanceof Element ? tmp_1 : null;
              var tmp_2;
              if (tmp5_elvis_lhs == null) {
                continue $l$loop;
              } else {
                tmp_2 = tmp5_elvis_lhs;
              }
              var btn = tmp_2;
              btn.addEventListener('click', attachListeners$lambda_21(btn, u));
            }
             while (inductionVariable < last);
          var bookNodes = root.querySelectorAll('.sd-list .sd-btn-small[data-window-id]');
          var inductionVariable_0 = 0;
          var last_0 = bookNodes.length;
          if (inductionVariable_0 < last_0)
            $l$loop_1: do {
              var k_0 = inductionVariable_0;
              inductionVariable_0 = inductionVariable_0 + 1 | 0;
              var tmp_3 = bookNodes.item(k_0);
              var tmp6_elvis_lhs = tmp_3 instanceof Element ? tmp_3 : null;
              var tmp_4;
              if (tmp6_elvis_lhs == null) {
                continue $l$loop_1;
              } else {
                tmp_4 = tmp6_elvis_lhs;
              }
              var btn_0 = tmp_4;
              var tmp7_elvis_lhs = btn_0.getAttribute('data-window-id');
              var tmp_5;
              if (tmp7_elvis_lhs == null) {
                continue $l$loop_1;
              } else {
                tmp_5 = tmp7_elvis_lhs;
              }
              var wid = tmp_5;
              btn_0.addEventListener('click', attachListeners$lambda_22(wid, u));
            }
             while (inductionVariable_0 < last_0);
          var tmp8_safe_receiver_0 = document.getElementById('sd-balance-clear-selection');
          if (tmp8_safe_receiver_0 == null)
            null;
          else {
            tmp8_safe_receiver_0.addEventListener('click', attachListeners$lambda_23);
          }
          var tmp9_safe_receiver = document.getElementById('sd-balance-credit');
          if (tmp9_safe_receiver == null)
            null;
          else {
            tmp9_safe_receiver.addEventListener('click', attachListeners$lambda_24(u));
          }
          var tmp10_safe_receiver_0 = document.getElementById('sd-balance-debit');
          if (tmp10_safe_receiver_0 == null)
            null;
          else {
            tmp10_safe_receiver_0.addEventListener('click', attachListeners$lambda_25(u));
          }
          var tmp11_safe_receiver_0 = document.getElementById('sd-balance-set');
          if (tmp11_safe_receiver_0 == null)
            null;
          else {
            tmp11_safe_receiver_0.addEventListener('click', attachListeners$lambda_26(u));
          }
          var tmp12_safe_receiver_0 = document.getElementById('sd-settings-save');
          if (tmp12_safe_receiver_0 == null)
            null;
          else {
            tmp12_safe_receiver_0.addEventListener('click', attachListeners$lambda_27(u));
          }
          var tmp13_safe_receiver_0 = document.getElementById('sd-settings-password');
          var tmp_6;
          if (tmp13_safe_receiver_0 == null) {
            tmp_6 = null;
          } else {
            tmp13_safe_receiver_0.addEventListener('click', attachListeners$lambda_28);
            tmp_6 = Unit_getInstance();
          }
        }

        break;
      default:
        noWhenBranchMatchedException();
        break;
    }
  }
  function sendChatMessage(chatInput, uid) {
    _init_properties_Main_kt__xi25uv();
    var tmp1_elvis_lhs = chatInput == null ? null : chatInput.value;
    // Inline function 'kotlin.text.trim' call
    var this_0 = tmp1_elvis_lhs == null ? '' : tmp1_elvis_lhs;
    var text = toString_0(trim(isCharSequence(this_0) ? this_0 : THROW_CCE()));
    if (isBlank(text) || uid == null)
      return Unit_getInstance();
    var tmp2_elvis_lhs = get_appState().get_selectedChatContactId_qi8d29_k$();
    var tmp;
    if (tmp2_elvis_lhs == null) {
      return Unit_getInstance();
    } else {
      tmp = tmp2_elvis_lhs;
    }
    var contactId = tmp;
    var roomId = chatRoomId(uid, contactId);
    var tmp_0 = sendMessage(roomId, uid, text);
    var tmp_1 = tmp_0.then(sendChatMessage$lambda(chatInput));
    tmp_1.catch(sendChatMessage$lambda_0);
  }
  function sam$kotlin_Comparator$0(function_0) {
    this.function_1 = function_0;
  }
  protoOf(sam$kotlin_Comparator$0).compare_bczr_k$ = function (a, b) {
    return this.function_1(a, b);
  };
  protoOf(sam$kotlin_Comparator$0).compare = function (a, b) {
    return this.compare_bczr_k$(a, b);
  };
  protoOf(sam$kotlin_Comparator$0).getFunctionDelegate_jtodtf_k$ = function () {
    return this.function_1;
  };
  protoOf(sam$kotlin_Comparator$0).equals = function (other) {
    var tmp;
    if (!(other == null) ? isInterface(other, Comparator) : false) {
      var tmp_0;
      if (!(other == null) ? isInterface(other, FunctionAdapter) : false) {
        tmp_0 = equals(this.getFunctionDelegate_jtodtf_k$(), other.getFunctionDelegate_jtodtf_k$());
      } else {
        tmp_0 = false;
      }
      tmp = tmp_0;
    } else {
      tmp = false;
    }
    return tmp;
  };
  protoOf(sam$kotlin_Comparator$0).hashCode = function () {
    return hashCode(this.getFunctionDelegate_jtodtf_k$());
  };
  function sam$kotlin_Comparator$0_0(function_0) {
    this.function_1 = function_0;
  }
  protoOf(sam$kotlin_Comparator$0_0).compare_bczr_k$ = function (a, b) {
    return this.function_1(a, b);
  };
  protoOf(sam$kotlin_Comparator$0_0).compare = function (a, b) {
    return this.compare_bczr_k$(a, b);
  };
  protoOf(sam$kotlin_Comparator$0_0).getFunctionDelegate_jtodtf_k$ = function () {
    return this.function_1;
  };
  protoOf(sam$kotlin_Comparator$0_0).equals = function (other) {
    var tmp;
    if (!(other == null) ? isInterface(other, Comparator) : false) {
      var tmp_0;
      if (!(other == null) ? isInterface(other, FunctionAdapter) : false) {
        tmp_0 = equals(this.getFunctionDelegate_jtodtf_k$(), other.getFunctionDelegate_jtodtf_k$());
      } else {
        tmp_0 = false;
      }
      tmp = tmp_0;
    } else {
      tmp = false;
    }
    return tmp;
  };
  protoOf(sam$kotlin_Comparator$0_0).hashCode = function () {
    return hashCode(this.getFunctionDelegate_jtodtf_k$());
  };
  function sam$kotlin_Comparator$0_1(function_0) {
    this.function_1 = function_0;
  }
  protoOf(sam$kotlin_Comparator$0_1).compare_bczr_k$ = function (a, b) {
    return this.function_1(a, b);
  };
  protoOf(sam$kotlin_Comparator$0_1).compare = function (a, b) {
    return this.compare_bczr_k$(a, b);
  };
  protoOf(sam$kotlin_Comparator$0_1).getFunctionDelegate_jtodtf_k$ = function () {
    return this.function_1;
  };
  protoOf(sam$kotlin_Comparator$0_1).equals = function (other) {
    var tmp;
    if (!(other == null) ? isInterface(other, Comparator) : false) {
      var tmp_0;
      if (!(other == null) ? isInterface(other, FunctionAdapter) : false) {
        tmp_0 = equals(this.getFunctionDelegate_jtodtf_k$(), other.getFunctionDelegate_jtodtf_k$());
      } else {
        tmp_0 = false;
      }
      tmp = tmp_0;
    } else {
      tmp = false;
    }
    return tmp;
  };
  protoOf(sam$kotlin_Comparator$0_1).hashCode = function () {
    return hashCode(this.getFunctionDelegate_jtodtf_k$());
  };
  function sam$kotlin_Comparator$0_2(function_0) {
    this.function_1 = function_0;
  }
  protoOf(sam$kotlin_Comparator$0_2).compare_bczr_k$ = function (a, b) {
    return this.function_1(a, b);
  };
  protoOf(sam$kotlin_Comparator$0_2).compare = function (a, b) {
    return this.compare_bczr_k$(a, b);
  };
  protoOf(sam$kotlin_Comparator$0_2).getFunctionDelegate_jtodtf_k$ = function () {
    return this.function_1;
  };
  protoOf(sam$kotlin_Comparator$0_2).equals = function (other) {
    var tmp;
    if (!(other == null) ? isInterface(other, Comparator) : false) {
      var tmp_0;
      if (!(other == null) ? isInterface(other, FunctionAdapter) : false) {
        tmp_0 = equals(this.getFunctionDelegate_jtodtf_k$(), other.getFunctionDelegate_jtodtf_k$());
      } else {
        tmp_0 = false;
      }
      tmp = tmp_0;
    } else {
      tmp = false;
    }
    return tmp;
  };
  protoOf(sam$kotlin_Comparator$0_2).hashCode = function () {
    return hashCode(this.getFunctionDelegate_jtodtf_k$());
  };
  function renderChatTabContent$contactRow(c) {
    // Inline function 'kotlin.run' call
    // Inline function 'kotlin.text.ifEmpty' call
    var this_0 = c.initials_4g6y0f_k$();
    var tmp;
    // Inline function 'kotlin.text.isEmpty' call
    if (charSequenceLength(this_0) === 0) {
      tmp = '?';
    } else {
      tmp = this_0;
    }
    var tmp$ret$2 = tmp;
    var initials = escapeHtml(tmp$ret$2);
    var avatarBg = escapeHtml(avatarColorForId(c.get_id_kntnx8_k$()));
    var statusClass = get_appState().get_chatContactOnlineIds_e5rmjq_k$().contains_aljjnj_k$(c.get_id_kntnx8_k$()) ? 'sd-chat-contact-status sd-chat-contact-status-online' : 'sd-chat-contact-status sd-chat-contact-status-offline';
    var statusText = get_appState().get_chatContactOnlineIds_e5rmjq_k$().contains_aljjnj_k$(c.get_id_kntnx8_k$()) ? ' (\u0432 \u0441\u0435\u0442\u0438)' : ' (\u043D\u0435 \u0432 \u0441\u0435\u0442\u0438)';
    return '<button type="button" class="sd-chat-contact" data-contact-id="' + escapeHtml(c.get_id_kntnx8_k$()) + '"><span class="sd-chat-contact-avatar" style="background:' + avatarBg + '">' + initials + '<\/span><span class="sd-chat-contact-info">' + escapeHtml(c.get_fullName_9skygt_k$()) + ' \xB7 ' + c.get_role_wotsxr_k$() + '<span class="' + statusClass + '">' + statusText + '<\/span><\/span><\/button>';
  }
  function renderBalanceTabContent$ticketWord(n) {
    var a = n % 100 | 0;
    if (11 <= a ? a <= 14 : false)
      return '\u0442\u0430\u043B\u043E\u043D\u043E\u0432';
    switch (n % 10 | 0) {
      case 1:
        return '\u0442\u0430\u043B\u043E\u043D';
      case 2:
      case 3:
      case 4:
        return '\u0442\u0430\u043B\u043E\u043D\u0430';
      default:
        return '\u0442\u0430\u043B\u043E\u043D\u043E\u0432';
    }
  }
  function attachListeners$_anonymous_$doBalanceOp_vagh69($usr, type) {
    var tmp0_elvis_lhs = get_appState().get_balanceAdminSelectedUserId_4lrwub_k$();
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return Unit_getInstance();
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var targetId = tmp;
    var tmp_0 = document.getElementById('sd-balance-amount');
    var input = tmp_0 instanceof HTMLInputElement ? tmp_0 : null;
    var tmp2_elvis_lhs = input == null ? null : input.value;
    var tmp3_elvis_lhs = toIntOrNull(tmp2_elvis_lhs == null ? '0' : tmp2_elvis_lhs);
    var amount = tmp3_elvis_lhs == null ? 0 : tmp3_elvis_lhs;
    if (amount < 0)
      return Unit_getInstance();
    var tmp_1 = $usr.get_id_kntnx8_k$();
    updateBalance(targetId, type, amount, tmp_1, attachListeners$_anonymous_$doBalanceOp$lambda_6obd22);
  }
  function subscribeChatPresence$lambda($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_chatContactOnlineIds_1muzq_k$(emptySet());
    return Unit_getInstance();
  }
  function subscribeChatPresence$lambda$lambda($online, $id) {
    return function ($this$updateState) {
      var newSet = toMutableSet($this$updateState.get_chatContactOnlineIds_e5rmjq_k$());
      if ($online)
        newSet.add_utx5q5_k$($id);
      else
        newSet.remove_cedx0m_k$($id);
      $this$updateState.set_chatContactOnlineIds_1muzq_k$(newSet);
      return Unit_getInstance();
    };
  }
  function subscribeChatPresence$lambda_0($id) {
    return function (online) {
      updateState(subscribeChatPresence$lambda$lambda(online, $id));
      return Unit_getInstance();
    };
  }
  function main$lambda(_unused_var__etf5q3) {
    _init_properties_Main_kt__xi25uv();
    var tmp0_elvis_lhs = document.getElementById('root');
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return Unit_getInstance();
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var root = tmp;
    initFirebase();
    var lastRenderedTabIndex = {_v: null};
    var renderScheduled = {_v: false};
    set_onStateChanged(main$lambda$lambda(renderScheduled, root, lastRenderedTabIndex));
    setupPanelClickDelegation(root);
    onAuthStateChanged(main$lambda$lambda_0);
    invoke$render(root, lastRenderedTabIndex);
    return Unit_getInstance();
  }
  function invoke$render(root, lastRenderedTabIndex) {
    var state = get_appState();
    var tmp0_safe_receiver = state.get_networkError_3tn6pv_k$();
    var tmp;
    if (tmp0_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.let' call
      tmp = '<div class="sd-network-error" id="sd-network-error"><span>' + tmp0_safe_receiver + '<\/span> <button type="button" id="sd-dismiss-network-error" class="sd-btn-inline">\u0417\u0430\u043A\u0440\u044B\u0442\u044C<\/button><\/div>';
    }
    var tmp1_elvis_lhs = tmp;
    var networkBanner = tmp1_elvis_lhs == null ? '' : tmp1_elvis_lhs;
    var loadingOverlay = state.get_loading_6tzj9v_k$() ? '<div class="sd-loading-overlay" id="sd-loading-overlay"><div class="sd-spinner"><\/div><p>\u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430\u2026<\/p><\/div>' : '';
    var panelScreen = state.get_screen_jed7jp_k$().equals(AppScreen_Admin_getInstance()) || state.get_screen_jed7jp_k$().equals(AppScreen_Instructor_getInstance()) || state.get_screen_jed7jp_k$().equals(AppScreen_Cadet_getInstance());
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    var tmp_0 = root.querySelector('#sd-card');
    var sdCard = tmp_0 instanceof Element ? tmp_0 : null;
    if (panelScreen && !(state.get_user_wovspg_k$() == null) && !(sdCard == null) && state.get_networkError_3tn6pv_k$() == null && !state.get_loading_6tzj9v_k$()) {
      var tabs;
      switch (state.get_screen_jed7jp_k$().get_ordinal_ip24qg_k$()) {
        case 4:
          tabs = listOf(['\u0413\u043B\u0430\u0432\u043D\u0430\u044F', '\u0411\u0430\u043B\u0430\u043D\u0441', '\u0427\u0430\u0442', '\u0418\u0441\u0442\u043E\u0440\u0438\u044F']);
          break;
        case 5:
          tabs = listOf(['\u0413\u043B\u0430\u0432\u043D\u0430\u044F', '\u0417\u0430\u043F\u0438\u0441\u044C', '\u0427\u0430\u0442', '\u0411\u0438\u043B\u0435\u0442\u044B', '\u0418\u0441\u0442\u043E\u0440\u0438\u044F', '\u041D\u0430\u0441\u0442\u0440\u043E\u0439\u043A\u0438']);
          break;
        default:
          tabs = listOf(['\u0413\u043B\u0430\u0432\u043D\u0430\u044F', '\u0417\u0430\u043F\u0438\u0441\u044C', '\u0427\u0430\u0442', '\u0411\u0438\u043B\u0435\u0442\u044B', '\u0418\u0441\u0442\u043E\u0440\u0438\u044F', '\u041D\u0430\u0441\u0442\u0440\u043E\u0439\u043A\u0438']);
          break;
      }
      if (state.get_screen_jed7jp_k$().equals(AppScreen_Admin_getInstance()) && state.get_selectedTabIndex_3g78ox_k$() === 1) {
        var historyDetails = sdCard.querySelector('details[data-balance-section="history"]');
        updateState(main$lambda$render$lambda(historyDetails));
      }
      var _destruct__k2r9zo = getPanelTabButtonsAndContent(ensureNotNull(state.get_user_wovspg_k$()), tabs);
      var tabButtons = _destruct__k2r9zo.component1_7eebsc_k$();
      var tabContent = _destruct__k2r9zo.component2_7eebsb_k$();
      if (!(lastRenderedTabIndex._v === state.get_selectedTabIndex_3g78ox_k$())) {
        // Inline function 'kotlin.js.unsafeCast' call
        // Inline function 'kotlin.js.asDynamic' call
        var tmp_1 = root.querySelector('nav.sd-tabs');
        var tmp3_safe_receiver = tmp_1 instanceof Element ? tmp_1 : null;
        if (tmp3_safe_receiver == null)
          null;
        else {
          tmp3_safe_receiver.innerHTML = tabButtons;
        }
        lastRenderedTabIndex._v = state.get_selectedTabIndex_3g78ox_k$();
      }
      sdCard.innerHTML = tabContent;
      attachListeners(root);
      return Unit_getInstance();
    }
    lastRenderedTabIndex._v = null;
    var tmp_2;
    switch (state.get_screen_jed7jp_k$().get_ordinal_ip24qg_k$()) {
      case 0:
        tmp_2 = renderLogin(state.get_error_iqzvfj_k$(), state.get_loading_6tzj9v_k$());
        break;
      case 1:
        tmp_2 = renderRegister(state.get_error_iqzvfj_k$(), state.get_loading_6tzj9v_k$());
        break;
      case 2:
        tmp_2 = renderPendingApproval();
        break;
      case 3:
        var tmp5_elvis_lhs = state.get_error_iqzvfj_k$();
        tmp_2 = renderProfileNotFound(tmp5_elvis_lhs == null ? '\u041F\u0440\u043E\u0444\u0438\u043B\u044C \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D.' : tmp5_elvis_lhs);
        break;
      case 4:
        tmp_2 = renderPanel(ensureNotNull(state.get_user_wovspg_k$()), '\u0410\u0434\u043C\u0438\u043D\u0438\u0441\u0442\u0440\u0430\u0442\u043E\u0440', listOf(['\u0413\u043B\u0430\u0432\u043D\u0430\u044F', '\u0411\u0430\u043B\u0430\u043D\u0441', '\u0427\u0430\u0442', '\u0418\u0441\u0442\u043E\u0440\u0438\u044F']));
        break;
      case 5:
        tmp_2 = renderPanel(ensureNotNull(state.get_user_wovspg_k$()), '\u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440', listOf(['\u0413\u043B\u0430\u0432\u043D\u0430\u044F', '\u0417\u0430\u043F\u0438\u0441\u044C', '\u0427\u0430\u0442', '\u0411\u0438\u043B\u0435\u0442\u044B', '\u0418\u0441\u0442\u043E\u0440\u0438\u044F', '\u041D\u0430\u0441\u0442\u0440\u043E\u0439\u043A\u0438']));
        break;
      case 6:
        tmp_2 = renderPanel(ensureNotNull(state.get_user_wovspg_k$()), '\u041A\u0443\u0440\u0441\u0430\u043D\u0442', listOf(['\u0413\u043B\u0430\u0432\u043D\u0430\u044F', '\u0417\u0430\u043F\u0438\u0441\u044C', '\u0427\u0430\u0442', '\u0411\u0438\u043B\u0435\u0442\u044B', '\u0418\u0441\u0442\u043E\u0440\u0438\u044F', '\u041D\u0430\u0441\u0442\u0440\u043E\u0439\u043A\u0438']));
        break;
      default:
        noWhenBranchMatchedException();
        break;
    }
    var html = tmp_2;
    root.innerHTML = networkBanner + loadingOverlay + html;
    attachListeners(root);
  }
  function invoke$scheduleRender(renderScheduled, root, lastRenderedTabIndex) {
    if (renderScheduled._v)
      return Unit_getInstance();
    renderScheduled._v = true;
    var tmp = window;
    tmp.requestAnimationFrame(main$lambda$scheduleRender$lambda(renderScheduled, root, lastRenderedTabIndex));
  }
  function main$lambda$lambda($renderScheduled, $root, $lastRenderedTabIndex) {
    return function () {
      invoke$scheduleRender($renderScheduled, $root, $lastRenderedTabIndex);
      return Unit_getInstance();
    };
  }
  function main$lambda$lambda_0(uid) {
    _init_properties_Main_kt__xi25uv();
    if (uid == null) {
      updateState(main$lambda$lambda$lambda);
      return Unit_getInstance();
    }
    updateState(main$lambda$lambda$lambda_0);
    getCurrentUser(main$lambda$lambda$lambda_1);
    return Unit_getInstance();
  }
  function main$lambda$lambda$lambda($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_screen_91y0x9_k$(AppScreen_Login_getInstance());
    $this$updateState.set_user_gqmzbm_k$(null);
    $this$updateState.set_error_5ld8to_k$(null);
    return Unit_getInstance();
  }
  function main$lambda$lambda$lambda_0($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_loading_7od76y_k$(true);
    $this$updateState.set_error_5ld8to_k$(null);
    return Unit_getInstance();
  }
  function main$lambda$lambda$lambda_1(user, errorMsg) {
    _init_properties_Main_kt__xi25uv();
    updateState(main$lambda$lambda$lambda$lambda);
    if (user == null) {
      updateState(main$lambda$lambda$lambda$lambda_0(errorMsg));
      return Unit_getInstance();
    }
    updateState(main$lambda$lambda$lambda$lambda_1(user));
    updateState(main$lambda$lambda$lambda$lambda_2(user));
    setPresence(user.get_id_kntnx8_k$(), true);
    return Unit_getInstance();
  }
  function main$lambda$lambda$lambda$lambda($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_loading_7od76y_k$(false);
    $this$updateState.set_networkError_6qqylc_k$(null);
    return Unit_getInstance();
  }
  function main$lambda$lambda$lambda$lambda_0($errorMsg) {
    return function ($this$updateState) {
      $this$updateState.set_screen_91y0x9_k$(AppScreen_ProfileNotFound_getInstance());
      $this$updateState.set_user_gqmzbm_k$(null);
      var tmp0_elvis_lhs = $errorMsg;
      $this$updateState.set_error_5ld8to_k$(tmp0_elvis_lhs == null ? '\u041F\u0440\u043E\u0444\u0438\u043B\u044C \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D \u0432 \u0431\u0430\u0437\u0435.' : tmp0_elvis_lhs);
      return Unit_getInstance();
    };
  }
  function main$lambda$lambda$lambda$lambda_1($user) {
    return function ($this$updateState) {
      $this$updateState.set_user_gqmzbm_k$($user);
      $this$updateState.set_error_5ld8to_k$(null);
      $this$updateState.set_networkError_6qqylc_k$(null);
      return Unit_getInstance();
    };
  }
  function main$lambda$lambda$lambda$lambda_2($user) {
    return function ($this$updateState) {
      var tmp0_subject = $user.get_role_wotsxr_k$();
      $this$updateState.set_screen_91y0x9_k$(tmp0_subject === 'admin' ? AppScreen_Admin_getInstance() : tmp0_subject === 'instructor' ? $user.get_isActive_quafmh_k$() ? AppScreen_Instructor_getInstance() : AppScreen_PendingApproval_getInstance() : tmp0_subject === 'cadet' ? $user.get_isActive_quafmh_k$() ? AppScreen_Cadet_getInstance() : AppScreen_PendingApproval_getInstance() : AppScreen_PendingApproval_getInstance());
      return Unit_getInstance();
    };
  }
  function main$lambda$render$lambda($historyDetails) {
    return function ($this$updateState) {
      var tmp0_safe_receiver = $historyDetails;
      var tmp;
      if (tmp0_safe_receiver == null) {
        tmp = null;
      } else {
        // Inline function 'kotlin.js.unsafeCast' call
        // Inline function 'kotlin.js.asDynamic' call
        tmp = tmp0_safe_receiver;
      }
      var tmp1_safe_receiver = tmp;
      $this$updateState.set_balanceHistorySectionOpen_9pgeu3_k$((tmp1_safe_receiver == null ? null : tmp1_safe_receiver.open) == true);
      return Unit_getInstance();
    };
  }
  function main$lambda$scheduleRender$lambda($renderScheduled, $root, $lastRenderedTabIndex) {
    return function (it) {
      invoke$render($root, $lastRenderedTabIndex);
      $renderScheduled._v = false;
      return Unit_getInstance();
    };
  }
  function renderChatTabContent$lambda($myId, $iconCheck) {
    return function (msg) {
      var isMe = msg.get_senderId_b5xjpj_k$() === $myId;
      var cls = isMe ? 'sd-msg sd-msg-me' : 'sd-msg sd-msg-them';
      var timeStr = escapeHtml(formatMessageDateTime(msg.get_timestamp_9fccx9_k$()));
      var tmp;
      if (isMe) {
        var isRead = msg.get_status_jnf6d7_k$() === 'read';
        var checks = isRead ? $iconCheck + $iconCheck : $iconCheck;
        var checkClass = isRead ? 'sd-msg-checks sd-msg-checks-read' : 'sd-msg-checks sd-msg-checks-sent';
        tmp = '<span class="' + checkClass + '" title="' + (isRead ? '\u041F\u0440\u043E\u0447\u0438\u0442\u0430\u043D\u043E' : '\u0414\u043E\u0441\u0442\u0430\u0432\u043B\u0435\u043D\u043E') + '">' + checks + '<\/span>';
      } else {
        tmp = '';
      }
      var statusHtml = tmp;
      var timeRow = '<span class="sd-msg-time">' + timeStr + '<\/span>';
      return '<div class="' + cls + '"><span class="sd-msg-text">' + escapeHtml(msg.get_text_wouvsm_k$()) + '<\/span><div class="sd-msg-footer">' + timeRow + statusHtml + '<\/div><\/div>';
    };
  }
  function renderChatTabContent$lambda_0(a, b) {
    _init_properties_Main_kt__xi25uv();
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.get_fullName_9skygt_k$();
    var tmp$ret$1 = b.get_fullName_9skygt_k$();
    return compareValues(tmp, tmp$ret$1);
  }
  function renderChatTabContent$lambda_1(a, b) {
    _init_properties_Main_kt__xi25uv();
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.get_fullName_9skygt_k$();
    var tmp$ret$1 = b.get_fullName_9skygt_k$();
    return compareValues(tmp, tmp$ret$1);
  }
  function renderChatTabContent$lambda_2(a, b) {
    _init_properties_Main_kt__xi25uv();
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.get_fullName_9skygt_k$();
    var tmp$ret$1 = b.get_fullName_9skygt_k$();
    return compareValues(tmp, tmp$ret$1);
  }
  function renderChatTabContent$lambda_3(it) {
    _init_properties_Main_kt__xi25uv();
    return renderChatTabContent$contactRow(it);
  }
  function renderChatTabContent$lambda_4(it) {
    _init_properties_Main_kt__xi25uv();
    return renderChatTabContent$contactRow(it);
  }
  function renderChatTabContent$lambda_5(it) {
    _init_properties_Main_kt__xi25uv();
    return renderChatTabContent$contactRow(it);
  }
  function renderAdminHomeContent$lambda(a, b) {
    _init_properties_Main_kt__xi25uv();
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.get_fullName_9skygt_k$();
    var tmp$ret$1 = b.get_fullName_9skygt_k$();
    return compareValues(tmp, tmp$ret$1);
  }
  function renderAdminHomeContent$lambda_0(a, b) {
    _init_properties_Main_kt__xi25uv();
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.get_fullName_9skygt_k$();
    var tmp$ret$1 = b.get_fullName_9skygt_k$();
    return compareValues(tmp, tmp$ret$1);
  }
  function renderAdminHomeContent$lambda_1(a, b) {
    _init_properties_Main_kt__xi25uv();
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.get_fullName_9skygt_k$();
    var tmp$ret$1 = b.get_fullName_9skygt_k$();
    return compareValues(tmp, tmp$ret$1);
  }
  function renderAdminHomeContent$lambda_2(u) {
    _init_properties_Main_kt__xi25uv();
    var roleLabel = u.get_role_wotsxr_k$() === 'instructor' ? '\u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440' : '\u041A\u0443\u0440\u0441\u0430\u043D\u0442';
    // Inline function 'kotlin.text.ifBlank' call
    var this_0 = u.get_fullName_9skygt_k$();
    var tmp;
    if (isBlank(this_0)) {
      tmp = '\u0418\u043C\u044F \u043D\u0435 \u0443\u043A\u0430\u0437\u0430\u043D\u043E';
    } else {
      tmp = this_0;
    }
    var tmp$ret$1 = tmp;
    return '<div class="sd-admin-card sd-admin-card-pending">\n            <div class="sd-admin-card-info">\n                <p class="sd-admin-card-name">' + escapeHtml(tmp$ret$1) + '<\/p>\n                <p class="sd-admin-card-meta">' + escapeHtml(u.get_email_iqwbqr_k$()) + '<\/p>\n                <p class="sd-admin-card-meta">' + escapeHtml(u.get_phone_iwv5tx_k$()) + '<\/p>\n                <p class="sd-admin-card-meta"><span class="sd-admin-role-label">\u0420\u043E\u043B\u044C \u043F\u0440\u0438 \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u0438:<\/span> ' + roleLabel + '<\/p>\n            <\/div>\n            <div class="sd-admin-card-actions">\n                <button type="button" class="sd-btn sd-btn-small sd-btn-primary" data-admin-activate="' + escapeHtml(u.get_id_kntnx8_k$()) + '" title="\u0410\u043A\u0442\u0438\u0432\u0438\u0440\u043E\u0432\u0430\u0442\u044C \u2014 \u043F\u0435\u0440\u0435\u0432\u0435\u0434\u0451\u0442 \u0432 \u0440\u0430\u0437\u0434\u0435\u043B \xAB' + roleLabel + '\xBB">\u0410\u043A\u0442\u0438\u0432\u0438\u0440\u043E\u0432\u0430\u0442\u044C<\/button>\n                <button type="button" class="sd-btn sd-btn-small sd-btn-delete" data-admin-delete="' + escapeHtml(u.get_id_kntnx8_k$()) + '" title="\u0423\u0434\u0430\u043B\u0438\u0442\u044C">\u0423\u0434\u0430\u043B\u0438\u0442\u044C<\/button>\n            <\/div>\n        <\/div>';
  }
  function renderAdminHomeContent$lambda_3(a, b) {
    _init_properties_Main_kt__xi25uv();
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.get_fullName_9skygt_k$();
    var tmp$ret$1 = b.get_fullName_9skygt_k$();
    return compareValues(tmp, tmp$ret$1);
  }
  function renderAdminHomeContent$lambda_4(c) {
    _init_properties_Main_kt__xi25uv();
    return '<div class="sd-assign-section-row sd-assign-section-row-current"><span class="sd-assign-section-name">' + escapeHtml(formatShortName(c.get_fullName_9skygt_k$())) + '<\/span><\/div>';
  }
  function renderAdminHomeContent$lambda_5(a, b) {
    _init_properties_Main_kt__xi25uv();
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.get_fullName_9skygt_k$();
    var tmp$ret$1 = b.get_fullName_9skygt_k$();
    return compareValues(tmp, tmp$ret$1);
  }
  function renderAdminHomeContent$lambda_6(a, b) {
    _init_properties_Main_kt__xi25uv();
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.get_fullName_9skygt_k$();
    var tmp$ret$1 = b.get_fullName_9skygt_k$();
    return compareValues(tmp, tmp$ret$1);
  }
  function renderAdminHomeContent$lambda_7($assignInstructorId) {
    return function (c) {
      return '<div class="sd-assign-section-row"><span class="sd-assign-section-name">' + escapeHtml(formatShortName(c.get_fullName_9skygt_k$())) + '<\/span><button type="button" class="sd-btn sd-btn-small sd-btn-primary" data-admin-assign-instructor="' + escapeHtml($assignInstructorId) + '" data-admin-assign-cadet="' + escapeHtml(c.get_id_kntnx8_k$()) + '">\u041D\u0430\u0437\u043D\u0430\u0447\u0438\u0442\u044C<\/button><\/div>';
    };
  }
  function renderAdminHomeContent$lambda$lambda(a, b) {
    _init_properties_Main_kt__xi25uv();
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.get_fullName_9skygt_k$();
    var tmp$ret$1 = b.get_fullName_9skygt_k$();
    return compareValues(tmp, tmp$ret$1);
  }
  function renderAdminHomeContent$lambda$lambda_0($assignInstructorId) {
    return function (c) {
      return '<div class="sd-assign-section-row"><span class="sd-assign-section-name">' + escapeHtml(formatShortName(c.get_fullName_9skygt_k$())) + '<\/span><button type="button" class="sd-btn sd-btn-small sd-btn-primary" data-admin-assign-instructor="' + escapeHtml($assignInstructorId) + '" data-admin-assign-cadet="' + escapeHtml(c.get_id_kntnx8_k$()) + '">\u041D\u0430\u0437\u043D\u0430\u0447\u0438\u0442\u044C<\/button><\/div>';
    };
  }
  function renderAdminHomeContent$lambda_8($cadets, $assignInstructorId) {
    return function (other) {
      // Inline function 'kotlin.collections.filter' call
      var tmp0 = $cadets;
      // Inline function 'kotlin.collections.filterTo' call
      var destination = ArrayList_init_$Create$();
      var _iterator__ex2g4s = tmp0.iterator_jk1svi_k$();
      while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
        var element = _iterator__ex2g4s.next_20eer_k$();
        if (element.get_assignedInstructorId_laxw6p_k$() === other.get_id_kntnx8_k$()) {
          destination.add_utx5q5_k$(element);
        }
      }
      // Inline function 'kotlin.collections.sortedBy' call
      // Inline function 'kotlin.comparisons.compareBy' call
      var tmp = renderAdminHomeContent$lambda$lambda;
      var tmp$ret$3 = new sam$kotlin_Comparator$0_0(tmp);
      var otherCadets = sortedWith(destination, tmp$ret$3);
      var rows = joinToString(otherCadets, '', VOID, VOID, VOID, VOID, renderAdminHomeContent$lambda$lambda_0($assignInstructorId));
      var otherShort = escapeHtml(formatShortName(other.get_fullName_9skygt_k$()));
      return '<div class="sd-assign-section"><h4 class="sd-assign-section-title">\u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440 ' + otherShort + ' (' + otherCadets.get_size_woubt6_k$() + ')<\/h4><div class="sd-assign-section-list">' + (otherCadets.isEmpty_y1axqb_k$() ? '<p class="sd-muted">\u041D\u0435\u0442 \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D\u043D\u044B\u0445 \u043A\u0443\u0440\u0441\u0430\u043D\u0442\u043E\u0432.<\/p>' : rows) + '<\/div><\/div>';
    };
  }
  function renderAdminHomeContent$lambda_9($assignCadetId) {
    return function (inst) {
      return '<div class="sd-assign-section-row"><span class="sd-assign-section-name">' + escapeHtml(formatShortName(inst.get_fullName_9skygt_k$())) + '<\/span><button type="button" class="sd-btn sd-btn-small sd-btn-primary" data-admin-assign-instructor="' + escapeHtml(inst.get_id_kntnx8_k$()) + '" data-admin-assign-cadet="' + escapeHtml($assignCadetId) + '">\u041D\u0430\u0437\u043D\u0430\u0447\u0438\u0442\u044C<\/button><\/div>';
    };
  }
  function renderAdminHomeContent$lambda$lambda_1(a, b) {
    _init_properties_Main_kt__xi25uv();
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.get_fullName_9skygt_k$();
    var tmp$ret$1 = b.get_fullName_9skygt_k$();
    return compareValues(tmp, tmp$ret$1);
  }
  function renderAdminHomeContent$lambda_10($cadets) {
    return function (u) {
      // Inline function 'kotlin.collections.filter' call
      var tmp0 = $cadets;
      // Inline function 'kotlin.collections.filterTo' call
      var destination = ArrayList_init_$Create$();
      var _iterator__ex2g4s = tmp0.iterator_jk1svi_k$();
      while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
        var element = _iterator__ex2g4s.next_20eer_k$();
        if (element.get_assignedInstructorId_laxw6p_k$() === u.get_id_kntnx8_k$()) {
          destination.add_utx5q5_k$(element);
        }
      }
      // Inline function 'kotlin.collections.sortedBy' call
      // Inline function 'kotlin.comparisons.compareBy' call
      var tmp = renderAdminHomeContent$lambda$lambda_1;
      var tmp$ret$3 = new sam$kotlin_Comparator$0_0(tmp);
      var assignedCadets = sortedWith(destination, tmp$ret$3);
      var cadetsRow = '<div class="sd-admin-card-row-label sd-admin-card-row-cadets"><span class="sd-admin-card-label-icon">' + get_iconInstructorSvg() + '<\/span>\u041A\u0443\u0440\u0441\u0430\u043D\u0442\u044B: ' + assignedCadets.get_size_woubt6_k$() + '<button type="button" class="sd-btn-inline sd-instructor-cadets-toggle" data-instructor-cadets-modal="' + escapeHtml(u.get_id_kntnx8_k$()) + '">\u041F\u043E\u0441\u043C\u043E\u0442\u0440\u0435\u0442\u044C<\/button><\/div>';
      var tmp_0;
      // Inline function 'kotlin.text.isNotBlank' call
      var this_0 = u.get_phone_iwv5tx_k$();
      if (!isBlank(this_0)) {
        tmp_0 = 'tel:' + escapeHtml(u.get_phone_iwv5tx_k$());
      } else {
        tmp_0 = '#';
      }
      var phoneHrefInst = tmp_0;
      var tmp_1;
      // Inline function 'kotlin.text.isNotBlank' call
      var this_1 = u.get_phone_iwv5tx_k$();
      if (!isBlank(this_1)) {
        tmp_1 = 'sd-btn sd-btn-icon sd-btn-icon-right';
      } else {
        tmp_1 = 'sd-btn sd-btn-icon sd-btn-icon-right sd-btn-disabled';
      }
      var phoneClassInst = tmp_1;
      var tmp_2 = get_iconUserSvg();
      var tmp_3 = escapeHtml(u.get_fullName_9skygt_k$());
      var tmp_4 = get_iconPhoneSvg();
      var tmp_5 = escapeHtml(u.get_id_kntnx8_k$());
      var tmp_6 = get_iconChatSvg();
      var tmp_7 = get_iconPhoneLabelSvg();
      // Inline function 'kotlin.text.ifBlank' call
      var this_2 = u.get_phone_iwv5tx_k$();
      var tmp_8;
      if (isBlank(this_2)) {
        tmp_8 = '\u2014';
      } else {
        tmp_8 = this_2;
      }
      var tmp$ret$8 = tmp_8;
      var tmp_9 = escapeHtml(tmp$ret$8);
      var tmp_10 = get_iconEmailLabelSvg();
      // Inline function 'kotlin.text.ifBlank' call
      var this_3 = u.get_email_iqwbqr_k$();
      var tmp_11;
      if (isBlank(this_3)) {
        tmp_11 = '\u2014';
      } else {
        tmp_11 = this_3;
      }
      var tmp$ret$10 = tmp_11;
      return '<div class="sd-admin-card sd-admin-card-instructor">\n            <div class="sd-admin-card-body">\n                <div class="sd-admin-card-row-main">\n                    <p class="sd-admin-card-fio"><span class="sd-admin-card-label-icon">' + tmp_2 + '<\/span>' + tmp_3 + '<\/p>\n                    <div class="sd-admin-card-icons">\n                        <a href="' + phoneHrefInst + '" class="' + phoneClassInst + '" title="\u041F\u043E\u0437\u0432\u043E\u043D\u0438\u0442\u044C">' + tmp_4 + '<\/a>\n                        <button type="button" class="sd-btn sd-btn-icon sd-btn-icon-right sd-admin-open-chat" data-contact-id="' + tmp_5 + '" title="\u0427\u0430\u0442">' + tmp_6 + '<\/button>\n                    <\/div>\n                <\/div>\n                <p class="sd-admin-card-row-label"><span class="sd-admin-card-label-icon">' + tmp_7 + '<\/span>\u0422\u0435\u043B.: ' + tmp_9 + '<\/p>\n                <p class="sd-admin-card-row-label"><span class="sd-admin-card-label-icon">' + tmp_10 + '<\/span>Email: ' + escapeHtml(tmp$ret$10) + '<\/p>\n                <p class="sd-admin-card-row-label"><span class="sd-admin-card-label-icon">' + get_iconTicketSvg() + '<\/span>\u0411\u0430\u043B\u0430\u043D\u0441 \u0442\u0430\u043B\u043E\u043D\u043E\u0432: ' + u.get_balance_4cdzil_k$() + '<\/p>\n                ' + cadetsRow + '\n            <\/div>\n            <div class="sd-admin-card-footer">\n                <button type="button" class="sd-btn sd-btn-small" data-admin-assign="' + escapeHtml(u.get_id_kntnx8_k$()) + '">' + get_iconUserPlusSvg() + ' \u041D\u0430\u0437\u043D\u0430\u0447\u0438\u0442\u044C \u043A\u0443\u0440\u0441\u0430\u043D\u0442\u0430<\/button>\n                <button type="button" class="sd-btn sd-btn-small" data-admin-activate="' + escapeHtml(u.get_id_kntnx8_k$()) + '" data-admin-active="' + u.get_isActive_quafmh_k$() + '">' + get_iconPowerSvg() + ' ' + (u.get_isActive_quafmh_k$() ? '\u0414\u0435\u0430\u043A\u0442\u0438\u0432\u0438\u0440\u043E\u0432\u0430\u0442\u044C' : '\u0410\u043A\u0442\u0438\u0432\u0438\u0440\u043E\u0432\u0430\u0442\u044C') + '<\/button>\n                <button type="button" class="sd-btn sd-btn-small sd-btn-delete" data-admin-delete="' + escapeHtml(u.get_id_kntnx8_k$()) + '">' + get_iconTrashSvg() + ' \u0423\u0434\u0430\u043B\u0438\u0442\u044C<\/button>\n            <\/div>\n        <\/div>';
    };
  }
  function renderAdminHomeContent$lambda_11($instructors) {
    return function (u) {
      var instId = u.get_assignedInstructorId_laxw6p_k$();
      var tmp;
      if (instId == null) {
        tmp = null;
      } else {
        // Inline function 'kotlin.let' call
        // Inline function 'kotlin.collections.find' call
        var tmp0 = $instructors;
        var tmp$ret$1;
        $l$block: {
          // Inline function 'kotlin.collections.firstOrNull' call
          var _iterator__ex2g4s = tmp0.iterator_jk1svi_k$();
          while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
            var element = _iterator__ex2g4s.next_20eer_k$();
            if (element.get_id_kntnx8_k$() === instId) {
              tmp$ret$1 = element;
              break $l$block;
            }
          }
          tmp$ret$1 = null;
        }
        var tmp0_safe_receiver = tmp$ret$1;
        var tmp1_elvis_lhs = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.get_fullName_9skygt_k$();
        tmp = tmp1_elvis_lhs == null ? '\u2014' : tmp1_elvis_lhs;
      }
      var tmp1_elvis_lhs_0 = tmp;
      var instName = tmp1_elvis_lhs_0 == null ? '\u2014' : tmp1_elvis_lhs_0;
      var displayInstText = !(instId == null) ? instName : '\u041D\u0435 \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D';
      var tmp_0;
      // Inline function 'kotlin.text.isNotBlank' call
      var this_0 = u.get_phone_iwv5tx_k$();
      if (!isBlank(this_0)) {
        tmp_0 = 'tel:' + escapeHtml(u.get_phone_iwv5tx_k$());
      } else {
        tmp_0 = '#';
      }
      var phoneHrefCadet = tmp_0;
      var tmp_1;
      // Inline function 'kotlin.text.isNotBlank' call
      var this_1 = u.get_phone_iwv5tx_k$();
      if (!isBlank(this_1)) {
        tmp_1 = 'sd-btn sd-btn-icon sd-btn-icon-right';
      } else {
        tmp_1 = 'sd-btn sd-btn-icon sd-btn-icon-right sd-btn-disabled';
      }
      var phoneClassCadet = tmp_1;
      var unlinkOrAssign = !(instId == null) ? '<button type="button" class="sd-btn sd-btn-small sd-admin-unlink-right" data-admin-unlink-instructor="' + escapeHtml(instId) + '" data-admin-unlink-cadet="' + escapeHtml(u.get_id_kntnx8_k$()) + '">' + get_iconUnlinkSvg() + ' \u041E\u0442\u0432\u044F\u0437\u0430\u0442\u044C<\/button>' : '<button type="button" class="sd-btn sd-btn-small sd-btn-primary sd-admin-assign-cadet-btn" data-admin-assign-cadet="' + escapeHtml(u.get_id_kntnx8_k$()) + '">\u041D\u0430\u0437\u043D\u0430\u0447\u0438\u0442\u044C<\/button>';
      var instructorRow = '<div class="sd-admin-card-row-label sd-admin-card-row-instructor"><span class="sd-admin-card-label-icon">' + get_iconInstructorSvg() + '<\/span>\u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440: ' + displayInstText + unlinkOrAssign + '<\/div>';
      var footerButtons = '<button type="button" class="sd-btn sd-btn-small" data-admin-activate="' + escapeHtml(u.get_id_kntnx8_k$()) + '" data-admin-active="' + u.get_isActive_quafmh_k$() + '">' + get_iconPowerSvg() + ' ' + (u.get_isActive_quafmh_k$() ? '\u0414\u0435\u0430\u043A\u0442\u0438\u0432\u0438\u0440\u043E\u0432\u0430\u0442\u044C' : '\u0410\u043A\u0442\u0438\u0432\u0438\u0440\u043E\u0432\u0430\u0442\u044C') + '<\/button><button type="button" class="sd-btn sd-btn-small sd-btn-delete" data-admin-delete="' + escapeHtml(u.get_id_kntnx8_k$()) + '">' + get_iconTrashSvg() + ' \u0423\u0434\u0430\u043B\u0438\u0442\u044C<\/button>';
      var tmp_2 = get_iconUserSvg();
      var tmp_3 = escapeHtml(u.get_fullName_9skygt_k$());
      var tmp_4 = get_iconPhoneSvg();
      var tmp_5 = escapeHtml(u.get_id_kntnx8_k$());
      var tmp_6 = get_iconChatSvg();
      var tmp_7 = get_iconPhoneLabelSvg();
      // Inline function 'kotlin.text.ifBlank' call
      var this_2 = u.get_phone_iwv5tx_k$();
      var tmp_8;
      if (isBlank(this_2)) {
        tmp_8 = '\u2014';
      } else {
        tmp_8 = this_2;
      }
      var tmp$ret$8 = tmp_8;
      var tmp_9 = escapeHtml(tmp$ret$8);
      var tmp_10 = get_iconEmailLabelSvg();
      // Inline function 'kotlin.text.ifBlank' call
      var this_3 = u.get_email_iqwbqr_k$();
      var tmp_11;
      if (isBlank(this_3)) {
        tmp_11 = '\u2014';
      } else {
        tmp_11 = this_3;
      }
      var tmp$ret$10 = tmp_11;
      return '<div class="sd-admin-card sd-admin-card-cadet">\n            <div class="sd-admin-card-body">\n                <div class="sd-admin-card-row-main">\n                    <p class="sd-admin-card-fio"><span class="sd-admin-card-label-icon">' + tmp_2 + '<\/span>' + tmp_3 + '<\/p>\n                    <div class="sd-admin-card-icons">\n                        <a href="' + phoneHrefCadet + '" class="' + phoneClassCadet + '" title="\u041F\u043E\u0437\u0432\u043E\u043D\u0438\u0442\u044C">' + tmp_4 + '<\/a>\n                        <button type="button" class="sd-btn sd-btn-icon sd-btn-icon-right sd-admin-open-chat" data-contact-id="' + tmp_5 + '" title="\u0427\u0430\u0442">' + tmp_6 + '<\/button>\n                    <\/div>\n                <\/div>\n                <p class="sd-admin-card-row-label"><span class="sd-admin-card-label-icon">' + tmp_7 + '<\/span>\u0422\u0435\u043B.: ' + tmp_9 + '<\/p>\n                <p class="sd-admin-card-row-label"><span class="sd-admin-card-label-icon">' + tmp_10 + '<\/span>Email: ' + escapeHtml(tmp$ret$10) + '<\/p>\n                ' + instructorRow + '\n            <\/div>\n            <div class="sd-admin-card-footer">' + footerButtons + '<\/div>\n        <\/div>';
    };
  }
  function renderAdminHomeContent$lambda_12(a, b) {
    _init_properties_Main_kt__xi25uv();
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.get_fullName_9skygt_k$();
    var tmp$ret$1 = b.get_fullName_9skygt_k$();
    return compareValues(tmp, tmp$ret$1);
  }
  function renderAdminHomeContent$lambda_13(c) {
    _init_properties_Main_kt__xi25uv();
    return '<li class="sd-instructor-cadet-name">' + escapeHtml(formatShortName(c.get_fullName_9skygt_k$())) + '<\/li>';
  }
  function renderInstructorHomeContent$lambda(a, b) {
    _init_properties_Main_kt__xi25uv();
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.get_fullName_9skygt_k$();
    var tmp$ret$1 = b.get_fullName_9skygt_k$();
    return compareValues(tmp, tmp$ret$1);
  }
  function renderInstructorHomeContent$lambda_0($allSessions) {
    return function (c) {
      var tmp0 = $allSessions;
      var tmp$ret$0;
      $l$block: {
        // Inline function 'kotlin.collections.count' call
        var tmp;
        if (isInterface(tmp0, Collection)) {
          tmp = tmp0.isEmpty_y1axqb_k$();
        } else {
          tmp = false;
        }
        if (tmp) {
          tmp$ret$0 = 0;
          break $l$block;
        }
        var count = 0;
        var _iterator__ex2g4s = tmp0.iterator_jk1svi_k$();
        while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
          var element = _iterator__ex2g4s.next_20eer_k$();
          if (element.get_cadetId_a7olqf_k$() === c.get_id_kntnx8_k$() && element.get_status_jnf6d7_k$() === 'completed') {
            count = count + 1 | 0;
            checkCountOverflow(count);
          }
        }
        tmp$ret$0 = count;
      }
      var completedCount = tmp$ret$0;
      // Inline function 'kotlin.collections.mapNotNull' call
      var tmp0_0 = take(split(c.get_fullName_9skygt_k$(), [' ']), 2);
      // Inline function 'kotlin.collections.mapNotNullTo' call
      var destination = ArrayList_init_$Create$();
      // Inline function 'kotlin.collections.forEach' call
      var _iterator__ex2g4s_0 = tmp0_0.iterator_jk1svi_k$();
      while (_iterator__ex2g4s_0.hasNext_bitz1p_k$()) {
        var element_0 = _iterator__ex2g4s_0.next_20eer_k$();
        var tmp0_safe_receiver = firstOrNull(element_0);
        var tmp_0;
        var tmp_1 = tmp0_safe_receiver;
        if ((tmp_1 == null ? null : new Char(tmp_1)) == null) {
          tmp_0 = null;
        } else {
          // Inline function 'kotlin.text.uppercase' call
          // Inline function 'kotlin.js.asDynamic' call
          // Inline function 'kotlin.js.unsafeCast' call
          tmp_0 = toString_1(tmp0_safe_receiver).toUpperCase();
        }
        var tmp0_safe_receiver_0 = tmp_0;
        if (tmp0_safe_receiver_0 == null)
          null;
        else {
          // Inline function 'kotlin.let' call
          destination.add_utx5q5_k$(tmp0_safe_receiver_0);
        }
      }
      var initials = joinToString(destination, '');
      // Inline function 'kotlin.text.ifBlank' call
      var this_0 = c.get_phone_iwv5tx_k$();
      var tmp_2;
      if (isBlank(this_0)) {
        tmp_2 = '\u2014';
      } else {
        tmp_2 = this_0;
      }
      var tmp$ret$13 = tmp_2;
      var phoneDisplay = escapeHtml(tmp$ret$13);
      var tmp_3;
      // Inline function 'kotlin.text.isNotBlank' call
      var this_1 = c.get_phone_iwv5tx_k$();
      if (!isBlank(this_1)) {
        tmp_3 = 'tel:' + escapeHtml(c.get_phone_iwv5tx_k$());
      } else {
        tmp_3 = '#';
      }
      var phoneHref = tmp_3;
      var tmp_4;
      // Inline function 'kotlin.text.isNotBlank' call
      var this_2 = c.get_phone_iwv5tx_k$();
      if (!isBlank(this_2)) {
        tmp_4 = 'sd-btn sd-btn-circle sd-btn-phone';
      } else {
        tmp_4 = 'sd-btn sd-btn-circle sd-btn-phone sd-btn-disabled';
      }
      var phoneClass = tmp_4;
      return '<div class="sd-cadet-card">\n            <p class="sd-cadet-card-title">\u041A\u0430\u0440\u0442\u043E\u0447\u043A\u0430 \u043A\u0443\u0440\u0441\u0430\u043D\u0442\u0430:<\/p>\n            <div class="sd-cadet-card-body">\n                <div class="sd-cadet-avatar">' + initials + '<\/div>\n                <div class="sd-cadet-info">\n                    <p class="sd-cadet-name">' + escapeHtml(c.get_fullName_9skygt_k$()) + '<\/p>\n                    <p class="sd-cadet-row"><span class="sd-cadet-label">\u0422\u0435\u043B\u0435\u0444\u043E\u043D:<\/span> ' + phoneDisplay + '<\/p>\n                    <p class="sd-cadet-row"><span class="sd-cadet-label">\u0412\u043E\u0436\u0434\u0435\u043D\u0438\u0439:<\/span> ' + completedCount + '<\/p>\n                    <p class="sd-cadet-row"><span class="sd-cadet-label">\u0411\u0430\u043B\u0430\u043D\u0441:<\/span> ' + c.get_balance_4cdzil_k$() + ' \u0442\u0430\u043B\u043E\u043D\u043E\u0432<\/p>\n                <\/div>\n            <\/div>\n            <div class="sd-cadet-card-actions">\n                <button type="button" class="sd-btn sd-btn-circle sd-btn-chat sd-cadet-chat-btn" data-contact-id="' + escapeHtml(c.get_id_kntnx8_k$()) + '" title="\u0427\u0430\u0442">\u0427\u0430\u0442<\/button>\n                <a href="' + phoneHref + '" class="' + phoneClass + '" title="\u041F\u043E\u0437\u0432\u043E\u043D\u0438\u0442\u044C">\u0422\u0435\u043B\u0435\u0444\u043E\u043D<\/a>\n            <\/div>\n        <\/div>';
    };
  }
  function renderInstructorHomeContent$lambda_1(s) {
    _init_properties_Main_kt__xi25uv();
    return '<div class="sd-record-row"><span>' + formatDateTime(s.get_startTimeMillis_a8mb9o_k$()) + '<\/span> \u2014 ' + s.get_status_jnf6d7_k$() + '<\/div>';
  }
  function renderCadetHomeContent$lambda(it) {
    _init_properties_Main_kt__xi25uv();
    return '<div class="sd-record-row">' + formatDateTime(it.get_startTimeMillis_a8mb9o_k$()) + ' \u2014 \u0437\u0430\u043F\u043B\u0430\u043D\u0438\u0440\u043E\u0432\u0430\u043D\u043E<\/div>';
  }
  function renderRecordingTabContent$lambda(w) {
    _init_properties_Main_kt__xi25uv();
    var dt = formatDateTime(w.get_dateTimeMillis_6iuuye_k$());
    var status = w.get_status_jnf6d7_k$() === 'booked' ? ' (\u0437\u0430\u0431\u0440\u043E\u043D\u0438\u0440\u043E\u0432\u0430\u043D\u043E)' : '';
    return '<div class="sd-record-row"><span>' + dt + '<\/span> ' + status + ' <button type="button" class="sd-btn sd-btn-small sd-btn-delete" data-window-id="' + escapeHtml(w.get_id_kntnx8_k$()) + '">\u0423\u0434\u0430\u043B\u0438\u0442\u044C<\/button><\/div>';
  }
  function renderRecordingTabContent$lambda_0(s) {
    _init_properties_Main_kt__xi25uv();
    return '<div class="sd-record-row"><span>' + formatDateTime(s.get_startTimeMillis_a8mb9o_k$()) + '<\/span> \u2014 ' + s.get_status_jnf6d7_k$() + '<\/div>';
  }
  function renderRecordingTabContent$lambda_1(w) {
    _init_properties_Main_kt__xi25uv();
    return '<div class="sd-record-row"><span>' + formatDateTime(w.get_dateTimeMillis_6iuuye_k$()) + '<\/span> <button type="button" class="sd-btn sd-btn-primary sd-btn-small" data-window-id="' + escapeHtml(w.get_id_kntnx8_k$()) + '">\u0417\u0430\u043F\u0438\u0441\u0430\u0442\u044C\u0441\u044F<\/button><\/div>';
  }
  function renderRecordingTabContent$lambda_2(it) {
    _init_properties_Main_kt__xi25uv();
    return '<div class="sd-record-row">' + formatDateTime(it.get_startTimeMillis_a8mb9o_k$()) + ' \u2014 ' + it.get_status_jnf6d7_k$() + '<\/div>';
  }
  function renderHistoryTabContent$lambda(s) {
    _init_properties_Main_kt__xi25uv();
    return '<div class="sd-record-row"><span>' + formatDateTime(s.get_startTimeMillis_a8mb9o_k$()) + '<\/span> ' + s.get_status_jnf6d7_k$() + ' ' + (s.get_instructorRating_lprecj_k$() > 0 ? '\u2605' + s.get_instructorRating_lprecj_k$() : '') + '<\/div>';
  }
  function renderHistoryTabContent$lambda_0($users) {
    return function (b) {
      // Inline function 'kotlin.collections.find' call
      var tmp0 = $users;
      var tmp$ret$1;
      $l$block: {
        // Inline function 'kotlin.collections.firstOrNull' call
        var _iterator__ex2g4s = tmp0.iterator_jk1svi_k$();
        while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
          var element = _iterator__ex2g4s.next_20eer_k$();
          if (element.get_id_kntnx8_k$() === b.get_userId_kl13yn_k$()) {
            tmp$ret$1 = element;
            break $l$block;
          }
        }
        tmp$ret$1 = null;
      }
      var tmp0_safe_receiver = tmp$ret$1;
      var tmp1_elvis_lhs = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.get_fullName_9skygt_k$();
      var name = tmp1_elvis_lhs == null ? take_0(b.get_userId_kl13yn_k$(), 8) + '\u2026' : tmp1_elvis_lhs;
      var typeStr;
      switch (b.get_type_wovaf7_k$()) {
        case 'credit':
          typeStr = '+';
          break;
        case 'debit':
          typeStr = '\u2212';
          break;
        case 'set':
          typeStr = '=';
          break;
        default:
          typeStr = '';
          break;
      }
      return '<div class="sd-record-row"><span>' + formatDateTime(b.get_timestampMillis_cbfaxf_k$()) + '<\/span> ' + name + ' \u2014 ' + typeStr + b.get_amount_b10di9_k$() + '<\/div>';
    };
  }
  function renderHistoryTabContent$lambda_1(b) {
    _init_properties_Main_kt__xi25uv();
    var typeStr;
    switch (b.get_type_wovaf7_k$()) {
      case 'credit':
        typeStr = '+';
        break;
      case 'debit':
        typeStr = '\u2212';
        break;
      case 'set':
        typeStr = '=';
        break;
      default:
        typeStr = '';
        break;
    }
    return '<div class="sd-record-row"><span>' + formatDateTime(b.get_timestampMillis_cbfaxf_k$()) + '<\/span> ' + typeStr + b.get_amount_b10di9_k$() + '<\/div>';
  }
  function renderBalanceTabContent$lambda(u) {
    _init_properties_Main_kt__xi25uv();
    return '<div class="sd-balance-card">\n            <div class="sd-balance-card-body">\n                <p class="sd-balance-card-row"><span class="sd-balance-card-label">' + get_iconUserSvg() + ' \u0424\u0418\u041E:<\/span> ' + escapeHtml(u.get_fullName_9skygt_k$()) + '<\/p>\n                <p class="sd-balance-card-row"><span class="sd-balance-card-label">' + get_iconTicketSvg() + ' \u0411\u0430\u043B\u0430\u043D\u0441 \u0442\u0430\u043B\u043E\u043D\u043E\u0432:<\/span> ' + u.get_balance_4cdzil_k$() + '<\/p>\n            <\/div>\n            <div class="sd-balance-card-action">\n                <button type="button" class="sd-btn sd-btn-select" data-balance-select="' + escapeHtml(u.get_id_kntnx8_k$()) + '">' + get_iconSelectSvg() + ' \u0412\u044B\u0431\u0440\u0430\u0442\u044C<\/button>\n            <\/div>\n        <\/div>';
  }
  function renderBalanceTabContent$lambda_0($balanceCardHtml) {
    return function (it) {
      return $balanceCardHtml(it);
    };
  }
  function renderBalanceTabContent$lambda_1($balanceCardHtml) {
    return function (it) {
      return $balanceCardHtml(it);
    };
  }
  function renderBalanceTabContent$lambda_2(t) {
    _init_properties_Main_kt__xi25uv();
    switch (t) {
      case 'credit':
        return '\u0437\u0430\u0447\u0438\u0441\u043B\u0435\u043D\u043E';
      case 'debit':
        return '\u0441\u043F\u0438\u0441\u0430\u043D\u043E';
      case 'set':
        return '\u0443\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D\u043E';
      default:
        return t;
    }
  }
  function renderBalanceTabContent$lambda_3(a, b) {
    _init_properties_Main_kt__xi25uv();
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp0_elvis_lhs = b.get_timestampMillis_cbfaxf_k$();
    var tmp = tmp0_elvis_lhs == null ? new Long(0, 0) : tmp0_elvis_lhs;
    var tmp0_elvis_lhs_0 = a.get_timestampMillis_cbfaxf_k$();
    var tmp$ret$1 = tmp0_elvis_lhs_0 == null ? new Long(0, 0) : tmp0_elvis_lhs_0;
    return compareValues(tmp, tmp$ret$1);
  }
  function renderBalanceTabContent$lambda$lambda($users, $typeLabel) {
    return function (b) {
      // Inline function 'kotlin.collections.find' call
      var tmp0 = $users;
      var tmp$ret$1;
      $l$block: {
        // Inline function 'kotlin.collections.firstOrNull' call
        var _iterator__ex2g4s = tmp0.iterator_jk1svi_k$();
        while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
          var element = _iterator__ex2g4s.next_20eer_k$();
          if (element.get_id_kntnx8_k$() === b.get_userId_kl13yn_k$()) {
            tmp$ret$1 = element;
            break $l$block;
          }
        }
        tmp$ret$1 = null;
      }
      var tmp0_safe_receiver = tmp$ret$1;
      var tmp1_elvis_lhs = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.get_fullName_9skygt_k$();
      var userName = tmp1_elvis_lhs == null ? take_0(b.get_userId_kl13yn_k$(), 8) + '\u2026' : tmp1_elvis_lhs;
      var label = $typeLabel(b.get_type_wovaf7_k$());
      var tail = '' + b.get_amount_b10di9_k$() + ' ' + renderBalanceTabContent$ticketWord(b.get_amount_b10di9_k$());
      // Inline function 'kotlin.text.ifEmpty' call
      var this_0 = substringAfter(formatDateTimeEkaterinburg(b.get_timestampMillis_cbfaxf_k$()), ', ');
      var tmp;
      // Inline function 'kotlin.text.isEmpty' call
      if (charSequenceLength(this_0) === 0) {
        tmp = '\u2014';
      } else {
        tmp = this_0;
      }
      return '<div class="sd-record-row"><span class="sd-balance-history-time">' + tmp + '<\/span> \u2014 <strong>' + escapeHtml(userName) + '<\/strong>: ' + label + ' ' + tail + '<\/div>';
    };
  }
  function renderBalanceTabContent$lambda_4($users, $typeLabel) {
    return function (_destruct__k2r9zo) {
      // Inline function 'kotlin.collections.component1' call
      var dateStr = _destruct__k2r9zo.get_key_18j28a_k$();
      // Inline function 'kotlin.collections.component2' call
      var entries = _destruct__k2r9zo.get_value_j01efc_k$();
      var rows = joinToString(entries, '', VOID, VOID, VOID, VOID, renderBalanceTabContent$lambda$lambda($users, $typeLabel));
      return '<div class="sd-balance-history-day"><p class="sd-balance-history-day-title">' + dateStr + '<\/p><div class="sd-balance-history-day-list">' + rows + '<\/div><\/div>';
    };
  }
  function setupPanelClickDelegation$lambda(e) {
    _init_properties_Main_kt__xi25uv();
    if (!get_appState().get_screen_jed7jp_k$().equals(AppScreen_Admin_getInstance()) && !get_appState().get_screen_jed7jp_k$().equals(AppScreen_Instructor_getInstance()) && !get_appState().get_screen_jed7jp_k$().equals(AppScreen_Cadet_getInstance()))
      return Unit_getInstance();
    var tmp = e == null ? null : e.target;
    var tmp1_elvis_lhs = tmp instanceof Element ? tmp : null;
    var tmp_0;
    if (tmp1_elvis_lhs == null) {
      return Unit_getInstance();
    } else {
      tmp_0 = tmp1_elvis_lhs;
    }
    var target = tmp_0;
    // Inline function 'kotlin.js.unsafeCast' call
    var closestHelper = function (el, sel) {
      return el && el.closest ? el.closest(sel) : null;
    };
    var closest = setupPanelClickDelegation$lambda$lambda(closestHelper, target);
    var summaryInMainSection = closest('details[data-admin-section] summary');
    var tmp_1;
    if (!(summaryInMainSection == null)) {
      var tmp3_safe_receiver = get_appState().get_user_wovspg_k$();
      tmp_1 = (tmp3_safe_receiver == null ? null : tmp3_safe_receiver.get_role_wotsxr_k$()) === 'admin';
    } else {
      tmp_1 = false;
    }
    if (tmp_1) {
      var tmp2_elvis_lhs = summaryInMainSection.parentElement;
      var tmp_2;
      if (tmp2_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_2 = tmp2_elvis_lhs;
      }
      var detailsEl = tmp_2;
      var tmp_3 = window;
      tmp_3.setTimeout(setupPanelClickDelegation$lambda$lambda_0, 0);
      return Unit_getInstance();
    }
    var cadetsToggleBtn = closest('.sd-instructor-cadets-toggle');
    if (!(cadetsToggleBtn == null)) {
      var tmp4_elvis_lhs = cadetsToggleBtn.getAttribute('data-instructor-cadets-modal');
      var tmp_4;
      if (tmp4_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_4 = tmp4_elvis_lhs;
      }
      var instId = tmp_4;
      updateState(setupPanelClickDelegation$lambda$lambda_1(instId));
      e.preventDefault();
      var tmp5_safe_receiver = e instanceof Event ? e : null;
      if (tmp5_safe_receiver == null)
        null;
      else {
        tmp5_safe_receiver.stopPropagation();
      }
      return Unit_getInstance();
    }
    var modalCloseBtn = closest('#sd-admin-cadets-modal-close');
    if (!(modalCloseBtn == null)) {
      updateState(setupPanelClickDelegation$lambda$lambda_2);
      e.preventDefault();
      var tmp6_safe_receiver = e instanceof Event ? e : null;
      if (tmp6_safe_receiver == null)
        null;
      else {
        tmp6_safe_receiver.stopPropagation();
      }
      return Unit_getInstance();
    }
    var modalOverlay = target.id === 'sd-admin-cadets-modal-overlay' ? target : null;
    if (!(modalOverlay == null)) {
      updateState(setupPanelClickDelegation$lambda$lambda_3);
      e.preventDefault();
      var tmp7_safe_receiver = e instanceof Event ? e : null;
      if (tmp7_safe_receiver == null)
        null;
      else {
        tmp7_safe_receiver.stopPropagation();
      }
      return Unit_getInstance();
    }
    var tabBtn = closest('.sd-tab');
    if (!(tabBtn == null)) {
      var tmp8_safe_receiver = tabBtn.getAttribute('data-tab');
      var tmp9_elvis_lhs = tmp8_safe_receiver == null ? null : toIntOrNull(tmp8_safe_receiver);
      var tmp_5;
      if (tmp9_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_5 = tmp9_elvis_lhs;
      }
      var idx = tmp_5;
      var newbiesOpen = get_appState().get_adminNewbiesSectionOpen_l2t6yo_k$();
      var instOpen = get_appState().get_adminInstructorsSectionOpen_frw1fj_k$();
      var cadetsOpen = get_appState().get_adminCadetsSectionOpen_e2mf91_k$();
      var tmp_6;
      var tmp_7;
      var tmp16_safe_receiver = get_appState().get_user_wovspg_k$();
      if ((tmp16_safe_receiver == null ? null : tmp16_safe_receiver.get_role_wotsxr_k$()) === 'admin') {
        tmp_7 = get_appState().get_selectedTabIndex_3g78ox_k$() === 0;
      } else {
        tmp_7 = false;
      }
      if (tmp_7) {
        tmp_6 = !(idx === 0);
      } else {
        tmp_6 = false;
      }
      if (tmp_6) {
        var tmp_8 = document.getElementById('sd-card');
        var cardEl = tmp_8 instanceof Element ? tmp_8 : null;
        if (!(cardEl == null)) {
          var newbiesDetails = cardEl.querySelector('details[data-admin-section="newbies"]');
          var instDetails = cardEl.querySelector('details[data-admin-section="instructors"]');
          var cadetDetails = cardEl.querySelector('details[data-admin-section="cadets"]');
          var tmp_9;
          if (newbiesDetails == null) {
            tmp_9 = null;
          } else {
            // Inline function 'kotlin.js.unsafeCast' call
            // Inline function 'kotlin.js.asDynamic' call
            tmp_9 = newbiesDetails;
          }
          var tmp11_safe_receiver = tmp_9;
          newbiesOpen = (tmp11_safe_receiver == null ? null : tmp11_safe_receiver.open) == true;
          var tmp_10;
          if (instDetails == null) {
            tmp_10 = null;
          } else {
            // Inline function 'kotlin.js.unsafeCast' call
            // Inline function 'kotlin.js.asDynamic' call
            tmp_10 = instDetails;
          }
          var tmp13_safe_receiver = tmp_10;
          instOpen = (tmp13_safe_receiver == null ? null : tmp13_safe_receiver.open) == true;
          var tmp_11;
          if (cadetDetails == null) {
            tmp_11 = null;
          } else {
            // Inline function 'kotlin.js.unsafeCast' call
            // Inline function 'kotlin.js.asDynamic' call
            tmp_11 = cadetDetails;
          }
          var tmp15_safe_receiver = tmp_11;
          cadetsOpen = (tmp15_safe_receiver == null ? null : tmp15_safe_receiver.open) == true;
        }
      }
      if (!(idx === 2)) {
        updateState(setupPanelClickDelegation$lambda$lambda_4);
        unsubscribeChat();
      }
      var nOpen = newbiesOpen;
      var iOpen = instOpen;
      var cOpen = cadetsOpen;
      updateState(setupPanelClickDelegation$lambda$lambda_5(idx, nOpen, iOpen, cOpen));
      var tmp17_elvis_lhs = get_appState().get_user_wovspg_k$();
      var tmp_12;
      if (tmp17_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_12 = tmp17_elvis_lhs;
      }
      var user = tmp_12;
      switch (idx) {
        case 0:
          if (user.get_role_wotsxr_k$() === 'admin' && !get_appState().get_adminHomeLoading_t0xi21_k$()) {
            updateState(setupPanelClickDelegation$lambda$lambda_6);
            var tmp_13 = window;
            var tid = tmp_13.setTimeout(setupPanelClickDelegation$lambda$lambda_7, 8000);
            getUsersWithError(setupPanelClickDelegation$lambda$lambda_8(tid));
          }

          break;
        case 1:
          if (user.get_role_wotsxr_k$() === 'admin' && !get_appState().get_balanceAdminLoading_slms8w_k$()) {
            updateState(setupPanelClickDelegation$lambda$lambda_9);
            var tmp_14 = window;
            var tid_0 = tmp_14.setTimeout(setupPanelClickDelegation$lambda$lambda_10, 8000);
            getUsers(setupPanelClickDelegation$lambda$lambda_11(tid_0));
          }

          break;
        case 2:
          if (get_appState().get_chatContacts_b21mhg_k$().isEmpty_y1axqb_k$() && !get_appState().get_chatContactsLoading_r2se4o_k$()) {
            updateState(setupPanelClickDelegation$lambda$lambda_12);
            var tmp_15 = window;
            var chatTid = tmp_15.setTimeout(setupPanelClickDelegation$lambda$lambda_13, 5000);
            getUsersForChat(user, setupPanelClickDelegation$lambda$lambda_14(chatTid));
          }

          break;
        default:
          break;
      }
      if ((idx === 0 || idx === 1) && (user.get_role_wotsxr_k$() === 'instructor' || user.get_role_wotsxr_k$() === 'cadet') && !get_appState().get_recordingLoading_h2yjak_k$()) {
        updateState(setupPanelClickDelegation$lambda$lambda_15);
        var tmp_16 = window;
        var tid_1 = tmp_16.setTimeout(setupPanelClickDelegation$lambda$lambda_16, 8000);
        if (user.get_role_wotsxr_k$() === 'instructor') {
          var tmp_17 = user.get_id_kntnx8_k$();
          getOpenWindowsForInstructor(tmp_17, setupPanelClickDelegation$lambda$lambda_17(user, tid_1));
        } else {
          var tmp19_elvis_lhs = user.get_assignedInstructorId_laxw6p_k$();
          var instId_0 = tmp19_elvis_lhs == null ? '' : tmp19_elvis_lhs;
          getOpenWindowsForCadet(instId_0, setupPanelClickDelegation$lambda$lambda_18(user, tid_1));
        }
      }
      e.preventDefault();
      e.stopPropagation();
      return Unit_getInstance();
    }
    var btnActivate = closest('[data-admin-activate]');
    if (!(btnActivate == null)) {
      var tmp20_elvis_lhs = btnActivate.getAttribute('data-admin-activate');
      var tmp_18;
      if (tmp20_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_18 = tmp20_elvis_lhs;
      }
      var userId = tmp_18;
      var currentlyActive = btnActivate.getAttribute('data-admin-active') === 'true';
      var tmp_19 = !currentlyActive;
      setActive(userId, tmp_19, setupPanelClickDelegation$lambda$lambda_19);
      e.preventDefault();
      e.stopPropagation();
      return Unit_getInstance();
    }
    var btnAssign = closest('[data-admin-assign]');
    if (!(btnAssign == null)) {
      var tmp21_elvis_lhs = btnAssign.getAttribute('data-admin-assign');
      var tmp_20;
      if (tmp21_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_20 = tmp21_elvis_lhs;
      }
      var id = tmp_20;
      updateState(setupPanelClickDelegation$lambda$lambda_20(id));
      var tmp_21 = window;
      tmp_21.setTimeout(setupPanelClickDelegation$lambda$lambda_21, 150);
      e.preventDefault();
      var tmp22_safe_receiver = e instanceof Event ? e : null;
      if (tmp22_safe_receiver == null)
        null;
      else {
        tmp22_safe_receiver.stopPropagation();
      }
      return Unit_getInstance();
    }
    var btnAssignCadetFromCard = closest('.sd-admin-assign-cadet-btn');
    if (!(btnAssignCadetFromCard == null)) {
      var tmp23_elvis_lhs = btnAssignCadetFromCard.getAttribute('data-admin-assign-cadet');
      var tmp_22;
      if (tmp23_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_22 = tmp23_elvis_lhs;
      }
      var cadetId = tmp_22;
      updateState(setupPanelClickDelegation$lambda$lambda_22(cadetId));
      var tmp_23 = window;
      tmp_23.setTimeout(setupPanelClickDelegation$lambda$lambda_23, 150);
      e.preventDefault();
      var tmp24_safe_receiver = e instanceof Event ? e : null;
      if (tmp24_safe_receiver == null)
        null;
      else {
        tmp24_safe_receiver.stopPropagation();
      }
      return Unit_getInstance();
    }
    var btnAssignCadet = closest('[data-admin-assign-instructor]');
    if (!(btnAssignCadet == null)) {
      var tmp25_elvis_lhs = btnAssignCadet.getAttribute('data-admin-assign-instructor');
      var tmp_24;
      if (tmp25_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_24 = tmp25_elvis_lhs;
      }
      var instId_1 = tmp_24;
      var tmp26_elvis_lhs = btnAssignCadet.getAttribute('data-admin-assign-cadet');
      var tmp_25;
      if (tmp26_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_25 = tmp26_elvis_lhs;
      }
      var cadetId_0 = tmp_25;
      assignCadetToInstructor(instId_1, cadetId_0, setupPanelClickDelegation$lambda$lambda_24);
      e.preventDefault();
      var tmp27_safe_receiver = e instanceof Event ? e : null;
      if (tmp27_safe_receiver == null)
        null;
      else {
        tmp27_safe_receiver.stopPropagation();
      }
      return Unit_getInstance();
    }
    var btnUnlink = closest('[data-admin-unlink-instructor]');
    if (!(btnUnlink == null)) {
      var tmp28_elvis_lhs = btnUnlink.getAttribute('data-admin-unlink-instructor');
      var tmp_26;
      if (tmp28_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_26 = tmp28_elvis_lhs;
      }
      var instId_2 = tmp_26;
      var tmp29_elvis_lhs = btnUnlink.getAttribute('data-admin-unlink-cadet');
      var tmp_27;
      if (tmp29_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_27 = tmp29_elvis_lhs;
      }
      var cadetId_1 = tmp_27;
      removeCadetFromInstructor(instId_2, cadetId_1, setupPanelClickDelegation$lambda$lambda_25);
      e.preventDefault();
      e.stopPropagation();
      return Unit_getInstance();
    }
    var btnDelete = closest('[data-admin-delete]');
    if (!(btnDelete == null)) {
      var tmp30_elvis_lhs = btnDelete.getAttribute('data-admin-delete');
      var tmp_28;
      if (tmp30_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_28 = tmp30_elvis_lhs;
      }
      var userId_0 = tmp_28;
      if (!window.confirm('\u0423\u0434\u0430\u043B\u0438\u0442\u044C \u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u0435\u043B\u044F \u0438\u0437 \u0431\u0430\u0437\u044B? \u042D\u0442\u043E \u043D\u0435 \u0443\u0434\u0430\u043B\u0438\u0442 \u0430\u043A\u043A\u0430\u0443\u043D\u0442 Firebase Auth.'))
        return Unit_getInstance();
      deleteUser(userId_0, setupPanelClickDelegation$lambda$lambda_26);
      e.preventDefault();
      e.stopPropagation();
      return Unit_getInstance();
    }
    var btnBalanceSelect = closest('[data-balance-select]');
    if (!(btnBalanceSelect == null)) {
      var tmp31_elvis_lhs = btnBalanceSelect.getAttribute('data-balance-select');
      var tmp_29;
      if (tmp31_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_29 = tmp31_elvis_lhs;
      }
      var id_0 = tmp_29;
      updateState(setupPanelClickDelegation$lambda$lambda_27(id_0));
      var tmp_30 = window;
      tmp_30.setTimeout(setupPanelClickDelegation$lambda$lambda_28, 150);
      e.preventDefault();
      e.stopPropagation();
      return Unit_getInstance();
    }
    var chatContact = closest('.sd-chat-contact');
    if (!(chatContact == null)) {
      var tmp32_elvis_lhs = chatContact.getAttribute('data-contact-id');
      var tmp_31;
      if (tmp32_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_31 = tmp32_elvis_lhs;
      }
      var contactId = tmp_31;
      var tmp33_safe_receiver = get_appState().get_user_wovspg_k$();
      var tmp34_elvis_lhs = tmp33_safe_receiver == null ? null : tmp33_safe_receiver.get_id_kntnx8_k$();
      var tmp_32;
      if (tmp34_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_32 = tmp34_elvis_lhs;
      }
      var uid = tmp_32;
      updateState(setupPanelClickDelegation$lambda$lambda_29(contactId));
      unsubscribeChat();
      var tmp_33 = chatRoomId(uid, contactId);
      subscribeMessages(tmp_33, setupPanelClickDelegation$lambda$lambda_30);
      e.preventDefault();
      e.stopPropagation();
      return Unit_getInstance();
    }
    var adminOpenChat = closest('.sd-admin-open-chat');
    if (!(adminOpenChat == null)) {
      var tmp35_elvis_lhs = adminOpenChat.getAttribute('data-contact-id');
      var tmp_34;
      if (tmp35_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_34 = tmp35_elvis_lhs;
      }
      var contactId_0 = tmp_34;
      var tmp36_safe_receiver = get_appState().get_user_wovspg_k$();
      var tmp37_elvis_lhs = tmp36_safe_receiver == null ? null : tmp36_safe_receiver.get_id_kntnx8_k$();
      var tmp_35;
      if (tmp37_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_35 = tmp37_elvis_lhs;
      }
      var uid_0 = tmp_35;
      updateState(setupPanelClickDelegation$lambda$lambda_31(contactId_0));
      unsubscribeChat();
      var tmp_36 = chatRoomId(uid_0, contactId_0);
      subscribeMessages(tmp_36, setupPanelClickDelegation$lambda$lambda_32);
      e.preventDefault();
      e.stopPropagation();
      return Unit_getInstance();
    }
    var cadetChatBtn = closest('.sd-cadet-chat-btn');
    if (!(cadetChatBtn == null)) {
      var tmp38_elvis_lhs = cadetChatBtn.getAttribute('data-contact-id');
      var tmp_37;
      if (tmp38_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_37 = tmp38_elvis_lhs;
      }
      var contactId_1 = tmp_37;
      var tmp39_safe_receiver = get_appState().get_user_wovspg_k$();
      var tmp40_elvis_lhs = tmp39_safe_receiver == null ? null : tmp39_safe_receiver.get_id_kntnx8_k$();
      var tmp_38;
      if (tmp40_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_38 = tmp40_elvis_lhs;
      }
      var uid_1 = tmp_38;
      updateState(setupPanelClickDelegation$lambda$lambda_33(contactId_1));
      unsubscribeChat();
      var tmp_39 = chatRoomId(uid_1, contactId_1);
      subscribeMessages(tmp_39, setupPanelClickDelegation$lambda$lambda_34);
      e.preventDefault();
      e.stopPropagation();
    }
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda($closestHelper, $target) {
    return function (s) {
      var tmp;
      try {
        var tmp_0 = $closestHelper($target, s);
        tmp = tmp_0 instanceof Element ? tmp_0 : null;
      } catch ($p) {
        var tmp_1;
        if ($p instanceof Error) {
          var _unused_var__etf5q3 = $p;
          tmp_1 = null;
        } else {
          throw $p;
        }
        tmp = tmp_1;
      }
      return tmp;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_0() {
    _init_properties_Main_kt__xi25uv();
    var tmp = document.getElementById('sd-card');
    var cardEl = tmp instanceof Element ? tmp : null;
    if (!(cardEl == null)) {
      var newbiesDetails = cardEl.querySelector('details[data-admin-section="newbies"]');
      var instDetails = cardEl.querySelector('details[data-admin-section="instructors"]');
      var cadetDetails = cardEl.querySelector('details[data-admin-section="cadets"]');
      updateState(setupPanelClickDelegation$lambda$lambda$lambda(newbiesDetails, instDetails, cadetDetails));
    }
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda($newbiesDetails, $instDetails, $cadetDetails) {
    return function ($this$updateState) {
      var tmp0_safe_receiver = $newbiesDetails;
      var tmp;
      if (tmp0_safe_receiver == null) {
        tmp = null;
      } else {
        // Inline function 'kotlin.js.unsafeCast' call
        // Inline function 'kotlin.js.asDynamic' call
        tmp = tmp0_safe_receiver;
      }
      var tmp1_safe_receiver = tmp;
      $this$updateState.set_adminNewbiesSectionOpen_h0xy2x_k$((tmp1_safe_receiver == null ? null : tmp1_safe_receiver.open) == true);
      var tmp2_safe_receiver = $instDetails;
      var tmp_0;
      if (tmp2_safe_receiver == null) {
        tmp_0 = null;
      } else {
        // Inline function 'kotlin.js.unsafeCast' call
        // Inline function 'kotlin.js.asDynamic' call
        tmp_0 = tmp2_safe_receiver;
      }
      var tmp3_safe_receiver = tmp_0;
      $this$updateState.set_adminInstructorsSectionOpen_ns1zpy_k$((tmp3_safe_receiver == null ? null : tmp3_safe_receiver.open) == true);
      var tmp4_safe_receiver = $cadetDetails;
      var tmp_1;
      if (tmp4_safe_receiver == null) {
        tmp_1 = null;
      } else {
        // Inline function 'kotlin.js.unsafeCast' call
        // Inline function 'kotlin.js.asDynamic' call
        tmp_1 = tmp4_safe_receiver;
      }
      var tmp5_safe_receiver = tmp_1;
      $this$updateState.set_adminCadetsSectionOpen_fepkwa_k$((tmp5_safe_receiver == null ? null : tmp5_safe_receiver.open) == true);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_1($instId) {
    return function ($this$updateState) {
      $this$updateState.set_adminInstructorCadetsModalId_iqbhi4_k$($instId);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_2($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_adminInstructorCadetsModalId_iqbhi4_k$(null);
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda_3($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_adminInstructorCadetsModalId_iqbhi4_k$(null);
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda_4($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_selectedChatContactId_74xer0_k$(null);
    $this$updateState.set_chatMessages_b9arcw_k$(emptyList());
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda_5($idx, $nOpen, $iOpen, $cOpen) {
    return function ($this$updateState) {
      $this$updateState.set_selectedTabIndex_84igtv_k$($idx);
      $this$updateState.set_recordingLoading_87pylv_k$(false);
      $this$updateState.set_historyLoading_b45h4g_k$(false);
      $this$updateState.set_balanceAdminLoading_w4lmmh_k$(false);
      $this$updateState.set_chatContactsLoading_kr0hj5_k$(false);
      $this$updateState.set_adminHomeLoading_891u22_k$(false);
      var tmp;
      var tmp_0;
      var tmp0_safe_receiver = get_appState().get_user_wovspg_k$();
      if ((tmp0_safe_receiver == null ? null : tmp0_safe_receiver.get_role_wotsxr_k$()) === 'admin') {
        tmp_0 = !($idx === 0);
      } else {
        tmp_0 = false;
      }
      if (tmp_0) {
        $this$updateState.set_adminNewbiesSectionOpen_h0xy2x_k$($nOpen);
        $this$updateState.set_adminInstructorsSectionOpen_ns1zpy_k$($iOpen);
        $this$updateState.set_adminCadetsSectionOpen_fepkwa_k$($cOpen);
        tmp = Unit_getInstance();
      }
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_6($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_adminHomeLoading_891u22_k$(true);
    $this$updateState.set_networkError_6qqylc_k$(null);
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda_7() {
    _init_properties_Main_kt__xi25uv();
    updateState(setupPanelClickDelegation$lambda$lambda$lambda_0);
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_0($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_adminHomeLoading_891u22_k$(false);
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_1($list, $err) {
    return function ($this$updateState) {
      $this$updateState.set_adminHomeUsers_48iycq_k$($list);
      $this$updateState.set_balanceAdminUsers_8is91l_k$($list);
      $this$updateState.set_adminHomeLoading_891u22_k$(false);
      var tmp;
      if (!($err == null)) {
        $this$updateState.set_networkError_6qqylc_k$($err);
        tmp = Unit_getInstance();
      }
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_8($tid) {
    return function (list, err) {
      window.clearTimeout($tid);
      updateState(setupPanelClickDelegation$lambda$lambda$lambda_1(list, err));
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_9($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_balanceAdminLoading_w4lmmh_k$(true);
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda_10() {
    _init_properties_Main_kt__xi25uv();
    updateState(setupPanelClickDelegation$lambda$lambda$lambda_2);
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_2($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_balanceAdminLoading_w4lmmh_k$(false);
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda($list, $hist) {
    return function ($this$updateState) {
      $this$updateState.set_balanceAdminUsers_8is91l_k$($list);
      $this$updateState.set_adminHomeUsers_48iycq_k$($list);
      $this$updateState.set_balanceAdminHistory_5kfiug_k$($hist);
      $this$updateState.set_balanceAdminLoading_w4lmmh_k$(false);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_3($tid, $list) {
    return function (hist) {
      window.clearTimeout($tid);
      updateState(setupPanelClickDelegation$lambda$lambda$lambda$lambda($list, hist));
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_11($tid) {
    return function (list) {
      // Inline function 'kotlin.collections.map' call
      // Inline function 'kotlin.collections.mapTo' call
      var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(list, 10));
      var _iterator__ex2g4s = list.iterator_jk1svi_k$();
      while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
        var item = _iterator__ex2g4s.next_20eer_k$();
        var tmp$ret$0 = item.get_id_kntnx8_k$();
        destination.add_utx5q5_k$(tmp$ret$0);
      }
      var tmp = destination;
      loadBalanceHistoryForUsers(tmp, setupPanelClickDelegation$lambda$lambda$lambda_3($tid, list));
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_12($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_chatContactsLoading_kr0hj5_k$(true);
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda_13() {
    _init_properties_Main_kt__xi25uv();
    updateState(setupPanelClickDelegation$lambda$lambda$lambda_4);
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_4($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_chatContactsLoading_kr0hj5_k$(false);
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_5($list) {
    return function ($this$updateState) {
      $this$updateState.set_chatContacts_40tbaf_k$($list);
      $this$updateState.set_chatContactsLoading_kr0hj5_k$(false);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_14($chatTid) {
    return function (list) {
      window.clearTimeout($chatTid);
      updateState(setupPanelClickDelegation$lambda$lambda$lambda_5(list));
      // Inline function 'kotlin.collections.map' call
      // Inline function 'kotlin.collections.mapTo' call
      var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(list, 10));
      var _iterator__ex2g4s = list.iterator_jk1svi_k$();
      while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
        var item = _iterator__ex2g4s.next_20eer_k$();
        var tmp$ret$0 = item.get_id_kntnx8_k$();
        destination.add_utx5q5_k$(tmp$ret$0);
      }
      subscribeChatPresence(destination);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_15($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_recordingLoading_87pylv_k$(true);
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda_16() {
    _init_properties_Main_kt__xi25uv();
    updateState(setupPanelClickDelegation$lambda$lambda$lambda_6);
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_6($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_recordingLoading_87pylv_k$(false);
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda_0($wins, $sess) {
    return function ($this$updateState) {
      $this$updateState.set_recordingOpenWindows_boom0c_k$($wins);
      $this$updateState.set_recordingSessions_nfqq7s_k$($sess);
      $this$updateState.set_recordingLoading_87pylv_k$(false);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda$lambda($list, $user) {
    return function ($this$updateState) {
      // Inline function 'kotlin.collections.filter' call
      var tmp0 = $list;
      // Inline function 'kotlin.collections.filterTo' call
      var destination = ArrayList_init_$Create$();
      var _iterator__ex2g4s = tmp0.iterator_jk1svi_k$();
      while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
        var element = _iterator__ex2g4s.next_20eer_k$();
        if ($user.get_assignedCadets_bue0kr_k$().contains_aljjnj_k$(element.get_id_kntnx8_k$())) {
          destination.add_utx5q5_k$(element);
        }
      }
      $this$updateState.set_instructorCadets_zfaiwn_k$(destination);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda_1($user) {
    return function (list) {
      updateState(setupPanelClickDelegation$lambda$lambda$lambda$lambda$lambda(list, $user));
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_7($tid, $wins, $user) {
    return function (sess) {
      window.clearTimeout($tid);
      updateState(setupPanelClickDelegation$lambda$lambda$lambda$lambda_0($wins, sess));
      getUsers(setupPanelClickDelegation$lambda$lambda$lambda$lambda_1($user));
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_17($user, $tid) {
    return function (wins) {
      var tmp = $user.get_id_kntnx8_k$();
      getSessionsForInstructor(tmp, setupPanelClickDelegation$lambda$lambda$lambda_7($tid, wins, $user));
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda_2($wins, $sess) {
    return function ($this$updateState) {
      $this$updateState.set_recordingOpenWindows_boom0c_k$($wins);
      $this$updateState.set_recordingSessions_nfqq7s_k$($sess);
      $this$updateState.set_recordingLoading_87pylv_k$(false);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda_3(inst) {
    _init_properties_Main_kt__xi25uv();
    updateState(setupPanelClickDelegation$lambda$lambda$lambda$lambda$lambda_0(inst));
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda$lambda_0($inst) {
    return function ($this$updateState) {
      $this$updateState.set_cadetInstructor_tunjhh_k$($inst);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_8($tid, $user, $wins) {
    return function (sess) {
      window.clearTimeout($tid);
      updateState(setupPanelClickDelegation$lambda$lambda$lambda$lambda_2($wins, sess));
      var tmp0_safe_receiver = $user.get_assignedInstructorId_laxw6p_k$();
      if (tmp0_safe_receiver == null)
        null;
      else {
        // Inline function 'kotlin.let' call
        getUserById(tmp0_safe_receiver, setupPanelClickDelegation$lambda$lambda$lambda$lambda_3);
      }
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_18($user, $tid) {
    return function (wins) {
      var tmp = $user.get_id_kntnx8_k$();
      getSessionsForCadet(tmp, setupPanelClickDelegation$lambda$lambda$lambda_8($tid, $user, wins));
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_19(err) {
    _init_properties_Main_kt__xi25uv();
    if (!(err == null)) {
      updateState(setupPanelClickDelegation$lambda$lambda$lambda_9(err));
    } else {
      getUsers(setupPanelClickDelegation$lambda$lambda$lambda_10);
    }
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_9($err) {
    return function ($this$updateState) {
      $this$updateState.set_networkError_6qqylc_k$($err);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_10(list) {
    _init_properties_Main_kt__xi25uv();
    updateState(setupPanelClickDelegation$lambda$lambda$lambda$lambda_4(list));
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda_4($list) {
    return function ($this$updateState) {
      $this$updateState.set_adminHomeUsers_48iycq_k$($list);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_20($id) {
    return function ($this$updateState) {
      $this$updateState.set_adminAssignInstructorId_hlct9e_k$($id);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_21() {
    _init_properties_Main_kt__xi25uv();
    var tmp0_safe_receiver = document.getElementById('sd-assign-panel');
    var tmp;
    if (tmp0_safe_receiver == null) {
      tmp = null;
    } else {
      tmp0_safe_receiver.scrollIntoView({block: 'start', behavior: 'smooth'});
      tmp = Unit_getInstance();
    }
    return tmp;
  }
  function setupPanelClickDelegation$lambda$lambda_22($cadetId) {
    return function ($this$updateState) {
      $this$updateState.set_adminAssignCadetId_ihf1g8_k$($cadetId);
      $this$updateState.set_adminAssignInstructorId_hlct9e_k$(null);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_23() {
    _init_properties_Main_kt__xi25uv();
    var tmp0_safe_receiver = document.getElementById('sd-assign-panel');
    var tmp;
    if (tmp0_safe_receiver == null) {
      tmp = null;
    } else {
      tmp0_safe_receiver.scrollIntoView({block: 'start', behavior: 'smooth'});
      tmp = Unit_getInstance();
    }
    return tmp;
  }
  function setupPanelClickDelegation$lambda$lambda_24(err) {
    _init_properties_Main_kt__xi25uv();
    if (!(err == null)) {
      updateState(setupPanelClickDelegation$lambda$lambda$lambda_11(err));
    } else {
      getUsers(setupPanelClickDelegation$lambda$lambda$lambda_12);
    }
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_11($err) {
    return function ($this$updateState) {
      $this$updateState.set_networkError_6qqylc_k$($err);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_12(list) {
    _init_properties_Main_kt__xi25uv();
    updateState(setupPanelClickDelegation$lambda$lambda$lambda$lambda_5(list));
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda_5($list) {
    return function ($this$updateState) {
      $this$updateState.set_adminHomeUsers_48iycq_k$($list);
      $this$updateState.set_adminAssignCadetId_ihf1g8_k$(null);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_25(err) {
    _init_properties_Main_kt__xi25uv();
    if (!(err == null)) {
      updateState(setupPanelClickDelegation$lambda$lambda$lambda_13(err));
    } else {
      getUsers(setupPanelClickDelegation$lambda$lambda$lambda_14);
    }
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_13($err) {
    return function ($this$updateState) {
      $this$updateState.set_networkError_6qqylc_k$($err);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_14(list) {
    _init_properties_Main_kt__xi25uv();
    updateState(setupPanelClickDelegation$lambda$lambda$lambda$lambda_6(list));
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda_6($list) {
    return function ($this$updateState) {
      $this$updateState.set_adminHomeUsers_48iycq_k$($list);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_26(err) {
    _init_properties_Main_kt__xi25uv();
    if (!(err == null)) {
      updateState(setupPanelClickDelegation$lambda$lambda$lambda_15(err));
    } else {
      getUsers(setupPanelClickDelegation$lambda$lambda$lambda_16);
    }
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_15($err) {
    return function ($this$updateState) {
      $this$updateState.set_networkError_6qqylc_k$($err);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_16(list) {
    _init_properties_Main_kt__xi25uv();
    updateState(setupPanelClickDelegation$lambda$lambda$lambda$lambda_7(list));
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda_7($list) {
    return function ($this$updateState) {
      $this$updateState.set_adminHomeUsers_48iycq_k$($list);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_27($id) {
    return function ($this$updateState) {
      $this$updateState.set_balanceAdminSelectedUserId_tuhz6u_k$($id);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_28() {
    _init_properties_Main_kt__xi25uv();
    var tmp0_safe_receiver = document.getElementById('sd-balance-selected-block');
    var tmp;
    if (tmp0_safe_receiver == null) {
      tmp = null;
    } else {
      tmp0_safe_receiver.scrollIntoView({block: 'start', behavior: 'smooth'});
      tmp = Unit_getInstance();
    }
    return tmp;
  }
  function setupPanelClickDelegation$lambda$lambda_29($contactId) {
    return function ($this$updateState) {
      $this$updateState.set_selectedChatContactId_74xer0_k$($contactId);
      $this$updateState.set_chatMessages_b9arcw_k$(emptyList());
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_30(list) {
    _init_properties_Main_kt__xi25uv();
    updateState(setupPanelClickDelegation$lambda$lambda$lambda_17(list));
    var tmp = window;
    tmp.setTimeout(setupPanelClickDelegation$lambda$lambda$lambda_18, 100);
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_17($list) {
    return function ($this$updateState) {
      $this$updateState.set_chatMessages_b9arcw_k$($list);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_18() {
    _init_properties_Main_kt__xi25uv();
    var tmp0_safe_receiver = document.getElementById('sd-chat-messages');
    var tmp1_safe_receiver = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.lastElementChild;
    var tmp;
    if (tmp1_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.js.unsafeCast' call
      // Inline function 'kotlin.js.asDynamic' call
      tmp = tmp1_safe_receiver;
    }
    var tmp2_safe_receiver = tmp;
    return tmp2_safe_receiver == null ? null : tmp2_safe_receiver.scrollIntoView({block: 'end', behavior: 'smooth'});
  }
  function setupPanelClickDelegation$lambda$lambda_31($contactId) {
    return function ($this$updateState) {
      $this$updateState.set_selectedTabIndex_84igtv_k$(2);
      $this$updateState.set_selectedChatContactId_74xer0_k$($contactId);
      $this$updateState.set_chatMessages_b9arcw_k$(emptyList());
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_32(list) {
    _init_properties_Main_kt__xi25uv();
    updateState(setupPanelClickDelegation$lambda$lambda$lambda_19(list));
    var tmp = window;
    tmp.setTimeout(setupPanelClickDelegation$lambda$lambda$lambda_20, 100);
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_19($list) {
    return function ($this$updateState) {
      $this$updateState.set_chatMessages_b9arcw_k$($list);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_20() {
    _init_properties_Main_kt__xi25uv();
    var tmp0_safe_receiver = document.getElementById('sd-chat-messages');
    var tmp1_safe_receiver = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.lastElementChild;
    var tmp;
    if (tmp1_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.js.unsafeCast' call
      // Inline function 'kotlin.js.asDynamic' call
      tmp = tmp1_safe_receiver;
    }
    var tmp2_safe_receiver = tmp;
    return tmp2_safe_receiver == null ? null : tmp2_safe_receiver.scrollIntoView({block: 'end', behavior: 'smooth'});
  }
  function setupPanelClickDelegation$lambda$lambda_33($contactId) {
    return function ($this$updateState) {
      $this$updateState.set_selectedTabIndex_84igtv_k$(2);
      $this$updateState.set_selectedChatContactId_74xer0_k$($contactId);
      $this$updateState.set_chatMessages_b9arcw_k$(emptyList());
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda_34(list) {
    _init_properties_Main_kt__xi25uv();
    updateState(setupPanelClickDelegation$lambda$lambda$lambda_21(list));
    var tmp = window;
    tmp.setTimeout(setupPanelClickDelegation$lambda$lambda$lambda_22, 100);
    return Unit_getInstance();
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_21($list) {
    return function ($this$updateState) {
      $this$updateState.set_chatMessages_b9arcw_k$($list);
      return Unit_getInstance();
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_22() {
    _init_properties_Main_kt__xi25uv();
    var tmp0_safe_receiver = document.getElementById('sd-chat-messages');
    var tmp1_safe_receiver = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.lastElementChild;
    var tmp;
    if (tmp1_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.js.unsafeCast' call
      // Inline function 'kotlin.js.asDynamic' call
      tmp = tmp1_safe_receiver;
    }
    var tmp2_safe_receiver = tmp;
    return tmp2_safe_receiver == null ? null : tmp2_safe_receiver.scrollIntoView({block: 'end', behavior: 'smooth'});
  }
  function attachListeners$lambda(it) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_networkError_6qqylc_k$(null);
    return Unit_getInstance();
  }
  function attachListeners$lambda_0(it) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda_0);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_0($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_recordingLoading_87pylv_k$(false);
    return Unit_getInstance();
  }
  function attachListeners$lambda_1(it) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda_1);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_1($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_historyLoading_b45h4g_k$(false);
    return Unit_getInstance();
  }
  function attachListeners$lambda_2(it) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda_2);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_2($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_balanceAdminLoading_w4lmmh_k$(false);
    return Unit_getInstance();
  }
  function attachListeners$lambda_3(it) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda_3);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_3($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_chatContactsLoading_kr0hj5_k$(false);
    return Unit_getInstance();
  }
  function attachListeners$lambda_4(it) {
    _init_properties_Main_kt__xi25uv();
    var tmp0_elvis_lhs = get_appState().get_user_wovspg_k$();
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return Unit_getInstance();
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var u = tmp;
    updateState(attachListeners$lambda$lambda_4);
    getUsersForChat(u, attachListeners$lambda$lambda_5);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_4($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_chatContacts_40tbaf_k$(emptyList());
    $this$updateState.set_chatContactsLoading_kr0hj5_k$(true);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_5(list) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda$lambda(list));
    // Inline function 'kotlin.collections.map' call
    // Inline function 'kotlin.collections.mapTo' call
    var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(list, 10));
    var _iterator__ex2g4s = list.iterator_jk1svi_k$();
    while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
      var item = _iterator__ex2g4s.next_20eer_k$();
      var tmp$ret$0 = item.get_id_kntnx8_k$();
      destination.add_utx5q5_k$(tmp$ret$0);
    }
    subscribeChatPresence(destination);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda($list) {
    return function ($this$updateState) {
      $this$updateState.set_chatContacts_40tbaf_k$($list);
      $this$updateState.set_chatContactsLoading_kr0hj5_k$(false);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda_5(it) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda_6);
    var tmp = window;
    var tid = tmp.setTimeout(attachListeners$lambda$lambda_7, 8000);
    getUsersWithError(attachListeners$lambda$lambda_8(tid));
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_6($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_adminHomeLoading_891u22_k$(true);
    $this$updateState.set_networkError_6qqylc_k$(null);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_7() {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda$lambda_0);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_0($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_adminHomeLoading_891u22_k$(false);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_1($list, $err) {
    return function ($this$updateState) {
      $this$updateState.set_adminHomeUsers_48iycq_k$($list);
      $this$updateState.set_balanceAdminUsers_8is91l_k$($list);
      $this$updateState.set_adminHomeLoading_891u22_k$(false);
      var tmp;
      if (!($err == null)) {
        $this$updateState.set_networkError_6qqylc_k$($err);
        tmp = Unit_getInstance();
      }
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_8($tid) {
    return function (list, err) {
      window.clearTimeout($tid);
      updateState(attachListeners$lambda$lambda$lambda_1(list, err));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda_6(it) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda_9);
    var tmp = window;
    var tid = tmp.setTimeout(attachListeners$lambda$lambda_10, 8000);
    getUsers(attachListeners$lambda$lambda_11(tid));
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_9($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_balanceAdminLoading_w4lmmh_k$(true);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_10() {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda$lambda_2);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_2($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_balanceAdminLoading_w4lmmh_k$(false);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda$lambda($list, $hist) {
    return function ($this$updateState) {
      $this$updateState.set_balanceAdminUsers_8is91l_k$($list);
      $this$updateState.set_adminHomeUsers_48iycq_k$($list);
      $this$updateState.set_balanceAdminHistory_5kfiug_k$($hist);
      $this$updateState.set_balanceAdminLoading_w4lmmh_k$(false);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda_3($tid, $list) {
    return function (hist) {
      window.clearTimeout($tid);
      updateState(attachListeners$lambda$lambda$lambda$lambda($list, hist));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_11($tid) {
    return function (list) {
      // Inline function 'kotlin.collections.map' call
      // Inline function 'kotlin.collections.mapTo' call
      var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(list, 10));
      var _iterator__ex2g4s = list.iterator_jk1svi_k$();
      while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
        var item = _iterator__ex2g4s.next_20eer_k$();
        var tmp$ret$0 = item.get_id_kntnx8_k$();
        destination.add_utx5q5_k$(tmp$ret$0);
      }
      var tmp = destination;
      loadBalanceHistoryForUsers(tmp, attachListeners$lambda$lambda$lambda_3($tid, list));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda_7(it) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda_12);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_12($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_adminAssignInstructorId_hlct9e_k$(null);
    $this$updateState.set_adminAssignCadetId_ihf1g8_k$(null);
    return Unit_getInstance();
  }
  function attachListeners$lambda_8(it) {
    _init_properties_Main_kt__xi25uv();
    var tmp = document.getElementById('sd-email');
    var tmp0_safe_receiver = tmp instanceof HTMLInputElement ? tmp : null;
    var tmp1_elvis_lhs = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.value;
    // Inline function 'kotlin.text.trim' call
    var this_0 = tmp1_elvis_lhs == null ? '' : tmp1_elvis_lhs;
    var email = toString_0(trim(isCharSequence(this_0) ? this_0 : THROW_CCE()));
    var tmp_0 = document.getElementById('sd-password');
    var tmp2_safe_receiver = tmp_0 instanceof HTMLInputElement ? tmp_0 : null;
    var tmp3_elvis_lhs = tmp2_safe_receiver == null ? null : tmp2_safe_receiver.value;
    var password = tmp3_elvis_lhs == null ? '' : tmp3_elvis_lhs;
    if (isBlank(email) || isBlank(password)) {
      updateState(attachListeners$lambda$lambda_13);
      return Unit_getInstance();
    }
    updateState(attachListeners$lambda$lambda_14);
    var tmp_1 = signIn(email, password);
    var tmp_2 = tmp_1.then(attachListeners$lambda$lambda_15);
    tmp_2.catch(attachListeners$lambda$lambda_16);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_13($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_error_5ld8to_k$('\u0412\u0432\u0435\u0434\u0438\u0442\u0435 email \u0438 \u043F\u0430\u0440\u043E\u043B\u044C');
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_14($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_loading_7od76y_k$(true);
    $this$updateState.set_error_5ld8to_k$(null);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_15(it) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda$lambda_4);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_4($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_loading_7od76y_k$(false);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_16(e) {
    _init_properties_Main_kt__xi25uv();
    // Inline function 'kotlin.js.asDynamic' call
    var tmp = e.code;
    var code = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
    // Inline function 'kotlin.js.asDynamic' call
    var tmp_0 = e.message;
    var tmp0_elvis_lhs = (!(tmp_0 == null) ? typeof tmp_0 === 'string' : false) ? tmp_0 : null;
    var rawMsg = tmp0_elvis_lhs == null ? '' : tmp0_elvis_lhs;
    var tmp_1;
    switch (code) {
      case 'auth/invalid-credential':
      case 'auth/wrong-password':
      case 'auth/user-not-found':
        tmp_1 = '\u041D\u0435\u0432\u0435\u0440\u043D\u044B\u0439 email \u0438\u043B\u0438 \u043F\u0430\u0440\u043E\u043B\u044C. \u0418\u0441\u043F\u043E\u043B\u044C\u0437\u0443\u0439\u0442\u0435 \u0443\u0447\u0451\u0442\u043D\u0443\u044E \u0437\u0430\u043F\u0438\u0441\u044C \u0438\u0437 \u043F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u044F \u0438\u043B\u0438 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u0443\u0439\u0442\u0435\u0441\u044C.';
        break;
      case 'auth/invalid-email':
        tmp_1 = '\u041D\u0435\u043A\u043E\u0440\u0440\u0435\u043A\u0442\u043D\u044B\u0439 email.';
        break;
      case 'auth/too-many-requests':
        tmp_1 = '\u0421\u043B\u0438\u0448\u043A\u043E\u043C \u043C\u043D\u043E\u0433\u043E \u043F\u043E\u043F\u044B\u0442\u043E\u043A. \u041F\u043E\u0434\u043E\u0436\u0434\u0438\u0442\u0435 \u0438 \u043F\u043E\u043F\u0440\u043E\u0431\u0443\u0439\u0442\u0435 \u0441\u043D\u043E\u0432\u0430.';
        break;
      case 'auth/network-request-failed':
        tmp_1 = '\u041D\u0435\u0442 \u0441\u043E\u0435\u0434\u0438\u043D\u0435\u043D\u0438\u044F \u0441 \u0438\u043D\u0442\u0435\u0440\u043D\u0435\u0442\u043E\u043C.';
        break;
      default:
        // Inline function 'kotlin.text.ifBlank' call

        var tmp_2;
        if (isBlank(rawMsg)) {
          tmp_2 = '\u041E\u0448\u0438\u0431\u043A\u0430 \u0432\u0445\u043E\u0434\u0430';
        } else {
          tmp_2 = rawMsg;
        }

        tmp_1 = tmp_2;
        break;
    }
    var msg = tmp_1;
    if (code === 'auth/network-request-failed') {
      updateState(attachListeners$lambda$lambda$lambda_5);
    }
    updateState(attachListeners$lambda$lambda$lambda_6(msg));
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_5($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_networkError_6qqylc_k$('\u041D\u0435\u0442 \u0441\u043E\u0435\u0434\u0438\u043D\u0435\u043D\u0438\u044F \u0441 \u0438\u043D\u0442\u0435\u0440\u043D\u0435\u0442\u043E\u043C.');
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_6($msg) {
    return function ($this$updateState) {
      $this$updateState.set_loading_7od76y_k$(false);
      $this$updateState.set_error_5ld8to_k$($msg);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda_9(it) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda_17);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_17($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_screen_91y0x9_k$(AppScreen_Register_getInstance());
    $this$updateState.set_error_5ld8to_k$(null);
    return Unit_getInstance();
  }
  function attachListeners$lambda_10(it) {
    _init_properties_Main_kt__xi25uv();
    var tmp = document.getElementById('sd-fullName');
    var tmp0_safe_receiver = tmp instanceof HTMLInputElement ? tmp : null;
    var tmp1_elvis_lhs = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.value;
    // Inline function 'kotlin.text.trim' call
    var this_0 = tmp1_elvis_lhs == null ? '' : tmp1_elvis_lhs;
    var fullName = toString_0(trim(isCharSequence(this_0) ? this_0 : THROW_CCE()));
    var tmp_0 = document.getElementById('sd-reg-email');
    var tmp2_safe_receiver = tmp_0 instanceof HTMLInputElement ? tmp_0 : null;
    var tmp3_elvis_lhs = tmp2_safe_receiver == null ? null : tmp2_safe_receiver.value;
    // Inline function 'kotlin.text.trim' call
    var this_1 = tmp3_elvis_lhs == null ? '' : tmp3_elvis_lhs;
    var email = toString_0(trim(isCharSequence(this_1) ? this_1 : THROW_CCE()));
    var tmp_1 = document.getElementById('sd-phone');
    var tmp4_safe_receiver = tmp_1 instanceof HTMLInputElement ? tmp_1 : null;
    var tmp5_elvis_lhs = tmp4_safe_receiver == null ? null : tmp4_safe_receiver.value;
    // Inline function 'kotlin.text.trim' call
    var this_2 = tmp5_elvis_lhs == null ? '' : tmp5_elvis_lhs;
    var phone = toString_0(trim(isCharSequence(this_2) ? this_2 : THROW_CCE()));
    var tmp_2 = document.getElementById('sd-reg-password');
    var tmp6_safe_receiver = tmp_2 instanceof HTMLInputElement ? tmp_2 : null;
    var tmp7_elvis_lhs = tmp6_safe_receiver == null ? null : tmp6_safe_receiver.value;
    var password = tmp7_elvis_lhs == null ? '' : tmp7_elvis_lhs;
    var tmp_3 = document.getElementById('sd-role');
    var tmp8_safe_receiver = tmp_3 instanceof HTMLSelectElement ? tmp_3 : null;
    var tmp9_elvis_lhs = tmp8_safe_receiver == null ? null : tmp8_safe_receiver.value;
    var role = tmp9_elvis_lhs == null ? 'cadet' : tmp9_elvis_lhs;
    if (isBlank(fullName) || isBlank(email) || isBlank(password)) {
      updateState(attachListeners$lambda$lambda_18);
      return Unit_getInstance();
    }
    if (password.length < 6) {
      updateState(attachListeners$lambda$lambda_19);
      return Unit_getInstance();
    }
    updateState(attachListeners$lambda$lambda_20);
    var tmp_4 = register(fullName, email, phone, password, role);
    var tmp_5 = tmp_4.then(attachListeners$lambda$lambda_21);
    tmp_5.catch(attachListeners$lambda$lambda_22);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_18($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_error_5ld8to_k$('\u0417\u0430\u043F\u043E\u043B\u043D\u0438\u0442\u0435 \u043E\u0431\u044F\u0437\u0430\u0442\u0435\u043B\u044C\u043D\u044B\u0435 \u043F\u043E\u043B\u044F');
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_19($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_error_5ld8to_k$('\u041F\u0430\u0440\u043E\u043B\u044C \u0434\u043E\u043B\u0436\u0435\u043D \u0431\u044B\u0442\u044C \u043D\u0435 \u043A\u043E\u0440\u043E\u0447\u0435 6 \u0441\u0438\u043C\u0432\u043E\u043B\u043E\u0432');
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_20($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_loading_7od76y_k$(true);
    $this$updateState.set_error_5ld8to_k$(null);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_21(it) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda$lambda_7);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_7($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_loading_7od76y_k$(false);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_22(e) {
    _init_properties_Main_kt__xi25uv();
    // Inline function 'kotlin.js.asDynamic' call
    var tmp = e.code;
    var code = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
    // Inline function 'kotlin.js.asDynamic' call
    var tmp_0 = e.message;
    var tmp0_elvis_lhs = (!(tmp_0 == null) ? typeof tmp_0 === 'string' : false) ? tmp_0 : null;
    var rawMsg = tmp0_elvis_lhs == null ? '' : tmp0_elvis_lhs;
    var tmp_1;
    switch (code) {
      case 'auth/email-already-in-use':
        tmp_1 = '\u042D\u0442\u043E\u0442 email \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D. \u0412\u043E\u0439\u0434\u0438\u0442\u0435 \u0438\u043B\u0438 \u0432\u043E\u0441\u0441\u0442\u0430\u043D\u043E\u0432\u0438\u0442\u0435 \u043F\u0430\u0440\u043E\u043B\u044C.';
        break;
      case 'auth/invalid-email':
        tmp_1 = '\u041D\u0435\u043A\u043E\u0440\u0440\u0435\u043A\u0442\u043D\u044B\u0439 email.';
        break;
      case 'auth/weak-password':
        tmp_1 = '\u041F\u0430\u0440\u043E\u043B\u044C \u0441\u043B\u0438\u0448\u043A\u043E\u043C \u043F\u0440\u043E\u0441\u0442\u043E\u0439 (\u043C\u0438\u043D\u0438\u043C\u0443\u043C 6 \u0441\u0438\u043C\u0432\u043E\u043B\u043E\u0432).';
        break;
      case 'auth/network-request-failed':
        tmp_1 = '\u041D\u0435\u0442 \u0441\u043E\u0435\u0434\u0438\u043D\u0435\u043D\u0438\u044F \u0441 \u0438\u043D\u0442\u0435\u0440\u043D\u0435\u0442\u043E\u043C.';
        break;
      default:
        // Inline function 'kotlin.text.ifBlank' call

        var tmp_2;
        if (isBlank(rawMsg)) {
          tmp_2 = '\u041E\u0448\u0438\u0431\u043A\u0430 \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u0438';
        } else {
          tmp_2 = rawMsg;
        }

        tmp_1 = tmp_2;
        break;
    }
    var msg = tmp_1;
    if (code === 'auth/network-request-failed') {
      updateState(attachListeners$lambda$lambda$lambda_8);
    }
    updateState(attachListeners$lambda$lambda$lambda_9(msg));
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_8($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_networkError_6qqylc_k$('\u041D\u0435\u0442 \u0441\u043E\u0435\u0434\u0438\u043D\u0435\u043D\u0438\u044F \u0441 \u0438\u043D\u0442\u0435\u0440\u043D\u0435\u0442\u043E\u043C.');
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_9($msg) {
    return function ($this$updateState) {
      $this$updateState.set_loading_7od76y_k$(false);
      $this$updateState.set_error_5ld8to_k$($msg);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda_11(it) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda_23);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_23($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_screen_91y0x9_k$(AppScreen_Login_getInstance());
    $this$updateState.set_error_5ld8to_k$(null);
    return Unit_getInstance();
  }
  function attachListeners$lambda_12(it) {
    _init_properties_Main_kt__xi25uv();
    var tmp0_elvis_lhs = getCurrentUserId();
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return Unit_getInstance();
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var uid = tmp;
    updateState(attachListeners$lambda$lambda_24);
    getCurrentUser(attachListeners$lambda$lambda_25);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_24($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_loading_7od76y_k$(true);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_25(user, _unused_var__etf5q3) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda$lambda_10);
    if (!(user == null) && user.get_isActive_quafmh_k$()) {
      updateState(attachListeners$lambda$lambda$lambda_11(user));
    }
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_10($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_loading_7od76y_k$(false);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_11($user) {
    return function ($this$updateState) {
      $this$updateState.set_user_gqmzbm_k$($user);
      var tmp0_subject = $user.get_role_wotsxr_k$();
      $this$updateState.set_screen_91y0x9_k$(tmp0_subject === 'admin' ? AppScreen_Admin_getInstance() : tmp0_subject === 'instructor' ? AppScreen_Instructor_getInstance() : tmp0_subject === 'cadet' ? AppScreen_Cadet_getInstance() : AppScreen_PendingApproval_getInstance());
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda_13(it) {
    _init_properties_Main_kt__xi25uv();
    signOut();
    return Unit_getInstance();
  }
  function attachListeners$lambda_14(it) {
    _init_properties_Main_kt__xi25uv();
    signOut();
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_26($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_chatContactsLoading_kr0hj5_k$(true);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_27() {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda$lambda_12);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_12($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_chatContactsLoading_kr0hj5_k$(false);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_13($list) {
    return function ($this$updateState) {
      $this$updateState.set_chatContacts_40tbaf_k$($list);
      $this$updateState.set_chatContactsLoading_kr0hj5_k$(false);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_28($chatTid) {
    return function (list) {
      window.clearTimeout($chatTid);
      updateState(attachListeners$lambda$lambda$lambda_13(list));
      // Inline function 'kotlin.collections.map' call
      // Inline function 'kotlin.collections.mapTo' call
      var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(list, 10));
      var _iterator__ex2g4s = list.iterator_jk1svi_k$();
      while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
        var item = _iterator__ex2g4s.next_20eer_k$();
        var tmp$ret$0 = item.get_id_kntnx8_k$();
        destination.add_utx5q5_k$(tmp$ret$0);
      }
      subscribeChatPresence(destination);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_29($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_recordingLoading_87pylv_k$(true);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_30() {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda$lambda_14);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_14($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_recordingLoading_87pylv_k$(false);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda$lambda_0($wins, $sess) {
    return function ($this$updateState) {
      $this$updateState.set_recordingOpenWindows_boom0c_k$($wins);
      $this$updateState.set_recordingSessions_nfqq7s_k$($sess);
      $this$updateState.set_recordingLoading_87pylv_k$(false);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda$lambda$lambda($list, $usr) {
    return function ($this$updateState) {
      // Inline function 'kotlin.collections.filter' call
      var tmp0 = $list;
      // Inline function 'kotlin.collections.filterTo' call
      var destination = ArrayList_init_$Create$();
      var _iterator__ex2g4s = tmp0.iterator_jk1svi_k$();
      while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
        var element = _iterator__ex2g4s.next_20eer_k$();
        if ($usr.get_assignedCadets_bue0kr_k$().contains_aljjnj_k$(element.get_id_kntnx8_k$())) {
          destination.add_utx5q5_k$(element);
        }
      }
      $this$updateState.set_instructorCadets_zfaiwn_k$(destination);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda$lambda_1($usr) {
    return function (list) {
      updateState(attachListeners$lambda$lambda$lambda$lambda$lambda(list, $usr));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda_15($tid, $wins, $usr) {
    return function (sess) {
      window.clearTimeout($tid);
      updateState(attachListeners$lambda$lambda$lambda$lambda_0($wins, sess));
      getUsers(attachListeners$lambda$lambda$lambda$lambda_1($usr));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_31($usr, $tid) {
    return function (wins) {
      var tmp = $usr.get_id_kntnx8_k$();
      getSessionsForInstructor(tmp, attachListeners$lambda$lambda$lambda_15($tid, wins, $usr));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda$lambda_2($list, $ids, $freshUser) {
    return function ($this$updateState) {
      // Inline function 'kotlin.collections.filter' call
      var tmp0 = $list;
      // Inline function 'kotlin.collections.filterTo' call
      var destination = ArrayList_init_$Create$();
      var _iterator__ex2g4s = tmp0.iterator_jk1svi_k$();
      while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
        var element = _iterator__ex2g4s.next_20eer_k$();
        if ($ids.contains_aljjnj_k$(element.get_id_kntnx8_k$())) {
          destination.add_utx5q5_k$(element);
        }
      }
      $this$updateState.set_instructorCadets_zfaiwn_k$(destination);
      var tmp;
      if (!($freshUser == null)) {
        $this$updateState.set_user_gqmzbm_k$($freshUser);
        tmp = Unit_getInstance();
      }
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda_16($ids, $freshUser) {
    return function (list) {
      updateState(attachListeners$lambda$lambda$lambda$lambda_2(list, $ids, $freshUser));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_32($usr) {
    return function (freshUser, _unused_var__etf5q3) {
      var tmp1_elvis_lhs = freshUser == null ? null : freshUser.get_assignedCadets_bue0kr_k$();
      var ids = tmp1_elvis_lhs == null ? $usr.get_assignedCadets_bue0kr_k$() : tmp1_elvis_lhs;
      getUsers(attachListeners$lambda$lambda$lambda_16(ids, freshUser));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_33($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_recordingLoading_87pylv_k$(true);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_34() {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda$lambda_17);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_17($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_recordingLoading_87pylv_k$(false);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda$lambda_3($wins, $sess) {
    return function ($this$updateState) {
      $this$updateState.set_recordingOpenWindows_boom0c_k$($wins);
      $this$updateState.set_recordingSessions_nfqq7s_k$($sess);
      $this$updateState.set_recordingLoading_87pylv_k$(false);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda$lambda_4(inst) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda$lambda$lambda$lambda_0(inst));
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda$lambda$lambda_0($inst) {
    return function ($this$updateState) {
      $this$updateState.set_cadetInstructor_tunjhh_k$($inst);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda_18($tid, $usr, $wins) {
    return function (sess) {
      window.clearTimeout($tid);
      updateState(attachListeners$lambda$lambda$lambda$lambda_3($wins, sess));
      var tmp0_safe_receiver = $usr.get_assignedInstructorId_laxw6p_k$();
      if (tmp0_safe_receiver == null)
        null;
      else {
        // Inline function 'kotlin.let' call
        getUserById(tmp0_safe_receiver, attachListeners$lambda$lambda$lambda$lambda_4);
      }
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_35($usr, $tid) {
    return function (wins) {
      var tmp = $usr.get_id_kntnx8_k$();
      getSessionsForCadet(tmp, attachListeners$lambda$lambda$lambda_18($tid, $usr, wins));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_36($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_historyLoading_b45h4g_k$(true);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_37() {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda$lambda_19);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_19($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_historyLoading_b45h4g_k$(false);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda$lambda_5($sess, $hist) {
    return function ($this$updateState) {
      $this$updateState.set_historySessions_497v17_k$($sess);
      $this$updateState.set_historyBalance_xu4jsp_k$($hist);
      $this$updateState.set_historyLoading_b45h4g_k$(false);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda_20($tid, $sess) {
    return function (hist) {
      window.clearTimeout($tid);
      updateState(attachListeners$lambda$lambda$lambda$lambda_5($sess, hist));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_38($usr, $tid) {
    return function (sess) {
      var tmp = $usr.get_id_kntnx8_k$();
      getBalanceHistory(tmp, attachListeners$lambda$lambda$lambda_20($tid, sess));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_39($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_historyLoading_b45h4g_k$(true);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_40() {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda$lambda_21);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_21($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_historyLoading_b45h4g_k$(false);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda$lambda_6($sess, $hist) {
    return function ($this$updateState) {
      $this$updateState.set_historySessions_497v17_k$($sess);
      $this$updateState.set_historyBalance_xu4jsp_k$($hist);
      $this$updateState.set_historyLoading_b45h4g_k$(false);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda_22($tid, $sess) {
    return function (hist) {
      window.clearTimeout($tid);
      updateState(attachListeners$lambda$lambda$lambda$lambda_6($sess, hist));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_41($usr, $tid) {
    return function (sess) {
      var tmp = $usr.get_id_kntnx8_k$();
      getBalanceHistory(tmp, attachListeners$lambda$lambda$lambda_22($tid, sess));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_42($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_historyLoading_b45h4g_k$(true);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_43() {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda$lambda_23);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_23($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_historyLoading_b45h4g_k$(false);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda$lambda_7($list, $hist) {
    return function ($this$updateState) {
      $this$updateState.set_balanceAdminUsers_8is91l_k$($list);
      $this$updateState.set_historyBalance_xu4jsp_k$($hist);
      $this$updateState.set_historyLoading_b45h4g_k$(false);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda_24($tid, $list) {
    return function (hist) {
      window.clearTimeout($tid);
      updateState(attachListeners$lambda$lambda$lambda$lambda_7($list, hist));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_44($tid) {
    return function (list) {
      // Inline function 'kotlin.collections.map' call
      // Inline function 'kotlin.collections.mapTo' call
      var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(list, 10));
      var _iterator__ex2g4s = list.iterator_jk1svi_k$();
      while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
        var item = _iterator__ex2g4s.next_20eer_k$();
        var tmp$ret$0 = item.get_id_kntnx8_k$();
        destination.add_utx5q5_k$(tmp$ret$0);
      }
      var tmp = destination;
      loadBalanceHistoryForUsers(tmp, attachListeners$lambda$lambda$lambda_24($tid, list));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_45($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_adminHomeLoading_891u22_k$(true);
    $this$updateState.set_networkError_6qqylc_k$(null);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_46() {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda$lambda_25);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_25($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_adminHomeLoading_891u22_k$(false);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_26($list, $err) {
    return function ($this$updateState) {
      $this$updateState.set_adminHomeUsers_48iycq_k$($list);
      $this$updateState.set_balanceAdminUsers_8is91l_k$($list);
      $this$updateState.set_adminHomeLoading_891u22_k$(false);
      var tmp;
      if (!($err == null)) {
        $this$updateState.set_networkError_6qqylc_k$($err);
        tmp = Unit_getInstance();
      }
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_47($tid) {
    return function (list, err) {
      window.clearTimeout($tid);
      updateState(attachListeners$lambda$lambda$lambda_26(list, err));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_48($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_balanceAdminLoading_w4lmmh_k$(true);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_49() {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda$lambda_27);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_27($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_balanceAdminLoading_w4lmmh_k$(false);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda$lambda_8($list, $hist) {
    return function ($this$updateState) {
      $this$updateState.set_balanceAdminUsers_8is91l_k$($list);
      $this$updateState.set_adminHomeUsers_48iycq_k$($list);
      $this$updateState.set_balanceAdminHistory_5kfiug_k$($hist);
      $this$updateState.set_balanceAdminLoading_w4lmmh_k$(false);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda_28($tid, $list) {
    return function (hist) {
      window.clearTimeout($tid);
      updateState(attachListeners$lambda$lambda$lambda$lambda_8($list, hist));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_50($tid) {
    return function (list) {
      // Inline function 'kotlin.collections.map' call
      // Inline function 'kotlin.collections.mapTo' call
      var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(list, 10));
      var _iterator__ex2g4s = list.iterator_jk1svi_k$();
      while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
        var item = _iterator__ex2g4s.next_20eer_k$();
        var tmp$ret$0 = item.get_id_kntnx8_k$();
        destination.add_utx5q5_k$(tmp$ret$0);
      }
      var tmp = destination;
      loadBalanceHistoryForUsers(tmp, attachListeners$lambda$lambda$lambda_28($tid, list));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda_15($uid) {
    return function () {
      var tmp;
      if (get_appState().get_selectedTabIndex_3g78ox_k$() === 2 && !($uid == null)) {
        var tmp_0;
        if (get_appState().get_chatContacts_b21mhg_k$().isEmpty_y1axqb_k$() && !get_appState().get_chatContactsLoading_r2se4o_k$()) {
          updateState(attachListeners$lambda$lambda_26);
          var tmp_1 = window;
          var chatTid = tmp_1.setTimeout(attachListeners$lambda$lambda_27, 5000);
          var tmp_2 = ensureNotNull(get_appState().get_user_wovspg_k$());
          getUsersForChat(tmp_2, attachListeners$lambda$lambda_28(chatTid));
          tmp_0 = Unit_getInstance();
        }
        tmp = tmp_0;
      }
      var tmp0_elvis_lhs = get_appState().get_user_wovspg_k$();
      var tmp_3;
      if (tmp0_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp_3 = tmp0_elvis_lhs;
      }
      var usr = tmp_3;
      var tmp_4;
      if (usr.get_role_wotsxr_k$() === 'instructor' && (get_appState().get_selectedTabIndex_3g78ox_k$() === 0 || get_appState().get_selectedTabIndex_3g78ox_k$() === 1)) {
        if (!get_appState().get_recordingLoading_h2yjak_k$() && get_appState().get_recordingSessions_veidxh_k$().isEmpty_y1axqb_k$() && get_appState().get_recordingOpenWindows_ecepd_k$().isEmpty_y1axqb_k$()) {
          updateState(attachListeners$lambda$lambda_29);
          var tmp_5 = window;
          var tid = tmp_5.setTimeout(attachListeners$lambda$lambda_30, 8000);
          var tmp_6 = usr.get_id_kntnx8_k$();
          getOpenWindowsForInstructor(tmp_6, attachListeners$lambda$lambda_31(usr, tid));
        }
        var tmp_7;
        if (get_appState().get_instructorCadets_elsrtw_k$().isEmpty_y1axqb_k$()) {
          getCurrentUser(attachListeners$lambda$lambda_32(usr));
          tmp_7 = Unit_getInstance();
        }
        tmp_4 = tmp_7;
      } else if (usr.get_role_wotsxr_k$() === 'cadet' && (get_appState().get_selectedTabIndex_3g78ox_k$() === 0 || get_appState().get_selectedTabIndex_3g78ox_k$() === 1)) {
        var tmp_8;
        if (!get_appState().get_recordingLoading_h2yjak_k$() && get_appState().get_recordingSessions_veidxh_k$().isEmpty_y1axqb_k$()) {
          updateState(attachListeners$lambda$lambda_33);
          var tmp_9 = window;
          var tid_0 = tmp_9.setTimeout(attachListeners$lambda$lambda_34, 8000);
          var tmp1_elvis_lhs = usr.get_assignedInstructorId_laxw6p_k$();
          var instId = tmp1_elvis_lhs == null ? '' : tmp1_elvis_lhs;
          getOpenWindowsForCadet(instId, attachListeners$lambda$lambda_35(usr, tid_0));
          tmp_8 = Unit_getInstance();
        }
        tmp_4 = tmp_8;
      } else if (usr.get_role_wotsxr_k$() === 'instructor' && get_appState().get_selectedTabIndex_3g78ox_k$() === 4) {
        var tmp_10;
        if (!get_appState().get_historyLoading_ytzv9t_k$() && get_appState().get_historySessions_72jo14_k$().isEmpty_y1axqb_k$()) {
          updateState(attachListeners$lambda$lambda_36);
          var tmp_11 = window;
          var tid_1 = tmp_11.setTimeout(attachListeners$lambda$lambda_37, 8000);
          var tmp_12 = usr.get_id_kntnx8_k$();
          getSessionsForInstructor(tmp_12, attachListeners$lambda$lambda_38(usr, tid_1));
          tmp_10 = Unit_getInstance();
        }
        tmp_4 = tmp_10;
      } else if (usr.get_role_wotsxr_k$() === 'cadet' && get_appState().get_selectedTabIndex_3g78ox_k$() === 4) {
        var tmp_13;
        if (!get_appState().get_historyLoading_ytzv9t_k$() && get_appState().get_historySessions_72jo14_k$().isEmpty_y1axqb_k$()) {
          updateState(attachListeners$lambda$lambda_39);
          var tmp_14 = window;
          var tid_2 = tmp_14.setTimeout(attachListeners$lambda$lambda_40, 8000);
          var tmp_15 = usr.get_id_kntnx8_k$();
          getSessionsForCadet(tmp_15, attachListeners$lambda$lambda_41(usr, tid_2));
          tmp_13 = Unit_getInstance();
        }
        tmp_4 = tmp_13;
      } else if (usr.get_role_wotsxr_k$() === 'admin' && get_appState().get_selectedTabIndex_3g78ox_k$() === 3) {
        var tmp_16;
        if (!get_appState().get_historyLoading_ytzv9t_k$() && get_appState().get_historyBalance_nnmchd_k$().isEmpty_y1axqb_k$()) {
          updateState(attachListeners$lambda$lambda_42);
          var tmp_17 = window;
          var tid_3 = tmp_17.setTimeout(attachListeners$lambda$lambda_43, 8000);
          getUsers(attachListeners$lambda$lambda_44(tid_3));
          tmp_16 = Unit_getInstance();
        }
        tmp_4 = tmp_16;
      } else if (usr.get_role_wotsxr_k$() === 'admin' && get_appState().get_selectedTabIndex_3g78ox_k$() === 0) {
        var tmp_18;
        if (!get_appState().get_adminHomeLoading_t0xi21_k$()) {
          updateState(attachListeners$lambda$lambda_45);
          var tmp_19 = window;
          var tid_4 = tmp_19.setTimeout(attachListeners$lambda$lambda_46, 8000);
          getUsersWithError(attachListeners$lambda$lambda_47(tid_4));
          tmp_18 = Unit_getInstance();
        }
        tmp_4 = tmp_18;
      } else if (usr.get_role_wotsxr_k$() === 'admin' && get_appState().get_selectedTabIndex_3g78ox_k$() === 1) {
        var tmp_20;
        if (!get_appState().get_balanceAdminLoading_slms8w_k$() && get_appState().get_balanceAdminUsers_ico6ks_k$().isEmpty_y1axqb_k$()) {
          updateState(attachListeners$lambda$lambda_48);
          var tmp_21 = window;
          var tid_5 = tmp_21.setTimeout(attachListeners$lambda$lambda_49, 8000);
          getUsers(attachListeners$lambda$lambda_50(tid_5));
          tmp_20 = Unit_getInstance();
        }
        tmp_4 = tmp_20;
      }
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda_16(it) {
    _init_properties_Main_kt__xi25uv();
    signOut();
    return Unit_getInstance();
  }
  function attachListeners$lambda_17(it) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda_51);
    unsubscribeChat();
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_51($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_selectedChatContactId_74xer0_k$(null);
    $this$updateState.set_chatMessages_b9arcw_k$(emptyList());
    return Unit_getInstance();
  }
  function attachListeners$lambda_18($chatInput, $uid) {
    return function (it) {
      sendChatMessage($chatInput, $uid);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda_19($chatInput, $uid) {
    return function (e) {
      var tmp;
      if ((e == null ? null : e.key) == 'Enter') {
        sendChatMessage($chatInput, $uid);
        tmp = Unit_getInstance();
      }
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda_29($err) {
    return function ($this$updateState) {
      $this$updateState.set_networkError_6qqylc_k$($err);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda$lambda$lambda_1($wins, $sess) {
    return function ($this$updateState) {
      $this$updateState.set_recordingOpenWindows_boom0c_k$($wins);
      $this$updateState.set_recordingSessions_nfqq7s_k$($sess);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda$lambda_9($wins) {
    return function (sess) {
      updateState(attachListeners$lambda$lambda$lambda$lambda$lambda_1($wins, sess));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda_30($usr) {
    return function (wins) {
      var tmp = $usr.get_id_kntnx8_k$();
      getSessionsForInstructor(tmp, attachListeners$lambda$lambda$lambda$lambda_9(wins));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_52($usr) {
    return function (_unused_var__etf5q3, err) {
      var tmp;
      if (!(err == null)) {
        updateState(attachListeners$lambda$lambda$lambda_29(err));
        tmp = Unit_getInstance();
      } else {
        var tmp_0 = $usr.get_id_kntnx8_k$();
        getOpenWindowsForInstructor(tmp_0, attachListeners$lambda$lambda$lambda_30($usr));
        tmp = Unit_getInstance();
      }
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda_20($usr) {
    return function (it) {
      var tmp = document.getElementById('sd-new-window-dt');
      var input = tmp instanceof HTMLInputElement ? tmp : null;
      var tmp1_elvis_lhs = input == null ? null : input.value;
      var v = tmp1_elvis_lhs == null ? '' : tmp1_elvis_lhs;
      var tmp_0;
      if (isBlank(v)) {
        return Unit_getInstance();
      }
      // Inline function 'kotlin.js.unsafeCast' call
      var dateFn = function (s) {
        return (new Date(s)).getTime();
      };
      var ms = numberToLong(dateFn(v));
      var tmp_1;
      if (ms.compareTo_9jj042_k$(new Long(0, 0)) <= 0) {
        return Unit_getInstance();
      }
      var tmp_2 = $usr.get_id_kntnx8_k$();
      addOpenWindow(tmp_2, ms, attachListeners$lambda$lambda_52($usr));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda_31($err) {
    return function ($this$updateState) {
      $this$updateState.set_networkError_6qqylc_k$($err);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda_32(wins) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda$lambda$lambda_10(wins));
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda$lambda_10($wins) {
    return function ($this$updateState) {
      $this$updateState.set_recordingOpenWindows_boom0c_k$($wins);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_53($usr) {
    return function (err) {
      var tmp;
      if (!(err == null)) {
        updateState(attachListeners$lambda$lambda$lambda_31(err));
        tmp = Unit_getInstance();
      } else {
        var tmp_0 = $usr.get_id_kntnx8_k$();
        getOpenWindowsForInstructor(tmp_0, attachListeners$lambda$lambda$lambda_32);
        tmp = Unit_getInstance();
      }
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda_21($btn, $usr) {
    return function (it) {
      var tmp0_elvis_lhs = $btn.getAttribute('data-window-id');
      var tmp;
      if (tmp0_elvis_lhs == null) {
        return Unit_getInstance();
      } else {
        tmp = tmp0_elvis_lhs;
      }
      var wid = tmp;
      deleteOpenWindow(wid, attachListeners$lambda$lambda_53($usr));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda_33($err) {
    return function ($this$updateState) {
      $this$updateState.set_networkError_6qqylc_k$($err);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda$lambda$lambda_2($wins, $sess) {
    return function ($this$updateState) {
      $this$updateState.set_recordingOpenWindows_boom0c_k$($wins);
      $this$updateState.set_recordingSessions_nfqq7s_k$($sess);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda$lambda_11($wins) {
    return function (sess) {
      updateState(attachListeners$lambda$lambda$lambda$lambda$lambda_2($wins, sess));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda_34($usr) {
    return function (wins) {
      var tmp = $usr.get_id_kntnx8_k$();
      getSessionsForCadet(tmp, attachListeners$lambda$lambda$lambda$lambda_11(wins));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_54($usr) {
    return function (err) {
      var tmp;
      if (!(err == null)) {
        updateState(attachListeners$lambda$lambda$lambda_33(err));
        tmp = Unit_getInstance();
      } else {
        var tmp0_elvis_lhs = $usr.get_assignedInstructorId_laxw6p_k$();
        var instId = tmp0_elvis_lhs == null ? '' : tmp0_elvis_lhs;
        getOpenWindowsForCadet(instId, attachListeners$lambda$lambda$lambda_34($usr));
        tmp = Unit_getInstance();
      }
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda_22($wid, $usr) {
    return function (it) {
      var tmp = $usr.get_id_kntnx8_k$();
      bookWindow($wid, tmp, attachListeners$lambda$lambda_54($usr));
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda_23(it) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda_55);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_55($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_balanceAdminSelectedUserId_tuhz6u_k$(null);
    return Unit_getInstance();
  }
  function attachListeners$lambda_24($usr) {
    return function (it) {
      attachListeners$_anonymous_$doBalanceOp_vagh69($usr, 'credit');
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda_25($usr) {
    return function (it) {
      attachListeners$_anonymous_$doBalanceOp_vagh69($usr, 'debit');
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda_26($usr) {
    return function (it) {
      attachListeners$_anonymous_$doBalanceOp_vagh69($usr, 'set');
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda_56(err) {
    _init_properties_Main_kt__xi25uv();
    if (!(err == null)) {
      updateState(attachListeners$lambda$lambda$lambda_35(err));
    } else {
      getCurrentUser(attachListeners$lambda$lambda$lambda_36);
    }
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_35($err) {
    return function ($this$updateState) {
      $this$updateState.set_networkError_6qqylc_k$($err);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda$lambda$lambda_36(newUser, _unused_var__etf5q3) {
    _init_properties_Main_kt__xi25uv();
    if (!(newUser == null)) {
      updateState(attachListeners$lambda$lambda$lambda$lambda_12(newUser));
    }
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda$lambda_12($newUser) {
    return function ($this$updateState) {
      $this$updateState.set_user_gqmzbm_k$($newUser);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda_27($usr) {
    return function (it) {
      var tmp = document.getElementById('sd-settings-fullName');
      var tmp0_safe_receiver = tmp instanceof HTMLInputElement ? tmp : null;
      var tmp1_elvis_lhs = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.value;
      // Inline function 'kotlin.text.trim' call
      var this_0 = tmp1_elvis_lhs == null ? '' : tmp1_elvis_lhs;
      var fullName = toString_0(trim(isCharSequence(this_0) ? this_0 : THROW_CCE()));
      var tmp_0 = document.getElementById('sd-settings-phone');
      var tmp2_safe_receiver = tmp_0 instanceof HTMLInputElement ? tmp_0 : null;
      var tmp3_elvis_lhs = tmp2_safe_receiver == null ? null : tmp2_safe_receiver.value;
      // Inline function 'kotlin.text.trim' call
      var this_1 = tmp3_elvis_lhs == null ? '' : tmp3_elvis_lhs;
      var phone = toString_0(trim(isCharSequence(this_1) ? this_1 : THROW_CCE()));
      var tmp_1 = $usr.get_id_kntnx8_k$();
      updateProfile(tmp_1, fullName, phone, attachListeners$lambda$lambda_56);
      return Unit_getInstance();
    };
  }
  function attachListeners$lambda_28(it) {
    _init_properties_Main_kt__xi25uv();
    var tmp = document.getElementById('sd-settings-newpassword');
    var tmp0_safe_receiver = tmp instanceof HTMLInputElement ? tmp : null;
    var tmp1_elvis_lhs = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.value;
    var newPass = tmp1_elvis_lhs == null ? '' : tmp1_elvis_lhs;
    if (newPass.length < 6) {
      updateState(attachListeners$lambda$lambda_57);
      return Unit_getInstance();
    }
    var tmp_0 = changePassword(newPass);
    var tmp_1 = tmp_0.then(attachListeners$lambda$lambda_58);
    tmp_1.catch(attachListeners$lambda$lambda_59);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_57($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_networkError_6qqylc_k$('\u041F\u0430\u0440\u043E\u043B\u044C \u043D\u0435 \u043C\u0435\u043D\u0435\u0435 6 \u0441\u0438\u043C\u0432\u043E\u043B\u043E\u0432');
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_58(it) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda$lambda_37);
    var tmp = document.getElementById('sd-settings-newpassword');
    var tmp0_safe_receiver = tmp instanceof HTMLInputElement ? tmp : null;
    if (tmp0_safe_receiver == null)
      null;
    else {
      tmp0_safe_receiver.value = '';
    }
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_37($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_networkError_6qqylc_k$(null);
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda_59(e) {
    _init_properties_Main_kt__xi25uv();
    updateState(attachListeners$lambda$lambda$lambda_38(e));
    return Unit_getInstance();
  }
  function attachListeners$lambda$lambda$lambda_38($e) {
    return function ($this$updateState) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = $e.message;
      var tmp0_elvis_lhs = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $this$updateState.set_networkError_6qqylc_k$(tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430 \u0441\u043C\u0435\u043D\u044B \u043F\u0430\u0440\u043E\u043B\u044F' : tmp0_elvis_lhs);
      return Unit_getInstance();
    };
  }
  function sendChatMessage$lambda($chatInput) {
    return function (it) {
      var tmp0_safe_receiver = $chatInput;
      if (tmp0_safe_receiver == null)
        null;
      else {
        tmp0_safe_receiver.value = '';
      }
      return Unit_getInstance();
    };
  }
  function sendChatMessage$lambda_0(_unused_var__etf5q3) {
    _init_properties_Main_kt__xi25uv();
    updateState(sendChatMessage$lambda$lambda);
    return Unit_getInstance();
  }
  function sendChatMessage$lambda$lambda($this$updateState) {
    _init_properties_Main_kt__xi25uv();
    $this$updateState.set_networkError_6qqylc_k$('\u041D\u0435 \u0443\u0434\u0430\u043B\u043E\u0441\u044C \u043E\u0442\u043F\u0440\u0430\u0432\u0438\u0442\u044C \u0441\u043E\u043E\u0431\u0449\u0435\u043D\u0438\u0435.');
    return Unit_getInstance();
  }
  function attachListeners$_anonymous_$doBalanceOp$lambda_6obd22(err) {
    _init_properties_Main_kt__xi25uv();
    if (!(err == null)) {
      updateState(attachListeners$_anonymous_$doBalanceOp$lambda$lambda_w7h4nl(err));
    } else {
      getUsers(attachListeners$_anonymous_$doBalanceOp$lambda$lambda_w7h4nl_0);
    }
    return Unit_getInstance();
  }
  function attachListeners$_anonymous_$doBalanceOp$lambda$lambda_w7h4nl($err) {
    return function ($this$updateState) {
      $this$updateState.set_networkError_6qqylc_k$($err);
      return Unit_getInstance();
    };
  }
  function attachListeners$_anonymous_$doBalanceOp$lambda$lambda_w7h4nl_0(list) {
    _init_properties_Main_kt__xi25uv();
    // Inline function 'kotlin.collections.map' call
    // Inline function 'kotlin.collections.mapTo' call
    var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(list, 10));
    var _iterator__ex2g4s = list.iterator_jk1svi_k$();
    while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
      var item = _iterator__ex2g4s.next_20eer_k$();
      var tmp$ret$0 = item.get_id_kntnx8_k$();
      destination.add_utx5q5_k$(tmp$ret$0);
    }
    var tmp = destination;
    loadBalanceHistoryForUsers(tmp, attachListeners$_anonymous_$doBalanceOp$lambda$lambda$lambda_os9kp2(list));
    return Unit_getInstance();
  }
  function attachListeners$_anonymous_$doBalanceOp$lambda$lambda$lambda$lambda_p5yxtd($list, $hist) {
    return function ($this$updateState) {
      $this$updateState.set_balanceAdminUsers_8is91l_k$($list);
      $this$updateState.set_balanceAdminHistory_5kfiug_k$($hist);
      return Unit_getInstance();
    };
  }
  function attachListeners$_anonymous_$doBalanceOp$lambda$lambda$lambda_os9kp2($list) {
    return function (hist) {
      updateState(attachListeners$_anonymous_$doBalanceOp$lambda$lambda$lambda$lambda_p5yxtd($list, hist));
      return Unit_getInstance();
    };
  }
  var properties_initialized_Main_kt_gqj46d;
  function _init_properties_Main_kt__xi25uv() {
    if (!properties_initialized_Main_kt_gqj46d) {
      properties_initialized_Main_kt_gqj46d = true;
      // Inline function 'kotlin.collections.mutableListOf' call
      presenceUnsubscribes = ArrayList_init_$Create$();
      iconPhoneSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/><\/svg>';
      iconChatSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/><\/svg>';
      iconUserPlusSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/><\/svg>';
      iconPowerSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18.36 6.64a9 9 0 1 1-12.73 0"/><line x1="12" y1="2" x2="12" y2="12"/><\/svg>';
      iconTrashSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><\/svg>';
      iconUnlinkSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18.84 12.25l5.72-5.72a2.5 2.5 0 0 0-3.54-3.54l-5.72 5.72"/><path d="M5.16 11.75l-5.72 5.72a2.5 2.5 0 0 0 3.54 3.54l5.72-5.72"/><line x1="8" y1="16" x2="16" y2="8"/><\/svg>';
      iconUserSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/><\/svg>';
      iconPhoneLabelSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/><\/svg>';
      iconEmailLabelSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/><\/svg>';
      iconTicketSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2Z"/><path d="M13 5v2"/><path d="M13 17v2"/><path d="M13 11v2"/><\/svg>';
      iconInstructorSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/><\/svg>';
      iconSelectSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/><\/svg>';
      iconCreditSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/><\/svg>';
      iconDebitSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"/><\/svg>';
      iconSetSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="9" x2="19" y2="9"/><line x1="5" y1="15" x2="19" y2="15"/><\/svg>';
      iconResetSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/><\/svg>';
    }
  }
  function mainWrapper() {
    main();
  }
  function set_currentUnsubscribe(_set____db54di) {
    currentUnsubscribe = _set____db54di;
  }
  function get_currentUnsubscribe() {
    return currentUnsubscribe;
  }
  var currentUnsubscribe;
  function ChatMessage(id, senderId, text, timestamp, status) {
    id = id === VOID ? '' : id;
    senderId = senderId === VOID ? '' : senderId;
    text = text === VOID ? '' : text;
    timestamp = timestamp === VOID ? new Long(0, 0) : timestamp;
    status = status === VOID ? 'sent' : status;
    this.id_1 = id;
    this.senderId_1 = senderId;
    this.text_1 = text;
    this.timestamp_1 = timestamp;
    this.status_1 = status;
  }
  protoOf(ChatMessage).get_id_kntnx8_k$ = function () {
    return this.id_1;
  };
  protoOf(ChatMessage).get_senderId_b5xjpj_k$ = function () {
    return this.senderId_1;
  };
  protoOf(ChatMessage).get_text_wouvsm_k$ = function () {
    return this.text_1;
  };
  protoOf(ChatMessage).get_timestamp_9fccx9_k$ = function () {
    return this.timestamp_1;
  };
  protoOf(ChatMessage).get_status_jnf6d7_k$ = function () {
    return this.status_1;
  };
  protoOf(ChatMessage).component1_7eebsc_k$ = function () {
    return this.id_1;
  };
  protoOf(ChatMessage).component2_7eebsb_k$ = function () {
    return this.senderId_1;
  };
  protoOf(ChatMessage).component3_7eebsa_k$ = function () {
    return this.text_1;
  };
  protoOf(ChatMessage).component4_7eebs9_k$ = function () {
    return this.timestamp_1;
  };
  protoOf(ChatMessage).component5_7eebs8_k$ = function () {
    return this.status_1;
  };
  protoOf(ChatMessage).copy_jhptxr_k$ = function (id, senderId, text, timestamp, status) {
    return new ChatMessage(id, senderId, text, timestamp, status);
  };
  protoOf(ChatMessage).copy$default_gt5qwd_k$ = function (id, senderId, text, timestamp, status, $super) {
    id = id === VOID ? this.id_1 : id;
    senderId = senderId === VOID ? this.senderId_1 : senderId;
    text = text === VOID ? this.text_1 : text;
    timestamp = timestamp === VOID ? this.timestamp_1 : timestamp;
    status = status === VOID ? this.status_1 : status;
    return $super === VOID ? this.copy_jhptxr_k$(id, senderId, text, timestamp, status) : $super.copy_jhptxr_k$.call(this, id, senderId, text, timestamp, status);
  };
  protoOf(ChatMessage).toString = function () {
    return 'ChatMessage(id=' + this.id_1 + ', senderId=' + this.senderId_1 + ', text=' + this.text_1 + ', timestamp=' + this.timestamp_1.toString() + ', status=' + this.status_1 + ')';
  };
  protoOf(ChatMessage).hashCode = function () {
    var result = getStringHashCode(this.id_1);
    result = imul(result, 31) + getStringHashCode(this.senderId_1) | 0;
    result = imul(result, 31) + getStringHashCode(this.text_1) | 0;
    result = imul(result, 31) + this.timestamp_1.hashCode() | 0;
    result = imul(result, 31) + getStringHashCode(this.status_1) | 0;
    return result;
  };
  protoOf(ChatMessage).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof ChatMessage))
      return false;
    var tmp0_other_with_cast = other instanceof ChatMessage ? other : THROW_CCE();
    if (!(this.id_1 === tmp0_other_with_cast.id_1))
      return false;
    if (!(this.senderId_1 === tmp0_other_with_cast.senderId_1))
      return false;
    if (!(this.text_1 === tmp0_other_with_cast.text_1))
      return false;
    if (!this.timestamp_1.equals(tmp0_other_with_cast.timestamp_1))
      return false;
    if (!(this.status_1 === tmp0_other_with_cast.status_1))
      return false;
    return true;
  };
  function chatRoomId(id1, id2) {
    var sorted_0 = sorted(listOf([id1, id2]));
    return sorted_0.get_c1px32_k$(0) + '_' + sorted_0.get_c1px32_k$(1);
  }
  function subscribeMessages(roomId, callback) {
    var tmp0_safe_receiver = currentUnsubscribe;
    if (tmp0_safe_receiver == null)
      null;
    else
      tmp0_safe_receiver();
    var db = getDatabase();
    if (db == null) {
      callback(emptyList());
      return Unit_getInstance();
    }
    var ref = db.ref('chats/' + roomId + '/messages').orderByChild('timestamp');
    var listener = subscribeMessages$lambda(callback);
    ref.on('value', listener);
    currentUnsubscribe = subscribeMessages$lambda_0(ref, listener);
  }
  function unsubscribeChat() {
    var tmp0_safe_receiver = currentUnsubscribe;
    if (tmp0_safe_receiver == null)
      null;
    else
      tmp0_safe_receiver();
    currentUnsubscribe = null;
  }
  function sendMessage(roomId, senderId, text) {
    var tmp0_elvis_lhs = getDatabase();
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return Promise.reject(Error('Database not initialized'));
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var db = tmp;
    var ref = db.ref('chats/' + roomId + '/messages').push();
    var serverTimestamp = getDatabaseServerTimestamp();
    var data = json([to('senderId', senderId), to('text', text), to('timestamp', serverTimestamp), to('status', 'sent')]);
    return ref.set(data).then(sendMessage$lambda);
  }
  function sam$kotlin_Comparator$0_3(function_0) {
    this.function_1 = function_0;
  }
  protoOf(sam$kotlin_Comparator$0_3).compare_bczr_k$ = function (a, b) {
    return this.function_1(a, b);
  };
  protoOf(sam$kotlin_Comparator$0_3).compare = function (a, b) {
    return this.compare_bczr_k$(a, b);
  };
  protoOf(sam$kotlin_Comparator$0_3).getFunctionDelegate_jtodtf_k$ = function () {
    return this.function_1;
  };
  protoOf(sam$kotlin_Comparator$0_3).equals = function (other) {
    var tmp;
    if (!(other == null) ? isInterface(other, Comparator) : false) {
      var tmp_0;
      if (!(other == null) ? isInterface(other, FunctionAdapter) : false) {
        tmp_0 = equals(this.getFunctionDelegate_jtodtf_k$(), other.getFunctionDelegate_jtodtf_k$());
      } else {
        tmp_0 = false;
      }
      tmp = tmp_0;
    } else {
      tmp = false;
    }
    return tmp;
  };
  protoOf(sam$kotlin_Comparator$0_3).hashCode = function () {
    return hashCode(this.getFunctionDelegate_jtodtf_k$());
  };
  function subscribeMessages$lambda$lambda(a, b) {
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.timestamp_1;
    var tmp$ret$1 = b.timestamp_1;
    return compareValues(tmp, tmp$ret$1);
  }
  function subscribeMessages$lambda($callback) {
    return function (snap) {
      // Inline function 'kotlin.collections.mutableListOf' call
      var list = ArrayList_init_$Create$();
      var val_ = snap == null ? null : snap.val();
      var tmp;
      if (val_ != null && val_ != undefined) {
        // Inline function 'kotlin.js.unsafeCast' call
        var obj = val_;
        // Inline function 'kotlin.js.unsafeCast' call
        var keys = Object.keys(obj);
        // Inline function 'kotlin.collections.forEach' call
        var inductionVariable = 0;
        var last = keys.length;
        while (inductionVariable < last) {
          var element = keys[inductionVariable];
          inductionVariable = inductionVariable + 1 | 0;
          var m = obj[element];
          var tmp_0 = m == null ? null : m.timestamp;
          var ts = isNumber(tmp_0) ? tmp_0 : null;
          var tmp_1 = m == null ? null : m.senderId;
          var tmp2_elvis_lhs = (!(tmp_1 == null) ? typeof tmp_1 === 'string' : false) ? tmp_1 : null;
          var tmp_2 = tmp2_elvis_lhs == null ? '' : tmp2_elvis_lhs;
          var tmp_3 = m == null ? null : m.text;
          var tmp4_elvis_lhs = (!(tmp_3 == null) ? typeof tmp_3 === 'string' : false) ? tmp_3 : null;
          var tmp_4 = tmp4_elvis_lhs == null ? '' : tmp4_elvis_lhs;
          var tmp6_elvis_lhs = ts == null ? null : numberToLong(ts);
          var tmp_5 = tmp6_elvis_lhs == null ? new Long(0, 0) : tmp6_elvis_lhs;
          var tmp_6 = m == null ? null : m.status;
          var tmp8_elvis_lhs = (!(tmp_6 == null) ? typeof tmp_6 === 'string' : false) ? tmp_6 : null;
          list.add_utx5q5_k$(new ChatMessage(element, tmp_2, tmp_4, tmp_5, tmp8_elvis_lhs == null ? 'sent' : tmp8_elvis_lhs));
        }
        tmp = Unit_getInstance();
      }
      // Inline function 'kotlin.collections.sortBy' call
      if (list.get_size_woubt6_k$() > 1) {
        // Inline function 'kotlin.comparisons.compareBy' call
        var tmp_7 = subscribeMessages$lambda$lambda;
        var tmp$ret$5 = new sam$kotlin_Comparator$0_3(tmp_7);
        sortWith(list, tmp$ret$5);
      }
      $callback(list);
      return Unit_getInstance();
    };
  }
  function subscribeMessages$lambda_0($ref, $listener) {
    return function () {
      $ref.off('value', $listener);
      currentUnsubscribe = null;
      return Unit_getInstance();
    };
  }
  function sendMessage$lambda() {
    return undefined;
  }
  function DrivingSession(id, instructorId, cadetId, startTimeMillis, status, instructorRating, cadetRating, openWindowId, instructorConfirmed) {
    id = id === VOID ? '' : id;
    instructorId = instructorId === VOID ? '' : instructorId;
    cadetId = cadetId === VOID ? '' : cadetId;
    startTimeMillis = startTimeMillis === VOID ? null : startTimeMillis;
    status = status === VOID ? '' : status;
    instructorRating = instructorRating === VOID ? 0 : instructorRating;
    cadetRating = cadetRating === VOID ? 0 : cadetRating;
    openWindowId = openWindowId === VOID ? '' : openWindowId;
    instructorConfirmed = instructorConfirmed === VOID ? false : instructorConfirmed;
    this.id_1 = id;
    this.instructorId_1 = instructorId;
    this.cadetId_1 = cadetId;
    this.startTimeMillis_1 = startTimeMillis;
    this.status_1 = status;
    this.instructorRating_1 = instructorRating;
    this.cadetRating_1 = cadetRating;
    this.openWindowId_1 = openWindowId;
    this.instructorConfirmed_1 = instructorConfirmed;
  }
  protoOf(DrivingSession).get_id_kntnx8_k$ = function () {
    return this.id_1;
  };
  protoOf(DrivingSession).get_instructorId_h2ldcx_k$ = function () {
    return this.instructorId_1;
  };
  protoOf(DrivingSession).get_cadetId_a7olqf_k$ = function () {
    return this.cadetId_1;
  };
  protoOf(DrivingSession).get_startTimeMillis_a8mb9o_k$ = function () {
    return this.startTimeMillis_1;
  };
  protoOf(DrivingSession).get_status_jnf6d7_k$ = function () {
    return this.status_1;
  };
  protoOf(DrivingSession).get_instructorRating_lprecj_k$ = function () {
    return this.instructorRating_1;
  };
  protoOf(DrivingSession).get_cadetRating_fw5t2v_k$ = function () {
    return this.cadetRating_1;
  };
  protoOf(DrivingSession).get_openWindowId_r8z07y_k$ = function () {
    return this.openWindowId_1;
  };
  protoOf(DrivingSession).get_instructorConfirmed_vjgtll_k$ = function () {
    return this.instructorConfirmed_1;
  };
  protoOf(DrivingSession).component1_7eebsc_k$ = function () {
    return this.id_1;
  };
  protoOf(DrivingSession).component2_7eebsb_k$ = function () {
    return this.instructorId_1;
  };
  protoOf(DrivingSession).component3_7eebsa_k$ = function () {
    return this.cadetId_1;
  };
  protoOf(DrivingSession).component4_7eebs9_k$ = function () {
    return this.startTimeMillis_1;
  };
  protoOf(DrivingSession).component5_7eebs8_k$ = function () {
    return this.status_1;
  };
  protoOf(DrivingSession).component6_7eebs7_k$ = function () {
    return this.instructorRating_1;
  };
  protoOf(DrivingSession).component7_7eebs6_k$ = function () {
    return this.cadetRating_1;
  };
  protoOf(DrivingSession).component8_7eebs5_k$ = function () {
    return this.openWindowId_1;
  };
  protoOf(DrivingSession).component9_7eebs4_k$ = function () {
    return this.instructorConfirmed_1;
  };
  protoOf(DrivingSession).copy_vz4rsr_k$ = function (id, instructorId, cadetId, startTimeMillis, status, instructorRating, cadetRating, openWindowId, instructorConfirmed) {
    return new DrivingSession(id, instructorId, cadetId, startTimeMillis, status, instructorRating, cadetRating, openWindowId, instructorConfirmed);
  };
  protoOf(DrivingSession).copy$default_lkaabr_k$ = function (id, instructorId, cadetId, startTimeMillis, status, instructorRating, cadetRating, openWindowId, instructorConfirmed, $super) {
    id = id === VOID ? this.id_1 : id;
    instructorId = instructorId === VOID ? this.instructorId_1 : instructorId;
    cadetId = cadetId === VOID ? this.cadetId_1 : cadetId;
    startTimeMillis = startTimeMillis === VOID ? this.startTimeMillis_1 : startTimeMillis;
    status = status === VOID ? this.status_1 : status;
    instructorRating = instructorRating === VOID ? this.instructorRating_1 : instructorRating;
    cadetRating = cadetRating === VOID ? this.cadetRating_1 : cadetRating;
    openWindowId = openWindowId === VOID ? this.openWindowId_1 : openWindowId;
    instructorConfirmed = instructorConfirmed === VOID ? this.instructorConfirmed_1 : instructorConfirmed;
    return $super === VOID ? this.copy_vz4rsr_k$(id, instructorId, cadetId, startTimeMillis, status, instructorRating, cadetRating, openWindowId, instructorConfirmed) : $super.copy_vz4rsr_k$.call(this, id, instructorId, cadetId, startTimeMillis, status, instructorRating, cadetRating, openWindowId, instructorConfirmed);
  };
  protoOf(DrivingSession).toString = function () {
    return 'DrivingSession(id=' + this.id_1 + ', instructorId=' + this.instructorId_1 + ', cadetId=' + this.cadetId_1 + ', startTimeMillis=' + toString(this.startTimeMillis_1) + ', status=' + this.status_1 + ', instructorRating=' + this.instructorRating_1 + ', cadetRating=' + this.cadetRating_1 + ', openWindowId=' + this.openWindowId_1 + ', instructorConfirmed=' + this.instructorConfirmed_1 + ')';
  };
  protoOf(DrivingSession).hashCode = function () {
    var result = getStringHashCode(this.id_1);
    result = imul(result, 31) + getStringHashCode(this.instructorId_1) | 0;
    result = imul(result, 31) + getStringHashCode(this.cadetId_1) | 0;
    result = imul(result, 31) + (this.startTimeMillis_1 == null ? 0 : this.startTimeMillis_1.hashCode()) | 0;
    result = imul(result, 31) + getStringHashCode(this.status_1) | 0;
    result = imul(result, 31) + this.instructorRating_1 | 0;
    result = imul(result, 31) + this.cadetRating_1 | 0;
    result = imul(result, 31) + getStringHashCode(this.openWindowId_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.instructorConfirmed_1) | 0;
    return result;
  };
  protoOf(DrivingSession).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof DrivingSession))
      return false;
    var tmp0_other_with_cast = other instanceof DrivingSession ? other : THROW_CCE();
    if (!(this.id_1 === tmp0_other_with_cast.id_1))
      return false;
    if (!(this.instructorId_1 === tmp0_other_with_cast.instructorId_1))
      return false;
    if (!(this.cadetId_1 === tmp0_other_with_cast.cadetId_1))
      return false;
    if (!equals(this.startTimeMillis_1, tmp0_other_with_cast.startTimeMillis_1))
      return false;
    if (!(this.status_1 === tmp0_other_with_cast.status_1))
      return false;
    if (!(this.instructorRating_1 === tmp0_other_with_cast.instructorRating_1))
      return false;
    if (!(this.cadetRating_1 === tmp0_other_with_cast.cadetRating_1))
      return false;
    if (!(this.openWindowId_1 === tmp0_other_with_cast.openWindowId_1))
      return false;
    if (!(this.instructorConfirmed_1 === tmp0_other_with_cast.instructorConfirmed_1))
      return false;
    return true;
  };
  function InstructorOpenWindow(id, instructorId, cadetId, dateTimeMillis, status) {
    id = id === VOID ? '' : id;
    instructorId = instructorId === VOID ? '' : instructorId;
    cadetId = cadetId === VOID ? null : cadetId;
    dateTimeMillis = dateTimeMillis === VOID ? null : dateTimeMillis;
    status = status === VOID ? '' : status;
    this.id_1 = id;
    this.instructorId_1 = instructorId;
    this.cadetId_1 = cadetId;
    this.dateTimeMillis_1 = dateTimeMillis;
    this.status_1 = status;
  }
  protoOf(InstructorOpenWindow).get_id_kntnx8_k$ = function () {
    return this.id_1;
  };
  protoOf(InstructorOpenWindow).get_instructorId_h2ldcx_k$ = function () {
    return this.instructorId_1;
  };
  protoOf(InstructorOpenWindow).get_cadetId_a7olqf_k$ = function () {
    return this.cadetId_1;
  };
  protoOf(InstructorOpenWindow).get_dateTimeMillis_6iuuye_k$ = function () {
    return this.dateTimeMillis_1;
  };
  protoOf(InstructorOpenWindow).get_status_jnf6d7_k$ = function () {
    return this.status_1;
  };
  protoOf(InstructorOpenWindow).component1_7eebsc_k$ = function () {
    return this.id_1;
  };
  protoOf(InstructorOpenWindow).component2_7eebsb_k$ = function () {
    return this.instructorId_1;
  };
  protoOf(InstructorOpenWindow).component3_7eebsa_k$ = function () {
    return this.cadetId_1;
  };
  protoOf(InstructorOpenWindow).component4_7eebs9_k$ = function () {
    return this.dateTimeMillis_1;
  };
  protoOf(InstructorOpenWindow).component5_7eebs8_k$ = function () {
    return this.status_1;
  };
  protoOf(InstructorOpenWindow).copy_mghap3_k$ = function (id, instructorId, cadetId, dateTimeMillis, status) {
    return new InstructorOpenWindow(id, instructorId, cadetId, dateTimeMillis, status);
  };
  protoOf(InstructorOpenWindow).copy$default_x6dsuf_k$ = function (id, instructorId, cadetId, dateTimeMillis, status, $super) {
    id = id === VOID ? this.id_1 : id;
    instructorId = instructorId === VOID ? this.instructorId_1 : instructorId;
    cadetId = cadetId === VOID ? this.cadetId_1 : cadetId;
    dateTimeMillis = dateTimeMillis === VOID ? this.dateTimeMillis_1 : dateTimeMillis;
    status = status === VOID ? this.status_1 : status;
    return $super === VOID ? this.copy_mghap3_k$(id, instructorId, cadetId, dateTimeMillis, status) : $super.copy_mghap3_k$.call(this, id, instructorId, cadetId, dateTimeMillis, status);
  };
  protoOf(InstructorOpenWindow).toString = function () {
    return 'InstructorOpenWindow(id=' + this.id_1 + ', instructorId=' + this.instructorId_1 + ', cadetId=' + this.cadetId_1 + ', dateTimeMillis=' + toString(this.dateTimeMillis_1) + ', status=' + this.status_1 + ')';
  };
  protoOf(InstructorOpenWindow).hashCode = function () {
    var result = getStringHashCode(this.id_1);
    result = imul(result, 31) + getStringHashCode(this.instructorId_1) | 0;
    result = imul(result, 31) + (this.cadetId_1 == null ? 0 : getStringHashCode(this.cadetId_1)) | 0;
    result = imul(result, 31) + (this.dateTimeMillis_1 == null ? 0 : this.dateTimeMillis_1.hashCode()) | 0;
    result = imul(result, 31) + getStringHashCode(this.status_1) | 0;
    return result;
  };
  protoOf(InstructorOpenWindow).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof InstructorOpenWindow))
      return false;
    var tmp0_other_with_cast = other instanceof InstructorOpenWindow ? other : THROW_CCE();
    if (!(this.id_1 === tmp0_other_with_cast.id_1))
      return false;
    if (!(this.instructorId_1 === tmp0_other_with_cast.instructorId_1))
      return false;
    if (!(this.cadetId_1 == tmp0_other_with_cast.cadetId_1))
      return false;
    if (!equals(this.dateTimeMillis_1, tmp0_other_with_cast.dateTimeMillis_1))
      return false;
    if (!(this.status_1 === tmp0_other_with_cast.status_1))
      return false;
    return true;
  };
  function BalanceHistoryEntry(id, userId, amount, type, performedBy, timestampMillis) {
    id = id === VOID ? '' : id;
    userId = userId === VOID ? '' : userId;
    amount = amount === VOID ? 0 : amount;
    type = type === VOID ? '' : type;
    performedBy = performedBy === VOID ? '' : performedBy;
    timestampMillis = timestampMillis === VOID ? null : timestampMillis;
    this.id_1 = id;
    this.userId_1 = userId;
    this.amount_1 = amount;
    this.type_1 = type;
    this.performedBy_1 = performedBy;
    this.timestampMillis_1 = timestampMillis;
  }
  protoOf(BalanceHistoryEntry).get_id_kntnx8_k$ = function () {
    return this.id_1;
  };
  protoOf(BalanceHistoryEntry).get_userId_kl13yn_k$ = function () {
    return this.userId_1;
  };
  protoOf(BalanceHistoryEntry).get_amount_b10di9_k$ = function () {
    return this.amount_1;
  };
  protoOf(BalanceHistoryEntry).get_type_wovaf7_k$ = function () {
    return this.type_1;
  };
  protoOf(BalanceHistoryEntry).get_performedBy_al4aia_k$ = function () {
    return this.performedBy_1;
  };
  protoOf(BalanceHistoryEntry).get_timestampMillis_cbfaxf_k$ = function () {
    return this.timestampMillis_1;
  };
  protoOf(BalanceHistoryEntry).component1_7eebsc_k$ = function () {
    return this.id_1;
  };
  protoOf(BalanceHistoryEntry).component2_7eebsb_k$ = function () {
    return this.userId_1;
  };
  protoOf(BalanceHistoryEntry).component3_7eebsa_k$ = function () {
    return this.amount_1;
  };
  protoOf(BalanceHistoryEntry).component4_7eebs9_k$ = function () {
    return this.type_1;
  };
  protoOf(BalanceHistoryEntry).component5_7eebs8_k$ = function () {
    return this.performedBy_1;
  };
  protoOf(BalanceHistoryEntry).component6_7eebs7_k$ = function () {
    return this.timestampMillis_1;
  };
  protoOf(BalanceHistoryEntry).copy_shy5vs_k$ = function (id, userId, amount, type, performedBy, timestampMillis) {
    return new BalanceHistoryEntry(id, userId, amount, type, performedBy, timestampMillis);
  };
  protoOf(BalanceHistoryEntry).copy$default_hi9oxk_k$ = function (id, userId, amount, type, performedBy, timestampMillis, $super) {
    id = id === VOID ? this.id_1 : id;
    userId = userId === VOID ? this.userId_1 : userId;
    amount = amount === VOID ? this.amount_1 : amount;
    type = type === VOID ? this.type_1 : type;
    performedBy = performedBy === VOID ? this.performedBy_1 : performedBy;
    timestampMillis = timestampMillis === VOID ? this.timestampMillis_1 : timestampMillis;
    return $super === VOID ? this.copy_shy5vs_k$(id, userId, amount, type, performedBy, timestampMillis) : $super.copy_shy5vs_k$.call(this, id, userId, amount, type, performedBy, timestampMillis);
  };
  protoOf(BalanceHistoryEntry).toString = function () {
    return 'BalanceHistoryEntry(id=' + this.id_1 + ', userId=' + this.userId_1 + ', amount=' + this.amount_1 + ', type=' + this.type_1 + ', performedBy=' + this.performedBy_1 + ', timestampMillis=' + toString(this.timestampMillis_1) + ')';
  };
  protoOf(BalanceHistoryEntry).hashCode = function () {
    var result = getStringHashCode(this.id_1);
    result = imul(result, 31) + getStringHashCode(this.userId_1) | 0;
    result = imul(result, 31) + this.amount_1 | 0;
    result = imul(result, 31) + getStringHashCode(this.type_1) | 0;
    result = imul(result, 31) + getStringHashCode(this.performedBy_1) | 0;
    result = imul(result, 31) + (this.timestampMillis_1 == null ? 0 : this.timestampMillis_1.hashCode()) | 0;
    return result;
  };
  protoOf(BalanceHistoryEntry).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof BalanceHistoryEntry))
      return false;
    var tmp0_other_with_cast = other instanceof BalanceHistoryEntry ? other : THROW_CCE();
    if (!(this.id_1 === tmp0_other_with_cast.id_1))
      return false;
    if (!(this.userId_1 === tmp0_other_with_cast.userId_1))
      return false;
    if (!(this.amount_1 === tmp0_other_with_cast.amount_1))
      return false;
    if (!(this.type_1 === tmp0_other_with_cast.type_1))
      return false;
    if (!(this.performedBy_1 === tmp0_other_with_cast.performedBy_1))
      return false;
    if (!equals(this.timestampMillis_1, tmp0_other_with_cast.timestampMillis_1))
      return false;
    return true;
  };
  function parseTimestamp(ts) {
    if (ts == null || ts == undefined)
      return null;
    // Inline function 'kotlin.js.unsafeCast' call
    var t = ts;
    var tmp1_safe_receiver = t == null ? null : t.toMillis;
    var tmp;
    if (tmp1_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.js.unsafeCast' call
      tmp = tmp1_safe_receiver;
    }
    var toMillis = tmp;
    var tmp_0;
    if (toMillis != null) {
      var tmp_1 = toMillis.call(t);
      var tmp2_safe_receiver = isNumber(tmp_1) ? tmp_1 : null;
      tmp_0 = tmp2_safe_receiver == null ? null : numberToLong(tmp2_safe_receiver);
    } else {
      tmp_0 = null;
    }
    var tmp3_elvis_lhs = tmp_0;
    var tmp_2;
    if (tmp3_elvis_lhs == null) {
      var tmp_3 = t == null ? null : t.seconds;
      var tmp5_safe_receiver = isNumber(tmp_3) ? tmp_3 : null;
      var tmp6_safe_receiver = tmp5_safe_receiver == null ? null : numberToLong(tmp5_safe_receiver);
      var tmp_4;
      if (tmp6_safe_receiver == null) {
        tmp_4 = null;
      } else {
        // Inline function 'kotlin.let' call
        // Inline function 'kotlin.Long.times' call
        tmp_4 = tmp6_safe_receiver.times_nfzjiw_k$(toLong(1000));
      }
      tmp_2 = tmp_4;
    } else {
      tmp_2 = tmp3_elvis_lhs;
    }
    var tmp7_elvis_lhs = tmp_2;
    var tmp_5;
    if (tmp7_elvis_lhs == null) {
      var tmp8_safe_receiver = isNumber(ts) ? ts : null;
      tmp_5 = tmp8_safe_receiver == null ? null : numberToLong(tmp8_safe_receiver);
    } else {
      tmp_5 = tmp7_elvis_lhs;
    }
    return tmp_5;
  }
  function getSessionsForInstructor(instructorId, callback) {
    getFirestore().collection('driving_sessions').where('instructorId', '==', instructorId).orderBy('startTime', 'asc').get().then(getSessionsForInstructor$lambda(callback)).catch(getSessionsForInstructor$lambda_0(callback));
  }
  function getSessionsForCadet(cadetId, callback) {
    getFirestore().collection('driving_sessions').where('cadetId', '==', cadetId).orderBy('startTime', 'desc').get().then(getSessionsForCadet$lambda(callback)).catch(getSessionsForCadet$lambda_0(callback));
  }
  function getOpenWindowsForInstructor(instructorId, callback) {
    getFirestore().collection('instructor_open_windows').where('instructorId', '==', instructorId).orderBy('dateTime', 'asc').get().then(getOpenWindowsForInstructor$lambda(callback)).catch(getOpenWindowsForInstructor$lambda_0(callback));
  }
  function getOpenWindowsForCadet(instructorId, callback) {
    getFirestore().collection('instructor_open_windows').where('instructorId', '==', instructorId).where('status', '==', 'free').orderBy('dateTime', 'asc').get().then(getOpenWindowsForCadet$lambda(callback)).catch(getOpenWindowsForCadet$lambda_0(callback));
  }
  function addOpenWindow(instructorId, dateTimeMillis, callback) {
    var ts = getFirestoreTimestampFromMillis(dateTimeMillis);
    var ref = getFirestore().collection('instructor_open_windows').doc();
    ref.set(json([to('instructorId', instructorId), to('dateTime', ts), to('status', 'free')])).then(addOpenWindow$lambda(callback, ref)).catch(addOpenWindow$lambda_0(callback));
  }
  function bookWindow(windowId, cadetId, callback) {
    var firestore = getFirestore();
    var windowRef = firestore.collection('instructor_open_windows').doc(windowId);
    windowRef.update(json([to('cadetId', cadetId), to('status', 'booked')])).then(bookWindow$lambda(windowRef)).then(bookWindow$lambda_0(callback, firestore, cadetId, windowId)).then(bookWindow$lambda_1(callback)).catch(bookWindow$lambda_2(callback));
  }
  function deleteOpenWindow(windowId, callback) {
    getFirestore().collection('instructor_open_windows').doc(windowId).delete().then(deleteOpenWindow$lambda(callback)).catch(deleteOpenWindow$lambda_0(callback));
  }
  function getBalanceHistory(userId, callback) {
    getFirestore().collection('users').doc(userId).collection('balance_history').orderBy('timestamp', 'desc').get().then(getBalanceHistory$lambda(callback, userId)).catch(getBalanceHistory$lambda_0(callback));
  }
  function loadBalanceHistoryForUsers(userIds, callback) {
    if (userIds.isEmpty_y1axqb_k$()) {
      callback(emptyList());
      return Unit_getInstance();
    }
    // Inline function 'kotlin.collections.mutableListOf' call
    var results = ArrayList_init_$Create$();
    var pending = {_v: userIds.get_size_woubt6_k$()};
    // Inline function 'kotlin.collections.forEach' call
    var _iterator__ex2g4s = userIds.iterator_jk1svi_k$();
    while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
      var element = _iterator__ex2g4s.next_20eer_k$();
      getBalanceHistory(element, loadBalanceHistoryForUsers$lambda(results, pending, callback));
    }
  }
  function updateBalance(userId, type, amount, performedBy, callback) {
    var firestore = getFirestore();
    var userRef = firestore.collection('users').doc(userId);
    var historyRef = userRef.collection('balance_history').doc();
    userRef.get().then(updateBalance$lambda(type, amount, userRef, historyRef, userId, performedBy)).then(updateBalance$lambda_0(callback)).catch(updateBalance$lambda_1(callback));
  }
  function getSessionsForInstructor$lambda($callback) {
    return function (snap) {
      var tmp;
      try {
        var tmp1_elvis_lhs = snap == null ? null : snap.docs;
        var docs = tmp1_elvis_lhs == null ? [] : tmp1_elvis_lhs;
        var tmp_0 = docs.length;
        var tmp2_elvis_lhs = (!(tmp_0 == null) ? typeof tmp_0 === 'number' : false) ? tmp_0 : null;
        var len = tmp2_elvis_lhs == null ? 0 : tmp2_elvis_lhs;
        // Inline function 'kotlin.collections.map' call
        var this_0 = until(0, len);
        // Inline function 'kotlin.collections.mapTo' call
        var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_0, 10));
        var inductionVariable = this_0.get_first_irdx8n_k$();
        var last = this_0.get_last_wopotb_k$();
        if (inductionVariable <= last)
          do {
            var item = inductionVariable;
            inductionVariable = inductionVariable + 1 | 0;
            var doc = docs[item];
            // Inline function 'kotlin.js.unsafeCast' call
            var d = doc.data();
            var startTime = d == null ? null : d.startTime;
            var tmp_1 = doc.id;
            var tmp_2 = d == null ? null : d.instructorId;
            var tmp2_elvis_lhs_0 = (!(tmp_2 == null) ? typeof tmp_2 === 'string' : false) ? tmp_2 : null;
            var tmp_3 = tmp2_elvis_lhs_0 == null ? '' : tmp2_elvis_lhs_0;
            var tmp_4 = d == null ? null : d.cadetId;
            var tmp4_elvis_lhs = (!(tmp_4 == null) ? typeof tmp_4 === 'string' : false) ? tmp_4 : null;
            var tmp_5 = tmp4_elvis_lhs == null ? '' : tmp4_elvis_lhs;
            var tmp_6 = parseTimestamp(startTime);
            var tmp_7 = d == null ? null : d.status;
            var tmp6_elvis_lhs = (!(tmp_7 == null) ? typeof tmp_7 === 'string' : false) ? tmp_7 : null;
            var tmp_8 = tmp6_elvis_lhs == null ? '' : tmp6_elvis_lhs;
            var tmp_9 = d == null ? null : d.instructorRating;
            var tmp8_safe_receiver = isNumber(tmp_9) ? tmp_9 : null;
            var tmp9_elvis_lhs = tmp8_safe_receiver == null ? null : numberToInt(tmp8_safe_receiver);
            var tmp_10 = tmp9_elvis_lhs == null ? 0 : tmp9_elvis_lhs;
            var tmp_11 = d == null ? null : d.cadetRating;
            var tmp11_safe_receiver = isNumber(tmp_11) ? tmp_11 : null;
            var tmp12_elvis_lhs = tmp11_safe_receiver == null ? null : numberToInt(tmp11_safe_receiver);
            var tmp_12 = tmp12_elvis_lhs == null ? 0 : tmp12_elvis_lhs;
            var tmp_13 = d == null ? null : d.openWindowId;
            var tmp14_elvis_lhs = (!(tmp_13 == null) ? typeof tmp_13 === 'string' : false) ? tmp_13 : null;
            var tmp_14 = tmp14_elvis_lhs == null ? '' : tmp14_elvis_lhs;
            var tmp_15 = d == null ? null : d.instructorConfirmed;
            var tmp16_elvis_lhs = (!(tmp_15 == null) ? typeof tmp_15 === 'boolean' : false) ? tmp_15 : null;
            var tmp$ret$1 = new DrivingSession(tmp_1, tmp_3, tmp_5, tmp_6, tmp_8, tmp_10, tmp_12, tmp_14, tmp16_elvis_lhs == null ? false : tmp16_elvis_lhs);
            destination.add_utx5q5_k$(tmp$ret$1);
          }
           while (!(item === last));
        tmp = $callback(destination);
      } catch ($p) {
        var tmp_16;
        if ($p instanceof Error) {
          var e = $p;
          tmp_16 = $callback(emptyList());
        } else {
          throw $p;
        }
        tmp = tmp_16;
      }
      return Unit_getInstance();
    };
  }
  function getSessionsForInstructor$lambda_0($callback) {
    return function (_unused_var__etf5q3) {
      $callback(emptyList());
      return Unit_getInstance();
    };
  }
  function getSessionsForCadet$lambda($callback) {
    return function (snap) {
      var tmp;
      try {
        var tmp1_elvis_lhs = snap == null ? null : snap.docs;
        var docs = tmp1_elvis_lhs == null ? [] : tmp1_elvis_lhs;
        var tmp_0 = docs.length;
        var tmp2_elvis_lhs = (!(tmp_0 == null) ? typeof tmp_0 === 'number' : false) ? tmp_0 : null;
        var len = tmp2_elvis_lhs == null ? 0 : tmp2_elvis_lhs;
        // Inline function 'kotlin.collections.map' call
        var this_0 = until(0, len);
        // Inline function 'kotlin.collections.mapTo' call
        var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_0, 10));
        var inductionVariable = this_0.get_first_irdx8n_k$();
        var last = this_0.get_last_wopotb_k$();
        if (inductionVariable <= last)
          do {
            var item = inductionVariable;
            inductionVariable = inductionVariable + 1 | 0;
            var doc = docs[item];
            // Inline function 'kotlin.js.unsafeCast' call
            var d = doc.data();
            var startTime = d == null ? null : d.startTime;
            var tmp_1 = doc.id;
            var tmp_2 = d == null ? null : d.instructorId;
            var tmp2_elvis_lhs_0 = (!(tmp_2 == null) ? typeof tmp_2 === 'string' : false) ? tmp_2 : null;
            var tmp_3 = tmp2_elvis_lhs_0 == null ? '' : tmp2_elvis_lhs_0;
            var tmp_4 = d == null ? null : d.cadetId;
            var tmp4_elvis_lhs = (!(tmp_4 == null) ? typeof tmp_4 === 'string' : false) ? tmp_4 : null;
            var tmp_5 = tmp4_elvis_lhs == null ? '' : tmp4_elvis_lhs;
            var tmp_6 = parseTimestamp(startTime);
            var tmp_7 = d == null ? null : d.status;
            var tmp6_elvis_lhs = (!(tmp_7 == null) ? typeof tmp_7 === 'string' : false) ? tmp_7 : null;
            var tmp_8 = tmp6_elvis_lhs == null ? '' : tmp6_elvis_lhs;
            var tmp_9 = d == null ? null : d.instructorRating;
            var tmp8_safe_receiver = isNumber(tmp_9) ? tmp_9 : null;
            var tmp9_elvis_lhs = tmp8_safe_receiver == null ? null : numberToInt(tmp8_safe_receiver);
            var tmp_10 = tmp9_elvis_lhs == null ? 0 : tmp9_elvis_lhs;
            var tmp_11 = d == null ? null : d.cadetRating;
            var tmp11_safe_receiver = isNumber(tmp_11) ? tmp_11 : null;
            var tmp12_elvis_lhs = tmp11_safe_receiver == null ? null : numberToInt(tmp11_safe_receiver);
            var tmp_12 = tmp12_elvis_lhs == null ? 0 : tmp12_elvis_lhs;
            var tmp_13 = d == null ? null : d.openWindowId;
            var tmp14_elvis_lhs = (!(tmp_13 == null) ? typeof tmp_13 === 'string' : false) ? tmp_13 : null;
            var tmp_14 = tmp14_elvis_lhs == null ? '' : tmp14_elvis_lhs;
            var tmp_15 = d == null ? null : d.instructorConfirmed;
            var tmp16_elvis_lhs = (!(tmp_15 == null) ? typeof tmp_15 === 'boolean' : false) ? tmp_15 : null;
            var tmp$ret$1 = new DrivingSession(tmp_1, tmp_3, tmp_5, tmp_6, tmp_8, tmp_10, tmp_12, tmp_14, tmp16_elvis_lhs == null ? false : tmp16_elvis_lhs);
            destination.add_utx5q5_k$(tmp$ret$1);
          }
           while (!(item === last));
        tmp = $callback(destination);
      } catch ($p) {
        var tmp_16;
        if ($p instanceof Error) {
          var e = $p;
          tmp_16 = $callback(emptyList());
        } else {
          throw $p;
        }
        tmp = tmp_16;
      }
      return Unit_getInstance();
    };
  }
  function getSessionsForCadet$lambda_0($callback) {
    return function (_unused_var__etf5q3) {
      $callback(emptyList());
      return Unit_getInstance();
    };
  }
  function getOpenWindowsForInstructor$lambda($callback) {
    return function (snap) {
      var tmp;
      try {
        var tmp1_elvis_lhs = snap == null ? null : snap.docs;
        var docs = tmp1_elvis_lhs == null ? [] : tmp1_elvis_lhs;
        var tmp_0 = docs.length;
        var tmp2_elvis_lhs = (!(tmp_0 == null) ? typeof tmp_0 === 'number' : false) ? tmp_0 : null;
        var len = tmp2_elvis_lhs == null ? 0 : tmp2_elvis_lhs;
        // Inline function 'kotlin.collections.map' call
        var this_0 = until(0, len);
        // Inline function 'kotlin.collections.mapTo' call
        var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_0, 10));
        var inductionVariable = this_0.get_first_irdx8n_k$();
        var last = this_0.get_last_wopotb_k$();
        if (inductionVariable <= last)
          do {
            var item = inductionVariable;
            inductionVariable = inductionVariable + 1 | 0;
            var doc = docs[item];
            // Inline function 'kotlin.js.unsafeCast' call
            var d = doc.data();
            var tmp_1 = doc.id;
            var tmp_2 = d == null ? null : d.instructorId;
            var tmp1_elvis_lhs_0 = (!(tmp_2 == null) ? typeof tmp_2 === 'string' : false) ? tmp_2 : null;
            var tmp_3 = tmp1_elvis_lhs_0 == null ? '' : tmp1_elvis_lhs_0;
            var tmp_4 = d == null ? null : d.cadetId;
            var tmp_5 = (!(tmp_4 == null) ? typeof tmp_4 === 'string' : false) ? tmp_4 : null;
            var tmp_6 = parseTimestamp(d == null ? null : d.dateTime);
            var tmp_7 = d == null ? null : d.status;
            var tmp5_elvis_lhs = (!(tmp_7 == null) ? typeof tmp_7 === 'string' : false) ? tmp_7 : null;
            var tmp$ret$1 = new InstructorOpenWindow(tmp_1, tmp_3, tmp_5, tmp_6, tmp5_elvis_lhs == null ? '' : tmp5_elvis_lhs);
            destination.add_utx5q5_k$(tmp$ret$1);
          }
           while (!(item === last));
        tmp = $callback(destination);
      } catch ($p) {
        var tmp_8;
        if ($p instanceof Error) {
          var e = $p;
          tmp_8 = $callback(emptyList());
        } else {
          throw $p;
        }
        tmp = tmp_8;
      }
      return Unit_getInstance();
    };
  }
  function getOpenWindowsForInstructor$lambda_0($callback) {
    return function (_unused_var__etf5q3) {
      $callback(emptyList());
      return Unit_getInstance();
    };
  }
  function getOpenWindowsForCadet$lambda($callback) {
    return function (snap) {
      var tmp;
      try {
        var tmp1_elvis_lhs = snap == null ? null : snap.docs;
        var docs = tmp1_elvis_lhs == null ? [] : tmp1_elvis_lhs;
        var tmp_0 = docs.length;
        var tmp2_elvis_lhs = (!(tmp_0 == null) ? typeof tmp_0 === 'number' : false) ? tmp_0 : null;
        var len = tmp2_elvis_lhs == null ? 0 : tmp2_elvis_lhs;
        // Inline function 'kotlin.collections.map' call
        var this_0 = until(0, len);
        // Inline function 'kotlin.collections.mapTo' call
        var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_0, 10));
        var inductionVariable = this_0.get_first_irdx8n_k$();
        var last = this_0.get_last_wopotb_k$();
        if (inductionVariable <= last)
          do {
            var item = inductionVariable;
            inductionVariable = inductionVariable + 1 | 0;
            var doc = docs[item];
            // Inline function 'kotlin.js.unsafeCast' call
            var d = doc.data();
            var tmp_1 = doc.id;
            var tmp_2 = d == null ? null : d.instructorId;
            var tmp1_elvis_lhs_0 = (!(tmp_2 == null) ? typeof tmp_2 === 'string' : false) ? tmp_2 : null;
            var tmp_3 = tmp1_elvis_lhs_0 == null ? '' : tmp1_elvis_lhs_0;
            var tmp_4 = d == null ? null : d.cadetId;
            var tmp_5 = (!(tmp_4 == null) ? typeof tmp_4 === 'string' : false) ? tmp_4 : null;
            var tmp_6 = parseTimestamp(d == null ? null : d.dateTime);
            var tmp_7 = d == null ? null : d.status;
            var tmp5_elvis_lhs = (!(tmp_7 == null) ? typeof tmp_7 === 'string' : false) ? tmp_7 : null;
            var tmp$ret$1 = new InstructorOpenWindow(tmp_1, tmp_3, tmp_5, tmp_6, tmp5_elvis_lhs == null ? '' : tmp5_elvis_lhs);
            destination.add_utx5q5_k$(tmp$ret$1);
          }
           while (!(item === last));
        tmp = $callback(destination);
      } catch ($p) {
        var tmp_8;
        if ($p instanceof Error) {
          var e = $p;
          tmp_8 = $callback(emptyList());
        } else {
          throw $p;
        }
        tmp = tmp_8;
      }
      return Unit_getInstance();
    };
  }
  function getOpenWindowsForCadet$lambda_0($callback) {
    return function (_unused_var__etf5q3) {
      $callback(emptyList());
      return Unit_getInstance();
    };
  }
  function addOpenWindow$lambda($callback, $ref) {
    return function () {
      $callback($ref.id, null);
      return Unit_getInstance();
    };
  }
  function addOpenWindow$lambda_0($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.message;
      var tmp0_elvis_lhs = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $callback(null, tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430' : tmp0_elvis_lhs);
      return Unit_getInstance();
    };
  }
  function bookWindow$lambda($windowRef) {
    return function () {
      return $windowRef.get();
    };
  }
  function bookWindow$lambda_0($callback, $firestore, $cadetId, $windowId) {
    return function (windowDoc) {
      var tmp1_safe_receiver = windowDoc == null ? null : windowDoc.data;
      var d = tmp1_safe_receiver == null ? null : tmp1_safe_receiver.call(windowDoc);
      var tmp = d == null ? null : d.instructorId;
      var instructorId = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      var dateTime = d == null ? null : d.dateTime;
      var tmp_0;
      if (instructorId == null || dateTime == null) {
        $callback('\u041D\u0435\u0442 \u0434\u0430\u043D\u043D\u044B\u0445 \u043E\u043A\u043D\u0430');
        return Promise.resolve();
      }
      var sessionsRef = $firestore.collection('driving_sessions').doc();
      return sessionsRef.set(json([to('instructorId', instructorId), to('cadetId', $cadetId), to('startTime', dateTime), to('status', 'scheduled'), to('instructorRating', 0), to('cadetRating', 0), to('instructorConfirmed', false), to('openWindowId', $windowId)]));
    };
  }
  function bookWindow$lambda_1($callback) {
    return function () {
      $callback(null);
      return Unit_getInstance();
    };
  }
  function bookWindow$lambda_2($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.message;
      var tmp0_elvis_lhs = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $callback(tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430 \u0431\u0440\u043E\u043D\u0438\u0440\u043E\u0432\u0430\u043D\u0438\u044F' : tmp0_elvis_lhs);
      return Unit_getInstance();
    };
  }
  function deleteOpenWindow$lambda($callback) {
    return function () {
      $callback(null);
      return Unit_getInstance();
    };
  }
  function deleteOpenWindow$lambda_0($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.message;
      var tmp0_elvis_lhs = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $callback(tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430' : tmp0_elvis_lhs);
      return Unit_getInstance();
    };
  }
  function getBalanceHistory$lambda($callback, $userId) {
    return function (snap) {
      var tmp;
      try {
        var tmp1_elvis_lhs = snap == null ? null : snap.docs;
        var docs = tmp1_elvis_lhs == null ? [] : tmp1_elvis_lhs;
        var tmp_0 = docs.length;
        var tmp2_elvis_lhs = (!(tmp_0 == null) ? typeof tmp_0 === 'number' : false) ? tmp_0 : null;
        var len = tmp2_elvis_lhs == null ? 0 : tmp2_elvis_lhs;
        // Inline function 'kotlin.collections.map' call
        var this_0 = until(0, len);
        // Inline function 'kotlin.collections.mapTo' call
        var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_0, 10));
        var inductionVariable = this_0.get_first_irdx8n_k$();
        var last = this_0.get_last_wopotb_k$();
        if (inductionVariable <= last)
          do {
            var item = inductionVariable;
            inductionVariable = inductionVariable + 1 | 0;
            var doc = docs[item];
            // Inline function 'kotlin.js.unsafeCast' call
            var d = doc.data();
            var tmp_1 = doc.id;
            var tmp_2 = d == null ? null : d.userId;
            var tmp1_elvis_lhs_0 = (!(tmp_2 == null) ? typeof tmp_2 === 'string' : false) ? tmp_2 : null;
            var tmp_3 = tmp1_elvis_lhs_0 == null ? $userId : tmp1_elvis_lhs_0;
            var tmp_4 = d == null ? null : d.amount;
            var tmp3_safe_receiver = isNumber(tmp_4) ? tmp_4 : null;
            var tmp4_elvis_lhs = tmp3_safe_receiver == null ? null : numberToInt(tmp3_safe_receiver);
            var tmp_5 = tmp4_elvis_lhs == null ? 0 : tmp4_elvis_lhs;
            var tmp_6 = d == null ? null : d.type;
            var tmp6_elvis_lhs = (!(tmp_6 == null) ? typeof tmp_6 === 'string' : false) ? tmp_6 : null;
            var tmp_7 = tmp6_elvis_lhs == null ? '' : tmp6_elvis_lhs;
            var tmp_8 = d == null ? null : d.performedBy;
            var tmp8_elvis_lhs = (!(tmp_8 == null) ? typeof tmp_8 === 'string' : false) ? tmp_8 : null;
            var tmp_9 = tmp8_elvis_lhs == null ? '' : tmp8_elvis_lhs;
            var tmp$ret$1 = new BalanceHistoryEntry(tmp_1, tmp_3, tmp_5, tmp_7, tmp_9, parseTimestamp(d == null ? null : d.timestamp));
            destination.add_utx5q5_k$(tmp$ret$1);
          }
           while (!(item === last));
        tmp = $callback(destination);
      } catch ($p) {
        var tmp_10;
        if ($p instanceof Error) {
          var e = $p;
          tmp_10 = $callback(emptyList());
        } else {
          throw $p;
        }
        tmp = tmp_10;
      }
      return Unit_getInstance();
    };
  }
  function getBalanceHistory$lambda_0($callback) {
    return function (_unused_var__etf5q3) {
      $callback(emptyList());
      return Unit_getInstance();
    };
  }
  function sam$kotlin_Comparator$0_4(function_0) {
    this.function_1 = function_0;
  }
  protoOf(sam$kotlin_Comparator$0_4).compare_bczr_k$ = function (a, b) {
    return this.function_1(a, b);
  };
  protoOf(sam$kotlin_Comparator$0_4).compare = function (a, b) {
    return this.compare_bczr_k$(a, b);
  };
  protoOf(sam$kotlin_Comparator$0_4).getFunctionDelegate_jtodtf_k$ = function () {
    return this.function_1;
  };
  protoOf(sam$kotlin_Comparator$0_4).equals = function (other) {
    var tmp;
    if (!(other == null) ? isInterface(other, Comparator) : false) {
      var tmp_0;
      if (!(other == null) ? isInterface(other, FunctionAdapter) : false) {
        tmp_0 = equals(this.getFunctionDelegate_jtodtf_k$(), other.getFunctionDelegate_jtodtf_k$());
      } else {
        tmp_0 = false;
      }
      tmp = tmp_0;
    } else {
      tmp = false;
    }
    return tmp;
  };
  protoOf(sam$kotlin_Comparator$0_4).hashCode = function () {
    return hashCode(this.getFunctionDelegate_jtodtf_k$());
  };
  function loadBalanceHistoryForUsers$lambda$lambda(a, b) {
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp0_elvis_lhs = b.timestampMillis_1;
    var tmp = tmp0_elvis_lhs == null ? new Long(0, 0) : tmp0_elvis_lhs;
    var tmp0_elvis_lhs_0 = a.timestampMillis_1;
    var tmp$ret$1 = tmp0_elvis_lhs_0 == null ? new Long(0, 0) : tmp0_elvis_lhs_0;
    return compareValues(tmp, tmp$ret$1);
  }
  function loadBalanceHistoryForUsers$lambda($results, $pending, $callback) {
    return function (list) {
      $results.addAll_4lagoh_k$(list);
      var _unary__edvuaz = $pending._v;
      $pending._v = _unary__edvuaz - 1 | 0;
      var tmp;
      if ($pending._v === 0) {
        // Inline function 'kotlin.collections.sortedByDescending' call
        var this_0 = $results;
        // Inline function 'kotlin.comparisons.compareByDescending' call
        var tmp_0 = loadBalanceHistoryForUsers$lambda$lambda;
        var tmp$ret$0 = new sam$kotlin_Comparator$0_4(tmp_0);
        var tmp$ret$1 = sortedWith(this_0, tmp$ret$0);
        tmp = $callback(take(tmp$ret$1, 50));
      }
      return Unit_getInstance();
    };
  }
  function updateBalance$lambda($type, $amount, $userRef, $historyRef, $userId, $performedBy) {
    return function (snap) {
      var tmp1_safe_receiver = snap == null ? null : snap.data;
      var d = tmp1_safe_receiver == null ? null : tmp1_safe_receiver.call(snap);
      var tmp = d == null ? null : d.balance;
      var tmp3_safe_receiver = isNumber(tmp) ? tmp : null;
      var tmp4_elvis_lhs = tmp3_safe_receiver == null ? null : numberToInt(tmp3_safe_receiver);
      var current = tmp4_elvis_lhs == null ? 0 : tmp4_elvis_lhs;
      var newBalance;
      switch ($type) {
        case 'credit':
          newBalance = current + $amount | 0;
          break;
        case 'debit':
          newBalance = coerceAtLeast(current - $amount | 0, 0);
          break;
        case 'set':
          newBalance = $amount;
          break;
        default:
          newBalance = current;
          break;
      }
      $userRef.update(json([to('balance', newBalance)]));
      return $historyRef.set(json([to('userId', $userId), to('amount', $amount), to('type', $type), to('performedBy', $performedBy), to('timestamp', getFirestoreTimestampNow())]));
    };
  }
  function updateBalance$lambda_0($callback) {
    return function () {
      $callback(null);
      return Unit_getInstance();
    };
  }
  function updateBalance$lambda_1($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.message;
      var tmp0_elvis_lhs = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $callback(tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430' : tmp0_elvis_lhs);
      return Unit_getInstance();
    };
  }
  function set_authInstance(_set____db54di) {
    _init_properties_Firebase_kt__4razx5();
    authInstance = _set____db54di;
  }
  function get_authInstance() {
    _init_properties_Firebase_kt__4razx5();
    return authInstance;
  }
  var authInstance;
  function set_firestoreInstance(_set____db54di) {
    _init_properties_Firebase_kt__4razx5();
    firestoreInstance = _set____db54di;
  }
  function get_firestoreInstance() {
    _init_properties_Firebase_kt__4razx5();
    return firestoreInstance;
  }
  var firestoreInstance;
  function set_firebaseCompat(_set____db54di) {
    _init_properties_Firebase_kt__4razx5();
    firebaseCompat = _set____db54di;
  }
  function get_firebaseCompat() {
    _init_properties_Firebase_kt__4razx5();
    return firebaseCompat;
  }
  var firebaseCompat;
  function set_databaseInstance(_set____db54di) {
    _init_properties_Firebase_kt__4razx5();
    databaseInstance = _set____db54di;
  }
  function get_databaseInstance() {
    _init_properties_Firebase_kt__4razx5();
    return databaseInstance;
  }
  var databaseInstance;
  function get_newDateFromMillis() {
    _init_properties_Firebase_kt__4razx5();
    return newDateFromMillis;
  }
  var newDateFromMillis;
  function getFirebaseConfig() {
    _init_properties_Firebase_kt__4razx5();
    // Inline function 'kotlin.js.asDynamic' call
    var w = window;
    if (w.__FIREBASE_CONFIG__ != undefined)
      return w.__FIREBASE_CONFIG__;
    return json([to('apiKey', 'YOUR_WEB_API_KEY'), to('authDomain', 'startdrive-573fa.firebaseapp.com'), to('projectId', 'startdrive-573fa'), to('storageBucket', 'startdrive-573fa.firebasestorage.app'), to('messagingSenderId', 'YOUR_SENDER_ID'), to('appId', 'YOUR_APP_ID')]);
  }
  function initFirebase() {
    _init_properties_Firebase_kt__4razx5();
    if (get_authInstance() != null)
      return Unit_getInstance();
    var firebase = require('firebase/compat/app');
    require('firebase/compat/auth');
    require('firebase/compat/firestore');
    require('firebase/compat/database');
    set_firebaseCompat(firebase);
    var config = getFirebaseConfig();
    var app = firebase.initializeApp(config);
    set_authInstance(firebase.auth(app));
    set_firestoreInstance(firebase.firestore(app));
    set_databaseInstance(firebase.database(app));
  }
  function getFirestoreTimestampNow() {
    _init_properties_Firebase_kt__4razx5();
    var tmp0_safe_receiver = get_firebaseCompat();
    var tmp1_safe_receiver = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.firestore;
    var ts = tmp1_safe_receiver == null ? null : tmp1_safe_receiver.Timestamp;
    var tmp3_safe_receiver = ts == null ? null : ts.now;
    var tmp;
    if (tmp3_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.js.unsafeCast' call
      tmp = tmp3_safe_receiver;
    }
    var tmp4_safe_receiver = tmp;
    var tmp5_elvis_lhs = tmp4_safe_receiver == null ? null : tmp4_safe_receiver.call(ts);
    return tmp5_elvis_lhs == null ? new Date() : tmp5_elvis_lhs;
  }
  function getFirestoreTimestampFromMillis(ms) {
    _init_properties_Firebase_kt__4razx5();
    var tmp0_safe_receiver = get_firebaseCompat();
    var tmp1_safe_receiver = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.firestore;
    var ts = tmp1_safe_receiver == null ? null : tmp1_safe_receiver.Timestamp;
    var tmp3_safe_receiver = ts == null ? null : ts.fromMillis;
    var tmp;
    if (tmp3_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.js.unsafeCast' call
      tmp = tmp3_safe_receiver;
    }
    var tmp4_safe_receiver = tmp;
    var tmp5_elvis_lhs = tmp4_safe_receiver == null ? null : tmp4_safe_receiver.call(ts, ms);
    return tmp5_elvis_lhs == null ? get_newDateFromMillis()(ms) : tmp5_elvis_lhs;
  }
  function getAuth() {
    _init_properties_Firebase_kt__4razx5();
    return get_authInstance();
  }
  function getFirestore() {
    _init_properties_Firebase_kt__4razx5();
    return get_firestoreInstance();
  }
  function getDatabase() {
    _init_properties_Firebase_kt__4razx5();
    return get_databaseInstance();
  }
  function getDatabaseServerTimestamp() {
    _init_properties_Firebase_kt__4razx5();
    var tmp0_safe_receiver = get_firebaseCompat();
    var tmp1_safe_receiver = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.database;
    var tmp;
    if (tmp1_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.js.unsafeCast' call
      tmp = tmp1_safe_receiver;
    }
    var tmp2_safe_receiver = tmp;
    var tmp3_safe_receiver = tmp2_safe_receiver == null ? null : tmp2_safe_receiver.ServerValue;
    var tmp4_elvis_lhs = tmp3_safe_receiver == null ? null : tmp3_safe_receiver.TIMESTAMP;
    return tmp4_elvis_lhs == null ? Date.now() : tmp4_elvis_lhs;
  }
  function onAuthStateChanged(callback) {
    _init_properties_Firebase_kt__4razx5();
    getAuth().onAuthStateChanged(onAuthStateChanged$lambda(callback));
  }
  function signIn(email, password) {
    _init_properties_Firebase_kt__4razx5();
    return getAuth().signInWithEmailAndPassword(email, password).then(signIn$lambda);
  }
  function signOut() {
    _init_properties_Firebase_kt__4razx5();
    return getAuth().signOut();
  }
  function register(fullName, email, phone, password, role) {
    _init_properties_Firebase_kt__4razx5();
    var auth = getAuth();
    var firestore = getFirestore();
    return auth.createUserWithEmailAndPassword(email, password).then(register$lambda(role, fullName, email, phone, firestore)).then(register$lambda_0);
  }
  function getCurrentUserId() {
    _init_properties_Firebase_kt__4razx5();
    var tmp0_safe_receiver = getAuth().currentUser;
    var tmp = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.uid;
    return (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
  }
  function updateProfile(uid, fullName, phone, callback) {
    _init_properties_Firebase_kt__4razx5();
    getFirestore().collection('users').doc(uid).update(json([to('fullName', fullName), to('phone', phone)])).then(updateProfile$lambda(callback)).catch(updateProfile$lambda_0(callback));
  }
  function changePassword(newPassword) {
    _init_properties_Firebase_kt__4razx5();
    var tmp0_elvis_lhs = getAuth().currentUser;
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return Promise.reject(Error('Not signed in'));
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var user = tmp;
    return user.updatePassword(newPassword).then(changePassword$lambda);
  }
  function parseUserFromDoc(doc, d) {
    _init_properties_Firebase_kt__4razx5();
    var tmp = doc.id;
    var id = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : THROW_CCE();
    var tmp_0 = d == null ? null : d.fullName;
    var tmp1_elvis_lhs = (!(tmp_0 == null) ? typeof tmp_0 === 'string' : false) ? tmp_0 : null;
    var fullName = tmp1_elvis_lhs == null ? '' : tmp1_elvis_lhs;
    var tmp_1 = d == null ? null : d.email;
    var tmp3_elvis_lhs = (!(tmp_1 == null) ? typeof tmp_1 === 'string' : false) ? tmp_1 : null;
    var email = tmp3_elvis_lhs == null ? '' : tmp3_elvis_lhs;
    var tmp_2 = d == null ? null : d.phone;
    var tmp5_elvis_lhs = (!(tmp_2 == null) ? typeof tmp_2 === 'string' : false) ? tmp_2 : null;
    var phone = tmp5_elvis_lhs == null ? '' : tmp5_elvis_lhs;
    var tmp_3 = d == null ? null : d.role;
    var tmp7_elvis_lhs = (!(tmp_3 == null) ? typeof tmp_3 === 'string' : false) ? tmp_3 : null;
    var role = tmp7_elvis_lhs == null ? '' : tmp7_elvis_lhs;
    var tmp_4 = d == null ? null : d.balance;
    var tmp9_safe_receiver = isNumber(tmp_4) ? tmp_4 : null;
    var tmp10_elvis_lhs = tmp9_safe_receiver == null ? null : numberToInt(tmp9_safe_receiver);
    var balance = tmp10_elvis_lhs == null ? 0 : tmp10_elvis_lhs;
    var tmp_5 = d == null ? null : d.isActive;
    var tmp12_elvis_lhs = (!(tmp_5 == null) ? typeof tmp_5 === 'boolean' : false) ? tmp_5 : null;
    var isActive = tmp12_elvis_lhs == null ? false : tmp12_elvis_lhs;
    var tmp_6 = d == null ? null : d.assignedInstructorId;
    var assignedInstructorId = (!(tmp_6 == null) ? typeof tmp_6 === 'string' : false) ? tmp_6 : null;
    var assignedCadetsRaw = d == null ? null : d.assignedCadets;
    var tmp15_safe_receiver = (!(assignedCadetsRaw == null) ? isArray(assignedCadetsRaw) : false) ? assignedCadetsRaw : null;
    var tmp_7;
    if (tmp15_safe_receiver == null) {
      tmp_7 = null;
    } else {
      // Inline function 'kotlin.collections.mapNotNull' call
      // Inline function 'kotlin.collections.mapNotNullTo' call
      var destination = ArrayList_init_$Create$();
      // Inline function 'kotlin.collections.forEach' call
      var inductionVariable = 0;
      var last = tmp15_safe_receiver.length;
      while (inductionVariable < last) {
        var element = tmp15_safe_receiver[inductionVariable];
        inductionVariable = inductionVariable + 1 | 0;
        var tmp0_safe_receiver = element == null ? null : toString_0(element);
        if (tmp0_safe_receiver == null)
          null;
        else {
          // Inline function 'kotlin.let' call
          destination.add_utx5q5_k$(tmp0_safe_receiver);
        }
      }
      tmp_7 = destination;
    }
    var tmp16_elvis_lhs = tmp_7;
    var assignedCadets = tmp16_elvis_lhs == null ? emptyList() : tmp16_elvis_lhs;
    var tmp_8 = d == null ? null : d.chatAvatarUrl;
    var chatAvatarUrl = (!(tmp_8 == null) ? typeof tmp_8 === 'string' : false) ? tmp_8 : null;
    return new User(id, fullName, email, phone, role, balance, VOID, assignedInstructorId, assignedCadets, isActive, null, chatAvatarUrl);
  }
  function getCurrentUser(callback) {
    _init_properties_Firebase_kt__4razx5();
    var uid = getCurrentUserId();
    if (uid == null) {
      callback(null, null);
      return Unit_getInstance();
    }
    getFirestore().collection('users').doc(uid).get().then(getCurrentUser$lambda(callback)).catch(getCurrentUser$lambda_0(callback));
  }
  function getUsers(callback) {
    _init_properties_Firebase_kt__4razx5();
    getUsersWithError(getUsers$lambda(callback));
  }
  function getUsersWithError(callback) {
    _init_properties_Firebase_kt__4razx5();
    getFirestore().collection('users').get().then(getUsersWithError$lambda(callback)).catch(getUsersWithError$lambda_0(callback));
  }
  function getUserById(userId, callback) {
    _init_properties_Firebase_kt__4razx5();
    getFirestore().collection('users').doc(userId).get().then(getUserById$lambda(callback)).catch(getUserById$lambda_0(callback));
  }
  function setActive(userId, active, callback) {
    _init_properties_Firebase_kt__4razx5();
    getFirestore().collection('users').doc(userId).update('isActive', active).then(setActive$lambda(callback)).catch(setActive$lambda_0(callback));
  }
  function assignCadetToInstructor(instructorId, cadetId, callback) {
    _init_properties_Firebase_kt__4razx5();
    var firestore = getFirestore();
    var instructorRef = firestore.collection('users').doc(instructorId);
    var cadetRef = firestore.collection('users').doc(cadetId);
    firestore.runTransaction(assignCadetToInstructor$lambda(instructorRef, cadetId, cadetRef, instructorId)).then(assignCadetToInstructor$lambda_0(callback)).catch(assignCadetToInstructor$lambda_1(callback));
  }
  function removeCadetFromInstructor(instructorId, cadetId, callback) {
    _init_properties_Firebase_kt__4razx5();
    var firestore = getFirestore();
    var instructorRef = firestore.collection('users').doc(instructorId);
    var cadetRef = firestore.collection('users').doc(cadetId);
    firestore.runTransaction(removeCadetFromInstructor$lambda(instructorRef, cadetId, cadetRef)).then(removeCadetFromInstructor$lambda_0(callback)).catch(removeCadetFromInstructor$lambda_1(callback));
  }
  function deleteUser(userId, callback) {
    _init_properties_Firebase_kt__4razx5();
    getFirestore().collection('users').doc(userId).delete().then(deleteUser$lambda(callback)).catch(deleteUser$lambda_0(callback));
  }
  function subscribePresence(userId, callback) {
    _init_properties_Firebase_kt__4razx5();
    var tmp0_elvis_lhs = getDatabase();
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return subscribePresence$lambda;
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var db = tmp;
    var ref = db.ref('presence/' + userId);
    var handler = subscribePresence$lambda_0(callback);
    ref.on('value', handler);
    return subscribePresence$lambda_1(ref);
  }
  function setPresence(userId, online) {
    _init_properties_Firebase_kt__4razx5();
    var tmp0_elvis_lhs = getDatabase();
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return Unit_getInstance();
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var db = tmp;
    var ref = db.ref('presence/' + userId);
    if (online) {
      ref.set({status: 'online', lastSeen: Date.now()});
      ref.onDisconnect().set({status: 'offline', lastSeen: {'.sv': 'timestamp'}});
    } else {
      ref.set({status: 'offline', lastSeen: Date.now()});
    }
  }
  function getUsersForChat(currentUser, callback) {
    _init_properties_Firebase_kt__4razx5();
    getUsers(getUsersForChat$lambda(currentUser, callback));
  }
  function onAuthStateChanged$lambda($callback) {
    return function (user) {
      var tmp = user == null ? null : user.uid;
      $callback((!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null);
      return Unit_getInstance();
    };
  }
  function signIn$lambda() {
    _init_properties_Firebase_kt__4razx5();
    return undefined;
  }
  function register$lambda($role, $fullName, $email, $phone, $firestore) {
    return function (result) {
      var tmp1_safe_receiver = result == null ? null : result.user;
      var tmp2_elvis_lhs = tmp1_safe_receiver == null ? null : tmp1_safe_receiver.uid;
      var tmp;
      if (tmp2_elvis_lhs == null) {
        throw Error('No user id');
      } else {
        tmp = tmp2_elvis_lhs;
      }
      var uid = tmp;
      var isActive = $role === 'admin';
      var data = json([to('fullName', $fullName), to('email', $email), to('phone', $phone), to('role', $role), to('balance', 0), to('isActive', isActive), to('createdAt', getFirestoreTimestampNow())]);
      return $firestore.collection('users').doc(uid).set(data);
    };
  }
  function register$lambda_0() {
    _init_properties_Firebase_kt__4razx5();
    return undefined;
  }
  function updateProfile$lambda($callback) {
    return function () {
      $callback(null);
      return Unit_getInstance();
    };
  }
  function updateProfile$lambda_0($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.message;
      var tmp0_elvis_lhs = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $callback(tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430' : tmp0_elvis_lhs);
      return Unit_getInstance();
    };
  }
  function changePassword$lambda() {
    _init_properties_Firebase_kt__4razx5();
    return undefined;
  }
  function getCurrentUser$lambda($callback) {
    return function (doc) {
      var exists = doc == null ? null : doc.exists;
      var tmp;
      if (exists != true) {
        $callback(null, '\u041F\u0440\u043E\u0444\u0438\u043B\u044C \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D \u0432 \u0431\u0430\u0437\u0435. \u0412\u043E\u0439\u0434\u0438\u0442\u0435 \u0447\u0435\u0440\u0435\u0437 \u043F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u0435 \u0438\u043B\u0438 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u0443\u0439\u0442\u0435\u0441\u044C \u043D\u0430 \u0441\u0430\u0439\u0442\u0435.');
        return Unit_getInstance();
      }
      // Inline function 'kotlin.js.unsafeCast' call
      var d = doc.data();
      var tmp_0;
      if (d == null) {
        $callback(null, '\u0414\u0430\u043D\u043D\u044B\u0435 \u043F\u0440\u043E\u0444\u0438\u043B\u044F \u043F\u0443\u0441\u0442\u044B.');
        return Unit_getInstance();
      }
      var user = parseUserFromDoc(doc, d);
      $callback(user, null);
      return Unit_getInstance();
    };
  }
  function getCurrentUser$lambda_0($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.code;
      var code = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      var tmp_0;
      if (code === 'permission-denied') {
        tmp_0 = '\u041D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u0430 \u043A \u0431\u0430\u0437\u0435. \u041F\u0440\u043E\u0432\u0435\u0440\u044C\u0442\u0435 \u043F\u0440\u0430\u0432\u0438\u043B\u0430 Firestore \u0434\u043B\u044F \u043A\u043E\u043B\u043B\u0435\u043A\u0446\u0438\u0438 users.';
      } else {
        // Inline function 'kotlin.js.asDynamic' call
        var tmp_1 = e.message;
        var tmp1_elvis_lhs = (!(tmp_1 == null) ? typeof tmp_1 === 'string' : false) ? tmp_1 : null;
        tmp_0 = tmp1_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430 Firestore: ' + e.message : tmp1_elvis_lhs;
      }
      var msg = tmp_0;
      $callback(null, msg);
      return Unit_getInstance();
    };
  }
  function getUsers$lambda($callback) {
    return function (list, _unused_var__etf5q3) {
      $callback(list);
      return Unit_getInstance();
    };
  }
  function getUsersWithError$lambda($callback) {
    return function (snap) {
      var tmp;
      try {
        var tmp1_elvis_lhs = snap == null ? null : snap.docs;
        var rawDocs = tmp1_elvis_lhs == null ? [] : tmp1_elvis_lhs;
        var tmp_0 = rawDocs.length;
        var tmp2_elvis_lhs = (!(tmp_0 == null) ? typeof tmp_0 === 'number' : false) ? tmp_0 : null;
        var len = tmp2_elvis_lhs == null ? 0 : tmp2_elvis_lhs;
        // Inline function 'kotlin.collections.mapNotNull' call
        var tmp0 = until(0, len);
        // Inline function 'kotlin.collections.mapNotNullTo' call
        var destination = ArrayList_init_$Create$();
        // Inline function 'kotlin.collections.forEach' call
        var inductionVariable = tmp0.get_first_irdx8n_k$();
        var last = tmp0.get_last_wopotb_k$();
        if (inductionVariable <= last)
          do {
            var element = inductionVariable;
            inductionVariable = inductionVariable + 1 | 0;
            var tmp0_0 = element;
            var tmp$ret$1;
            $l$block: {
              var doc = rawDocs[tmp0_0];
              // Inline function 'kotlin.js.unsafeCast' call
              var d = doc.data();
              if (d == null) {
                tmp$ret$1 = null;
                break $l$block;
              }
              // Inline function 'kotlin.js.unsafeCast' call
              tmp$ret$1 = parseUserFromDoc(doc, d);
            }
            var tmp0_safe_receiver = tmp$ret$1;
            if (tmp0_safe_receiver == null)
              null;
            else {
              // Inline function 'kotlin.let' call
              destination.add_utx5q5_k$(tmp0_safe_receiver);
            }
          }
           while (!(element === last));
        var list = destination;
        tmp = $callback(list, null);
      } catch ($p) {
        var tmp_1;
        if ($p instanceof Error) {
          var e = $p;
          var tmp_2 = emptyList();
          // Inline function 'kotlin.js.asDynamic' call
          var tmp_3 = e.message;
          var tmp3_elvis_lhs = (!(tmp_3 == null) ? typeof tmp_3 === 'string' : false) ? tmp_3 : null;
          tmp_1 = $callback(tmp_2, tmp3_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430 \u0447\u0442\u0435\u043D\u0438\u044F \u0441\u043F\u0438\u0441\u043A\u0430' : tmp3_elvis_lhs);
        } else {
          throw $p;
        }
        tmp = tmp_1;
      }
      return Unit_getInstance();
    };
  }
  function getUsersWithError$lambda_0($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.code;
      var code = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      var tmp_0;
      switch (code) {
        case 'permission-denied':
          tmp_0 = '\u041D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u0430 \u043A Firestore. \u041F\u0440\u043E\u0432\u0435\u0440\u044C\u0442\u0435 \u043F\u0440\u0430\u0432\u0438\u043B\u0430: \u0447\u0442\u0435\u043D\u0438\u0435 \u043A\u043E\u043B\u043B\u0435\u043A\u0446\u0438\u0438 users \u0434\u043B\u044F \u0430\u0432\u0442\u043E\u0440\u0438\u0437\u043E\u0432\u0430\u043D\u043D\u044B\u0445.';
          break;
        case 'unavailable':
          tmp_0 = 'Firestore \u043D\u0435\u0434\u043E\u0441\u0442\u0443\u043F\u0435\u043D. \u041F\u0440\u043E\u0432\u0435\u0440\u044C\u0442\u0435 \u0438\u043D\u0442\u0435\u0440\u043D\u0435\u0442.';
          break;
        default:
          // Inline function 'kotlin.js.asDynamic' call

          var tmp_1 = e.message;
          var tmp1_elvis_lhs = (!(tmp_1 == null) ? typeof tmp_1 === 'string' : false) ? tmp_1 : null;
          tmp_0 = tmp1_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u0433\u0440\u0443\u0437\u043A\u0438 \u0441\u043F\u0438\u0441\u043A\u0430 \u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u0435\u043B\u0435\u0439' : tmp1_elvis_lhs;
          break;
      }
      var msg = tmp_0;
      $callback(emptyList(), msg);
      return Unit_getInstance();
    };
  }
  function getUserById$lambda($callback) {
    return function (doc) {
      var tmp;
      if ((doc == null ? null : doc.exists) != true) {
        $callback(null);
        return Unit_getInstance();
      }
      // Inline function 'kotlin.js.unsafeCast' call
      var docD = doc;
      var d = docD.data();
      var tmp_0;
      if (d == null) {
        $callback(null);
        return Unit_getInstance();
      }
      $callback(parseUserFromDoc(docD, d));
      return Unit_getInstance();
    };
  }
  function getUserById$lambda_0($callback) {
    return function (_unused_var__etf5q3) {
      $callback(null);
      return Unit_getInstance();
    };
  }
  function setActive$lambda($callback) {
    return function () {
      $callback(null);
      return Unit_getInstance();
    };
  }
  function setActive$lambda_0($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.message;
      var tmp0_elvis_lhs = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $callback(tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430' : tmp0_elvis_lhs);
      return Unit_getInstance();
    };
  }
  function assignCadetToInstructor$lambda$lambda($cadetId, $transaction, $instructorRef, $cadetRef, $instructorId) {
    return function (snap) {
      // Inline function 'kotlin.js.unsafeCast' call
      var data = snap.data();
      var tmp = data == null ? null : data.assignedCadets;
      var tmp1_safe_receiver = (!(tmp == null) ? isArray(tmp) : false) ? tmp : null;
      var tmp2_safe_receiver = tmp1_safe_receiver == null ? null : toList(tmp1_safe_receiver);
      var tmp_0;
      if (tmp2_safe_receiver == null) {
        tmp_0 = null;
      } else {
        // Inline function 'kotlin.collections.mapNotNull' call
        // Inline function 'kotlin.collections.mapNotNullTo' call
        var destination = ArrayList_init_$Create$();
        // Inline function 'kotlin.collections.forEach' call
        var _iterator__ex2g4s = tmp2_safe_receiver.iterator_jk1svi_k$();
        while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
          var element = _iterator__ex2g4s.next_20eer_k$();
          var tmp0_safe_receiver = element == null ? null : toString_0(element);
          if (tmp0_safe_receiver == null)
            null;
          else {
            // Inline function 'kotlin.let' call
            destination.add_utx5q5_k$(tmp0_safe_receiver);
          }
        }
        tmp_0 = destination;
      }
      var tmp3_elvis_lhs = tmp_0;
      var existing = tmp3_elvis_lhs == null ? emptyList() : tmp3_elvis_lhs;
      var list = toMutableList(existing);
      var tmp_1;
      if (!list.contains_aljjnj_k$($cadetId)) {
        list.add_utx5q5_k$($cadetId);
        tmp_1 = Unit_getInstance();
      }
      // Inline function 'kotlin.collections.toTypedArray' call
      var tmp$ret$8 = copyToArray(list);
      $transaction.update($instructorRef, json([to('assignedCadets', tmp$ret$8)]));
      return $transaction.update($cadetRef, json([to('assignedInstructorId', $instructorId)]));
    };
  }
  function assignCadetToInstructor$lambda($instructorRef, $cadetId, $cadetRef, $instructorId) {
    return function (transaction) {
      return transaction.get($instructorRef).then(assignCadetToInstructor$lambda$lambda($cadetId, transaction, $instructorRef, $cadetRef, $instructorId));
    };
  }
  function assignCadetToInstructor$lambda_0($callback) {
    return function () {
      $callback(null);
      return Unit_getInstance();
    };
  }
  function assignCadetToInstructor$lambda_1($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.message;
      var tmp0_elvis_lhs = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $callback(tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430' : tmp0_elvis_lhs);
      return Unit_getInstance();
    };
  }
  function removeCadetFromInstructor$lambda$lambda($cadetId, $transaction, $instructorRef, $cadetRef) {
    return function (snap) {
      // Inline function 'kotlin.js.unsafeCast' call
      var data = snap.data();
      var tmp = data == null ? null : data.assignedCadets;
      var tmp1_safe_receiver = (!(tmp == null) ? isArray(tmp) : false) ? tmp : null;
      var tmp2_safe_receiver = tmp1_safe_receiver == null ? null : toList(tmp1_safe_receiver);
      var tmp_0;
      if (tmp2_safe_receiver == null) {
        tmp_0 = null;
      } else {
        // Inline function 'kotlin.collections.mapNotNull' call
        // Inline function 'kotlin.collections.mapNotNullTo' call
        var destination = ArrayList_init_$Create$();
        // Inline function 'kotlin.collections.forEach' call
        var _iterator__ex2g4s = tmp2_safe_receiver.iterator_jk1svi_k$();
        while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
          var element = _iterator__ex2g4s.next_20eer_k$();
          var tmp0_safe_receiver = element == null ? null : toString_0(element);
          if (tmp0_safe_receiver == null)
            null;
          else {
            // Inline function 'kotlin.let' call
            destination.add_utx5q5_k$(tmp0_safe_receiver);
          }
        }
        tmp_0 = destination;
      }
      var tmp3_elvis_lhs = tmp_0;
      var list = toMutableList(tmp3_elvis_lhs == null ? emptyList() : tmp3_elvis_lhs);
      list.remove_cedx0m_k$($cadetId);
      // Inline function 'kotlin.collections.toTypedArray' call
      var tmp$ret$8 = copyToArray(list);
      $transaction.update($instructorRef, json([to('assignedCadets', tmp$ret$8)]));
      return $transaction.update($cadetRef, json([to('assignedInstructorId', null)]));
    };
  }
  function removeCadetFromInstructor$lambda($instructorRef, $cadetId, $cadetRef) {
    return function (transaction) {
      return transaction.get($instructorRef).then(removeCadetFromInstructor$lambda$lambda($cadetId, transaction, $instructorRef, $cadetRef));
    };
  }
  function removeCadetFromInstructor$lambda_0($callback) {
    return function () {
      $callback(null);
      return Unit_getInstance();
    };
  }
  function removeCadetFromInstructor$lambda_1($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.message;
      var tmp0_elvis_lhs = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $callback(tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430' : tmp0_elvis_lhs);
      return Unit_getInstance();
    };
  }
  function deleteUser$lambda($callback) {
    return function () {
      $callback(null);
      return Unit_getInstance();
    };
  }
  function deleteUser$lambda_0($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.message;
      var tmp0_elvis_lhs = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $callback(tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430' : tmp0_elvis_lhs);
      return Unit_getInstance();
    };
  }
  function subscribePresence$lambda() {
    _init_properties_Firebase_kt__4razx5();
    return Unit_getInstance();
  }
  function subscribePresence$lambda_0($callback) {
    return function (snap) {
      var v = snap == null ? null : snap.val();
      var tmp = v == null ? null : v.status;
      var status = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $callback(status === 'online');
      return Unit_getInstance();
    };
  }
  function subscribePresence$lambda_1($ref) {
    return function () {
      $ref.off('value');
      return Unit_getInstance();
    };
  }
  function getUsersForChat$lambda($currentUser, $callback) {
    return function (all) {
      var tmp;
      switch ($currentUser.get_role_wotsxr_k$()) {
        case 'admin':
          // Inline function 'kotlin.collections.filter' call

          // Inline function 'kotlin.collections.filterTo' call

          var destination = ArrayList_init_$Create$();
          var _iterator__ex2g4s = all.iterator_jk1svi_k$();
          while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
            var element = _iterator__ex2g4s.next_20eer_k$();
            if (!(element.get_role_wotsxr_k$() === 'admin')) {
              destination.add_utx5q5_k$(element);
            }
          }

          tmp = destination;
          break;
        case 'instructor':
          var tmp$ret$4;
          $l$block: {
            // Inline function 'kotlin.collections.firstOrNull' call
            var _iterator__ex2g4s_0 = all.iterator_jk1svi_k$();
            while (_iterator__ex2g4s_0.hasNext_bitz1p_k$()) {
              var element_0 = _iterator__ex2g4s_0.next_20eer_k$();
              if (element_0.get_role_wotsxr_k$() === 'admin') {
                tmp$ret$4 = element_0;
                break $l$block;
              }
            }
            tmp$ret$4 = null;
          }

          var admin = tmp$ret$4;
          // Inline function 'kotlin.collections.mapNotNull' call

          var tmp0 = $currentUser.get_assignedCadets_bue0kr_k$();
          // Inline function 'kotlin.collections.mapNotNullTo' call

          var destination_0 = ArrayList_init_$Create$();
          // Inline function 'kotlin.collections.forEach' call

          var _iterator__ex2g4s_1 = tmp0.iterator_jk1svi_k$();
          while (_iterator__ex2g4s_1.hasNext_bitz1p_k$()) {
            var element_1 = _iterator__ex2g4s_1.next_20eer_k$();
            // Inline function 'kotlin.collections.find' call
            var tmp$ret$6;
            $l$block_0: {
              // Inline function 'kotlin.collections.firstOrNull' call
              var _iterator__ex2g4s_2 = all.iterator_jk1svi_k$();
              while (_iterator__ex2g4s_2.hasNext_bitz1p_k$()) {
                var element_2 = _iterator__ex2g4s_2.next_20eer_k$();
                if (element_2.get_id_kntnx8_k$() === element_1) {
                  tmp$ret$6 = element_2;
                  break $l$block_0;
                }
              }
              tmp$ret$6 = null;
            }
            var tmp0_safe_receiver = tmp$ret$6;
            if (tmp0_safe_receiver == null)
              null;
            else {
              // Inline function 'kotlin.let' call
              destination_0.add_utx5q5_k$(tmp0_safe_receiver);
            }
          }

          var cadets = destination_0;
          tmp = plus(listOfNotNull(admin), cadets);
          break;
        case 'cadet':
          var tmp$ret$16;
          $l$block_1: {
            // Inline function 'kotlin.collections.firstOrNull' call
            var _iterator__ex2g4s_3 = all.iterator_jk1svi_k$();
            while (_iterator__ex2g4s_3.hasNext_bitz1p_k$()) {
              var element_3 = _iterator__ex2g4s_3.next_20eer_k$();
              if (element_3.get_role_wotsxr_k$() === 'admin') {
                tmp$ret$16 = element_3;
                break $l$block_1;
              }
            }
            tmp$ret$16 = null;
          }

          var admin_0 = tmp$ret$16;
          var tmp1_safe_receiver = $currentUser.get_assignedInstructorId_laxw6p_k$();
          var tmp_0;
          if (tmp1_safe_receiver == null) {
            tmp_0 = null;
          } else {
            // Inline function 'kotlin.let' call
            // Inline function 'kotlin.collections.find' call
            var tmp$ret$18;
            $l$block_2: {
              // Inline function 'kotlin.collections.firstOrNull' call
              var _iterator__ex2g4s_4 = all.iterator_jk1svi_k$();
              while (_iterator__ex2g4s_4.hasNext_bitz1p_k$()) {
                var element_4 = _iterator__ex2g4s_4.next_20eer_k$();
                if (element_4.get_id_kntnx8_k$() === tmp1_safe_receiver) {
                  tmp$ret$18 = element_4;
                  break $l$block_2;
                }
              }
              tmp$ret$18 = null;
            }
            tmp_0 = tmp$ret$18;
          }

          var instructor = tmp_0;
          tmp = distinct(listOfNotNull_0([admin_0, instructor]));
          break;
        default:
          tmp = emptyList();
          break;
      }
      var list = tmp;
      $callback(list);
      return Unit_getInstance();
    };
  }
  var properties_initialized_Firebase_kt_x94qll;
  function _init_properties_Firebase_kt__4razx5() {
    if (!properties_initialized_Firebase_kt_x94qll) {
      properties_initialized_Firebase_kt_x94qll = true;
      authInstance = null;
      firestoreInstance = null;
      firebaseCompat = null;
      databaseInstance = null;
      // Inline function 'kotlin.js.unsafeCast' call
      newDateFromMillis = function (ms) {
        return new Date(ms);
      };
    }
  }
  //region block: init
  currentUnsubscribe = null;
  //endregion
  mainWrapper();
  return _;
}));
