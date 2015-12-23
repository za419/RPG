package com.RyanHodin.RPG;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

class Cuser implements Serializable, Parcelable
{
	private static final long serialVersionUID = 460425255578699183L;

	public String name;
	public int parsedGender;
	public String gender;
	public String genderAddress;
	public Cweapon weapon;
	public Cgold gold;
	public Map<String, String> genderAddressComp;
	public Map<String, Integer> genderComp;
	public Thread worker;
	public boolean dead;
	public boolean isArthur;

	public static MainActivity t;

	public Cuser ()
	{
		name=null;
		gender=null;
		weapon=new Cweapon();
		gold=new Cgold();
		dead=false;
		parsedGender=-1;
		genderAddress=null;
		buildGenderAddressComp();
		buildGenderComp();
		isArthur=false;
	}

	private Cuser (Parcel in)
	{
		name=in.readString();
		gender=in.readString();
		genderAddress=in.readString();
		parsedGender=in.readInt();
		weapon=in.readParcelable(Cweapon.class.getClassLoader());
		gold=in.readParcelable(Cgold.class.getClassLoader());
		boolean vals []=new boolean[1];
		in.readBooleanArray(vals);
		isArthur=vals[0];
	}

	public void saveTo(SharedPreferences.Editor edit) // Does not commit changes. This is the responsibility of the calling function.
	{
		edit.putString("userName", name);
		edit.putString("userGender", gender);
		edit.putString("userGenderAddress", genderAddress);
		edit.putInt("userParsedGender", parsedGender);
		edit.putBoolean("userIsArthur", isArthur);
		weapon.saveTo(edit);
		gold.saveTo(edit);
		if (Build.VERSION.SDK_INT>=9)
			edit.apply();
		else
			edit.commit();
	}

	public void loadFrom(SharedPreferences sp)
	{
		name=sp.getString("userName", name);
		gender=sp.getString("userGender", gender);
		genderAddress=sp.getString("userGenderAddress", genderAddress);
		parsedGender=sp.getInt("userParsedGender", parsedGender);
		isArthur=sp.getBoolean("userIsArthur", isArthur);
		if (Thread.interrupted())
			return;
		weapon.loadFrom(sp);
		if (Thread.interrupted())
			return;
		gold.loadFrom(sp);
	}

