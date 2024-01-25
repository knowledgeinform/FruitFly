//=============================== UNCLASSIFIED ==================================
//
// Copyright (c) 1999 The Johns Hopkins University/Applied Physics Laboratory
// Developed by JHU/APL.
//
// This material may be reproduced by or for the U.S. Government pursuant to
// the copyright license under the clause at DFARS 252.227-7013 (OCT 1988).
// For any other permissions please contact JHU/APL.
//
//=============================== UNCLASSIFIED ==================================

// File created: Tue Nov  9 13:10:34 1999

package edu.jhuapl.nstd.swarm.belief;

import java.util.logging.*;

import java.lang.*;



import java.util.*;

/**
 * This is the interface that defines all possible classifications
 *
 * <P>UNCLASSIFIED
 * @author Chad Hawthorne ext. 3728
 */

public interface Classification {

  public static final int DEFAULT = 0;
  public static final int UNKNOWN = 1;
  public static final int ACOUSTIC_TARGET = 2;
  //public static final int NON_ACOUSTIC_TARGET = 3;
  public static final int FRIENDLY = 4;
  public static final int ASSET = 5;
  public static final int HVA = 6;
  public static final int UGV = 7;
  public static final int UAV = 8;
  public static final int NEUTRALIZED = 9;
  public static final int PERSON_TARGET = 10;
  public static final int VEHICLE_TARGET = 11;
  public static final int ATR = 12;
  public static final int NEUTRAL = 13;
  public static final int COT_NEUTRAL = 14;
  public static final int BASE = 15;
  public static final int CAMERA_FRIENDLY = 16;
	public static final int LANDED = 17;
	public static final int AUV_SONAR_SOURCE = 18;
	public static final int SENSOR_COVERED = 19;
	public static final int SENSOR_UNCOVERED = 20;
	public static final int TARGET_COVERED = 21;
	public static final int TARGET_UNCOVERED = 22;
	public static final int VTOL = 23;
	public static final int INFO = 24;
}

//=============================== UNCLASSIFIED ==================================
