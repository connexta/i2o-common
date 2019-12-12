/**
 * Copyright (c) Connexta
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package com.connexta.commons.persistence;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import javax.annotation.Nullable;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PersistableTest {
  private static final String TYPE = "type";
  private static final UUID ID = UUID.randomUUID();
  private static final String STRING = "string-b";

  @Rule public ExpectedException exception = ExpectedException.none();

  private final TestPersistable persistable =
      new TestPersistable(PersistableTest.TYPE, PersistableTest.ID);

  @Test
  public void testCtorWithType() throws Exception {
    final TestPersistable persistable = new TestPersistable(PersistableTest.TYPE);

    Assert.assertThat(persistable.getPersistableType(), Matchers.equalTo(PersistableTest.TYPE));
    Assert.assertThat(persistable.getId(), Matchers.notNullValue());
  }

  @Test
  public void testCtorWithTypeAndId() throws Exception {
    Assert.assertThat(persistable.getPersistableType(), Matchers.equalTo(PersistableTest.TYPE));
    Assert.assertThat(persistable.getId(), Matchers.equalTo(PersistableTest.ID));
  }

  @Test
  public void testCtorWithTypeAndNullId() throws Exception {
    final TestPersistable persistable = new TestPersistable(PersistableTest.TYPE, null);

    Assert.assertThat(persistable.getPersistableType(), Matchers.equalTo(PersistableTest.TYPE));
    Assert.assertThat(persistable.getId(), Matchers.nullValue());
  }

  @Test
  public void testGetId() throws Exception {
    Assert.assertThat(persistable.getId(), Matchers.equalTo(PersistableTest.ID));
  }

  @Test
  public void testWriteTo() throws Exception {
    final TestPojo pojo = new TestPojo();

    final TestPojo written = persistable.writeTo(pojo);

    Assert.assertThat(written, Matchers.sameInstance(pojo));
    Assert.assertThat(pojo.getId(), Matchers.equalTo(PersistableTest.ID));
  }

  @Test
  public void testWriteToWithNullId() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern(".*missing.*" + PersistableTest.TYPE + ".*id.*"));

    final TestPojo pojo = new TestPojo();
    final TestPersistable persistable = new TestPersistable("type", null);

    persistable.writeTo(pojo);
  }

  @Test
  public void testWriteToWhenHasUnknown() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*unknown.*" + PersistableTest.TYPE + ".*"));

    final TestPojo unknownPojo =
        new UnknownTestPojo().setId(PersistableTest.ID).setVersion(TestPojo.CURRENT_VERSION);
    final TestPersistable persistable3 = new TestPersistable();

    persistable3.readFrom(unknownPojo);

    final TestPojo pojo = new TestPojo();

    persistable3.writeTo(pojo);
  }

  @Test
  public void testReadFrom() throws Exception {
    final TestPojo pojo =
        new TestPojo().setId(PersistableTest.ID).setVersion(TestPojo.CURRENT_VERSION);
    final TestPersistable persistable = new TestPersistable();

    persistable.readFrom(pojo);

    Assert.assertThat(persistable.getId(), Matchers.equalTo(PersistableTest.ID));
    Assert.assertThat(persistable.hasUnknowns, Matchers.equalTo(false));
  }

  @Test
  public void testReadFromWithNullId() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern(".*missing.*" + PersistableTest.TYPE + ".*id.*"));

    final TestPojo pojo = new TestPojo().setId(null).setVersion(TestPojo.CURRENT_VERSION);
    final TestPersistable persistable = new TestPersistable();

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromResetsUnknownFlag() throws Exception {
    final TestPojo pojo =
        new TestPojo().setId(PersistableTest.ID).setVersion(TestPojo.CURRENT_VERSION);
    final TestPersistable persistable = new TestPersistable();

    persistable.hasUnknowns = true;

    persistable.readFrom(pojo);

    Assert.assertThat(persistable.getId(), Matchers.equalTo(PersistableTest.ID));
    Assert.assertThat(persistable.hasUnknowns, Matchers.equalTo(false));
  }

  @Test
  public void testSetOrFailIfNullWhenNullAndIdNotNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern(
            "missing " + PersistableTest.TYPE + " string.*: " + PersistableTest.ID + "$"));

    final TestPojo pojo = new TestPojo().setString(null);

    persistable.setOrFailIfNull("string", pojo::getString, persistable::setString);
  }

  @Test
  public void testSetOrFailIfNullWhenNullAndIdIsNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern("missing " + PersistableTest.TYPE + " string$"));

    final TestPojo pojo = new TestPojo().setString(null);
    final TestPersistable persistable = new TestPersistable(PersistableTest.TYPE, null);

    persistable.setOrFailIfNull("string", pojo::getString, persistable::setString);
  }

  @Test
  public void testSetOrFailIfNullWhenValid() throws Exception {
    final TestPojo pojo = new TestPojo().setString(PersistableTest.STRING);

    persistable.setOrFailIfNull("string-dummy", pojo::getString, persistable::setString);

    Assert.assertThat(persistable.getString(), Matchers.equalTo(PersistableTest.STRING));
  }

  @Test
  public void testSetOrFailIfNullOrEmptyWhenNullAndIdNotNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern(
            "missing " + PersistableTest.TYPE + " string.*: " + PersistableTest.ID + "$"));

    final TestPojo pojo = new TestPojo().setString(null);

    persistable.setOrFailIfNullOrEmpty("string", pojo::getString, persistable::setString);
  }

  @Test
  public void testSetOrFailIfNullOrEmptyWhenNullAndIdIsNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern("missing " + PersistableTest.TYPE + " string$"));

    final TestPojo pojo = new TestPojo().setString(null);
    final TestPersistable persistable = new TestPersistable(PersistableTest.TYPE, null);

    persistable.setOrFailIfNullOrEmpty("string", pojo::getString, persistable::setString);
  }

  @Test
  public void testSetOrFailIfNullOrEmptyWhenEmptyAndIdNotNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern(
            "empty " + PersistableTest.TYPE + " string.*: " + PersistableTest.ID + "$"));

    final TestPojo pojo = new TestPojo().setString("");

    persistable.setOrFailIfNullOrEmpty("string", pojo::getString, persistable::setString);
  }

  @Test
  public void testSetOrFailIfEmptyOrEmptyWhenNullAndIdIsNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern("empty " + PersistableTest.TYPE + " string$"));

    final TestPojo pojo = new TestPojo().setString("");
    final TestPersistable persistable = new TestPersistable(PersistableTest.TYPE, null);

    persistable.setOrFailIfNullOrEmpty("string", pojo::getString, persistable::setString);
  }

  @Test
  public void testSetOrFailIfNullOrEmptyWhenValid() throws Exception {
    final TestPojo pojo = new TestPojo().setString(PersistableTest.STRING);

    persistable.setOrFailIfNullOrEmpty("string-dummy", pojo::getString, persistable::setString);

    Assert.assertThat(persistable.getString(), Matchers.equalTo(PersistableTest.STRING));
  }

  @Test
  public void testConvertAndSet() throws Exception {
    final TestPojo pojo = new TestPojo().setString("true");

    persistable.convertAndSet(
        "field", pojo::getString, Boolean::parseBoolean, persistable::setBool);

    Assert.assertThat(persistable.getBool(), Matchers.equalTo(true));
  }

  @Test
  public void testConvertAndSetWhenNull() throws Exception {
    final TestPojo pojo = new TestPojo().setString(null);

    persistable.convertAndSet("url", pojo::getString, PersistableTest::toUrl, persistable::setUrl);

    Assert.assertThat(persistable.getUrl(), Matchers.nullValue());
  }

  @Test
  public void testConvertAndSetWhenConverterThrowsInvalidFieldException() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage("testing invalid url");

    final TestPojo pojo = new TestPojo().setString("invalid-url");

    persistable.convertAndSet("url", pojo::getString, PersistableTest::toUrl, persistable::setUrl);
  }

  @Test
  public void testConvertAndSetWhenConverterThrowsSomeException() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern(
            "invalid " + PersistableTest.TYPE + " url.*: " + PersistableTest.ID + "$"));
    exception.expectCause(Matchers.isA(MalformedURLException.class));

    final TestPojo pojo = new TestPojo().setString("invalid-url");

    persistable.convertAndSet("url", pojo::getString, PersistableTest::toUrl2, persistable::setUrl);
  }

  @Test
  public void testConvertAndSetOrFailIfNullWhenValid() throws Exception {
    final TestPojo pojo = new TestPojo().setString("true");

    persistable.convertAndSetOrFailIfNull(
        "bool-dummy", pojo::getString, Boolean::parseBoolean, persistable::setBool);

    Assert.assertThat(persistable.getBool(), Matchers.equalTo(true));
  }

  @Test
  public void testConvertAndSetOrFailIfNullWhenNullAndIdNotNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern(
            "missing " + PersistableTest.TYPE + " bool.*: " + PersistableTest.ID + "$"));

    final TestPojo pojo = new TestPojo().setString(null);

    persistable.convertAndSetOrFailIfNull(
        "bool", pojo::getString, Boolean::parseBoolean, persistable::setBool);
  }

  @Test
  public void testConvertAndSetOrFailIfNullWhenNullAndIdIsNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern(
            "missing " + PersistableTest.TYPE + " bool.*: " + PersistableTest.ID + "$"));

    final TestPojo pojo = new TestPojo().setString(null);

    persistable.convertAndSetOrFailIfNull(
        "bool", pojo::getString, Boolean::parseBoolean, persistable::setBool);
  }

  @Test
  public void testConvertAndSetOrFailIfNullWhenConverterThrowsInvalidFieldException()
      throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage("testing invalid url");

    final TestPojo pojo = new TestPojo().setString("invalid-url");

    persistable.convertAndSetOrFailIfNull(
        "url", pojo::getString, PersistableTest::toUrl, persistable::setUrl);
  }

  @Test
  public void testConvertAndSetOrFailIfNullWhenConverterThrowsSomeException() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern(
            "invalid " + PersistableTest.TYPE + " url.*: " + PersistableTest.ID + "$"));
    exception.expectCause(Matchers.isA(MalformedURLException.class));

    final TestPojo pojo = new TestPojo().setString("invalid-url");

    persistable.convertAndSetOrFailIfNull(
        "url", pojo::getString, PersistableTest::toUrl2, persistable::setUrl);
  }

  @Test
  public void testConvertAndSetSetOrFailIfNullWhenNullAndIdIsNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern("missing " + PersistableTest.TYPE + " bool"));

    final TestPojo pojo = new TestPojo().setString(null);
    final TestPersistable persistable = new TestPersistable(PersistableTest.TYPE, null);

    persistable.convertAndSetOrFailIfNull(
        "bool", pojo::getString, Boolean::parseBoolean, persistable::setBool);
  }

  @Test
  public void testConvertAndSetOrFailIfNullOrEmpty() throws Exception {
    final TestPojo pojo = new TestPojo().setString("true");

    persistable.convertAndSetOrFailIfNullOrEmpty(
        "bool", pojo::getString, Boolean::parseBoolean, persistable::setBool);

    Assert.assertThat(persistable.getBool(), Matchers.equalTo(true));
  }

  @Test
  public void testConvertAndSetOrFailIfNullOrEmptyWhenValid() throws Exception {
    final TestPojo pojo = new TestPojo().setString("true");

    persistable.convertAndSetOrFailIfNullOrEmpty(
        "bool-dummy", pojo::getString, Boolean::parseBoolean, persistable::setBool);

    Assert.assertThat(persistable.getBool(), Matchers.equalTo(true));
  }

  @Test
  public void testConvertAndSetOrFailIfNullOrEmptyWhenConverterThrowsInvalidFieldException()
      throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage("testing invalid url");

    final TestPojo pojo = new TestPojo().setString("invalid-url");

    persistable.convertAndSetOrFailIfNullOrEmpty(
        "url", pojo::getString, PersistableTest::toUrl, persistable::setUrl);
  }

  @Test
  public void testConvertAndSetOrFailIfNullOrEmptyWhenConverterThrowsSomeException()
      throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern(
            "invalid " + PersistableTest.TYPE + " url.*: " + PersistableTest.ID + "$"));
    exception.expectCause(Matchers.isA(MalformedURLException.class));

    final TestPojo pojo = new TestPojo().setString("invalid-url");

    persistable.convertAndSetOrFailIfNullOrEmpty(
        "url", pojo::getString, PersistableTest::toUrl2, persistable::setUrl);
  }

  @Test
  public void testConvertAndSetOrFailIfNullOrEmptyWhenNullAndIdNotNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern(
            "missing " + PersistableTest.TYPE + " bool.*: " + PersistableTest.ID + "$"));

    final TestPojo pojo = new TestPojo().setString(null);

    persistable.convertAndSetOrFailIfNullOrEmpty(
        "bool", pojo::getString, Boolean::parseBoolean, persistable::setBool);
  }

  @Test
  public void testConvertAndSetSetOrFailIfNullOrEmptyWhenNullAndIdIsNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern("missing " + PersistableTest.TYPE + " bool"));

    final TestPojo pojo = new TestPojo().setString(null);
    final TestPersistable persistable = new TestPersistable(PersistableTest.TYPE, null);

    persistable.convertAndSetOrFailIfNullOrEmpty(
        "bool", pojo::getString, Boolean::parseBoolean, persistable::setBool);
  }

  @Test
  public void testConvertAndSetOrFailIfNullOrEmptyWhenEmptyAndIdNotNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern(
            "empty " + PersistableTest.TYPE + " bool.*: " + PersistableTest.ID + "$"));

    final TestPojo pojo = new TestPojo().setString("");

    persistable.convertAndSetOrFailIfNullOrEmpty(
        "bool", pojo::getString, Boolean::parseBoolean, persistable::setBool);
  }

  @Test
  public void testConvertAndSetOrFailIfNullOrEmptyWhenNullAndIdIsNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern("empty " + PersistableTest.TYPE + " bool$"));

    final TestPojo pojo = new TestPojo().setString("");
    final TestPersistable persistable = new TestPersistable(PersistableTest.TYPE, null);

    persistable.convertAndSetOrFailIfNullOrEmpty(
        "bool", pojo::getString, Boolean::parseBoolean, persistable::setBool);
  }

  // HERE
  @Test
  public void testConvertAndSetEnumValueOrFailIfNullOrUnknownWhenValid() throws Exception {
    final TestPojo pojo = new TestPojo();

    persistable.setEnum(TestEnum.ENUM_A);

    persistable.convertAndSetEnumValueOrFailIfNullOrUnknown(
        "enum", TestEnum.ENUM_UNKNOWN, persistable::getEnum, pojo::setEnum);

    Assert.assertThat(pojo.getEnum(), Matchers.equalTo(TestEnum.ENUM_A.name()));
  }

  @Test
  public void testConvertAndSetEnumValueOrFailIfNullOrUnknownWhenNullAndIdNotNull()
      throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern(
            "missing " + PersistableTest.TYPE + " enum.*: " + PersistableTest.ID + "$"));

    final TestPojo pojo = new TestPojo();

    persistable.setEnum(null);

    persistable.convertAndSetEnumValueOrFailIfNullOrUnknown(
        "enum", TestEnum.ENUM_UNKNOWN, persistable::getEnum, pojo::setEnum);
  }

  @Test
  public void testConvertAndSetEnumValueOrFailIfNullOrUnknownWhenNullAndIdIsNull()
      throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern("missing " + PersistableTest.TYPE + " enum.*$"));

    final TestPojo pojo = new TestPojo();

    final TestPersistable persistable = new TestPersistable(PersistableTest.TYPE, null);

    persistable.convertAndSetEnumValueOrFailIfNullOrUnknown(
        "enum", TestEnum.ENUM_UNKNOWN, persistable::getEnum, pojo::setEnum);
  }

  @Test
  public void testConvertAndSetEnumValueOrFailIfNullOrUnknownWhenUnknown() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern(
            "unknown " + PersistableTest.TYPE + " enum.*: " + PersistableTest.ID + "$"));

    final TestPojo pojo = new TestPojo();

    persistable.setEnum(TestEnum.ENUM_UNKNOWN);

    persistable.convertAndSetEnumValueOrFailIfNullOrUnknown(
        "enum", TestEnum.ENUM_UNKNOWN, persistable::getEnum, pojo::setEnum);
  }

  @Test
  public void testConvertAndSetEnumValueOrFailIfNullWhenNullAndIdNotNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern(
            "missing " + PersistableTest.TYPE + " enum.*: " + PersistableTest.ID + "$"));

    final TestPojo pojo = new TestPojo().setEnum(null);

    persistable.convertAndSetEnumValueOrFailIfNullOrEmpty(
        "enum", TestEnum.class, TestEnum.ENUM_UNKNOWN, pojo::getEnum, persistable::setEnum);
  }

  @Test
  public void testConvertAndSetEnumValueOrFailIfNullWhenNullAndIdIsNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern("missing " + PersistableTest.TYPE + " enum.*$"));

    final TestPojo pojo = new TestPojo().setEnum(null);
    final TestPersistable persistable = new TestPersistable(PersistableTest.TYPE, null);

    persistable.convertAndSetEnumValueOrFailIfNullOrEmpty(
        "enum", TestEnum.class, TestEnum.ENUM_UNKNOWN, pojo::getEnum, persistable::setEnum);
  }

  @Test
  public void testConvertAndSetEnumValueOrFailIfNullWhenEmptyAndIdNotNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(
        Matchers.matchesPattern(
            "empty " + PersistableTest.TYPE + " enum.*: " + PersistableTest.ID + "$"));

    final TestPojo pojo = new TestPojo().setEnum("");

    persistable.convertAndSetEnumValueOrFailIfNullOrEmpty(
        "enum", TestEnum.class, TestEnum.ENUM_UNKNOWN, pojo::getEnum, persistable::setEnum);
  }

  @Test
  public void testConvertAndSetEnumValueOrFailIfNullWhenEmptyAndIdIsNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern("empty " + PersistableTest.TYPE + " enum.*$"));

    final TestPojo pojo = new TestPojo().setEnum("");
    final TestPersistable persistable = new TestPersistable(PersistableTest.TYPE, null);

    persistable.convertAndSetEnumValueOrFailIfNullOrEmpty(
        "enum", TestEnum.class, TestEnum.ENUM_UNKNOWN, pojo::getEnum, persistable::setEnum);
  }

  @Test
  public void testConvertAndSetEnumValueOrFailIfNullOrEmptyWhenValid() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum(TestEnum.ENUM_A.name());

    persistable.convertAndSetEnumValueOrFailIfNullOrEmpty(
        "enum-dummy", TestEnum.class, TestEnum.ENUM_UNKNOWN, pojo::getEnum, persistable::setEnum);

    Assert.assertThat(persistable.getEnum(), Matchers.equalTo(TestEnum.ENUM_A));
  }

  @Test
  public void testConvertAndSetEnumValueOrFailIfNullOrEmptyWithNewValue() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum("what-is-this");

    persistable.convertAndSetEnumValueOrFailIfNullOrEmpty(
        "enum-dummy", TestEnum.class, TestEnum.ENUM_UNKNOWN, pojo::getEnum, persistable::setEnum);

    Assert.assertThat(persistable.getEnum(), Matchers.equalTo(TestEnum.ENUM_UNKNOWN));
  }

  @Test
  public void testConvertAndSetEnumValueWhenNotSpecifyingANullValue() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum(TestEnum.ENUM_A.name());

    persistable.convertAndSetEnumValue(
        TestEnum.class, TestEnum.ENUM_UNKNOWN, pojo::getEnum, persistable::setEnum);

    Assert.assertThat(persistable.getEnum(), Matchers.equalTo(TestEnum.ENUM_A));
  }

  @Test
  public void testConvertAndSetEnumValueWithNewValueWhenNotSpecifyingANullValue() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum("what-is-this");

    persistable.convertAndSetEnumValue(
        TestEnum.class, TestEnum.ENUM_UNKNOWN, pojo::getEnum, persistable::setEnum);

    Assert.assertThat(persistable.getEnum(), Matchers.equalTo(TestEnum.ENUM_UNKNOWN));
  }

  @Test
  public void testConvertAndSetEnumValueWithNullWhenNotSpecifyingANullValue() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum(null);

    persistable.convertAndSetEnumValue(
        TestEnum.class, TestEnum.ENUM_UNKNOWN, pojo::getEnum, persistable::setEnum);

    Assert.assertThat(persistable.getEnum(), Matchers.equalTo(TestEnum.ENUM_UNKNOWN));
  }

  @Test
  public void testConvertAndSetEnumValueWithEmptyWhenNotSpecifyingANullValue() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum("");

    persistable.convertAndSetEnumValue(
        TestEnum.class, TestEnum.ENUM_UNKNOWN, pojo::getEnum, persistable::setEnum);

    Assert.assertThat(persistable.getEnum(), Matchers.equalTo(TestEnum.ENUM_UNKNOWN));
  }

  @Test
  public void testConvertAndSetEnumValueWhenSpecifyingANullValue() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum(TestEnum.ENUM_A.name());

    persistable.convertAndSetEnumValue(
        TestEnum.class,
        TestEnum.ENUM_UNKNOWN,
        TestEnum.ENUM_UNKNOWN,
        pojo::getEnum,
        persistable::setEnum);

    Assert.assertThat(persistable.getEnum(), Matchers.equalTo(TestEnum.ENUM_A));
  }

  @Test
  public void testConvertAndSetEnumValueWithNewValueWhenSpecifyingANullValue() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum("what-is-this");

    persistable.convertAndSetEnumValue(
        TestEnum.class,
        TestEnum.ENUM_UNKNOWN,
        TestEnum.ENUM_UNKNOWN,
        pojo::getEnum,
        persistable::setEnum);

    Assert.assertThat(persistable.getEnum(), Matchers.equalTo(TestEnum.ENUM_UNKNOWN));
  }

  @Test
  public void testConvertAndSetEnumValueWithNullWhenSpecifyingANullValue() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum(null);

    persistable.convertAndSetEnumValue(
        TestEnum.class, null, TestEnum.ENUM_UNKNOWN, pojo::getEnum, persistable::setEnum);

    Assert.assertThat(persistable.getEnum(), Matchers.nullValue());
  }

  @Test
  public void testConvertAndSetEnumValueWithEmptyWhenSpecifyingANullValue() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum("");

    persistable.convertAndSetEnumValue(
        TestEnum.class,
        TestEnum.ENUM_UNKNOWN,
        TestEnum.ENUM_UNKNOWN,
        pojo::getEnum,
        persistable::setEnum);

    Assert.assertThat(persistable.getEnum(), Matchers.equalTo(TestEnum.ENUM_UNKNOWN));
  }

  @Test
  public void testConvertAndSetOrFailIfNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern("missing.*url.*$"));

    final TestPojo pojo = new TestPojo().setUrl(null);

    persistable.convertAndSetOrFailIfNull(
        "url", pojo::getUrl, PersistableTest::toUrl, persistable::setUrl);
  }

  @Test
  public void testConvertAndSetOrFailIfInvalidUrl() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(Matchers.equalTo("testing invalid url"));
    exception.expectCause(Matchers.instanceOf(MalformedURLException.class));

    final TestPojo pojo = new TestPojo().setUrl("invalid-url");

    persistable.convertAndSetOrFailIfNull(
        "url", pojo::getUrl, PersistableTest::toUrl, persistable::setUrl);
  }

  @Test
  public void testHasUnknowns() throws Exception {
    Assert.assertThat(persistable.hasUnknowns(), Matchers.equalTo(false));
  }

  @Test
  public void testHashCodeWhenEquals() throws Exception {
    final TestPersistable persistable2 =
        new TestPersistable(PersistableTest.TYPE, PersistableTest.ID);

    Assert.assertThat(persistable.hashCode(), Matchers.equalTo(persistable2.hashCode()));
  }

  @Test
  public void testHashCodeWhenDifferent() throws Exception {
    final TestPersistable persistable2 =
        new TestPersistable(PersistableTest.TYPE, UUID.randomUUID());

    Assert.assertThat(
        persistable.hashCode(), Matchers.not(Matchers.equalTo(persistable2.hashCode())));
  }

  @Test
  public void testEqualsWhenEquals() throws Exception {
    final TestPersistable persistable2 =
        new TestPersistable(PersistableTest.TYPE, PersistableTest.ID);

    Assert.assertThat(persistable.equals(persistable2), Matchers.equalTo(true));
  }

  @Test
  public void testEqualsWhenIdentical() throws Exception {
    Assert.assertThat(persistable.equals(persistable), Matchers.equalTo(true));
  }

  @SuppressWarnings("PMD.EqualsNull" /* purposely testing equals() when called with null */)
  @Test
  public void testEqualsWhenNull() throws Exception {
    Assert.assertThat(persistable.equals(null), Matchers.equalTo(false));
  }

  @SuppressWarnings(
      "PMD.PositionLiteralsFirstInComparisons" /* purposely testing equals() when call with something else than expected */)
  @Test
  public void testEqualsWhenNotARequestInfoImpl() throws Exception {
    Assert.assertThat(persistable.equals("test"), Matchers.equalTo(false));
  }

  @Test
  public void testEqualsWhenIdIsDifferent() throws Exception {
    final TestPersistable persistable2 =
        new TestPersistable(PersistableTest.TYPE, UUID.randomUUID());

    Assert.assertThat(persistable.equals(persistable2), Matchers.not(Matchers.equalTo(true)));
  }

  private static URL toUrl(String url) throws TestInvalidFieldException {
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      throw new TestInvalidFieldException("testing invalid url", e);
    }
  }

  private static URL toUrl2(String url) throws MalformedURLException {
    return new URL(url);
  }

  private static class TestPersistable extends Persistable<TestPojo, TestPersistenceException> {
    @Nullable private String string;
    @Nullable private TestEnum anEnum;
    @Nullable private URL url;
    private boolean bool;

    TestPersistable() {
      super(PersistableTest.TYPE, TestPersistenceException.class, TestInvalidFieldException::new);
    }

    TestPersistable(String type) {
      super(type, TestPersistenceException.class, TestInvalidFieldException::new);
    }

    TestPersistable(String type, @Nullable UUID id) {
      super(type, id, TestPersistenceException.class, TestInvalidFieldException::new);
    }

    @Nullable
    public String getString() {
      return string;
    }

    public void setString(@Nullable String string) {
      this.string = string;
    }

    @Nullable
    public TestEnum getEnum() {
      return anEnum;
    }

    public void setEnum(@Nullable TestEnum anEnum) {
      this.anEnum = anEnum;
    }

    @Nullable
    public URL getUrl() {
      return url;
    }

    public void setUrl(@Nullable URL url) {
      this.url = url;
    }

    public boolean getBool() {
      return bool;
    }

    public void setBool(boolean bool) {
      this.bool = bool;
    }
  }

  private static class TestPojo extends Pojo<TestPojo> {
    public static final int CURRENT_VERSION = 1;

    @Nullable private String string;
    @Nullable private String anEnum;
    @Nullable private String url;

    public TestPojo() {
      super.setVersion(TestPojo.CURRENT_VERSION);
    }

    @Nullable
    public String getString() {
      return string;
    }

    public TestPojo setString(@Nullable String string) {
      this.string = string;
      return this;
    }

    @Nullable
    public String getEnum() {
      return anEnum;
    }

    public TestPojo setEnum(@Nullable String anEnum) {
      this.anEnum = anEnum;
      return this;
    }

    @Nullable
    public String getUrl() {
      return url;
    }

    public TestPojo setUrl(@Nullable String url) {
      this.url = url;
      return this;
    }
  }

  private static class UnknownTestPojo extends TestPojo implements UnknownPojo {}

  private static enum TestEnum {
    ENUM_A,
    ENUM_B,
    ENUM_UNKNOWN
  }

  private static class TestPersistenceException extends Exception {
    public TestPersistenceException(String message) {
      super(message);
    }

    public TestPersistenceException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  private static class TestInvalidFieldException extends TestPersistenceException {
    public TestInvalidFieldException(String message) {
      super(message);
    }

    public TestInvalidFieldException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
