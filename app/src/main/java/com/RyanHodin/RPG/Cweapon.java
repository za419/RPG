package com.RyanHodin.RPG;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.Serializable;
import java.util.Random;

class Cweapon implements Serializable, Parcelable
{
	private static final long serialVersionUID = 7651921632553878314L;

	public byte type; // There can't be all too many types. As of the writing of this comment, there are 8. We save a bit of memory by shortening the data field.
	public int characteristics;
	public String name;
	public double strengthModifier; // THIS SHOULD BE KEPT NEAR ZERO. Positive values add strength, negative subtract it. If it is too high, it will massively alter the behavior of the weapon. During construction, it is clamped to +=.2, but this can be altered directly afterwards, with extreme caution.
	protected double m_randomCondition; // Holds a random value that describes the condition of this individual weapon
	public Cweapon backup; // For holding a secondary. Usually null.
	public static MainActivity t;

	public Cweapon ()
	{
		Random gen=new Random();
		type=0;
		characteristics=0;
		name="fists";
		backup=null;
		strengthModifier=0;
		m_randomCondition=gen.nextGaussian()/8;
	}

	public Cweapon (byte typ, int vals, String nomer, Cweapon secondary)
	{
		type=typ;
		characteristics=vals;
		name=nomer;
		backup=secondary;
		strengthModifier=0;
		Random gen=new Random();
		long seedPart=gen.nextLong();
		long sum=0;
		for (int i=0; i<name.length(); ++i)
			sum+=name.charAt(i);
		gen.setSeed((seedPart|sum|name.hashCode())+sum); // Seed the generator both randomly and based on the sum of all characters in the weapon name.
		m_randomCondition=gen.nextGaussian()/8;
	}

	public Cweapon (byte typ, int vals, double strength, String nomer, Cweapon secondary)
	{
		type=typ;
		characteristics=vals;
		name=nomer;
		backup=secondary;
		strengthModifier=Math.max(Math.min(strength, .2), -.2);
		Random gen=new Random();
		long seedPart=gen.nextLong();
		long sum=0;
		for (int i=0; i<name.length(); ++i)
			sum+=name.charAt(i);
		gen.setSeed((seedPart|sum^name.hashCode())+sum); // Seed the generator both randomly and based on the sum of all characters in the weapon name.
		m_randomCondition=gen.nextGaussian()/8;
	}

	public Cweapon (Cweapon in)
	{
		type=in.type;
		characteristics=in.characteristics;
		name=in.name;
		backup=in.backup;
		strengthModifier=in.strengthModifier; // Intentionally unclamped. Copy constructors copy state entirely.
		m_randomCondition=in.m_randomCondition;
	}

	private Cweapon (Parcel in)
	{
		type=in.readByte();
		characteristics=in.readInt();
		name=in.readString();
		boolean[] getBak=new boolean [1];
		in.readBooleanArray(getBak);
		if (getBak[0])
			backup=in.readParcelable(null);
		strengthModifier=in.readDouble();
		m_randomCondition=in.readDouble();
	}

	public void saveTo(SharedPreferences.Editor edit) // Does not commit changes. This is the responsibility of the calling function.
	{
		edit.putInt("weaponType", type);
		edit.putInt("weaponCharacteristics", characteristics);
		edit.putString("weaponName", name);
		edit.putFloat("weaponStrengthModifier", (float)strengthModifier);
		edit.putFloat("weaponCondition", (float)m_randomCondition);
		edit.putBoolean("weaponHasBackup", backup!=null);
		if (backup!=null)
		{
			edit.putInt("backupWeaponType", backup.type);
			edit.putInt("backupWeaponCharacteristics", backup.characteristics);
			edit.putString("backupWeaponName", backup.name);
			edit.putFloat("backupWeaponStrengthModifier", (float)backup.strengthModifier);
			edit.putFloat("backupWeaponCondition", (float)backup.m_randomCondition);
		}
		if (Build.VERSION.SDK_INT>=9)
			edit.apply();
		else
			edit.commit();
	}

