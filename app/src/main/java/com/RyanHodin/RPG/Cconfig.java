package com.RyanHodin.RPG;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Random;

class Cconfig implements Serializable, Parcelable
{
	private static final long serialVersionUID = 1686868589364848738L;

	public int difficulty; // Difficulty level.
	public boolean easterEggs; // Are easter eggs enabled?
	public int easterFrequency; // If the above is true, frequency of appearance modifier for randomly appearing eggs.
	public boolean schoolEggs; // If easter eggs are ON, do eggs like IB appear?
	public boolean GoTEggs; // If easter eggs are ON, do eggs like Khaleesi appear?
	public boolean ESEggs; // If easter eggs are ON, do eggs like Dovahkiin appear?
	public boolean litEggs; // If easter eggs are ON, do literary eggs like "It is a far, far better rest than I go to" appear?
	public boolean specMon; // Are there special actions for special monsters? Ex. "You have been eaten by a grue."
	public boolean gender; // Is gender enabled?
	public boolean twoGender; // If gender is ON, is gender restricted to Male/Female?
	public boolean specialGender; // If gender is ON AND twoGender is OFF, do we have special genders like "angel"?
	public boolean customGender; // If gender is ON AND twoGender is OFF, can the user input a custom gender? (If addressGender is ON, add custom addressing field)
	public boolean addressGender; // If gender is ON, do we add gender specific addressing? Ex. Bob the man.
	public boolean fullscreen; // Whether to use fullscreen systemUi or low profile
	public boolean autosave; // Whether to autosave on stages or just when the user leaves the game (like how config itself is saved)
	public boolean persist; // Whether cosmetic config changes persist through game loads.
	public double pauseMultiplier; // The multiplier for the pause duration
	public int batching; // How many characters are delivered in a batch
	public double difficultyMult; // The multiplier for survival.
	public Thread difficultyComputer; // The thread to actively calculate difficulty.

	public int gameNumber;

	public boolean computerPaused; // Whether the above thread should wait to perform calculations.
	public boolean computeOnce; // Whether the computation should be automatically paused after one run.

	public static MainActivity t;

	public Cconfig () // Initialize default config.
	{
		difficulty=80;
		difficultyMult=.2;
		easterEggs=true;
		easterFrequency=75;
		schoolEggs=true;
		GoTEggs=true;
		ESEggs=true;
		litEggs=true;
		specMon=true;
		gender=true;
		twoGender=false;
		specialGender=true;
		customGender=true;
		addressGender=true;
		fullscreen=true;
		autosave=true;
		persist=true;
		pauseMultiplier=1.0;
		batching=1;
		gameNumber=-1;
		computerPaused=false;
		computeOnce=false;
	}

	private Cconfig (Parcel in)
	{
		difficulty=in.readInt();
		easterFrequency=in.readInt();
		batching=in.readInt();
		pauseMultiplier=in.readDouble();
		difficultyMult=in.readDouble();
		gameNumber=in.readInt();
		boolean vals []=new boolean[14];
		in.readBooleanArray(vals);
		easterEggs=vals[0];
		schoolEggs=vals[1];
		GoTEggs=vals[2];
		ESEggs=vals[3];
		litEggs=vals[4];
		specMon=vals[5];
		gender=vals[6];
		twoGender=vals[7];
		specialGender=vals[8];
		customGender=vals[9];
		addressGender=vals[10];
		fullscreen=vals[11];
		autosave=vals[12];
		persist=vals[13];
	}

	public boolean triggerEgg(double oddsOfTrigger)
	{
		oddsOfTrigger=Math.abs(oddsOfTrigger);
		if (oddsOfTrigger>=1.0 && easterEggs)
			return triggerEgg(1.0, oddsOfTrigger);
		return easterEggs && (t.gen.nextDouble()*t.gen.nextDouble())<=(oddsOfTrigger*(easterFrequency/100.0));
	}

	public boolean triggerEgg(double num, double den)
	{
		return triggerEgg(num/den);
	}

	public boolean triggerEgg(int oneIn)
	{
		return triggerEgg(1, oneIn);
	}

	public void startCompute()
	{
		if (difficultyComputer!=null)
		{
			computeOnce=false;
			computerPaused=false;
			return;
		}
		difficultyComputer=new Thread (new Runnable ()
		{
			@Override
			public void run()
			{
				Thread cur=Thread.currentThread();
				cur.setPriority(Thread.MIN_PRIORITY);
				Random gen=new Random();
				final double tmp=7*(5+(difficulty*.9))/100;
				double tmp2;
				double sum;
				int pause;
				int p;
				while (true)
				{
					if (Thread.interrupted())
						return;
					pause=10;
					while (computerPaused)
					{
						if (t.delay(pause))
							return;
						pause+=10;
					}
					if (computeOnce)
					{
						computeOnce=false;
						computerPaused=true;
					}
					pause=10;
					while (t.game==null) // Avoid some crashes by accidentally restarting computation while game is null.
					{
						if (t.delay(pause))
							return;
						pause+=10;
					}
					tmp2=Math.min(gen.nextInt(Math.max(1,t.game.stage+1))/((double)(gen.nextInt(Math.max(1, t.game.stage+2))+1)), (gen.nextDouble()/2)+.5);
					Thread.yield();
					sum=tmp+tmp2+difficultyMult;
					if (t.delay(20))
						return;
					difficultyMult=Math.max(Math.min(sum/(gen.nextInt(2)+8), .98), .02);
					p=cur.getPriority();
					cur.setPriority(Thread.MIN_PRIORITY);
					if (t.delay(gen.nextInt(250)+900))
						return;
					if (cur.getPriority()==Thread.MIN_PRIORITY)
						cur.setPriority(p);
				}
			}
		});
		difficultyComputer.start();
	}

