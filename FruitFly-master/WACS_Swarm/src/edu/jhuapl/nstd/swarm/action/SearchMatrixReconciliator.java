//=============================== UNCLASSIFIED ==================================
//
// Copyright (c) 2008 The Johns Hopkins University/Applied Physics Laboratory
// Developed by JHU/APL.
//
// This material may be reproduced by or for the U.S. Government pursuant to
// the copyright license under the clause at DFARS 252.227-7013 (OCT 1988).
// For any other permissions please contact JHU/APL.
//
//=============================== UNCLASSIFIED ==================================

package edu.jhuapl.nstd.swarm.action;

import java.util.logging.*;

import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.belief.*;

/**
 * This class reconciles search matrices that are of different dimensions.
 * It does this by simply detecting mismatches between the search belief
 * and the search goal belief, and resetting the search belief to the goal's
 * dimensions if they are different. It is meant to run after beliefs are
 * merged on the update thread, as the belief manager is locked and the
 * subject beliefs are edited in place.
 */
public class SearchMatrixReconciliator implements Updateable {
	
	protected BeliefManager _beliefManager;

	public SearchMatrixReconciliator(BeliefManager bm) {
		_beliefManager = bm;
	}

	public void update() 
        {
            try
            {
		SearchBelief sBelief = (SearchBelief)_beliefManager.get(SearchBelief.BELIEF_NAME);
		SearchGoalBelief gBelief = (SearchGoalBelief)_beliefManager.get(SearchGoalBelief.BELIEF_NAME);

		if (sBelief == null || gBelief == null)
			return;

		PrimitiveTypeGeocentricMatrix sMatrix = sBelief.getStateSpace();
		PrimitiveTypeGeocentricMatrix gMatrix = gBelief.getStateSpace();

		if (sMatrix == null || sMatrix.getXSize() != gMatrix.getXSize() || sMatrix.getYSize() != gMatrix.getYSize() ||
				!sMatrix.getSWCorner().equals(gMatrix.getSWCorner()) ||
				!sMatrix.getNECorner().equals(gMatrix.getNECorner()))
		{
			PrimitiveTypeGeocentricMatrix newMatrix = new PrimitiveTypeGeocentricMatrix(gMatrix.getHeader());
			newMatrix.add(sMatrix);
			sBelief.setStateSpace(newMatrix);
		}

		CloudBeliefMatrix cloudBelief = (CloudBeliefMatrix)_beliefManager.get(CloudBeliefMatrix.BELIEF_NAME);
		if (cloudBelief == null)
			return;

		PrimitiveTypeGeocentricMatrix cMatrix = cloudBelief.getStateSpace();

		if (cMatrix.getXSize() != gMatrix.getXSize() || cMatrix.getYSize() != gMatrix.getYSize() ||
				!cMatrix.getSWCorner().equals(gMatrix.getSWCorner()) ||
				!cMatrix.getNECorner().equals(gMatrix.getNECorner()))
		{
			CloudGeocentricMatrix newMatrix = new CloudGeocentricMatrix(gMatrix.getHeader());
			newMatrix.add(cMatrix);
			cloudBelief.setStateSpace(newMatrix);
			Logger.getLogger("GLOBAL").info("moved cloud matrix");
		}
            }
            catch (Exception e)
            {
                System.err.println ("Exception in update thread - caught and ignored");
                e.printStackTrace();
            }

	}
}
