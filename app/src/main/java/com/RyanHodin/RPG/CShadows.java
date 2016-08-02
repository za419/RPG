package com.RyanHodin.RPG;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.Serializable;

/**
 * Created by Ryan on 8/2/2016.
 */
class CShadows implements Parcelable, Serializable{
	public Cgame game;
	private MainActivity t;

	private static final long serialVersionUID=0L; // Augment when appropriate

	byte number;
	byte stage;
	byte input;

	public CShadows() {
		t=Cgame.t;
		game=t.game;

		number=(byte)((t.config.difficultyMult*t.gen.nextInt(10))+3);
		stage=0;
		input=0;
	}

	private CShadows(Parcel in) {
		t=Cgame.t;
		game=t.game;

		number=in.readByte();
		stage=in.readByte();
		input=in.readByte();
	}

	@Override
	public int describeContents()
	{
		return 9;
	}

	@Override
	public void writeToParcel(Parcel out, int unused) {
		out.writeByte(number);
		out.writeByte(stage);
		out.writeByte(input);
	}

	public static final Parcelable.Creator<CShadows> CREATOR=new Parcelable.Creator<CShadows> ()
	{
		@Override
		public CShadows createFromParcel (Parcel in)
		{
			return new CShadows(in);
		}

		@Override
		public CShadows[] newArray (int n)
		{
			return new CShadows[n];
		}
	};

