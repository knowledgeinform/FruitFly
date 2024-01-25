//=============================== UNCLASSIFIED ==================================
//
// Copyright (c) 2009 The Johns Hopkins University/Applied Physics Laboratory
// Developed by JHU/APL.
//
// This material may be reproduced by or for the U.S. Government pursuant to
// the copyright license under the clause at DFARS 252.227-7013 (OCT 1988).
// For any other permissions please contact JHU/APL.
//
//=============================== UNCLASSIFIED ==================================

package edu.jhuapl.nstd.swarm.util;

import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.jlib.math.position.RangeBearingHeightOffset;
import java.util.*;

public class WindHistory
{

    private int updatecount;

    private NavyAngle _avgWindDir = NavyAngle.ZERO;
    private Speed _avgWindSpeed = Speed.ZERO;
    private String _name;

    protected TreeSet<METPositionTimeName> _windHistory;

	private int _maxHistoryAge = Config.getConfig().getPropertyAsInteger("WindHistory.MaxHistoryAge.Ms", 240000);

	public WindHistory(String name)
    {
        _name = name;
        _windHistory = new TreeSet(new METComparator());
	}

    public void addMETPosition(METPositionTimeName p )
    {
        Collection c = _windHistory;
        int cnt;
        if (c!=null)
        {
            cnt = c.size();
            if(cnt == 0)
            {
                _avgWindSpeed = p.getWindSpeed();
                _avgWindDir = p.getWindBearing();
            }
            else
            {
                cnt++;
                LatLonAltPosition startPosition = LatLonAltPosition.ORIGIN;
                RangeBearingHeightOffset avgcomponent = new RangeBearingHeightOffset(new Length(getAvgWindSpeed().getDoubleValue(Speed.METERS_PER_SECOND)*(cnt-1)/cnt,Length.METERS),getAvgWindDir(), new Length(0,Length.METERS));
                RangeBearingHeightOffset newcomponent = new RangeBearingHeightOffset(new Length(p.getWindSpeed().getDoubleValue(Speed.METERS_PER_SECOND)/cnt,Length.METERS), p.getWindBearing(), new Length(0,Length.METERS));
                startPosition = startPosition.translatedBy(avgcomponent).asLatLonAltPosition();
                startPosition = startPosition.translatedBy(newcomponent).asLatLonAltPosition();
                _avgWindDir = LatLonAltPosition.ORIGIN.getBearingTo(startPosition);
                _avgWindSpeed = new Speed(LatLonAltPosition.ORIGIN.getRangeTo(startPosition).getDoubleValue(Length.METERS),Speed.METERS_PER_SECOND);
            }

        }



        TreeSet t = _windHistory;

		if (t == null)
        {
            t = new TreeSet(new METComparator());
		}
		t.add(p);



		// this could be quite expensive. We have to dupe the set because of
		// concurrent modification exceptions

		SortedSet subSet = new TreeSet(t.headSet(new METPositionTimeName("", null, null, null, new Date(System.currentTimeMillis() - _maxHistoryAge))));
		if (subSet != null && subSet.size() > 0)
			t.removeAll(subSet);

	}



    public TreeSet<METPositionTimeName> getWindHistory()
    {
    return _windHistory;
	}

    public METTimeName getAverageWind()
    {
        return new METTimeName(_name,_avgWindDir,_avgWindSpeed, new Date());
    }

    /**
     * @return the _avgWindDir
     */
    public NavyAngle getAvgWindDir() {
        return _avgWindDir;
    }

    /**
     * @return the _avgWindSpeed
     */
    public Speed getAvgWindSpeed() {
        return _avgWindSpeed;
    }

	public static class METComparator implements Comparator
    {
		public METComparator() {}

		public int compare(Object o1, Object o2)
        {
			METPositionTimeName p1 = (METPositionTimeName)o1;
			METPositionTimeName p2 = (METPositionTimeName)o2;

			Date t1 = p1.getTime();
			Date t2 = p2.getTime();

			return t1.compareTo(t2);
		}
	}
}