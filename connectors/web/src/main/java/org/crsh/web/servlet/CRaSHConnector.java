package org.crsh.web.servlet;

import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

/** @author Julien Viet */
@ServerEndpoint(value = "/crash")
public class CRaSHConnector {

  public CRaSHConnector() {
    System.out.println("Connector created");
  }

  /** . */
  private final ConcurrentHashMap<String, CRaSHSession> sessions = new ConcurrentHashMap<String, CRaSHSession>();

  @OnOpen
  public void start(Session wsSession) {
    URI uri = wsSession.getRequestURI();
    String path = uri.getPath();
    String contextPath = path.substring(0, path.indexOf('/', 1));
    WSContext context = WSLifeCycle.getContext(contextPath);
    if (context != null) {
      Shell shell = context.factory.create(null);
      sessions.put(wsSession.getId(), new CRaSHSession(wsSession, shell));
      System.out.println("start " + uri + " with a life cycle " + context);
    } else {
      System.out.println("Could not find crash");
    }
  }

  @OnClose
  public void end(Session wsSession) {

    CRaSHSession session = sessions.remove(wsSession.getId());
    System.out.println("stop " + session);
  }

  @OnMessage
  public void incoming(String message, Session wsSession) {
    CRaSHSession session = sessions.get(wsSession.getId());
    if (session != null) {
      System.out.println("Executed " + message);
      ShellProcess process = session.shell.createProcess(message);
      WSProcessContext context = new WSProcessContext(session);
      process.execute(context);
    } else {
      System.out.println("No message handler");
    }
  }
}