	public void runStage()
	{
		if (t.config.autosave)
		{
			(new Thread(new Runnable() // We don't want to save on the runStage thread, that might cause a noticeable delay. Note that if saving takes too long, we may get a conflict between several saves occurring at once. Consider naming the save thread.
			{
				@Override
				public void run()
				{
					t.saveGame();
				}
			})).start();
		}

		if (number==0)
			runStage();
		else
		{
			if (stage==0)
			{
				t.fadeout();
				if (t.user.weapon.type<=Cweapon.TYPE_BLUNT)
				{
					if (t.user.weapon.backup==null || t.user.weapon.backup.type<=Cweapon.TYPE_BLUNT)
					{
						t.user.dead=true;
						t.th=new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								t.say("Unprepared", "You prepare to engage the fearsome figure with your "+t.user.weapon+", but it only seems to laugh.\n\n\tDiscarding your weapon, you ready your "+(t.user.weapon.backup==null ? "body for hand-to-hand combat" : t.user.weapon.backup)+", and charge.");
							}
						});
						t.th.start();
						game.prepContinueButton(new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								t.user.weapon=(t.user.weapon.backup==null ? new Cweapon() : t.user.weapon.backup);
								t.th=new Thread(new Runnable()
								{
									@Override
									public void run()
									{
										runStage();
									}
								});
								t.th.start();
							}
						});
						return;
					}
					else
					{
						t.th=new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								t.say("Always prepared", "You prepare to fight the spirit with your "+t.user.weapon+".\n\n\n\tIt seemingly finds your effort humorous, as it pauses its approach to laugh.\n\n\n\n\tThinking your "+t.user.weapon.backup+" more suited to the task, you discard the "+t.user.weapon+", throwing it to the side and away from your presence, and draw your "+t.user.weapon.backup+".\n\n\tThe shadow thinks this a more serious threat, as it stops its laughter and prepares itself to engage you.");
							}
						});
						t.th.start();
						game.prepContinueButton(new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								t.user.weapon=t.user.weapon.backup;
								t.th=new Thread(new Runnable()
								{
									@Override
									public void run()
									{
										runStage();
									}
								});
								t.th.start();
							}
						});
						return;
					}
				}
				else if (t.user.weapon.backup!=null && t.user.weapon.backup.type<=Cweapon.TYPE_BLUNT)
				{
					t.th=new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							t.say("In your rush to prepare to defend yourself against the strange and mysterious figure before you, you fail to notice that your "+t.user.weapon.backup+" has slipped out of its holster.");
						}
					});
					t.th.start();
					game.prepContinueButton(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							if (t.user.weapon.backup.backup!=null && t.user.weapon.backup.backup.type>Cweapon.TYPE_BLUNT)
								t.user.weapon.backup=t.user.weapon.backup.backup;
							else
								t.user.weapon.backup=null;
							t.th=new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									runStage();
								}
							});
							t.th.start();
						}
					});
					return;
				}
				t.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						LinearLayout l= Cgame.prepInput();
						game.doCommitSuicide=true;
						Button b=new Button(t);
						b.setText("Fight the "+(t.gen.nextBoolean() ? "shadow" : "apparition")+" with your primary weapon, a "+t.user.weapon);
						b.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								v.setOnClickListener(null);
								t.th.interrupt();
								t.th=new Thread(new Runnable()
								{
									@Override
									public void run()
									{
										stage=1;
										input=0;
										runStage();
									}
								});
								t.th.start();
							}
						});
						l.addView(b);
						t.user.weapon.addSwapperTo(l, new Runnable()
						{
							@Override
							public void run()
							{
								runStage();
							}
						});
						b=new Button(t);
						b.setText((t.gen.nextBoolean() ? "Run away" : "Flee")+" from the "+(t.gen.nextBoolean() ? "ghostly" : "shadowy")+" vision before you");
						b.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								v.setOnClickListener(null);

								t.th.interrupt();
								t.th=new Thread(new Runnable()
								{
									@Override
									public void run()
									{
										stage=1;
										input=1;
										runStage();
									}
								});
								t.th.start();
							}
						});
						l.addView(b);
						if (t.config.triggerEgg(t.gen.nextInt(10)+1,11))
						{
							b=new Button(t);
							b.setText("Make friends with the creature");
							b.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									v.setOnClickListener(null);

									t.th.interrupt();
									t.th=new Thread(new Runnable()
									{
										@Override
										public void run()
										{
											stage=1;
											input=2;
											runStage();
										}
									});
									t.th.start();
								}
							});
							l.addView(b);
						}
					}
				});
			}
			else
			{
				t.displayHandler.removeMessages(1);
				if (Thread.interrupted() || game.onMove(t.gen.nextInt(85)+1))
					return;
				switch (input)
				{
				case 0:
					Runnable r;
					switch (t.user.weapon.type)
					{
					case Cweapon.TYPE_HAND_TO_HAND:
						t.user.dead=true;
						r=new Runnable() {
							@Override
							public void run() {
								t.say("Fistfight", t.config.triggerEgg(.2) ? "Why did you think that was a good idea?" : "You run up to the "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and punch it.\n\n\tBewildered, it seems to stare at you for a moment before laughing hysterically and unmaking you.");
							}
						};
						break;
					case Cweapon.TYPE_SHARP:
						t.determineUserDeath(t.user.weapon.getRelativeStrength(Cweapon.CLOSE_RANGE|Cweapon.CLOSE_RANGE_ONLY|Cweapon.LIGHT)*.5);
						r=new Runnable()
						{
							@Override
							public void run()
							{
								if (t.user.dead)
									t.say("Swordfight", "You swing at the "+(t.gen.nextBoolean() ? "shadow" : "vision")+", and miss.\n\n\tBefore you can ready yourself, the shadow consumes you, and you are no more.");
								else
								{
									String word;
									switch (t.gen.nextInt(3))
									{
									case 0:
										word="even ";
										break;
									case 1:
										word="yet ";
										break;
									case 2:
										word="";
										break;
									default:
										word="considerably ";
									}
									t.say("Close call", "You swing your sword, and connect.\n\n\n\tThe moment your sword touches the "+(t.gen.nextBoolean() ? "shadow" : "shape")+", the creature dissolves into a swirl of smoke, and your sword continues as if it wasn\'t there.\n\n\n\n\n\tYou shiver, questioning reality, and your sanity, "+word+"more than you already were.\n\n\n\n\n\n\n\tTrying to walk it off, you decide to continue along the highway.");
								}
							}
						};
						break;
					case Cweapon.TYPE_ARCHERY:
						t.determineUserDeath(t.user.weapon.getRelativeStrength(Cweapon.ACCURATE|Cweapon.CLOSE_RANGE), 3);
						r=new Runnable()
						{
							@Override
							public void run()
							{
								if (t.user.dead)
									t.say("Bowman", "You ready your "+t.user.weapon+", and aim it at the shadow.\n\tJust as you loose the arrow, almost as if it were inside your mind, the shadow dodges, and lunges at you.\n\n\tThe last thing you hear is your "+t.user.gold+" spilling onto the highway.");
								else
									t.say("Deadeye", "You bring your "+t.user.weapon+" up to bear, and fire it at the shadow.\n\n\n\tThe projectile soars to the shadow, hitting it.\n\n\tThe moment it touches the vision, the shadow disappears into a swirl of smoke, and the arrow falls straight to the ground, as if it had hit an invisible wall.\n\n\n\n\tBy the time you reach it, it\'s gone.\n\n\n\n\n\tYou shiver.\n\n\tYou wonder how sane you are...");
							}
						};
						break;
					case Cweapon.TYPE_MODERN:
						t.determineUserDeath(t.user.weapon.getRelativeStrength(Cweapon.ACCURATE|Cweapon.CLOSE_RANGE|Cweapon.HIGH_POWER_ROUNDS|Cweapon.QUICK_RELOAD), 4);
						r=new Runnable()
						{
							@Override
							public void run()
							{
								if (t.user.dead)
								{
									if (t.config.litEggs && t.config.triggerEgg(6))
										t.say("Alas, poor Yorick...","...I knew that one once.\n\n\tThey also called that adventurer \""+t.user+"\".\n\n\n\tThen, one day, a shadow appeared along the Highway to Hell, and the "+t.user.weapon+" the poor fool was carrying jammed, and no more was poor Yorick.");
									else
										t.say("A shot and a miss", "You take aim, your sights leveled at the "+(t.gen.nextBoolean() ? "shadow" : "figure")+".\n\n\tJust as you pull the trigger, the pits rumble with fiery power, shaking the ground, making you miss.\n\n\n\tOne day, years later, when the pits will have cooled, a child will come across the spot where the shadow consumed you, and all "+(t.gen.nextBoolean() ? "he" : "she")+" will find of you is your "+t.user.weapon+".");
								}
								else
									t.say("Sharpshooter","You level your "+t.user.weapon+" directly at the shadow, and fire.\n\n\tThe round hits it, and the shadow disappears into a swirl of smoke.\n\n\tThe fired round clatters straight to the ground, as the smoke dissipates.\n\n\n\tYou walk over to it, but you can\'t find the projectile.\n\n\tYou can\'t quite shake the feeling that what you know isn\'t real.");
							}
						};
						break;
					case Cweapon.TYPE_NUCLEAR:
						t.user.dead=true;
						r=new Runnable()
						{
							@Override
							public void run()
							{
								t.say("Doomsday", "You get out your "+t.user.weapon+", arm it, set the timer to one second, and start it.\n\n\tFrom familiarity, the shopkeepers gaze at the mushroom cloud from where you once stood, as the shadow of a massive beast looms behind them.");
							}
						};
						break;
					case Cweapon.TYPE_FUTURE:
						t.user.dead=false;
						r=new Runnable()
						{
							@Override
							public void run()
							{
								if (t.user.weapon.characteristicSet(Cweapon.CLOSE_RANGE_ONLY))
									t.say("Jedi","You meet the "+(t.gen.nextBoolean() ? "shadow" : "vision")+", and swing your lightsaber.\n\n\tThe shadow disappears into smoke at the edges of your vision as the "+t.user.weapon+" slices through it.\n\n\tYou continue, not particularly caring if it was real.");
								else
									t.say(t.capitalize(t.user.weapon.toString())+" über alles", "You level your "+t.user.weapon+" at the "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and pull the trigger.\n\n\tBy the time the splash from the impact of the shot clears, the shadow is gone, but strangely there\'s no ash.\n\n\tYou ignore it, not caring about your own sanity.");
							}
						};
						break;
					default:
						t.logError("Invalid weapon code: "+t.user.weapon.type);
						t.user.weapon.type=Cweapon.TYPE_USED_FOR_CONVENIENCE;
						r=new Runnable()
						{
							@Override
							public void run()
							{
								t.say("Unknown","Your weapon "+(t.user.weapon.name==null || "".equals(t.user.weapon.name) ? "" : ", a "+t.user.weapon.name)+", is unknown to the universe, so...");
							}
						};
					}
					t.th=new Thread(r);
					t.th.start();
					game.prepContinueButton(new View.OnClickListener()
					{
						public void onClick(View v)
						{
							t.th=new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									if (t.user.weapon.type==Cweapon.TYPE_USED_FOR_CONVENIENCE)
										t.user.commitSuicide();
									else if (t.user.dead)
									{
										t.game.stage=-1;
										t.game.runStage();
									}
									else {
										number-=1;
										stage=0;
										runStage();
									}
								}
							});
							t.th.start();
						}
					});
					break;
				case 1:
					t.user.dead=true;
					t.th=new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							t.say("Deserter", "You try to run, but the shadow disappears, and reappears in front of you.");
						}
					});
					t.th.start();
					game.prepContinueButton();
					break;
				case 2:
					t.user.dead=true;
					t.th=new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							if (t.config.triggerEgg(0.5))
							{
								String[] weapons=new String[t.gen.nextInt(3)+3];
								for (int i=0; i<weapons.length; ++i)
								{
									switch (t.gen.nextInt(8))
									{
									case 0:
										weapons[i]="sword";
										break;
									case 1:
										weapons[i]="crossbow";
										break;
									case 2:
										weapons[i]="pistol";
										break;
									case 3:
										weapons[i]="rifle";
										break;
									case 4:
										weapons[i]="machine gun";
										break;
									case 5:
										weapons[i]=((t.user.weapon.type!=Cweapon.TYPE_NUCLEAR && t.user.weapon.type!=Cweapon.TYPE_FUTURE) ? t.user.weapon.name : "strangely frightening weapon");
										break;
									case 6:
										weapons[i]="M16";
										break;
									case 7:
										weapons[i]="P90";
										break;
									default:
										weapons[i]="sniper rifle";
									}
								}
								String message="";
								for (int i=1; i<weapons.length; ++i)
								{
									for (int n=0, m=(t.gen.nextInt(4)+2); n<m; ++n)
										message+="\n";
									message+="\tAfter a while of wandering along the Highway, yet another adventurer appears, this time with a "+weapons[i]+"!\n\n\tBegrudgingly, you kill "+(t.gen.nextBoolean() ? "him" : "her")+", and continue walking along the Highway, caring even less about the humans.";
								}
								t.say("Assimilation", "You try to make friends with the "+(t.gen.nextBoolean() ? "shadow" : "vision")+"...\n\n\tMiraculously, it accepts you!\n\n\n\tIt escorts you to the pits, where you are brought to the shadow headquarters.\n\n\tThe "+(t.gen.nextBoolean() ? "shadows" : "mystical beings")+" perform some sort of ritual, and you watch as your body slips away from you as you become one of them.\n\n\n\tYou move out to the Highway, and immediately, you\'re attacked by an adventurer with a "+weapons[0]+"!\n\n\tYou try to befriend "+(t.gen.nextBoolean() ? "him" : "her")+", but you\'re forced to retaliate.\n\n\tYou can\'t help but lose some faith in humans."+message+"\n\n\tSuddenly, you find an adventurer!\n\t"+(t.gen.nextBoolean() ? "He" : "She")+" tries to run, but you can\'t help but kill any adventurer in your path.\n\n\n\tBarely afterward, another comes along, and tries to make friends.\n\n\tPerhaps if you hadn\'t spent so long on the Highway, you\'d accept, but now...\n\tYou kill "+(t.gen.nextBoolean() ? "him" : "her")+".\n\n\n\tThree days later, an adventurer appears with a "+(t.gen.nextBoolean() ? "raygun" : "lightsaber")+", and ends you.\n\n\n\tGandalf nods in approval, as you disappear into a swirl of smoke.");
							}
							else
							{
								String address;
								switch (t.gen.nextInt(3))
								{
								case 0:
									address="shadow";
									break;
								case 1:
									address="vision";
									break;
								case 2:
									address="other";
									break;
								default:
									address="creature";
								}
								t.say("Friendship", "Friends are not so easily earned.\n\n\tThe "+address+" is not interested.");
							}
						}
					});
					t.th.start();
					if (t.config.easterEggs)
						t.delay(50); // If the easter egg is triggered, processing might take some time. We delay the UI work just to be sure.
					game.prepContinueButton();
				}
			}
		}
	}
}