	private void buildGenderAddressComp()
	{
		if (genderAddressComp!=null)
			return;
		genderAddressComp=new HashMap<String, String>(90, .2f);
		genderAddressComp.put("Male", "The Man");
		genderAddressComp.put("Female", "The Warrior Princess");
		genderAddressComp.put("Transsexual male", "The Masculine");
		genderAddressComp.put("Transsexual female", "The Feminine");
		genderAddressComp.put("Metrosexual male", "The Novelly Gendered Man");
		genderAddressComp.put("Metrosexual female", "The Novelly Gendered Warrior Princess");
		genderAddressComp.put("Male to Female", "The Convert to Femininity");
		genderAddressComp.put("Female to Male", "The Convert to Masculinity");
		genderAddressComp.put("Uncertain", "The Eternally unsure");
		genderAddressComp.put("Unwilling to say", "The Private");
		genderAddressComp.put("It\'s complicated", "The Complex");
		genderAddressComp.put("Genderqueer", "The Radically Gendered");
		genderAddressComp.put("Dual", "The One of Pure Duality, the Tutu Wearer");
		genderAddressComp.put("Male, but curious as to what being a female is like", "The Explorative Man");
		genderAddressComp.put("Female, but curious as to what being a male is like", "The Explorative Warrior Princess");
		genderAddressComp.put("Male, but overweight, so have moobs", "The Average Male American");
		genderAddressComp.put("Female, but have Adam\'s apple", "The Biologically Cursed Warrior Princess");
		genderAddressComp.put("Hermaphrodite with strong male leanings", "The One who prefers his Male Side");
		genderAddressComp.put("Hermaphrodite with strong female leanings", "The One who prefers her Inner Warrior Princess");
		genderAddressComp.put("Hermaphrodite with no strong gender leanings", "The One who perfers not to choose between one\'s own components");
		genderAddressComp.put("Conjoined twin - Male", "The Man attached to his Twin");
		genderAddressComp.put("Conjoined twin - Female", "The Warrior Princess attached to her Twin");
		genderAddressComp.put("Conjoined twin - Other", "The Other attached to a Twin");
		genderAddressComp.put("Born without genitals - Identify as male", "The Ungendered by birth, but Man by choice");
		genderAddressComp.put("Born without genitals - Identify as female", "The Ungendered by birth, but a Warrior Princess by choice");
		genderAddressComp.put("Born without genitals - Identify otherwise", "The Ungendered by birth, and Radically Gendered by choice");
		genderAddressComp.put("Born without genitals - and proud of it", "The Ungendered by birth, and Highly Courageous");
		genderAddressComp.put("Born male, had bad circumcision, raised female", "The Unfortunately Castrated");
		genderAddressComp.put("WOMYN, thank you very much!", "The Staunchly Feminist, who will not let words be");
		genderAddressComp.put("Angel", "The Heavenly");
		genderAddressComp.put("Mortal Angel", "The Once Heavenly, who fell in love with a mortal woman");
		genderAddressComp.put("Sentient Artificial Intelligence - Identify as ungendered", "The Ungendered Computer, The Most Hailed of Technology");
		genderAddressComp.put("Sentient Artificial Intelligence - Identify as male", "The Masculine Computer, The Most Hailed and Manly of Technology");
		genderAddressComp.put("Sentient Artificial Intelligence - Identify as female", "The Feminine Computer, The Most Hailed of all Technological Acheivements");
		genderAddressComp.put("Sentient Artificial Intelligence - Identify as other", "The Otherwise Gendered Computer, The Most Hailed of all Technology With Unspecified Gender");
		genderAddressComp.put("Household pet that walked across the device - Male", "The Manly Pet of Humans");
		genderAddressComp.put("Household pet that walked across the device - Female", "The Warrior Princess Pet of Humans");
		genderAddressComp.put("Household pet that walked across the device - Other", "The Radically Gendered Pet of Humans");
		genderAddressComp.put("Cross Dresser", "The Fashion Flexible");
		genderAddressComp.put("In between", "The Moderate");
		genderAddressComp.put("Intersex", "The One In the Middle");
		genderAddressComp.put("Pangender", "The Universally Gendered");
		genderAddressComp.put("Two spirit", "The One of Two Spirits");
		genderAddressComp.put("Other", "The Differentially Gendered");
		genderAddressComp.put("Neutrois", "The Neutrally Gendered");
		genderAddressComp.put("Prefer not to say", "The One Who Treasures Privacy");
		genderAddressComp.put("None of your business", "The One Who Rather Rudely Refuses Answers");
		genderAddressComp.put("Multiple", "The One of Non-Singular Genders");
		genderAddressComp.put("Unsure", "The Indecisive One");
		genderAddressComp.put("The Dothraki do not follow your Genders", "The Rude Horseman");
		genderAddressComp.put("Khalëësi", "Also Known As Daenarys Targaryen, Stormborn Mother of Dragons, Native Speaker of Valyrian");
		genderAddressComp.put("Kanye West", "The Self-Worshipper");
		genderAddressComp.put("Cheese", "The Delicious");
		genderAddressComp.put("Raygun", "The Superweapon");
		genderAddressComp.put("Dragon - Male", "The Son of Daenarys Stormborn");
		genderAddressComp.put("Dragon - Female", "The Daughter of Daenarys of house Targaryen");
		genderAddressComp.put("Dragon - Other", "The Child of Daenarys, Khalëësi to Khal Drogo");
		genderAddressComp.put("Direwolf", "The Beast of House Stark");
		genderAddressComp.put("White Walker", "The Fear of Men");
		genderAddressComp.put("Child of the Forest", "The Legend of Men");
		genderAddressComp.put("Khajiit", "The Sentient Feline");
		genderAddressComp.put("Dovahkiin", "The One who is too proud of Destiny to mention Gender");
		genderAddressComp.put("Dovah", "The Legendary Ungendered Beast of Men");
		genderAddressComp.put("Draugr", "The Undead");
		genderAddressComp.put("Student", "The \"Studious\"");
		genderAddressComp.put("IB Student", "The One Who is too Sleep Deprived To Find Their Own Gender, But Plays RPGs Anyway");
		genderAddressComp.put("Teacher", "The One Who Attempts to Induce Learning");
		genderAddressComp.put("IB Teacher", "The One Who Kills IB Students via Homework");
	}