	public void loadFrom(SharedPreferences sp)
	{
		type=(byte)sp.getInt("weaponType", type);
		characteristics=sp.getInt("weaponCharacteristics", characteristics);
		name=sp.getString("weaponName", name);
		strengthModifier=sp.getFloat("weaponStrengthModifier", (float)strengthModifier);
		m_randomCondition=sp.getFloat("weaponCondition", (float)m_randomCondition);
		if (sp.getBoolean("weaponHasBackup", backup!=null))
		{
			if (backup==null)
				backup=new Cweapon();
			backup.type=(byte)sp.getInt("backupWeaponType", backup.type);
			backup.characteristics=sp.getInt("backupWeaponCharacteristics", backup.characteristics);
			backup.name=sp.getString("backupWeaponName", backup.name);
			backup.strengthModifier=sp.getFloat("backupWeaponStrengthModifier", (float)backup.strengthModifier);
			backup.m_randomCondition=sp.getFloat("backupWeaponCondition", (float)backup.m_randomCondition);
		}
	}

	public Cweapon setPrimary (Cweapon primary) // Sets the weapon like the duplicate, keeps current as backup.
	{
		Cweapon bak=new Cweapon(this);
		type=primary.type;
		characteristics=primary.characteristics;
		name=primary.name;
		backup=bak;
		m_randomCondition=primary.m_randomCondition;
		strengthModifier=primary.strengthModifier;
		return this;
	}

	public Cweapon swapWithBackup ()
	{
		return setPrimary(backup);
	}

	public static final byte TYPE_HAND_TO_HAND=0;
	public static final byte TYPE_BLUNT=1;
	public static final byte TYPE_SHARP=2;
	public static final byte TYPE_ARCHERY=3;
	public static final byte TYPE_MODERN=4;
	public static final byte TYPE_NUCLEAR=5;
	public static final byte TYPE_FUTURE=6;
	public static final byte TYPE_USED_FOR_CONVENIENCE =7; // If a weapon type is set to this, attacking with it (should) result in automatic use of the backup. Later, of course.
	// To elaborate further, that last type is used so that calling commitSuicide() with it equipped will trigger the "Gandalf slaps you. You go flying" scene.

	public static final int AUTOMATIC=1;
	protected static final double AUTOMATIC_EFFECT=.15;
	public static final int BOLT_FIRE=1<<1;
	protected static final double BOLT_FIRE_EFFECT=-.2;
	public static final int LONG_RANGE=1<<2;
	protected static final double LONG_RANGE_EFFECT=.05;
	public static final int LONG_RANGE_ONLY=1<<3;
	protected static final double LONG_RANGE_ONLY_EFFECT=-.18;
	public static final int LARGE_MAGAZINE=1<<4;
	protected static final double LARGE_MAGAZINE_EFFECT=.025;
	public static final int ONE_ROUND_MAGAZINE=1<<5;
	protected static final double ONE_ROUND_MAGAZINE_EFFECT=-.05;
	public static final int QUICK_RELOAD=1<<6;
	protected static final double QUICK_RELOAD_EFFECT=.05;
	public static final int SLOW_RELOAD=1<<7;
	protected static final double SLOW_RELOAD_EFFECT=-.03;
	public static final int ACCURATE=1<<8;
	protected static final double ACCURATE_EFFECT=.1;
	public static final int HIGH_RECOIL=1<<9;
	protected static final double HIGH_RECOIL_EFFECT=-.075;
	public static final int HIGH_CALIBER=1<<10;
	protected static final double HIGH_CALIBER_EFFECT=.15;
	public static final int LOW_CALIBER=1<<11;
	protected static final double LOW_CALIBER_EFFECT=-.25;
	public static final int EXPLOSIVE=1<<12;
	protected static final double EXPLOSIVE_EFFECT=.2;
	public static final int LOW_POWER=1<<13;
	protected static final double LOW_POWER_EFFECT=-.1;
	public static final int CLOSE_RANGE=1<<14;
	protected static final double CLOSE_RANGE_EFFECT=.02;
	public static final int CLOSE_RANGE_ONLY=1<<15;
	protected static final double CLOSE_RANGE_ONLY_EFFECT=-.275;
	public static final int HIGH_POWER_ROUNDS=1<<16;
	protected static final double HIGH_POWER_ROUNDS_EFFECT=.18;
	public static final int WEAK_ROUNDS=1<<17;
	protected static final double WEAK_ROUNDS_EFFECT=-.3;
	public static final int LIGHT=1<<18;
	protected static final double LIGHT_EFFECT=.01;
	public static final int CUMBERSOME=1<<19;
	protected static final double CUMBERSOME_EFFECT=-.325;
	public static final int LEGENDARY=1<<20;
	protected static final double LEGENDARY_EFFECT=.4;
	public static final int ANCIENT=1<<21;
	protected static final double ANCIENT_EFFECT=-.1;

