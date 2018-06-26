/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2018 VillageReach
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

package org.openlmis.notification.util;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.openlmis.notification.domain.Notification;
import org.openlmis.notification.domain.NotificationMessage;
import org.openlmis.notification.service.NotificationChannel;

public class NotificationDataBuilder {

  private UUID userId;
  private Set<NotificationMessage> messages;

  public NotificationDataBuilder() {
    userId = UUID.randomUUID();
    messages = new HashSet<>();
  }

  public NotificationDataBuilder withUserId(UUID userId) {
    this.userId = userId;
    return this;
  }

  public NotificationDataBuilder withMessage(NotificationMessage message) {
    this.messages.add(message);
    return this;
  }

  public NotificationDataBuilder withMessage(NotificationChannel channel, String body) {
    return withMessage(new NotificationMessage(channel, body));
  }

  public NotificationDataBuilder withMessage(NotificationChannel channel, String body,
      String subject) {
    return withMessage(new NotificationMessage(channel, body, subject));
  }

  public NotificationDataBuilder withEmptyMessage(NotificationChannel channel) {
    return withMessage(channel, "");
  }

  /**
   * Build notification from settings.
   * 
   * @return new notification
   */
  public Notification build() {
    Notification newNotification = new Notification(userId, messages);
    for (NotificationMessage message : messages) {
      message.setNotification(newNotification);
    }
    return newNotification;
  }
}
