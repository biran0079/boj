/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.roster;

import com.boj.jooq.tables.records.UserRecord;
import com.google.inject.Singleton;
import org.jooq.DSLContext;

import javax.inject.Inject;

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

  public boolean isInRoster(String email) {
    return db.selectFrom(ROSTER).where(ROSTER.EMAIL.eq(email)).fetchAny() != null;
  }

  public Role getRole(UserRecord userRecord) {
    String role = db.select(ROSTER.ROLE)
        .from(ROSTER)
        .where(ROSTER.EMAIL.eq(userRecord.getEmail()))
        .fetchOne()
        .value1();
    return Role.valueOf(role);
  }
}
