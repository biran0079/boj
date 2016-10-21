/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.base;

import com.boj.annotation.IsAdmin;
import com.boj.jooq.tables.records.UserRecord;
import com.boj.user.UserManager;
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
  private final UserManager userManager;

  @Inject
  ModelAndViewFactory(Provider<UserRecord> userProvider,
                      @IsAdmin Provider<Boolean> isAdmin,
                      UserManager userManager) {
    this.userProvider = userProvider;
    this.isAdmin = isAdmin;
    this.userManager = userManager;
  }

  public ModelAndView create(Map<String, Object> model, String templatePath) {
    UserRecord user = userProvider.get();
    if (!userManager.isGuestUser(user)) {
      model.put("me", userProvider.get());
      model.put("admin", isAdmin.get());
    }
    return new ModelAndView(model, templatePath);
  }
}
