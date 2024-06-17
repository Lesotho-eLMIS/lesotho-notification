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

package org.openlmis.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.notification.domain.Notification;
import org.openlmis.notification.repository.custom.NotificationRepositoryCustom;
import org.openlmis.notification.service.NotificationChannel;
import org.openlmis.notification.util.NotificationDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public class NotificationRepositoryIntegrationTest 
    extends BaseCrudRepositoryIntegrationTest<Notification, UUID> {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Autowired
  private NotificationRepository repository;

  private Pageable pageable;
  private UUID userId1 = UUID.randomUUID();
  private UUID userId2 = UUID.randomUUID();
  private UUID userId3 = UUID.randomUUID();

  private Notification[] notifications;

  @Override
  CrudRepository<Notification, UUID> getRepository() {
    return repository;
  }

  @Override
  Notification generateInstance() {
    return generateInstance(UUID.randomUUID());
  }

  private Notification generateInstance(UUID userId) {
    return new NotificationDataBuilder()
        .withUserId(userId)
        .withEmptyMessage(NotificationChannel.EMAIL)
        .buildAsNew();
  }

  @Before
  public void setUp() throws InterruptedException {
    notifications = new Notification[25];
    pageable = PageRequest.of(0, notifications.length);

    for (int i = 0; i < notifications.length; ++i) {
      // we want to create ten (0..9) notifications for the first user,
      // seven (10..16) notifications for the second user, and the rest
      // (17..25) is for the third user
      UUID userId = i < 10 ? userId1 : (i < 17 ? userId2 : userId3);

      notifications[i] = repository.save(generateInstance(userId));

      // to make sure that the createdDate field in each
      // notification will have a different value
      TimeUnit.MILLISECONDS.sleep(100);
    }

    // to make sure that array contains objects
    // and to avoid unexpected NullPointerException
    assertThat(notifications).doesNotContainNull();
  }

  @Test
  public void shouldNotAllowDuplicateChannelsForNotification() {
    expectedException.expect(DataIntegrityViolationException.class);
    expectedException.expectMessage("unq_notification_messages_notificationid_channel");

    Notification notification = new NotificationDataBuilder()
        .withMessage(NotificationChannel.EMAIL, "Body", "Subject")
        .withMessage(NotificationChannel.EMAIL, "Body", "Subject")
        .build();
    repository.saveAndFlush(notification);
  }

  @Test
  public void shouldFindAllNotificationsIfNoParamsWereSet() {
    NotificationRepositoryCustom.SearchParams searchParams = new TestSearchParams(null, null, null, null);

    Page<Notification> search = repository.search(searchParams, pageable);
    assertThat(search.getContent()).contains(notifications);
  }

  @Test
  public void shouldFindNotificationsByUserId() {
    NotificationRepositoryCustom.SearchParams searchParams =
        new TestSearchParams(userId1, null, null, null);

    Page<Notification> search = repository.search(searchParams, pageable);
    assertThat(search.getContent()).contains(Arrays.copyOfRange(notifications, 0, 10));
  }

  @Test
  public void shouldFindNotificationsAfterDate() {
    int startIndex = 14;
    NotificationRepositoryCustom.SearchParams searchParams =
        new TestSearchParams(null, notifications[startIndex].getCreatedDate(), null, null);

    Page<Notification> search = repository.search(searchParams, pageable);
    assertThat(search.getContent())
        .hasSize(notifications.length - startIndex)
        .contains(Arrays.copyOfRange(notifications, startIndex, notifications.length));
  }

  @Test
  public void shouldFindNotificationsBeforeDate() {
    int endIndex = 7;
    NotificationRepositoryCustom.SearchParams searchParams =
        new TestSearchParams(null, null, notifications[endIndex].getCreatedDate(), null);

    Page<Notification> search = repository.search(searchParams, pageable);
    assertThat(search.getContent())
        .hasSize(endIndex + 1)
        .contains(Arrays.copyOfRange(notifications, 0, endIndex + 1));
  }

  @Test
  public void shouldFindNotificationsInDateRange() {
    int startIndex = 13;
    int endIndex = 20;
    NotificationRepositoryCustom.SearchParams searchParams =
        new TestSearchParams(null, notifications[startIndex].getCreatedDate(),
            notifications[endIndex].getCreatedDate(), null);

    Page<Notification> search = repository.search(searchParams, pageable);
    assertThat(search.getContent())
        .hasSize(endIndex - startIndex + 1)
        .contains(Arrays.copyOfRange(notifications, startIndex, endIndex + 1));
  }

  @Test
  public void shouldFindNotificationsThatMatchAllSearchParams() {
    int startIndex = 7;
    int endIndex = 20;
    NotificationRepositoryCustom.SearchParams searchParams =
        new TestSearchParams(userId2, notifications[startIndex].getCreatedDate(),
            notifications[endIndex].getCreatedDate(), null);

    Page<Notification> search = repository.search(searchParams, pageable);
    assertThat(search.getContent())
        .hasSize(7)
        .contains(Arrays.copyOfRange(notifications, 10, 17));
  }

  @Getter
  @AllArgsConstructor
  private static final class TestSearchParams
      implements NotificationRepositoryCustom.SearchParams {

    private final UUID userId;
    private final ZonedDateTime sendingDateFrom;
    private final ZonedDateTime sendingDateTo;
    private final Boolean isRead;
  }
}
