/* ******************************************************
 * Simovies - Eurobot 2015 Robomovies Simulator.
 * Copyright (C) 2014 <Binh-Minh.Bui-Xuan@ens-lyon.org>.
 * GPL version>=3 <http://www.gnu.org/licenses/>.
 * $Id: algorithms/BrainCanevas.java 2014-10-19 buixuan.
 * ******************************************************/
package algorithms;

import robotsimulator.Brain;
import characteristics.Parameters.Direction;

public class BrainSecondaryCanevas extends Brain {
	// ---VARIABLES---//

	// ---CONSTRUCTORS---//
	public BrainSecondaryCanevas() {
		super();
	}

	// ---ABSTRACT-METHODS-IMPLEMENTATION---//
	public void activate() {

	}

	public void step() {
		if (getHeading() <= -0.5 * Math.PI) {
			move();
		} else {
			stepTurn(Direction.LEFT);
		}
	}
}