	public void addCharacteristics (int flags)
	{
		characteristics|=flags;
	}

	public void removeCharacteristics (int flags)
	{
		characteristics&=~flags;
	}

	public void setCharacteristics (int flags)
	{
		characteristics=flags;
	}

	protected static boolean flagSet (int flags, int flag)
	{
		return (flags&flag)==flag;
	}

	public boolean characteristicSet (int flag)
	{
		return flagSet(characteristics, flag);
	}

	public double getAbsoluteStrength () // Normalized to 1.0 for having all characteristics and a condition of 0.
	{
		double out=1.0;
		if (!characteristicSet(AUTOMATIC))
			out*=(1-AUTOMATIC_EFFECT);
		if (!characteristicSet(BOLT_FIRE))
			out*=(1-BOLT_FIRE_EFFECT);
		if (!characteristicSet(LONG_RANGE))
			out*=(1-LONG_RANGE_EFFECT);
		if (!characteristicSet(LONG_RANGE_ONLY))
			out*=(1-LONG_RANGE_ONLY_EFFECT);
		if (!characteristicSet(LARGE_MAGAZINE))
			out*=(1-LARGE_MAGAZINE_EFFECT);
		if (!characteristicSet(ONE_ROUND_MAGAZINE))
			out*=(1-ONE_ROUND_MAGAZINE_EFFECT);
		if (!characteristicSet(QUICK_RELOAD))
			out*=(1-QUICK_RELOAD_EFFECT);
		if (!characteristicSet(SLOW_RELOAD))
			out*=(1-SLOW_RELOAD_EFFECT);
		if (!characteristicSet(ACCURATE))
			out*=(1-ACCURATE_EFFECT);
		if (!characteristicSet(HIGH_RECOIL))
			out*=(1-HIGH_RECOIL_EFFECT);
		if (!characteristicSet(HIGH_CALIBER))
			out*=(1-HIGH_CALIBER_EFFECT);
		if (!characteristicSet(LOW_CALIBER))
			out*=(1-LOW_CALIBER_EFFECT);
		if (!characteristicSet(EXPLOSIVE))
			out*=(1-EXPLOSIVE_EFFECT);
		if (!characteristicSet(LOW_POWER))
			out*=(1-LOW_POWER_EFFECT);
		if (!characteristicSet(CLOSE_RANGE))
			out*=(1-CLOSE_RANGE_EFFECT);
		if (!characteristicSet(CLOSE_RANGE_ONLY))
			out*=(1-CLOSE_RANGE_ONLY_EFFECT);
		if (!characteristicSet(HIGH_POWER_ROUNDS))
			out*=(1-HIGH_POWER_ROUNDS_EFFECT);
		if (!characteristicSet(WEAK_ROUNDS))
			out*=(1-WEAK_ROUNDS_EFFECT);
		if (!characteristicSet(LIGHT))
			out*=(1-LIGHT_EFFECT);
		if (!characteristicSet(CUMBERSOME))
			out*=(1-CUMBERSOME_EFFECT);
		if (!characteristicSet(LEGENDARY))
			out*=(1-LEGENDARY_EFFECT);
		if (!characteristicSet(ANCIENT))
			out*=(1-ANCIENT_EFFECT);
		return out+m_randomCondition+strengthModifier;
	}

