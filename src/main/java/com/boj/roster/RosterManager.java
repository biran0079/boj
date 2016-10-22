/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.roster;

import com.boj.jooq.tables.records.RosterRecord;
import com.boj.jooq.tables.records.UserRecord;
import com.google.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.Record1;

import javax.inject.Inject;
import java.util.List;

import static com.boj.jooq.tables.Roster.ROSTER;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
@Singleton
public class RosterManager {

  private final DSLContext db;

  @Inject
  public RosterManager(DSLContext db) {
    this.db = db;
  }

  public Role getRole(UserRecord userRecord) {
    Record1<String> record = db.select(ROSTER.ROLE)
        .from(ROSTER)
        .where(ROSTER.EMAIL.eq(userRecord.getEmail()))
        .fetchOne();
    if (record == null) {
      return Role.ALIEN;
    }
    return Role.valueOf(record.value1());
  }

  public List<RosterRecord> getRoster() {
    return db.selectFrom(ROSTER).fetch();
  }

  public List<RosterRecord> getRoster(Role role) {
    return db.selectFrom(ROSTER)
        .where(ROSTER.ROLE.eq(role.toString()))
        .fetch();
  }

  public RosterRecord createRosterEntry(String email, Role role) {
    RosterRecord rosterRecord = db.newRecord(ROSTER);
    rosterRecord.setEmail(email);
    rosterRecord.setRole(role.toString());
    rosterRecord.store();
    return rosterRecord;
  }

  public void deleteById(int rosterId) {
    db.deleteFrom(ROSTER)
        .where(ROSTER.ID.eq(rosterId))
        .execute();
  }
}
