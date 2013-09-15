package org.crsh.web.servlet;

import org.crsh.plugin.WebPluginLifeCycle;
import org.crsh.shell.ShellFactory;
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ConcurrentHashMap;

/** @author Julien Viet */
@WebListener
public class WSLifeCycle extends WebPluginLifeCycle {

  /** . */
  private static final ConcurrentHashMap<String, WSContext> registry = new ConcurrentHashMap<String, WSContext>();

  public static WSContext getContext(String context) {
    if (context == null) {
      throw new NullPointerException("No null context allowed");
    }
    return registry.get(context);
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    super.contextInitialized(sce);
    ShellFactory factory = getContext().getPlugin(ShellFactory.class);
    registry.put(sce.getServletContext().getContextPath(), new WSContext(factory));

    // Hack for avoiding NPE for now
    getContext().refresh();
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    registry.remove(sce.getServletContext().getContextPath());
    super.contextDestroyed(sce);
  }

  @Override
  protected FS createCommandFS(ServletContext context) {
    try {
      FS fs = super.createCommandFS(context);
      fs.mount(Thread.currentThread().getContextClassLoader(), Path.get("/crash/commands/"));
      // fs.mount(commands);
      return fs;
    }
    catch (Exception e) {
      throw new UndeclaredThrowableException(e);
    }
  }

  @Override
  protected FS createConfFS(ServletContext context) {
    try {
      FS fs = super.createCommandFS(context);
      fs.mount(Thread.currentThread().getContextClassLoader(), Path.get("/crash/"));
      return fs;
    }
    catch (Exception e) {
      throw new UndeclaredThrowableException(e);
    }
  }
}
