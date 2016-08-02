package com.RyanHodin.RPG;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by Ryan on 8/2/2016.
 */
class targetingMinigame
{
	public static class output // The type for returned data.
	{
		public double distance; // Distance from a true bullseye
		public int type; // The classification of this shot

		private static MainActivity t;

		public output()
		{
			distance=-1;
			type=-1;
			MAX_DISTANCE=-1;
		}

		public static final int MISSED=6;
		public static final int GRAZED=5;
		public static final int POOR=4;
		public static final int HIT=3;
		public static final int GOOD=2;
		public static final int CRITICAL=1;
		public static final int BULLSEYE=0;

		public static double MAX_DISTANCE;

		public static String toString (int n)
		{
			switch (n)
			{
			case MISSED:
				return t.gen.nextBoolean() ? "Miss" : "Fail";
			case GRAZED:
				return "Close"+(t.gen.nextBoolean() ? (" only counts in "+(t.config.triggerEgg(1, 3) ? "handshoes and horse grenades" : "horseshoes and hand grenades")) : ", but no cigar");
			case POOR:
				return t.gen.nextBoolean() ? "Needs practice" : "Just barely";
			case HIT:
				return "Solid "+(t.gen.nextBoolean() ? "shot" : "hit");
			case GOOD:
				return "Good "+(t.gen.nextBoolean() ? "shot" : "hit");
			case CRITICAL:
				String[] roots={
						"Critical",
						"Excellent",
						"Great",
						"Incredible",
						"Sharpshooter\'s",
						"Master\'s",
						"Masterful"
				};
				String[] suffixes={
						"hit",
						"shot",
						"impact"
				};
				return roots[t.gen.nextInt(roots.length)]+" "+suffixes[t.gen.nextInt(suffixes.length)];
			case BULLSEYE:
				return t.gen.nextBoolean() ? "Bullseye" : "Perfect "+(t.gen.nextBoolean() ? "shot" : "aim");
			default:
				return "Gunshot";
			}
		}

		public static String toString(output o)
		{
			return o.toString();
		}

		@Override
		public String toString()
		{
			return toString(type);
		}
	}

	public View crosshairs;
	public View bullseye;
	public View layout;
	RelativeLayout.LayoutParams crosshairCoord;
	public double weaponSwayMultiplier;
	public int weaponSwayMaximum;
	public double timeRemaining;
	public double deltaX;
	public double deltaY;
	public Thread swayer;

	private static MainActivity t;

	public targetingMinigame()
	{
		t=Cgame.t;
		output.t=t;

		weaponSwayMultiplier=.75+(.25*t.gen.nextGaussian());
		weaponSwayMaximum=3+t.gen.nextInt(4);
		timeRemaining=t.gen.nextInt(3)+3+t.gen.nextGaussian();
		deltaX=2*(t.gen.nextDouble()+t.gen.nextInt(21)+10);
		deltaY=2*(t.gen.nextDouble()+t.gen.nextInt(21)+10);
	}

	public void startSway()
	{
		if (swayer!=null)
			swayer.interrupt();
		swayer=new Thread(new Runnable() // Improvement target
		{
			@Override
			public void run()
			{
				final Runnable r=new Runnable()
				{
					@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
					@Override
					public void run()
					{
						if (!(Build.VERSION.SDK_INT>=18 && layout.isInLayout()))
							layout.requestLayout();
					}
				};
				double angle=t.gen.nextDouble()*2*Math.PI;
				while (!Thread.interrupted())
				{
					double velocity=weaponSwayMultiplier*weaponSwayMaximum*Math.max(.25, t.gen.nextDouble())*(MainActivity.TARGET_FPS/60.0);
					if (0==t.gen.nextInt((int)(MainActivity.TARGET_FPS+1)))
					{
						angle+=Math.max(Math.PI/6, t.gen.nextDouble()*Math.PI)*(t.gen.nextBoolean() ? -1 : 1);
						velocity*=.5;
					}
					crosshairCoord.leftMargin=(int)Math.round(Math.max(0, Math.min(layout.getWidth()-crosshairs.getWidth(), crosshairCoord.leftMargin+(Math.cos(angle)*velocity))));
					crosshairCoord.topMargin=(int)Math.round(Math.max(0, Math.min(layout.getHeight()-crosshairs.getHeight(), crosshairCoord.topMargin-(Math.sin(angle)*velocity))));
					t.runOnUiThread(r);
					t.frameTick(1);
				}
			}
		});
		swayer.start();
	}

	public output result()
	{
		output out=new output();
		RelativeLayout.LayoutParams bulls=(RelativeLayout.LayoutParams)bullseye.getLayoutParams();
		RelativeLayout.LayoutParams cross=(RelativeLayout.LayoutParams)crosshairs.getLayoutParams();
		double tmp=(bulls.leftMargin+(bullseye.getWidth()*.5))-(cross.leftMargin+(crosshairs.getWidth()*.5));
		double num=(bulls.topMargin+(bullseye.getHeight()*.5))-(cross.topMargin+(crosshairs.getHeight()*.5));
		out.distance=Math.sqrt((tmp*tmp)+(num*num));
		output.MAX_DISTANCE=.5*bullseye.getWidth();
		//			out.type=(int)Math.ceil(out.distance/bullseye.getWidth()/12);  // Mathematical way. Found to be buggy and disabled: If fixed, this will be more efficient.
		double diff=bullseye.getWidth()/10;
		if (out.distance==0)
			out.type=output.BULLSEYE;
		else if (out.distance<diff)
			out.type=output.CRITICAL;
		else if (out.distance<(2*diff))
			out.type=output.GOOD;
		else if (out.distance<(3*diff))
			out.type=output.HIT;
		else if (out.distance<(4*diff))
			out.type=output.POOR;
		else if (out.distance<(5*diff))
			out.type=output.GRAZED;
		else
			out.type=output.MISSED;
		return out;
	}
}
