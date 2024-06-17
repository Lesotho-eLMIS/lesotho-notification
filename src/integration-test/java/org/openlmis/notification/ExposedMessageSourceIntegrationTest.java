/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.notification;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.notification.i18n.ExposedMessageSource;
import org.openlmis.notification.i18n.MessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ActiveProfiles({"test", "test-run"})
@SpringBootTest(properties = {"notificationToSend.autoStartup=false"})
@Transactional
public class ExposedMessageSourceIntegrationTest {

  @Autowired
  @Qualifier("messageSource")
  private ExposedMessageSource exposedMessageSource;

  @Test
  public void shouldBePairsOfConstantValueAndPropertyKey() throws IllegalAccessException {
    Set<String> propertyKeys = getPropertyKeys();
    Set<String> constantValues = getConstantValues(MessageKeys.class);

    Set<String> all = new HashSet<>();
    all.addAll(propertyKeys);
    all.addAll(constantValues);

    for (String key : all) {
      assertThat(
          "Missing constant value for key: " + key,
          constantValues.contains(key), is(true)
      );
      assertThat(
          "Missing property entry in messages.properties for key: " + key,
          propertyKeys.contains(key), is(true)
      );
    }
  }

  private Set<String> getPropertyKeys() {
    return new HashSet<>(exposedMessageSource
        .getAllMessages(Locale.ENGLISH)
        .keySet())
        .stream()
        // those keys are saved in the database and they don't need constant values
        .filter(key -> !startsWith(key, "notification.digestConfiguration.requisition."))
        .collect(Collectors.toSet());
  }

  private Set<String> getConstantValues(Class clazz) throws IllegalAccessException {
    Set<String> set = new HashSet<>();

    for (Field field : clazz.getDeclaredFields()) {
      int modifiers = field.getModifiers();

      if (isPublic(modifiers) && isStatic(modifiers) && isFinal(modifiers)) {
        set.add(String.valueOf(field.get(null)));
      }
    }

    return set;
  }

}
