package org.crsh.web.servlet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.spi.Completion;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.util.Strings;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
      CRaSHSession session = new CRaSHSession(wsSession, shell);
      sessions.put(wsSession.getId(), session);
      session.send("print", shell.getWelcome());
      session.send("prompt", shell.getPrompt());
      System.out.println("Established session " + wsSession.getId());
    } else {
      System.out.println("Could not boot crash");
    }
  }

  @OnClose
  public void end(Session wsSession) {
    CRaSHSession session = sessions.remove(wsSession.getId());
    System.out.println("Removing session " + wsSession.getId());
    WSProcessContext current = session.current.getAndSet(null);
    if (current != null) {
      System.out.println("Cancelling: \"" + current.command + "\"");
      current.process.cancel();
    }
  }

  @OnMessage
  public void incoming(String message, Session wsSession) {
    CRaSHSession session = sessions.get(wsSession.getId());
    if (session != null) {
      JsonParser parser = new JsonParser();
      JsonElement json = parser.parse(message);
      if (json instanceof JsonObject) {
        JsonObject event = (JsonObject)json;
        JsonElement type = event.get("type");
        if (type.getAsString().equals("execute")) {
          String command = event.get("command").getAsString();
          int width = event.get("width").getAsInt();
          int height = event.get("height").getAsInt();
          ShellProcess process = session.shell.createProcess(command);
          WSProcessContext context = new WSProcessContext(session, process, command, width, height);
          if (session.current.getAndSet(context) == null) {
            System.out.println("Executing \"" + command + "\"");
            process.execute(context);
          } else {
            System.out.println("Could not execute \"" + command + "\"");
          }
        } else if (type.getAsString().equals("cancel")) {
          WSProcessContext current = session.current.getAndSet(null);
          if (current != null) {
            System.out.println("Cancelling: \"" + current.command + "\"");
            current.process.cancel();
          } else {
            System.out.println("Could not cancel");
          }
        } else if (type.getAsString().equals("complete")) {
          String prefix = event.get("prefix").getAsString();
          CompletionMatch completion = session.shell.complete(prefix);
          Completion completions = completion.getValue();
          Delimiter delimiter = completion.getDelimiter();
          StringBuilder sb = new StringBuilder();
          List<String> values = new ArrayList<String>();
          try {
            if (completions.getSize() == 1) {
              String value = completions.getValues().iterator().next();
              delimiter.escape(value, sb);
              if (completions.get(value)) {
                sb.append(delimiter.getValue());
              }
              values.add(sb.toString());
            }
            else {
              String commonCompletion = Strings.findLongestCommonPrefix(completions.getValues());
              if (commonCompletion.length() > 0) {
                delimiter.escape(commonCompletion, sb);
                values.add(sb.toString());
              }
              else {
                for (Map.Entry<String, Boolean> entry : completions) {
                  delimiter.escape(entry.getKey(), sb);
                  values.add(sb.toString());
                  sb.setLength(0);
                }
              }
            }
          }
          catch (IOException ignore) {
            // Should not happen
          }
          System.out.println("Complete: " + prefix + " with " + values);
          session.send("complete", values);
        }
      }
    } else {
      System.out.println("No message handler");
    }
  }
}
