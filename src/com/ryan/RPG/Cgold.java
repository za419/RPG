package com.ryan.RPG;

import android.os.*;

import java.util.*;
import java.io.Serializable;

import android.content.*;

class Cgold implements Serializable, Parcelable
{
	private static final long serialVersionUID = 7359486120840935464L;
	
	public int amount;
	public double interest; // When amount is negative, this is considered a debt. Interest can thus accumulate. This is the interest rate. THE IMPLEMENTATION WILL INSURE THIS IS POSITIVE.
	public double compoundsPerCycle; // The implementation will also insure that this is positive. If zero, it's compounded continuously.
	
	public static final double CONTINUOUSLY_COMPOUNDED=0; // Helper constant.
	
	public Cgold()
	{
		amount=0;
		interest=.05; // Reasonable default.
		compoundsPerCycle=1;
	}
	
	public Cgold(int value, double rate, double compounds)
	{
		amount=value;
		interest=Math.abs(rate);
		compoundsPerCycle=Math.abs(compounds);
	}
	
	private Cgold(Parcel in)
	{
		amount=in.readInt();
		interest=Math.abs(in.readDouble());
		compoundsPerCycle=Math.abs(in.readDouble());
	}
	
	public Cgold(Cgold cp)
	{
		amount=cp.amount;
		interest=Math.abs(cp.interest);
		compoundsPerCycle=Math.abs(cp.compoundsPerCycle);
	}
	
	public void saveTo(SharedPreferences.Editor edit) // Does not commit changes. This is the responsibility of the calling function.
	{
		edit.putInt("goldAmount", amount);
		edit.putFloat("goldInterest", (float)interest);
		edit.putFloat("goldCompounds", (float)compoundsPerCycle);
		if (Build.VERSION.SDK_INT>=9)
			edit.apply();
		else
			edit.commit();
	}
	
	public void loadFrom(SharedPreferences sp)
	{
		amount=sp.getInt("goldAmount", amount);
		interest=Math.abs(sp.getFloat("goldInterest", (float)interest));
		compoundsPerCycle=Math.abs(sp.getFloat("goldCompounds", (float)compoundsPerCycle));
	}
	
	public double interest() // Returns the current interest.
	{
		return Math.abs(interest);
	}
	
	public double interest(double rate) // Sets the interest rate. Returns the old rate.
	{
		double out=Math.abs(interest);
		interest=Math.abs(rate);
		return out;
	}
	
	public Cgold accrueInterest()
	{
		if (amount<0)
		{
			interest=Math.abs(interest);
			compoundsPerCycle=Math.abs(compoundsPerCycle);
			if (compoundsPerCycle==0) // Continuous compounding
				amount*=Math.pow(Math.E, interest);
			else
				amount*=Math.pow(1+(interest/compoundsPerCycle), compoundsPerCycle);
		}
		return this;
	}
	
	@Override
	public boolean equals (Object o)
	{
		if (o==null || !(o instanceof Cgold))
			return false;
		Cgold g=(Cgold)o;
		return amount==g.amount && interest==g.interest && compoundsPerCycle==g.compoundsPerCycle;
	}
	
	@Override
	public int hashCode()
	{
		return (int)Math.round(Math.pow(amount, 1000*(compoundsPerCycle/interest)));
	}
	
	@Override
	public String toString()
	{
		Random gen=new Random();
		switch (amount)
		{
		case 0:
			return gen.nextBoolean() ? "no gold coins" : "not a single gold coin";
		case 1:
			return gen.nextBoolean() ? "a "+(gen.nextBoolean() ? "single " : "")+"gold coin" : "one gold coin";
		default: // Improve this. Preferably change it from 20 gold coins to twenty gold coins.
			return amount+" gold coins";
		}
	}
	
	public static String toString(Cgold c)
	{
		return c.toString(); 
	}
	
	@Override
	public int describeContents()
	{
		return 16;
	}
	
	@Override
	public void writeToParcel(Parcel out, int n)
	{
		out.writeInt(amount);
		out.writeDouble(Math.abs(interest));
		out.writeDouble(Math.abs(compoundsPerCycle));
	}
	
	public static final Parcelable.Creator<Cgold> CREATOR=new Parcelable.Creator<Cgold> ()
	{
		@Override
		public Cgold createFromParcel(Parcel in)
		{
			return new Cgold(in);
		}
		
		@Override
		public Cgold[] newArray(int n)
		{
			return new Cgold[n];
		}
	};
}
