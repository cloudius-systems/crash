package org.crsh.web.servlet;

import org.crsh.shell.ShellFactory;

/** @author Julien Viet */
public class WSContext {

  /** . */
  final ShellFactory factory;

  public WSContext(ShellFactory factory) {
    this.factory = factory;
  }
}