	private void buildGenderComp()
	{
		if (genderComp!=null)
			return;
		genderComp=new HashMap<String, Integer>(90, .2f);
		genderComp.put("Male", 0);
		genderComp.put("Female", 1);
		genderComp.put("Transsexual male", 2);
		genderComp.put("Transsexual female", 2);
		genderComp.put("Metrosexual male", 3);
		genderComp.put("Metrosexual female", 3);
		genderComp.put("Male to Female", 1);
		genderComp.put("Female to Male", 0);
		genderComp.put("Uncertain", 4);
		genderComp.put("Unwilling to say", 5);
		genderComp.put("It\'s complicated", 4);
		genderComp.put("Genderqueer", 6);
		genderComp.put("Dual", 7);
		genderComp.put("Male, but curious as to what being a female is like", 0);
		genderComp.put("Female, but curious as to what being a male is like", 1);
		genderComp.put("Male, but overweight, so have moobs", 0);
		genderComp.put("Female, but have Adam\'s apple", 1);
		genderComp.put("Hermaphrodite with strong male leanings", 0);
		genderComp.put("Hermaphrodite with strong female leanings", 1);
		genderComp.put("Hermaphrodite with no strong gender leanings", 8);
		genderComp.put("Conjoined twin - Male", 9);
		genderComp.put("Conjoined twin - Female", 9);
		genderComp.put("Conjoined twin - Other", 9);
		Thread.yield();
		genderComp.put("Born without genitals - Identify as male", 10);
		genderComp.put("Born without genitals - Identify as female", 10);
		genderComp.put("Born without genitals - Identify otherwise", 10);
		genderComp.put("Born without genitals - and proud of it", 10);
		genderComp.put("Born male, had bad circumcision, raised female", 1);
		genderComp.put("WOMYN, thank you very much!", 1);
		genderComp.put("Angel", 11);
		genderComp.put("Mortal Angel", 11);
		genderComp.put("Sentient Artificial Intelligence - Identify as ungendered", 12);
		genderComp.put("Sentient Artificial Intelligence - Identify as male", 12);
		genderComp.put("Sentient Artificial Intelligence - Identify as female", 12);
		genderComp.put("Sentient Artificial Intelligence - Identify as other", 12);
		genderComp.put("Household pet that walked across the device - Male", 13);
		genderComp.put("Household pet that walked across the device - Female", 13);
		genderComp.put("Household pet that walked across the device - Other", 13);
		genderComp.put("Cross Dresser", 4);
		genderComp.put("In between", 14);
		genderComp.put("Intersex", 14);
		genderComp.put("Pangender", 15);
		genderComp.put("Two spirit", 8);
		genderComp.put("Other", 14);
		Thread.yield();
		genderComp.put("Neutrois", 14);
		genderComp.put("Prefer not to say", 5);
		genderComp.put("None of your business", 5);
		genderComp.put("Multiple", 15);
		genderComp.put("Unsure", 4);
		genderComp.put("The Dothraki do not follow your Genders", 16);
		genderComp.put("Khalëësi", 17);
		genderComp.put("Kanye West", 30);
		genderComp.put("Cheese", 31);
		genderComp.put("Raygun", 18);
		genderComp.put("Dragon - Male", 19);
		genderComp.put("Dragon - Female", 19);
		genderComp.put("Dragon - Other", 19);
		genderComp.put("Direwolf", 20);
		genderComp.put("White Walker", 21);
		genderComp.put("Child of the Forest", 21);
		genderComp.put("Khajiit", 22);
		genderComp.put("Dovahkiin", 23);
		genderComp.put("Dovah", 24);
		genderComp.put("Draugr", 25);
		genderComp.put("Student", 26);
		genderComp.put("IB Student", 27);
		genderComp.put("Teacher", 28);
		genderComp.put("IB Teacher", 29);
	}

	public void parseGenderAddress()
	{
		if (genderAddressComp==null)
			buildGenderAddressComp();
		genderAddress=genderAddressComp.get(gender);
		if (genderAddress==null)
			genderAddress="";
	}

	public void parseGender()
	{
		if (genderComp==null)
			buildGenderComp();
		Integer tmp=genderComp.get(gender);
		if (tmp==null)
			parsedGender=999999999;
		else
			parsedGender=tmp.intValue();
	}

