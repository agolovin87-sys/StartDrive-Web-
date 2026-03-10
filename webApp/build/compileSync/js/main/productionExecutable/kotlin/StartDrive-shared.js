(function (factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports', './kotlin-kotlin-stdlib.js', './kotlinx-serialization-kotlinx-serialization-core.js'], factory);
  else if (typeof exports === 'object')
    factory(module.exports, require('./kotlin-kotlin-stdlib.js'), require('./kotlinx-serialization-kotlinx-serialization-core.js'));
  else {
    if (typeof globalThis['kotlin-kotlin-stdlib'] === 'undefined') {
      throw new Error("Error loading module 'StartDrive-shared'. Its dependency 'kotlin-kotlin-stdlib' was not found. Please, check whether 'kotlin-kotlin-stdlib' is loaded prior to 'StartDrive-shared'.");
    }
    if (typeof globalThis['kotlinx-serialization-kotlinx-serialization-core'] === 'undefined') {
      throw new Error("Error loading module 'StartDrive-shared'. Its dependency 'kotlinx-serialization-kotlinx-serialization-core' was not found. Please, check whether 'kotlinx-serialization-kotlinx-serialization-core' is loaded prior to 'StartDrive-shared'.");
    }
    globalThis['StartDrive-shared'] = factory(typeof globalThis['StartDrive-shared'] === 'undefined' ? {} : globalThis['StartDrive-shared'], globalThis['kotlin-kotlin-stdlib'], globalThis['kotlinx-serialization-kotlinx-serialization-core']);
  }
}(function (_, kotlin_kotlin, kotlin_org_jetbrains_kotlinx_kotlinx_serialization_core) {
  'use strict';
  //region block: imports
  var imul = Math.imul;
  var ensureNotNull = kotlin_kotlin.$_$.o2;
  var protoOf = kotlin_kotlin.$_$.t1;
  var initMetadataForObject = kotlin_kotlin.$_$.l1;
  var initMetadataForCompanion = kotlin_kotlin.$_$.k1;
  var getStringHashCode = kotlin_kotlin.$_$.h1;
  var THROW_CCE = kotlin_kotlin.$_$.n2;
  var initMetadataForClass = kotlin_kotlin.$_$.j1;
  var StringSerializer_getInstance = kotlin_org_jetbrains_kotlinx_kotlinx_serialization_core.$_$.a;
  var ArrayListSerializer = kotlin_org_jetbrains_kotlinx_kotlinx_serialization_core.$_$.b;
  var LazyThreadSafetyMode_PUBLICATION_getInstance = kotlin_kotlin.$_$.c;
  var lazy = kotlin_kotlin.$_$.p2;
  var VOID = kotlin_kotlin.$_$.b;
  var emptyList = kotlin_kotlin.$_$.p;
  var split = kotlin_kotlin.$_$.b2;
  var getOrNull = kotlin_kotlin.$_$.q;
  var firstOrNull = kotlin_kotlin.$_$.z1;
  var toString = kotlin_kotlin.$_$.g;
  var Char = kotlin_kotlin.$_$.i2;
  var isCharSequence = kotlin_kotlin.$_$.n1;
  var trim = kotlin_kotlin.$_$.h2;
  var toString_0 = kotlin_kotlin.$_$.v1;
  var toString_1 = kotlin_kotlin.$_$.r2;
  var hashCode = kotlin_kotlin.$_$.i1;
  var getBooleanHashCode = kotlin_kotlin.$_$.g1;
  var equals = kotlin_kotlin.$_$.f1;
  //endregion
  //region block: pre-declaration
  initMetadataForObject(SharedFactory, 'SharedFactory');
  initMetadataForCompanion(Companion);
  initMetadataForClass(AppInfo, 'AppInfo');
  initMetadataForCompanion(Companion_0);
  initMetadataForClass(User, 'User', User);
  initMetadataForCompanion(Companion_1);
  initMetadataForClass(DefaultAppInfoRepository, 'DefaultAppInfoRepository', DefaultAppInfoRepository);
  //endregion
  function appInfoRepository($this) {
    if ($this.f9_1 == null)
      $this.f9_1 = new DefaultAppInfoRepository();
    return ensureNotNull($this.f9_1);
  }
  function SharedFactory() {
    this.f9_1 = null;
  }
  protoOf(SharedFactory).g9 = function () {
    return appInfoRepository(this);
  };
  var SharedFactory_instance;
  function SharedFactory_getInstance() {
    return SharedFactory_instance;
  }
  function Companion() {
  }
  var Companion_instance;
  function Companion_getInstance() {
    return Companion_instance;
  }
  function AppInfo(appName, version, platform) {
    this.h9_1 = appName;
    this.i9_1 = version;
    this.j9_1 = platform;
  }
  protoOf(AppInfo).toString = function () {
    return 'AppInfo(appName=' + this.h9_1 + ', version=' + this.i9_1 + ', platform=' + this.j9_1 + ')';
  };
  protoOf(AppInfo).hashCode = function () {
    var result = getStringHashCode(this.h9_1);
    result = imul(result, 31) + getStringHashCode(this.i9_1) | 0;
    result = imul(result, 31) + getStringHashCode(this.j9_1) | 0;
    return result;
  };
  protoOf(AppInfo).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof AppInfo))
      return false;
    var tmp0_other_with_cast = other instanceof AppInfo ? other : THROW_CCE();
    if (!(this.h9_1 === tmp0_other_with_cast.h9_1))
      return false;
    if (!(this.i9_1 === tmp0_other_with_cast.i9_1))
      return false;
    if (!(this.j9_1 === tmp0_other_with_cast.j9_1))
      return false;
    return true;
  };
  function User$Companion$$childSerializers$_anonymous__6nf9sv() {
    return new ArrayListSerializer(StringSerializer_getInstance());
  }
  function Companion_0() {
    Companion_instance_0 = this;
    var tmp = this;
    var tmp_0 = LazyThreadSafetyMode_PUBLICATION_getInstance();
    // Inline function 'kotlin.arrayOf' call
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    tmp.k9_1 = [null, null, null, null, null, null, null, null, lazy(tmp_0, User$Companion$$childSerializers$_anonymous__6nf9sv), null, null, null];
  }
  var Companion_instance_0;
  function Companion_getInstance_0() {
    if (Companion_instance_0 == null)
      new Companion_0();
    return Companion_instance_0;
  }
  function User(id, fullName, email, phone, role, balance, fcmToken, assignedInstructorId, assignedCadets, isActive, createdAtMillis, chatAvatarUrl) {
    Companion_getInstance_0();
    id = id === VOID ? '' : id;
    fullName = fullName === VOID ? '' : fullName;
    email = email === VOID ? '' : email;
    phone = phone === VOID ? '' : phone;
    role = role === VOID ? '' : role;
    balance = balance === VOID ? 0 : balance;
    fcmToken = fcmToken === VOID ? null : fcmToken;
    assignedInstructorId = assignedInstructorId === VOID ? null : assignedInstructorId;
    assignedCadets = assignedCadets === VOID ? emptyList() : assignedCadets;
    isActive = isActive === VOID ? false : isActive;
    createdAtMillis = createdAtMillis === VOID ? null : createdAtMillis;
    chatAvatarUrl = chatAvatarUrl === VOID ? null : chatAvatarUrl;
    this.l9_1 = id;
    this.m9_1 = fullName;
    this.n9_1 = email;
    this.o9_1 = phone;
    this.p9_1 = role;
    this.q9_1 = balance;
    this.r9_1 = fcmToken;
    this.s9_1 = assignedInstructorId;
    this.t9_1 = assignedCadets;
    this.u9_1 = isActive;
    this.v9_1 = createdAtMillis;
    this.w9_1 = chatAvatarUrl;
  }
  protoOf(User).x9 = function () {
    var parts = split(this.m9_1, [' ']);
    var tmp0_elvis_lhs = getOrNull(parts, 0);
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return this.m9_1;
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var first = tmp;
    var tmp1_safe_receiver = getOrNull(parts, 1);
    var tmp2_safe_receiver = tmp1_safe_receiver == null ? null : firstOrNull(tmp1_safe_receiver);
    var tmp_0;
    var tmp_1 = tmp2_safe_receiver;
    if ((tmp_1 == null ? null : new Char(tmp_1)) == null) {
      tmp_0 = null;
    } else {
      // Inline function 'kotlin.text.plus' call
      tmp_0 = toString(tmp2_safe_receiver) + '.';
    }
    var tmp3_elvis_lhs = tmp_0;
    var second = tmp3_elvis_lhs == null ? '' : tmp3_elvis_lhs;
    // Inline function 'kotlin.text.trim' call
    var this_0 = first + ' ' + second;
    return toString_0(trim(isCharSequence(this_0) ? this_0 : THROW_CCE()));
  };
  protoOf(User).toString = function () {
    return 'User(id=' + this.l9_1 + ', fullName=' + this.m9_1 + ', email=' + this.n9_1 + ', phone=' + this.o9_1 + ', role=' + this.p9_1 + ', balance=' + this.q9_1 + ', fcmToken=' + this.r9_1 + ', assignedInstructorId=' + this.s9_1 + ', assignedCadets=' + toString_0(this.t9_1) + ', isActive=' + this.u9_1 + ', createdAtMillis=' + toString_1(this.v9_1) + ', chatAvatarUrl=' + this.w9_1 + ')';
  };
  protoOf(User).hashCode = function () {
    var result = getStringHashCode(this.l9_1);
    result = imul(result, 31) + getStringHashCode(this.m9_1) | 0;
    result = imul(result, 31) + getStringHashCode(this.n9_1) | 0;
    result = imul(result, 31) + getStringHashCode(this.o9_1) | 0;
    result = imul(result, 31) + getStringHashCode(this.p9_1) | 0;
    result = imul(result, 31) + this.q9_1 | 0;
    result = imul(result, 31) + (this.r9_1 == null ? 0 : getStringHashCode(this.r9_1)) | 0;
    result = imul(result, 31) + (this.s9_1 == null ? 0 : getStringHashCode(this.s9_1)) | 0;
    result = imul(result, 31) + hashCode(this.t9_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.u9_1) | 0;
    result = imul(result, 31) + (this.v9_1 == null ? 0 : this.v9_1.hashCode()) | 0;
    result = imul(result, 31) + (this.w9_1 == null ? 0 : getStringHashCode(this.w9_1)) | 0;
    return result;
  };
  protoOf(User).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof User))
      return false;
    var tmp0_other_with_cast = other instanceof User ? other : THROW_CCE();
    if (!(this.l9_1 === tmp0_other_with_cast.l9_1))
      return false;
    if (!(this.m9_1 === tmp0_other_with_cast.m9_1))
      return false;
    if (!(this.n9_1 === tmp0_other_with_cast.n9_1))
      return false;
    if (!(this.o9_1 === tmp0_other_with_cast.o9_1))
      return false;
    if (!(this.p9_1 === tmp0_other_with_cast.p9_1))
      return false;
    if (!(this.q9_1 === tmp0_other_with_cast.q9_1))
      return false;
    if (!(this.r9_1 == tmp0_other_with_cast.r9_1))
      return false;
    if (!(this.s9_1 == tmp0_other_with_cast.s9_1))
      return false;
    if (!equals(this.t9_1, tmp0_other_with_cast.t9_1))
      return false;
    if (!(this.u9_1 === tmp0_other_with_cast.u9_1))
      return false;
    if (!equals(this.v9_1, tmp0_other_with_cast.v9_1))
      return false;
    if (!(this.w9_1 == tmp0_other_with_cast.w9_1))
      return false;
    return true;
  };
  function Companion_1() {
    this.y9_1 = '1.0.0';
  }
  var Companion_instance_1;
  function Companion_getInstance_1() {
    return Companion_instance_1;
  }
  function DefaultAppInfoRepository() {
  }
  protoOf(DefaultAppInfoRepository).z9 = function () {
    return new AppInfo('StartDrive', '1.0.0', platformName());
  };
  function platformName() {
    return 'Web';
  }
  //region block: init
  SharedFactory_instance = new SharedFactory();
  Companion_instance = new Companion();
  Companion_instance_1 = new Companion_1();
  //endregion
  //region block: exports
  _.$_$ = _.$_$ || {};
  _.$_$.a = User;
  _.$_$.b = SharedFactory_instance;
  //endregion
  return _;
}));

//# sourceMappingURL=StartDrive-shared.js.map
