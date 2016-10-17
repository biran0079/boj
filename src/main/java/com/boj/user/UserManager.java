package com.boj.user;

import com.boj.jooq.tables.User;
import com.boj.jooq.tables.records.UserRecord;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.inject.Inject;
import org.jooq.DSLContext;

/**
 * Created by biran on 10/17/16.
 */
public class UserManager {

  private final DSLContext db;

  @Inject
  public UserManager(DSLContext db) {
    this.db = db;
  }

  public UserRecord getUser(String id) {
    return db.selectFrom(User.USER)
        .where(User.USER.ID.eq(id))
        .fetchOne();
  }

  public UserRecord create(GoogleIdToken.Payload payload) {
    UserRecord record = db.newRecord(User.USER);
    record.setEmail(payload.getEmail());
    record.setId(payload.getSubject());
    record.store();
    return record;
  }
}
