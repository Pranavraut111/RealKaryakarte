package com.mandal.filter;

import com.mandal.util.SchemaMigrator;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Runs schema migrations when the application starts up.
 */
@WebListener
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[AppStartup] Application starting — running migrations...");
        SchemaMigrator.migrate();
    }
}
