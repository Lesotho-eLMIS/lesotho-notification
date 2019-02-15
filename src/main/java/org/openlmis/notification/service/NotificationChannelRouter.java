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

package org.openlmis.notification.service;

import static org.openlmis.notification.service.NotificationTransformer.CHANNEL_HEADER;
import static org.openlmis.notification.service.NotificationTransformer.READY_TO_SEND_CHANNEL;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Router;
import org.springframework.messaging.handler.annotation.Header;

@MessageEndpoint
public class NotificationChannelRouter {

  static final String EMAIL_SEND_NOW_CHANNEL = "notificationToSend.sendNow.readyToSend.email";

  /**
   * Defines which handler should be used for notification.
   */
  @Router(inputChannel = READY_TO_SEND_CHANNEL)
  public String route(@Header(CHANNEL_HEADER) NotificationChannel channel) {
    return NotificationChannel.EMAIL == channel
        ? EMAIL_SEND_NOW_CHANNEL
        : null;
  }

}