// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/AssertionException.java,v $
// $RCSfile: AssertionException.java,v $
// $Revision: 1.2.2.1 $
// $Date: 2004/10/14 18:27:45 $
// $Author: dietrick $
// 
// **********************************************************************

package edu.jhuapl.nstd.math.CoordConversions;

/**
 * Signals that an assertion has failed.
 * 
 * @author unascribed
 * @version $Revision: 1.2.2.1 $, $Date: 2004/10/14 18:27:45 $
 * @see Assert
 */
public class AssertionException extends RuntimeException {

    /**
     * Constructs a default <code>AssertionException</code>.
     */
    public AssertionException() {
        this("");
    }

    /**
     * Constructs an <code>AssertionException</code> with the
     * specified detail message.
     * 
     * @param s the detail message
     */
    public AssertionException(String s) {
        super(s);
    }
}