	public void commitSuicide()
	{
		t.displayHandler.removeMessages(1);
		t.config.computerPaused=true;
		if (t.game.stage==6 && t.game.line==1)
		{
			t.th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					dead=true;
					t.say("Extreme Failure", "You steal Gandalf\'s sword, and stab yourself.");
				}
			});
		}
		else if (t.game.stage==21)
		{
			if (t.game.onMove(2+t.gen.nextInt(5)))
				return;
			t.game.line=1;
			t.th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					t.say("The Superweapon", "You take a hidden path, and come to a chamber. On the ground, there is an odd looking object.\n\n\n\tIt looks... Almost like a pistol, but with all sorts of strange attachments:\n\t\tThe sight is a pair of perpendicular lines inscribed in a circle.\n\t\tThe weapon pulses green along the barrel, energy radiating along progressively smaller rings along the length of the barrel.\n\t\tThe entire weapon is made of some strange metal, like none you\'ve ever seen before, a shiny purplish red.");
				}
			});
		}
		else if (t.game.stage==36)
		{
			t.th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					weapon.setPrimary(new Cweapon(Cweapon.TYPE_NUCLEAR, Cweapon.ACCURATE|Cweapon.CLOSE_RANGE|Cweapon.EXPLOSIVE|Cweapon.HIGH_CALIBER|Cweapon.HIGH_POWER_ROUNDS|Cweapon.LIGHT|Cweapon.LONG_RANGE, "suitcase nuke", null));
					t.say("Manhattan Project","You see a glimmer in a corner. You grab it.\n\n\tYou examine it...\n\n\tIt\'s a "+weapon+"!\n\n\n\tYou decide to grab it.");
				}
			});
		}
		else if (t.game.stage==22 && t.game.line==1)
		{
			dead=true;
			t.th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					t.say("You grab the object in a bad way, pulling the trigger.\n\n\n\tYou see a bright flash."+(t.config.triggerEgg(.5) ? "\n\n\tYou become aware that you have been turned into a pile of ash." : ""));
				}
			});
		}
		else
		{
			dead=true;
			t.th=new Thread (new Runnable()
			{
				@Override
				public void run()
				{
					switch (weapon.type)
					{
					case Cweapon.TYPE_HAND_TO_HAND:
						t.say("You punch yourself in the face, and lose consciousness.");
						break;
					case Cweapon.TYPE_BLUNT:
						t.say("You drive the "+weapon+" through your eye.");
						break;
					case Cweapon.TYPE_SHARP:
						String action;
						if (t.gen.nextBoolean())
						{
							if (t.gen.nextBoolean())
								action="behead";
							else
								action="eviscerate";
						}
						else
						{
							if (t.gen.nextBoolean())
								action="stab";
							else
								action="commit a hastened version of hara-kiri upon";
						}
						t.say("Holding the "+weapon+" in both hands, you "+action+" yourself.");
						break;
					case Cweapon.TYPE_ARCHERY:
						t.say("You draw an arrow from your quiver, and stab yourself.");
						break;
					case Cweapon.TYPE_MODERN:
						String bodyPart;
						if (t.gen.nextBoolean())
						{
							if (t.gen.nextBoolean())
								bodyPart="head";
							else
								bodyPart="neck";
						}
						else
						{
							if (t.gen.nextBoolean())
								bodyPart="heart";
							else
								bodyPart="groin";
						}
						t.say("You point your "+weapon+" at your "+bodyPart+", and pull the trigger.");
						break;
					case Cweapon.TYPE_NUCLEAR:
						t.say("You grab the detonator for your "+weapon+", and hit the big red button.");
						break;
					case Cweapon.TYPE_FUTURE:
						t.say(t.capitalize(weapon.name)+"s are Strong","You point the "+weapon+" at your chest.\nRight when you\'re about to fire, Gandalf appears, and yells,\n\t\"No! You must not!\"");
						break;
					case Cweapon.TYPE_USED_FOR_CONVENIENCE:
					default:
						t.say("Gandalf slaps you, and you go flying, dropping everything along the way.\n\tYou see a familiar cave up ahead... You think you might be heading for it.\n\nYou hit your head on a rock on your way flying inside, and you lose consciousness.");
					}
				}
			});
		}
		t.th.start();
		t.game.prepContinueButton();
		if (weapon.type==Cweapon.TYPE_FUTURE)
			t.runOnUiThread(new Runnable()
			{
				@Override
				public void run ()
				{
					t.findViewById(R.id.gameContinueButton).setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							weapon.type = Cweapon.TYPE_USED_FOR_CONVENIENCE;
							commitSuicide();
						}
					});
				}
			});
		else if (weapon.type==Cweapon.TYPE_USED_FOR_CONVENIENCE)
			t.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					t.findViewById(R.id.gameContinueButton).setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							t.user = new Cuser();
							t.config.difficultyComputer.interrupt();
							t.startPlay(v);
						}
					});
				}
			});
		t.config.computerPaused=false;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o==null || !(o instanceof Cuser))
			return false;
		Cuser u=(Cuser)o;
		return toString().equals(u.toString()) && gold.equals(u.gold) && weapon.equals(u.weapon);
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode()+weapon.hashCode()+gold.hashCode();
	}

	@Override
	public String toString()
	{
		return name+(t.config.gender && t.config.addressGender ? " "+genderAddress : "");
	}

	@Override
	public int describeContents ()
	{
		return 2;
	}

	@Override
	public void writeToParcel(Parcel p, int n)
	{
		p.writeString(name);
		p.writeString(gender);
		p.writeString(genderAddress);
		p.writeInt(parsedGender);
		p.writeParcelable(weapon, n);
		p.writeParcelable(gold, n);
		p.writeBooleanArray(new boolean[] {isArthur});
	}

	public static final Parcelable.Creator<Cuser> CREATOR=new Parcelable.Creator<Cuser>()
	{
		@Override
		public Cuser createFromParcel (Parcel in)
		{
			return new Cuser(in);
		}

		@Override
		public Cuser[] newArray (int n)
		{
			return new Cuser[n];
		}
	};
}
