package org.crsh.web.servlet;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ApplicationListener;
import org.apache.tomcat.websocket.server.WsListener;
import org.apache.tomcat.websocket.server.WsServerContainer;
import org.junit.Test;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.websocket.server.ServerContainer;
import java.io.File;
import java.util.Collections;
import java.util.Set;

/** @author Julien Viet */
public class BootstrapTestCase {

  @Test
  public void testFoo() throws Exception {

    Tomcat tomcat = new Tomcat();
    Engine engine = tomcat.getEngine();

    Context ctx = tomcat.addWebapp("/app", new File("src/main/webapp").getAbsolutePath());
    ctx.addServletContainerInitializer(new ServletContainerInitializer() {
      public void onStartup(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {
        servletContext.addListener(WSLifeCycle.class);
      }
    }, Collections.<Class<?>>emptySet());
//    ctx.addApplicationListener(new ApplicationListener(WsListener.class.getName(), false));

//    WsServerContainer

    tomcat.start();

    //
    ServerContainer servetContainer = (ServerContainer)ctx.getServletContext().getAttribute("javax.websocket.server.ServerContainer");
    servetContainer.addEndpoint(ConnectorBootstrap.class);
//    servetContainer.addEndpoint(new );


    System.out.println("tomcat started");
    Thread.sleep(1000 * 1000);
    tomcat.stop();

  }

}
