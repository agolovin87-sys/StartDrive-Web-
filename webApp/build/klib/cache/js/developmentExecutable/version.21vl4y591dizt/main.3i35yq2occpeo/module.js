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
  var ensureNotNull = kotlin_kotlin.$_$.o8;
  var protoOf = kotlin_kotlin.$_$.w6;
  var initMetadataForObject = kotlin_kotlin.$_$.b6;
  var initMetadataForCompanion = kotlin_kotlin.$_$.z5;
  var PluginGeneratedSerialDescriptor = kotlin_org_jetbrains_kotlinx_kotlinx_serialization_core.$_$.p;
  var THROW_CCE = kotlin_kotlin.$_$.b8;
  var UnknownFieldException_init_$Create$ = kotlin_org_jetbrains_kotlinx_kotlinx_serialization_core.$_$.a;
  var StringSerializer_getInstance = kotlin_org_jetbrains_kotlinx_kotlinx_serialization_core.$_$.e;
  var typeParametersSerializers = kotlin_org_jetbrains_kotlinx_kotlinx_serialization_core.$_$.m;
  var GeneratedSerializer = kotlin_org_jetbrains_kotlinx_kotlinx_serialization_core.$_$.n;
  var VOID = kotlin_kotlin.$_$.e;
  var throwMissingFieldException = kotlin_org_jetbrains_kotlinx_kotlinx_serialization_core.$_$.q;
  var objectCreate = kotlin_kotlin.$_$.v6;
  var getStringHashCode = kotlin_kotlin.$_$.w5;
  var makeAssociatedObjectMapES5 = kotlin_kotlin.$_$.d;
  var SerializableWith = kotlin_org_jetbrains_kotlinx_kotlinx_serialization_core.$_$.r;
  var initMetadataForClass = kotlin_kotlin.$_$.y5;
  var ArrayListSerializer = kotlin_org_jetbrains_kotlinx_kotlinx_serialization_core.$_$.l;
  var LazyThreadSafetyMode_PUBLICATION_getInstance = kotlin_kotlin.$_$.f;
  var lazy = kotlin_kotlin.$_$.p8;
  var emptyList = kotlin_kotlin.$_$.l4;
  var equals = kotlin_kotlin.$_$.t5;
  var LongSerializer_getInstance = kotlin_org_jetbrains_kotlinx_kotlinx_serialization_core.$_$.d;
  var IntSerializer_getInstance = kotlin_org_jetbrains_kotlinx_kotlinx_serialization_core.$_$.c;
  var get_nullable = kotlin_org_jetbrains_kotlinx_kotlinx_serialization_core.$_$.f;
  var BooleanSerializer_getInstance = kotlin_org_jetbrains_kotlinx_kotlinx_serialization_core.$_$.b;
  var split = kotlin_kotlin.$_$.k7;
  var take = kotlin_kotlin.$_$.d5;
  var ArrayList_init_$Create$ = kotlin_kotlin.$_$.h;
  var firstOrNull = kotlin_kotlin.$_$.h7;
  var toString = kotlin_kotlin.$_$.d1;
  var Char = kotlin_kotlin.$_$.u7;
  var Unit_getInstance = kotlin_kotlin.$_$.e3;
  var joinToString = kotlin_kotlin.$_$.s4;
  var getOrNull = kotlin_kotlin.$_$.o4;
  var isCharSequence = kotlin_kotlin.$_$.g6;
  var trim = kotlin_kotlin.$_$.q7;
  var toString_0 = kotlin_kotlin.$_$.z6;
  var toString_1 = kotlin_kotlin.$_$.s8;
  var hashCode = kotlin_kotlin.$_$.x5;
  var getBooleanHashCode = kotlin_kotlin.$_$.u5;
  var initMetadataForInterface = kotlin_kotlin.$_$.a6;
  //endregion
  //region block: pre-declaration
  initMetadataForObject(SharedFactory, 'SharedFactory');
  initMetadataForCompanion(Companion);
  initMetadataForObject($serializer, '$serializer', VOID, VOID, [GeneratedSerializer]);
  initMetadataForClass(AppInfo, 'AppInfo', VOID, VOID, VOID, VOID, VOID, makeAssociatedObjectMapES5([SerializableWith, $serializer_getInstance]));
  initMetadataForCompanion(Companion_0);
  initMetadataForObject($serializer_0, '$serializer', VOID, VOID, [GeneratedSerializer]);
  initMetadataForClass(User, 'User', User, VOID, VOID, VOID, VOID, makeAssociatedObjectMapES5([SerializableWith, $serializer_getInstance_0]));
  initMetadataForInterface(AppInfoRepository, 'AppInfoRepository');
  initMetadataForCompanion(Companion_1);
  initMetadataForClass(DefaultAppInfoRepository, 'DefaultAppInfoRepository', DefaultAppInfoRepository, VOID, [AppInfoRepository]);
  //endregion
  function _set__appInfoRepository__d0e919($this, _set____db54di) {
    $this._appInfoRepository_1 = _set____db54di;
  }
  function _get__appInfoRepository__kxh233($this) {
    return $this._appInfoRepository_1;
  }
  function appInfoRepository($this) {
    if ($this._appInfoRepository_1 == null)
      $this._appInfoRepository_1 = new DefaultAppInfoRepository();
    return ensureNotNull($this._appInfoRepository_1);
  }
  function SharedFactory() {
    SharedFactory_instance = this;
    this._appInfoRepository_1 = null;
  }
  protoOf(SharedFactory).getAppInfoRepository_n0p3kz_k$ = function () {
    return appInfoRepository(this);
  };
  var SharedFactory_instance;
  function SharedFactory_getInstance() {
    if (SharedFactory_instance == null)
      new SharedFactory();
    return SharedFactory_instance;
  }
  function Companion() {
    Companion_instance = this;
  }
  protoOf(Companion).serializer_9w0wvi_k$ = function () {
    return $serializer_getInstance();
  };
  var Companion_instance;
  function Companion_getInstance() {
    if (Companion_instance == null)
      new Companion();
    return Companion_instance;
  }
  function $serializer() {
    $serializer_instance = this;
    var tmp0_serialDesc = new PluginGeneratedSerialDescriptor('com.example.startdrive.shared.model.AppInfo', this, 3);
    tmp0_serialDesc.addElement_5pzumi_k$('appName', false);
    tmp0_serialDesc.addElement_5pzumi_k$('version', false);
    tmp0_serialDesc.addElement_5pzumi_k$('platform', false);
    this.descriptor_1 = tmp0_serialDesc;
  }
  protoOf($serializer).serialize_dd03gu_k$ = function (encoder, value) {
    var tmp0_desc = this.descriptor_1;
    var tmp1_output = encoder.beginStructure_yljocp_k$(tmp0_desc);
    tmp1_output.encodeStringElement_1n5wu2_k$(tmp0_desc, 0, value.appName_1);
    tmp1_output.encodeStringElement_1n5wu2_k$(tmp0_desc, 1, value.version_1);
    tmp1_output.encodeStringElement_1n5wu2_k$(tmp0_desc, 2, value.platform_1);
    tmp1_output.endStructure_1xqz0n_k$(tmp0_desc);
  };
  protoOf($serializer).serialize_5ase3y_k$ = function (encoder, value) {
    return this.serialize_dd03gu_k$(encoder, value instanceof AppInfo ? value : THROW_CCE());
  };
  protoOf($serializer).deserialize_sy6x50_k$ = function (decoder) {
    var tmp0_desc = this.descriptor_1;
    var tmp1_flag = true;
    var tmp2_index = 0;
    var tmp3_bitMask0 = 0;
    var tmp4_local0 = null;
    var tmp5_local1 = null;
    var tmp6_local2 = null;
    var tmp7_input = decoder.beginStructure_yljocp_k$(tmp0_desc);
    if (tmp7_input.decodeSequentially_xlblqy_k$()) {
      tmp4_local0 = tmp7_input.decodeStringElement_3oenpg_k$(tmp0_desc, 0);
      tmp3_bitMask0 = tmp3_bitMask0 | 1;
      tmp5_local1 = tmp7_input.decodeStringElement_3oenpg_k$(tmp0_desc, 1);
      tmp3_bitMask0 = tmp3_bitMask0 | 2;
      tmp6_local2 = tmp7_input.decodeStringElement_3oenpg_k$(tmp0_desc, 2);
      tmp3_bitMask0 = tmp3_bitMask0 | 4;
    } else
      while (tmp1_flag) {
        tmp2_index = tmp7_input.decodeElementIndex_bstkhp_k$(tmp0_desc);
        switch (tmp2_index) {
          case -1:
            tmp1_flag = false;
            break;
          case 0:
            tmp4_local0 = tmp7_input.decodeStringElement_3oenpg_k$(tmp0_desc, 0);
            tmp3_bitMask0 = tmp3_bitMask0 | 1;
            break;
          case 1:
            tmp5_local1 = tmp7_input.decodeStringElement_3oenpg_k$(tmp0_desc, 1);
            tmp3_bitMask0 = tmp3_bitMask0 | 2;
            break;
          case 2:
            tmp6_local2 = tmp7_input.decodeStringElement_3oenpg_k$(tmp0_desc, 2);
            tmp3_bitMask0 = tmp3_bitMask0 | 4;
            break;
          default:
            throw UnknownFieldException_init_$Create$(tmp2_index);
        }
      }
    tmp7_input.endStructure_1xqz0n_k$(tmp0_desc);
    return AppInfo_init_$Create$(tmp3_bitMask0, tmp4_local0, tmp5_local1, tmp6_local2, null);
  };
  protoOf($serializer).get_descriptor_wjt6a0_k$ = function () {
    return this.descriptor_1;
  };
  protoOf($serializer).childSerializers_5ghqw5_k$ = function () {
    // Inline function 'kotlin.arrayOf' call
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    return [StringSerializer_getInstance(), StringSerializer_getInstance(), StringSerializer_getInstance()];
  };
  var $serializer_instance;
  function $serializer_getInstance() {
    if ($serializer_instance == null)
      new $serializer();
    return $serializer_instance;
  }
  function AppInfo_init_$Init$(seen0, appName, version, platform, serializationConstructorMarker, $this) {
    if (!(7 === (7 & seen0))) {
      throwMissingFieldException(seen0, 7, $serializer_getInstance().descriptor_1);
    }
    $this.appName_1 = appName;
    $this.version_1 = version;
    $this.platform_1 = platform;
    return $this;
  }
  function AppInfo_init_$Create$(seen0, appName, version, platform, serializationConstructorMarker) {
    return AppInfo_init_$Init$(seen0, appName, version, platform, serializationConstructorMarker, objectCreate(protoOf(AppInfo)));
  }
  function AppInfo(appName, version, platform) {
    Companion_getInstance();
    this.appName_1 = appName;
    this.version_1 = version;
    this.platform_1 = platform;
  }
  protoOf(AppInfo).get_appName_bv95lp_k$ = function () {
    return this.appName_1;
  };
  protoOf(AppInfo).get_version_72w4j3_k$ = function () {
    return this.version_1;
  };
  protoOf(AppInfo).get_platform_ssr7o_k$ = function () {
    return this.platform_1;
  };
  protoOf(AppInfo).component1_7eebsc_k$ = function () {
    return this.appName_1;
  };
  protoOf(AppInfo).component2_7eebsb_k$ = function () {
    return this.version_1;
  };
  protoOf(AppInfo).component3_7eebsa_k$ = function () {
    return this.platform_1;
  };
  protoOf(AppInfo).copy_nc7k0r_k$ = function (appName, version, platform) {
    return new AppInfo(appName, version, platform);
  };
  protoOf(AppInfo).copy$default_kr24ux_k$ = function (appName, version, platform, $super) {
    appName = appName === VOID ? this.appName_1 : appName;
    version = version === VOID ? this.version_1 : version;
    platform = platform === VOID ? this.platform_1 : platform;
    return $super === VOID ? this.copy_nc7k0r_k$(appName, version, platform) : $super.copy_nc7k0r_k$.call(this, appName, version, platform);
  };
  protoOf(AppInfo).toString = function () {
    return 'AppInfo(appName=' + this.appName_1 + ', version=' + this.version_1 + ', platform=' + this.platform_1 + ')';
  };
  protoOf(AppInfo).hashCode = function () {
    var result = getStringHashCode(this.appName_1);
    result = imul(result, 31) + getStringHashCode(this.version_1) | 0;
    result = imul(result, 31) + getStringHashCode(this.platform_1) | 0;
    return result;
  };
  protoOf(AppInfo).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof AppInfo))
      return false;
    var tmp0_other_with_cast = other instanceof AppInfo ? other : THROW_CCE();
    if (!(this.appName_1 === tmp0_other_with_cast.appName_1))
      return false;
    if (!(this.version_1 === tmp0_other_with_cast.version_1))
      return false;
    if (!(this.platform_1 === tmp0_other_with_cast.platform_1))
      return false;
    return true;
  };
  function _get_$childSerializers__r2zwns($this) {
    return $this.$childSerializers_1;
  }
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
    tmp.$childSerializers_1 = [null, null, null, null, null, null, null, null, lazy(tmp_0, User$Companion$$childSerializers$_anonymous__6nf9sv), null, null, null];
  }
  protoOf(Companion_0).serializer_9w0wvi_k$ = function () {
    return $serializer_getInstance_0();
  };
  var Companion_instance_0;
  function Companion_getInstance_0() {
    if (Companion_instance_0 == null)
      new Companion_0();
    return Companion_instance_0;
  }
  function $serializer_0() {
    $serializer_instance_0 = this;
    var tmp0_serialDesc = new PluginGeneratedSerialDescriptor('com.example.startdrive.shared.model.User', this, 12);
    tmp0_serialDesc.addElement_5pzumi_k$('id', true);
    tmp0_serialDesc.addElement_5pzumi_k$('fullName', true);
    tmp0_serialDesc.addElement_5pzumi_k$('email', true);
    tmp0_serialDesc.addElement_5pzumi_k$('phone', true);
    tmp0_serialDesc.addElement_5pzumi_k$('role', true);
    tmp0_serialDesc.addElement_5pzumi_k$('balance', true);
    tmp0_serialDesc.addElement_5pzumi_k$('fcmToken', true);
    tmp0_serialDesc.addElement_5pzumi_k$('assignedInstructorId', true);
    tmp0_serialDesc.addElement_5pzumi_k$('assignedCadets', true);
    tmp0_serialDesc.addElement_5pzumi_k$('isActive', true);
    tmp0_serialDesc.addElement_5pzumi_k$('createdAtMillis', true);
    tmp0_serialDesc.addElement_5pzumi_k$('chatAvatarUrl', true);
    this.descriptor_1 = tmp0_serialDesc;
  }
  protoOf($serializer_0).serialize_28tauy_k$ = function (encoder, value) {
    var tmp0_desc = this.descriptor_1;
    var tmp1_output = encoder.beginStructure_yljocp_k$(tmp0_desc);
    var tmp2_cached = Companion_getInstance_0().$childSerializers_1;
    if (tmp1_output.shouldEncodeElementDefault_x8eyid_k$(tmp0_desc, 0) ? true : !(value.id_1 === '')) {
      tmp1_output.encodeStringElement_1n5wu2_k$(tmp0_desc, 0, value.id_1);
    }
    if (tmp1_output.shouldEncodeElementDefault_x8eyid_k$(tmp0_desc, 1) ? true : !(value.fullName_1 === '')) {
      tmp1_output.encodeStringElement_1n5wu2_k$(tmp0_desc, 1, value.fullName_1);
    }
    if (tmp1_output.shouldEncodeElementDefault_x8eyid_k$(tmp0_desc, 2) ? true : !(value.email_1 === '')) {
      tmp1_output.encodeStringElement_1n5wu2_k$(tmp0_desc, 2, value.email_1);
    }
    if (tmp1_output.shouldEncodeElementDefault_x8eyid_k$(tmp0_desc, 3) ? true : !(value.phone_1 === '')) {
      tmp1_output.encodeStringElement_1n5wu2_k$(tmp0_desc, 3, value.phone_1);
    }
    if (tmp1_output.shouldEncodeElementDefault_x8eyid_k$(tmp0_desc, 4) ? true : !(value.role_1 === '')) {
      tmp1_output.encodeStringElement_1n5wu2_k$(tmp0_desc, 4, value.role_1);
    }
    if (tmp1_output.shouldEncodeElementDefault_x8eyid_k$(tmp0_desc, 5) ? true : !(value.balance_1 === 0)) {
      tmp1_output.encodeIntElement_krhhce_k$(tmp0_desc, 5, value.balance_1);
    }
    if (tmp1_output.shouldEncodeElementDefault_x8eyid_k$(tmp0_desc, 6) ? true : !(value.fcmToken_1 == null)) {
      tmp1_output.encodeNullableSerializableElement_5lquiv_k$(tmp0_desc, 6, StringSerializer_getInstance(), value.fcmToken_1);
    }
    if (tmp1_output.shouldEncodeElementDefault_x8eyid_k$(tmp0_desc, 7) ? true : !(value.assignedInstructorId_1 == null)) {
      tmp1_output.encodeNullableSerializableElement_5lquiv_k$(tmp0_desc, 7, StringSerializer_getInstance(), value.assignedInstructorId_1);
    }
    if (tmp1_output.shouldEncodeElementDefault_x8eyid_k$(tmp0_desc, 8) ? true : !equals(value.assignedCadets_1, emptyList())) {
      tmp1_output.encodeSerializableElement_isqxcl_k$(tmp0_desc, 8, tmp2_cached[8].get_value_j01efc_k$(), value.assignedCadets_1);
    }
    if (tmp1_output.shouldEncodeElementDefault_x8eyid_k$(tmp0_desc, 9) ? true : !(value.isActive_1 === false)) {
      tmp1_output.encodeBooleanElement_ydht7q_k$(tmp0_desc, 9, value.isActive_1);
    }
    if (tmp1_output.shouldEncodeElementDefault_x8eyid_k$(tmp0_desc, 10) ? true : !(value.createdAtMillis_1 == null)) {
      tmp1_output.encodeNullableSerializableElement_5lquiv_k$(tmp0_desc, 10, LongSerializer_getInstance(), value.createdAtMillis_1);
    }
    if (tmp1_output.shouldEncodeElementDefault_x8eyid_k$(tmp0_desc, 11) ? true : !(value.chatAvatarUrl_1 == null)) {
      tmp1_output.encodeNullableSerializableElement_5lquiv_k$(tmp0_desc, 11, StringSerializer_getInstance(), value.chatAvatarUrl_1);
    }
    tmp1_output.endStructure_1xqz0n_k$(tmp0_desc);
  };
  protoOf($serializer_0).serialize_5ase3y_k$ = function (encoder, value) {
    return this.serialize_28tauy_k$(encoder, value instanceof User ? value : THROW_CCE());
  };
  protoOf($serializer_0).deserialize_sy6x50_k$ = function (decoder) {
    var tmp0_desc = this.descriptor_1;
    var tmp1_flag = true;
    var tmp2_index = 0;
    var tmp3_bitMask0 = 0;
    var tmp4_local0 = null;
    var tmp5_local1 = null;
    var tmp6_local2 = null;
    var tmp7_local3 = null;
    var tmp8_local4 = null;
    var tmp9_local5 = 0;
    var tmp10_local6 = null;
    var tmp11_local7 = null;
    var tmp12_local8 = null;
    var tmp13_local9 = false;
    var tmp14_local10 = null;
    var tmp15_local11 = null;
    var tmp16_input = decoder.beginStructure_yljocp_k$(tmp0_desc);
    var tmp17_cached = Companion_getInstance_0().$childSerializers_1;
    if (tmp16_input.decodeSequentially_xlblqy_k$()) {
      tmp4_local0 = tmp16_input.decodeStringElement_3oenpg_k$(tmp0_desc, 0);
      tmp3_bitMask0 = tmp3_bitMask0 | 1;
      tmp5_local1 = tmp16_input.decodeStringElement_3oenpg_k$(tmp0_desc, 1);
      tmp3_bitMask0 = tmp3_bitMask0 | 2;
      tmp6_local2 = tmp16_input.decodeStringElement_3oenpg_k$(tmp0_desc, 2);
      tmp3_bitMask0 = tmp3_bitMask0 | 4;
      tmp7_local3 = tmp16_input.decodeStringElement_3oenpg_k$(tmp0_desc, 3);
      tmp3_bitMask0 = tmp3_bitMask0 | 8;
      tmp8_local4 = tmp16_input.decodeStringElement_3oenpg_k$(tmp0_desc, 4);
      tmp3_bitMask0 = tmp3_bitMask0 | 16;
      tmp9_local5 = tmp16_input.decodeIntElement_941u6a_k$(tmp0_desc, 5);
      tmp3_bitMask0 = tmp3_bitMask0 | 32;
      tmp10_local6 = tmp16_input.decodeNullableSerializableElement_k2y6ab_k$(tmp0_desc, 6, StringSerializer_getInstance(), tmp10_local6);
      tmp3_bitMask0 = tmp3_bitMask0 | 64;
      tmp11_local7 = tmp16_input.decodeNullableSerializableElement_k2y6ab_k$(tmp0_desc, 7, StringSerializer_getInstance(), tmp11_local7);
      tmp3_bitMask0 = tmp3_bitMask0 | 128;
      tmp12_local8 = tmp16_input.decodeSerializableElement_uahnnv_k$(tmp0_desc, 8, tmp17_cached[8].get_value_j01efc_k$(), tmp12_local8);
      tmp3_bitMask0 = tmp3_bitMask0 | 256;
      tmp13_local9 = tmp16_input.decodeBooleanElement_vuyhtj_k$(tmp0_desc, 9);
      tmp3_bitMask0 = tmp3_bitMask0 | 512;
      tmp14_local10 = tmp16_input.decodeNullableSerializableElement_k2y6ab_k$(tmp0_desc, 10, LongSerializer_getInstance(), tmp14_local10);
      tmp3_bitMask0 = tmp3_bitMask0 | 1024;
      tmp15_local11 = tmp16_input.decodeNullableSerializableElement_k2y6ab_k$(tmp0_desc, 11, StringSerializer_getInstance(), tmp15_local11);
      tmp3_bitMask0 = tmp3_bitMask0 | 2048;
    } else
      while (tmp1_flag) {
        tmp2_index = tmp16_input.decodeElementIndex_bstkhp_k$(tmp0_desc);
        switch (tmp2_index) {
          case -1:
            tmp1_flag = false;
            break;
          case 0:
            tmp4_local0 = tmp16_input.decodeStringElement_3oenpg_k$(tmp0_desc, 0);
            tmp3_bitMask0 = tmp3_bitMask0 | 1;
            break;
          case 1:
            tmp5_local1 = tmp16_input.decodeStringElement_3oenpg_k$(tmp0_desc, 1);
            tmp3_bitMask0 = tmp3_bitMask0 | 2;
            break;
          case 2:
            tmp6_local2 = tmp16_input.decodeStringElement_3oenpg_k$(tmp0_desc, 2);
            tmp3_bitMask0 = tmp3_bitMask0 | 4;
            break;
          case 3:
            tmp7_local3 = tmp16_input.decodeStringElement_3oenpg_k$(tmp0_desc, 3);
            tmp3_bitMask0 = tmp3_bitMask0 | 8;
            break;
          case 4:
            tmp8_local4 = tmp16_input.decodeStringElement_3oenpg_k$(tmp0_desc, 4);
            tmp3_bitMask0 = tmp3_bitMask0 | 16;
            break;
          case 5:
            tmp9_local5 = tmp16_input.decodeIntElement_941u6a_k$(tmp0_desc, 5);
            tmp3_bitMask0 = tmp3_bitMask0 | 32;
            break;
          case 6:
            tmp10_local6 = tmp16_input.decodeNullableSerializableElement_k2y6ab_k$(tmp0_desc, 6, StringSerializer_getInstance(), tmp10_local6);
            tmp3_bitMask0 = tmp3_bitMask0 | 64;
            break;
          case 7:
            tmp11_local7 = tmp16_input.decodeNullableSerializableElement_k2y6ab_k$(tmp0_desc, 7, StringSerializer_getInstance(), tmp11_local7);
            tmp3_bitMask0 = tmp3_bitMask0 | 128;
            break;
          case 8:
            tmp12_local8 = tmp16_input.decodeSerializableElement_uahnnv_k$(tmp0_desc, 8, tmp17_cached[8].get_value_j01efc_k$(), tmp12_local8);
            tmp3_bitMask0 = tmp3_bitMask0 | 256;
            break;
          case 9:
            tmp13_local9 = tmp16_input.decodeBooleanElement_vuyhtj_k$(tmp0_desc, 9);
            tmp3_bitMask0 = tmp3_bitMask0 | 512;
            break;
          case 10:
            tmp14_local10 = tmp16_input.decodeNullableSerializableElement_k2y6ab_k$(tmp0_desc, 10, LongSerializer_getInstance(), tmp14_local10);
            tmp3_bitMask0 = tmp3_bitMask0 | 1024;
            break;
          case 11:
            tmp15_local11 = tmp16_input.decodeNullableSerializableElement_k2y6ab_k$(tmp0_desc, 11, StringSerializer_getInstance(), tmp15_local11);
            tmp3_bitMask0 = tmp3_bitMask0 | 2048;
            break;
          default:
            throw UnknownFieldException_init_$Create$(tmp2_index);
        }
      }
    tmp16_input.endStructure_1xqz0n_k$(tmp0_desc);
    return User_init_$Create$(tmp3_bitMask0, tmp4_local0, tmp5_local1, tmp6_local2, tmp7_local3, tmp8_local4, tmp9_local5, tmp10_local6, tmp11_local7, tmp12_local8, tmp13_local9, tmp14_local10, tmp15_local11, null);
  };
  protoOf($serializer_0).get_descriptor_wjt6a0_k$ = function () {
    return this.descriptor_1;
  };
  protoOf($serializer_0).childSerializers_5ghqw5_k$ = function () {
    var tmp0_cached = Companion_getInstance_0().$childSerializers_1;
    // Inline function 'kotlin.arrayOf' call
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    return [StringSerializer_getInstance(), StringSerializer_getInstance(), StringSerializer_getInstance(), StringSerializer_getInstance(), StringSerializer_getInstance(), IntSerializer_getInstance(), get_nullable(StringSerializer_getInstance()), get_nullable(StringSerializer_getInstance()), tmp0_cached[8].get_value_j01efc_k$(), BooleanSerializer_getInstance(), get_nullable(LongSerializer_getInstance()), get_nullable(StringSerializer_getInstance())];
  };
  var $serializer_instance_0;
  function $serializer_getInstance_0() {
    if ($serializer_instance_0 == null)
      new $serializer_0();
    return $serializer_instance_0;
  }
  function User_init_$Init$(seen0, id, fullName, email, phone, role, balance, fcmToken, assignedInstructorId, assignedCadets, isActive, createdAtMillis, chatAvatarUrl, serializationConstructorMarker, $this) {
    if (!(0 === (0 & seen0))) {
      throwMissingFieldException(seen0, 0, $serializer_getInstance_0().descriptor_1);
    }
    if (0 === (seen0 & 1))
      $this.id_1 = '';
    else
      $this.id_1 = id;
    if (0 === (seen0 & 2))
      $this.fullName_1 = '';
    else
      $this.fullName_1 = fullName;
    if (0 === (seen0 & 4))
      $this.email_1 = '';
    else
      $this.email_1 = email;
    if (0 === (seen0 & 8))
      $this.phone_1 = '';
    else
      $this.phone_1 = phone;
    if (0 === (seen0 & 16))
      $this.role_1 = '';
    else
      $this.role_1 = role;
    if (0 === (seen0 & 32))
      $this.balance_1 = 0;
    else
      $this.balance_1 = balance;
    if (0 === (seen0 & 64))
      $this.fcmToken_1 = null;
    else
      $this.fcmToken_1 = fcmToken;
    if (0 === (seen0 & 128))
      $this.assignedInstructorId_1 = null;
    else
      $this.assignedInstructorId_1 = assignedInstructorId;
    if (0 === (seen0 & 256))
      $this.assignedCadets_1 = emptyList();
    else
      $this.assignedCadets_1 = assignedCadets;
    if (0 === (seen0 & 512))
      $this.isActive_1 = false;
    else
      $this.isActive_1 = isActive;
    if (0 === (seen0 & 1024))
      $this.createdAtMillis_1 = null;
    else
      $this.createdAtMillis_1 = createdAtMillis;
    if (0 === (seen0 & 2048))
      $this.chatAvatarUrl_1 = null;
    else
      $this.chatAvatarUrl_1 = chatAvatarUrl;
    return $this;
  }
  function User_init_$Create$(seen0, id, fullName, email, phone, role, balance, fcmToken, assignedInstructorId, assignedCadets, isActive, createdAtMillis, chatAvatarUrl, serializationConstructorMarker) {
    return User_init_$Init$(seen0, id, fullName, email, phone, role, balance, fcmToken, assignedInstructorId, assignedCadets, isActive, createdAtMillis, chatAvatarUrl, serializationConstructorMarker, objectCreate(protoOf(User)));
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
    this.id_1 = id;
    this.fullName_1 = fullName;
    this.email_1 = email;
    this.phone_1 = phone;
    this.role_1 = role;
    this.balance_1 = balance;
    this.fcmToken_1 = fcmToken;
    this.assignedInstructorId_1 = assignedInstructorId;
    this.assignedCadets_1 = assignedCadets;
    this.isActive_1 = isActive;
    this.createdAtMillis_1 = createdAtMillis;
    this.chatAvatarUrl_1 = chatAvatarUrl;
  }
  protoOf(User).get_id_kntnx8_k$ = function () {
    return this.id_1;
  };
  protoOf(User).get_fullName_9skygt_k$ = function () {
    return this.fullName_1;
  };
  protoOf(User).get_email_iqwbqr_k$ = function () {
    return this.email_1;
  };
  protoOf(User).get_phone_iwv5tx_k$ = function () {
    return this.phone_1;
  };
  protoOf(User).get_role_wotsxr_k$ = function () {
    return this.role_1;
  };
  protoOf(User).get_balance_4cdzil_k$ = function () {
    return this.balance_1;
  };
  protoOf(User).get_fcmToken_a95zde_k$ = function () {
    return this.fcmToken_1;
  };
  protoOf(User).get_assignedInstructorId_laxw6p_k$ = function () {
    return this.assignedInstructorId_1;
  };
  protoOf(User).get_assignedCadets_bue0kr_k$ = function () {
    return this.assignedCadets_1;
  };
  protoOf(User).get_isActive_quafmh_k$ = function () {
    return this.isActive_1;
  };
  protoOf(User).get_createdAtMillis_vwc77s_k$ = function () {
    return this.createdAtMillis_1;
  };
  protoOf(User).get_chatAvatarUrl_colcuj_k$ = function () {
    return this.chatAvatarUrl_1;
  };
  protoOf(User).initials_4g6y0f_k$ = function () {
    // Inline function 'kotlin.collections.mapNotNull' call
    var tmp0 = take(split(this.fullName_1, [' ']), 2);
    // Inline function 'kotlin.collections.mapNotNullTo' call
    var destination = ArrayList_init_$Create$();
    // Inline function 'kotlin.collections.forEach' call
    var _iterator__ex2g4s = tmp0.iterator_jk1svi_k$();
    while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
      var element = _iterator__ex2g4s.next_20eer_k$();
      var tmp0_safe_receiver = firstOrNull(element);
      var tmp;
      var tmp_0 = tmp0_safe_receiver;
      if ((tmp_0 == null ? null : new Char(tmp_0)) == null) {
        tmp = null;
      } else {
        // Inline function 'kotlin.text.uppercase' call
        // Inline function 'kotlin.js.asDynamic' call
        // Inline function 'kotlin.js.unsafeCast' call
        tmp = toString(tmp0_safe_receiver).toUpperCase();
      }
      var tmp0_safe_receiver_0 = tmp;
      if (tmp0_safe_receiver_0 == null)
        null;
      else {
        // Inline function 'kotlin.let' call
        destination.add_utx5q5_k$(tmp0_safe_receiver_0);
      }
    }
    return joinToString(destination, '');
  };
  protoOf(User).shortName_xjjrvd_k$ = function () {
    var parts = split(this.fullName_1, [' ']);
    var tmp0_elvis_lhs = getOrNull(parts, 0);
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return this.fullName_1;
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
  protoOf(User).component1_7eebsc_k$ = function () {
    return this.id_1;
  };
  protoOf(User).component2_7eebsb_k$ = function () {
    return this.fullName_1;
  };
  protoOf(User).component3_7eebsa_k$ = function () {
    return this.email_1;
  };
  protoOf(User).component4_7eebs9_k$ = function () {
    return this.phone_1;
  };
  protoOf(User).component5_7eebs8_k$ = function () {
    return this.role_1;
  };
  protoOf(User).component6_7eebs7_k$ = function () {
    return this.balance_1;
  };
  protoOf(User).component7_7eebs6_k$ = function () {
    return this.fcmToken_1;
  };
  protoOf(User).component8_7eebs5_k$ = function () {
    return this.assignedInstructorId_1;
  };
  protoOf(User).component9_7eebs4_k$ = function () {
    return this.assignedCadets_1;
  };
  protoOf(User).component10_gazzfo_k$ = function () {
    return this.isActive_1;
  };
  protoOf(User).component11_gazzfn_k$ = function () {
    return this.createdAtMillis_1;
  };
  protoOf(User).component12_gazzfm_k$ = function () {
    return this.chatAvatarUrl_1;
  };
  protoOf(User).copy_t9i8w0_k$ = function (id, fullName, email, phone, role, balance, fcmToken, assignedInstructorId, assignedCadets, isActive, createdAtMillis, chatAvatarUrl) {
    return new User(id, fullName, email, phone, role, balance, fcmToken, assignedInstructorId, assignedCadets, isActive, createdAtMillis, chatAvatarUrl);
  };
  protoOf(User).copy$default_e0vqjv_k$ = function (id, fullName, email, phone, role, balance, fcmToken, assignedInstructorId, assignedCadets, isActive, createdAtMillis, chatAvatarUrl, $super) {
    id = id === VOID ? this.id_1 : id;
    fullName = fullName === VOID ? this.fullName_1 : fullName;
    email = email === VOID ? this.email_1 : email;
    phone = phone === VOID ? this.phone_1 : phone;
    role = role === VOID ? this.role_1 : role;
    balance = balance === VOID ? this.balance_1 : balance;
    fcmToken = fcmToken === VOID ? this.fcmToken_1 : fcmToken;
    assignedInstructorId = assignedInstructorId === VOID ? this.assignedInstructorId_1 : assignedInstructorId;
    assignedCadets = assignedCadets === VOID ? this.assignedCadets_1 : assignedCadets;
    isActive = isActive === VOID ? this.isActive_1 : isActive;
    createdAtMillis = createdAtMillis === VOID ? this.createdAtMillis_1 : createdAtMillis;
    chatAvatarUrl = chatAvatarUrl === VOID ? this.chatAvatarUrl_1 : chatAvatarUrl;
    return $super === VOID ? this.copy_t9i8w0_k$(id, fullName, email, phone, role, balance, fcmToken, assignedInstructorId, assignedCadets, isActive, createdAtMillis, chatAvatarUrl) : $super.copy_t9i8w0_k$.call(this, id, fullName, email, phone, role, balance, fcmToken, assignedInstructorId, assignedCadets, isActive, createdAtMillis, chatAvatarUrl);
  };
  protoOf(User).toString = function () {
    return 'User(id=' + this.id_1 + ', fullName=' + this.fullName_1 + ', email=' + this.email_1 + ', phone=' + this.phone_1 + ', role=' + this.role_1 + ', balance=' + this.balance_1 + ', fcmToken=' + this.fcmToken_1 + ', assignedInstructorId=' + this.assignedInstructorId_1 + ', assignedCadets=' + toString_0(this.assignedCadets_1) + ', isActive=' + this.isActive_1 + ', createdAtMillis=' + toString_1(this.createdAtMillis_1) + ', chatAvatarUrl=' + this.chatAvatarUrl_1 + ')';
  };
  protoOf(User).hashCode = function () {
    var result = getStringHashCode(this.id_1);
    result = imul(result, 31) + getStringHashCode(this.fullName_1) | 0;
    result = imul(result, 31) + getStringHashCode(this.email_1) | 0;
    result = imul(result, 31) + getStringHashCode(this.phone_1) | 0;
    result = imul(result, 31) + getStringHashCode(this.role_1) | 0;
    result = imul(result, 31) + this.balance_1 | 0;
    result = imul(result, 31) + (this.fcmToken_1 == null ? 0 : getStringHashCode(this.fcmToken_1)) | 0;
    result = imul(result, 31) + (this.assignedInstructorId_1 == null ? 0 : getStringHashCode(this.assignedInstructorId_1)) | 0;
    result = imul(result, 31) + hashCode(this.assignedCadets_1) | 0;
    result = imul(result, 31) + getBooleanHashCode(this.isActive_1) | 0;
    result = imul(result, 31) + (this.createdAtMillis_1 == null ? 0 : this.createdAtMillis_1.hashCode()) | 0;
    result = imul(result, 31) + (this.chatAvatarUrl_1 == null ? 0 : getStringHashCode(this.chatAvatarUrl_1)) | 0;
    return result;
  };
  protoOf(User).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof User))
      return false;
    var tmp0_other_with_cast = other instanceof User ? other : THROW_CCE();
    if (!(this.id_1 === tmp0_other_with_cast.id_1))
      return false;
    if (!(this.fullName_1 === tmp0_other_with_cast.fullName_1))
      return false;
    if (!(this.email_1 === tmp0_other_with_cast.email_1))
      return false;
    if (!(this.phone_1 === tmp0_other_with_cast.phone_1))
      return false;
    if (!(this.role_1 === tmp0_other_with_cast.role_1))
      return false;
    if (!(this.balance_1 === tmp0_other_with_cast.balance_1))
      return false;
    if (!(this.fcmToken_1 == tmp0_other_with_cast.fcmToken_1))
      return false;
    if (!(this.assignedInstructorId_1 == tmp0_other_with_cast.assignedInstructorId_1))
      return false;
    if (!equals(this.assignedCadets_1, tmp0_other_with_cast.assignedCadets_1))
      return false;
    if (!(this.isActive_1 === tmp0_other_with_cast.isActive_1))
      return false;
    if (!equals(this.createdAtMillis_1, tmp0_other_with_cast.createdAtMillis_1))
      return false;
    if (!(this.chatAvatarUrl_1 == tmp0_other_with_cast.chatAvatarUrl_1))
      return false;
    return true;
  };
  function AppInfoRepository() {
  }
  function _get_APP_VERSION__3ne4yx($this) {
    return $this.APP_VERSION_1;
  }
  function Companion_1() {
    Companion_instance_1 = this;
    this.APP_VERSION_1 = '1.0.0';
  }
  var Companion_instance_1;
  function Companion_getInstance_1() {
    if (Companion_instance_1 == null)
      new Companion_1();
    return Companion_instance_1;
  }
  function DefaultAppInfoRepository() {
    Companion_getInstance_1();
  }
  protoOf(DefaultAppInfoRepository).getAppInfo_40fhh5_k$ = function () {
    return new AppInfo('StartDrive', '1.0.0', platformName());
  };
  function platformName() {
    return 'Web';
  }
  //region block: post-declaration
  protoOf($serializer).typeParametersSerializers_fr94fx_k$ = typeParametersSerializers;
  protoOf($serializer_0).typeParametersSerializers_fr94fx_k$ = typeParametersSerializers;
  //endregion
  //region block: exports
  _.$_$ = _.$_$ || {};
  _.$_$.a = User;
  _.$_$.b = SharedFactory_getInstance;
  //endregion
  return _;
}));
