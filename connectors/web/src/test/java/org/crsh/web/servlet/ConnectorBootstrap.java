package org.crsh.web.servlet;

import org.apache.tomcat.websocket.server.DefaultServerEndpointConfigurator;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/** @author Julien Viet */
@ServerEndpoint(value = "/crash", configurator = DefaultServerEndpointConfigurator.class)
public class ConnectorBootstrap extends CRaSHConnector {

  @OnOpen
  public void start(Session wsSession) {
    super.start(wsSession);
  }

  @OnClose
  public void end(Session wsSession) {
    super.end(wsSession);
  }

  @OnMessage
  public void incoming(String message, Session wsSession) {
    super.incoming(message, wsSession);
  }
}
