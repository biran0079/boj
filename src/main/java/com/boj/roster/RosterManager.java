/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.roster;

import com.boj.jooq.tables.records.RosterRecord;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.Record1;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.boj.jooq.tables.Roster.ROSTER;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
@Singleton
public class RosterManager {

  private final DSLContext db;

  private final LoadingCache<String, Role> emailToRoleCache = CacheBuilder.newBuilder()
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build(new CacheLoader<String, Role>() {
        @Override
        public Role load(String email) throws Exception {
          return getRoleFromDb(email);
        }
      });

  @Inject
  public RosterManager(DSLContext db) {
    this.db = db;
  }

  public Role getRole(String email) {
    try {
      return emailToRoleCache.get(email);
    } catch (ExecutionException e) {
      throw Throwables.propagate(e.getCause());
    }
  }

  private Role getRoleFromDb(String email) {
    Record1<String> record = db.select(ROSTER.ROLE)
        .from(ROSTER)
        .where(ROSTER.EMAIL.eq(email))
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
    emailToRoleCache.refresh(email);
    return rosterRecord;
  }

  public void deleteById(int rosterId) {
    RosterRecord roster = db.selectFrom(ROSTER).
        where(ROSTER.ID.eq(rosterId))
        .fetchAny();
    if (roster != null) {
      roster.delete();
      emailToRoleCache.refresh(roster.getEmail());
    }
  }
}