	public double getRelativeStrength (int flags) // Same as above, normalized to flags.
	{
		if (flags==(AUTOMATIC|BOLT_FIRE|LONG_RANGE|LONG_RANGE_ONLY|LARGE_MAGAZINE|ONE_ROUND_MAGAZINE|QUICK_RELOAD|SLOW_RELOAD|ACCURATE|HIGH_RECOIL|HIGH_CALIBER|LOW_CALIBER|EXPLOSIVE|LOW_POWER|CLOSE_RANGE|CLOSE_RANGE_ONLY|HIGH_POWER_ROUNDS|WEAK_ROUNDS|LIGHT|CUMBERSOME))
			return getAbsoluteStrength();
		double out=1.0;
		if (!flagSet(flags, AUTOMATIC))
		{
			if (characteristicSet(AUTOMATIC))
				out*=(1+AUTOMATIC_EFFECT);
		}
		else
		{
			if (!characteristicSet(AUTOMATIC))
				out*=(1-AUTOMATIC_EFFECT);
		}
		if (!flagSet(flags, BOLT_FIRE))
		{
			if (characteristicSet(BOLT_FIRE))
				out*=(1+BOLT_FIRE_EFFECT);
		}
		else
		{
			if (!characteristicSet(BOLT_FIRE))
				out*=(1-BOLT_FIRE_EFFECT);
		}
		if (!flagSet(flags, LONG_RANGE))
		{
			if (characteristicSet(LONG_RANGE))
				out*=(1+LONG_RANGE_EFFECT);
		}
		else
		{
			if (!characteristicSet(LONG_RANGE))
				out*=(1-LONG_RANGE_EFFECT);
		}
		if (!flagSet(flags, LONG_RANGE_ONLY))
		{
			if (characteristicSet(LONG_RANGE_ONLY))
				out*=(1+LONG_RANGE_ONLY_EFFECT);
		}
		else
		{
			if (!characteristicSet(LONG_RANGE_ONLY))
				out*=(1-LONG_RANGE_ONLY_EFFECT);
		}
		if (!flagSet(flags, LARGE_MAGAZINE))
		{
			if (characteristicSet(LARGE_MAGAZINE))
				out*=(1+LARGE_MAGAZINE_EFFECT);
		}
		else
		{
			if (!characteristicSet(LARGE_MAGAZINE))
				out*=(1-LARGE_MAGAZINE_EFFECT);
		}
		if (!flagSet(flags, ONE_ROUND_MAGAZINE))
		{
			if (characteristicSet(ONE_ROUND_MAGAZINE))
				out*=(1+ONE_ROUND_MAGAZINE_EFFECT);
		}
		else
		{
			if (!characteristicSet(ONE_ROUND_MAGAZINE))
				out*=(1-ONE_ROUND_MAGAZINE_EFFECT);
		}
		if (!flagSet(flags, QUICK_RELOAD))
		{
			if (characteristicSet(QUICK_RELOAD))
				out*=(1+QUICK_RELOAD_EFFECT);
		}
		else
		{
			if (!characteristicSet(QUICK_RELOAD))
				out*=(1-QUICK_RELOAD_EFFECT);
		}
		if (!flagSet(flags, SLOW_RELOAD))
		{
			if (characteristicSet(SLOW_RELOAD))
				out*=(1+SLOW_RELOAD_EFFECT);
		}
		else
		{
			if (!characteristicSet(SLOW_RELOAD))
				out*=(1-SLOW_RELOAD_EFFECT);
		}
		if (!flagSet(flags, ACCURATE))
		{
			if (characteristicSet(ACCURATE))
				out*=(1+ACCURATE_EFFECT);
		}
		else
		{
			if (!characteristicSet(ACCURATE))
				out*=(1-ACCURATE_EFFECT);
		}
		if (!flagSet(flags, HIGH_RECOIL))
		{
			if (characteristicSet(HIGH_RECOIL))
				out*=(1+HIGH_RECOIL_EFFECT);
		}
		else
		{
			if (!characteristicSet(HIGH_RECOIL))
				out*=(1-HIGH_RECOIL_EFFECT);
		}
		if (!flagSet(flags, HIGH_CALIBER))
		{
			if (characteristicSet(HIGH_CALIBER))
				out*=(1+HIGH_CALIBER_EFFECT);
		}
		else
		{
			if (!characteristicSet(HIGH_CALIBER))
				out*=(1-HIGH_CALIBER_EFFECT);
		}
		if (!flagSet(flags, LOW_CALIBER))
		{
			if (characteristicSet(LOW_CALIBER))
				out*=(1+LOW_CALIBER_EFFECT);
		}
		if (!flagSet(flags, EXPLOSIVE))
		{
			if (characteristicSet(EXPLOSIVE))
				out*=(1+EXPLOSIVE_EFFECT);
		}
		else
		{
			if (!characteristicSet(EXPLOSIVE))
				out*=(1-EXPLOSIVE_EFFECT);
		}
		if (!flagSet(flags, LOW_POWER))
		{
			if (characteristicSet(LOW_POWER))
				out*=(1+LOW_POWER_EFFECT);
		}
		else
		{
			if (!characteristicSet(LOW_POWER))
				out*=(1-LOW_POWER_EFFECT);
		}
		if (!flagSet(flags, CLOSE_RANGE))
		{
			if (characteristicSet(CLOSE_RANGE))
				out*=(1+CLOSE_RANGE_EFFECT);
		}
		else
		{
			if (!characteristicSet(CLOSE_RANGE))
				out*=(1-CLOSE_RANGE_EFFECT);
		}
		if (!flagSet(flags, CLOSE_RANGE_ONLY))
		{
			if (characteristicSet(CLOSE_RANGE_ONLY))
				out*=(1+CLOSE_RANGE_EFFECT);
		}
		else
		{
			if (!characteristicSet(CLOSE_RANGE))
				out*=(1-CLOSE_RANGE_EFFECT);
		}
		if (!flagSet(flags, HIGH_POWER_ROUNDS))
		{
			if (characteristicSet(HIGH_POWER_ROUNDS))
				out*=(1+HIGH_POWER_ROUNDS_EFFECT);
		}
		else
		{
			if (!characteristicSet(HIGH_POWER_ROUNDS))
				out*=(1-HIGH_POWER_ROUNDS_EFFECT);
		}
		if (!flagSet(flags, WEAK_ROUNDS))
		{
			if (characteristicSet(WEAK_ROUNDS))
				out*=(1+WEAK_ROUNDS_EFFECT);
		}
		else
		{
			if (!characteristicSet(WEAK_ROUNDS))
				out*=(1-WEAK_ROUNDS_EFFECT);
		}
		if (!flagSet(flags, LIGHT))
		{
			if (characteristicSet(LIGHT))
				out*=(1+LIGHT_EFFECT);
		}
		else
		{
			if (!characteristicSet(LIGHT))
				out*=(1-LIGHT_EFFECT);
		}
		if (!flagSet(flags, CUMBERSOME))
		{
			if (characteristicSet(CUMBERSOME))
				out*=(1+CUMBERSOME_EFFECT);
		}
		else
		{
			if (!characteristicSet(CUMBERSOME))
				out*=(1-CUMBERSOME_EFFECT);
		}
		if (!flagSet(flags, LEGENDARY))
		{
			if (characteristicSet(LEGENDARY))
				out*=(1+LEGENDARY_EFFECT);
		}
		else
		{
			if (!characteristicSet(LEGENDARY))
				out*=(1-LEGENDARY_EFFECT);
		}
		if (!flagSet(flags, ANCIENT))
		{
			if (characteristicSet(ANCIENT))
				out*=(1+ANCIENT_EFFECT);
		}
		else
		{
			if (!characteristicSet(ANCIENT))
				out*=(1-ANCIENT_EFFECT);
		}
		return out+m_randomCondition+strengthModifier;
	}

