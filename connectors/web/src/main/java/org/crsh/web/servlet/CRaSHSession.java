package org.crsh.web.servlet;

import org.crsh.shell.Shell;

import javax.websocket.Session;

/** @author Julien Viet */
class CRaSHSession {

  /** . */
  final Session wsSession;

  /** . */
  final Shell shell;

  CRaSHSession(Session wsSession, Shell shell) {
    this.wsSession = wsSession;
    this.shell = shell;
  }
}
