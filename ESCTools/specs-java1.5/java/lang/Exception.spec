// $Id: Exception.spec 1287 2005-03-01 02:58:57Z cok $

// Copyright (C) 2004 David Cok, Joe Kiniry

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

/*
 * @author David Cok <cok@frontiernet.net>
 * @author Joe Kiniry <kiniry@cs.kun.nl>
 */

package java.lang;

public class Exception extends Throwable
{
    /*@ public normal_behavior
      @   ensures standardThrowable(null);
      @*/
    public /*@ pure @*/ Exception();

    /*@ public normal_behavior
      @   ensures standardThrowable(message);
      @*/
    public /*@ pure @*/ Exception(String message);

    /*@ public normal_behavior
      @   ensures standardThrowable(null,cause);
      @*/
    public /*@ pure @*/ Exception(Throwable cause);

    /*@ public normal_behavior
      @   ensures standardThrowable(message,cause);
      @*/
    public /*@ pure @*/ Exception(String message, Throwable cause);
}