	public double getNeededFlags (int needed, double effectMultiplier) // Checks strength, based solely on set positive flags. If all are met, it is normalized to 1.
	{
		double out=1;
		if (flagSet(needed, AUTOMATIC) && !characteristicSet(AUTOMATIC))
			out*=(1-(effectMultiplier*AUTOMATIC_EFFECT));
		if (flagSet(needed, LONG_RANGE) && !characteristicSet(LONG_RANGE))
			out*=(1-(effectMultiplier*LONG_RANGE_EFFECT));
		if (flagSet(needed, LARGE_MAGAZINE) && !characteristicSet(LARGE_MAGAZINE))
			out*=(1-(effectMultiplier*LARGE_MAGAZINE_EFFECT));
		if (flagSet(needed, QUICK_RELOAD) && !characteristicSet(QUICK_RELOAD))
			out*=(1-(effectMultiplier*QUICK_RELOAD_EFFECT));
		if (flagSet(needed, ACCURATE) && !characteristicSet(ACCURATE))
			out*=(1-(effectMultiplier*ACCURATE_EFFECT));
		if (flagSet(needed, HIGH_CALIBER) && !characteristicSet(HIGH_CALIBER))
			out*=(1-(effectMultiplier*HIGH_CALIBER_EFFECT));
		if (flagSet(needed, EXPLOSIVE) && !characteristicSet(EXPLOSIVE))
			out*=(1-(effectMultiplier*EXPLOSIVE_EFFECT));
		if (flagSet(needed, CLOSE_RANGE) && !characteristicSet(CLOSE_RANGE))
			out*=(1-(effectMultiplier*CLOSE_RANGE_EFFECT));
		if (flagSet(needed, HIGH_POWER_ROUNDS) && !characteristicSet(HIGH_POWER_ROUNDS))
			out*=(1-(effectMultiplier*HIGH_POWER_ROUNDS_EFFECT));
		if (flagSet(needed, LIGHT) && !characteristicSet(LIGHT))
			out*=(1-(effectMultiplier*LIGHT_EFFECT));
		if (flagSet(needed, LEGENDARY) && !characteristicSet(LEGENDARY))
			out*=(1-(effectMultiplier*LEGENDARY_EFFECT));
		return out;
	}

