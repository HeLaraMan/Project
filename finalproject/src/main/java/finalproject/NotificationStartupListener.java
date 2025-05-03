package finalproject;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class NotificationStartupListener implements ServletContextListener {

//    private NotificationSystem notificationSystem;
//
//    @Override
//    public void contextInitialized(ServletContextEvent sce) {
//        notificationSystem = new NotificationSystem();
//        notificationSystem.start();  // start the scheduler thread
//        sce.getServletContext().setAttribute("notificationSystem", notificationSystem);
//        System.out.println("Notification system started.");
//    }
//
//    @Override
//    public void contextDestroyed(ServletContextEvent sce) {
//        if (notificationSystem != null) {
//            notificationSystem.stop();  // gracefully shut it down
//        }
//    }
}