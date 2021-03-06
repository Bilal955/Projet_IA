/* ******************************************************
 * Simovies - Eurobot 2015 Robomovies Simulator.
 * Copyright (C) 2014 <Binh-Minh.Bui-Xuan@ens-lyon.org>.
 * GPL version>=3 <http://www.gnu.org/licenses/>.
 * $Id: algorithms/BrainCanevas.java 2014-10-19 buixuan.
 * ******************************************************/
package algorithms;

import java.util.ArrayList;
import java.util.Random;

import robotsimulator.Brain;
import characteristics.IFrontSensorResult;
import characteristics.IRadarResult;
import characteristics.IRadarResult.Types;
import characteristics.Parameters.Direction;

public class BrainMainCanevas extends Brain {

	private boolean turnDir = false;
	private int countToTurn = 0;
	// ---VARIABLES---//
	private static final double HEADINGPRECISION = 0.001;
	private Random rand;
	private int shoot;
	private int shootEnemy;
	private boolean front;
	private boolean turning;
	private boolean avoid;
	private int nbBack;
	private int id;
	private static int gene = 1;

	private int cptDetectWreck = 0;

	private int nbTour;
	private boolean jePrendPlusEnCompte = false;
	private boolean avoidFinish = false;
	private boolean avance = false;
	private int cptJavance = 0;

	private double lastDir;
	private boolean firstAvoid;

	// ---CONSTRUCTORS---//
	public BrainMainCanevas() {
		super();
	}


	// ---ABSTRACT-METHODS-IMPLEMENTATION---//
	public void activate() {
		move();
		front = true;
		shoot = 0;
		shootEnemy = 0;
		nbBack = 0;
		rand = new Random();
		rand.setSeed(10);
		turning = false;
		avoid = false;
		id = gene++;
		nbTour = 0;
	}