	public void requestDifficultyUpdate()
	{
		Thread proc=new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				computerPaused=false;
				if (difficultyComputer==null)
					return;
				difficultyComputer.setPriority(Thread.MAX_PRIORITY);
				t.delay(t.gen.nextInt(1500)+1500);
				difficultyComputer.setPriority(Thread.MIN_PRIORITY+1);
			}
		});
		proc.start();
	}

	public void updateDifficultyOnce()
	{
		computeOnce=true;
		computerPaused=false;
	}

	@Override
	public int describeContents()
	{
		return 1;
	}

	@Override
	public void writeToParcel(Parcel p, int flags)
	{
		p.writeInt(difficulty);
		p.writeInt(easterFrequency);
		p.writeInt(batching);
		p.writeDouble(pauseMultiplier);
		p.writeDouble(difficultyMult);
		p.writeInt(gameNumber);
		p.writeBooleanArray(new boolean[]
				{
						easterEggs,
						schoolEggs,
						GoTEggs,
						ESEggs,
						litEggs,
						specMon,
						gender,
						twoGender,
						specialGender,
						customGender,
						addressGender,
						fullscreen,
						autosave,
						persist
				});
	}

	public static final Parcelable.Creator<Cconfig> CREATOR=new Parcelable.Creator<Cconfig> ()
			{
		@Override
		public Cconfig createFromParcel (Parcel in)
		{
			return new Cconfig(in);
		}

		@Override
		public Cconfig[] newArray (int n)
		{
			return new Cconfig[n];
		}
			};

			public void writePrefs (final SharedPreferences.Editor target)
			{
				Thread proc=new Thread(new Runnable()
				{
					@Override
					public void run() // None of the values written here are allowed for savegames either, as saves store configs.
					{
						target.putInt("difficulty", difficulty);
						target.putInt("easterFrequency", easterFrequency);
						target.putInt("batching", batching);
						target.putFloat("pauseMultiplier", (float)pauseMultiplier);
						target.putFloat("difficultyMult", (float)difficultyMult);
						target.putBoolean("easterEggs", easterEggs);
						target.putBoolean("schoolEggs", schoolEggs);
						target.putBoolean("GoTEggs", GoTEggs);
						target.putBoolean("ESEggs", ESEggs);
						target.putBoolean("litEggs", litEggs);
						target.putBoolean("specMon", specMon);
						target.putBoolean("gender", gender);
						target.putBoolean("twoGender", twoGender);
						target.putBoolean("specialGender", specialGender);
						target.putBoolean("customGender", customGender);
						target.putBoolean("addressGender", addressGender);
						target.putBoolean("fullscreen", fullscreen);
						target.putBoolean("autosave", autosave);
						target.putBoolean("persist", persist);
						if (Build.VERSION.SDK_INT>=9)
							target.apply();
						else
							target.commit();
					}
				});
				proc.start();
			}

			public void writePrefs (SharedPreferences prefs)
			{
				writePrefs(prefs.edit());
			}

			public void writePrefs()
			{
				writePrefs(t.getSharedPreferences("RPG Preferences", 0));
			}

			public void readPrefs (final SharedPreferences sp)
			{
				Thread proc=new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						difficulty=sp.getInt("difficulty", difficulty);
						difficultyMult=sp.getFloat("difficultyMult", (float)difficultyMult);
						easterEggs=sp.getBoolean("easterEggs", easterEggs);
						easterFrequency=sp.getInt("easterFrequency", easterFrequency);
						schoolEggs=sp.getBoolean("schoolEggs", schoolEggs);
						GoTEggs=sp.getBoolean("GoTEggs", GoTEggs);
						ESEggs=sp.getBoolean("ESEggs", ESEggs);
						litEggs=sp.getBoolean("litEggs", litEggs);
						specMon=sp.getBoolean("specMon", specMon);
						gender=sp.getBoolean("gender", gender);
						twoGender=sp.getBoolean("twoGender", twoGender);
						specialGender=sp.getBoolean("specialGender", specialGender);
						customGender=sp.getBoolean("customGender", customGender);
						addressGender=sp.getBoolean("addressGender", addressGender);
						pauseMultiplier=sp.getFloat("pauseMultiplier", (float)pauseMultiplier);
						batching=sp.getInt("batching", batching);
						fullscreen=sp.getBoolean("fullscreen", fullscreen);
						autosave=sp.getBoolean("autosave", autosave);
						persist=sp.getBoolean("persist", persist);
					}
				});
				proc.start();
			}

			public void readPrefs()
			{
				readPrefs(t.getSharedPreferences("RPG Preferences", 0));
			}
}
