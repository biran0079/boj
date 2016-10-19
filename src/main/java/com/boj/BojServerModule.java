package com.boj;

import com.boj.annotation.CheckstyleConfigPath;
import com.boj.annotation.CheckstyleJarPath;
import com.boj.annotation.IsAdmin;
import com.boj.annotation.JunitClassPath;
import com.boj.guice.RequestScopeModule;
import com.boj.jooq.tables.records.UserRecord;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.servlet.RequestScoped;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.inject.Singleton;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by biran on 10/17/16.
 */
public class BojServerModule extends AbstractModule {
  private static final String DB_URL = "jdbc:sqlite:/data/boj/boj.db";

  @Override
  protected void configure() {
    install(new RequestScopeModule());
    bind(UserRecord.class).in(RequestScoped.class);
    bindConstant().annotatedWith(JunitClassPath.class)
        .to(getPath("./junit-4.12.jar") + ":"
            + getPath("./hamcrest-core-1.3.jar"));
    bindConstant().annotatedWith(CheckstyleJarPath.class)
        .to(getPath("./checkstyle-7.1.2-all.jar"));
    bindConstant().annotatedWith(CheckstyleConfigPath.class)
        .to(getPath("./boj_check.xml"));
  }

  private static final String getPath(String relativePath) {
    File file = new File(relativePath);
    if (!file.exists()) {
      throw new RuntimeException("file " + relativePath + " not found");
    }
    return file.getAbsolutePath();
  }

  @Provides
  @RequestScoped
  @IsAdmin
  boolean providesIsAdmin(UserRecord userRecord) {
    return userRecord.getEmail().equals("biran0079@gmail.com");
  }

  @Provides
  @Singleton
  Flyway provideFlyway() {
    Flyway flyway = new Flyway();
    flyway.setDataSource(DB_URL, null, null);
    return flyway;
  }

  @Provides
  @Singleton
  Connection providesSqlConnection() throws ClassNotFoundException, SQLException {
    Class.forName("org.sqlite.JDBC");
    return DriverManager.getConnection(DB_URL);
  }

  @Provides
  @Singleton
  DSLContext providesDslContext(Connection con) {
    return DSL.using(con, SQLDialect.SQLITE);
  }
}