	public void step() {
		nbTour++;
		countToTurn++;
		//System.out.println(nbTour);
		if(nbTour < 100)
			return;


		//////////////////////////////////////// TODO
		if(nbTour > 5000) {
			int a = rand.nextInt(30);
		//	System.out.println("A = "+a);
			if(a == 15 && !avoid && (countToTurn > 1500)) {
				System.out.println("HASARD : "+a+" nbTour = "+nbTour);
				countToTurn = 0;
				avoid = true;
			}
		}
		/////////////////////////


		boolean nobody = true;
		IFrontSensorResult.Types frontType = detectFront().getObjectType();
		ArrayList<IRadarResult> res = detectRadar();

		if (getHealth()<=0) { return; }


		/* Si je vois un ennemi (le plus proche) je le shoot */
		IRadarResult nearestObj = null;
		IRadarResult mechantQuiTire = null;
		IRadarResult mechantQuiTirePas = null;

		//double minDist = Double.MAX_VALUE;
		double minDistTire = Double.MAX_VALUE;
		double minDistTirePas = Double.MAX_VALUE;

		for (IRadarResult iRadarResult : res) {
			double dist = iRadarResult.getObjectDistance();
			IRadarResult.Types type = iRadarResult.getObjectType();
			if (type != IRadarResult.Types.OpponentMainBot && type != IRadarResult.Types.OpponentSecondaryBot)
				continue;

			if (type != IRadarResult.Types.OpponentMainBot) {
				if (dist < minDistTire) {
					minDistTire = dist;
					mechantQuiTire = iRadarResult;
					//nearestObj = iRadarResult;
				}
			}
			else if (type != IRadarResult.Types.TeamSecondaryBot) {
				if (dist < minDistTirePas) {
					minDistTirePas = dist;
					mechantQuiTirePas = iRadarResult;
					//nearestObj = iRadarResult;
				}
			}
		}
		nearestObj = mechantQuiTirePas;
		if(mechantQuiTire != null)
			nearestObj = mechantQuiTire;


		if (nearestObj != null) {
			if (nearestObj.getObjectType() == IRadarResult.Types.OpponentMainBot
					|| nearestObj.getObjectType() == IRadarResult.Types.OpponentSecondaryBot) {
				fire(nearestObj.getObjectDirection());
				shootEnemy++;
				return;
			}
		}
		///////////////////////////// FIN ENEMY


		/*  JE SUIS BLOQUE */
		if(wreckNear() && !avoidFinish && nbTour > 1000) {
			avoid = true;
			avance = false;
			avoidFinish = false;
			cptJavance = 0;
			firstAvoid = true;
		}

		if(avoidFinish) {
			//System.out.println("LAAA");
			// J ai fini de trouve
			if(avance) {
				//System.out.println("JAVANCE");
				move();
				cptJavance++;
				if(cptJavance > 200) {
					avance = false;
					avoidFinish = false;
				}
				return;
			}
		}



		////////////////////////////////////////////////////::
		if (turning) {
			Direction toTurn = turnDir ? Direction.RIGHT : Direction.LEFT;
			double valTmp = toTurn == Direction.RIGHT ? -Math.PI/2 : Math.PI/2;
			stepTurn(toTurn);
			if (getHeading() != 0.0 && isEndTurn(valTmp)) {
				turning = false;
				turnDir = rand.nextBoolean();
				System.out.println("Turning : turnDir = "+turnDir);
			}
			return;
		}


		// PAS TOUCHE
		if (avoid) {
			//	System.out.println("J'avoid");
			if(firstAvoid) {  // TODO
				firstAvoid = false;
			}
			else {
				if(isHeading(lastDir))
					turnDir = !turnDir;
			}
			Direction toTurn = turnDir ? Direction.RIGHT : Direction.LEFT;
			stepTurn(toTurn);
			double valTmp = toTurn == Direction.RIGHT ? -Math.PI/2 : Math.PI/2; //
			if (getHeading() != 0.0 && isEndTurn(valTmp)) {//isEndTurn(Math.PI/2)) {
				System.out.println("Avoid : turnDir = "+turnDir);
				avoid = false;
				avance = true;
				avoidFinish = true;
				turnDir = rand.nextBoolean();
			}
			lastDir = getHeading(); // TODO
			return;
		}

		//System.out.println("LAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa");



		//		if (frontType == IFrontSensorResult.Types.Wreck) {
		//			moveBack();
		//			avoid = true;
		//			return;		
		//		}

		int nbEnemy = 0;
		for (IRadarResult iRadarResult : res) {
			IRadarResult.Types type = iRadarResult.getObjectType();
			if (type == IRadarResult.Types.OpponentMainBot || type == IRadarResult.Types.OpponentSecondaryBot) {
				nbEnemy++;
				nobody = false;
			}
		}





		//		for (IRadarResult iRadarResult : res) {
		//			IRadarResult.Types type = iRadarResult.getObjectType();
		//
		//			if (type == IRadarResult.Types.Wreck && isHeading(iRadarResult.getObjectDirection())) {
		//				// System.out.println(">" + id + "j 'essaye d'eviter WRECK");
		//				moveBack();
		//				avoid = true;
		//				return;
		//			}

		//}

		// Si je vois personne a l'horizon je bouge
		if (nobody)
			front = true;

		// S'il y a un mur je recule
		if (frontType == IFrontSensorResult.Types.WALL) {
			turning = true;
			//avoid = true;
		} else if ((frontType == IFrontSensorResult.Types.TeamMainBot
				|| frontType == IFrontSensorResult.Types.TeamSecondaryBot || frontType == IFrontSensorResult.Types.Wreck)
				) {
			// System.out.println("> " + id + " front: " + frontType);
			avoid = true;
			shoot = 1; // pas tirer sur ses partenaires
		}

		// Je bouge

		if (front)
			move();
		else
			moveBack();

		shoot++;
	}


	//	public double getDistToObj(double dir) {
	//		for (IRadarResult iRadarResult : detectRadar()) {
	//			IRadarResult.Types type = iRadarResult.getObjectType();
	//			double dir2 = iRadarResult.getObjectDirection();
	//			if(Math.abs(dir - dir2) < 0.01)
	//				
	//		}
	//	}

	private boolean wreckNear() {
		for (IRadarResult iRadarResult : detectRadar()) {
			IRadarResult.Types type = iRadarResult.getObjectType();
			double dist = iRadarResult.getObjectDistance();
			if ( (type == Types.Wreck || type == Types.TeamSecondaryBot || type == Types.TeamMainBot)  && dist <= 150) {
				return true;
			}
		}
		return false;
	}

	private boolean isHeading(double dir) {
		return Math.abs(Math.sin(getHeading() - dir)) < HEADINGPRECISION;
	}

	private boolean isHeading(double dir, double heading) {
		return Math.abs(Math.sin(heading - dir)) < HEADINGPRECISION;
	}

	public boolean isEndTurn(double val) {
		return Math.abs(Math.sin((getHeading() % val) - val)) < HEADINGPRECISION
				|| Math.abs(Math.sin(getHeading() % val)) < HEADINGPRECISION;
	}

	private boolean sameDirection(double dirA, double dirB) {
		boolean diffSign = (dirA < 0 && dirB > 0) || (dirA > 0 && dirB < 0);

		return diffSign;
	}
}
