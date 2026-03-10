(function (factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports', './kotlin-kotlin-stdlib.js'], factory);
  else if (typeof exports === 'object')
    factory(module.exports, require('./kotlin-kotlin-stdlib.js'));
  else {
    if (typeof globalThis['kotlin-kotlin-stdlib'] === 'undefined') {
      throw new Error("Error loading module 'kotlinx-serialization-kotlinx-serialization-core'. Its dependency 'kotlin-kotlin-stdlib' was not found. Please, check whether 'kotlin-kotlin-stdlib' is loaded prior to 'kotlinx-serialization-kotlinx-serialization-core'.");
    }
    globalThis['kotlinx-serialization-kotlinx-serialization-core'] = factory(typeof globalThis['kotlinx-serialization-kotlinx-serialization-core'] === 'undefined' ? {} : globalThis['kotlinx-serialization-kotlinx-serialization-core'], globalThis['kotlin-kotlin-stdlib']);
  }
}(function (_, kotlin_kotlin) {
  'use strict';
  //region block: imports
  var imul = Math.imul;
  var getKClassFromExpression = kotlin_kotlin.$_$.a;
  var ensureNotNull = kotlin_kotlin.$_$.o2;
  var protoOf = kotlin_kotlin.$_$.t1;
  var getStringHashCode = kotlin_kotlin.$_$.h1;
  var initMetadataForClass = kotlin_kotlin.$_$.j1;
  var initMetadataForObject = kotlin_kotlin.$_$.l1;
  var VOID = kotlin_kotlin.$_$.b;
  var equals = kotlin_kotlin.$_$.f1;
  var hashCode = kotlin_kotlin.$_$.i1;
  var toString = kotlin_kotlin.$_$.v1;
  //endregion
  //region block: pre-declaration
  initMetadataForClass(SerialKind, 'SerialKind');
  initMetadataForClass(PrimitiveKind, 'PrimitiveKind', VOID, SerialKind);
  initMetadataForObject(STRING, 'STRING', VOID, PrimitiveKind);
  initMetadataForClass(ListLikeDescriptor, 'ListLikeDescriptor');
  initMetadataForClass(ArrayListClassDesc, 'ArrayListClassDesc', VOID, ListLikeDescriptor);
  initMetadataForClass(AbstractCollectionSerializer, 'AbstractCollectionSerializer');
  initMetadataForClass(CollectionLikeSerializer, 'CollectionLikeSerializer', VOID, AbstractCollectionSerializer);
  initMetadataForClass(CollectionSerializer, 'CollectionSerializer', VOID, CollectionLikeSerializer);
  initMetadataForClass(ArrayListSerializer, 'ArrayListSerializer', VOID, CollectionSerializer);
  initMetadataForObject(StringSerializer, 'StringSerializer');
  initMetadataForClass(PrimitiveSerialDescriptor, 'PrimitiveSerialDescriptor');
  initMetadataForClass(SerializableWith, 'SerializableWith', VOID, VOID, VOID, VOID, 0);
  //endregion
  function SerialKind() {
  }
  protoOf(SerialKind).toString = function () {
    return ensureNotNull(getKClassFromExpression(this).z5());
  };
  protoOf(SerialKind).hashCode = function () {
    return getStringHashCode(this.toString());
  };
  function STRING() {
    STRING_instance = this;
    PrimitiveKind.call(this);
  }
  var STRING_instance;
  function STRING_getInstance() {
    if (STRING_instance == null)
      new STRING();
    return STRING_instance;
  }
  function PrimitiveKind() {
    SerialKind.call(this);
  }
  function ArrayListClassDesc(elementDesc) {
    ListLikeDescriptor.call(this, elementDesc);
  }
  protoOf(ArrayListClassDesc).v8 = function () {
    return 'kotlin.collections.ArrayList';
  };
  function ListLikeDescriptor(elementDescriptor) {
    this.w8_1 = elementDescriptor;
    this.x8_1 = 1;
  }
  protoOf(ListLikeDescriptor).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof ListLikeDescriptor))
      return false;
    if (equals(this.w8_1, other.w8_1) && this.v8() === other.v8())
      return true;
    return false;
  };
  protoOf(ListLikeDescriptor).hashCode = function () {
    return imul(hashCode(this.w8_1), 31) + getStringHashCode(this.v8()) | 0;
  };
  protoOf(ListLikeDescriptor).toString = function () {
    return this.v8() + '(' + toString(this.w8_1) + ')';
  };
  function ArrayListSerializer(element) {
    CollectionSerializer.call(this, element);
    this.z8_1 = new ArrayListClassDesc(element.a9());
  }
  protoOf(ArrayListSerializer).a9 = function () {
    return this.z8_1;
  };
  function CollectionSerializer(element) {
    CollectionLikeSerializer.call(this, element);
  }
  function CollectionLikeSerializer(elementSerializer) {
    AbstractCollectionSerializer.call(this);
    this.b9_1 = elementSerializer;
  }
  function AbstractCollectionSerializer() {
  }
  function StringSerializer() {
    StringSerializer_instance = this;
    this.c9_1 = new PrimitiveSerialDescriptor('kotlin.String', STRING_getInstance());
  }
  protoOf(StringSerializer).a9 = function () {
    return this.c9_1;
  };
  var StringSerializer_instance;
  function StringSerializer_getInstance() {
    if (StringSerializer_instance == null)
      new StringSerializer();
    return StringSerializer_instance;
  }
  function PrimitiveSerialDescriptor(serialName, kind) {
    this.d9_1 = serialName;
    this.e9_1 = kind;
  }
  protoOf(PrimitiveSerialDescriptor).toString = function () {
    return 'PrimitiveDescriptor(' + this.d9_1 + ')';
  };
  protoOf(PrimitiveSerialDescriptor).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof PrimitiveSerialDescriptor))
      return false;
    if (this.d9_1 === other.d9_1 && equals(this.e9_1, other.e9_1))
      return true;
    return false;
  };
  protoOf(PrimitiveSerialDescriptor).hashCode = function () {
    return getStringHashCode(this.d9_1) + imul(31, this.e9_1.hashCode()) | 0;
  };
  function SerializableWith() {
  }
  //region block: exports
  _.$_$ = _.$_$ || {};
  _.$_$.a = StringSerializer_getInstance;
  _.$_$.b = ArrayListSerializer;
  //endregion
  return _;
}));

//# sourceMappingURL=kotlinx-serialization-kotlinx-serialization-core.js.map
