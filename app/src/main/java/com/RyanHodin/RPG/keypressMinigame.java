package com.RyanHodin.RPG;

import android.view.View;

/**
 * Created by Ryan on 8/2/2016.
 */
class keypressMinigame
{
	public int presses;
	public double deltaX;
	public double deltaY;
	public double timeRemaining;
	public boolean done;
	public View button;

	public keypressMinigame()
	{
		presses=0;
		deltaX=t.gen.nextGaussian();
		deltaY=t.gen.nextGaussian();
		timeRemaining=t.gen.nextInt(3)+3+t.gen.nextGaussian();
		done=false;
	}

	public keypressMinigame(double speedMult)
	{
		presses=0;
		deltaX=speedMult*t.gen.nextGaussian();
		deltaY=speedMult*t.gen.nextGaussian();
		timeRemaining=t.gen.nextInt(3)+3+t.gen.nextGaussian();
		done=false;
	}
}
