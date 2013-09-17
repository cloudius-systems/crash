package org.crsh.web.servlet;

import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.text.CLS;
import org.crsh.text.Chunk;
import org.crsh.text.Style;
import org.crsh.text.Text;
import org.crsh.util.Safe;

import java.io.IOException;

/** @author Julien Viet */
public class WSProcessContext implements ShellProcessContext {

  /** . */
  final ShellProcess process;

  /** . */
  final CRaSHSession session;

  /** . */
  private StringBuilder buffer = new StringBuilder();

  /** . */
  final int width;

  /** . */
  final int height;

  /** . */
  final String command;

  public WSProcessContext(CRaSHSession session, ShellProcess process, String command, int width, int height) {
    this.session = session;
    this.process = process;
    this.width = width;
    this.height = height;
    this.command = command;
  }

  public void end(ShellResponse response) {
    System.out.println("Ended \"" + command + "\"");
    session.current.compareAndSet(this, null);
    Safe.flush(this);
    String msg = response.getMessage();
    if (msg.length() > 0) {
      session.send("print", msg);
    }
    String prompt = session.shell.getPrompt();
    session.send("prompt", prompt);
    session.send("end");
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
    return width;
  }

  public int getHeight() {
    return height;
  }

  public void write(Chunk chunk) throws IOException {
    if (chunk instanceof Text) {
      Text text = (Text)chunk;
      buffer.append(text.getText());
    } else if (chunk instanceof Style) {
      ((Style)chunk).writeAnsiTo(buffer);
    } else if (chunk instanceof CLS) {
      buffer.append("\033[");
      buffer.append("2J");
      buffer.append("\033[");
      buffer.append("1;1H");
    }
  }

  public void flush() throws IOException {
    if (buffer.length() > 0) {
      session.send("print", buffer.toString());
      buffer.setLength(0);
    }
  }
}
