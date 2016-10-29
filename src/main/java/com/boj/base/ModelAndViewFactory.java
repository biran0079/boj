/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.base;

import com.boj.annotation.IsAdmin;
import com.boj.jooq.tables.records.UserRecord;
import com.boj.user.LoginState;
import spark.ModelAndView;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Map;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
@Singleton
public class ModelAndViewFactory {

  private final Provider<UserRecord> userProvider;
  private final Provider<Boolean> isAdmin;
  private final Provider<LoginState> loginState;

  @Inject
  ModelAndViewFactory(Provider<UserRecord> userProvider,
                      @IsAdmin Provider<Boolean> isAdmin,
                      Provider<LoginState> loginState) {
    this.userProvider = userProvider;
    this.isAdmin = isAdmin;
    this.loginState = loginState;
  }

  public ModelAndView create(Map<String, Object> model, String templatePath) {
    if (loginState.get() == LoginState.OK) {
      model.put("me", userProvider.get());
      model.put("admin", isAdmin.get());
    }
    model.put("state", loginState.get().toString());
    return new ModelAndView(model, templatePath);
  }
}
