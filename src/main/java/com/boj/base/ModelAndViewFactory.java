/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.base;

import com.boj.annotation.IsAdmin;
import com.boj.guice.RequestScope;
import com.boj.jooq.tables.records.UserRecord;
import com.google.inject.ProvisionException;
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
  private final RequestScope requestScope;

  @Inject
  ModelAndViewFactory(Provider<UserRecord> userProvider,
                      @IsAdmin Provider<Boolean> isAdmin,
                      RequestScope requestScope) {
    this.userProvider = userProvider;
    this.isAdmin = isAdmin;
    this.requestScope = requestScope;
  }

  public ModelAndView create(Map<String, Object> model, String templatePath) {
    if (requestScope.isSeeded(UserRecord.class)) {
      model.put("me", userProvider.get());
      model.put("admin", isAdmin.get());
    }
    return new ModelAndView(model, templatePath);
  }
}
