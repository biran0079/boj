package com.boj.guice;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.RequestScoped;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
public class RequestScopeModule extends AbstractModule {

  @Override
  protected void configure() {
    RequestScope requestScope = new RequestScope();
    bindScope(RequestScoped.class, requestScope);
    bind(RequestScope.class).toInstance(requestScope);
  }
}
