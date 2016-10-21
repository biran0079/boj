package com.boj.user;

import com.boj.jooq.Tables;
import com.boj.jooq.tables.records.UserRecord;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.inject.Inject;
import org.jooq.DSLContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.boj.jooq.tables.User.USER;

/**
 * Created by biran on 10/17/16.
 */
public class UserManager {

  private static final String GUEST_USER_ID = "-1";

  private final DSLContext db;

  @Inject
  public UserManager(DSLContext db) {
    this.db = db;
  }

  public UserRecord getUser(String id) {
    return db.selectFrom(USER)
        .where(USER.ID.eq(id))
        .fetchOne();
  }

  public UserRecord create(GoogleIdToken.Payload payload) {
    UserRecord record = db.newRecord(USER);
    record.setEmail(payload.getEmail());
    record.setId(payload.getSubject());
    record.setName((String) payload.get("name"));
    record.setImageUrl((String) payload.get("picture"));
    record.store();
    return record;
  }

  public Map<String, UserRecord> getUsersByIds(Set<String> userIds) {
    Map<String, UserRecord> userRecordMap = new HashMap<>();
    for (UserRecord record : db.selectFrom(Tables.USER)
        .where(USER.ID.in(userIds))
        .fetch()) {
      userRecordMap.put(record.getId(), record);
    }
    return userRecordMap;
  }

  public UserRecord getGuestUser() {
    return getUser(GUEST_USER_ID);
  }
}
