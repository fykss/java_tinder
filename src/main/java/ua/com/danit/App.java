import dbConnection.DbConnection;
import filter.FilterCookie;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import servlet.*;

import javax.servlet.DispatcherType;
import java.sql.Connection;
import java.util.EnumSet;

public class App {
    public static void main(String[] args) {
        Connection dbConn = new DbConnection().connection();
        ServletContextHandler handler = new ServletContextHandler();

        handler.addServlet(ServletTemplates.class, "/target/classes/templates/css/*");
        handler.addServlet(new ServletHolder(new ServletRegistration(dbConn)), "/reg/*");
        handler.addServlet(new ServletHolder(new ServletLogin(dbConn)), "/login/*");
        handler.addServlet(new ServletHolder(new ServletUsers(dbConn)), "/users/*");
        handler.addServlet(new ServletHolder(new ServletPeopleList(dbConn)), "/liked/*");
        handler.addServlet(new ServletHolder(new ServletChat(dbConn)), "/messages/*");
//        handler.addServlet(new ServletHolder(new ServletLogout()), "/");
        handler.addFilter(new FilterHolder(new FilterCookie()), "/*", EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST));

        Server server = new Server(8080);
        server.setHandler(handler);
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}