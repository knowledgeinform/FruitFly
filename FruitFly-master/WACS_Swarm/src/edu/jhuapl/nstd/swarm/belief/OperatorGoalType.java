//=============================== UNCLASSIFIED ==================================
//
// Copyright (c) 2007 The Johns Hopkins University/Applied Physics Laboratory
// Developed by JHU/APL.
//
// This material may be reproduced by or for the U.S. Government pursuant to
// the copyright license under the clause at DFARS 252.227-7013 (OCT 1988).
// For any other permissions please contact JHU/APL.
//
//=============================== UNCLASSIFIED ==================================

package edu.jhuapl.nstd.swarm.belief;

import java.util.logging.*;

import java.io.Serializable;

/**
 * This enumeration defines all operator goal types.
 *
 * <P>UNCLASSIFIED
 */

public enum OperatorGoalType implements Serializable {
	TARGET_VIDEO ("Target Video Goal");

	private final String _string;

	OperatorGoalType(String string) {
		_string = string;
	}

	public String toString() { return _string; }
}

//=============================== UNCLASSIFIED ==================================