	public double getNeededFlags(int needed) // Shorthand form
	{
		return getNeededFlags(needed, 1);
	}

	public double getUnneededFlags(int needed, double effectMultiplier) // Checks strength solely based on negative flags that need to be unset. If all are met, it is normalized to 1.
	{
		double out=1;
		if (flagSet(needed, BOLT_FIRE) && characteristicSet(BOLT_FIRE))
			out*=(1+(effectMultiplier*BOLT_FIRE_EFFECT));
		if (flagSet(needed, LONG_RANGE_ONLY) && characteristicSet(LONG_RANGE_ONLY))
			out*=(1+(effectMultiplier*LONG_RANGE_ONLY_EFFECT));
		if (flagSet(needed, ONE_ROUND_MAGAZINE) && characteristicSet(ONE_ROUND_MAGAZINE))
			out*=(1+(effectMultiplier*ONE_ROUND_MAGAZINE_EFFECT));
		if (flagSet(needed, SLOW_RELOAD) && characteristicSet(SLOW_RELOAD))
			out*=(1+(effectMultiplier*SLOW_RELOAD_EFFECT));
		if (flagSet(needed, HIGH_RECOIL) && characteristicSet(HIGH_RECOIL))
			out*=(1+(effectMultiplier*HIGH_RECOIL_EFFECT));
		if (flagSet(needed, LOW_CALIBER) && characteristicSet(LOW_CALIBER))
			out*=(1+(effectMultiplier*LOW_CALIBER_EFFECT));
		if (flagSet(needed, LOW_POWER) && characteristicSet(LOW_POWER))
			out*=(1+(effectMultiplier*LOW_POWER_EFFECT));
		if (flagSet(needed, CLOSE_RANGE_ONLY) && characteristicSet(CLOSE_RANGE_ONLY))
			out*=(1+(effectMultiplier*CLOSE_RANGE_ONLY_EFFECT));
		if (flagSet(needed, WEAK_ROUNDS) && characteristicSet(WEAK_ROUNDS))
			out*=(1+(effectMultiplier*WEAK_ROUNDS_EFFECT));
		if (flagSet(needed, CUMBERSOME) && characteristicSet(CUMBERSOME))
			out*=(1+(effectMultiplier*CUMBERSOME_EFFECT));
		if (flagSet(needed, ANCIENT) && characteristicSet(ANCIENT))
			out*=(1+(effectMultiplier*ANCIENT_EFFECT));
		return out;
	}

