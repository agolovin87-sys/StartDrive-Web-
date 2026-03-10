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
  var Unit_instance = kotlin_kotlin.$_$.h;
  var Enum = kotlin_kotlin.$_$.k2;
  var protoOf = kotlin_kotlin.$_$.t1;
  var initMetadataForClass = kotlin_kotlin.$_$.j1;
  var VOID = kotlin_kotlin.$_$.b;
  var emptyList = kotlin_kotlin.$_$.p;
  var toString = kotlin_kotlin.$_$.r2;
  var toString_0 = kotlin_kotlin.$_$.v1;
  var getStringHashCode = kotlin_kotlin.$_$.h1;
  var getBooleanHashCode = kotlin_kotlin.$_$.g1;
  var hashCode = kotlin_kotlin.$_$.i1;
  var THROW_CCE = kotlin_kotlin.$_$.n2;
  var equals = kotlin_kotlin.$_$.f1;
  var trimIndent = kotlin_kotlin.$_$.g2;
  var joinToString = kotlin_kotlin.$_$.r;
  var isCharSequence = kotlin_kotlin.$_$.n1;
  var trim = kotlin_kotlin.$_$.h2;
  var split = kotlin_kotlin.$_$.b2;
  var ArrayList_init_$Create$ = kotlin_kotlin.$_$.e;
  var isBlank = kotlin_kotlin.$_$.a2;
  var drop = kotlin_kotlin.$_$.o;
  var collectionSizeOrDefault = kotlin_kotlin.$_$.l;
  var ArrayList_init_$Create$_0 = kotlin_kotlin.$_$.d;
  var firstOrNull = kotlin_kotlin.$_$.z1;
  var toString_1 = kotlin_kotlin.$_$.g;
  var Char = kotlin_kotlin.$_$.i2;
  var Long = kotlin_kotlin.$_$.l2;
  var sortedWith = kotlin_kotlin.$_$.x;
  var take = kotlin_kotlin.$_$.z;
  var LinkedHashMap_init_$Create$ = kotlin_kotlin.$_$.f;
  var substringBefore = kotlin_kotlin.$_$.d2;
  var SharedFactory_instance = kotlin_StartDrive_shared.$_$.b;
  var coerceIn = kotlin_kotlin.$_$.x1;
  var checkIndexOverflow = kotlin_kotlin.$_$.k;
  var Pair = kotlin_kotlin.$_$.m2;
  var noWhenBranchMatchedException = kotlin_kotlin.$_$.q2;
  var FunctionAdapter = kotlin_kotlin.$_$.d1;
  var isInterface = kotlin_kotlin.$_$.o1;
  var Comparator = kotlin_kotlin.$_$.j2;
  var toIntOrNull = kotlin_kotlin.$_$.f2;
  var listOf = kotlin_kotlin.$_$.u;
  var ensureNotNull = kotlin_kotlin.$_$.o2;
  var compareValues = kotlin_kotlin.$_$.c1;
  var Collection = kotlin_kotlin.$_$.i;
  var checkCountOverflow = kotlin_kotlin.$_$.j;
  var take_0 = kotlin_kotlin.$_$.e2;
  var substringAfter = kotlin_kotlin.$_$.c2;
  var charSequenceLength = kotlin_kotlin.$_$.e1;
  var numberToLong = kotlin_kotlin.$_$.s1;
  var sorted = kotlin_kotlin.$_$.y;
  var to = kotlin_kotlin.$_$.s2;
  var json = kotlin_kotlin.$_$.q1;
  var isNumber = kotlin_kotlin.$_$.p1;
  var sortWith = kotlin_kotlin.$_$.w;
  var toLong = kotlin_kotlin.$_$.u1;
  var until = kotlin_kotlin.$_$.y1;
  var numberToInt = kotlin_kotlin.$_$.r1;
  var coerceAtLeast = kotlin_kotlin.$_$.w1;
  var isArray = kotlin_kotlin.$_$.m1;
  var User = kotlin_StartDrive_shared.$_$.a;
  var toList = kotlin_kotlin.$_$.a1;
  var toMutableList = kotlin_kotlin.$_$.b1;
  var copyToArray = kotlin_kotlin.$_$.m;
  var listOfNotNull = kotlin_kotlin.$_$.s;
  var plus = kotlin_kotlin.$_$.v;
  var listOfNotNull_0 = kotlin_kotlin.$_$.t;
  var distinct = kotlin_kotlin.$_$.n;
  //endregion
  //region block: pre-declaration
  initMetadataForClass(AppScreen, 'AppScreen', VOID, Enum);
  initMetadataForClass(AppState, 'AppState', AppState);
  initMetadataForClass(sam$kotlin_Comparator$0, 'sam$kotlin_Comparator$0', VOID, VOID, [Comparator, FunctionAdapter]);
  initMetadataForClass(sam$kotlin_Comparator$0_0, 'sam$kotlin_Comparator$0', VOID, VOID, [Comparator, FunctionAdapter]);
  initMetadataForClass(sam$kotlin_Comparator$0_1, 'sam$kotlin_Comparator$0', VOID, VOID, [Comparator, FunctionAdapter]);
  initMetadataForClass(ChatMessage, 'ChatMessage', ChatMessage);
  initMetadataForClass(sam$kotlin_Comparator$0_2, 'sam$kotlin_Comparator$0', VOID, VOID, [Comparator, FunctionAdapter]);
  initMetadataForClass(DrivingSession, 'DrivingSession', DrivingSession);
  initMetadataForClass(InstructorOpenWindow, 'InstructorOpenWindow', InstructorOpenWindow);
  initMetadataForClass(BalanceHistoryEntry, 'BalanceHistoryEntry', BalanceHistoryEntry);
  initMetadataForClass(sam$kotlin_Comparator$0_3, 'sam$kotlin_Comparator$0', VOID, VOID, [Comparator, FunctionAdapter]);
  //endregion
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
  var AppScreen_entriesInitialized;
  function AppScreen_initEntries() {
    if (AppScreen_entriesInitialized)
      return Unit_instance;
    AppScreen_entriesInitialized = true;
    AppScreen_Login_instance = new AppScreen('Login', 0);
    AppScreen_Register_instance = new AppScreen('Register', 1);
    AppScreen_PendingApproval_instance = new AppScreen('PendingApproval', 2);
    AppScreen_ProfileNotFound_instance = new AppScreen('ProfileNotFound', 3);
    AppScreen_Admin_instance = new AppScreen('Admin', 4);
    AppScreen_Instructor_instance = new AppScreen('Instructor', 5);
    AppScreen_Cadet_instance = new AppScreen('Cadet', 6);
  }
  function AppScreen(name, ordinal) {
    Enum.call(this, name, ordinal);
  }
  function AppState(screen, user, error, loading, networkError, selectedTabIndex, chatContacts, chatContactsLoading, selectedChatContactId, chatMessages, recordingOpenWindows, recordingSessions, recordingLoading, historySessions, historyBalance, historyLoading, balanceAdminHistory, balanceAdminUsers, balanceAdminLoading, balanceAdminSelectedUserId, adminHomeUsers, adminHomeLoading, adminNewbiesSectionOpen, adminInstructorsSectionOpen, adminCadetsSectionOpen, balanceHistorySectionOpen, adminAssignInstructorId, adminAssignCadetId, adminInstructorCadetsModalId, cadetInstructor, instructorCadets) {
    screen = screen === VOID ? AppScreen_Login_getInstance() : screen;
    user = user === VOID ? null : user;
    error = error === VOID ? null : error;
    loading = loading === VOID ? false : loading;
    networkError = networkError === VOID ? null : networkError;
    selectedTabIndex = selectedTabIndex === VOID ? 0 : selectedTabIndex;
    chatContacts = chatContacts === VOID ? emptyList() : chatContacts;
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
    adminNewbiesSectionOpen = adminNewbiesSectionOpen === VOID ? false : adminNewbiesSectionOpen;
    adminInstructorsSectionOpen = adminInstructorsSectionOpen === VOID ? false : adminInstructorsSectionOpen;
    adminCadetsSectionOpen = adminCadetsSectionOpen === VOID ? false : adminCadetsSectionOpen;
    balanceHistorySectionOpen = balanceHistorySectionOpen === VOID ? false : balanceHistorySectionOpen;
    adminAssignInstructorId = adminAssignInstructorId === VOID ? null : adminAssignInstructorId;
    adminAssignCadetId = adminAssignCadetId === VOID ? null : adminAssignCadetId;
    adminInstructorCadetsModalId = adminInstructorCadetsModalId === VOID ? null : adminInstructorCadetsModalId;
    cadetInstructor = cadetInstructor === VOID ? null : cadetInstructor;
    instructorCadets = instructorCadets === VOID ? emptyList() : instructorCadets;
    this.aa_1 = screen;
    this.ba_1 = user;
    this.ca_1 = error;
    this.da_1 = loading;
    this.ea_1 = networkError;
    this.fa_1 = selectedTabIndex;
    this.ga_1 = chatContacts;
    this.ha_1 = chatContactsLoading;
    this.ia_1 = selectedChatContactId;
    this.ja_1 = chatMessages;
    this.ka_1 = recordingOpenWindows;
    this.la_1 = recordingSessions;
    this.ma_1 = recordingLoading;
    this.na_1 = historySessions;
    this.oa_1 = historyBalance;
    this.pa_1 = historyLoading;
    this.qa_1 = balanceAdminHistory;
    this.ra_1 = balanceAdminUsers;
    this.sa_1 = balanceAdminLoading;
    this.ta_1 = balanceAdminSelectedUserId;
    this.ua_1 = adminHomeUsers;
    this.va_1 = adminHomeLoading;
    this.wa_1 = adminNewbiesSectionOpen;
    this.xa_1 = adminInstructorsSectionOpen;
    this.ya_1 = adminCadetsSectionOpen;
    this.za_1 = balanceHistorySectionOpen;
    this.ab_1 = adminAssignInstructorId;
    this.bb_1 = adminAssignCadetId;
    this.cb_1 = adminInstructorCadetsModalId;
    this.db_1 = cadetInstructor;
    this.eb_1 = instructorCadets;
  }
  protoOf(AppState).toString = function () {
    return 'AppState(screen=' + this.aa_1.toString() + ', user=' + toString(this.ba_1) + ', error=' + this.ca_1 + ', loading=' + this.da_1 + ', networkError=' + this.ea_1 + ', selectedTabIndex=' + this.fa_1 + ', chatContacts=' + toString_0(this.ga_1) + ', chatContactsLoading=' + this.ha_1 + ', selectedChatContactId=' + this.ia_1 + ', chatMessages=' + toString_0(this.ja_1) + ', recordingOpenWindows=' + toString_0(this.ka_1) + ', recordingSessions=' + toString_0(this.la_1) + ', recordingLoading=' + this.ma_1 + ', historySessions=' + toString_0(this.na_1) + ', historyBalance=' + toString_0(this.oa_1) + ', historyLoading=' + this.pa_1 + ', balanceAdminHistory=' + toString_0(this.qa_1) + ', balanceAdminUsers=' + toString_0(this.ra_1) + ', balanceAdminLoading=' + this.sa_1 + ', balanceAdminSelectedUserId=' + this.ta_1 + ', adminHomeUsers=' + toString_0(this.ua_1) + ', adminHomeLoading=' + this.va_1 + ', adminNewbiesSectionOpen=' + this.wa_1 + ', adminInstructorsSectionOpen=' + this.xa_1 + ', adminCadetsSectionOpen=' + this.ya_1 + ', balanceHistorySectionOpen=' + this.za_1 + ', adminAssignInstructorId=' + this.ab_1 + ', adminAssignCadetId=' + this.bb_1 + ', adminInstructorCadetsModalId=' + this.cb_1 + ', cadetInstructor=' + toString(this.db_1) + ', instructorCadets=' + toString_0(this.eb_1) + ')';
  };
  protoOf(AppState).hashCode = function () {
    var result = this.aa_1.hashCode();
    result = imul(result, 31) + (this.ba_1 == null ? 0 : this.ba_1.hashCode()) | 0;
    result = imul(result, 31) + (this.ca_1 == null ? 0 : getStringHashCode(this.ca_1)) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.da_1) | 0;
    result = imul(result, 31) + (this.ea_1 == null ? 0 : getStringHashCode(this.ea_1)) | 0;
    result = imul(result, 31) + this.fa_1 | 0;
    result = imul(result, 31) + hashCode(this.ga_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.ha_1) | 0;
    result = imul(result, 31) + (this.ia_1 == null ? 0 : getStringHashCode(this.ia_1)) | 0;
    result = imul(result, 31) + hashCode(this.ja_1) | 0;
    result = imul(result, 31) + hashCode(this.ka_1) | 0;
    result = imul(result, 31) + hashCode(this.la_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.ma_1) | 0;
    result = imul(result, 31) + hashCode(this.na_1) | 0;
    result = imul(result, 31) + hashCode(this.oa_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.pa_1) | 0;
    result = imul(result, 31) + hashCode(this.qa_1) | 0;
    result = imul(result, 31) + hashCode(this.ra_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.sa_1) | 0;
    result = imul(result, 31) + (this.ta_1 == null ? 0 : getStringHashCode(this.ta_1)) | 0;
    result = imul(result, 31) + hashCode(this.ua_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.va_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.wa_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.xa_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.ya_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.za_1) | 0;
    result = imul(result, 31) + (this.ab_1 == null ? 0 : getStringHashCode(this.ab_1)) | 0;
    result = imul(result, 31) + (this.bb_1 == null ? 0 : getStringHashCode(this.bb_1)) | 0;
    result = imul(result, 31) + (this.cb_1 == null ? 0 : getStringHashCode(this.cb_1)) | 0;
    result = imul(result, 31) + (this.db_1 == null ? 0 : this.db_1.hashCode()) | 0;
    result = imul(result, 31) + hashCode(this.eb_1) | 0;
    return result;
  };
  protoOf(AppState).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof AppState))
      return false;
    var tmp0_other_with_cast = other instanceof AppState ? other : THROW_CCE();
    if (!this.aa_1.equals(tmp0_other_with_cast.aa_1))
      return false;
    if (!equals(this.ba_1, tmp0_other_with_cast.ba_1))
      return false;
    if (!(this.ca_1 == tmp0_other_with_cast.ca_1))
      return false;
    if (!(this.da_1 === tmp0_other_with_cast.da_1))
      return false;
    if (!(this.ea_1 == tmp0_other_with_cast.ea_1))
      return false;
    if (!(this.fa_1 === tmp0_other_with_cast.fa_1))
      return false;
    if (!equals(this.ga_1, tmp0_other_with_cast.ga_1))
      return false;
    if (!(this.ha_1 === tmp0_other_with_cast.ha_1))
      return false;
    if (!(this.ia_1 == tmp0_other_with_cast.ia_1))
      return false;
    if (!equals(this.ja_1, tmp0_other_with_cast.ja_1))
      return false;
    if (!equals(this.ka_1, tmp0_other_with_cast.ka_1))
      return false;
    if (!equals(this.la_1, tmp0_other_with_cast.la_1))
      return false;
    if (!(this.ma_1 === tmp0_other_with_cast.ma_1))
      return false;
    if (!equals(this.na_1, tmp0_other_with_cast.na_1))
      return false;
    if (!equals(this.oa_1, tmp0_other_with_cast.oa_1))
      return false;
    if (!(this.pa_1 === tmp0_other_with_cast.pa_1))
      return false;
    if (!equals(this.qa_1, tmp0_other_with_cast.qa_1))
      return false;
    if (!equals(this.ra_1, tmp0_other_with_cast.ra_1))
      return false;
    if (!(this.sa_1 === tmp0_other_with_cast.sa_1))
      return false;
    if (!(this.ta_1 == tmp0_other_with_cast.ta_1))
      return false;
    if (!equals(this.ua_1, tmp0_other_with_cast.ua_1))
      return false;
    if (!(this.va_1 === tmp0_other_with_cast.va_1))
      return false;
    if (!(this.wa_1 === tmp0_other_with_cast.wa_1))
      return false;
    if (!(this.xa_1 === tmp0_other_with_cast.xa_1))
      return false;
    if (!(this.ya_1 === tmp0_other_with_cast.ya_1))
      return false;
    if (!(this.za_1 === tmp0_other_with_cast.za_1))
      return false;
    if (!(this.ab_1 == tmp0_other_with_cast.ab_1))
      return false;
    if (!(this.bb_1 == tmp0_other_with_cast.bb_1))
      return false;
    if (!(this.cb_1 == tmp0_other_with_cast.cb_1))
      return false;
    if (!equals(this.db_1, tmp0_other_with_cast.db_1))
      return false;
    if (!equals(this.eb_1, tmp0_other_with_cast.eb_1))
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
  var iconPhoneSvg;
  var iconChatSvg;
  var iconUserPlusSvg;
  var iconPowerSvg;
  var iconTrashSvg;
  var iconUnlinkSvg;
  var iconUserSvg;
  var iconPhoneLabelSvg;
  var iconTicketSvg;
  var iconInstructorSvg;
  var iconSelectSvg;
  var iconCreditSvg;
  var iconDebitSvg;
  var iconSetSvg;
  var iconResetSvg;
  function main() {
    var tmp = window;
    var tmp_0 = main$lambda;
    tmp.onload = typeof tmp_0 === 'function' ? tmp_0 : THROW_CCE();
  }
  function renderLogin(error, loading) {
    var err = !(error == null) ? '<p class="sd-error">' + error + '<\/p>' : '';
    var btn = loading ? '\u0412\u0445\u043E\u0434\u2026' : '\u0412\u043E\u0439\u0442\u0438';
    return trimIndent('\n        <header class="sd-header">\n            <h1>StartDrive<\/h1>\n            <p>\u0412\u0445\u043E\u0434 \u0432 \u0432\u0435\u0431-\u0432\u0435\u0440\u0441\u0438\u044E<\/p>\n        <\/header>\n        <main class="sd-content">\n            <div class="sd-card sd-login-card">\n                <h2>\u0412\u0445\u043E\u0434<\/h2>\n                ' + err + '\n                <label>Email<\/label>\n                <input type="email" id="sd-email" class="sd-input" placeholder="email@example.com" />\n                <label>\u041F\u0430\u0440\u043E\u043B\u044C<\/label>\n                <input type="password" id="sd-password" class="sd-input" placeholder="\u041F\u0430\u0440\u043E\u043B\u044C" />\n                <label class="sd-checkbox"><input type="checkbox" id="sd-stay" checked /> \u041E\u0441\u0442\u0430\u0432\u0430\u0442\u044C\u0441\u044F \u0432 \u043F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u0438<\/label>\n                <button type="button" id="sd-btn-signin" class="sd-btn sd-btn-primary" ' + (loading ? 'disabled' : '') + '>' + btn + '<\/button>\n                <button type="button" id="sd-btn-register" class="sd-btn sd-btn-secondary">\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F<\/button>\n            <\/div>\n        <\/main>\n    ');
  }
  function renderRegister(error, loading) {
    var err = !(error == null) ? '<p class="sd-error">' + error + '<\/p>' : '';
    var btn = loading ? '\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F\u2026' : '\u0417\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u0442\u044C\u0441\u044F';
    return trimIndent('\n        <header class="sd-header">\n            <h1>StartDrive<\/h1>\n            <p>\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F<\/p>\n        <\/header>\n        <main class="sd-content">\n            <div class="sd-card sd-login-card">\n                <h2>\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F<\/h2>\n                ' + err + '\n                <label>\u0424\u0418\u041E<\/label>\n                <input type="text" id="sd-fullName" class="sd-input" placeholder="\u0418\u0432\u0430\u043D\u043E\u0432 \u0418\u0432\u0430\u043D \u0418\u0432\u0430\u043D\u043E\u0432\u0438\u0447" />\n                <label>Email<\/label>\n                <input type="email" id="sd-reg-email" class="sd-input" placeholder="email@example.com" />\n                <label>\u0422\u0435\u043B\u0435\u0444\u043E\u043D<\/label>\n                <input type="tel" id="sd-phone" class="sd-input" placeholder="+7 \u2026" />\n                <label>\u041F\u0430\u0440\u043E\u043B\u044C<\/label>\n                <input type="password" id="sd-reg-password" class="sd-input" placeholder="\u041F\u0430\u0440\u043E\u043B\u044C" />\n                <label>\u0420\u043E\u043B\u044C<\/label>\n                <select id="sd-role" class="sd-input">\n                    <option value="cadet">\u041A\u0443\u0440\u0441\u0430\u043D\u0442<\/option>\n                    <option value="instructor">\u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440<\/option>\n                    <option value="admin">\u0410\u0434\u043C\u0438\u043D\u0438\u0441\u0442\u0440\u0430\u0442\u043E\u0440<\/option>\n                <\/select>\n                <button type="button" id="sd-btn-do-register" class="sd-btn sd-btn-primary" ' + (loading ? 'disabled' : '') + '>' + btn + '<\/button>\n                <button type="button" id="sd-btn-back" class="sd-btn sd-btn-secondary">\u041D\u0430\u0437\u0430\u0434<\/button>\n            <\/div>\n        <\/main>\n    ');
  }
  function renderPendingApproval() {
    return '<header class="sd-header">\n    <h1>StartDrive<\/h1>\n    <p>\u041E\u0436\u0438\u0434\u0430\u043D\u0438\u0435 \u043F\u043E\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0435\u043D\u0438\u044F<\/p>\n<\/header>\n<main class="sd-content">\n    <div class="sd-card">\n        <h2>\u041E\u0436\u0438\u0434\u0430\u043D\u0438\u0435 \u043F\u043E\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0435\u043D\u0438\u044F<\/h2>\n        <p>\u0412\u0430\u0448\u0430 \u0437\u0430\u044F\u0432\u043A\u0430 \u043D\u0430 \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044E \u043E\u0442\u043F\u0440\u0430\u0432\u043B\u0435\u043D\u0430. \u0410\u0434\u043C\u0438\u043D\u0438\u0441\u0442\u0440\u0430\u0442\u043E\u0440 \u0430\u043A\u0442\u0438\u0432\u0438\u0440\u0443\u0435\u0442 \u0432\u0430\u0448 \u0430\u043A\u043A\u0430\u0443\u043D\u0442. \u041F\u043E\u0441\u043B\u0435 \u0430\u043A\u0442\u0438\u0432\u0430\u0446\u0438\u0438 \u0432\u044B \u0441\u043C\u043E\u0436\u0435\u0442\u0435 \u0432\u043E\u0439\u0442\u0438.<\/p>\n        <button type="button" id="sd-btn-check" class="sd-btn sd-btn-primary">\u041F\u0440\u043E\u0432\u0435\u0440\u0438\u0442\u044C \u0441\u043D\u043E\u0432\u0430<\/button>\n        <button type="button" id="sd-btn-signout-pending" class="sd-btn sd-btn-secondary">\u0412\u044B\u0439\u0442\u0438<\/button>\n    <\/div>\n<\/main>';
  }
  function renderProfileNotFound(message) {
    return trimIndent('\n    <header class="sd-header">\n        <h1>StartDrive<\/h1>\n        <p>\u041F\u0440\u043E\u0444\u0438\u043B\u044C \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D<\/p>\n    <\/header>\n    <main class="sd-content">\n        <div class="sd-card">\n            <h2>\u041D\u0435 \u0443\u0434\u0430\u043B\u043E\u0441\u044C \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044C \u043F\u0440\u043E\u0444\u0438\u043B\u044C<\/h2>\n            <p class="sd-error">' + message + '<\/p>\n            <p>\u0415\u0441\u043B\u0438 \u0432\u044B \u0432\u0445\u043E\u0434\u0438\u0442\u0435 \u0441 \u0442\u0435\u043C\u0438 \u0436\u0435 \u0434\u0430\u043D\u043D\u044B\u043C\u0438, \u0447\u0442\u043E \u0438 \u0432 \u043F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u0438 \u2014 \u0443\u0431\u0435\u0434\u0438\u0442\u0435\u0441\u044C, \u0447\u0442\u043E \u0432 Firebase Console \u0432 Firestore \u0435\u0441\u0442\u044C \u0434\u043E\u043A\u0443\u043C\u0435\u043D\u0442 \u0432 \u043A\u043E\u043B\u043B\u0435\u043A\u0446\u0438\u0438 <strong>users<\/strong> \u0441 id \u0432\u0430\u0448\u0435\u0433\u043E \u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u0435\u043B\u044F (UID \u0438\u0437 Authentication).<\/p>\n            <button type="button" id="sd-btn-signout-profile-not-found" class="sd-btn sd-btn-primary">\u0412\u044B\u0439\u0442\u0438<\/button>\n        <\/div>\n    <\/main>\n');
  }
  function renderChatTabContent(currentUser) {
    var contactId = get_appState().ia_1;
    var contacts = get_appState().ga_1;
    var loading = get_appState().ha_1;
    var messages = get_appState().ja_1;
    var myId = currentUser.l9_1;
    if (!(contactId == null)) {
      // Inline function 'kotlin.collections.find' call
      var tmp$ret$1;
      $l$block: {
        // Inline function 'kotlin.collections.firstOrNull' call
        var _iterator__ex2g4s = contacts.g();
        while (_iterator__ex2g4s.h()) {
          var element = _iterator__ex2g4s.i();
          if (element.l9_1 === contactId) {
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
      var msgsHtml = joinToString(messages, '', VOID, VOID, VOID, VOID, renderChatTabContent$lambda(myId, currentUser, contact));
      return trimIndent('\n            <div class="sd-chat-conversation">\n                <div class="sd-chat-header">\n                    <button type="button" id="sd-chat-back" class="sd-btn sd-btn-secondary">\u2190 \u041D\u0430\u0437\u0430\u0434<\/button>\n                    <span class="sd-chat-contact-name">' + escapeHtml(contact.m9_1) + '<\/span>\n                <\/div>\n                <div class="sd-chat-messages" id="sd-chat-messages">' + msgsHtml + '<\/div>\n                <div class="sd-chat-input-row">\n                    <input type="text" id="sd-chat-input" class="sd-input" placeholder="\u0421\u043E\u043E\u0431\u0449\u0435\u043D\u0438\u0435..." maxlength="2000" />\n                    <button type="button" id="sd-chat-send" class="sd-btn sd-btn-primary">\u041E\u0442\u043F\u0440\u0430\u0432\u0438\u0442\u044C<\/button>\n                <\/div>\n            <\/div>\n        ');
    }
    var loadingLine = loading ? '<p class="sd-loading-text">\u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430 \u043A\u043E\u043D\u0442\u0430\u043A\u0442\u043E\u0432\u2026 <button type="button" id="sd-chat-stop-loading" class="sd-btn sd-btn-small sd-btn-secondary">\u041F\u043E\u043A\u0430\u0437\u0430\u0442\u044C \u043F\u0443\u0441\u0442\u043E<\/button><\/p>' : '';
    var refreshBtn = '<button type="button" id="sd-chat-refresh" class="sd-btn sd-btn-small sd-btn-secondary">\u041E\u0431\u043D\u043E\u0432\u0438\u0442\u044C \u0441\u043F\u0438\u0441\u043E\u043A \u043A\u043E\u043D\u0442\u0430\u043A\u0442\u043E\u0432<\/button>';
    var listHtml = joinToString(contacts, '', VOID, VOID, VOID, VOID, renderChatTabContent$lambda_0);
    var contactsBlock = contacts.q() && !loading ? '<p>\u041D\u0435\u0442 \u043A\u043E\u043D\u0442\u0430\u043A\u0442\u043E\u0432 \u0434\u043B\u044F \u0447\u0430\u0442\u0430. \u0410\u0434\u043C\u0438\u043D: \u0432\u0441\u0435 \u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u0435\u043B\u0438 \u043A\u0440\u043E\u043C\u0435 \u0430\u0434\u043C\u0438\u043D\u043E\u0432. \u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440: \u0430\u0434\u043C\u0438\u043D + \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D\u043D\u044B\u0435 \u043A\u0443\u0440\u0441\u0430\u043D\u0442\u044B. \u041A\u0443\u0440\u0441\u0430\u043D\u0442: \u0430\u0434\u043C\u0438\u043D + \u0432\u0430\u0448 \u0438\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440.<\/p>' : '<div class="sd-chat-contacts">' + listHtml + '<\/div>';
    return '<h2>\u0427\u0430\u0442<\/h2>' + loadingLine + '<p>' + refreshBtn + '<\/p>' + contactsBlock;
  }
  function escapeHtml(_this__u8e3s4) {
    return _this__u8e3s4;
  }
  function formatShortName(fullName) {
    // Inline function 'kotlin.text.trim' call
    var tmp$ret$0 = toString_0(trim(isCharSequence(fullName) ? fullName : THROW_CCE()));
    // Inline function 'kotlin.collections.filter' call
    var tmp0 = split(tmp$ret$0, [' ']);
    // Inline function 'kotlin.collections.filterTo' call
    var destination = ArrayList_init_$Create$();
    var _iterator__ex2g4s = tmp0.g();
    while (_iterator__ex2g4s.h()) {
      var element = _iterator__ex2g4s.i();
      // Inline function 'kotlin.text.isNotBlank' call
      if (!isBlank(element)) {
        destination.e(element);
      }
    }
    var parts = destination;
    if (parts.q())
      return '\u2014';
    if (parts.j() === 1)
      return parts.o(0);
    var surname = parts.o(0);
    // Inline function 'kotlin.collections.map' call
    var this_0 = drop(parts, 1);
    // Inline function 'kotlin.collections.mapTo' call
    var destination_0 = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_0, 10));
    var _iterator__ex2g4s_0 = this_0.g();
    while (_iterator__ex2g4s_0.h()) {
      var item = _iterator__ex2g4s_0.i();
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
      destination_0.e(tmp$ret$8);
    }
    var initials = joinToString(destination_0, ' ');
    return surname + ' ' + initials;
  }
  function formatDateTime(ms) {
    if (ms == null || ms.w1(new Long(0, 0)) <= 0)
      return '\u2014';
    var d = new Date(ms);
    // Inline function 'kotlin.js.unsafeCast' call
    return d.toLocaleString('ru-RU');
  }
  function formatDateTimeEkaterinburg(ms) {
    if (ms == null || ms.w1(new Long(0, 0)) <= 0)
      return '\u2014';
    var d = new Date(ms);
    // Inline function 'kotlin.js.unsafeCast' call
    return d.toLocaleString('ru-RU', {timeZone: 'Asia/Yekaterinburg', dateStyle: 'short', timeStyle: 'short'});
  }
  function renderAdminHomeContent() {
    var loading = get_appState().va_1;
    var allUsers = get_appState().ua_1;
    // Inline function 'kotlin.collections.filter' call
    // Inline function 'kotlin.collections.filterTo' call
    var destination = ArrayList_init_$Create$();
    var _iterator__ex2g4s = allUsers.g();
    while (_iterator__ex2g4s.h()) {
      var element = _iterator__ex2g4s.i();
      if ((element.p9_1 === 'instructor' || element.p9_1 === 'cadet') && !element.u9_1) {
        destination.e(element);
      }
    }
    // Inline function 'kotlin.collections.sortedBy' call
    // Inline function 'kotlin.comparisons.compareBy' call
    var tmp = renderAdminHomeContent$lambda;
    var tmp$ret$3 = new sam$kotlin_Comparator$0(tmp);
    var newbies = sortedWith(destination, tmp$ret$3);
    // Inline function 'kotlin.collections.filter' call
    // Inline function 'kotlin.collections.filterTo' call
    var destination_0 = ArrayList_init_$Create$();
    var _iterator__ex2g4s_0 = allUsers.g();
    while (_iterator__ex2g4s_0.h()) {
      var element_0 = _iterator__ex2g4s_0.i();
      if (element_0.p9_1 === 'instructor' && element_0.u9_1) {
        destination_0.e(element_0);
      }
    }
    // Inline function 'kotlin.collections.sortedBy' call
    // Inline function 'kotlin.comparisons.compareBy' call
    var tmp_0 = renderAdminHomeContent$lambda_0;
    var tmp$ret$8 = new sam$kotlin_Comparator$0(tmp_0);
    var instructors = sortedWith(destination_0, tmp$ret$8);
    // Inline function 'kotlin.collections.filter' call
    // Inline function 'kotlin.collections.filterTo' call
    var destination_1 = ArrayList_init_$Create$();
    var _iterator__ex2g4s_1 = allUsers.g();
    while (_iterator__ex2g4s_1.h()) {
      var element_1 = _iterator__ex2g4s_1.i();
      if (element_1.p9_1 === 'cadet' && element_1.u9_1) {
        destination_1.e(element_1);
      }
    }
    // Inline function 'kotlin.collections.sortedBy' call
    // Inline function 'kotlin.comparisons.compareBy' call
    var tmp_1 = renderAdminHomeContent$lambda_1;
    var tmp$ret$13 = new sam$kotlin_Comparator$0(tmp_1);
    var cadets = sortedWith(destination_1, tmp$ret$13);
    var emptyLoadBtn = !loading && allUsers.q() ? '<p>\u0421\u043F\u0438\u0441\u043E\u043A \u043F\u0443\u0441\u0442. <button type="button" id="sd-admin-home-load" class="sd-btn sd-btn-primary">\u0417\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044C<\/button><\/p>' : '';
    var topSlotContent = emptyLoadBtn;
    var topSlot = '<div class="sd-admin-home-top-slot">' + topSlotContent + '<\/div>';
    var newbiesCards = joinToString(newbies, '', VOID, VOID, VOID, VOID, renderAdminHomeContent$lambda_2);
    var newbiesOpen = get_appState().wa_1 ? ' open' : '';
    var newbiesContent = newbies.q() ? '<p class="sd-muted">\u041D\u0435\u0442 \u043D\u043E\u0432\u044B\u0445 \u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u0435\u043B\u0435\u0439. \u041F\u043E\u0441\u043B\u0435 \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u0438 \u043E\u043D\u0438 \u043F\u043E\u044F\u0432\u044F\u0442\u0441\u044F \u0437\u0434\u0435\u0441\u044C; \u043F\u043E\u0441\u043B\u0435 \u0430\u043A\u0442\u0438\u0432\u0430\u0446\u0438\u0438 \u043F\u0435\u0440\u0435\u0439\u0434\u0443\u0442 \u0432 \u0440\u0430\u0437\u0434\u0435\u043B \xAB\u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440\u044B\xBB \u0438\u043B\u0438 \xAB\u041A\u0443\u0440\u0441\u0430\u043D\u0442\u044B\xBB \u043F\u043E \u0432\u044B\u0431\u0440\u0430\u043D\u043D\u043E\u0439 \u0440\u043E\u043B\u0438.<\/p>' : newbiesCards;
    var newbiesBlock = '<details class="sd-block sd-details-block" data-admin-section="newbies"' + newbiesOpen + '><summary class="sd-block-title">\u041D\u043E\u0432\u0435\u043D\u044C\u043A\u0438\u0435 (' + newbies.j() + ')<\/summary><div class="sd-admin-cards">' + newbiesContent + '<\/div><\/details>';
    var assignInstructorId = get_appState().ab_1;
    var assignCadetId = get_appState().bb_1;
    var tmp_2;
    if (!(assignInstructorId == null)) {
      // Inline function 'kotlin.collections.find' call
      var tmp$ret$16;
      $l$block: {
        // Inline function 'kotlin.collections.firstOrNull' call
        var _iterator__ex2g4s_2 = instructors.g();
        while (_iterator__ex2g4s_2.h()) {
          var element_2 = _iterator__ex2g4s_2.i();
          if (element_2.l9_1 === assignInstructorId) {
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
        tmp_3 = escapeHtml(formatShortName(inst.m9_1));
      }
      var tmp1_elvis_lhs = tmp_3;
      var instShortName = tmp1_elvis_lhs == null ? '\u2014' : tmp1_elvis_lhs;
      // Inline function 'kotlin.collections.filter' call
      // Inline function 'kotlin.collections.filterTo' call
      var destination_2 = ArrayList_init_$Create$();
      var _iterator__ex2g4s_3 = cadets.g();
      while (_iterator__ex2g4s_3.h()) {
        var element_3 = _iterator__ex2g4s_3.i();
        if (element_3.s9_1 == assignInstructorId) {
          destination_2.e(element_3);
        }
      }
      // Inline function 'kotlin.collections.sortedBy' call
      // Inline function 'kotlin.comparisons.compareBy' call
      var tmp_4 = renderAdminHomeContent$lambda_3;
      var tmp$ret$23 = new sam$kotlin_Comparator$0(tmp_4);
      var currentCadets = sortedWith(destination_2, tmp$ret$23);
      var currentSectionRows = joinToString(currentCadets, '', VOID, VOID, VOID, VOID, renderAdminHomeContent$lambda_4);
      var currentInstructorSection = '<div class="sd-assign-section sd-assign-section-current"><h4 class="sd-assign-section-title">\u041A\u0443\u0440\u0441\u0430\u043D\u0442\u044B \u0438\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440\u0430 (\u0443\u0436\u0435 \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D\u044B): ' + instShortName + ' (' + currentCadets.j() + ')<\/h4><div class="sd-assign-section-list">' + (currentCadets.q() ? '<p class="sd-muted">\u041D\u0435\u0442 \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D\u043D\u044B\u0445 \u043A\u0443\u0440\u0441\u0430\u043D\u0442\u043E\u0432.<\/p>' : currentSectionRows) + '<\/div><\/div>';
      // Inline function 'kotlin.collections.filter' call
      // Inline function 'kotlin.collections.filterTo' call
      var destination_3 = ArrayList_init_$Create$();
      var _iterator__ex2g4s_4 = cadets.g();
      while (_iterator__ex2g4s_4.h()) {
        var element_4 = _iterator__ex2g4s_4.i();
        if (element_4.s9_1 == null) {
          destination_3.e(element_4);
        }
      }
      // Inline function 'kotlin.collections.sortedBy' call
      // Inline function 'kotlin.comparisons.compareBy' call
      var tmp_5 = renderAdminHomeContent$lambda_5;
      var tmp$ret$28 = new sam$kotlin_Comparator$0(tmp_5);
      var unassigned = sortedWith(destination_3, tmp$ret$28);
      // Inline function 'kotlin.collections.filter' call
      // Inline function 'kotlin.collections.filterTo' call
      var destination_4 = ArrayList_init_$Create$();
      var _iterator__ex2g4s_5 = instructors.g();
      while (_iterator__ex2g4s_5.h()) {
        var element_5 = _iterator__ex2g4s_5.i();
        if (!(element_5.l9_1 === assignInstructorId)) {
          destination_4.e(element_5);
        }
      }
      // Inline function 'kotlin.collections.sortedBy' call
      // Inline function 'kotlin.comparisons.compareBy' call
      var tmp_6 = renderAdminHomeContent$lambda_6;
      var tmp$ret$33 = new sam$kotlin_Comparator$0(tmp_6);
      var otherInstructors = sortedWith(destination_4, tmp$ret$33);
      var newbiesRows = joinToString(unassigned, '', VOID, VOID, VOID, VOID, renderAdminHomeContent$lambda_7(assignInstructorId));
      var newbiesSection = '<div class="sd-assign-section"><h4 class="sd-assign-section-title">\u041D\u043E\u0432\u0435\u043D\u044C\u043A\u0438\u0435 (' + unassigned.j() + ')<\/h4><div class="sd-assign-section-list">' + (unassigned.q() ? '<p class="sd-muted">\u041D\u0435\u0442 \u043A\u0443\u0440\u0441\u0430\u043D\u0442\u043E\u0432 \u0431\u0435\u0437 \u0438\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440\u0430.<\/p>' : newbiesRows) + '<\/div><\/div>';
      var instructorsSections = joinToString(otherInstructors, '', VOID, VOID, VOID, VOID, renderAdminHomeContent$lambda_8(cadets, assignInstructorId));
      tmp_2 = '<div class="sd-assign-panel" id="sd-assign-panel"><h3 class="sd-assign-panel-title">\u041D\u0430\u0437\u043D\u0430\u0447\u0438\u0442\u044C \u043A\u0443\u0440\u0441\u0430\u043D\u0442\u0430 \u0438\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440\u0443: ' + instShortName + '<\/h3>' + currentInstructorSection + newbiesSection + instructorsSections + '<p class="sd-assign-panel-actions"><button type="button" id="sd-admin-assign-cancel" class="sd-btn sd-assign-close-btn">\u0417\u0430\u043A\u0440\u044B\u0442\u044C<\/button><\/p><\/div>';
    } else if (!(assignCadetId == null)) {
      // Inline function 'kotlin.collections.find' call
      var tmp$ret$36;
      $l$block_0: {
        // Inline function 'kotlin.collections.firstOrNull' call
        var _iterator__ex2g4s_6 = cadets.g();
        while (_iterator__ex2g4s_6.h()) {
          var element_6 = _iterator__ex2g4s_6.i();
          if (element_6.l9_1 === assignCadetId) {
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
        tmp_7 = escapeHtml(formatShortName(cadet.m9_1));
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
    var instOpen = get_appState().xa_1 ? ' open' : '';
    var cadetOpen = get_appState().ya_1 ? ' open' : '';
    var modalId = get_appState().cb_1;
    var tmp_8;
    if (!(modalId == null)) {
      // Inline function 'kotlin.collections.find' call
      var tmp$ret$41;
      $l$block_1: {
        // Inline function 'kotlin.collections.firstOrNull' call
        var _iterator__ex2g4s_7 = instructors.g();
        while (_iterator__ex2g4s_7.h()) {
          var element_7 = _iterator__ex2g4s_7.i();
          if (element_7.l9_1 === modalId) {
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
      var _iterator__ex2g4s_8 = cadets.g();
      while (_iterator__ex2g4s_8.h()) {
        var element_8 = _iterator__ex2g4s_8.i();
        if (element_8.s9_1 == modalId) {
          destination_5.e(element_8);
        }
      }
      // Inline function 'kotlin.collections.sortedBy' call
      // Inline function 'kotlin.comparisons.compareBy' call
      var tmp_9 = renderAdminHomeContent$lambda_12;
      var tmp$ret$46 = new sam$kotlin_Comparator$0(tmp_9);
      var modalCadets = sortedWith(destination_5, tmp$ret$46);
      var tmp5_elvis_lhs = inst_0 == null ? null : inst_0.m9_1;
      var instName = escapeHtml(tmp5_elvis_lhs == null ? '\u2014' : tmp5_elvis_lhs);
      var listItems = joinToString(modalCadets, '', VOID, VOID, VOID, VOID, renderAdminHomeContent$lambda_13);
      tmp_8 = '<div class="sd-modal-overlay" id="sd-admin-cadets-modal-overlay"><div class="sd-modal sd-admin-cadets-modal"><h3 class="sd-modal-title">\u041A\u0443\u0440\u0441\u0430\u043D\u0442\u044B \u0438\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440\u0430: ' + instName + '<\/h3><ul class="sd-instructor-cadets-list">' + listItems + '<\/ul><p class="sd-modal-actions"><button type="button" id="sd-admin-cadets-modal-close" class="sd-btn sd-assign-close-btn">\u0417\u0430\u043A\u0440\u044B\u0442\u044C<\/button><\/p><\/div><\/div>';
    } else {
      tmp_8 = '';
    }
    var cadetsModalHtml = tmp_8;
    return '<h2>\u0413\u043B\u0430\u0432\u043D\u0430\u044F<\/h2>' + topSlot + newbiesBlock + '<details class="sd-block sd-details-block" data-admin-section="instructors"' + instOpen + '><summary class="sd-block-title">\u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440\u044B (' + instructors.j() + ')<\/summary><div class="sd-admin-cards">' + instCards + '<\/div><\/details>' + assignBlock + '<details class="sd-block sd-details-block" data-admin-section="cadets"' + cadetOpen + '><summary class="sd-block-title">\u041A\u0443\u0440\u0441\u0430\u043D\u0442\u044B (' + cadets.j() + ')<\/summary><div class="sd-admin-cards">' + cadetCards + '<\/div><\/details>' + cadetsModalHtml;
  }
  function renderInstructorHomeContent(user, version) {
    var loading = get_appState().ma_1;
    // Inline function 'kotlin.collections.sortedBy' call
    var this_0 = get_appState().eb_1;
    // Inline function 'kotlin.comparisons.compareBy' call
    var tmp = renderInstructorHomeContent$lambda;
    var tmp$ret$0 = new sam$kotlin_Comparator$0_0(tmp);
    var cadets = sortedWith(this_0, tmp$ret$0);
    // Inline function 'kotlin.collections.filter' call
    var tmp0 = get_appState().la_1;
    // Inline function 'kotlin.collections.filterTo' call
    var destination = ArrayList_init_$Create$();
    var _iterator__ex2g4s = tmp0.g();
    while (_iterator__ex2g4s.h()) {
      var element = _iterator__ex2g4s.i();
      if (element.jb_1 === 'scheduled' || element.jb_1 === 'inProgress') {
        destination.e(element);
      }
    }
    var sessions = take(destination, 20);
    var allSessions = get_appState().la_1;
    var loadingLine = loading ? '<p class="sd-loading-text">\u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430\u2026<\/p>' : '';
    var tmp_0;
    if (cadets.q()) {
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
    var this_1 = user.m9_1;
    var tmp_1;
    if (isBlank(this_1)) {
      tmp_1 = '\u2014';
    } else {
      tmp_1 = this_1;
    }
    var tmp$ret$6 = tmp_1;
    var tmp_2 = escapeHtml(tmp$ret$6);
    // Inline function 'kotlin.text.ifBlank' call
    var this_2 = user.n9_1;
    var tmp_3;
    if (isBlank(this_2)) {
      tmp_3 = '\u2014';
    } else {
      tmp_3 = this_2;
    }
    var tmp$ret$8 = tmp_3;
    var tmp_4 = escapeHtml(tmp$ret$8);
    // Inline function 'kotlin.text.ifBlank' call
    var this_3 = user.o9_1;
    var tmp_5;
    if (isBlank(this_3)) {
      tmp_5 = '\u2014';
    } else {
      tmp_5 = this_3;
    }
    var tmp$ret$10 = tmp_5;
    var profileCard = '\n        <div class="sd-profile-card">\n            <div class="sd-profile-card-bg"><\/div>\n            <div class="sd-profile-card-overlay"><\/div>\n            <div class="sd-profile-card-shimmer" aria-hidden="true"><\/div>\n            <div class="sd-profile-card-inner">\n                <h3 class="sd-profile-card-title">' + iconPerson + ' \u041F\u0440\u043E\u0444\u0438\u043B\u044C \u0438\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440\u0430<\/h3>\n                <div class="sd-profile-row">' + iconPerson + '<span class="sd-profile-label">\u0424\u0418\u041E:<\/span><span class="sd-profile-value">' + tmp_2 + '<\/span><\/div>\n                <div class="sd-profile-row">' + iconEmail + '<span class="sd-profile-label">Email:<\/span><span class="sd-profile-value">' + tmp_4 + '<\/span><\/div>\n                <div class="sd-profile-row">' + iconPhone + '<span class="sd-profile-label">\u0422\u0435\u043B.:<\/span><span class="sd-profile-value">' + escapeHtml(tmp$ret$10) + '<\/span><\/div>\n                <div class="sd-profile-row">' + iconBadge + '<span class="sd-profile-label">\u0420\u043E\u043B\u044C:<\/span><span class="sd-profile-value">\u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440<\/span><\/div>\n                <div class="sd-profile-row sd-profile-row-balance">' + iconTicket + '<span class="sd-profile-label">\u0411\u0430\u043B\u0430\u043D\u0441 \u0442\u0430\u043B\u043E\u043D\u043E\u0432:<\/span><span class="sd-profile-value sd-balance-badge">' + user.q9_1 + '<\/span><\/div>\n            <\/div>\n        <\/div>';
    return '<h2>\u0413\u043B\u0430\u0432\u043D\u0430\u044F<\/h2>\n        ' + profileCard + '\n        <div class="sd-block"><h3 class="sd-block-title">\u041C\u043E\u0438 \u043A\u0443\u0440\u0441\u0430\u043D\u0442\u044B (' + cadets.j() + ')<\/h3><div class="sd-cadet-cards">' + cadetsListHtml + '<\/div><\/div>\n        ' + loadingLine + '\n        <div class="sd-block"><h3 class="sd-block-title">\u041C\u043E\u0439 \u0433\u0440\u0430\u0444\u0438\u043A<\/h3><div class="sd-list">' + sessList + '<\/div><\/div>\n        <p class="sd-version">\u0412\u0435\u0440\u0441\u0438\u044F: ' + version + '<\/p>';
  }
  function renderCadetHomeContent(user, version) {
    var inst = get_appState().db_1;
    var instText = !(inst == null) ? escapeHtml(inst.m9_1) : !(user.s9_1 == null) ? '\u0437\u0430\u0433\u0440\u0443\u0437\u043A\u0430\u2026' : '\u043D\u0435 \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D';
    var loading = get_appState().ma_1;
    // Inline function 'kotlin.collections.filter' call
    var tmp0 = get_appState().la_1;
    // Inline function 'kotlin.collections.filterTo' call
    var destination = ArrayList_init_$Create$();
    var _iterator__ex2g4s = tmp0.g();
    while (_iterator__ex2g4s.h()) {
      var element = _iterator__ex2g4s.i();
      if (element.jb_1 === 'scheduled') {
        destination.e(element);
      }
    }
    var sessions = take(destination, 20);
    var loadingLine = loading ? '<p class="sd-loading-text">\u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430\u2026<\/p>' : '';
    var sessList = joinToString(sessions, '', VOID, VOID, VOID, VOID, renderCadetHomeContent$lambda);
    return '<h2>\u0413\u043B\u0430\u0432\u043D\u0430\u044F<\/h2>\n        <div class="sd-home-card"><strong>\u0411\u0430\u043B\u0430\u043D\u0441:<\/strong> ' + user.q9_1 + ' \u0442\u0430\u043B\u043E\u043D\u043E\u0432.<\/div>\n        <div class="sd-home-card"><strong>\u041C\u043E\u0439 \u0438\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440:<\/strong> ' + instText + '<\/div>\n        ' + loadingLine + '\n        <div class="sd-block"><h3 class="sd-block-title">\u041C\u043E\u0451 \u0432\u043E\u0436\u0434\u0435\u043D\u0438\u0435<\/h3><div class="sd-list">' + sessList + '<\/div><\/div>\n        <p class="sd-version">\u0412\u0435\u0440\u0441\u0438\u044F: ' + version + '<\/p>';
  }
  function renderRecordingTabContent(user) {
    var loading = get_appState().ma_1;
    var windows = get_appState().ka_1;
    var sessions = get_appState().la_1;
    var loadingLine = loading ? '<p class="sd-loading-text">\u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430\u2026 <button type="button" id="sd-stop-loading" class="sd-btn sd-btn-small sd-btn-secondary">\u041F\u043E\u043A\u0430\u0437\u0430\u0442\u044C \u043F\u0443\u0441\u0442\u043E<\/button><\/p>' : '';
    var tmp;
    switch (user.p9_1) {
      case 'instructor':
        var list = joinToString(windows, '', VOID, VOID, VOID, VOID, renderRecordingTabContent$lambda);
        // Inline function 'kotlin.collections.filter' call

        // Inline function 'kotlin.collections.filterTo' call

        var destination = ArrayList_init_$Create$();
        var _iterator__ex2g4s = sessions.g();
        while (_iterator__ex2g4s.h()) {
          var element = _iterator__ex2g4s.i();
          if (element.jb_1 === 'scheduled' || element.jb_1 === 'inProgress') {
            destination.e(element);
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
        var _iterator__ex2g4s_0 = sessions.g();
        while (_iterator__ex2g4s_0.h()) {
          var element_0 = _iterator__ex2g4s_0.i();
          if (element_0.jb_1 === 'scheduled') {
            destination_0.e(element_0);
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
    var loadingLine = get_appState().pa_1 ? '<p class="sd-loading-text">\u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430\u2026 <button type="button" id="sd-stop-history-loading" class="sd-btn sd-btn-small sd-btn-secondary">\u041F\u043E\u043A\u0430\u0437\u0430\u0442\u044C \u043F\u0443\u0441\u0442\u043E<\/button><\/p>' : '';
    var sessions = take(get_appState().na_1, 30);
    var balance = take(get_appState().oa_1, 50);
    var sessionsHtml = joinToString(sessions, '', VOID, VOID, VOID, VOID, renderHistoryTabContent$lambda);
    if (user.p9_1 === 'admin') {
      var users = get_appState().ra_1;
      var balanceHtml = joinToString(balance, '', VOID, VOID, VOID, VOID, renderHistoryTabContent$lambda_0(users));
      return '<h2>\u0418\u0441\u0442\u043E\u0440\u0438\u044F<\/h2>' + loadingLine + '\n            <div class="sd-block"><h3 class="sd-block-title">\u0417\u0430\u0447\u0438\u0441\u043B\u0435\u043D\u0438\u044F \u0438 \u0441\u043F\u0438\u0441\u0430\u043D\u0438\u044F (' + balance.j() + ')<\/h3><div class="sd-list">' + balanceHtml + '<\/div><\/div>\n            <div class="sd-block"><h3 class="sd-block-title">\u0412\u043E\u0436\u0434\u0435\u043D\u0438\u0435<\/h3><p>\u0417\u0430\u0432\u0435\u0440\u0448\u0451\u043D\u043D\u044B\u0435 \u0438 \u043E\u0442\u043C\u0435\u043D\u0451\u043D\u043D\u044B\u0435 \u0432\u043E\u0436\u0434\u0435\u043D\u0438\u044F \u2014 \u0432 \u043F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u0438.<\/p><\/div>\n            <div class="sd-block"><h3 class="sd-block-title">\u0427\u0430\u0442<\/h3><p>\u041F\u0440\u043E\u0441\u043C\u043E\u0442\u0440 \u043F\u0435\u0440\u0435\u043F\u0438\u0441\u043A\u0438 \u2014 \u0432\u044B\u0431\u0440\u0430\u0442\u044C \u043A\u043E\u043D\u0442\u0430\u043A\u0442 \u0432\u043E \u0432\u043A\u043B\u0430\u0434\u043A\u0435 \u0427\u0430\u0442.<\/p><\/div>';
    }
    var balanceHtml_0 = joinToString(balance, '', VOID, VOID, VOID, VOID, renderHistoryTabContent$lambda_1);
    return '<h2>\u0418\u0441\u0442\u043E\u0440\u0438\u044F<\/h2>' + loadingLine + '<div class="sd-block"><h3 class="sd-block-title">\u0417\u0430\u043D\u044F\u0442\u0438\u044F (' + sessions.j() + ')<\/h3><div class="sd-list">' + sessionsHtml + '<\/div><\/div><div class="sd-block"><h3 class="sd-block-title">\u0411\u0430\u043B\u0430\u043D\u0441 (' + balance.j() + ')<\/h3><div class="sd-list">' + balanceHtml_0 + '<\/div><\/div>';
  }
  function renderSettingsTabContent(user) {
    return '<h2>\u041D\u0430\u0441\u0442\u0440\u043E\u0439\u043A\u0438<\/h2>\n       <label>\u0424\u0418\u041E<\/label><input type="text" id="sd-settings-fullName" class="sd-input" value="' + escapeHtml(user.m9_1) + '" />\n       <label>\u0422\u0435\u043B\u0435\u0444\u043E\u043D<\/label><input type="tel" id="sd-settings-phone" class="sd-input" value="' + escapeHtml(user.o9_1) + '" />\n       <button type="button" id="sd-settings-save" class="sd-btn sd-btn-primary">\u0421\u043E\u0445\u0440\u0430\u043D\u0438\u0442\u044C \u043F\u0440\u043E\u0444\u0438\u043B\u044C<\/button>\n       <p style="margin-top:16px">\u0421\u043C\u0435\u043D\u0438\u0442\u044C \u043F\u0430\u0440\u043E\u043B\u044C:<\/p>\n       <label>\u041D\u043E\u0432\u044B\u0439 \u043F\u0430\u0440\u043E\u043B\u044C<\/label><input type="password" id="sd-settings-newpassword" class="sd-input" placeholder="\u043C\u0438\u043D. 6 \u0441\u0438\u043C\u0432\u043E\u043B\u043E\u0432" />\n       <button type="button" id="sd-settings-password" class="sd-btn sd-btn-secondary">\u0421\u043C\u0435\u043D\u0438\u0442\u044C \u043F\u0430\u0440\u043E\u043B\u044C<\/button>';
  }
  function renderBalanceTabContent(user) {
    if (!(user.p9_1 === 'admin'))
      return '<h2>\u0411\u0430\u043B\u0430\u043D\u0441<\/h2><p>\u0412\u0430\u0448 \u0431\u0430\u043B\u0430\u043D\u0441: ' + user.q9_1 + ' \u0442\u0430\u043B\u043E\u043D\u043E\u0432.<\/p>';
    var loadingLine = get_appState().sa_1 ? '<p class="sd-loading-text">\u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430\u2026 <button type="button" id="sd-stop-balance-loading" class="sd-btn sd-btn-small sd-btn-secondary">\u041F\u043E\u043A\u0430\u0437\u0430\u0442\u044C \u043F\u0443\u0441\u0442\u043E<\/button><\/p>' : '';
    var users = get_appState().ra_1;
    var emptyBalanceBtn = !get_appState().sa_1 && users.q() ? '<p>\u0421\u043F\u0438\u0441\u043E\u043A \u043F\u0443\u0441\u0442. <button type="button" id="sd-balance-load" class="sd-btn sd-btn-primary">\u0417\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044C<\/button><\/p>' : '';
    var selectedId = get_appState().ta_1;
    // Inline function 'kotlin.collections.find' call
    var tmp$ret$1;
    $l$block: {
      // Inline function 'kotlin.collections.firstOrNull' call
      var _iterator__ex2g4s = users.g();
      while (_iterator__ex2g4s.h()) {
        var element = _iterator__ex2g4s.i();
        if (element.l9_1 === selectedId) {
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
    var _iterator__ex2g4s_0 = users.g();
    while (_iterator__ex2g4s_0.h()) {
      var element_0 = _iterator__ex2g4s_0.i();
      if (element_0.p9_1 === 'instructor') {
        destination.e(element_0);
      }
    }
    var instructors = destination;
    // Inline function 'kotlin.collections.filter' call
    // Inline function 'kotlin.collections.filterTo' call
    var destination_0 = ArrayList_init_$Create$();
    var _iterator__ex2g4s_1 = users.g();
    while (_iterator__ex2g4s_1.h()) {
      var element_1 = _iterator__ex2g4s_1.i();
      if (element_1.p9_1 === 'cadet') {
        destination_0.e(element_1);
      }
    }
    var cadets = destination_0;
    var balanceCardHtml = renderBalanceTabContent$lambda;
    var instRows = joinToString(instructors, '', VOID, VOID, VOID, VOID, renderBalanceTabContent$lambda_0(balanceCardHtml));
    var cadetRows = joinToString(cadets, '', VOID, VOID, VOID, VOID, renderBalanceTabContent$lambda_1(balanceCardHtml));
    var selectedBlock = !(selectedUser == null) ? '\n        <div class="sd-block" id="sd-balance-selected-block">\n            <h3 class="sd-block-title">\u0412\u044B\u0431\u0440\u0430\u043D<\/h3>\n            <p><strong>' + escapeHtml(selectedUser.m9_1) + '<\/strong> (' + selectedUser.p9_1 + '). \u0411\u0430\u043B\u0430\u043D\u0441: ' + selectedUser.q9_1 + ' \u0442\u0430\u043B\u043E\u043D\u043E\u0432.<\/p>\n            <label>\u041A\u043E\u043B\u0438\u0447\u0435\u0441\u0442\u0432\u043E \u0442\u0430\u043B\u043E\u043D\u043E\u0432<\/label><input type="number" id="sd-balance-amount" class="sd-input" value="0" min="0" />\n            <div class="sd-balance-selected-actions">\n                <button type="button" id="sd-balance-credit" class="sd-btn sd-balance-btn sd-balance-btn-credit">' + iconCreditSvg + ' \u0417\u0430\u0447\u0438\u0441\u043B\u0438\u0442\u044C (+N)<\/button>\n                <button type="button" id="sd-balance-debit" class="sd-btn sd-balance-btn sd-balance-btn-debit">' + iconDebitSvg + ' \u0421\u043F\u0438\u0441\u0430\u0442\u044C (\u2212N)<\/button>\n                <button type="button" id="sd-balance-set" class="sd-btn sd-balance-btn sd-balance-btn-set">' + iconSetSvg + ' \u0418\u0437\u043C\u0435\u043D\u0438\u0442\u044C \u043D\u0430 (= N)<\/button>\n                <button type="button" id="sd-balance-clear-selection" class="sd-btn sd-balance-btn sd-balance-btn-clear">' + iconResetSvg + ' \u0421\u0431\u0440\u043E\u0441\u0438\u0442\u044C \u0432\u044B\u0431\u043E\u0440<\/button>\n            <\/div>\n        <\/div>\n    ' : '';
    var history = take(get_appState().qa_1, 50);
    var typeLabel = renderBalanceTabContent$lambda_2;
    // Inline function 'kotlin.collections.sortedByDescending' call
    // Inline function 'kotlin.comparisons.compareByDescending' call
    var tmp = renderBalanceTabContent$lambda_3;
    var tmp$ret$9 = new sam$kotlin_Comparator$0_1(tmp);
    var sortedHistory = sortedWith(history, tmp$ret$9);
    // Inline function 'kotlin.collections.groupBy' call
    // Inline function 'kotlin.collections.groupByTo' call
    var destination_1 = LinkedHashMap_init_$Create$();
    var _iterator__ex2g4s_2 = sortedHistory.g();
    while (_iterator__ex2g4s_2.h()) {
      var element_2 = _iterator__ex2g4s_2.i();
      var key = substringBefore(formatDateTimeEkaterinburg(element_2.tb_1), ', ');
      // Inline function 'kotlin.collections.getOrPut' call
      var value = destination_1.l1(key);
      var tmp_0;
      if (value == null) {
        var answer = ArrayList_init_$Create$();
        destination_1.f3(key, answer);
        tmp_0 = answer;
      } else {
        tmp_0 = value;
      }
      var list = tmp_0;
      list.e(element_2);
    }
    var byDate = destination_1;
    var tmp_1 = byDate.m1();
    var historyRows = joinToString(tmp_1, '', VOID, VOID, VOID, VOID, renderBalanceTabContent$lambda_4(users, typeLabel));
    var historyEmptyMsg = history.q() ? '<p class="sd-muted sd-balance-history-empty">\u041D\u0435\u0442 \u0437\u0430\u043F\u0438\u0441\u0435\u0439. \u0412\u044B\u043F\u043E\u043B\u043D\u0438\u0442\u0435 \u0437\u0430\u0447\u0438\u0441\u043B\u0435\u043D\u0438\u0435 \u0438\u043B\u0438 \u0441\u043F\u0438\u0441\u0430\u043D\u0438\u0435 \u043F\u043E \u0432\u044B\u0431\u0440\u0430\u043D\u043D\u043E\u043C\u0443 \u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u0435\u043B\u044E \u2014 \u043E\u043F\u0435\u0440\u0430\u0446\u0438\u0438 \u043F\u043E\u044F\u0432\u044F\u0442\u0441\u044F \u0437\u0434\u0435\u0441\u044C.<\/p>' : '';
    var historyOpen = get_appState().za_1 ? ' open' : '';
    var historyDetailsContent = history.q() ? historyEmptyMsg : '<div class="sd-balance-history-by-date">' + historyRows + '<\/div>';
    var historyBlock = '<details class="sd-block sd-details-block" data-balance-section="history"' + historyOpen + '><summary class="sd-block-title">\u0418\u0441\u0442\u043E\u0440\u0438\u044F \u043E\u043F\u0435\u0440\u0430\u0446\u0438\u0439 (' + history.j() + ')<\/summary>' + historyDetailsContent + '<\/details>';
    return '<h2>\u0411\u0430\u043B\u0430\u043D\u0441<\/h2>' + loadingLine + emptyBalanceBtn + '\n        <div class="sd-block" id="sd-balance-instructors-block"><h3 class="sd-block-title">\u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440\u044B (' + instructors.j() + ')<\/h3><div id="sd-balance-instructors-list" class="sd-balance-cards">' + instRows + '<\/div><\/div>\n        <div class="sd-block" id="sd-balance-cadets-block"><h3 class="sd-block-title">\u041A\u0443\u0440\u0441\u0430\u043D\u0442\u044B (' + cadets.j() + ')<\/h3><div id="sd-balance-cadets-list" class="sd-balance-cards">' + cadetRows + '<\/div><\/div>\n        ' + selectedBlock + '\n        ' + historyBlock;
  }
  function getPanelTabButtonsAndContent(user, tabs) {
    var appInfo = SharedFactory_instance.g9().z9();
    var selected = coerceIn(get_appState().fa_1, 0, tabs.j() - 1 | 0);
    // Inline function 'kotlin.collections.mapIndexed' call
    // Inline function 'kotlin.collections.mapIndexedTo' call
    var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(tabs, 10));
    var index = 0;
    var _iterator__ex2g4s = tabs.g();
    while (_iterator__ex2g4s.h()) {
      var item = _iterator__ex2g4s.i();
      var _unary__edvuaz = index;
      index = _unary__edvuaz + 1 | 0;
      var i = checkIndexOverflow(_unary__edvuaz);
      var cls = i === selected ? 'sd-tab sd-active' : 'sd-tab';
      var tmp$ret$0 = '<button type="button" class="' + cls + '" data-tab="' + i + '">' + item + '<\/button>';
      destination.e(tmp$ret$0);
    }
    var tabButtons = joinToString(destination, '');
    var tabName = tabs.o(selected);
    var tmp;
    switch (tabName) {
      case '\u0413\u043B\u0430\u0432\u043D\u0430\u044F':
        switch (user.p9_1) {
          case 'admin':
            tmp = renderAdminHomeContent();
            break;
          case 'instructor':
            tmp = renderInstructorHomeContent(user, appInfo.i9_1);
            break;
          case 'cadet':
            tmp = renderCadetHomeContent(user, appInfo.i9_1);
            break;
          default:
            tmp = '<h2>' + tabName + '<\/h2><p>\u0411\u0430\u043B\u0430\u043D\u0441: ' + user.q9_1 + ' \u0442\u0430\u043B\u043E\u043D\u043E\u0432.<\/p><p>\u0412\u0435\u0440\u0441\u0438\u044F: ' + appInfo.i9_1 + '<\/p>';
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
    var _destruct__k2r9zo = getPanelTabButtonsAndContent(user, tabs);
    var tabButtons = _destruct__k2r9zo.x5();
    var tabContent = _destruct__k2r9zo.y5();
    return trimIndent('\n        <header class="sd-header sd-panel-header">\n            <h1>StartDrive \xB7 ' + roleTitle + '<\/h1>\n            <p>' + user.m9_1 + ' \xB7 ' + user.n9_1 + '<\/p>\n            <button type="button" id="sd-btn-signout" class="sd-btn sd-btn-signout">\u0412\u044B\u0439\u0442\u0438<\/button>\n        <\/header>\n        <nav class="sd-tabs">' + tabButtons + '<\/nav>\n        <main class="sd-content">\n            <div class="sd-card" id="sd-card">\n                ' + tabContent + '\n            <\/div>\n        <\/main>\n    ');
  }
  function setupPanelClickDelegation(root) {
    root.addEventListener('click', setupPanelClickDelegation$lambda, true);
  }
  function attachListeners(root) {
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
    switch (get_appState().aa_1.o1_1) {
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
        var tmp17_safe_receiver = get_appState().ba_1;
        var uid = tmp17_safe_receiver == null ? null : tmp17_safe_receiver.l9_1;
        if (!(get_appState().fa_1 === 2)) {
          unsubscribeChat();
        }

        var tmp = window;
        tmp.setTimeout(attachListeners$lambda_15(uid), 0);
        var u = get_appState().ba_1;
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
            tmp_6 = Unit_instance;
          }
        }

        break;
      default:
        noWhenBranchMatchedException();
        break;
    }
  }
  function sendChatMessage(chatInput, uid) {
    var tmp1_elvis_lhs = chatInput == null ? null : chatInput.value;
    // Inline function 'kotlin.text.trim' call
    var this_0 = tmp1_elvis_lhs == null ? '' : tmp1_elvis_lhs;
    var text = toString_0(trim(isCharSequence(this_0) ? this_0 : THROW_CCE()));
    if (isBlank(text) || uid == null)
      return Unit_instance;
    var tmp2_elvis_lhs = get_appState().ia_1;
    var tmp;
    if (tmp2_elvis_lhs == null) {
      return Unit_instance;
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
    this.ub_1 = function_0;
  }
  protoOf(sam$kotlin_Comparator$0).e7 = function (a, b) {
    return this.ub_1(a, b);
  };
  protoOf(sam$kotlin_Comparator$0).compare = function (a, b) {
    return this.e7(a, b);
  };
  protoOf(sam$kotlin_Comparator$0).e2 = function () {
    return this.ub_1;
  };
  protoOf(sam$kotlin_Comparator$0).equals = function (other) {
    var tmp;
    if (!(other == null) ? isInterface(other, Comparator) : false) {
      var tmp_0;
      if (!(other == null) ? isInterface(other, FunctionAdapter) : false) {
        tmp_0 = equals(this.e2(), other.e2());
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
    return hashCode(this.e2());
  };
  function sam$kotlin_Comparator$0_0(function_0) {
    this.vb_1 = function_0;
  }
  protoOf(sam$kotlin_Comparator$0_0).e7 = function (a, b) {
    return this.vb_1(a, b);
  };
  protoOf(sam$kotlin_Comparator$0_0).compare = function (a, b) {
    return this.e7(a, b);
  };
  protoOf(sam$kotlin_Comparator$0_0).e2 = function () {
    return this.vb_1;
  };
  protoOf(sam$kotlin_Comparator$0_0).equals = function (other) {
    var tmp;
    if (!(other == null) ? isInterface(other, Comparator) : false) {
      var tmp_0;
      if (!(other == null) ? isInterface(other, FunctionAdapter) : false) {
        tmp_0 = equals(this.e2(), other.e2());
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
    return hashCode(this.e2());
  };
  function sam$kotlin_Comparator$0_1(function_0) {
    this.wb_1 = function_0;
  }
  protoOf(sam$kotlin_Comparator$0_1).e7 = function (a, b) {
    return this.wb_1(a, b);
  };
  protoOf(sam$kotlin_Comparator$0_1).compare = function (a, b) {
    return this.e7(a, b);
  };
  protoOf(sam$kotlin_Comparator$0_1).e2 = function () {
    return this.wb_1;
  };
  protoOf(sam$kotlin_Comparator$0_1).equals = function (other) {
    var tmp;
    if (!(other == null) ? isInterface(other, Comparator) : false) {
      var tmp_0;
      if (!(other == null) ? isInterface(other, FunctionAdapter) : false) {
        tmp_0 = equals(this.e2(), other.e2());
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
    return hashCode(this.e2());
  };
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
    var tmp0_elvis_lhs = get_appState().ta_1;
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return Unit_instance;
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
      return Unit_instance;
    updateBalance(targetId, type, amount, $usr.l9_1, attachListeners$_anonymous_$doBalanceOp$lambda_6obd22);
  }
  function main$lambda(_unused_var__etf5q3) {
    var tmp0_elvis_lhs = document.getElementById('root');
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return Unit_instance;
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
    return Unit_instance;
  }
  function invoke$render(root, lastRenderedTabIndex) {
    var state = get_appState();
    var tmp0_safe_receiver = state.ea_1;
    var tmp;
    if (tmp0_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.let' call
      tmp = '<div class="sd-network-error" id="sd-network-error"><span>' + tmp0_safe_receiver + '<\/span> <button type="button" id="sd-dismiss-network-error" class="sd-btn-inline">\u0417\u0430\u043A\u0440\u044B\u0442\u044C<\/button><\/div>';
    }
    var tmp1_elvis_lhs = tmp;
    var networkBanner = tmp1_elvis_lhs == null ? '' : tmp1_elvis_lhs;
    var loadingOverlay = state.da_1 ? '<div class="sd-loading-overlay" id="sd-loading-overlay"><div class="sd-spinner"><\/div><p>\u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430\u2026<\/p><\/div>' : '';
    var panelScreen = state.aa_1.equals(AppScreen_Admin_getInstance()) || state.aa_1.equals(AppScreen_Instructor_getInstance()) || state.aa_1.equals(AppScreen_Cadet_getInstance());
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    var tmp_0 = root.querySelector('#sd-card');
    var sdCard = tmp_0 instanceof Element ? tmp_0 : null;
    if (panelScreen && !(state.ba_1 == null) && !(sdCard == null) && state.ea_1 == null && !state.da_1) {
      var tabs;
      switch (state.aa_1.o1_1) {
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
      if (state.aa_1.equals(AppScreen_Admin_getInstance()) && state.fa_1 === 0) {
        var newbiesDetails = sdCard.querySelector('details[data-admin-section="newbies"]');
        var instDetails = sdCard.querySelector('details[data-admin-section="instructors"]');
        var cadetDetails = sdCard.querySelector('details[data-admin-section="cadets"]');
        updateState(main$lambda$render$lambda(newbiesDetails, instDetails, cadetDetails));
      }
      if (state.aa_1.equals(AppScreen_Admin_getInstance()) && state.fa_1 === 1) {
        var historyDetails = sdCard.querySelector('details[data-balance-section="history"]');
        updateState(main$lambda$render$lambda_0(historyDetails));
      }
      var _destruct__k2r9zo = getPanelTabButtonsAndContent(ensureNotNull(state.ba_1), tabs);
      var tabButtons = _destruct__k2r9zo.x5();
      var tabContent = _destruct__k2r9zo.y5();
      if (!(lastRenderedTabIndex._v === state.fa_1)) {
        // Inline function 'kotlin.js.unsafeCast' call
        // Inline function 'kotlin.js.asDynamic' call
        var tmp_1 = root.querySelector('nav.sd-tabs');
        var tmp3_safe_receiver = tmp_1 instanceof Element ? tmp_1 : null;
        if (tmp3_safe_receiver == null)
          null;
        else {
          tmp3_safe_receiver.innerHTML = tabButtons;
        }
        lastRenderedTabIndex._v = state.fa_1;
      }
      sdCard.innerHTML = tabContent;
      attachListeners(root);
      return Unit_instance;
    }
    lastRenderedTabIndex._v = null;
    var tmp_2;
    switch (state.aa_1.o1_1) {
      case 0:
        tmp_2 = renderLogin(state.ca_1, state.da_1);
        break;
      case 1:
        tmp_2 = renderRegister(state.ca_1, state.da_1);
        break;
      case 2:
        tmp_2 = renderPendingApproval();
        break;
      case 3:
        var tmp5_elvis_lhs = state.ca_1;
        tmp_2 = renderProfileNotFound(tmp5_elvis_lhs == null ? '\u041F\u0440\u043E\u0444\u0438\u043B\u044C \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D.' : tmp5_elvis_lhs);
        break;
      case 4:
        tmp_2 = renderPanel(ensureNotNull(state.ba_1), '\u0410\u0434\u043C\u0438\u043D\u0438\u0441\u0442\u0440\u0430\u0442\u043E\u0440', listOf(['\u0413\u043B\u0430\u0432\u043D\u0430\u044F', '\u0411\u0430\u043B\u0430\u043D\u0441', '\u0427\u0430\u0442', '\u0418\u0441\u0442\u043E\u0440\u0438\u044F']));
        break;
      case 5:
        tmp_2 = renderPanel(ensureNotNull(state.ba_1), '\u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440', listOf(['\u0413\u043B\u0430\u0432\u043D\u0430\u044F', '\u0417\u0430\u043F\u0438\u0441\u044C', '\u0427\u0430\u0442', '\u0411\u0438\u043B\u0435\u0442\u044B', '\u0418\u0441\u0442\u043E\u0440\u0438\u044F', '\u041D\u0430\u0441\u0442\u0440\u043E\u0439\u043A\u0438']));
        break;
      case 6:
        tmp_2 = renderPanel(ensureNotNull(state.ba_1), '\u041A\u0443\u0440\u0441\u0430\u043D\u0442', listOf(['\u0413\u043B\u0430\u0432\u043D\u0430\u044F', '\u0417\u0430\u043F\u0438\u0441\u044C', '\u0427\u0430\u0442', '\u0411\u0438\u043B\u0435\u0442\u044B', '\u0418\u0441\u0442\u043E\u0440\u0438\u044F', '\u041D\u0430\u0441\u0442\u0440\u043E\u0439\u043A\u0438']));
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
      return Unit_instance;
    renderScheduled._v = true;
    var tmp = window;
    tmp.requestAnimationFrame(main$lambda$scheduleRender$lambda(renderScheduled, root, lastRenderedTabIndex));
  }
  function main$lambda$lambda($renderScheduled, $root, $lastRenderedTabIndex) {
    return function () {
      invoke$scheduleRender($renderScheduled, $root, $lastRenderedTabIndex);
      return Unit_instance;
    };
  }
  function main$lambda$lambda_0(uid) {
    if (uid == null) {
      updateState(main$lambda$lambda$lambda);
      return Unit_instance;
    }
    updateState(main$lambda$lambda$lambda_0);
    getCurrentUser(main$lambda$lambda$lambda_1);
    return Unit_instance;
  }
  function main$lambda$lambda$lambda($this$updateState) {
    $this$updateState.aa_1 = AppScreen_Login_getInstance();
    $this$updateState.ba_1 = null;
    $this$updateState.ca_1 = null;
    return Unit_instance;
  }
  function main$lambda$lambda$lambda_0($this$updateState) {
    $this$updateState.da_1 = true;
    $this$updateState.ca_1 = null;
    return Unit_instance;
  }
  function main$lambda$lambda$lambda_1(user, errorMsg) {
    updateState(main$lambda$lambda$lambda$lambda);
    if (user == null) {
      updateState(main$lambda$lambda$lambda$lambda_0(errorMsg));
      return Unit_instance;
    }
    updateState(main$lambda$lambda$lambda$lambda_1(user));
    updateState(main$lambda$lambda$lambda$lambda_2(user));
    return Unit_instance;
  }
  function main$lambda$lambda$lambda$lambda($this$updateState) {
    $this$updateState.da_1 = false;
    $this$updateState.ea_1 = null;
    return Unit_instance;
  }
  function main$lambda$lambda$lambda$lambda_0($errorMsg) {
    return function ($this$updateState) {
      $this$updateState.aa_1 = AppScreen_ProfileNotFound_getInstance();
      $this$updateState.ba_1 = null;
      var tmp = $this$updateState;
      var tmp0_elvis_lhs = $errorMsg;
      tmp.ca_1 = tmp0_elvis_lhs == null ? '\u041F\u0440\u043E\u0444\u0438\u043B\u044C \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D \u0432 \u0431\u0430\u0437\u0435.' : tmp0_elvis_lhs;
      return Unit_instance;
    };
  }
  function main$lambda$lambda$lambda$lambda_1($user) {
    return function ($this$updateState) {
      $this$updateState.ba_1 = $user;
      $this$updateState.ca_1 = null;
      $this$updateState.ea_1 = null;
      return Unit_instance;
    };
  }
  function main$lambda$lambda$lambda$lambda_2($user) {
    return function ($this$updateState) {
      var tmp = $this$updateState;
      switch ($user.p9_1) {
        case 'admin':
          tmp.aa_1 = AppScreen_Admin_getInstance();
          break;
        case 'instructor':
          tmp.aa_1 = $user.u9_1 ? AppScreen_Instructor_getInstance() : AppScreen_PendingApproval_getInstance();
          break;
        case 'cadet':
          tmp.aa_1 = $user.u9_1 ? AppScreen_Cadet_getInstance() : AppScreen_PendingApproval_getInstance();
          break;
        default:
          tmp.aa_1 = AppScreen_PendingApproval_getInstance();
          break;
      }
      return Unit_instance;
    };
  }
  function main$lambda$render$lambda($newbiesDetails, $instDetails, $cadetDetails) {
    return function ($this$updateState) {
      var tmp = $this$updateState;
      var tmp0_safe_receiver = $newbiesDetails;
      var tmp_0;
      if (tmp0_safe_receiver == null) {
        tmp_0 = null;
      } else {
        // Inline function 'kotlin.js.unsafeCast' call
        // Inline function 'kotlin.js.asDynamic' call
        tmp_0 = tmp0_safe_receiver;
      }
      var tmp1_safe_receiver = tmp_0;
      tmp.wa_1 = (tmp1_safe_receiver == null ? null : tmp1_safe_receiver.open) == true;
      var tmp_1 = $this$updateState;
      var tmp2_safe_receiver = $instDetails;
      var tmp_2;
      if (tmp2_safe_receiver == null) {
        tmp_2 = null;
      } else {
        // Inline function 'kotlin.js.unsafeCast' call
        // Inline function 'kotlin.js.asDynamic' call
        tmp_2 = tmp2_safe_receiver;
      }
      var tmp3_safe_receiver = tmp_2;
      tmp_1.xa_1 = (tmp3_safe_receiver == null ? null : tmp3_safe_receiver.open) == true;
      var tmp_3 = $this$updateState;
      var tmp4_safe_receiver = $cadetDetails;
      var tmp_4;
      if (tmp4_safe_receiver == null) {
        tmp_4 = null;
      } else {
        // Inline function 'kotlin.js.unsafeCast' call
        // Inline function 'kotlin.js.asDynamic' call
        tmp_4 = tmp4_safe_receiver;
      }
      var tmp5_safe_receiver = tmp_4;
      tmp_3.ya_1 = (tmp5_safe_receiver == null ? null : tmp5_safe_receiver.open) == true;
      return Unit_instance;
    };
  }
  function main$lambda$render$lambda_0($historyDetails) {
    return function ($this$updateState) {
      var tmp = $this$updateState;
      var tmp0_safe_receiver = $historyDetails;
      var tmp_0;
      if (tmp0_safe_receiver == null) {
        tmp_0 = null;
      } else {
        // Inline function 'kotlin.js.unsafeCast' call
        // Inline function 'kotlin.js.asDynamic' call
        tmp_0 = tmp0_safe_receiver;
      }
      var tmp1_safe_receiver = tmp_0;
      tmp.za_1 = (tmp1_safe_receiver == null ? null : tmp1_safe_receiver.open) == true;
      return Unit_instance;
    };
  }
  function main$lambda$scheduleRender$lambda($renderScheduled, $root, $lastRenderedTabIndex) {
    return function (it) {
      invoke$render($root, $lastRenderedTabIndex);
      $renderScheduled._v = false;
      return Unit_instance;
    };
  }
  function renderChatTabContent$lambda($myId, $currentUser, $contact) {
    return function (msg) {
      var isMe = msg.yb_1 === $myId;
      var cls = isMe ? 'sd-msg sd-msg-me' : 'sd-msg sd-msg-them';
      var name = isMe ? $currentUser.x9() : $contact.x9();
      return '<div class="' + cls + '"><span class="sd-msg-name">' + name + '<\/span><span class="sd-msg-text">' + escapeHtml(msg.zb_1) + '<\/span><\/div>';
    };
  }
  function renderChatTabContent$lambda_0(c) {
    return '<button type="button" class="sd-chat-contact" data-contact-id="' + escapeHtml(c.l9_1) + '">' + escapeHtml(c.m9_1) + ' \xB7 ' + c.p9_1 + '<\/button>';
  }
  function renderAdminHomeContent$lambda(a, b) {
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.m9_1;
    var tmp$ret$1 = b.m9_1;
    return compareValues(tmp, tmp$ret$1);
  }
  function renderAdminHomeContent$lambda_0(a, b) {
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.m9_1;
    var tmp$ret$1 = b.m9_1;
    return compareValues(tmp, tmp$ret$1);
  }
  function renderAdminHomeContent$lambda_1(a, b) {
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.m9_1;
    var tmp$ret$1 = b.m9_1;
    return compareValues(tmp, tmp$ret$1);
  }
  function renderAdminHomeContent$lambda_2(u) {
    var roleLabel = u.p9_1 === 'instructor' ? '\u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440' : '\u041A\u0443\u0440\u0441\u0430\u043D\u0442';
    // Inline function 'kotlin.text.ifBlank' call
    var this_0 = u.m9_1;
    var tmp;
    if (isBlank(this_0)) {
      tmp = '\u0418\u043C\u044F \u043D\u0435 \u0443\u043A\u0430\u0437\u0430\u043D\u043E';
    } else {
      tmp = this_0;
    }
    var tmp$ret$1 = tmp;
    return '<div class="sd-admin-card sd-admin-card-pending">\n            <div class="sd-admin-card-info">\n                <p class="sd-admin-card-name">' + escapeHtml(tmp$ret$1) + '<\/p>\n                <p class="sd-admin-card-meta">' + escapeHtml(u.n9_1) + '<\/p>\n                <p class="sd-admin-card-meta">' + escapeHtml(u.o9_1) + '<\/p>\n                <p class="sd-admin-card-meta"><span class="sd-admin-role-label">\u0420\u043E\u043B\u044C \u043F\u0440\u0438 \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u0438:<\/span> ' + roleLabel + '<\/p>\n            <\/div>\n            <div class="sd-admin-card-actions">\n                <button type="button" class="sd-btn sd-btn-small sd-btn-primary" data-admin-activate="' + escapeHtml(u.l9_1) + '" title="\u0410\u043A\u0442\u0438\u0432\u0438\u0440\u043E\u0432\u0430\u0442\u044C \u2014 \u043F\u0435\u0440\u0435\u0432\u0435\u0434\u0451\u0442 \u0432 \u0440\u0430\u0437\u0434\u0435\u043B \xAB' + roleLabel + '\xBB">\u0410\u043A\u0442\u0438\u0432\u0438\u0440\u043E\u0432\u0430\u0442\u044C<\/button>\n                <button type="button" class="sd-btn sd-btn-small sd-btn-delete" data-admin-delete="' + escapeHtml(u.l9_1) + '" title="\u0423\u0434\u0430\u043B\u0438\u0442\u044C">\u0423\u0434\u0430\u043B\u0438\u0442\u044C<\/button>\n            <\/div>\n        <\/div>';
  }
  function renderAdminHomeContent$lambda_3(a, b) {
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.m9_1;
    var tmp$ret$1 = b.m9_1;
    return compareValues(tmp, tmp$ret$1);
  }
  function renderAdminHomeContent$lambda_4(c) {
    return '<div class="sd-assign-section-row sd-assign-section-row-current"><span class="sd-assign-section-name">' + escapeHtml(formatShortName(c.m9_1)) + '<\/span><\/div>';
  }
  function renderAdminHomeContent$lambda_5(a, b) {
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.m9_1;
    var tmp$ret$1 = b.m9_1;
    return compareValues(tmp, tmp$ret$1);
  }
  function renderAdminHomeContent$lambda_6(a, b) {
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.m9_1;
    var tmp$ret$1 = b.m9_1;
    return compareValues(tmp, tmp$ret$1);
  }
  function renderAdminHomeContent$lambda_7($assignInstructorId) {
    return function (c) {
      return '<div class="sd-assign-section-row"><span class="sd-assign-section-name">' + escapeHtml(formatShortName(c.m9_1)) + '<\/span><button type="button" class="sd-btn sd-btn-small sd-btn-primary" data-admin-assign-instructor="' + escapeHtml($assignInstructorId) + '" data-admin-assign-cadet="' + escapeHtml(c.l9_1) + '">\u041D\u0430\u0437\u043D\u0430\u0447\u0438\u0442\u044C<\/button><\/div>';
    };
  }
  function renderAdminHomeContent$lambda$lambda(a, b) {
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.m9_1;
    var tmp$ret$1 = b.m9_1;
    return compareValues(tmp, tmp$ret$1);
  }
  function renderAdminHomeContent$lambda$lambda_0($assignInstructorId) {
    return function (c) {
      return '<div class="sd-assign-section-row"><span class="sd-assign-section-name">' + escapeHtml(formatShortName(c.m9_1)) + '<\/span><button type="button" class="sd-btn sd-btn-small sd-btn-primary" data-admin-assign-instructor="' + escapeHtml($assignInstructorId) + '" data-admin-assign-cadet="' + escapeHtml(c.l9_1) + '">\u041D\u0430\u0437\u043D\u0430\u0447\u0438\u0442\u044C<\/button><\/div>';
    };
  }
  function renderAdminHomeContent$lambda_8($cadets, $assignInstructorId) {
    return function (other) {
      // Inline function 'kotlin.collections.filter' call
      var tmp0 = $cadets;
      // Inline function 'kotlin.collections.filterTo' call
      var destination = ArrayList_init_$Create$();
      var _iterator__ex2g4s = tmp0.g();
      while (_iterator__ex2g4s.h()) {
        var element = _iterator__ex2g4s.i();
        if (element.s9_1 === other.l9_1) {
          destination.e(element);
        }
      }
      // Inline function 'kotlin.collections.sortedBy' call
      // Inline function 'kotlin.comparisons.compareBy' call
      var tmp = renderAdminHomeContent$lambda$lambda;
      var tmp$ret$3 = new sam$kotlin_Comparator$0(tmp);
      var otherCadets = sortedWith(destination, tmp$ret$3);
      var rows = joinToString(otherCadets, '', VOID, VOID, VOID, VOID, renderAdminHomeContent$lambda$lambda_0($assignInstructorId));
      var otherShort = escapeHtml(formatShortName(other.m9_1));
      return '<div class="sd-assign-section"><h4 class="sd-assign-section-title">\u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440 ' + otherShort + ' (' + otherCadets.j() + ')<\/h4><div class="sd-assign-section-list">' + (otherCadets.q() ? '<p class="sd-muted">\u041D\u0435\u0442 \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D\u043D\u044B\u0445 \u043A\u0443\u0440\u0441\u0430\u043D\u0442\u043E\u0432.<\/p>' : rows) + '<\/div><\/div>';
    };
  }
  function renderAdminHomeContent$lambda_9($assignCadetId) {
    return function (inst) {
      return '<div class="sd-assign-section-row"><span class="sd-assign-section-name">' + escapeHtml(formatShortName(inst.m9_1)) + '<\/span><button type="button" class="sd-btn sd-btn-small sd-btn-primary" data-admin-assign-instructor="' + escapeHtml(inst.l9_1) + '" data-admin-assign-cadet="' + escapeHtml($assignCadetId) + '">\u041D\u0430\u0437\u043D\u0430\u0447\u0438\u0442\u044C<\/button><\/div>';
    };
  }
  function renderAdminHomeContent$lambda$lambda_1(a, b) {
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.m9_1;
    var tmp$ret$1 = b.m9_1;
    return compareValues(tmp, tmp$ret$1);
  }
  function renderAdminHomeContent$lambda_10($cadets) {
    return function (u) {
      // Inline function 'kotlin.collections.filter' call
      var tmp0 = $cadets;
      // Inline function 'kotlin.collections.filterTo' call
      var destination = ArrayList_init_$Create$();
      var _iterator__ex2g4s = tmp0.g();
      while (_iterator__ex2g4s.h()) {
        var element = _iterator__ex2g4s.i();
        if (element.s9_1 === u.l9_1) {
          destination.e(element);
        }
      }
      // Inline function 'kotlin.collections.sortedBy' call
      // Inline function 'kotlin.comparisons.compareBy' call
      var tmp = renderAdminHomeContent$lambda$lambda_1;
      var tmp$ret$3 = new sam$kotlin_Comparator$0(tmp);
      var assignedCadets = sortedWith(destination, tmp$ret$3);
      var cadetsRow = '<div class="sd-admin-card-row-label sd-admin-card-row-cadets"><span class="sd-admin-card-label-icon">' + iconInstructorSvg + '<\/span>\u041A\u0443\u0440\u0441\u0430\u043D\u0442\u044B: ' + assignedCadets.j() + '<button type="button" class="sd-btn-inline sd-instructor-cadets-toggle" data-instructor-cadets-modal="' + escapeHtml(u.l9_1) + '">\u041F\u043E\u0441\u043C\u043E\u0442\u0440\u0435\u0442\u044C<\/button><\/div>';
      var tmp_0;
      // Inline function 'kotlin.text.isNotBlank' call
      var this_0 = u.o9_1;
      if (!isBlank(this_0)) {
        tmp_0 = 'tel:' + escapeHtml(u.o9_1);
      } else {
        tmp_0 = '#';
      }
      var phoneHrefInst = tmp_0;
      var tmp_1;
      // Inline function 'kotlin.text.isNotBlank' call
      var this_1 = u.o9_1;
      if (!isBlank(this_1)) {
        tmp_1 = 'sd-btn sd-btn-icon sd-btn-icon-right';
      } else {
        tmp_1 = 'sd-btn sd-btn-icon sd-btn-icon-right sd-btn-disabled';
      }
      var phoneClassInst = tmp_1;
      var tmp_2 = escapeHtml(u.m9_1);
      var tmp_3 = escapeHtml(u.l9_1);
      // Inline function 'kotlin.text.ifBlank' call
      var this_2 = u.o9_1;
      var tmp_4;
      if (isBlank(this_2)) {
        tmp_4 = '\u2014';
      } else {
        tmp_4 = this_2;
      }
      var tmp$ret$8 = tmp_4;
      return '<div class="sd-admin-card sd-admin-card-instructor">\n            <div class="sd-admin-card-body">\n                <div class="sd-admin-card-row-main">\n                    <p class="sd-admin-card-fio"><span class="sd-admin-card-label-icon">' + iconUserSvg + '<\/span>' + tmp_2 + '<\/p>\n                    <div class="sd-admin-card-icons">\n                        <a href="' + phoneHrefInst + '" class="' + phoneClassInst + '" title="\u041F\u043E\u0437\u0432\u043E\u043D\u0438\u0442\u044C">' + iconPhoneSvg + '<\/a>\n                        <button type="button" class="sd-btn sd-btn-icon sd-btn-icon-right sd-admin-open-chat" data-contact-id="' + tmp_3 + '" title="\u0427\u0430\u0442">' + iconChatSvg + '<\/button>\n                    <\/div>\n                <\/div>\n                <p class="sd-admin-card-row-label"><span class="sd-admin-card-label-icon">' + iconPhoneLabelSvg + '<\/span>\u0422\u0435\u043B.: ' + escapeHtml(tmp$ret$8) + '<\/p>\n                <p class="sd-admin-card-row-label"><span class="sd-admin-card-label-icon">' + iconTicketSvg + '<\/span>\u0411\u0430\u043B\u0430\u043D\u0441 \u0442\u0430\u043B\u043E\u043D\u043E\u0432: ' + u.q9_1 + '<\/p>\n                ' + cadetsRow + '\n            <\/div>\n            <div class="sd-admin-card-footer">\n                <button type="button" class="sd-btn sd-btn-small" data-admin-assign="' + escapeHtml(u.l9_1) + '">' + iconUserPlusSvg + ' \u041D\u0430\u0437\u043D\u0430\u0447\u0438\u0442\u044C \u043A\u0443\u0440\u0441\u0430\u043D\u0442\u0430<\/button>\n                <button type="button" class="sd-btn sd-btn-small" data-admin-activate="' + escapeHtml(u.l9_1) + '" data-admin-active="' + u.u9_1 + '">' + iconPowerSvg + ' ' + (u.u9_1 ? '\u0414\u0435\u0430\u043A\u0442\u0438\u0432\u0438\u0440\u043E\u0432\u0430\u0442\u044C' : '\u0410\u043A\u0442\u0438\u0432\u0438\u0440\u043E\u0432\u0430\u0442\u044C') + '<\/button>\n                <button type="button" class="sd-btn sd-btn-small sd-btn-delete" data-admin-delete="' + escapeHtml(u.l9_1) + '">' + iconTrashSvg + ' \u0423\u0434\u0430\u043B\u0438\u0442\u044C<\/button>\n            <\/div>\n        <\/div>';
    };
  }
  function renderAdminHomeContent$lambda_11($instructors) {
    return function (u) {
      var instId = u.s9_1;
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
          var _iterator__ex2g4s = tmp0.g();
          while (_iterator__ex2g4s.h()) {
            var element = _iterator__ex2g4s.i();
            if (element.l9_1 === instId) {
              tmp$ret$1 = element;
              break $l$block;
            }
          }
          tmp$ret$1 = null;
        }
        var tmp0_safe_receiver = tmp$ret$1;
        var tmp1_elvis_lhs = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.m9_1;
        tmp = tmp1_elvis_lhs == null ? '\u2014' : tmp1_elvis_lhs;
      }
      var tmp1_elvis_lhs_0 = tmp;
      var instName = tmp1_elvis_lhs_0 == null ? '\u2014' : tmp1_elvis_lhs_0;
      var displayInstText = !(instId == null) ? instName : '\u041D\u0435 \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D';
      var tmp_0;
      // Inline function 'kotlin.text.isNotBlank' call
      var this_0 = u.o9_1;
      if (!isBlank(this_0)) {
        tmp_0 = 'tel:' + escapeHtml(u.o9_1);
      } else {
        tmp_0 = '#';
      }
      var phoneHrefCadet = tmp_0;
      var tmp_1;
      // Inline function 'kotlin.text.isNotBlank' call
      var this_1 = u.o9_1;
      if (!isBlank(this_1)) {
        tmp_1 = 'sd-btn sd-btn-icon sd-btn-icon-right';
      } else {
        tmp_1 = 'sd-btn sd-btn-icon sd-btn-icon-right sd-btn-disabled';
      }
      var phoneClassCadet = tmp_1;
      var unlinkOrAssign = !(instId == null) ? '<button type="button" class="sd-btn sd-btn-small sd-admin-unlink-right" data-admin-unlink-instructor="' + escapeHtml(instId) + '" data-admin-unlink-cadet="' + escapeHtml(u.l9_1) + '">' + iconUnlinkSvg + ' \u041E\u0442\u0432\u044F\u0437\u0430\u0442\u044C<\/button>' : '<button type="button" class="sd-btn sd-btn-small sd-btn-primary sd-admin-assign-cadet-btn" data-admin-assign-cadet="' + escapeHtml(u.l9_1) + '">\u041D\u0430\u0437\u043D\u0430\u0447\u0438\u0442\u044C<\/button>';
      var instructorRow = '<div class="sd-admin-card-row-label sd-admin-card-row-instructor"><span class="sd-admin-card-label-icon">' + iconInstructorSvg + '<\/span>\u0418\u043D\u0441\u0442\u0440\u0443\u043A\u0442\u043E\u0440: ' + displayInstText + unlinkOrAssign + '<\/div>';
      var footerButtons = '<button type="button" class="sd-btn sd-btn-small" data-admin-activate="' + escapeHtml(u.l9_1) + '" data-admin-active="' + u.u9_1 + '">' + iconPowerSvg + ' ' + (u.u9_1 ? '\u0414\u0435\u0430\u043A\u0442\u0438\u0432\u0438\u0440\u043E\u0432\u0430\u0442\u044C' : '\u0410\u043A\u0442\u0438\u0432\u0438\u0440\u043E\u0432\u0430\u0442\u044C') + '<\/button><button type="button" class="sd-btn sd-btn-small sd-btn-delete" data-admin-delete="' + escapeHtml(u.l9_1) + '">' + iconTrashSvg + ' \u0423\u0434\u0430\u043B\u0438\u0442\u044C<\/button>';
      var tmp_2 = escapeHtml(u.m9_1);
      var tmp_3 = escapeHtml(u.l9_1);
      // Inline function 'kotlin.text.ifBlank' call
      var this_2 = u.o9_1;
      var tmp_4;
      if (isBlank(this_2)) {
        tmp_4 = '\u2014';
      } else {
        tmp_4 = this_2;
      }
      var tmp$ret$8 = tmp_4;
      return '<div class="sd-admin-card sd-admin-card-cadet">\n            <div class="sd-admin-card-body">\n                <div class="sd-admin-card-row-main">\n                    <p class="sd-admin-card-fio"><span class="sd-admin-card-label-icon">' + iconUserSvg + '<\/span>' + tmp_2 + '<\/p>\n                    <div class="sd-admin-card-icons">\n                        <a href="' + phoneHrefCadet + '" class="' + phoneClassCadet + '" title="\u041F\u043E\u0437\u0432\u043E\u043D\u0438\u0442\u044C">' + iconPhoneSvg + '<\/a>\n                        <button type="button" class="sd-btn sd-btn-icon sd-btn-icon-right sd-admin-open-chat" data-contact-id="' + tmp_3 + '" title="\u0427\u0430\u0442">' + iconChatSvg + '<\/button>\n                    <\/div>\n                <\/div>\n                <p class="sd-admin-card-row-label"><span class="sd-admin-card-label-icon">' + iconPhoneLabelSvg + '<\/span>\u0422\u0435\u043B.: ' + escapeHtml(tmp$ret$8) + '<\/p>\n                ' + instructorRow + '\n            <\/div>\n            <div class="sd-admin-card-footer">' + footerButtons + '<\/div>\n        <\/div>';
    };
  }
  function renderAdminHomeContent$lambda_12(a, b) {
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.m9_1;
    var tmp$ret$1 = b.m9_1;
    return compareValues(tmp, tmp$ret$1);
  }
  function renderAdminHomeContent$lambda_13(c) {
    return '<li class="sd-instructor-cadet-name">' + escapeHtml(formatShortName(c.m9_1)) + '<\/li>';
  }
  function renderInstructorHomeContent$lambda(a, b) {
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.m9_1;
    var tmp$ret$1 = b.m9_1;
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
          tmp = tmp0.q();
        } else {
          tmp = false;
        }
        if (tmp) {
          tmp$ret$0 = 0;
          break $l$block;
        }
        var count = 0;
        var _iterator__ex2g4s = tmp0.g();
        while (_iterator__ex2g4s.h()) {
          var element = _iterator__ex2g4s.i();
          if (element.hb_1 === c.l9_1 && element.jb_1 === 'completed') {
            count = count + 1 | 0;
            checkCountOverflow(count);
          }
        }
        tmp$ret$0 = count;
      }
      var completedCount = tmp$ret$0;
      // Inline function 'kotlin.collections.mapNotNull' call
      var tmp0_0 = take(split(c.m9_1, [' ']), 2);
      // Inline function 'kotlin.collections.mapNotNullTo' call
      var destination = ArrayList_init_$Create$();
      // Inline function 'kotlin.collections.forEach' call
      var _iterator__ex2g4s_0 = tmp0_0.g();
      while (_iterator__ex2g4s_0.h()) {
        var element_0 = _iterator__ex2g4s_0.i();
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
          destination.e(tmp0_safe_receiver_0);
        }
      }
      var initials = joinToString(destination, '');
      // Inline function 'kotlin.text.ifBlank' call
      var this_0 = c.o9_1;
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
      var this_1 = c.o9_1;
      if (!isBlank(this_1)) {
        tmp_3 = 'tel:' + escapeHtml(c.o9_1);
      } else {
        tmp_3 = '#';
      }
      var phoneHref = tmp_3;
      var tmp_4;
      // Inline function 'kotlin.text.isNotBlank' call
      var this_2 = c.o9_1;
      if (!isBlank(this_2)) {
        tmp_4 = 'sd-btn sd-btn-circle sd-btn-phone';
      } else {
        tmp_4 = 'sd-btn sd-btn-circle sd-btn-phone sd-btn-disabled';
      }
      var phoneClass = tmp_4;
      return '<div class="sd-cadet-card">\n            <p class="sd-cadet-card-title">\u041A\u0430\u0440\u0442\u043E\u0447\u043A\u0430 \u043A\u0443\u0440\u0441\u0430\u043D\u0442\u0430:<\/p>\n            <div class="sd-cadet-card-body">\n                <div class="sd-cadet-avatar">' + initials + '<\/div>\n                <div class="sd-cadet-info">\n                    <p class="sd-cadet-name">' + escapeHtml(c.m9_1) + '<\/p>\n                    <p class="sd-cadet-row"><span class="sd-cadet-label">\u0422\u0435\u043B\u0435\u0444\u043E\u043D:<\/span> ' + phoneDisplay + '<\/p>\n                    <p class="sd-cadet-row"><span class="sd-cadet-label">\u0412\u043E\u0436\u0434\u0435\u043D\u0438\u0439:<\/span> ' + completedCount + '<\/p>\n                    <p class="sd-cadet-row"><span class="sd-cadet-label">\u0411\u0430\u043B\u0430\u043D\u0441:<\/span> ' + c.q9_1 + ' \u0442\u0430\u043B\u043E\u043D\u043E\u0432<\/p>\n                <\/div>\n            <\/div>\n            <div class="sd-cadet-card-actions">\n                <button type="button" class="sd-btn sd-btn-circle sd-btn-chat sd-cadet-chat-btn" data-contact-id="' + escapeHtml(c.l9_1) + '" title="\u0427\u0430\u0442">\u0427\u0430\u0442<\/button>\n                <a href="' + phoneHref + '" class="' + phoneClass + '" title="\u041F\u043E\u0437\u0432\u043E\u043D\u0438\u0442\u044C">\u0422\u0435\u043B\u0435\u0444\u043E\u043D<\/a>\n            <\/div>\n        <\/div>';
    };
  }
  function renderInstructorHomeContent$lambda_1(s) {
    return '<div class="sd-record-row"><span>' + formatDateTime(s.ib_1) + '<\/span> \u2014 ' + s.jb_1 + '<\/div>';
  }
  function renderCadetHomeContent$lambda(it) {
    return '<div class="sd-record-row">' + formatDateTime(it.ib_1) + ' \u2014 \u0437\u0430\u043F\u043B\u0430\u043D\u0438\u0440\u043E\u0432\u0430\u043D\u043E<\/div>';
  }
  function renderRecordingTabContent$lambda(w) {
    var dt = formatDateTime(w.fc_1);
    var status = w.gc_1 === 'booked' ? ' (\u0437\u0430\u0431\u0440\u043E\u043D\u0438\u0440\u043E\u0432\u0430\u043D\u043E)' : '';
    return '<div class="sd-record-row"><span>' + dt + '<\/span> ' + status + ' <button type="button" class="sd-btn sd-btn-small sd-btn-delete" data-window-id="' + escapeHtml(w.cc_1) + '">\u0423\u0434\u0430\u043B\u0438\u0442\u044C<\/button><\/div>';
  }
  function renderRecordingTabContent$lambda_0(s) {
    return '<div class="sd-record-row"><span>' + formatDateTime(s.ib_1) + '<\/span> \u2014 ' + s.jb_1 + '<\/div>';
  }
  function renderRecordingTabContent$lambda_1(w) {
    return '<div class="sd-record-row"><span>' + formatDateTime(w.fc_1) + '<\/span> <button type="button" class="sd-btn sd-btn-primary sd-btn-small" data-window-id="' + escapeHtml(w.cc_1) + '">\u0417\u0430\u043F\u0438\u0441\u0430\u0442\u044C\u0441\u044F<\/button><\/div>';
  }
  function renderRecordingTabContent$lambda_2(it) {
    return '<div class="sd-record-row">' + formatDateTime(it.ib_1) + ' \u2014 ' + it.jb_1 + '<\/div>';
  }
  function renderHistoryTabContent$lambda(s) {
    return '<div class="sd-record-row"><span>' + formatDateTime(s.ib_1) + '<\/span> ' + s.jb_1 + ' ' + (s.kb_1 > 0 ? '\u2605' + s.kb_1 : '') + '<\/div>';
  }
  function renderHistoryTabContent$lambda_0($users) {
    return function (b) {
      // Inline function 'kotlin.collections.find' call
      var tmp0 = $users;
      var tmp$ret$1;
      $l$block: {
        // Inline function 'kotlin.collections.firstOrNull' call
        var _iterator__ex2g4s = tmp0.g();
        while (_iterator__ex2g4s.h()) {
          var element = _iterator__ex2g4s.i();
          if (element.l9_1 === b.pb_1) {
            tmp$ret$1 = element;
            break $l$block;
          }
        }
        tmp$ret$1 = null;
      }
      var tmp0_safe_receiver = tmp$ret$1;
      var tmp1_elvis_lhs = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.m9_1;
      var name = tmp1_elvis_lhs == null ? take_0(b.pb_1, 8) + '\u2026' : tmp1_elvis_lhs;
      var typeStr;
      switch (b.rb_1) {
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
      return '<div class="sd-record-row"><span>' + formatDateTime(b.tb_1) + '<\/span> ' + name + ' \u2014 ' + typeStr + b.qb_1 + '<\/div>';
    };
  }
  function renderHistoryTabContent$lambda_1(b) {
    var typeStr;
    switch (b.rb_1) {
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
    return '<div class="sd-record-row"><span>' + formatDateTime(b.tb_1) + '<\/span> ' + typeStr + b.qb_1 + '<\/div>';
  }
  function renderBalanceTabContent$lambda(u) {
    return '<div class="sd-balance-card">\n            <div class="sd-balance-card-body">\n                <p class="sd-balance-card-row"><span class="sd-balance-card-label">' + iconUserSvg + ' \u0424\u0418\u041E:<\/span> ' + escapeHtml(u.m9_1) + '<\/p>\n                <p class="sd-balance-card-row"><span class="sd-balance-card-label">' + iconTicketSvg + ' \u0411\u0430\u043B\u0430\u043D\u0441 \u0442\u0430\u043B\u043E\u043D\u043E\u0432:<\/span> ' + u.q9_1 + '<\/p>\n            <\/div>\n            <div class="sd-balance-card-action">\n                <button type="button" class="sd-btn sd-btn-select" data-balance-select="' + escapeHtml(u.l9_1) + '">' + iconSelectSvg + ' \u0412\u044B\u0431\u0440\u0430\u0442\u044C<\/button>\n            <\/div>\n        <\/div>';
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
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp0_elvis_lhs = b.tb_1;
    var tmp = tmp0_elvis_lhs == null ? new Long(0, 0) : tmp0_elvis_lhs;
    var tmp0_elvis_lhs_0 = a.tb_1;
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
        var _iterator__ex2g4s = tmp0.g();
        while (_iterator__ex2g4s.h()) {
          var element = _iterator__ex2g4s.i();
          if (element.l9_1 === b.pb_1) {
            tmp$ret$1 = element;
            break $l$block;
          }
        }
        tmp$ret$1 = null;
      }
      var tmp0_safe_receiver = tmp$ret$1;
      var tmp1_elvis_lhs = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.m9_1;
      var userName = tmp1_elvis_lhs == null ? take_0(b.pb_1, 8) + '\u2026' : tmp1_elvis_lhs;
      var label = $typeLabel(b.rb_1);
      var tail = '' + b.qb_1 + ' ' + renderBalanceTabContent$ticketWord(b.qb_1);
      // Inline function 'kotlin.text.ifEmpty' call
      var this_0 = substringAfter(formatDateTimeEkaterinburg(b.tb_1), ', ');
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
      var dateStr = _destruct__k2r9zo.i1();
      // Inline function 'kotlin.collections.component2' call
      var entries = _destruct__k2r9zo.j1();
      var rows = joinToString(entries, '', VOID, VOID, VOID, VOID, renderBalanceTabContent$lambda$lambda($users, $typeLabel));
      return '<div class="sd-balance-history-day"><p class="sd-balance-history-day-title">' + dateStr + '<\/p><div class="sd-balance-history-day-list">' + rows + '<\/div><\/div>';
    };
  }
  function setupPanelClickDelegation$lambda(e) {
    if (!get_appState().aa_1.equals(AppScreen_Admin_getInstance()) && !get_appState().aa_1.equals(AppScreen_Instructor_getInstance()) && !get_appState().aa_1.equals(AppScreen_Cadet_getInstance()))
      return Unit_instance;
    var tmp = e == null ? null : e.target;
    var tmp1_elvis_lhs = tmp instanceof Element ? tmp : null;
    var tmp_0;
    if (tmp1_elvis_lhs == null) {
      return Unit_instance;
    } else {
      tmp_0 = tmp1_elvis_lhs;
    }
    var target = tmp_0;
    // Inline function 'kotlin.js.unsafeCast' call
    var closestHelper = function (el, sel) {
      return el && el.closest ? el.closest(sel) : null;
    };
    var closest = setupPanelClickDelegation$lambda$lambda(closestHelper, target);
    var cadetsToggleBtn = closest('.sd-instructor-cadets-toggle');
    if (!(cadetsToggleBtn == null)) {
      var tmp2_elvis_lhs = cadetsToggleBtn.getAttribute('data-instructor-cadets-modal');
      var tmp_1;
      if (tmp2_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp_1 = tmp2_elvis_lhs;
      }
      var instId = tmp_1;
      updateState(setupPanelClickDelegation$lambda$lambda_0(instId));
      e.preventDefault();
      var tmp3_safe_receiver = e instanceof Event ? e : null;
      if (tmp3_safe_receiver == null)
        null;
      else {
        tmp3_safe_receiver.stopPropagation();
      }
      return Unit_instance;
    }
    var modalCloseBtn = closest('#sd-admin-cadets-modal-close');
    if (!(modalCloseBtn == null)) {
      updateState(setupPanelClickDelegation$lambda$lambda_1);
      e.preventDefault();
      var tmp4_safe_receiver = e instanceof Event ? e : null;
      if (tmp4_safe_receiver == null)
        null;
      else {
        tmp4_safe_receiver.stopPropagation();
      }
      return Unit_instance;
    }
    var modalOverlay = target.id === 'sd-admin-cadets-modal-overlay' ? target : null;
    if (!(modalOverlay == null)) {
      updateState(setupPanelClickDelegation$lambda$lambda_2);
      e.preventDefault();
      var tmp5_safe_receiver = e instanceof Event ? e : null;
      if (tmp5_safe_receiver == null)
        null;
      else {
        tmp5_safe_receiver.stopPropagation();
      }
      return Unit_instance;
    }
    var tabBtn = closest('.sd-tab');
    if (!(tabBtn == null)) {
      var tmp6_safe_receiver = tabBtn.getAttribute('data-tab');
      var tmp7_elvis_lhs = tmp6_safe_receiver == null ? null : toIntOrNull(tmp6_safe_receiver);
      var tmp_2;
      if (tmp7_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp_2 = tmp7_elvis_lhs;
      }
      var idx = tmp_2;
      if (!(idx === 2)) {
        updateState(setupPanelClickDelegation$lambda$lambda_3);
        unsubscribeChat();
      }
      updateState(setupPanelClickDelegation$lambda$lambda_4(idx));
      var tmp8_elvis_lhs = get_appState().ba_1;
      var tmp_3;
      if (tmp8_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp_3 = tmp8_elvis_lhs;
      }
      var user = tmp_3;
      switch (idx) {
        case 0:
          if (user.p9_1 === 'admin' && !get_appState().va_1) {
            updateState(setupPanelClickDelegation$lambda$lambda_5);
            var tmp_4 = window;
            var tid = tmp_4.setTimeout(setupPanelClickDelegation$lambda$lambda_6, 8000);
            getUsersWithError(setupPanelClickDelegation$lambda$lambda_7(tid));
          }

          break;
        case 1:
          if (user.p9_1 === 'admin' && !get_appState().sa_1) {
            updateState(setupPanelClickDelegation$lambda$lambda_8);
            var tmp_5 = window;
            var tid_0 = tmp_5.setTimeout(setupPanelClickDelegation$lambda$lambda_9, 8000);
            getUsers(setupPanelClickDelegation$lambda$lambda_10(tid_0));
          }

          break;
        case 2:
          if (get_appState().ga_1.q() && !get_appState().ha_1) {
            updateState(setupPanelClickDelegation$lambda$lambda_11);
            var tmp_6 = window;
            var chatTid = tmp_6.setTimeout(setupPanelClickDelegation$lambda$lambda_12, 5000);
            getUsersForChat(user, setupPanelClickDelegation$lambda$lambda_13(chatTid));
          }

          break;
        default:
          break;
      }
      if ((idx === 0 || idx === 1) && (user.p9_1 === 'instructor' || user.p9_1 === 'cadet') && !get_appState().ma_1) {
        updateState(setupPanelClickDelegation$lambda$lambda_14);
        var tmp_7 = window;
        var tid_1 = tmp_7.setTimeout(setupPanelClickDelegation$lambda$lambda_15, 8000);
        if (user.p9_1 === 'instructor') {
          getOpenWindowsForInstructor(user.l9_1, setupPanelClickDelegation$lambda$lambda_16(user, tid_1));
        } else {
          var tmp10_elvis_lhs = user.s9_1;
          var instId_0 = tmp10_elvis_lhs == null ? '' : tmp10_elvis_lhs;
          getOpenWindowsForCadet(instId_0, setupPanelClickDelegation$lambda$lambda_17(user, tid_1));
        }
      }
      e.preventDefault();
      e.stopPropagation();
      return Unit_instance;
    }
    var btnActivate = closest('[data-admin-activate]');
    if (!(btnActivate == null)) {
      var tmp11_elvis_lhs = btnActivate.getAttribute('data-admin-activate');
      var tmp_8;
      if (tmp11_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp_8 = tmp11_elvis_lhs;
      }
      var userId = tmp_8;
      var currentlyActive = btnActivate.getAttribute('data-admin-active') === 'true';
      var tmp_9 = !currentlyActive;
      setActive(userId, tmp_9, setupPanelClickDelegation$lambda$lambda_18);
      e.preventDefault();
      e.stopPropagation();
      return Unit_instance;
    }
    var btnAssign = closest('[data-admin-assign]');
    if (!(btnAssign == null)) {
      var tmp12_elvis_lhs = btnAssign.getAttribute('data-admin-assign');
      var tmp_10;
      if (tmp12_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp_10 = tmp12_elvis_lhs;
      }
      var id = tmp_10;
      updateState(setupPanelClickDelegation$lambda$lambda_19(id));
      var tmp_11 = window;
      tmp_11.setTimeout(setupPanelClickDelegation$lambda$lambda_20, 150);
      e.preventDefault();
      var tmp13_safe_receiver = e instanceof Event ? e : null;
      if (tmp13_safe_receiver == null)
        null;
      else {
        tmp13_safe_receiver.stopPropagation();
      }
      return Unit_instance;
    }
    var btnAssignCadetFromCard = closest('.sd-admin-assign-cadet-btn');
    if (!(btnAssignCadetFromCard == null)) {
      var tmp14_elvis_lhs = btnAssignCadetFromCard.getAttribute('data-admin-assign-cadet');
      var tmp_12;
      if (tmp14_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp_12 = tmp14_elvis_lhs;
      }
      var cadetId = tmp_12;
      updateState(setupPanelClickDelegation$lambda$lambda_21(cadetId));
      var tmp_13 = window;
      tmp_13.setTimeout(setupPanelClickDelegation$lambda$lambda_22, 150);
      e.preventDefault();
      var tmp15_safe_receiver = e instanceof Event ? e : null;
      if (tmp15_safe_receiver == null)
        null;
      else {
        tmp15_safe_receiver.stopPropagation();
      }
      return Unit_instance;
    }
    var btnAssignCadet = closest('[data-admin-assign-instructor]');
    if (!(btnAssignCadet == null)) {
      var tmp16_elvis_lhs = btnAssignCadet.getAttribute('data-admin-assign-instructor');
      var tmp_14;
      if (tmp16_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp_14 = tmp16_elvis_lhs;
      }
      var instId_1 = tmp_14;
      var tmp17_elvis_lhs = btnAssignCadet.getAttribute('data-admin-assign-cadet');
      var tmp_15;
      if (tmp17_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp_15 = tmp17_elvis_lhs;
      }
      var cadetId_0 = tmp_15;
      assignCadetToInstructor(instId_1, cadetId_0, setupPanelClickDelegation$lambda$lambda_23);
      e.preventDefault();
      var tmp18_safe_receiver = e instanceof Event ? e : null;
      if (tmp18_safe_receiver == null)
        null;
      else {
        tmp18_safe_receiver.stopPropagation();
      }
      return Unit_instance;
    }
    var btnUnlink = closest('[data-admin-unlink-instructor]');
    if (!(btnUnlink == null)) {
      var tmp19_elvis_lhs = btnUnlink.getAttribute('data-admin-unlink-instructor');
      var tmp_16;
      if (tmp19_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp_16 = tmp19_elvis_lhs;
      }
      var instId_2 = tmp_16;
      var tmp20_elvis_lhs = btnUnlink.getAttribute('data-admin-unlink-cadet');
      var tmp_17;
      if (tmp20_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp_17 = tmp20_elvis_lhs;
      }
      var cadetId_1 = tmp_17;
      removeCadetFromInstructor(instId_2, cadetId_1, setupPanelClickDelegation$lambda$lambda_24);
      e.preventDefault();
      e.stopPropagation();
      return Unit_instance;
    }
    var btnDelete = closest('[data-admin-delete]');
    if (!(btnDelete == null)) {
      var tmp21_elvis_lhs = btnDelete.getAttribute('data-admin-delete');
      var tmp_18;
      if (tmp21_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp_18 = tmp21_elvis_lhs;
      }
      var userId_0 = tmp_18;
      if (!window.confirm('\u0423\u0434\u0430\u043B\u0438\u0442\u044C \u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u0435\u043B\u044F \u0438\u0437 \u0431\u0430\u0437\u044B? \u042D\u0442\u043E \u043D\u0435 \u0443\u0434\u0430\u043B\u0438\u0442 \u0430\u043A\u043A\u0430\u0443\u043D\u0442 Firebase Auth.'))
        return Unit_instance;
      deleteUser(userId_0, setupPanelClickDelegation$lambda$lambda_25);
      e.preventDefault();
      e.stopPropagation();
      return Unit_instance;
    }
    var btnBalanceSelect = closest('[data-balance-select]');
    if (!(btnBalanceSelect == null)) {
      var tmp22_elvis_lhs = btnBalanceSelect.getAttribute('data-balance-select');
      var tmp_19;
      if (tmp22_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp_19 = tmp22_elvis_lhs;
      }
      var id_0 = tmp_19;
      updateState(setupPanelClickDelegation$lambda$lambda_26(id_0));
      var tmp_20 = window;
      tmp_20.setTimeout(setupPanelClickDelegation$lambda$lambda_27, 150);
      e.preventDefault();
      e.stopPropagation();
      return Unit_instance;
    }
    var chatContact = closest('.sd-chat-contact');
    if (!(chatContact == null)) {
      var tmp23_elvis_lhs = chatContact.getAttribute('data-contact-id');
      var tmp_21;
      if (tmp23_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp_21 = tmp23_elvis_lhs;
      }
      var contactId = tmp_21;
      var tmp24_safe_receiver = get_appState().ba_1;
      var tmp25_elvis_lhs = tmp24_safe_receiver == null ? null : tmp24_safe_receiver.l9_1;
      var tmp_22;
      if (tmp25_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp_22 = tmp25_elvis_lhs;
      }
      var uid = tmp_22;
      updateState(setupPanelClickDelegation$lambda$lambda_28(contactId));
      unsubscribeChat();
      var tmp_23 = chatRoomId(uid, contactId);
      subscribeMessages(tmp_23, setupPanelClickDelegation$lambda$lambda_29);
      e.preventDefault();
      e.stopPropagation();
      return Unit_instance;
    }
    var adminOpenChat = closest('.sd-admin-open-chat');
    if (!(adminOpenChat == null)) {
      var tmp26_elvis_lhs = adminOpenChat.getAttribute('data-contact-id');
      var tmp_24;
      if (tmp26_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp_24 = tmp26_elvis_lhs;
      }
      var contactId_0 = tmp_24;
      var tmp27_safe_receiver = get_appState().ba_1;
      var tmp28_elvis_lhs = tmp27_safe_receiver == null ? null : tmp27_safe_receiver.l9_1;
      var tmp_25;
      if (tmp28_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp_25 = tmp28_elvis_lhs;
      }
      var uid_0 = tmp_25;
      updateState(setupPanelClickDelegation$lambda$lambda_30(contactId_0));
      unsubscribeChat();
      var tmp_26 = chatRoomId(uid_0, contactId_0);
      subscribeMessages(tmp_26, setupPanelClickDelegation$lambda$lambda_31);
      e.preventDefault();
      e.stopPropagation();
      return Unit_instance;
    }
    var cadetChatBtn = closest('.sd-cadet-chat-btn');
    if (!(cadetChatBtn == null)) {
      var tmp29_elvis_lhs = cadetChatBtn.getAttribute('data-contact-id');
      var tmp_27;
      if (tmp29_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp_27 = tmp29_elvis_lhs;
      }
      var contactId_1 = tmp_27;
      var tmp30_safe_receiver = get_appState().ba_1;
      var tmp31_elvis_lhs = tmp30_safe_receiver == null ? null : tmp30_safe_receiver.l9_1;
      var tmp_28;
      if (tmp31_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp_28 = tmp31_elvis_lhs;
      }
      var uid_1 = tmp_28;
      updateState(setupPanelClickDelegation$lambda$lambda_32(contactId_1));
      unsubscribeChat();
      var tmp_29 = chatRoomId(uid_1, contactId_1);
      subscribeMessages(tmp_29, setupPanelClickDelegation$lambda$lambda_33);
      e.preventDefault();
      e.stopPropagation();
    }
    return Unit_instance;
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
  function setupPanelClickDelegation$lambda$lambda_0($instId) {
    return function ($this$updateState) {
      $this$updateState.cb_1 = $instId;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_1($this$updateState) {
    $this$updateState.cb_1 = null;
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda_2($this$updateState) {
    $this$updateState.cb_1 = null;
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda_3($this$updateState) {
    $this$updateState.ia_1 = null;
    $this$updateState.ja_1 = emptyList();
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda_4($idx) {
    return function ($this$updateState) {
      $this$updateState.fa_1 = $idx;
      $this$updateState.ma_1 = false;
      $this$updateState.pa_1 = false;
      $this$updateState.sa_1 = false;
      $this$updateState.ha_1 = false;
      $this$updateState.va_1 = false;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_5($this$updateState) {
    $this$updateState.va_1 = true;
    $this$updateState.ea_1 = null;
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda_6() {
    updateState(setupPanelClickDelegation$lambda$lambda$lambda);
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda($this$updateState) {
    $this$updateState.va_1 = false;
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_0($list, $err) {
    return function ($this$updateState) {
      $this$updateState.ua_1 = $list;
      $this$updateState.ra_1 = $list;
      $this$updateState.va_1 = false;
      var tmp;
      if (!($err == null)) {
        $this$updateState.ea_1 = $err;
        tmp = Unit_instance;
      }
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_7($tid) {
    return function (list, err) {
      window.clearTimeout($tid);
      updateState(setupPanelClickDelegation$lambda$lambda$lambda_0(list, err));
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_8($this$updateState) {
    $this$updateState.sa_1 = true;
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda_9() {
    updateState(setupPanelClickDelegation$lambda$lambda$lambda_1);
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_1($this$updateState) {
    $this$updateState.sa_1 = false;
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda($list, $hist) {
    return function ($this$updateState) {
      $this$updateState.ra_1 = $list;
      $this$updateState.ua_1 = $list;
      $this$updateState.qa_1 = $hist;
      $this$updateState.sa_1 = false;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_2($tid, $list) {
    return function (hist) {
      window.clearTimeout($tid);
      updateState(setupPanelClickDelegation$lambda$lambda$lambda$lambda($list, hist));
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_10($tid) {
    return function (list) {
      // Inline function 'kotlin.collections.map' call
      // Inline function 'kotlin.collections.mapTo' call
      var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(list, 10));
      var _iterator__ex2g4s = list.g();
      while (_iterator__ex2g4s.h()) {
        var item = _iterator__ex2g4s.i();
        var tmp$ret$0 = item.l9_1;
        destination.e(tmp$ret$0);
      }
      var tmp = destination;
      loadBalanceHistoryForUsers(tmp, setupPanelClickDelegation$lambda$lambda$lambda_2($tid, list));
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_11($this$updateState) {
    $this$updateState.ha_1 = true;
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda_12() {
    updateState(setupPanelClickDelegation$lambda$lambda$lambda_3);
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_3($this$updateState) {
    $this$updateState.ha_1 = false;
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_4($list) {
    return function ($this$updateState) {
      $this$updateState.ga_1 = $list;
      $this$updateState.ha_1 = false;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_13($chatTid) {
    return function (list) {
      window.clearTimeout($chatTid);
      updateState(setupPanelClickDelegation$lambda$lambda$lambda_4(list));
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_14($this$updateState) {
    $this$updateState.ma_1 = true;
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda_15() {
    updateState(setupPanelClickDelegation$lambda$lambda$lambda_5);
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_5($this$updateState) {
    $this$updateState.ma_1 = false;
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda_0($wins, $sess) {
    return function ($this$updateState) {
      $this$updateState.ka_1 = $wins;
      $this$updateState.la_1 = $sess;
      $this$updateState.ma_1 = false;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda$lambda($list, $user) {
    return function ($this$updateState) {
      var tmp = $this$updateState;
      // Inline function 'kotlin.collections.filter' call
      var tmp0 = $list;
      // Inline function 'kotlin.collections.filterTo' call
      var destination = ArrayList_init_$Create$();
      var _iterator__ex2g4s = tmp0.g();
      while (_iterator__ex2g4s.h()) {
        var element = _iterator__ex2g4s.i();
        if ($user.t9_1.g1(element.l9_1)) {
          destination.e(element);
        }
      }
      tmp.eb_1 = destination;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda_1($user) {
    return function (list) {
      updateState(setupPanelClickDelegation$lambda$lambda$lambda$lambda$lambda(list, $user));
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_6($tid, $wins, $user) {
    return function (sess) {
      window.clearTimeout($tid);
      updateState(setupPanelClickDelegation$lambda$lambda$lambda$lambda_0($wins, sess));
      getUsers(setupPanelClickDelegation$lambda$lambda$lambda$lambda_1($user));
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_16($user, $tid) {
    return function (wins) {
      getSessionsForInstructor($user.l9_1, setupPanelClickDelegation$lambda$lambda$lambda_6($tid, wins, $user));
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda_2($wins, $sess) {
    return function ($this$updateState) {
      $this$updateState.ka_1 = $wins;
      $this$updateState.la_1 = $sess;
      $this$updateState.ma_1 = false;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda_3(inst) {
    updateState(setupPanelClickDelegation$lambda$lambda$lambda$lambda$lambda_0(inst));
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda$lambda_0($inst) {
    return function ($this$updateState) {
      $this$updateState.db_1 = $inst;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_7($tid, $user, $wins) {
    return function (sess) {
      window.clearTimeout($tid);
      updateState(setupPanelClickDelegation$lambda$lambda$lambda$lambda_2($wins, sess));
      var tmp0_safe_receiver = $user.s9_1;
      if (tmp0_safe_receiver == null)
        null;
      else {
        // Inline function 'kotlin.let' call
        getUserById(tmp0_safe_receiver, setupPanelClickDelegation$lambda$lambda$lambda$lambda_3);
      }
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_17($user, $tid) {
    return function (wins) {
      getSessionsForCadet($user.l9_1, setupPanelClickDelegation$lambda$lambda$lambda_7($tid, $user, wins));
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_18(err) {
    if (!(err == null)) {
      updateState(setupPanelClickDelegation$lambda$lambda$lambda_8(err));
    } else {
      getUsers(setupPanelClickDelegation$lambda$lambda$lambda_9);
    }
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_8($err) {
    return function ($this$updateState) {
      $this$updateState.ea_1 = $err;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_9(list) {
    updateState(setupPanelClickDelegation$lambda$lambda$lambda$lambda_4(list));
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda_4($list) {
    return function ($this$updateState) {
      $this$updateState.ua_1 = $list;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_19($id) {
    return function ($this$updateState) {
      $this$updateState.ab_1 = $id;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_20() {
    var tmp0_safe_receiver = document.getElementById('sd-assign-panel');
    var tmp;
    if (tmp0_safe_receiver == null) {
      tmp = null;
    } else {
      tmp0_safe_receiver.scrollIntoView({block: 'start', behavior: 'smooth'});
      tmp = Unit_instance;
    }
    return tmp;
  }
  function setupPanelClickDelegation$lambda$lambda_21($cadetId) {
    return function ($this$updateState) {
      $this$updateState.bb_1 = $cadetId;
      $this$updateState.ab_1 = null;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_22() {
    var tmp0_safe_receiver = document.getElementById('sd-assign-panel');
    var tmp;
    if (tmp0_safe_receiver == null) {
      tmp = null;
    } else {
      tmp0_safe_receiver.scrollIntoView({block: 'start', behavior: 'smooth'});
      tmp = Unit_instance;
    }
    return tmp;
  }
  function setupPanelClickDelegation$lambda$lambda_23(err) {
    if (!(err == null)) {
      updateState(setupPanelClickDelegation$lambda$lambda$lambda_10(err));
    } else {
      getUsers(setupPanelClickDelegation$lambda$lambda$lambda_11);
    }
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_10($err) {
    return function ($this$updateState) {
      $this$updateState.ea_1 = $err;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_11(list) {
    updateState(setupPanelClickDelegation$lambda$lambda$lambda$lambda_5(list));
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda_5($list) {
    return function ($this$updateState) {
      $this$updateState.ua_1 = $list;
      $this$updateState.bb_1 = null;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_24(err) {
    if (!(err == null)) {
      updateState(setupPanelClickDelegation$lambda$lambda$lambda_12(err));
    } else {
      getUsers(setupPanelClickDelegation$lambda$lambda$lambda_13);
    }
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_12($err) {
    return function ($this$updateState) {
      $this$updateState.ea_1 = $err;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_13(list) {
    updateState(setupPanelClickDelegation$lambda$lambda$lambda$lambda_6(list));
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda_6($list) {
    return function ($this$updateState) {
      $this$updateState.ua_1 = $list;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_25(err) {
    if (!(err == null)) {
      updateState(setupPanelClickDelegation$lambda$lambda$lambda_14(err));
    } else {
      getUsers(setupPanelClickDelegation$lambda$lambda$lambda_15);
    }
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_14($err) {
    return function ($this$updateState) {
      $this$updateState.ea_1 = $err;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_15(list) {
    updateState(setupPanelClickDelegation$lambda$lambda$lambda$lambda_7(list));
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda$lambda_7($list) {
    return function ($this$updateState) {
      $this$updateState.ua_1 = $list;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_26($id) {
    return function ($this$updateState) {
      $this$updateState.ta_1 = $id;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_27() {
    var tmp0_safe_receiver = document.getElementById('sd-balance-selected-block');
    var tmp;
    if (tmp0_safe_receiver == null) {
      tmp = null;
    } else {
      tmp0_safe_receiver.scrollIntoView({block: 'start', behavior: 'smooth'});
      tmp = Unit_instance;
    }
    return tmp;
  }
  function setupPanelClickDelegation$lambda$lambda_28($contactId) {
    return function ($this$updateState) {
      $this$updateState.ia_1 = $contactId;
      $this$updateState.ja_1 = emptyList();
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_29(list) {
    updateState(setupPanelClickDelegation$lambda$lambda$lambda_16(list));
    var tmp = window;
    tmp.setTimeout(setupPanelClickDelegation$lambda$lambda$lambda_17, 100);
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_16($list) {
    return function ($this$updateState) {
      $this$updateState.ja_1 = $list;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_17() {
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
  function setupPanelClickDelegation$lambda$lambda_30($contactId) {
    return function ($this$updateState) {
      $this$updateState.fa_1 = 2;
      $this$updateState.ia_1 = $contactId;
      $this$updateState.ja_1 = emptyList();
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_31(list) {
    updateState(setupPanelClickDelegation$lambda$lambda$lambda_18(list));
    var tmp = window;
    tmp.setTimeout(setupPanelClickDelegation$lambda$lambda$lambda_19, 100);
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_18($list) {
    return function ($this$updateState) {
      $this$updateState.ja_1 = $list;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_19() {
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
  function setupPanelClickDelegation$lambda$lambda_32($contactId) {
    return function ($this$updateState) {
      $this$updateState.fa_1 = 2;
      $this$updateState.ia_1 = $contactId;
      $this$updateState.ja_1 = emptyList();
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda_33(list) {
    updateState(setupPanelClickDelegation$lambda$lambda$lambda_20(list));
    var tmp = window;
    tmp.setTimeout(setupPanelClickDelegation$lambda$lambda$lambda_21, 100);
    return Unit_instance;
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_20($list) {
    return function ($this$updateState) {
      $this$updateState.ja_1 = $list;
      return Unit_instance;
    };
  }
  function setupPanelClickDelegation$lambda$lambda$lambda_21() {
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
    updateState(attachListeners$lambda$lambda);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda($this$updateState) {
    $this$updateState.ea_1 = null;
    return Unit_instance;
  }
  function attachListeners$lambda_0(it) {
    updateState(attachListeners$lambda$lambda_0);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_0($this$updateState) {
    $this$updateState.ma_1 = false;
    return Unit_instance;
  }
  function attachListeners$lambda_1(it) {
    updateState(attachListeners$lambda$lambda_1);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_1($this$updateState) {
    $this$updateState.pa_1 = false;
    return Unit_instance;
  }
  function attachListeners$lambda_2(it) {
    updateState(attachListeners$lambda$lambda_2);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_2($this$updateState) {
    $this$updateState.sa_1 = false;
    return Unit_instance;
  }
  function attachListeners$lambda_3(it) {
    updateState(attachListeners$lambda$lambda_3);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_3($this$updateState) {
    $this$updateState.ha_1 = false;
    return Unit_instance;
  }
  function attachListeners$lambda_4(it) {
    var tmp0_elvis_lhs = get_appState().ba_1;
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return Unit_instance;
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var u = tmp;
    updateState(attachListeners$lambda$lambda_4);
    getUsersForChat(u, attachListeners$lambda$lambda_5);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_4($this$updateState) {
    $this$updateState.ga_1 = emptyList();
    $this$updateState.ha_1 = true;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_5(list) {
    updateState(attachListeners$lambda$lambda$lambda(list));
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda($list) {
    return function ($this$updateState) {
      $this$updateState.ga_1 = $list;
      $this$updateState.ha_1 = false;
      return Unit_instance;
    };
  }
  function attachListeners$lambda_5(it) {
    updateState(attachListeners$lambda$lambda_6);
    var tmp = window;
    var tid = tmp.setTimeout(attachListeners$lambda$lambda_7, 8000);
    getUsersWithError(attachListeners$lambda$lambda_8(tid));
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_6($this$updateState) {
    $this$updateState.va_1 = true;
    $this$updateState.ea_1 = null;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_7() {
    updateState(attachListeners$lambda$lambda$lambda_0);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_0($this$updateState) {
    $this$updateState.va_1 = false;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_1($list, $err) {
    return function ($this$updateState) {
      $this$updateState.ua_1 = $list;
      $this$updateState.ra_1 = $list;
      $this$updateState.va_1 = false;
      var tmp;
      if (!($err == null)) {
        $this$updateState.ea_1 = $err;
        tmp = Unit_instance;
      }
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_8($tid) {
    return function (list, err) {
      window.clearTimeout($tid);
      updateState(attachListeners$lambda$lambda$lambda_1(list, err));
      return Unit_instance;
    };
  }
  function attachListeners$lambda_6(it) {
    updateState(attachListeners$lambda$lambda_9);
    var tmp = window;
    var tid = tmp.setTimeout(attachListeners$lambda$lambda_10, 8000);
    getUsers(attachListeners$lambda$lambda_11(tid));
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_9($this$updateState) {
    $this$updateState.sa_1 = true;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_10() {
    updateState(attachListeners$lambda$lambda$lambda_2);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_2($this$updateState) {
    $this$updateState.sa_1 = false;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda$lambda($list, $hist) {
    return function ($this$updateState) {
      $this$updateState.ra_1 = $list;
      $this$updateState.ua_1 = $list;
      $this$updateState.qa_1 = $hist;
      $this$updateState.sa_1 = false;
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda_3($tid, $list) {
    return function (hist) {
      window.clearTimeout($tid);
      updateState(attachListeners$lambda$lambda$lambda$lambda($list, hist));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_11($tid) {
    return function (list) {
      // Inline function 'kotlin.collections.map' call
      // Inline function 'kotlin.collections.mapTo' call
      var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(list, 10));
      var _iterator__ex2g4s = list.g();
      while (_iterator__ex2g4s.h()) {
        var item = _iterator__ex2g4s.i();
        var tmp$ret$0 = item.l9_1;
        destination.e(tmp$ret$0);
      }
      var tmp = destination;
      loadBalanceHistoryForUsers(tmp, attachListeners$lambda$lambda$lambda_3($tid, list));
      return Unit_instance;
    };
  }
  function attachListeners$lambda_7(it) {
    updateState(attachListeners$lambda$lambda_12);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_12($this$updateState) {
    $this$updateState.ab_1 = null;
    $this$updateState.bb_1 = null;
    return Unit_instance;
  }
  function attachListeners$lambda_8(it) {
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
      return Unit_instance;
    }
    updateState(attachListeners$lambda$lambda_14);
    var tmp_1 = signIn(email, password);
    var tmp_2 = tmp_1.then(attachListeners$lambda$lambda_15);
    tmp_2.catch(attachListeners$lambda$lambda_16);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_13($this$updateState) {
    $this$updateState.ca_1 = '\u0412\u0432\u0435\u0434\u0438\u0442\u0435 email \u0438 \u043F\u0430\u0440\u043E\u043B\u044C';
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_14($this$updateState) {
    $this$updateState.da_1 = true;
    $this$updateState.ca_1 = null;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_15(it) {
    updateState(attachListeners$lambda$lambda$lambda_4);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_4($this$updateState) {
    $this$updateState.da_1 = false;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_16(e) {
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
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_5($this$updateState) {
    $this$updateState.ea_1 = '\u041D\u0435\u0442 \u0441\u043E\u0435\u0434\u0438\u043D\u0435\u043D\u0438\u044F \u0441 \u0438\u043D\u0442\u0435\u0440\u043D\u0435\u0442\u043E\u043C.';
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_6($msg) {
    return function ($this$updateState) {
      $this$updateState.da_1 = false;
      $this$updateState.ca_1 = $msg;
      return Unit_instance;
    };
  }
  function attachListeners$lambda_9(it) {
    updateState(attachListeners$lambda$lambda_17);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_17($this$updateState) {
    $this$updateState.aa_1 = AppScreen_Register_getInstance();
    $this$updateState.ca_1 = null;
    return Unit_instance;
  }
  function attachListeners$lambda_10(it) {
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
      return Unit_instance;
    }
    if (password.length < 6) {
      updateState(attachListeners$lambda$lambda_19);
      return Unit_instance;
    }
    updateState(attachListeners$lambda$lambda_20);
    var tmp_4 = register(fullName, email, phone, password, role);
    var tmp_5 = tmp_4.then(attachListeners$lambda$lambda_21);
    tmp_5.catch(attachListeners$lambda$lambda_22);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_18($this$updateState) {
    $this$updateState.ca_1 = '\u0417\u0430\u043F\u043E\u043B\u043D\u0438\u0442\u0435 \u043E\u0431\u044F\u0437\u0430\u0442\u0435\u043B\u044C\u043D\u044B\u0435 \u043F\u043E\u043B\u044F';
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_19($this$updateState) {
    $this$updateState.ca_1 = '\u041F\u0430\u0440\u043E\u043B\u044C \u0434\u043E\u043B\u0436\u0435\u043D \u0431\u044B\u0442\u044C \u043D\u0435 \u043A\u043E\u0440\u043E\u0447\u0435 6 \u0441\u0438\u043C\u0432\u043E\u043B\u043E\u0432';
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_20($this$updateState) {
    $this$updateState.da_1 = true;
    $this$updateState.ca_1 = null;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_21(it) {
    updateState(attachListeners$lambda$lambda$lambda_7);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_7($this$updateState) {
    $this$updateState.da_1 = false;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_22(e) {
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
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_8($this$updateState) {
    $this$updateState.ea_1 = '\u041D\u0435\u0442 \u0441\u043E\u0435\u0434\u0438\u043D\u0435\u043D\u0438\u044F \u0441 \u0438\u043D\u0442\u0435\u0440\u043D\u0435\u0442\u043E\u043C.';
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_9($msg) {
    return function ($this$updateState) {
      $this$updateState.da_1 = false;
      $this$updateState.ca_1 = $msg;
      return Unit_instance;
    };
  }
  function attachListeners$lambda_11(it) {
    updateState(attachListeners$lambda$lambda_23);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_23($this$updateState) {
    $this$updateState.aa_1 = AppScreen_Login_getInstance();
    $this$updateState.ca_1 = null;
    return Unit_instance;
  }
  function attachListeners$lambda_12(it) {
    var tmp0_elvis_lhs = getCurrentUserId();
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return Unit_instance;
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var uid = tmp;
    updateState(attachListeners$lambda$lambda_24);
    getCurrentUser(attachListeners$lambda$lambda_25);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_24($this$updateState) {
    $this$updateState.da_1 = true;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_25(user, _unused_var__etf5q3) {
    updateState(attachListeners$lambda$lambda$lambda_10);
    if (!(user == null) && user.u9_1) {
      updateState(attachListeners$lambda$lambda$lambda_11(user));
    }
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_10($this$updateState) {
    $this$updateState.da_1 = false;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_11($user) {
    return function ($this$updateState) {
      $this$updateState.ba_1 = $user;
      var tmp = $this$updateState;
      switch ($user.p9_1) {
        case 'admin':
          tmp.aa_1 = AppScreen_Admin_getInstance();
          break;
        case 'instructor':
          tmp.aa_1 = AppScreen_Instructor_getInstance();
          break;
        case 'cadet':
          tmp.aa_1 = AppScreen_Cadet_getInstance();
          break;
        default:
          tmp.aa_1 = AppScreen_PendingApproval_getInstance();
          break;
      }
      return Unit_instance;
    };
  }
  function attachListeners$lambda_13(it) {
    signOut();
    return Unit_instance;
  }
  function attachListeners$lambda_14(it) {
    signOut();
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_26($this$updateState) {
    $this$updateState.ha_1 = true;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_27() {
    updateState(attachListeners$lambda$lambda$lambda_12);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_12($this$updateState) {
    $this$updateState.ha_1 = false;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_13($list) {
    return function ($this$updateState) {
      $this$updateState.ga_1 = $list;
      $this$updateState.ha_1 = false;
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_28($chatTid) {
    return function (list) {
      window.clearTimeout($chatTid);
      updateState(attachListeners$lambda$lambda$lambda_13(list));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_29($this$updateState) {
    $this$updateState.ma_1 = true;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_30() {
    updateState(attachListeners$lambda$lambda$lambda_14);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_14($this$updateState) {
    $this$updateState.ma_1 = false;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda$lambda_0($wins, $sess) {
    return function ($this$updateState) {
      $this$updateState.ka_1 = $wins;
      $this$updateState.la_1 = $sess;
      $this$updateState.ma_1 = false;
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda$lambda$lambda($list, $usr) {
    return function ($this$updateState) {
      var tmp = $this$updateState;
      // Inline function 'kotlin.collections.filter' call
      var tmp0 = $list;
      // Inline function 'kotlin.collections.filterTo' call
      var destination = ArrayList_init_$Create$();
      var _iterator__ex2g4s = tmp0.g();
      while (_iterator__ex2g4s.h()) {
        var element = _iterator__ex2g4s.i();
        if ($usr.t9_1.g1(element.l9_1)) {
          destination.e(element);
        }
      }
      tmp.eb_1 = destination;
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda$lambda_1($usr) {
    return function (list) {
      updateState(attachListeners$lambda$lambda$lambda$lambda$lambda(list, $usr));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda_15($tid, $wins, $usr) {
    return function (sess) {
      window.clearTimeout($tid);
      updateState(attachListeners$lambda$lambda$lambda$lambda_0($wins, sess));
      getUsers(attachListeners$lambda$lambda$lambda$lambda_1($usr));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_31($usr, $tid) {
    return function (wins) {
      getSessionsForInstructor($usr.l9_1, attachListeners$lambda$lambda$lambda_15($tid, wins, $usr));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda$lambda_2($list, $ids, $freshUser) {
    return function ($this$updateState) {
      var tmp = $this$updateState;
      // Inline function 'kotlin.collections.filter' call
      var tmp0 = $list;
      // Inline function 'kotlin.collections.filterTo' call
      var destination = ArrayList_init_$Create$();
      var _iterator__ex2g4s = tmp0.g();
      while (_iterator__ex2g4s.h()) {
        var element = _iterator__ex2g4s.i();
        if ($ids.g1(element.l9_1)) {
          destination.e(element);
        }
      }
      tmp.eb_1 = destination;
      var tmp_0;
      if (!($freshUser == null)) {
        $this$updateState.ba_1 = $freshUser;
        tmp_0 = Unit_instance;
      }
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda_16($ids, $freshUser) {
    return function (list) {
      updateState(attachListeners$lambda$lambda$lambda$lambda_2(list, $ids, $freshUser));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_32($usr) {
    return function (freshUser, _unused_var__etf5q3) {
      var tmp1_elvis_lhs = freshUser == null ? null : freshUser.t9_1;
      var ids = tmp1_elvis_lhs == null ? $usr.t9_1 : tmp1_elvis_lhs;
      getUsers(attachListeners$lambda$lambda$lambda_16(ids, freshUser));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_33($this$updateState) {
    $this$updateState.ma_1 = true;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_34() {
    updateState(attachListeners$lambda$lambda$lambda_17);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_17($this$updateState) {
    $this$updateState.ma_1 = false;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda$lambda_3($wins, $sess) {
    return function ($this$updateState) {
      $this$updateState.ka_1 = $wins;
      $this$updateState.la_1 = $sess;
      $this$updateState.ma_1 = false;
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda$lambda_4(inst) {
    updateState(attachListeners$lambda$lambda$lambda$lambda$lambda_0(inst));
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda$lambda$lambda_0($inst) {
    return function ($this$updateState) {
      $this$updateState.db_1 = $inst;
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda_18($tid, $usr, $wins) {
    return function (sess) {
      window.clearTimeout($tid);
      updateState(attachListeners$lambda$lambda$lambda$lambda_3($wins, sess));
      var tmp0_safe_receiver = $usr.s9_1;
      if (tmp0_safe_receiver == null)
        null;
      else {
        // Inline function 'kotlin.let' call
        getUserById(tmp0_safe_receiver, attachListeners$lambda$lambda$lambda$lambda_4);
      }
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_35($usr, $tid) {
    return function (wins) {
      getSessionsForCadet($usr.l9_1, attachListeners$lambda$lambda$lambda_18($tid, $usr, wins));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_36($this$updateState) {
    $this$updateState.pa_1 = true;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_37() {
    updateState(attachListeners$lambda$lambda$lambda_19);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_19($this$updateState) {
    $this$updateState.pa_1 = false;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda$lambda_5($sess, $hist) {
    return function ($this$updateState) {
      $this$updateState.na_1 = $sess;
      $this$updateState.oa_1 = $hist;
      $this$updateState.pa_1 = false;
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda_20($tid, $sess) {
    return function (hist) {
      window.clearTimeout($tid);
      updateState(attachListeners$lambda$lambda$lambda$lambda_5($sess, hist));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_38($usr, $tid) {
    return function (sess) {
      getBalanceHistory($usr.l9_1, attachListeners$lambda$lambda$lambda_20($tid, sess));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_39($this$updateState) {
    $this$updateState.pa_1 = true;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_40() {
    updateState(attachListeners$lambda$lambda$lambda_21);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_21($this$updateState) {
    $this$updateState.pa_1 = false;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda$lambda_6($sess, $hist) {
    return function ($this$updateState) {
      $this$updateState.na_1 = $sess;
      $this$updateState.oa_1 = $hist;
      $this$updateState.pa_1 = false;
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda_22($tid, $sess) {
    return function (hist) {
      window.clearTimeout($tid);
      updateState(attachListeners$lambda$lambda$lambda$lambda_6($sess, hist));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_41($usr, $tid) {
    return function (sess) {
      getBalanceHistory($usr.l9_1, attachListeners$lambda$lambda$lambda_22($tid, sess));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_42($this$updateState) {
    $this$updateState.pa_1 = true;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_43() {
    updateState(attachListeners$lambda$lambda$lambda_23);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_23($this$updateState) {
    $this$updateState.pa_1 = false;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda$lambda_7($list, $hist) {
    return function ($this$updateState) {
      $this$updateState.ra_1 = $list;
      $this$updateState.oa_1 = $hist;
      $this$updateState.pa_1 = false;
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda_24($tid, $list) {
    return function (hist) {
      window.clearTimeout($tid);
      updateState(attachListeners$lambda$lambda$lambda$lambda_7($list, hist));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_44($tid) {
    return function (list) {
      // Inline function 'kotlin.collections.map' call
      // Inline function 'kotlin.collections.mapTo' call
      var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(list, 10));
      var _iterator__ex2g4s = list.g();
      while (_iterator__ex2g4s.h()) {
        var item = _iterator__ex2g4s.i();
        var tmp$ret$0 = item.l9_1;
        destination.e(tmp$ret$0);
      }
      var tmp = destination;
      loadBalanceHistoryForUsers(tmp, attachListeners$lambda$lambda$lambda_24($tid, list));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_45($this$updateState) {
    $this$updateState.va_1 = true;
    $this$updateState.ea_1 = null;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_46() {
    updateState(attachListeners$lambda$lambda$lambda_25);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_25($this$updateState) {
    $this$updateState.va_1 = false;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_26($list, $err) {
    return function ($this$updateState) {
      $this$updateState.ua_1 = $list;
      $this$updateState.ra_1 = $list;
      $this$updateState.va_1 = false;
      var tmp;
      if (!($err == null)) {
        $this$updateState.ea_1 = $err;
        tmp = Unit_instance;
      }
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_47($tid) {
    return function (list, err) {
      window.clearTimeout($tid);
      updateState(attachListeners$lambda$lambda$lambda_26(list, err));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_48($this$updateState) {
    $this$updateState.sa_1 = true;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_49() {
    updateState(attachListeners$lambda$lambda$lambda_27);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_27($this$updateState) {
    $this$updateState.sa_1 = false;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda$lambda_8($list, $hist) {
    return function ($this$updateState) {
      $this$updateState.ra_1 = $list;
      $this$updateState.ua_1 = $list;
      $this$updateState.qa_1 = $hist;
      $this$updateState.sa_1 = false;
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda_28($tid, $list) {
    return function (hist) {
      window.clearTimeout($tid);
      updateState(attachListeners$lambda$lambda$lambda$lambda_8($list, hist));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_50($tid) {
    return function (list) {
      // Inline function 'kotlin.collections.map' call
      // Inline function 'kotlin.collections.mapTo' call
      var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(list, 10));
      var _iterator__ex2g4s = list.g();
      while (_iterator__ex2g4s.h()) {
        var item = _iterator__ex2g4s.i();
        var tmp$ret$0 = item.l9_1;
        destination.e(tmp$ret$0);
      }
      var tmp = destination;
      loadBalanceHistoryForUsers(tmp, attachListeners$lambda$lambda$lambda_28($tid, list));
      return Unit_instance;
    };
  }
  function attachListeners$lambda_15($uid) {
    return function () {
      var tmp;
      if (get_appState().fa_1 === 2 && !($uid == null)) {
        var tmp_0;
        if (get_appState().ga_1.q() && !get_appState().ha_1) {
          updateState(attachListeners$lambda$lambda_26);
          var tmp_1 = window;
          var chatTid = tmp_1.setTimeout(attachListeners$lambda$lambda_27, 5000);
          var tmp_2 = ensureNotNull(get_appState().ba_1);
          getUsersForChat(tmp_2, attachListeners$lambda$lambda_28(chatTid));
          tmp_0 = Unit_instance;
        }
        tmp = tmp_0;
      }
      var tmp0_elvis_lhs = get_appState().ba_1;
      var tmp_3;
      if (tmp0_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp_3 = tmp0_elvis_lhs;
      }
      var usr = tmp_3;
      var tmp_4;
      if (usr.p9_1 === 'instructor' && (get_appState().fa_1 === 0 || get_appState().fa_1 === 1)) {
        if (!get_appState().ma_1 && get_appState().la_1.q() && get_appState().ka_1.q()) {
          updateState(attachListeners$lambda$lambda_29);
          var tmp_5 = window;
          var tid = tmp_5.setTimeout(attachListeners$lambda$lambda_30, 8000);
          getOpenWindowsForInstructor(usr.l9_1, attachListeners$lambda$lambda_31(usr, tid));
        }
        var tmp_6;
        if (get_appState().eb_1.q()) {
          getCurrentUser(attachListeners$lambda$lambda_32(usr));
          tmp_6 = Unit_instance;
        }
        tmp_4 = tmp_6;
      } else if (usr.p9_1 === 'cadet' && (get_appState().fa_1 === 0 || get_appState().fa_1 === 1)) {
        var tmp_7;
        if (!get_appState().ma_1 && get_appState().la_1.q()) {
          updateState(attachListeners$lambda$lambda_33);
          var tmp_8 = window;
          var tid_0 = tmp_8.setTimeout(attachListeners$lambda$lambda_34, 8000);
          var tmp1_elvis_lhs = usr.s9_1;
          var instId = tmp1_elvis_lhs == null ? '' : tmp1_elvis_lhs;
          getOpenWindowsForCadet(instId, attachListeners$lambda$lambda_35(usr, tid_0));
          tmp_7 = Unit_instance;
        }
        tmp_4 = tmp_7;
      } else if (usr.p9_1 === 'instructor' && get_appState().fa_1 === 4) {
        var tmp_9;
        if (!get_appState().pa_1 && get_appState().na_1.q()) {
          updateState(attachListeners$lambda$lambda_36);
          var tmp_10 = window;
          var tid_1 = tmp_10.setTimeout(attachListeners$lambda$lambda_37, 8000);
          getSessionsForInstructor(usr.l9_1, attachListeners$lambda$lambda_38(usr, tid_1));
          tmp_9 = Unit_instance;
        }
        tmp_4 = tmp_9;
      } else if (usr.p9_1 === 'cadet' && get_appState().fa_1 === 4) {
        var tmp_11;
        if (!get_appState().pa_1 && get_appState().na_1.q()) {
          updateState(attachListeners$lambda$lambda_39);
          var tmp_12 = window;
          var tid_2 = tmp_12.setTimeout(attachListeners$lambda$lambda_40, 8000);
          getSessionsForCadet(usr.l9_1, attachListeners$lambda$lambda_41(usr, tid_2));
          tmp_11 = Unit_instance;
        }
        tmp_4 = tmp_11;
      } else if (usr.p9_1 === 'admin' && get_appState().fa_1 === 3) {
        var tmp_13;
        if (!get_appState().pa_1 && get_appState().oa_1.q()) {
          updateState(attachListeners$lambda$lambda_42);
          var tmp_14 = window;
          var tid_3 = tmp_14.setTimeout(attachListeners$lambda$lambda_43, 8000);
          getUsers(attachListeners$lambda$lambda_44(tid_3));
          tmp_13 = Unit_instance;
        }
        tmp_4 = tmp_13;
      } else if (usr.p9_1 === 'admin' && get_appState().fa_1 === 0) {
        var tmp_15;
        if (!get_appState().va_1) {
          updateState(attachListeners$lambda$lambda_45);
          var tmp_16 = window;
          var tid_4 = tmp_16.setTimeout(attachListeners$lambda$lambda_46, 8000);
          getUsersWithError(attachListeners$lambda$lambda_47(tid_4));
          tmp_15 = Unit_instance;
        }
        tmp_4 = tmp_15;
      } else if (usr.p9_1 === 'admin' && get_appState().fa_1 === 1) {
        var tmp_17;
        if (!get_appState().sa_1 && get_appState().ra_1.q()) {
          updateState(attachListeners$lambda$lambda_48);
          var tmp_18 = window;
          var tid_5 = tmp_18.setTimeout(attachListeners$lambda$lambda_49, 8000);
          getUsers(attachListeners$lambda$lambda_50(tid_5));
          tmp_17 = Unit_instance;
        }
        tmp_4 = tmp_17;
      }
      return Unit_instance;
    };
  }
  function attachListeners$lambda_16(it) {
    signOut();
    return Unit_instance;
  }
  function attachListeners$lambda_17(it) {
    updateState(attachListeners$lambda$lambda_51);
    unsubscribeChat();
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_51($this$updateState) {
    $this$updateState.ia_1 = null;
    $this$updateState.ja_1 = emptyList();
    return Unit_instance;
  }
  function attachListeners$lambda_18($chatInput, $uid) {
    return function (it) {
      sendChatMessage($chatInput, $uid);
      return Unit_instance;
    };
  }
  function attachListeners$lambda_19($chatInput, $uid) {
    return function (e) {
      var tmp;
      if ((e == null ? null : e.key) == 'Enter') {
        sendChatMessage($chatInput, $uid);
        tmp = Unit_instance;
      }
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda_29($err) {
    return function ($this$updateState) {
      $this$updateState.ea_1 = $err;
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda$lambda$lambda_1($wins, $sess) {
    return function ($this$updateState) {
      $this$updateState.ka_1 = $wins;
      $this$updateState.la_1 = $sess;
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda$lambda_9($wins) {
    return function (sess) {
      updateState(attachListeners$lambda$lambda$lambda$lambda$lambda_1($wins, sess));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda_30($usr) {
    return function (wins) {
      getSessionsForInstructor($usr.l9_1, attachListeners$lambda$lambda$lambda$lambda_9(wins));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_52($usr) {
    return function (_unused_var__etf5q3, err) {
      var tmp;
      if (!(err == null)) {
        updateState(attachListeners$lambda$lambda$lambda_29(err));
        tmp = Unit_instance;
      } else {
        getOpenWindowsForInstructor($usr.l9_1, attachListeners$lambda$lambda$lambda_30($usr));
        tmp = Unit_instance;
      }
      return Unit_instance;
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
        return Unit_instance;
      }
      // Inline function 'kotlin.js.unsafeCast' call
      var dateFn = function (s) {
        return (new Date(s)).getTime();
      };
      var ms = numberToLong(dateFn(v));
      var tmp_1;
      if (ms.w1(new Long(0, 0)) <= 0) {
        return Unit_instance;
      }
      addOpenWindow($usr.l9_1, ms, attachListeners$lambda$lambda_52($usr));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda_31($err) {
    return function ($this$updateState) {
      $this$updateState.ea_1 = $err;
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda_32(wins) {
    updateState(attachListeners$lambda$lambda$lambda$lambda_10(wins));
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda$lambda_10($wins) {
    return function ($this$updateState) {
      $this$updateState.ka_1 = $wins;
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_53($usr) {
    return function (err) {
      var tmp;
      if (!(err == null)) {
        updateState(attachListeners$lambda$lambda$lambda_31(err));
        tmp = Unit_instance;
      } else {
        getOpenWindowsForInstructor($usr.l9_1, attachListeners$lambda$lambda$lambda_32);
        tmp = Unit_instance;
      }
      return Unit_instance;
    };
  }
  function attachListeners$lambda_21($btn, $usr) {
    return function (it) {
      var tmp0_elvis_lhs = $btn.getAttribute('data-window-id');
      var tmp;
      if (tmp0_elvis_lhs == null) {
        return Unit_instance;
      } else {
        tmp = tmp0_elvis_lhs;
      }
      var wid = tmp;
      deleteOpenWindow(wid, attachListeners$lambda$lambda_53($usr));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda_33($err) {
    return function ($this$updateState) {
      $this$updateState.ea_1 = $err;
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda$lambda$lambda_2($wins, $sess) {
    return function ($this$updateState) {
      $this$updateState.ka_1 = $wins;
      $this$updateState.la_1 = $sess;
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda$lambda_11($wins) {
    return function (sess) {
      updateState(attachListeners$lambda$lambda$lambda$lambda$lambda_2($wins, sess));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda_34($usr) {
    return function (wins) {
      getSessionsForCadet($usr.l9_1, attachListeners$lambda$lambda$lambda$lambda_11(wins));
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_54($usr) {
    return function (err) {
      var tmp;
      if (!(err == null)) {
        updateState(attachListeners$lambda$lambda$lambda_33(err));
        tmp = Unit_instance;
      } else {
        var tmp0_elvis_lhs = $usr.s9_1;
        var instId = tmp0_elvis_lhs == null ? '' : tmp0_elvis_lhs;
        getOpenWindowsForCadet(instId, attachListeners$lambda$lambda$lambda_34($usr));
        tmp = Unit_instance;
      }
      return Unit_instance;
    };
  }
  function attachListeners$lambda_22($wid, $usr) {
    return function (it) {
      bookWindow($wid, $usr.l9_1, attachListeners$lambda$lambda_54($usr));
      return Unit_instance;
    };
  }
  function attachListeners$lambda_23(it) {
    updateState(attachListeners$lambda$lambda_55);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_55($this$updateState) {
    $this$updateState.ta_1 = null;
    return Unit_instance;
  }
  function attachListeners$lambda_24($usr) {
    return function (it) {
      attachListeners$_anonymous_$doBalanceOp_vagh69($usr, 'credit');
      return Unit_instance;
    };
  }
  function attachListeners$lambda_25($usr) {
    return function (it) {
      attachListeners$_anonymous_$doBalanceOp_vagh69($usr, 'debit');
      return Unit_instance;
    };
  }
  function attachListeners$lambda_26($usr) {
    return function (it) {
      attachListeners$_anonymous_$doBalanceOp_vagh69($usr, 'set');
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda_56(err) {
    if (!(err == null)) {
      updateState(attachListeners$lambda$lambda$lambda_35(err));
    } else {
      getCurrentUser(attachListeners$lambda$lambda$lambda_36);
    }
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_35($err) {
    return function ($this$updateState) {
      $this$updateState.ea_1 = $err;
      return Unit_instance;
    };
  }
  function attachListeners$lambda$lambda$lambda_36(newUser, _unused_var__etf5q3) {
    if (!(newUser == null)) {
      updateState(attachListeners$lambda$lambda$lambda$lambda_12(newUser));
    }
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda$lambda_12($newUser) {
    return function ($this$updateState) {
      $this$updateState.ba_1 = $newUser;
      return Unit_instance;
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
      updateProfile($usr.l9_1, fullName, phone, attachListeners$lambda$lambda_56);
      return Unit_instance;
    };
  }
  function attachListeners$lambda_28(it) {
    var tmp = document.getElementById('sd-settings-newpassword');
    var tmp0_safe_receiver = tmp instanceof HTMLInputElement ? tmp : null;
    var tmp1_elvis_lhs = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.value;
    var newPass = tmp1_elvis_lhs == null ? '' : tmp1_elvis_lhs;
    if (newPass.length < 6) {
      updateState(attachListeners$lambda$lambda_57);
      return Unit_instance;
    }
    var tmp_0 = changePassword(newPass);
    var tmp_1 = tmp_0.then(attachListeners$lambda$lambda_58);
    tmp_1.catch(attachListeners$lambda$lambda_59);
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_57($this$updateState) {
    $this$updateState.ea_1 = '\u041F\u0430\u0440\u043E\u043B\u044C \u043D\u0435 \u043C\u0435\u043D\u0435\u0435 6 \u0441\u0438\u043C\u0432\u043E\u043B\u043E\u0432';
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_58(it) {
    updateState(attachListeners$lambda$lambda$lambda_37);
    var tmp = document.getElementById('sd-settings-newpassword');
    var tmp0_safe_receiver = tmp instanceof HTMLInputElement ? tmp : null;
    if (tmp0_safe_receiver == null)
      null;
    else {
      tmp0_safe_receiver.value = '';
    }
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_37($this$updateState) {
    $this$updateState.ea_1 = null;
    return Unit_instance;
  }
  function attachListeners$lambda$lambda_59(e) {
    updateState(attachListeners$lambda$lambda$lambda_38(e));
    return Unit_instance;
  }
  function attachListeners$lambda$lambda$lambda_38($e) {
    return function ($this$updateState) {
      var tmp = $this$updateState;
      // Inline function 'kotlin.js.asDynamic' call
      var tmp_0 = $e.message;
      var tmp0_elvis_lhs = (!(tmp_0 == null) ? typeof tmp_0 === 'string' : false) ? tmp_0 : null;
      tmp.ea_1 = tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430 \u0441\u043C\u0435\u043D\u044B \u043F\u0430\u0440\u043E\u043B\u044F' : tmp0_elvis_lhs;
      return Unit_instance;
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
      return Unit_instance;
    };
  }
  function sendChatMessage$lambda_0(_unused_var__etf5q3) {
    updateState(sendChatMessage$lambda$lambda);
    return Unit_instance;
  }
  function sendChatMessage$lambda$lambda($this$updateState) {
    $this$updateState.ea_1 = '\u041D\u0435 \u0443\u0434\u0430\u043B\u043E\u0441\u044C \u043E\u0442\u043F\u0440\u0430\u0432\u0438\u0442\u044C \u0441\u043E\u043E\u0431\u0449\u0435\u043D\u0438\u0435.';
    return Unit_instance;
  }
  function attachListeners$_anonymous_$doBalanceOp$lambda_6obd22(err) {
    if (!(err == null)) {
      updateState(attachListeners$_anonymous_$doBalanceOp$lambda$lambda_w7h4nl(err));
    } else {
      getUsers(attachListeners$_anonymous_$doBalanceOp$lambda$lambda_w7h4nl_0);
    }
    return Unit_instance;
  }
  function attachListeners$_anonymous_$doBalanceOp$lambda$lambda_w7h4nl($err) {
    return function ($this$updateState) {
      $this$updateState.ea_1 = $err;
      return Unit_instance;
    };
  }
  function attachListeners$_anonymous_$doBalanceOp$lambda$lambda_w7h4nl_0(list) {
    // Inline function 'kotlin.collections.map' call
    // Inline function 'kotlin.collections.mapTo' call
    var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(list, 10));
    var _iterator__ex2g4s = list.g();
    while (_iterator__ex2g4s.h()) {
      var item = _iterator__ex2g4s.i();
      var tmp$ret$0 = item.l9_1;
      destination.e(tmp$ret$0);
    }
    var tmp = destination;
    loadBalanceHistoryForUsers(tmp, attachListeners$_anonymous_$doBalanceOp$lambda$lambda$lambda_os9kp2(list));
    return Unit_instance;
  }
  function attachListeners$_anonymous_$doBalanceOp$lambda$lambda$lambda$lambda_p5yxtd($list, $hist) {
    return function ($this$updateState) {
      $this$updateState.ra_1 = $list;
      $this$updateState.qa_1 = $hist;
      return Unit_instance;
    };
  }
  function attachListeners$_anonymous_$doBalanceOp$lambda$lambda$lambda_os9kp2($list) {
    return function (hist) {
      updateState(attachListeners$_anonymous_$doBalanceOp$lambda$lambda$lambda$lambda_p5yxtd($list, hist));
      return Unit_instance;
    };
  }
  function mainWrapper() {
    main();
  }
  var currentUnsubscribe;
  function ChatMessage(id, senderId, text, timestamp, status) {
    id = id === VOID ? '' : id;
    senderId = senderId === VOID ? '' : senderId;
    text = text === VOID ? '' : text;
    timestamp = timestamp === VOID ? new Long(0, 0) : timestamp;
    status = status === VOID ? 'sent' : status;
    this.xb_1 = id;
    this.yb_1 = senderId;
    this.zb_1 = text;
    this.ac_1 = timestamp;
    this.bc_1 = status;
  }
  protoOf(ChatMessage).toString = function () {
    return 'ChatMessage(id=' + this.xb_1 + ', senderId=' + this.yb_1 + ', text=' + this.zb_1 + ', timestamp=' + this.ac_1.toString() + ', status=' + this.bc_1 + ')';
  };
  protoOf(ChatMessage).hashCode = function () {
    var result = getStringHashCode(this.xb_1);
    result = imul(result, 31) + getStringHashCode(this.yb_1) | 0;
    result = imul(result, 31) + getStringHashCode(this.zb_1) | 0;
    result = imul(result, 31) + this.ac_1.hashCode() | 0;
    result = imul(result, 31) + getStringHashCode(this.bc_1) | 0;
    return result;
  };
  protoOf(ChatMessage).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof ChatMessage))
      return false;
    var tmp0_other_with_cast = other instanceof ChatMessage ? other : THROW_CCE();
    if (!(this.xb_1 === tmp0_other_with_cast.xb_1))
      return false;
    if (!(this.yb_1 === tmp0_other_with_cast.yb_1))
      return false;
    if (!(this.zb_1 === tmp0_other_with_cast.zb_1))
      return false;
    if (!this.ac_1.equals(tmp0_other_with_cast.ac_1))
      return false;
    if (!(this.bc_1 === tmp0_other_with_cast.bc_1))
      return false;
    return true;
  };
  function chatRoomId(id1, id2) {
    var sorted_0 = sorted(listOf([id1, id2]));
    return sorted_0.o(0) + '_' + sorted_0.o(1);
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
      return Unit_instance;
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
    var serverTimestamp = require('firebase/compat/database').ServerValue.TIMESTAMP;
    var data = json([to('senderId', senderId), to('text', text), to('timestamp', serverTimestamp), to('status', 'sent')]);
    return ref.set(data).then(sendMessage$lambda);
  }
  function sam$kotlin_Comparator$0_2(function_0) {
    this.hc_1 = function_0;
  }
  protoOf(sam$kotlin_Comparator$0_2).e7 = function (a, b) {
    return this.hc_1(a, b);
  };
  protoOf(sam$kotlin_Comparator$0_2).compare = function (a, b) {
    return this.e7(a, b);
  };
  protoOf(sam$kotlin_Comparator$0_2).e2 = function () {
    return this.hc_1;
  };
  protoOf(sam$kotlin_Comparator$0_2).equals = function (other) {
    var tmp;
    if (!(other == null) ? isInterface(other, Comparator) : false) {
      var tmp_0;
      if (!(other == null) ? isInterface(other, FunctionAdapter) : false) {
        tmp_0 = equals(this.e2(), other.e2());
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
    return hashCode(this.e2());
  };
  function subscribeMessages$lambda$lambda(a, b) {
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp = a.ac_1;
    var tmp$ret$1 = b.ac_1;
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
          list.e(new ChatMessage(element, tmp_2, tmp_4, tmp_5, tmp8_elvis_lhs == null ? 'sent' : tmp8_elvis_lhs));
        }
        tmp = Unit_instance;
      }
      // Inline function 'kotlin.collections.sortBy' call
      if (list.j() > 1) {
        // Inline function 'kotlin.comparisons.compareBy' call
        var tmp_7 = subscribeMessages$lambda$lambda;
        var tmp$ret$5 = new sam$kotlin_Comparator$0_2(tmp_7);
        sortWith(list, tmp$ret$5);
      }
      $callback(list);
      return Unit_instance;
    };
  }
  function subscribeMessages$lambda_0($ref, $listener) {
    return function () {
      $ref.off('value', $listener);
      currentUnsubscribe = null;
      return Unit_instance;
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
    this.fb_1 = id;
    this.gb_1 = instructorId;
    this.hb_1 = cadetId;
    this.ib_1 = startTimeMillis;
    this.jb_1 = status;
    this.kb_1 = instructorRating;
    this.lb_1 = cadetRating;
    this.mb_1 = openWindowId;
    this.nb_1 = instructorConfirmed;
  }
  protoOf(DrivingSession).toString = function () {
    return 'DrivingSession(id=' + this.fb_1 + ', instructorId=' + this.gb_1 + ', cadetId=' + this.hb_1 + ', startTimeMillis=' + toString(this.ib_1) + ', status=' + this.jb_1 + ', instructorRating=' + this.kb_1 + ', cadetRating=' + this.lb_1 + ', openWindowId=' + this.mb_1 + ', instructorConfirmed=' + this.nb_1 + ')';
  };
  protoOf(DrivingSession).hashCode = function () {
    var result = getStringHashCode(this.fb_1);
    result = imul(result, 31) + getStringHashCode(this.gb_1) | 0;
    result = imul(result, 31) + getStringHashCode(this.hb_1) | 0;
    result = imul(result, 31) + (this.ib_1 == null ? 0 : this.ib_1.hashCode()) | 0;
    result = imul(result, 31) + getStringHashCode(this.jb_1) | 0;
    result = imul(result, 31) + this.kb_1 | 0;
    result = imul(result, 31) + this.lb_1 | 0;
    result = imul(result, 31) + getStringHashCode(this.mb_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.nb_1) | 0;
    return result;
  };
  protoOf(DrivingSession).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof DrivingSession))
      return false;
    var tmp0_other_with_cast = other instanceof DrivingSession ? other : THROW_CCE();
    if (!(this.fb_1 === tmp0_other_with_cast.fb_1))
      return false;
    if (!(this.gb_1 === tmp0_other_with_cast.gb_1))
      return false;
    if (!(this.hb_1 === tmp0_other_with_cast.hb_1))
      return false;
    if (!equals(this.ib_1, tmp0_other_with_cast.ib_1))
      return false;
    if (!(this.jb_1 === tmp0_other_with_cast.jb_1))
      return false;
    if (!(this.kb_1 === tmp0_other_with_cast.kb_1))
      return false;
    if (!(this.lb_1 === tmp0_other_with_cast.lb_1))
      return false;
    if (!(this.mb_1 === tmp0_other_with_cast.mb_1))
      return false;
    if (!(this.nb_1 === tmp0_other_with_cast.nb_1))
      return false;
    return true;
  };
  function InstructorOpenWindow(id, instructorId, cadetId, dateTimeMillis, status) {
    id = id === VOID ? '' : id;
    instructorId = instructorId === VOID ? '' : instructorId;
    cadetId = cadetId === VOID ? null : cadetId;
    dateTimeMillis = dateTimeMillis === VOID ? null : dateTimeMillis;
    status = status === VOID ? '' : status;
    this.cc_1 = id;
    this.dc_1 = instructorId;
    this.ec_1 = cadetId;
    this.fc_1 = dateTimeMillis;
    this.gc_1 = status;
  }
  protoOf(InstructorOpenWindow).toString = function () {
    return 'InstructorOpenWindow(id=' + this.cc_1 + ', instructorId=' + this.dc_1 + ', cadetId=' + this.ec_1 + ', dateTimeMillis=' + toString(this.fc_1) + ', status=' + this.gc_1 + ')';
  };
  protoOf(InstructorOpenWindow).hashCode = function () {
    var result = getStringHashCode(this.cc_1);
    result = imul(result, 31) + getStringHashCode(this.dc_1) | 0;
    result = imul(result, 31) + (this.ec_1 == null ? 0 : getStringHashCode(this.ec_1)) | 0;
    result = imul(result, 31) + (this.fc_1 == null ? 0 : this.fc_1.hashCode()) | 0;
    result = imul(result, 31) + getStringHashCode(this.gc_1) | 0;
    return result;
  };
  protoOf(InstructorOpenWindow).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof InstructorOpenWindow))
      return false;
    var tmp0_other_with_cast = other instanceof InstructorOpenWindow ? other : THROW_CCE();
    if (!(this.cc_1 === tmp0_other_with_cast.cc_1))
      return false;
    if (!(this.dc_1 === tmp0_other_with_cast.dc_1))
      return false;
    if (!(this.ec_1 == tmp0_other_with_cast.ec_1))
      return false;
    if (!equals(this.fc_1, tmp0_other_with_cast.fc_1))
      return false;
    if (!(this.gc_1 === tmp0_other_with_cast.gc_1))
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
    this.ob_1 = id;
    this.pb_1 = userId;
    this.qb_1 = amount;
    this.rb_1 = type;
    this.sb_1 = performedBy;
    this.tb_1 = timestampMillis;
  }
  protoOf(BalanceHistoryEntry).toString = function () {
    return 'BalanceHistoryEntry(id=' + this.ob_1 + ', userId=' + this.pb_1 + ', amount=' + this.qb_1 + ', type=' + this.rb_1 + ', performedBy=' + this.sb_1 + ', timestampMillis=' + toString(this.tb_1) + ')';
  };
  protoOf(BalanceHistoryEntry).hashCode = function () {
    var result = getStringHashCode(this.ob_1);
    result = imul(result, 31) + getStringHashCode(this.pb_1) | 0;
    result = imul(result, 31) + this.qb_1 | 0;
    result = imul(result, 31) + getStringHashCode(this.rb_1) | 0;
    result = imul(result, 31) + getStringHashCode(this.sb_1) | 0;
    result = imul(result, 31) + (this.tb_1 == null ? 0 : this.tb_1.hashCode()) | 0;
    return result;
  };
  protoOf(BalanceHistoryEntry).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof BalanceHistoryEntry))
      return false;
    var tmp0_other_with_cast = other instanceof BalanceHistoryEntry ? other : THROW_CCE();
    if (!(this.ob_1 === tmp0_other_with_cast.ob_1))
      return false;
    if (!(this.pb_1 === tmp0_other_with_cast.pb_1))
      return false;
    if (!(this.qb_1 === tmp0_other_with_cast.qb_1))
      return false;
    if (!(this.rb_1 === tmp0_other_with_cast.rb_1))
      return false;
    if (!(this.sb_1 === tmp0_other_with_cast.sb_1))
      return false;
    if (!equals(this.tb_1, tmp0_other_with_cast.tb_1))
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
        tmp_4 = tmp6_safe_receiver.y1(toLong(1000));
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
    if (userIds.q()) {
      callback(emptyList());
      return Unit_instance;
    }
    // Inline function 'kotlin.collections.mutableListOf' call
    var results = ArrayList_init_$Create$();
    var pending = {_v: userIds.j()};
    // Inline function 'kotlin.collections.forEach' call
    var _iterator__ex2g4s = userIds.g();
    while (_iterator__ex2g4s.h()) {
      var element = _iterator__ex2g4s.i();
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
        var inductionVariable = this_0.p7_1;
        var last = this_0.q7_1;
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
            destination.e(tmp$ret$1);
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
      return Unit_instance;
    };
  }
  function getSessionsForInstructor$lambda_0($callback) {
    return function (_unused_var__etf5q3) {
      $callback(emptyList());
      return Unit_instance;
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
        var inductionVariable = this_0.p7_1;
        var last = this_0.q7_1;
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
            destination.e(tmp$ret$1);
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
      return Unit_instance;
    };
  }
  function getSessionsForCadet$lambda_0($callback) {
    return function (_unused_var__etf5q3) {
      $callback(emptyList());
      return Unit_instance;
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
        var inductionVariable = this_0.p7_1;
        var last = this_0.q7_1;
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
            destination.e(tmp$ret$1);
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
      return Unit_instance;
    };
  }
  function getOpenWindowsForInstructor$lambda_0($callback) {
    return function (_unused_var__etf5q3) {
      $callback(emptyList());
      return Unit_instance;
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
        var inductionVariable = this_0.p7_1;
        var last = this_0.q7_1;
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
            destination.e(tmp$ret$1);
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
      return Unit_instance;
    };
  }
  function getOpenWindowsForCadet$lambda_0($callback) {
    return function (_unused_var__etf5q3) {
      $callback(emptyList());
      return Unit_instance;
    };
  }
  function addOpenWindow$lambda($callback, $ref) {
    return function () {
      $callback($ref.id, null);
      return Unit_instance;
    };
  }
  function addOpenWindow$lambda_0($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.message;
      var tmp0_elvis_lhs = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $callback(null, tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430' : tmp0_elvis_lhs);
      return Unit_instance;
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
      return Unit_instance;
    };
  }
  function bookWindow$lambda_2($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.message;
      var tmp0_elvis_lhs = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $callback(tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430 \u0431\u0440\u043E\u043D\u0438\u0440\u043E\u0432\u0430\u043D\u0438\u044F' : tmp0_elvis_lhs);
      return Unit_instance;
    };
  }
  function deleteOpenWindow$lambda($callback) {
    return function () {
      $callback(null);
      return Unit_instance;
    };
  }
  function deleteOpenWindow$lambda_0($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.message;
      var tmp0_elvis_lhs = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $callback(tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430' : tmp0_elvis_lhs);
      return Unit_instance;
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
        var inductionVariable = this_0.p7_1;
        var last = this_0.q7_1;
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
            destination.e(tmp$ret$1);
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
      return Unit_instance;
    };
  }
  function getBalanceHistory$lambda_0($callback) {
    return function (_unused_var__etf5q3) {
      $callback(emptyList());
      return Unit_instance;
    };
  }
  function sam$kotlin_Comparator$0_3(function_0) {
    this.ic_1 = function_0;
  }
  protoOf(sam$kotlin_Comparator$0_3).e7 = function (a, b) {
    return this.ic_1(a, b);
  };
  protoOf(sam$kotlin_Comparator$0_3).compare = function (a, b) {
    return this.e7(a, b);
  };
  protoOf(sam$kotlin_Comparator$0_3).e2 = function () {
    return this.ic_1;
  };
  protoOf(sam$kotlin_Comparator$0_3).equals = function (other) {
    var tmp;
    if (!(other == null) ? isInterface(other, Comparator) : false) {
      var tmp_0;
      if (!(other == null) ? isInterface(other, FunctionAdapter) : false) {
        tmp_0 = equals(this.e2(), other.e2());
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
    return hashCode(this.e2());
  };
  function loadBalanceHistoryForUsers$lambda$lambda(a, b) {
    // Inline function 'kotlin.comparisons.compareValuesBy' call
    var tmp0_elvis_lhs = b.tb_1;
    var tmp = tmp0_elvis_lhs == null ? new Long(0, 0) : tmp0_elvis_lhs;
    var tmp0_elvis_lhs_0 = a.tb_1;
    var tmp$ret$1 = tmp0_elvis_lhs_0 == null ? new Long(0, 0) : tmp0_elvis_lhs_0;
    return compareValues(tmp, tmp$ret$1);
  }
  function loadBalanceHistoryForUsers$lambda($results, $pending, $callback) {
    return function (list) {
      $results.p(list);
      var _unary__edvuaz = $pending._v;
      $pending._v = _unary__edvuaz - 1 | 0;
      var tmp;
      if ($pending._v === 0) {
        // Inline function 'kotlin.collections.sortedByDescending' call
        var this_0 = $results;
        // Inline function 'kotlin.comparisons.compareByDescending' call
        var tmp_0 = loadBalanceHistoryForUsers$lambda$lambda;
        var tmp$ret$0 = new sam$kotlin_Comparator$0_3(tmp_0);
        var tmp$ret$1 = sortedWith(this_0, tmp$ret$0);
        tmp = $callback(take(tmp$ret$1, 50));
      }
      return Unit_instance;
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
      return Unit_instance;
    };
  }
  function updateBalance$lambda_1($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.message;
      var tmp0_elvis_lhs = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $callback(tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430' : tmp0_elvis_lhs);
      return Unit_instance;
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
      return Unit_instance;
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
          destination.e(tmp0_safe_receiver);
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
      return Unit_instance;
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
  function getUsersForChat(currentUser, callback) {
    _init_properties_Firebase_kt__4razx5();
    getUsers(getUsersForChat$lambda(currentUser, callback));
  }
  function onAuthStateChanged$lambda($callback) {
    return function (user) {
      var tmp = user == null ? null : user.uid;
      $callback((!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null);
      return Unit_instance;
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
      return Unit_instance;
    };
  }
  function updateProfile$lambda_0($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.message;
      var tmp0_elvis_lhs = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $callback(tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430' : tmp0_elvis_lhs);
      return Unit_instance;
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
        return Unit_instance;
      }
      // Inline function 'kotlin.js.unsafeCast' call
      var d = doc.data();
      var tmp_0;
      if (d == null) {
        $callback(null, '\u0414\u0430\u043D\u043D\u044B\u0435 \u043F\u0440\u043E\u0444\u0438\u043B\u044F \u043F\u0443\u0441\u0442\u044B.');
        return Unit_instance;
      }
      var user = parseUserFromDoc(doc, d);
      $callback(user, null);
      return Unit_instance;
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
      return Unit_instance;
    };
  }
  function getUsers$lambda($callback) {
    return function (list, _unused_var__etf5q3) {
      $callback(list);
      return Unit_instance;
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
        var inductionVariable = tmp0.p7_1;
        var last = tmp0.q7_1;
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
              destination.e(tmp0_safe_receiver);
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
      return Unit_instance;
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
      return Unit_instance;
    };
  }
  function getUserById$lambda($callback) {
    return function (doc) {
      var tmp;
      if ((doc == null ? null : doc.exists) != true) {
        $callback(null);
        return Unit_instance;
      }
      // Inline function 'kotlin.js.unsafeCast' call
      var docD = doc;
      var d = docD.data();
      var tmp_0;
      if (d == null) {
        $callback(null);
        return Unit_instance;
      }
      $callback(parseUserFromDoc(docD, d));
      return Unit_instance;
    };
  }
  function getUserById$lambda_0($callback) {
    return function (_unused_var__etf5q3) {
      $callback(null);
      return Unit_instance;
    };
  }
  function setActive$lambda($callback) {
    return function () {
      $callback(null);
      return Unit_instance;
    };
  }
  function setActive$lambda_0($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.message;
      var tmp0_elvis_lhs = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $callback(tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430' : tmp0_elvis_lhs);
      return Unit_instance;
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
        var _iterator__ex2g4s = tmp2_safe_receiver.g();
        while (_iterator__ex2g4s.h()) {
          var element = _iterator__ex2g4s.i();
          var tmp0_safe_receiver = element == null ? null : toString_0(element);
          if (tmp0_safe_receiver == null)
            null;
          else {
            // Inline function 'kotlin.let' call
            destination.e(tmp0_safe_receiver);
          }
        }
        tmp_0 = destination;
      }
      var tmp3_elvis_lhs = tmp_0;
      var existing = tmp3_elvis_lhs == null ? emptyList() : tmp3_elvis_lhs;
      var list = toMutableList(existing);
      var tmp_1;
      if (!list.g1($cadetId)) {
        list.e($cadetId);
        tmp_1 = Unit_instance;
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
      return Unit_instance;
    };
  }
  function assignCadetToInstructor$lambda_1($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.message;
      var tmp0_elvis_lhs = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $callback(tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430' : tmp0_elvis_lhs);
      return Unit_instance;
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
        var _iterator__ex2g4s = tmp2_safe_receiver.g();
        while (_iterator__ex2g4s.h()) {
          var element = _iterator__ex2g4s.i();
          var tmp0_safe_receiver = element == null ? null : toString_0(element);
          if (tmp0_safe_receiver == null)
            null;
          else {
            // Inline function 'kotlin.let' call
            destination.e(tmp0_safe_receiver);
          }
        }
        tmp_0 = destination;
      }
      var tmp3_elvis_lhs = tmp_0;
      var list = toMutableList(tmp3_elvis_lhs == null ? emptyList() : tmp3_elvis_lhs);
      list.j2($cadetId);
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
      return Unit_instance;
    };
  }
  function removeCadetFromInstructor$lambda_1($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.message;
      var tmp0_elvis_lhs = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $callback(tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430' : tmp0_elvis_lhs);
      return Unit_instance;
    };
  }
  function deleteUser$lambda($callback) {
    return function () {
      $callback(null);
      return Unit_instance;
    };
  }
  function deleteUser$lambda_0($callback) {
    return function (e) {
      // Inline function 'kotlin.js.asDynamic' call
      var tmp = e.message;
      var tmp0_elvis_lhs = (!(tmp == null) ? typeof tmp === 'string' : false) ? tmp : null;
      $callback(tmp0_elvis_lhs == null ? '\u041E\u0448\u0438\u0431\u043A\u0430' : tmp0_elvis_lhs);
      return Unit_instance;
    };
  }
  function getUsersForChat$lambda($currentUser, $callback) {
    return function (all) {
      var tmp;
      switch ($currentUser.p9_1) {
        case 'admin':
          // Inline function 'kotlin.collections.filter' call

          // Inline function 'kotlin.collections.filterTo' call

          var destination = ArrayList_init_$Create$();
          var _iterator__ex2g4s = all.g();
          while (_iterator__ex2g4s.h()) {
            var element = _iterator__ex2g4s.i();
            if (!(element.p9_1 === 'admin')) {
              destination.e(element);
            }
          }

          tmp = destination;
          break;
        case 'instructor':
          var tmp$ret$4;
          $l$block: {
            // Inline function 'kotlin.collections.firstOrNull' call
            var _iterator__ex2g4s_0 = all.g();
            while (_iterator__ex2g4s_0.h()) {
              var element_0 = _iterator__ex2g4s_0.i();
              if (element_0.p9_1 === 'admin') {
                tmp$ret$4 = element_0;
                break $l$block;
              }
            }
            tmp$ret$4 = null;
          }

          var admin = tmp$ret$4;
          // Inline function 'kotlin.collections.mapNotNull' call

          var tmp0 = $currentUser.t9_1;
          // Inline function 'kotlin.collections.mapNotNullTo' call

          var destination_0 = ArrayList_init_$Create$();
          // Inline function 'kotlin.collections.forEach' call

          var _iterator__ex2g4s_1 = tmp0.g();
          while (_iterator__ex2g4s_1.h()) {
            var element_1 = _iterator__ex2g4s_1.i();
            // Inline function 'kotlin.collections.find' call
            var tmp$ret$6;
            $l$block_0: {
              // Inline function 'kotlin.collections.firstOrNull' call
              var _iterator__ex2g4s_2 = all.g();
              while (_iterator__ex2g4s_2.h()) {
                var element_2 = _iterator__ex2g4s_2.i();
                if (element_2.l9_1 === element_1) {
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
              destination_0.e(tmp0_safe_receiver);
            }
          }

          var cadets = destination_0;
          tmp = plus(listOfNotNull(admin), cadets);
          break;
        case 'cadet':
          var tmp$ret$16;
          $l$block_1: {
            // Inline function 'kotlin.collections.firstOrNull' call
            var _iterator__ex2g4s_3 = all.g();
            while (_iterator__ex2g4s_3.h()) {
              var element_3 = _iterator__ex2g4s_3.i();
              if (element_3.p9_1 === 'admin') {
                tmp$ret$16 = element_3;
                break $l$block_1;
              }
            }
            tmp$ret$16 = null;
          }

          var admin_0 = tmp$ret$16;
          var tmp1_safe_receiver = $currentUser.s9_1;
          var tmp_0;
          if (tmp1_safe_receiver == null) {
            tmp_0 = null;
          } else {
            // Inline function 'kotlin.let' call
            // Inline function 'kotlin.collections.find' call
            var tmp$ret$18;
            $l$block_2: {
              // Inline function 'kotlin.collections.firstOrNull' call
              var _iterator__ex2g4s_4 = all.g();
              while (_iterator__ex2g4s_4.h()) {
                var element_4 = _iterator__ex2g4s_4.i();
                if (element_4.l9_1 === tmp1_safe_receiver) {
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
      return Unit_instance;
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
  iconPhoneSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/><\/svg>';
  iconChatSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/><\/svg>';
  iconUserPlusSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/><\/svg>';
  iconPowerSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18.36 6.64a9 9 0 1 1-12.73 0"/><line x1="12" y1="2" x2="12" y2="12"/><\/svg>';
  iconTrashSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><\/svg>';
  iconUnlinkSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18.84 12.25l5.72-5.72a2.5 2.5 0 0 0-3.54-3.54l-5.72 5.72"/><path d="M5.16 11.75l-5.72 5.72a2.5 2.5 0 0 0 3.54 3.54l5.72-5.72"/><line x1="8" y1="16" x2="16" y2="8"/><\/svg>';
  iconUserSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/><\/svg>';
  iconPhoneLabelSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/><\/svg>';
  iconTicketSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2Z"/><path d="M13 5v2"/><path d="M13 17v2"/><path d="M13 11v2"/><\/svg>';
  iconInstructorSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/><\/svg>';
  iconSelectSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/><\/svg>';
  iconCreditSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/><\/svg>';
  iconDebitSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"/><\/svg>';
  iconSetSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="9" x2="19" y2="9"/><line x1="5" y1="15" x2="19" y2="15"/><\/svg>';
  iconResetSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/><\/svg>';
  currentUnsubscribe = null;
  //endregion
  mainWrapper();
  return _;
}));

//# sourceMappingURL=StartDrive-webApp.js.map
