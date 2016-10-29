package com.boj.route;

import com.boj.base.MapBuilder;
import com.boj.base.ModelAndViewFactory;
import com.boj.jooq.tables.records.RosterRecord;
import com.boj.jooq.tables.records.UserRecord;
import com.boj.roster.Role;
import com.boj.roster.RosterManager;
import com.boj.submission.SubmissionManager;
import com.boj.user.UserManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by biran on 10/22/16.
 */
public class LeaderBoardRoute implements TemplateViewRoute {

  private final RosterManager rosterManager;
  private final UserManager userManager;
  private final ModelAndViewFactory factory;

  @Inject
  public LeaderBoardRoute(RosterManager rosterManager,
                          UserManager userManager,
                          ModelAndViewFactory factory) {
    this.rosterManager = rosterManager;
    this.userManager = userManager;
    this.factory = factory;
  }

  @Override
  public ModelAndView handle(Request request, Response response) throws Exception {
    List<String> studentEmails = rosterManager.getRoster(Role.STUDENT).stream()
        .map(RosterRecord::getEmail)
        .collect(Collectors.toList());
    List<UserRecord> studentUsers = userManager.getUsersByEmails(studentEmails);
    Collections.sort(studentUsers, Comparator.comparing(UserRecord::getScore).reversed());
    return factory.create(MapBuilder.create()
        .put("users", studentUsers)
        .build(),
        "leader_board.html");
  }
}
