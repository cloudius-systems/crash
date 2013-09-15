package org.crsh.web.servlet;

import com.google.gson.Gson;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.text.Chunk;
import org.crsh.text.Text;

import java.io.IOException;

/** @author Julien Viet */
public class WSProcessContext implements ShellProcessContext {

  /** . */
  final CRaSHSession session;

  /** . */
  private StringBuilder buffer = new StringBuilder();

  public WSProcessContext(CRaSHSession session) {
    this.session = session;
  }

  public void end(ShellResponse response) {
  }

  public boolean takeAlternateBuffer() throws IOException {
    return false;
  }

  public boolean releaseAlternateBuffer() throws IOException {
    return false;
  }

  public String getProperty(String propertyName) {
    return null;
  }

  public String readLine(String msg, boolean echo) {
    return null;
  }

  public int getWidth() {
    return 80;
  }

  public int getHeight() {
    return 20;
  }

  public void write(Chunk chunk) throws IOException {
    if (chunk instanceof Text) {
      Text text = (Text)chunk;
      buffer.append(text.getText());
    } else {
      // todo
    }
  }

  public void flush() throws IOException {
    if (buffer.length() > 0) {
      session.wsSession.getBasicRemote().sendText(buffer.toString());
    }
  }
}
