package com.boj.base;

import com.boj.jooq.tables.records.UserRecord;
import com.boj.user.LoginState;

/**
 * Created by biran on 10/28/16.
 */
public class SessionKeys {

  public static SessionKey<UserRecord> USER_RECORD = new SessionKey<>("user");
  public static SessionKey<LoginState> LOGIN_STATE = new SessionKey<>("login_state");
}
