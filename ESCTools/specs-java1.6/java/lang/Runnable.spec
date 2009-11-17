// @(#)$Id: Runnable.spec 1287 2005-03-01 02:58:57Z cok $

// Copyright (C) 2001, 2002 Iowa State University

// This file is part of JML

// JML is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.

// JML is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with JML; see the file COPYING.  If not, write to
// the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.

package java.lang;

/** JML's specification of java.lang.Runnable.
 * @version $Revision: 1287 $
 * @author Gary T. Leavens
 */
public interface Runnable {

    /*@ public behavior
      @    diverges true;
      @    assignable \everything;
      @*/
    void run();
}
