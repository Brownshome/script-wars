package brownshome.scriptwars.site;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import brownshome.scriptwars.server.Server;

@WebListener
public class StartupHooks implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		Server.initialize();
	}
}
