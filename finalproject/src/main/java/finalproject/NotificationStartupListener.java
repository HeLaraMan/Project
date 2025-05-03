package finalproject;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class NotificationStartupListener implements ServletContextListener {

    private NotificationSystem notificationSystem;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        notificationSystem = new NotificationSystem();
        // no need to manually call start(), constructor handles it
        sce.getServletContext().setAttribute("notificationSystem", notificationSystem);
        System.out.println("Notification system started.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (notificationSystem != null) {
            notificationSystem.shutdown();  // this method is defined and safe
            System.out.println("Notification system shut down.");
        }
    }
}