	public double getUnneededFlags(int unneeded)
	{
		return getUnneededFlags(unneeded, 1);
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o==null || !(o instanceof Cweapon))
			return false;
		Cweapon w=(Cweapon)o;
		return type==w.type && characteristics==w.characteristics && strengthModifier==w.strengthModifier && /*m_randomCondition==w.m_randomCondition &&*/ name.equals(w.name);
	}

	@Override
	public int hashCode()
	{
		return name.hashCode()+(characteristics*type)+(int)Math.round(1000*(strengthModifier/*+m_randomCondition*/));
	}

	@Override
	public int describeContents ()
	{
		return 8;
	}

	@Override
	public void writeToParcel (Parcel out, int n)
	{
		out.writeByte(type);
		out.writeInt(characteristics);
		out.writeString(name);
		boolean[] writ=new boolean[1];
		if (backup==null)
		{
			writ[0]=false;
			out.writeBooleanArray(writ);
		}
		else
		{
			writ[0]=true;
			out.writeBooleanArray(writ);
			out.writeParcelable(backup, n);
		}
		out.writeDouble(strengthModifier);
		out.writeDouble(m_randomCondition);
	}

	public boolean addSwapperTo (final LinearLayout l, final Runnable runFunction) // runFunction shall be run on a newly assigned t.th.
	{
		if (backup==null)
			return false;
		if (backup.type== TYPE_USED_FOR_CONVENIENCE) // This shouldn't exist. Remove it.
		{
			if (backup.backup!=null) // If the backup has a backup
			{
				backup=backup.backup; // Add its backup to be ours
				return addSwapperTo(l, runFunction); // Try again.
			}
			backup=null;
			return false;
		}
		String[] titles={
				"Swap your primary "+name+" and your backup "+backup,
				"Switch to use your "+backup,
				"Draw your "+backup,
				"Put your "+name+" away, the "+backup+" is the tool for this job",
				"Give your "+name+" a break, use your "+backup,
				"Prepare your "+backup,
				"Prepare your "+backup+", the "+name+" is no good for this"
		};
		final String title=titles[t.gen.nextInt(titles.length)];
		final int color;
		int delta=t.gen.nextInt(10);
		if (backup.type>type || (backup.type==type && getAbsoluteStrength()>backup.getAbsoluteStrength()))
			color=Color.rgb(245+delta, 255, 245+delta);
		else if (backup.type<type || getAbsoluteStrength()<backup.getAbsoluteStrength())
			color=Color.rgb(255, 245+delta, 245+delta);
		else
			color=Color.WHITE;
		t.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Button b=new Button(t);
				b.setText(title);
				b.setTextColor(color);
				b.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						v.setOnClickListener(null);
						swapWithBackup();
						t.th=new Thread(runFunction);
						t.th.start();
					}
				});
				l.addView(b);
			}
		});
		return true;
	}

	private static final Runnable runStage=new Runnable()
	{
		@Override
		public void run()
		{
			--t.game.stage;
			t.game.runStage();
		}
	};

	public boolean addSwapperTo(final LinearLayout l) // Reasonable default: call runStage()
	{
		return addSwapperTo(l, runStage);
	}

	public static final Parcelable.Creator<Cweapon> CREATOR=new Parcelable.Creator<Cweapon> ()
			{
		@Override
		public Cweapon createFromParcel(Parcel in)
		{
			return new Cweapon (in);
		}

		@Override
		public Cweapon[] newArray (int n)
		{
			return new Cweapon[n];
		}
			};
}
