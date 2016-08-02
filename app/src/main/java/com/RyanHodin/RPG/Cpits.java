package com.RyanHodin.RPG;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.Serializable;

/**
 * Created by Ryan on 8/2/2016.
 */
class Cpits implements Serializable, Parcelable {
	public Cgame game;
	public static MainActivity t;

	private static final long serialVersionUID=0L; // Update when necessary

	byte number;
	byte stage;
	byte input;
	byte input2;
	boolean hasAssaultedHQ;

	public Cpits()
	{
		game=t.game;

		number=(byte)(t.gen.nextInt(6)+3);
		stage=0;
		input=0;
		input2=0;
		hasAssaultedHQ=false;
	}

	private Cpits(Parcel in)
	{
		number=in.readByte();
		stage=in.readByte();
		input=in.readByte();
		input2=in.readByte();
		hasAssaultedHQ=in.readByte()==1; // Saved as a byte. Correct this if we ever have multiple bools
	}

	@Override
	public int describeContents()
	{
		return 10;
	}

	@Override
	public void writeToParcel(Parcel out, int unused)
	{
		out.writeByte(number);
		out.writeByte(stage);
		out.writeByte(input);
		out.writeByte(input2);
		out.writeByte((byte)(hasAssaultedHQ ? 1 : 0)); // Saved as a byte. Correct this if we ever have multiple bools
	}

	public static final Parcelable.Creator<Cpits> CREATOR=new Parcelable.Creator<Cpits> ()
	{
		@Override
		public Cpits createFromParcel (Parcel in)
		{
			return new Cpits(in);
		}

		@Override
		public Cpits[] newArray (int n)
		{
			return new Cpits[n];
		}
	};

	public void saveTo(SharedPreferences.Editor edit)
	{
		edit.putInt("pitsNumber", number);
		edit.putInt("pitsStage", stage);
		edit.putInt("pitsInput", input);
		edit.putInt("pitsInput2", input2);
		edit.putBoolean("pitsHasAssaultedHQ", hasAssaultedHQ);
		if (Build.VERSION.SDK_INT>=9)
			edit.apply();
		else
			edit.commit();
	}

	public void loadFrom(SharedPreferences sp)
	{
		number=(byte)sp.getInt("pitsNumber", number);
		stage=(byte)sp.getInt("pitsStage", stage);
		input=(byte)sp.getInt("pitsInput", input);
		input2=(byte)sp.getInt("pitsInput2", input2);
		hasAssaultedHQ=sp.getBoolean("pitsHasAssaultedHQ", hasAssaultedHQ);
	}

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
			game.runStage();
		else
		{
			switch(stage)
			{
			case 0: // Stage 0: User input on direction
				t.fadeout();
				t.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						LinearLayout l= Cgame.prepInput();
						Button b=new Button(t);
						if (!hasAssaultedHQ)
						{
							b.setText("Mount an assault on the House of Shadows");
							b.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									v.setOnClickListener(null);

									t.th=new Thread(new Runnable()
									{
										@Override
										public void run()
										{
											stage=1;
											input=0;
											hasAssaultedHQ=true;
											runStage();
										}
									});
									t.th.start();
								}
							});
							l.addView(b);
							b=new Button(t);
						}
						if (t.gen.nextBoolean())
						{
							b.setText("Explore to your left");
							b.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									v.setOnClickListener(null);

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
							b=new Button(t);

						}
						if (t.gen.nextBoolean())
						{
							b.setText("Continue straight ahead");
							b.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									v.setOnClickListener(null);

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
							b=new Button(t);
						}
						if (t.gen.nextBoolean())
						{
							b.setText("Explore to your right");
							b.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									v.setOnClickListener(null);

									t.th=new Thread(new Runnable()
									{
										@Override
										public void run()
										{
											stage=1;
											input=3;
											runStage();
										}
									});
									t.th.start();
								}
							});
							l.addView(b);
							b=new Button(t);
						}
						b.setText("Make your way to the depression at the center of the pits");
						b.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								v.setOnClickListener(null);

								t.th=new Thread(new Runnable()
								{
									@Override
									public void run()
									{
										number=0;
										runStage();
									}
								});
								t.th.start();
							}
						});
						l.addView(b);
					}
				});
				break;
			case 1: // Stage 1: Describe findings
				if (game.onMove(111))
					return;
				switch (input)
				{
				case 0: // Input 0: Assault HoS
					t.th=new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							t.say(t.gen.nextBoolean() ? "Assault" : "Infiltration", "You charge the House of Shadows.\n\n\tAs you approach, you find more and more shadows.\n\n\tYou suspect that they know you\'re coming.\n\n\tYou try to sneak in, planning to use stealth to kill each shadow alone.\n\n\tUnfortunately, you\'re caught trying to enter by a sentry group of three shadows.\n\n\n\tInside the House, you see an object.\n\n\tUpon closer examination, you can read the label: \"US Army M183 Demolition Charge: 20 lbs.\"\n\n\tFrom what little you know about explosives, you think that that charge would be large enough to turn the House of Shadows into one large fragmentation grenade.\n\n\n\n\n\tThe "+(t.gen.nextBoolean() ? "shadows" : "visions")+" ready for battle.");
						}
					});
					t.th.start();
					game.prepContinueButton(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							v.setOnClickListener(null);

							t.th=new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									stage=2;
									runStage();
								}
							});
							t.th.start();
						}
					});
					break;
				case 1: // Input 1: Explore left
					if (t.gen.nextBoolean())
					{
						t.th=new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								t.say("Explorer", "You head to an abandoned shop in ruins...\n\n\tBut a shadow catches you along the way!");
							}
						});
						t.th.start();
						game.prepContinueButton(new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								v.setOnClickListener(null);

								t.th=new Thread(new Runnable()
								{
									@Override
									public void run()
									{
										stage=2;
										runStage();
									}
								});
								t.th.start();
							}
						});
					}
					else {
						stage=100;
						runStage();
					}
					break;
				case 2: // Input 2: Continue straight
					if (t.gen.nextBoolean())
					{
						t.th=new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								t.say("Conquest", "You march straight ahead, towards the destroyed remains of a household.\n\n\n\tA "+(t.gen.nextBoolean() ? "shadow" : "figure")+" blocks your path, guarding the ruined house as if it had been "+(t.gen.nextBoolean() ? "his" : "her")+" home.");
							}
						});
						t.th.start();
						game.prepContinueButton(new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								v.setOnClickListener(null);

								t.th=new Thread(new Runnable()
								{
									@Override
									public void run()
									{
										stage=2;
										runStage();
									}
								});
								t.th.start();
							}
						});
					}
					else {
						stage=100;
						runStage();
					}
					break;
				case 3: // Input 3: Explore right
					if (t.gen.nextBoolean())
					{
						t.th=new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								t.say("Pits: The Final Frontier", "You travel towards what appears to be a bank.\n\n\tJust as you approach, a shadow opens the door.");
							}
						});
						t.th.start();
						game.prepContinueButton(new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								v.setOnClickListener(null);

								t.th=new Thread(new Runnable()
								{
									@Override
									public void run()
									{
										stage=2;
										runStage();
									}
								});
								t.th.start();
							}
						});
					}
					else {
						stage=100;
						runStage();
					}
					break;
				}
				break;
			case 2: // Stage 2: Combat
				Runnable r;
				switch (input)
				{
				case 0: // Input 0: Assault HoS
					r=new Runnable()
					{
						@Override
						public void run()
						{
							LinearLayout l= Cgame.prepInput();
							game.doCommitSuicide=true;
							Button b=new Button(t);
							b.setText("Fight the trio of shadows with your primary "+t.user.weapon);
							b.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									v.setOnClickListener(null);

									t.th=new Thread(new Runnable()
									{
										@Override
										public void run()
										{
											stage=3;
											input2=0;
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
							b.setText("Make a run for the demolition charge");
							b.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									v.setOnClickListener(null);

									t.th=new Thread(new Runnable()
									{
										@Override
										public void run()
										{
											stage=3;
											input2=1;
											runStage();
										}
									});
								}
							});
							l.addView(b);
							b=new Button(t);
							b.setText(t.gen.nextBoolean() ? "Run away!" : "Flee");
							b.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									v.setOnClickListener(null);

									t.th=new Thread(new Runnable()
									{
										@Override
										public void run()
										{
											stage=3;
											input2=2;
											runStage();
										}
									});
									t.th.start();
								}
							});
							l.addView(b);
						}
					};
					break;
				case 1: // Input 1: Explore left
				case 2: // Input 2: Continue straight
				case 3: // Input 3: Explore right - All these are duplicate cases.
					r=new Runnable()
					{
						@Override
						public void run()
						{
							LinearLayout l= Cgame.prepInput();
							game.doCommitSuicide=true;
							Button b=new Button(t);
							String word;
							switch (t.gen.nextInt(4))
							{
							case 0:
								word="shadow";
								break;
							case 1:
								word="figure";
								break;
							case 2:
								word="vision";
								break;
							case 3:
								word="monster";
								break;
							default:
								word="creature";
							}
							b.setText("Fight the "+word+" with your primary "+t.user.weapon);
							b.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									v.setOnClickListener(null);

									t.th=new Thread(new Runnable()
									{
										@Override
										public void run()
										{
											stage=3;
											input2=0;
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
							b.setText(t.gen.nextBoolean() ? "Run away" : "Flee");
							b.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									v.setOnClickListener(null);

									t.th=new Thread(new Runnable()
									{
										@Override
										public void run()
										{
											stage=3;
											input=1;
											runStage();
										}
									});
									t.th.start();
								}
							});
							l.addView(b);
						}
					};
					break;
				default:
					r=new Runnable()
					{
						@Override
						public void run()
						{
							LinearLayout l= Cgame.prepInput("Your situation is unknown");
							game.doCommitSuicide=true;
							Button b=new Button(t);
							b.setText("Try this again");
							b.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									v.setOnClickListener(null);

									t.th=new Thread(new Runnable()
									{
										@Override
										public void run()
										{
											stage=0;
											input=0;
											input2=0;
											hasAssaultedHQ=true;
											runStage();
										}
									});
									t.th.start();
								}
							});
							l.addView(b);
							b=new Button(t);
							b.setText("Be done with it");
							b.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									v.setOnClickListener(null);

									t.th=new Thread(new Runnable()
									{
										@Override
										public void run()
										{
											t.user.commitSuicide();
										}
									});
									t.th.start();
								}
							});
							l.addView(b);
						}
					};
				}
				t.fadeout();
				t.runOnUiThread(r);
				break;
			case 3: // Stage 3: Results of combat
				switch (input)
				{
				case 0:
					switch (input2)
					{
					case 0:
						if (t.user.weapon.type==Cweapon.TYPE_ARCHERY && .5>(t.config.difficultyMult*.75*t.user.weapon.getAbsoluteStrength()))
						{
							game.archeryMinigame((int)Math.ceil(t.gen.nextInt(40)+(t.config.difficultyMult*10)+5), ((20*(1-t.config.difficultyMult))+5)/5);
							t.th=new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									if (t.user.dead)
									{
										String title;
										switch (t.gen.nextInt(3))
										{
										case 0:
											title="Archer";
											break;
										case 1:
											title="Bowman";
											break;
										case 2:
											title="Outnumbered";
											break;
										default:
											title="Outdone by the dark";
										}
										switch (t.gen.nextInt(3))
										{
										case 0:
											t.say(title, "You turn to face the "+(t.gen.nextBoolean() ? "figures" : "shadows")+", but you\'re immediately killed by the leading shadow.");
											break;
										case 1:
											t.say(title, "You draw your "+t.user.weapon+", turning against the "+(t.gen.nextBoolean() ? "shadows" : "figures")+", shooting the leading one, and turning it to swirling smoke, but the others reach you before you can ready another shot.");
											break;
										case 2:
											t.say(title, "You quickly turn and fire an arrow at the lead "+(t.gen.nextBoolean() ? "shadow" : "vision")+", turning it into a cloud of smoke. Quickly, you dodge to the side of the remaining two attacks.\n\n\tYou ready and fire a second shot, eliminating another opponent, but the last defeats you.");
											break;
										default:
											Log.wtf("Cgame at House of Shadows", "Invalid return from Random.nextInt()");
											t.say(title, "The enemies that stand before you overpower you.");
										}
									}
									else
										t.say("Veteran", "Despite the odds against you, your archery skills prevail in a tale that will be sung throughout time: You shoot the leading "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and dodge the attack of the next without pausing to watch the smoke swirl away.\n\n\tIn the blink of an eye, the second "+(t.gen.nextBoolean() ? "shadow" : "vision")+" recuperates, but you ready your "+t.user.weapon+" even faster, and shoot it too.\n\n\tThe third is upon you quickly, and you defend yourself by blocking with your "+t.user.weapon+", which gets tossed aside in the process.\n\n\n\tBefore the final "+(t.gen.nextBoolean() ? "shadow" : "creature")+" can "+(t.gen.nextBoolean() ? "attack" : "assault")+" you, you draw "+("crossbow".equalsIgnoreCase(t.user.weapon.name) ? "a bolt" : "an arrow")+" from your stockpile, and stab it.\n\n\tIt vanishes in a swirling cloud of smoke.\n\n\tYou note your spent ammunition has disappeared entirely.\n\n\n\tBrushing this fact aside, you turn your attention to the House of Shadows.");
								}
							});
							t.th.start();
							if (t.user.dead)
								game.prepContinueButton();
							else
								game.prepContinueButton(new View.OnClickListener()
								{
									@Override
									public void onClick(View v)
									{
										t.th=new Thread(new Runnable()
										{
											@Override
											public void run()
											{
												stage=4;
												runStage();
											}
										});
										t.th.start();
									}
								});
						}
						else
						{
							boolean undead=false;
							switch (t.user.weapon.type)
							{
							case Cweapon.TYPE_SHARP:
								t.determineUserDeath((t.user.weapon.getUnneededFlags(Cweapon.CLOSE_RANGE_ONLY, 5)+(1-t.user.weapon.getNeededFlags(Cweapon.ACCURATE|Cweapon.CLOSE_RANGE|Cweapon.LIGHT|Cweapon.QUICK_RELOAD, 2)))*.99);
								r=new Runnable()
								{
									@Override
									public void run()
									{
										if (t.user.dead)
										{
											switch (t.gen.nextInt(3))
											{
											case 0:
												t.say("Outnumbered and outdone", "You face the creatures with your "+t.user.weapon+", fending them away, but one gets past you long before you can strike any.");
												break;
											case 1:
												t.say("Death "+(t.gen.nextBoolean() ? "Unbounded" : "knows no bounds"), "You swing your sword at the leading "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and, miraculously, connect.\n\n\tThe lead shadow disappears in a swirl of smoke, and you turn to face the next, but you\'re caught from behind by the last.");
												break;
											case 2:
												t.say("The Darkness of Death", "You move quickly, as quickly as you ever have in your life.\n\n\tYou turn the leading shadow into a swirl of smoke, then the second.\n\nThe third, however, evades your swing, not letting it touch, and ends you.");
												break;
											default:
												Log.wtf("Cgame at House of Shadows", "Invalid result from Random.nextInt()");
												t.say("Nonexistence", "You corrupt the state of the Universe and cease to exist.");
											}
										}
										else
											t.say(t.gen.nextBoolean() ? "Swordsman" : "Sword master", "You swing your sword as fast as humanly possible, turning one, then two, "+(t.gen.nextBoolean() ? "shadows" : "figures")+" into swirling smoke.\n\n\tThe last shadow charges toward you, but your sword nicks it, and it disappears.\n\n\tA booming voice, almost like Gandalf\'s, echoes from the clouds:\n\n\t\tWhy do you still carry that puny "+t.user.weapon+"??\n\t\tFind a new weapon.\n\t\tNow.\n\n\n\n\n\tYou turn your attention away from the voice, and to the House of Shadows.");
									}
								};
								break;
							case Cweapon.TYPE_ARCHERY:
								t.determineUserDeath(.75*t.user.weapon.getAbsoluteStrength());
								r=new Runnable()
								{
									@Override
									public void run()
									{
										if (t.user.dead)
										{
											String title;
											switch (t.gen.nextInt(4))
											{
											case 0:
												title="Archer";
												break;
											case 1:
												title="Bowman";
												break;
											case 2:
												title="Outnumbered";
												break;
											default:
												title="Outdone by the dark";
											}
											switch (t.gen.nextInt(3))
											{
											case 0:
												t.say(title, "You turn to face the shadows, but they easily defeat you before you get off a single shot.");
												break;
											case 1:
												t.say(title, "You spin to face your enemy, and fire a single "+("crossbow".equalsIgnoreCase(t.user.weapon.name) ? "bolt" : "arrow")+" at the leading "+(t.gen.nextBoolean() ? "shadow" : "apparition")+".\n\n\tYour aim "+(t.gen.nextBoolean() ? "is true" : (t.gen.nextBoolean() ? "is that of a master" : "is masterful"))+", turning the leader of the trio into a swirl of smoke.\n\n\tHowever, before you can ready yourself again, the other two eliminate you.");
												break;
											case 2:
												t.say(title, "You load one "+("crossbow".equalsIgnoreCase(t.user.weapon.name) ? "bolt" : "arrow")+", and thoughtlessly fire it at the lead "+(t.gen.nextBoolean() ? "shadow" : "figure")+".\n\n\tYou\'ve practiced your archery considerably, and the shot flies directly into its target.\n\n\tYou ignore the sight as your opponent disappears into a swirling cloud of shadowy smoke, and dodge the advance of your second adversary, which you shoot shortly afterwards.\n\n\n\tAlas, you lost track of the third shadow, which comes from behind you, and kills you.");
												break;
											default:
												Log.wtf("Cgame at the House of Shadows", "Invalid output from Random.nextInt()");
											}
										}
										else
											t.say((t.config.GoTEggs && t.config.triggerEgg(.8)) ? "Lightbringer" : "Defeater of Shadows", "You turn, snappily aiming your "+t.user.weapon+" directly at the leading "+(t.gen.nextBoolean() ? "shadow" : "apparition")+", and shooting it.\n\n\tPaying it no attention as it disappears into swirling smoke.\n\n\tInstead, as if by instinct, you roll towards the House of Shadows."+(t.gen.nextBoolean() ? "" : "\n\n\tYou quickly recover from the pain of your miscalculation after you slam into the outer wall.")+"\n\n\tWithout taking the time to stand, you adjust your aim, directly at the second enemy.\n\n\tQuickly, you release your shot, sending "+("crossbow".equalsIgnoreCase(t.user.weapon.name) ? "a bolt" : "an arrow")+" flying into the second shape, then clattering to the ground as your opponent swirls away.\n\n\n\tThe third charges, but you pull one "+("crossbow".equalsIgnoreCase(t.user.weapon.name) ? "bolt" : "arrow")+" from your reserves, and drive it through your final adversary, noting the lack of resistance as it goes through.\n\n\tYou watch the remains of your opponent swirl away, and note your two fired shots have vanished.");
									}
								};
								break;
							case Cweapon.TYPE_MODERN:
								final targetingMinigame.output res=game.aimMinigame();
								undead=(res.type==targetingMinigame.output.BULLSEYE);
								final String title=res.toString();
								r=new Runnable()
								{
									@Override
									public void run()
									{
										if (t.user.weapon.characteristicSet(Cweapon.AUTOMATIC))
										{
											t.user.dead=(res.distance/(.8*targetingMinigame.output.MAX_DISTANCE))<((.5*(t.gen.nextDouble()+t.config.difficultyMult))+.5);
											if (t.user.dead)
											{
												switch (res.type)
												{
												case targetingMinigame.output.MISSED:
													t.say(title, "You fire a burst of bullets towards the shadows, but your aim is off, and the "+(t.gen.nextBoolean() ? "shadows" : "figures")+" end you.");
													break;
												case targetingMinigame.output.GRAZED:
													t.say(title, "You turn at the shadows, and fire a burst of automatic fire. One shot hits the nearest "+(t.gen.nextBoolean() ? "shadow" : "vision")+", turning it to a swirl of smoke, but the remaining two intercept you.");
													break;
												case targetingMinigame.output.POOR:
													t.say(title, "You fire at the shadows, grazing one, turning it to swirling smoke, and barely hitting another.\n\n\tThe last consumes you as the second disappears.");
													break;
												case targetingMinigame.output.HIT:
													t.say(title, "You fire at the shadows, hitting one with a solid shot, and "+(t.gen.nextBoolean() ? "turning" : "converting")+" it into a swirling cloud.\n\n\tAnother bullet strikes the second, eliminating it as well, but "+(t.user.weapon.characteristicSet(Cweapon.HIGH_RECOIL) ? "the recoil of your "+t.user.weapon+" throws off your aim." : "the last shadow moves out of your sights too quickly."));
													break;
												case targetingMinigame.output.GOOD:
													t.say(title, "You hit two shadows with near perfect shots.\n\n\tThe shadows disappear into swirling smoke, as the third approaches you.\n\n\n\n\n\tYou quickly twist, aiming directly at the last shadow.\n\n\n\n\n\n\tYou pull the trigger, just as it reaches you."+(t.config.triggerEgg(.5) ? "\n\n\n\tThe shadow of a dragon looms over the pits, the rush of air stirring everything around, as you both become swirling smoke, and "+t.user.gold+", along with your "+t.user.weapon+", clatter to the ground." : ""));
													break;
												case targetingMinigame.output.CRITICAL:
													t.say(title, "You hit the first two shadows with perfect shots, but find that the third seems to have disappeared.\n\n\tYou twist and turn, searching for it, "+(t.config.triggerEgg(.75) ? "and feel a thud, and a weight.\n\n\tYou fall, watching the shadow run, and attempt to give chase.\n\n\tSoon, it leaves your sight. Contented, you enter the House of Shadows, grabbing the detonator, assuming that the charge is at it was.\n\n\n\n\tYou walk clear of the House, wondering why you feel as if there is a huge weight on your back.\n\n\n\tYou realize that the shadow planted the charge on your back a moment too late, as you trigger the explosive." : "and soon finding it, right behind you.\n\n\tYou dodge its lunge, dropping your "+t.user.weapon+" in your haste to "+(t.gen.nextBoolean() ? "escape" : "survive")+", and, just as you think you\'ve escaped, you trip, rolling down a hill, coming to a stop in a ditch full of a hundred shadows."));
													break;
												case targetingMinigame.output.BULLSEYE:
													t.user.dead=false;
													t.say(title+", despite impossibility", "You fire three utterly perfect shots, cleanly converting two shadows into swirling smoke, and then you fire a hail of bullets into the third, ending it as well.\n\n\tDespite the incredibly difficult odds against you, you have prevailed.");
													break;
												default:
													t.say("Gunshot", "You are easily defeated by the shadows.");
												}
											}
											else
											{
												switch (res.type)
												{
												case targetingMinigame.output.MISSED:
													t.say(title, "You miss every shot in your magazine, yet manage to kill all three shadows by using your "+t.user.weapon+" as a club.\n\n\n\tFrom the clouds, a booming voice, almost like Gandalf\'s, echoes through the pits:\n\n\t\tGet better aim!!\n\t\tSeriously, go to target practice or something...\n\t\tThat was ridiculous.\n\n\n\n\n\n\tYou shake it off and turn your attention to the House of Shadows.");
													break;
												case targetingMinigame.output.GRAZED:
													t.say(title, "You fire at the enemies, full automatic fire, and yet the shadows keep managing to dodge your fire.\n\n\t"+(t.user.weapon.characteristicSet(Cweapon.HIGH_RECOIL) ? "You suspect that its because your "+t.user.weapon+" has too much recoil.\n\n\t" : "")+"Just as you begin to worry about the need to reload, one bullet grazes the leading shadow.\n\tSomehow, to your surprise, it disappears, turning into swirling smoke.\n\n\n\n\tThe remaining "+(t.gen.nextBoolean() ? "shadows" : "figures")+" mirror your surprise: They pause in shock.\n\n\tYou take advantage of their surprise to fire "+(t.gen.nextBoolean() ? "a trio" : "a hail")+" of "+(t.gen.nextBoolean() ? "rounds" : "bullets")+" at the second "+(t.gen.nextBoolean() ? "figure" : "shadow")+".\n\n\tIt disappears into a swirling cloud of smoke, and you continue firing, now at the last enemy, and barely manage to graze it.\n\n\n\tIgnoring what is now just a cloud of smoke, you turn on the House of Shadows.");
													break;
												case targetingMinigame.output.POOR:
													t.say(title, "You level the sights of your "+t.user.weapon+" directly at the leading "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and fire a "+(t.gen.nextBoolean() ? "hail" : "number")+" of rounds into it.\n\n\n\tIt "+(t.gen.nextBoolean() ? "becomes" : "disappears into")+" a swirling cloud of smoke, as you turn your attention to the second enemy.\n\n\tIt approaches "+(t.gen.nextBoolean() ? "menacingly" : "threateningly")+", but you "+(t.gen.nextBoolean() ? "are able" : "manage")+" to level your sights at it, and you squeeze the trigger, watching the bullets barely hit the "+(t.gen.nextBoolean() ? "shadow" : "figure")+".\n\n\tLuckily, it seems that any hit will "+(t.gen.nextBoolean() ? "dissolve" : "destroy")+" the shadow, and you watch as it disappears into a swirling cloud of smoke.\n\n\n\n\n\tUnfortunately, you seem to have lost track of the third...\n\n\n\n\tYou look left...\n\n\t...and right...\n\n\t...left...\n\n\t...and there it is!\n\n\n\n\tThe last "+(t.gen.nextBoolean() ? "shadow" : "figure")+" "+(t.gen.nextBoolean() ? "shows itself" : "appears")+" immediately just in front of you!\n\n\n\n\n\n\tYou hastily fire your "+t.user.weapon+", and turn it to smoke, barely surviving the encounter.\n\n\tYou shake yourself off, and turn to face the House of Shadows.");
													break;
												case targetingMinigame.output.HIT:
													t.say(title, "You aim your "+t.user.weapon+" directly at the leading enemy, and "+(t.gen.nextBoolean() ? "squeeze" : "pull")+" the trigger.\n\n\tA "+(t.gen.nextBoolean() ? "burst" : "number")+" of rounds fly directly at it, and strike it, turning it into a swirling cloud of smoke.\n\n\tYou quickly turn, and aim at the second "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and manage to send a hail of "+(t.gen.nextBoolean() ? "rounds" : "fire")+" flying directly at it.\n\n\n\tYou pause for a split second to watch as the "+(t.gen.nextBoolean() ? "apparition" : "shadow")+" dissolves into a cloud of smoke.\n\n\tThe final shadow seems to have disappeared.\n\n\t"+(t.gen.nextBoolean() ? "You" : "Your "+t.user.weapon)+" feels light, "+(t.user.weapon.characteristicSet(Cweapon.QUICK_RELOAD) ? "so you quickly switch cartridges." : "but you decide it would take too long to reload it.")+"\n\n\n\n\tYou return your attention to where it probably should be, the rather deadly "+(t.gen.nextBoolean() ? "shadow" : "apparition")+" that could reappear and kill you at any moment.\n\tSuddenly, it reappears: Right in front of you!\n\n\n\n\tYou quickly pull the trigger, firing several shots into the "+(t.gen.nextBoolean() ? "figure" : "shadow")+" before you.\n\n\n\tYou turn to oppose the House of Shadows as it disappears into a swirling cloud of smoke.");
													break;
												case targetingMinigame.output.GOOD:
													t.say(title, "You aim, and fire your "+t.user.weapon+" directly at the "+(t.gen.nextBoolean() ? "nearest" : (t.gen.nextBoolean() ? "closest" : "nearest"))+" "+(t.gen.nextBoolean() ? "shadow" : "figure")+", sending a hail of bullets flying directly at its core.\n\n\n\tThe first round out of your "+(t.gen.nextBoolean() ? "weapon" : t.user.weapon)+" hits it, turning it into a swirling cloud of smoke.\n\n\n\tYou quickly shift your aim, leveling your sights at the next target, and "+(t.gen.nextBoolean() ? "squeeze" : "pull")+" the trigger, scoring a solid hit, and turning the shadow into a swirling cloud of smoke.\n\n\n\tThe third shadow comes around to face you, and dodges your first burst.\n\n\n\tYou quickly adjust your aim, and "+(t.gen.nextBoolean() ? "squeeze" : "pull")+" the trigger, scoring another hit and ending the shadow.\n\n\n\tYou turn to face the House of Shadows as it turns to swirling smoke.");
													break;
												case targetingMinigame.output.CRITICAL:
													t.say(title, "You aim at the leading "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and fire a hail of rounds from your "+t.user.weapon+", scoring an extremely solid shot and dissolving it.\n\n\n\tAs it disappears into smoke, you adjust your aim, aiming for the next "+(t.gen.nextBoolean() ? "shadow" : "apparition")+", and fire a trio of bullets into its core, turning it into a swirling cloud of smoke.\n\n\n\n\tThe last shadow attempts to disappear, to catch you from behind, but you fire, and hit it precisely in its center of mass.\n\n\n\tAs it dissolves into swirling smoke, you turn to face the House of Shadows.");
													break;
												case targetingMinigame.output.BULLSEYE:
													t.say(title, "You level your "+t.user.weapon+" at the "+(t.gen.nextBoolean() ? "leading" : "nearest")+" "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and squeeze the trigger, sending a hail of bullets flying directly towards it, striking it perfectly between the two glowing spots that pass for eyes.\n\n\tAs it disappears into swirling smoke, you let off the trigger, and adjust your aim, ready to eliminate the second one.\n\n\n\n\tJust as it begins to approach you, you fire again, and a trio of bullets score a perfect hit on your target.\n\n\n\tIt disappears into smoke, and you aim directly at the last target.\n\n\tIt attempts to flee, but you don\'t allow it the chance: You "+(t.gen.nextBoolean() ? "squeeze" : "pull")+" the trigger, sending a single round into the "+(t.gen.nextBoolean() ? "shadow" : "figure")+", hitting its forehead dead-center, and converting it into a swirling cloud of smoke.\n\n\n\n\tYou turn to face the House of Shadows.");
													break;
												default:
													t.say("A "+t.user.weapon+" to a shadowfight", "You manage to defeat the shadows.");
												}
											}
										}
										else
										{
											t.user.dead=(res.distance/(.6*targetingMinigame.output.MAX_DISTANCE))<((.5*(t.gen.nextDouble()+t.config.difficultyMult))+.5);
											if (t.user.dead)
											{
												switch (res.type)
												{
												case targetingMinigame.output.MISSED:
													t.say(title, "You aim at the leading "+(t.gen.nextBoolean() ? "figure" : "shadow")+", and fire.\n\n\tYou miss.\n\tBadly.\n\n\t"+(t.config.triggerEgg(.5) ? ("Years from now, after the Fall of the Shadows, and after another explorer, perhaps even one called "+t.user+", has liberated the Great Familiar City, a small "+(t.gen.nextBoolean() ? "boy, and his" : "girl, and her")+" "+(t.gen.nextBoolean() ? "mother" : (t.config.triggerEgg(.2) ? "Big Daddy" : "father"))+" will come across the spot where you faced the shadows, and they will take all they find, your "+t.user.weapon+", home as a souvenir.") : (t.user.weapon.characteristicSet(Cweapon.BOLT_FIRE) ? "Before you can cycle the action, they are upon you." : "They take the chance to consume you.")));
													break;
												case targetingMinigame.output.GRAZED:
													t.say(title, "You level your "+t.user.weapon+" at the lead "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and squeeze the trigger.\n\n\n\tA single round flies into the side of it, and it vanishes from the slightest impact.\n\n\tIt would appear that the slightest impact will eliminate a shadow.\n\n\n\n\tYou level your weapon at the second shadow, but you have just enough time to notice that the third has disappeared from your sight when it appears behind you, and ends you.");
													break;
												case targetingMinigame.output.POOR:
													t.say(title, "You place the sights of your "+t.user.weapon+" at the leading "+(t.gen.nextBoolean() ? "apparition" : "shadow")+", and pull the trigger.\n\n\n\tAlmost as if by instinct, you find that you're adjusting your aim to fire at the second "+(t.gen.nextBoolean() ? "shadow" : "figure")+".\n\n\n\tAlas, your instinct is not as skilled as your hand, and your shot flies wide.\n\n\n\n\tYou try to readjust your aim, but the "+(t.gen.nextBoolean() ? "shadows" : "figures")+" are too fast.");
													break;
												case targetingMinigame.output.HIT:
													t.say(title, "You aim towards the leading shadow, and pull the trigger, sending a single round flying into it.\n\n\tThe shot scores a solid hit, and turns the "+(t.gen.nextBoolean() ? "shadow" : "figure")+" into a swirling cloud of smoke.\n\n\tWithout pausing to watch it, you adjust your aim, leveling your "+t.user.weapon+" at your second opponent.\n\n\tYou fire a shot, and barely nick it, turning it into smoke.\n\n\tThe last "+(t.gen.nextBoolean() ? "shadow" : "apparition")+" seems to have disappeared.\n\n\tYou look left..\n\t..and right..\n\n\n\n\t...left...\n\t...and right...\n\n\n\tCautiously, you turn to face the House of Shadows, only to find it in mid-air, flying directly towards you.");
													break;
												case targetingMinigame.output.GOOD:
													t.say(title, "You direct your "+t.user.weapon+" at the "+(t.gen.nextBoolean() ? "lead" : "first")+" "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and pull the trigger.\n\n\n\tA single round flies towards it, striking it right in the chest.\n\n\n\tAs it disappears into swirling smoke, you twist to aim at the second.\n\n\tJust as it lines up with your sight, you squeeze the trigger, and a "+(t.gen.nextBoolean() ? "single" : "solitary")+" round flies straight and true, "+(t.gen.nextBoolean() ? "hitting" : "striking")+" it right where its heart should be, and turning it to a swirling cloud of smoke.\n\n\n\tYou aim at the third, firing another round, but the "+(t.gen.nextBoolean() ? "shadow" : "apparition")+" manages to "+(t.gen.nextBoolean() ? "dodge" : "evade")+" your shot.\n\n\t"+(t.user.weapon.characteristicSet(Cweapon.ONE_ROUND_MAGAZINE) ? "Before you can switch magazines, " : "Before you can get off another round, ")+"it is upon you.");
													break;
												case targetingMinigame.output.CRITICAL:
													t.say(title, "You aim your "+t.user.weapon+" at the "+(t.gen.nextBoolean() ? "leading" : "primary")+" "+(t.gen.nextBoolean() ? "shadow" : "apparition")+", and fire.\n\n\n\tYour aim was true, and your round flies directly into its head, turning it into swirling smoke.\n\n\n\tThe second shadow "+(t.gen.nextBoolean() ? "tries" : "attempts")+" to "+(t.gen.nextBoolean() ? "dodge" : "evade")+" your shot, but you are too skilled a "+(t.gen.nextBoolean() ? "gunslinger" : "sharpshooter")+", and it vanishes into swirling smoke.\n\n\n\tYou turn yor attention to the third.\n\n\n\n\n\tIt seems to have disappeared.\n\n\tYou look left...\n\t...and right...\n\n\n\n\t...left...\n\t...and right...\n\n\n\n\n\t...Suddenly, its in front of you!\n\n\n\n\t"+(t.user.weapon.characteristicSet(Cweapon.CLOSE_RANGE) ? "You jump away, bringing your "+t.user.weapon+" to bear.\n\n\n\n\tYou manage to get off a shot just as it is upon you, turning the "+(t.gen.nextBoolean() ? "shadow" : "apparition")+" into swirling smoke, just as your life is prematurely ended by it.\n\n\n\n\t" : "You try to get off a shot with your "+t.user.weapon+", but you find that it is too cumbersome to be used effectively at close range, and the shadow consumes you.\n\n\t")+t.capitalize(t.user.gold.toString())+" and your "+t.user.weapon+" clatter to the ground."+(t.config.triggerEgg(.4) ? "\n\n\n\tSomeday, years after the Great Shadow War, and after the Great Familiar City has been conquered by the Army of the Second Coming, a Little Sister of the Rapture, along with her Big Daddy, will come across the remnants of the Pits, and all they will find of you is "+t.user.gold+" and your "+t.user.weapon+"." : "Somewhere in the distance, another adventurer approaches the Pits."));
													break;
												case targetingMinigame.output.BULLSEYE:
													t.user.dead=false;
													t.say(title+" against the odds", "You level your "+t.user.weapon+" at the shadows.\n\n\n\n\tDespite the impossibility of the odds, you ready yourself to fight.\n\n\n\tIn quick succession, you fire a round at each shadow, adjusting your aim without pausing to see if your aim is true, hitting each shadow right where the bright spots that pass for their eyes are.\n\n\n\n\tDespite the impossibility of how outmatched you are by the shadows, you have emerged victorious.");
													break;
												default:
													t.say("Absorbed", "Your aim is out of this world!\n\n\n\tNo, seriously, that shot was... I don't even know.\n\n\n\tLet\'s just say this...");
												}
											}
											else
											{
												switch (res.type)
												{
												case targetingMinigame.output.MISSED:
													t.say(title, "You fire one round at the "+(t.gen.nextBoolean() ? "nearest" : "leading")+" "+(t.gen.nextBoolean() ? "shadow" : "figure")+".\n\n\n\n\tYou miss."+(t.user.weapon.characteristicSet(Cweapon.ONE_ROUND_MAGAZINE) ? "You opt to forgo loading another shot, instead using your "+t.user.weapon+" as if it were a club." : "You aim again, and once more, you miss.\n\n\n\tThis process repeats itself over and over, until the "+(t.gen.nextBoolean() ? "shadows" : "apparitions")+" cease to worry about your shots, and instead process to laugh at you and your "+t.user.weapon+".\n\n\tAt that point, you decide it is better used as a club.")+"\n\n\n\tYou rush the shadows, striking one, and then proceeding to fight the others.\n\n\n\tMiraculously, it works.\n\n\n\tAs the "+(t.gen.nextBoolean() ? "last" : "final")+" "+(t.gen.nextBoolean() ? "shadow" : "figure")+" turns into swirling smoke, a voice, a great and majestic voice, echoes through the Pits:\n\n\n\n\t\tLearn to aim!!\n\t\tThat was awful!\n\t\tGo to the shooting range or something.\n\t\tNow!!\n\n\tIgnoring the voice, shaking off the fright, you turn to face the House of Shadows.");
													break;
												case targetingMinigame.output.GRAZED:
													t.say(title, "You level your "+t.user.weapon+" at the nearest "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and squeeze the trigger.\n\n\tThe round flies straight and true, flying towards its target.\n\tThe "+(t.gen.nextBoolean() ? "shadow" : "apparition")+", as if it had sensed that you were shooting at it, "+(t.gen.nextBoolean() ? "dodging" : "evading")+" the shot.\n\n\tUnfortunately, at least for it, the round barely grazes it.\n\n\n\n\tApparently, even such a small impact is plenty to end it, turning it into a swirling cloud of smoke.\n\n\n\tThe second pauses, if only for a split second, and you take the opportunity to aim at the second one.\n\n\tYour shot is imperfect, and the "+(t.gen.nextBoolean() ? "shadow" : "apparition")+" manages to "+(t.gen.nextBoolean() ? "evade" : "dodge")+" it.\n\n\n\tSwearing quietly, you "+(t.user.weapon.characteristicSet(Cweapon.BOLT_FIRE) ? "cycle the bolt on your "+t.user.weapon : "adjust your aim")+", firing again just in time to hit the second before it gets close.\n\n\n\n\tWithout pausing to watch it disappear into a cloud of smoke, you roll to the side, evading the lunge of the last "+(t.gen.nextBoolean() ? "shadow" : "figure")+".\n\n\tYou manage to bring your "+t.user.weapon+" to bear, and fire a single shot, managing to end it.\n\n\n\tShaking off your close encounter, you turn to face the House of Shadows.");
													break;
												case targetingMinigame.output.POOR:
													t.say(title, "You aim your "+t.user.weapon+" directly at the leading "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and fire.\n\n\n\tThe round flies at its target, hitting it off center, and turning it into a swirling cloud of smoke, as you adjust your aim to hit the second enemy.\n\n\n\tThe second "+(t.gen.nextBoolean() ? "figure" : "shadow")+" faces you as you "+(t.user.weapon.characteristicSet(Cweapon.BOLT_FIRE) ? "cycle the bolt on your"+t.user.weapon : "steady your aim")+", approaching as you do.\n\n\t"+(t.gen.nextBoolean() ? "Luckily" : "Fortunately")+", you\'re able to get off the shot before it gets close.\n\n\tThe round hits the "+(t.gen.nextBoolean() ? "shadow" : "apparition")+", barely, turning it into a swirling cloud of smoke.\n\n\tThe final shadow turns to flee, as you level your "+t.user.weapon+" directly at it.\n\n\n\tYou fire before it gets out of range or into cover, and the final shadow disappears into swirling smoke.\n\n\n\n\n\tSatisfied with your victory, you turn to face the House of Shadows.");
													break;
												case targetingMinigame.output.HIT:
													t.say(title, "You ready yourself, aiming directly at the closest "+(t.gen.nextBoolean() ? "shadow" : "apparition")+".\n\n\n\tYou quickly fire a shot, fully aware of the threat of the two remaining shadows, and watch only for a moment as the round flies through the air, and hits its mark.\n\n\n\tThe "+(t.gen.nextBoolean() ? "shadow" : "figure")+" turns into a swirling cloud of smoke, and you swing your "+t.user.weapon+" to fire at its lieutenant.\n\n\n\tYour enemy advances towards you, and you calmly "+(t.gen.nextBoolean() ? "squeeze" : "pull")+" the trigger.\n\n\n\tYour aim was near-perfect, but the "+(t.gen.nextBoolean() ? "figure" : "shadow")+" sees it coming, and moves to "+(t.gen.nextBoolean() ? "evade" : "dodge")+" it.\n\n\tThe shadow is not quite quick enough, and the round strikes it, turning it to smoke.\n\n\n\tThe final shadow seems to have disappeared, but you see a hint of motion in the corner of your eye, and, almost as if by instinct, turn to face it.\n\n\n\n\n\n\tThe shadow is charging at you, doing its best to end you and defend the House of Shadows, but you are too quick for it, deftly pulling the trigger, and turning the shadow into a cloud of smoke.\n\n\n\tAfter contently watching it swirl away, you turn to face the House of Shadows.");
													break;
												case targetingMinigame.output.GOOD:
													t.say(title, "You quickly survey the field before you, and the three shadows that are before you.\n\n\n\tYou adjust your aim, leveling your "+t.user.weapon+" at the "+(t.gen.nextBoolean() ? "nearest" : "leading")+" "+(t.gen.nextBoolean() ? "apparition" : "shadow")+", prepared to fire.\n\n\n\tAs the "+(t.gen.nextBoolean() ? "figures" : "shadows")+" begin to approach you, you grin ever-so-slightly, and "+(t.gen.nextBoolean() ? "squeeze" : "pull")+" the trigger.\n\n\n\tThe round glides smoothly towards its target, impacting it, and converting it into a swirling cloud of smoke.\n\n\n\tYou turn slightly, ready to fire at the next enemy, and "+(t.gen.nextBoolean() ? "adjust" : "calibrate")+" your aim.\n\n\n\n\tWhen your sights are in place, you fire, and turn the second "+(t.gen.nextBoolean() ? "enemy" : "apparition")+" into swirling smoke.\n\n\n\n\tYou frown slightly.\n\n\tYou could have sworn there where three, yet an empty battlefield and two kills seems to suggest otherwise.\n\n\n\tYou cautiously survey the area, then slowly start to move towards the House of Shadows.\n\n\n\tSuddenly, you see a dark smudge on the edge of your vision, and you turn to grab a better look.\n\n\n\tThe "+(t.gen.nextBoolean() ? "shadow" : "apparition")+", seeing that you found it, charges, as if it was hoping to eliminate you before you can do the same to it, but you fire, and it vanishes, replaced only by smoke.");
													break;
												case targetingMinigame.output.CRITICAL:
													t.say(title, "You stare down the three approaching "+(t.gen.nextBoolean() ? "shadows" : "figures")+", sizing up your opposition.\n\n\n\tThe moment they move to attack, you start to fire, cleanly eliminating your leading "+(t.gen.nextBoolean() ? "target" : "enemy")+", and adjust your aim, targeting the second.\n\n\tIt makes an attempt to flee, but you fire, hitting it in what passes as its head.\n\n\n\tThe last shadow attempts to dodge your line of sight, but you follow it, and eliminate it too.\n\n\n\n\n\tYou pause for only a second to watch the trio disappear into swirling smoke as it diffuses, then turn towards the House of Shadows.");
													break;
												case targetingMinigame.output.BULLSEYE:
													t.say(title, "You effortlessly fire a perfect shot at the "+(t.gen.nextBoolean() ? "nearest" : "leading")+"shadow, a single blast from your"+t.user.weapon+" turning it to smoke.\n\n\tThe remaining shadows hesitate, and thus, you get in a shot at the second "+(t.gen.nextBoolean() ? "figure" : "shadow")+", turning it as well into a swirling cloud of smoke.\n\n\n\tThe last one attempts to flee, but you allow it no such luxury.\n\n\n\n\tWith all three shadows dispatched, you turn to face the House of Shadows.");
													break;
												default:
													t.say("The brightness of a "+t.user.weapon, "In a long and drawn-out engagement, you and your "+t.user.weapon+" outshine the darkness of the shadows.\n\n\n\tFeeling invincible, you turn to face the House of Shadows.");
												}
											}
										}
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
										t.say("A"+(t.gen.nextBoolean() ? " second" : "nother")+" sun", "You hit the detonator on your "+t.user.weapon+".\n\n\n\tA second fireball appears on the horizon, as the people stand in a familiar landscape, and watch you disappear.\n\n\n\tBehind them, the deadly, sickly glow illuminates a terrifying figure.");
									}
								};
								break;
							case Cweapon.TYPE_FUTURE:
								r=new Runnable()
								{
									@Override
									public void run()
									{
										if (t.user.weapon.characteristicSet(Cweapon.CLOSE_RANGE_ONLY)) // CLOSE_RANGE_ONLY is the identifying flag for future weapons. If it's set, we're dealing with a lightsaber. Else, we (probably) have a raygun. These two get different dialogue.
											t.say("Sword of light", "You charge the "+(t.gen.nextBoolean() ? "figures" : "shadows")+", holding your "+t.user.weapon+" up on high.\n\n\n\tThe leading shadow, seemingly feeling courageous, steps toward you, but you counter its approach, dissolving it.\n\n\n\tThe other two "+(t.gen.nextBoolean() ? "shadows" : "apparitions")+" step away, deterred slightly by your charge, but not far enough.\n\n\n\n\n\tA lunge and a quick extension of the arm is enough to turn the shadow to ash almost akin to a hallucination.\n\n\tThe third shadow begins full flight, perhaps attempting to survive, perhaps attempting to draw you away from the House of Shadows.\n\tTo you, it doesn't matter: You chase it down, and eliminate it.\n\n\n\n\tHaving won a simple victory, you grin, and move towards the House of Shadows.");
										else
											t.say("The "+t.capitalize(t.user.weapon.name), "You aim your "+t.user.weapon+" directly at the leading "+t.user.weapon+", and pull the trigger.\n\n\n\tThe bolt of brilliant energy glides to its target, and hits it squarely.\n\n\n\tIt disappears into a mere wisp of smoke, leaving not a mere grain of ash.\n\n\n\n\tThe second and third shadows each seem afraid of you, and your "+t.user.weapon+", but you pay them no attention.\n\n\tYou place your sights in between the bright spots on the second "+(t.gen.nextBoolean() ? "apparition" : "figure")+", and pull the trigger.\n\n\n\tIt attempts to maneuver out of the way, but the bolt strikes it nonetheless, and it disappears as if it were never there.\n\n\n\tThe third turns to "+(t.gen.nextBoolean() ? "escape" : "run away")+", but your bolt outruns it by a factor of several million, and disintegrates it.\n\n\n\n\n\t"+(t.gen.nextBoolean() ? "Victorious" : "Feeling invincible")+", you turn towards the House of Shadows.");
									}
								};
								break;
							default:
								t.user.dead=true;
								r=new Runnable()
								{
									@Override
									public void run()
									{
										t.say("Unknowable", "You suddenly find yourself unable to "+(t.gen.nextBoolean() ? "use" : (t.gen.nextBoolean() ? "grip" : "hold"))+" your "+t.user.weapon+".");
									}
								};
							}
							t.th=new Thread(r);
							t.th.start();
							t.snooze(25); // We need a bit longer to insure processing finishes.
							if (t.user.dead && !undead)
								game.prepContinueButton();
							else
								game.prepContinueButton(new View.OnClickListener()
								{
									@Override
									public void onClick(View v)
									{
										t.th.interrupt();
										t.th=new Thread(new Runnable()
										{
											@Override
											public void run()
											{
												stage=4;
												runStage();
											}
										});
										t.th.start();
									}
								});
						}
						break;
					case 1:
						t.th.interrupt();
						Runnable r1;
						if (t.config.triggerEgg(.05))
						{
							t.user.dead=false;
							r1=new Runnable()
							{
								@Override
								public void run()
								{
									t.say("Impossible", "You face the "+(t.gen.nextBoolean() ? "shadows" : "figures")+" for only a split second before turning, and sprinting for the House of Shadows.\n\n\tYou "+(t.gen.nextBoolean() ? "grab" : "snatch")+" the detonator from its resting place, glancing only for an instant at the cratering charge it controls.\n\n\n\n\tClosely followed by the shadows, you grimace, hoping your distance from the charge is sufficient.\n\n\n\n\n\n\tYou hit the detonator, and the charge goes off, destroying the House and the shadows with it.\n\n\tThe three shadows that had assaulted you are not alone in disappearing, as an invisible wave passes over the pits, turning each shadow it finds into swirling smoke.\n\n\tUnfortunately, you were not quite far enough from the charge, and it throws you up and away, towards the center of the Pits.\n\n\n\n\n\n\n\tIn the mere seconds of flight you get, you resign yourself to your fate of dying, and of never finding out what exists beyond the image of the familiar landscape.\n\n\tAs soon as you do, as if it had been waiting, a strange force slows you and gently redirects your path.\n\n\tYou land, safe but not intact, in the center of "+(t.gen.nextBoolean() ? "Hell." : "the Pits."));
								}
							};
						}
						else
						{
							t.user.dead=true;
							r1=new Runnable()
							{
								@Override
								public void run()
								{
									t.say("Foolhardy", "You quickly turn and sprint towards the House of Shadows.\n\n\n\tUnfortunately, the "+(t.gen.nextBoolean() ? "apparitions" : "shadows")+" sense your advance, and absorb you.");
								}
							};
						}
						t.th.interrupt();
						t.th=new Thread(r1);
						t.th.start();
						if (t.user.dead)
							game.prepContinueButton();
						else
						{
							game.prepContinueButton(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									t.th.interrupt();
									t.th=new Thread(new Runnable()
									{
										@Override
										public void run()
										{
											number=0;
											hasAssaultedHQ=true;
											runStage();
										}
									});
									t.th.start();
								}
							});
						}
						break;
					case 2:
						t.user.dead=true;
						t.th.interrupt();
						t.th=new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								t.say("Outdone", "You attempt to flee from the shadows. They give chase, herding you using their superior numbers.\n\n\tJust when you think you\'ve lost them, you trip on a well, or poorly, placed rock, and tumble down a hill.\n\n\n\n\tWhen you stop, you look at a black sky, only to realize that you\'re staring at a shadow.");
							}
						});
						t.th.start();
						game.prepContinueButton();
						break;
					default:
						game.dataError();
					}
					break;
				case 1:
					switch (input2)
					{
					case 0:
						if (t.user.weapon.type==Cweapon.TYPE_ARCHERY && .65>(t.config.difficultyMult*.75*t.user.weapon.getAbsoluteStrength()))
						{
							game.archeryMinigame((int)Math.ceil(t.gen.nextInt(40)+(t.config.difficultyMult*10)+5), ((20*(1-t.config.difficultyMult))+5)/5);
							t.th=new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									if (t.user.dead)
									{
										String title;
										switch (t.gen.nextInt(3))
										{
										case 0:
											title="Archer";
											break;
										case 1:
											title="Bowman";
											break;
										case 2:
											title="Shoplifter";
											break;
										default:
											title="Outdone by the dark";
										}
										if (t.user.weapon.characteristicSet(Cweapon.SLOW_RELOAD)) // Crossbow
											t.say(title, "You "+(t.gen.nextBoolean() ? "bring" : "swing")+" your "+t.user.weapon+" to bear, and fire it at your target.\n\n\tThe bolt flies straight and true, towards a point just slightly away from the "+(t.gen.nextBoolean() ? "shadow" : "being")+".\n\n\tYou quickly "+(t.gen.nextBoolean() ? "grab" : "snatch")+" another bolt, trying your best to load your weapon.\n\n\tJust when you\'re ready, you look up to find a pair of glowing eyes staring into your soul.");
										else
											t.say(title, "You immediately draw an arrow, bringing it to bear against your foe.\n\n\tUnfortunately, you miss.\n\tYou quickly "+(t.gen.nextBoolean() ? "draw" : "grab")+" another "+(t.gen.nextBoolean() ? "arrow" : "shot")+", and barely manage to draw your "+t.user.weapon+" before the "+(t.gen.nextBoolean() ? "shadow " : "figure ")+(t.gen.nextBoolean() ? "absorbs" : "ends")+" you.");
									}
									else
										t.say(t.gen.nextBoolean() ? "Shopkeeper" : "Veteran", "Your "+(t.gen.nextBoolean() ? "enemy" : "foe")+" charges at you, swaying to "+(t.gen.nextBoolean() ? "evade" : "avoid")+" your line of fire.\n\n\tUnfortunately, for it, you manage to follow its movements, and thusly "+(t.gen.nextBoolean() ? "end" : "kill")+" it, turning it into swirling smoke. You turn your gaze towards the now-cleared shop.");
								}
							});
							t.th.start();
							if (t.user.dead)
								game.prepContinueButton();
							else
								game.prepContinueButton(new View.OnClickListener()
								{
									@Override
									public void onClick(View v)
									{
										t.th=new Thread(new Runnable()
										{
											@Override
											public void run()
											{
												stage=4;
												runStage();
											}
										});
										t.th.start();
									}
								});
						}
						else
						{
							boolean undead=false;
							switch (t.user.weapon.type)
							{
							case Cweapon.TYPE_SHARP:
								t.determineUserDeath((1-t.user.weapon.getNeededFlags(Cweapon.CLOSE_RANGE|Cweapon.LIGHT, 2))*.99);
								r=new Runnable()
								{
									@Override
									public void run()
									{
										if (t.user.dead)
										{
											if (t.gen.nextBoolean())
												t.say("You swing your "+(t.gen.nextBoolean() ? "blade" : t.user.weapon)+", catching the "+(t.gen.nextBoolean() ? "shadow" : "figure")+" off guard."+t.multiplyString('\n', t.gen.nextInt(5)+2)+"\tUnfortunately, it manages to dance its way out of the way, and it ends you.");
											else
												t.say(t.gen.nextBoolean() ? "Newcomer" : "Novice", "You bring your "+t.user.weapon+" to bear, fumbling only slightly, then beginning to "+(t.gen.nextBoolean() ? "slash" : "stab")+" at the "+(t.gen.nextBoolean() ? "shadow" : "figure")+", but it evades you at every thrust."+t.multiplyString('\n', t.gen.nextInt(4)+2)+"\tFinally, after a number of mistakes, it outmaneuvers you."+t.multiplyString('\n', t.gen.nextInt(5)+2)+"\tThe last thought in your head before your "+(t.gen.nextBoolean() ? "time" : "existence")+" is ended, "+((t.config.litEggs && t.config.triggerEgg(.8)) ? ("if you had been allowed to give an utterance to the thoughts that were inspiring you, and they were prophetic, they would have been these:"+t.multiplyString('\n', 2+t.gen.nextInt(3))+"\t\t\"I see a great evil fall across a skyline, a city I thought I used to know.\n\t\t\"I see a man, old, bearded, watching in disappointment as the blots form on my name. I see him, foremost of judges and honored men, bringing another, all too much like me, to this place - Then fair to look upon, without a trace of this day\'s disfigurement - and I hear him tell my replacement my story, with a disappointed and somber voice.\n\t\t\"It is a far, far, sorrier thing that I do, than I have ever done; it is a far, far more dismaying rest that I go to than I have ever known.\""+t.multiplyString('\n', t.gen.nextInt(4)+2)+"\tUnfortunately for your memory, you failed to realize that this was not, in fact, \"A Tale of Two Shadows\".") /* Note that adding some gender-specific pronouns to that might not be the worst idea. */ : "is one of sadness: Sadness that you were never able to properly use your "+t.user.weapon+" well enough to defeat the Shadow in the Pits."));
										}
										else
											t.say(t.gen.nextBoolean() ? "Swordsman" : "Sword master", "You swing your sword as fast as humanly possible, turning one, then two, "+(t.gen.nextBoolean() ? "shadows" : "figures")+" into swirling smoke.\n\n\tThe last shadow charges toward you, but your sword nicks it, and it disappears.\n\n\tA booming voice, almost like Gandalf\'s, echoes from the clouds:\n\n\t\tWhy do you still carry that puny "+t.user.weapon+"??\n\t\tFind a new weapon.\n\t\tNow.\n\n\n\n\n\tYou turn your attention away from the voice, and to the House of Shadows.");
									}
								};
								break;
							case Cweapon.TYPE_ARCHERY:
								t.determineUserDeath(.75*t.user.weapon.getAbsoluteStrength());
								r=new Runnable()
								{
									@Override
									public void run()
									{
										if (t.user.dead)
										{
											String title;
											switch (t.gen.nextInt(4))
											{
											case 0:
												title="Archer";
												break;
											case 1:
												title="Bowman";
												break;
											case 2:
												title="Outnumbered";
												break;
											default:
												title="Outdone by the dark";
											}
											switch (t.gen.nextInt(3))
											{
											case 0:
												t.say(title, "You turn to face the shadows, but they easily defeat you before you get off a single shot.");
												break;
											case 1:
												t.say(title, "You spin to face your enemy, and fire a single "+("crossbow".equalsIgnoreCase(t.user.weapon.name) ? "bolt" : "arrow")+" at the leading "+(t.gen.nextBoolean() ? "shadow" : "apparition")+".\n\n\tYour aim "+(t.gen.nextBoolean() ? "is true" : (t.gen.nextBoolean() ? "is that of a master" : "is masterful"))+", turning the leader of the trio into a swirl of smoke.\n\n\tHowever, before you can ready yourself again, the other two eliminate you.");
												break;
											case 2:
												t.say(title, "You load one "+("crossbow".equalsIgnoreCase(t.user.weapon.name) ? "bolt" : "arrow")+", and thoughtlessly fire it at the lead "+(t.gen.nextBoolean() ? "shadow" : "figure")+".\n\n\tYou\'ve practiced your archery considerably, and the shot flies directly into its target.\n\n\tYou ignore the sight as your opponent disappears into a swirling cloud of shadowy smoke, and dodge the advance of your second adversary, which you shoot shortly afterwards.\n\n\n\tAlas, you lost track of the third shadow, which comes from behind you, and kills you.");
												break;
											default:
												Log.wtf("Cgame at the House of Shadows", "Invalid output from Random.nextInt()");
											}
										}
										else
											t.say((t.config.GoTEggs && t.config.triggerEgg(.8)) ? "Lightbringer" : "Defeater of Shadows", "You turn, snappily aiming your "+t.user.weapon+" directly at the leading "+(t.gen.nextBoolean() ? "shadow" : "apparition")+", and shooting it.\n\n\tPaying it no attention as it disappears into swirling smoke.\n\n\tInstead, as if by instinct, you roll towards the House of Shadows."+(t.gen.nextBoolean() ? "" : "\n\n\tYou quickly recover from the pain of your miscalculation after you slam into the outer wall.")+"\n\n\tWithout taking the time to stand, you adjust your aim, directly at the second enemy.\n\n\tQuickly, you release your shot, sending "+("crossbow".equalsIgnoreCase(t.user.weapon.name) ? "a bolt" : "an arrow")+" flying into the second shape, then clattering to the ground as your opponent swirls away.\n\n\n\tThe third charges, but you pull one "+("crossbow".equalsIgnoreCase(t.user.weapon.name) ? "bolt" : "arrow")+" from your reserves, and drive it through your final adversary, noting the lack of resistance as it goes through.\n\n\tYou watch the remains of your opponent swirl away, and note your two fired shots have vanished.");
									}
								};
								break;
							case Cweapon.TYPE_MODERN:
								final targetingMinigame.output res=game.aimMinigame();
								undead=(res.type==targetingMinigame.output.BULLSEYE);
								final String title=res.toString();
								r=new Runnable()
								{
									@Override
									public void run()
									{
										if (t.user.weapon.characteristicSet(Cweapon.AUTOMATIC))
										{
											t.user.dead=(res.distance/(.8*targetingMinigame.output.MAX_DISTANCE))<((.5*(t.gen.nextDouble()+t.config.difficultyMult))+.5);
											if (t.user.dead)
											{
												switch (res.type)
												{
												case targetingMinigame.output.MISSED:
													t.say(title, "You fire a burst of bullets towards the shadows, but your aim is off, and the "+(t.gen.nextBoolean() ? "shadows" : "figures")+" end you.");
													break;
												case targetingMinigame.output.GRAZED:
													t.say(title, "You turn at the shadows, and fire a burst of automatic fire. One shot hits the nearest "+(t.gen.nextBoolean() ? "shadow" : "vision")+", turning it to a swirl of smoke, but the remaining two intercept you.");
													break;
												case targetingMinigame.output.POOR:
													t.say(title, "You fire at the shadows, grazing one, turning it to swirling smoke, and barely hitting another.\n\n\tThe last consumes you as the second disappears.");
													break;
												case targetingMinigame.output.HIT:
													t.say(title, "You fire at the shadows, hitting one with a solid shot, and "+(t.gen.nextBoolean() ? "turning" : "converting")+" it into a swirling cloud.\n\n\tAnother bullet strikes the second, eliminating it as well, but "+(t.user.weapon.characteristicSet(Cweapon.HIGH_RECOIL) ? "the recoil of your "+t.user.weapon+" throws off your aim." : "the last shadow moves out of your sights too quickly."));
													break;
												case targetingMinigame.output.GOOD:
													t.say(title, "You hit two shadows with near perfect shots.\n\n\tThe shadows disappear into swirling smoke, as the third approaches you.\n\n\n\n\n\tYou quickly twist, aiming directly at the last shadow.\n\n\n\n\n\n\tYou pull the trigger, just as it reaches you."+(t.config.triggerEgg(.5) ? "\n\n\n\tThe shadow of a dragon looms over the pits, the rush of air stirring everything around, as you both become swirling smoke, and "+t.user.gold+", along with your "+t.user.weapon+", clatter to the ground." : ""));
													break;
												case targetingMinigame.output.CRITICAL:
													t.say(title, "You hit the first two shadows with perfect shots, but find that the third seems to have disappeared.\n\n\tYou twist and turn, searching for it, "+(t.config.triggerEgg(.75) ? "and feel a thud, and a weight.\n\n\tYou fall, watching the shadow run, and attempt to give chase.\n\n\tSoon, it leaves your sight. Contented, you enter the House of Shadows, grabbing the detonator, assuming that the charge is at it was.\n\n\n\n\tYou walk clear of the House, wondering why you feel as if there is a huge weight on your back.\n\n\n\tYou realize that the shadow planted the charge on your back a moment too late, as you trigger the explosive." : "and soon finding it, right behind you.\n\n\tYou dodge its lunge, dropping your "+t.user.weapon+" in your haste to "+(t.gen.nextBoolean() ? "escape" : "survive")+", and, just as you think you\'ve escaped, you trip, rolling down a hill, coming to a stop in a ditch full of a hundred shadows."));
													break;
												case targetingMinigame.output.BULLSEYE:
													t.user.dead=false;
													t.say(title+", despite impossibility", "You fire three utterly perfect shots, cleanly converting two shadows into swirling smoke, and then you fire a hail of bullets into the third, ending it as well.\n\n\tDespite the incredibly difficult odds against you, you have prevailed.");
													break;
												default:
													t.say("Gunshot", "You are easily defeated by the shadows.");
												}
											}
											else
											{
												switch (res.type)
												{
												case targetingMinigame.output.MISSED:
													t.say(title, "You miss every shot in your magazine, yet manage to kill all three shadows by using your "+t.user.weapon+" as a club.\n\n\n\tFrom the clouds, a booming voice, almost like Gandalf\'s, echoes through the pits:\n\n\t\tGet better aim!!\n\t\tSeriously, go to target practice or something...\n\t\tThat was ridiculous.\n\n\n\n\n\n\tYou shake it off and turn your attention to the House of Shadows.");
													break;
												case targetingMinigame.output.GRAZED:
													t.say(title, "You fire at the enemies, full automatic fire, and yet the shadows keep managing to dodge your fire.\n\n\t"+(t.user.weapon.characteristicSet(Cweapon.HIGH_RECOIL) ? "You suspect that its because your "+t.user.weapon+" has too much recoil.\n\n\t" : "")+"Just as you begin to worry about the need to reload, one bullet grazes the leading shadow.\n\tSomehow, to your surprise, it disappears, turning into swirling smoke.\n\n\n\n\tThe remaining "+(t.gen.nextBoolean() ? "shadows" : "figures")+" mirror your surprise: They pause in shock.\n\n\tYou take advantage of their surprise to fire "+(t.gen.nextBoolean() ? "a trio" : "a hail")+" of "+(t.gen.nextBoolean() ? "rounds" : "bullets")+" at the second "+(t.gen.nextBoolean() ? "figure" : "shadow")+".\n\n\tIt disappears into a swirling cloud of smoke, and you continue firing, now at the last enemy, and barely manage to graze it.\n\n\n\tIgnoring what is now just a cloud of smoke, you turn on the House of Shadows.");
													break;
												case targetingMinigame.output.POOR:
													t.say(title, "You level the sights of your "+t.user.weapon+" directly at the leading "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and fire a "+(t.gen.nextBoolean() ? "hail" : "number")+" of rounds into it.\n\n\n\tIt "+(t.gen.nextBoolean() ? "becomes" : "disappears into")+" a swirling cloud of smoke, as you turn your attention to the second enemy.\n\n\tIt approaches "+(t.gen.nextBoolean() ? "menacingly" : "threateningly")+", but you "+(t.gen.nextBoolean() ? "are able" : "manage")+" to level your sights at it, and you squeeze the trigger, watching the bullets barely hit the "+(t.gen.nextBoolean() ? "shadow" : "figure")+".\n\n\tLuckily, it seems that any hit will "+(t.gen.nextBoolean() ? "dissolve" : "destroy")+" the shadow, and you watch as it disappears into a swirling cloud of smoke.\n\n\n\n\n\tUnfortunately, you seem to have lost track of the third...\n\n\n\n\tYou look left...\n\n\t...and right...\n\n\t...left...\n\n\t...and there it is!\n\n\n\n\tThe last "+(t.gen.nextBoolean() ? "shadow" : "figure")+" "+(t.gen.nextBoolean() ? "shows itself" : "appears")+" immediately just in front of you!\n\n\n\n\n\n\tYou hastily fire your "+t.user.weapon+", and turn it to smoke, barely surviving the encounter.\n\n\tYou shake yourself off, and turn to face the House of Shadows.");
													break;
												case targetingMinigame.output.HIT:
													t.say(title, "You aim your "+t.user.weapon+" directly at the leading enemy, and "+(t.gen.nextBoolean() ? "squeeze" : "pull")+" the trigger.\n\n\tA "+(t.gen.nextBoolean() ? "burst" : "number")+" of rounds fly directly at it, and strike it, turning it into a swirling cloud of smoke.\n\n\tYou quickly turn, and aim at the second "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and manage to send a hail of "+(t.gen.nextBoolean() ? "rounds" : "fire")+" flying directly at it.\n\n\n\tYou pause for a split second to watch as the "+(t.gen.nextBoolean() ? "apparition" : "shadow")+" dissolves into a cloud of smoke.\n\n\tThe final shadow seems to have disappeared.\n\n\t"+(t.gen.nextBoolean() ? "You" : "Your "+t.user.weapon)+" feels light, "+(t.user.weapon.characteristicSet(Cweapon.QUICK_RELOAD) ? "so you quickly switch cartridges." : "but you decide it would take too long to reload it.")+"\n\n\n\n\tYou return your attention to where it probably should be, the rather deadly "+(t.gen.nextBoolean() ? "shadow" : "apparition")+" that could reappear and kill you at any moment.\n\tSuddenly, it reappears: Right in front of you!\n\n\n\n\tYou quickly pull the trigger, firing several shots into the "+(t.gen.nextBoolean() ? "figure" : "shadow")+" before you.\n\n\n\tYou turn to oppose the House of Shadows as it disappears into a swirling cloud of smoke.");
													break;
												case targetingMinigame.output.GOOD:
													t.say(title, "You aim, and fire your "+t.user.weapon+" directly at the "+(t.gen.nextBoolean() ? "nearest" : (t.gen.nextBoolean() ? "closest" : "nearest"))+" "+(t.gen.nextBoolean() ? "shadow" : "figure")+", sending a hail of bullets flying directly at its core.\n\n\n\tThe first round out of your "+(t.gen.nextBoolean() ? "weapon" : t.user.weapon)+" hits it, turning it into a swirling cloud of smoke.\n\n\n\tYou quickly shift your aim, leveling your sights at the next target, and "+(t.gen.nextBoolean() ? "squeeze" : "pull")+" the trigger, scoring a solid hit, and turning the shadow into a swirling cloud of smoke.\n\n\n\tThe third shadow comes around to face you, and dodges your first burst.\n\n\n\tYou quickly adjust your aim, and "+(t.gen.nextBoolean() ? "squeeze" : "pull")+" the trigger, scoring another hit and ending the shadow.\n\n\n\tYou turn to face the House of Shadows as it turns to swirling smoke.");
													break;
												case targetingMinigame.output.CRITICAL:
													t.say(title, "You aim at the leading "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and fire a hail of rounds from your "+t.user.weapon+", scoring an extremely solid shot and dissolving it.\n\n\n\tAs it disappears into smoke, you adjust your aim, aiming for the next "+(t.gen.nextBoolean() ? "shadow" : "apparition")+", and fire a trio of bullets into its core, turning it into a swirling cloud of smoke.\n\n\n\n\tThe last shadow attempts to disappear, to catch you from behind, but you fire, and hit it precisely in its center of mass.\n\n\n\tAs it dissolves into swirling smoke, you turn to face the House of Shadows.");
													break;
												case targetingMinigame.output.BULLSEYE:
													t.say(title, "You level your "+t.user.weapon+" at the "+(t.gen.nextBoolean() ? "leading" : "nearest")+" "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and squeeze the trigger, sending a hail of bullets flying directly towards it, striking it perfectly between the two glowing spots that pass for eyes.\n\n\tAs it disappears into swirling smoke, you let off the trigger, and adjust your aim, ready to eliminate the second one.\n\n\n\n\tJust as it begins to approach you, you fire again, and a trio of bullets score a perfect hit on your target.\n\n\n\tIt disappears into smoke, and you aim directly at the last target.\n\n\tIt attempts to flee, but you don\'t allow it the chance: You "+(t.gen.nextBoolean() ? "squeeze" : "pull")+" the trigger, sending a single round into the "+(t.gen.nextBoolean() ? "shadow" : "figure")+", hitting its forehead dead-center, and converting it into a swirling cloud of smoke.\n\n\n\n\tYou turn to face the House of Shadows.");
													break;
												default:
													t.say("A "+t.user.weapon+" to a shadowfight", "You manage to defeat the shadows.");
												}
											}
										}
										else
										{
											t.user.dead=(res.distance/(.6*targetingMinigame.output.MAX_DISTANCE))<((.5*(t.gen.nextDouble()+t.config.difficultyMult))+.5);
											if (t.user.dead)
											{
												switch (res.type)
												{
												case targetingMinigame.output.MISSED:
													t.say(title, "You aim at the leading "+(t.gen.nextBoolean() ? "figure" : "shadow")+", and fire.\n\n\tYou miss.\n\tBadly.\n\n\t"+(t.config.triggerEgg(.5) ? ("Years from now, after the Fall of the Shadows, and after another explorer, perhaps even one called "+t.user+", has liberated the Great Familiar City, a small "+(t.gen.nextBoolean() ? "boy, and his" : "girl, and her")+" "+(t.gen.nextBoolean() ? "mother" : (t.config.triggerEgg(.2) ? "Big Daddy" : "father"))+" will come across the spot where you faced the shadows, and they will take all they find, your "+t.user.weapon+", home as a souvenir.") : (t.user.weapon.characteristicSet(Cweapon.BOLT_FIRE) ? "Before you can cycle the action, they are upon you." : "They take the chance to consume you.")));
													break;
												case targetingMinigame.output.GRAZED:
													t.say(title, "You level your "+t.user.weapon+" at the lead "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and squeeze the trigger.\n\n\n\tA single round flies into the side of it, and it vanishes from the slightest impact.\n\n\tIt would appear that the slightest impact will eliminate a shadow.\n\n\n\n\tYou level your weapon at the second shadow, but you have just enough time to notice that the third has disappeared from your sight when it appears behind you, and ends you.");
													break;
												case targetingMinigame.output.POOR:
													t.say(title, "You place the sights of your "+t.user.weapon+" at the leading "+(t.gen.nextBoolean() ? "apparition" : "shadow")+", and pull the trigger.\n\n\n\tAlmost as if by instinct, you find that you're adjusting your aim to fire at the second "+(t.gen.nextBoolean() ? "shadow" : "figure")+".\n\n\n\tAlas, your instinct is not as skilled as your hand, and your shot flies wide.\n\n\n\n\tYou try to readjust your aim, but the "+(t.gen.nextBoolean() ? "shadows" : "figures")+" are too fast.");
													break;
												case targetingMinigame.output.HIT:
													t.say(title, "You aim towards the leading shadow, and pull the trigger, sending a single round flying into it.\n\n\tThe shot scores a solid hit, and turns the "+(t.gen.nextBoolean() ? "shadow" : "figure")+" into a swirling cloud of smoke.\n\n\tWithout pausing to watch it, you adjust your aim, leveling your "+t.user.weapon+" at your second opponent.\n\n\tYou fire a shot, and barely nick it, turning it into smoke.\n\n\tThe last "+(t.gen.nextBoolean() ? "shadow" : "apparition")+" seems to have disappeared.\n\n\tYou look left..\n\t..and right..\n\n\n\n\t...left...\n\t...and right...\n\n\n\tCautiously, you turn to face the House of Shadows, only to find it in mid-air, flying directly towards you.");
													break;
												case targetingMinigame.output.GOOD:
													t.say(title, "You direct your "+t.user.weapon+" at the "+(t.gen.nextBoolean() ? "lead" : "first")+" "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and pull the trigger.\n\n\n\tA single round flies towards it, striking it right in the chest.\n\n\n\tAs it disappears into swirling smoke, you twist to aim at the second.\n\n\tJust as it lines up with your sight, you squeeze the trigger, and a "+(t.gen.nextBoolean() ? "single" : "solitary")+" round flies straight and true, "+(t.gen.nextBoolean() ? "hitting" : "striking")+" it right where its heart should be, and turning it to a swirling cloud of smoke.\n\n\n\tYou aim at the third, firing another round, but the "+(t.gen.nextBoolean() ? "shadow" : "apparition")+" manages to "+(t.gen.nextBoolean() ? "dodge" : "evade")+" your shot.\n\n\t"+(t.user.weapon.characteristicSet(Cweapon.ONE_ROUND_MAGAZINE) ? "Before you can switch magazines, " : "Before you can get off another round, ")+"it is upon you.");
													break;
												case targetingMinigame.output.CRITICAL:
													t.say(title, "You aim your "+t.user.weapon+" at the "+(t.gen.nextBoolean() ? "leading" : "primary")+" "+(t.gen.nextBoolean() ? "shadow" : "apparition")+", and fire.\n\n\n\tYour aim was true, and your round flies directly into its head, turning it into swirling smoke.\n\n\n\tThe second shadow "+(t.gen.nextBoolean() ? "tries" : "attempts")+" to "+(t.gen.nextBoolean() ? "dodge" : "evade")+" your shot, but you are too skilled a "+(t.gen.nextBoolean() ? "gunslinger" : "sharpshooter")+", and it vanishes into swirling smoke.\n\n\n\tYou turn yor attention to the third.\n\n\n\n\n\tIt seems to have disappeared.\n\n\tYou look left...\n\t...and right...\n\n\n\n\t...left...\n\t...and right...\n\n\n\n\n\t...Suddenly, its in front of you!\n\n\n\n\t"+(t.user.weapon.characteristicSet(Cweapon.CLOSE_RANGE) ? "You jump away, bringing your "+t.user.weapon+" to bear.\n\n\n\n\tYou manage to get off a shot just as it is upon you, turning the "+(t.gen.nextBoolean() ? "shadow" : "apparition")+" into swirling smoke, just as your life is prematurely ended by it.\n\n\n\n\t" : "You try to get off a shot with your "+t.user.weapon+", but you find that it is too cumbersome to be used effectively at close range, and the shadow consumes you.\n\n\t")+t.capitalize(t.user.gold.toString())+" and your "+t.user.weapon+" clatter to the ground."+(t.config.triggerEgg(.4) ? "\n\n\n\tSomeday, years after the Great Shadow War, and after the Great Familiar City has been conquered by the Army of the Second Coming, a Little Sister of the Rapture, along with her Big Daddy, will come across the remnants of the Pits, and all they will find of you is "+t.user.gold+" and your "+t.user.weapon+"." : "Somewhere in the distance, another adventurer approaches the Pits."));
													break;
												case targetingMinigame.output.BULLSEYE:
													t.user.dead=false;
													t.say(title+" against the odds", "You level your "+t.user.weapon+" at the shadows.\n\n\n\n\tDespite the impossibility of the odds, you ready yourself to fight.\n\n\n\tIn quick succession, you fire a round at each shadow, adjusting your aim without pausing to see if your aim is true, hitting each shadow right where the bright spots that pass for their eyes are.\n\n\n\n\tDespite the impossibility of how outmatched you are by the shadows, you have emerged victorious.");
													break;
												default:
													t.say("Absorbed", "Your aim is out of this world!\n\n\n\tNo, seriously, that shot was... I don't even know.\n\n\n\tLet\'s just say this...");
												}
											}
											else
											{
												switch (res.type)
												{
												case targetingMinigame.output.MISSED:
													t.say(title, "You fire one round at the "+(t.gen.nextBoolean() ? "nearest" : "leading")+" "+(t.gen.nextBoolean() ? "shadow" : "figure")+".\n\n\n\n\tYou miss."+(t.user.weapon.characteristicSet(Cweapon.ONE_ROUND_MAGAZINE) ? "You opt to forgo loading another shot, instead using your "+t.user.weapon+" as if it were a club." : "You aim again, and once more, you miss.\n\n\n\tThis process repeats itself over and over, until the "+(t.gen.nextBoolean() ? "shadows" : "apparitions")+" cease to worry about your shots, and instead process to laugh at you and your "+t.user.weapon+".\n\n\tAt that point, you decide it is better used as a club.")+"\n\n\n\tYou rush the shadows, striking one, and then proceeding to fight the others.\n\n\n\tMiraculously, it works.\n\n\n\tAs the "+(t.gen.nextBoolean() ? "last" : "final")+" "+(t.gen.nextBoolean() ? "shadow" : "figure")+" turns into swirling smoke, a voice, a great and majestic voice, echoes through the Pits:\n\n\n\n\t\tLearn to aim!!\n\t\tThat was awful!\n\t\tGo to the shooting range or something.\n\t\tNow!!\n\n\tIgnoring the voice, shaking off the fright, you turn to face the House of Shadows.");
													break;
												case targetingMinigame.output.GRAZED:
													t.say(title, "You level your "+t.user.weapon+" at the nearest "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and squeeze the trigger.\n\n\tThe round flies straight and true, flying towards its target.\n\tThe "+(t.gen.nextBoolean() ? "shadow" : "apparition")+", as if it had sensed that you were shooting at it, "+(t.gen.nextBoolean() ? "dodging" : "evading")+" the shot.\n\n\tUnfortunately, at least for it, the round barely grazes it.\n\n\n\n\tApparently, even such a small impact is plenty to end it, turning it into a swirling cloud of smoke.\n\n\n\tThe second pauses, if only for a split second, and you take the opportunity to aim at the second one.\n\n\tYour shot is imperfect, and the "+(t.gen.nextBoolean() ? "shadow" : "apparition")+" manages to "+(t.gen.nextBoolean() ? "evade" : "dodge")+" it.\n\n\n\tSwearing quietly, you "+(t.user.weapon.characteristicSet(Cweapon.BOLT_FIRE) ? "cycle the bolt on your "+t.user.weapon : "adjust your aim")+", firing again just in time to hit the second before it gets close.\n\n\n\n\tWithout pausing to watch it disappear into a cloud of smoke, you roll to the side, evading the lunge of the last "+(t.gen.nextBoolean() ? "shadow" : "figure")+".\n\n\tYou manage to bring your "+t.user.weapon+" to bear, and fire a single shot, managing to end it.\n\n\n\tShaking off your close encounter, you turn to face the House of Shadows.");
													break;
												case targetingMinigame.output.POOR:
													t.say(title, "You aim your "+t.user.weapon+" directly at the leading "+(t.gen.nextBoolean() ? "shadow" : "figure")+", and fire.\n\n\n\tThe round flies at its target, hitting it off center, and turning it into a swirling cloud of smoke, as you adjust your aim to hit the second enemy.\n\n\n\tThe second "+(t.gen.nextBoolean() ? "figure" : "shadow")+" faces you as you "+(t.user.weapon.characteristicSet(Cweapon.BOLT_FIRE) ? "cycle the bolt on your"+t.user.weapon : "steady your aim")+", approaching as you do.\n\n\t"+(t.gen.nextBoolean() ? "Luckily" : "Fortunately")+", you\'re able to get off the shot before it gets close.\n\n\tThe round hits the "+(t.gen.nextBoolean() ? "shadow" : "apparition")+", barely, turning it into a swirling cloud of smoke.\n\n\tThe final shadow turns to flee, as you level your "+t.user.weapon+" directly at it.\n\n\n\tYou fire before it gets out of range or into cover, and the final shadow disappears into swirling smoke.\n\n\n\n\n\tSatisfied with your victory, you turn to face the House of Shadows.");
													break;
												case targetingMinigame.output.HIT:
													t.say(title, "You ready yourself, aiming directly at the closest "+(t.gen.nextBoolean() ? "shadow" : "apparition")+".\n\n\n\tYou quickly fire a shot, fully aware of the threat of the two remaining shadows, and watch only for a moment as the round flies through the air, and hits its mark.\n\n\n\tThe "+(t.gen.nextBoolean() ? "shadow" : "figure")+" turns into a swirling cloud of smoke, and you swing your "+t.user.weapon+" to fire at its lieutenant.\n\n\n\tYour enemy advances towards you, and you calmly "+(t.gen.nextBoolean() ? "squeeze" : "pull")+" the trigger.\n\n\n\tYour aim was near-perfect, but the "+(t.gen.nextBoolean() ? "figure" : "shadow")+" sees it coming, and moves to "+(t.gen.nextBoolean() ? "evade" : "dodge")+" it.\n\n\tThe shadow is not quite quick enough, and the round strikes it, turning it to smoke.\n\n\n\tThe final shadow seems to have disappeared, but you see a hint of motion in the corner of your eye, and, almost as if by instinct, turn to face it.\n\n\n\n\n\n\tThe shadow is charging at you, doing its best to end you and defend the House of Shadows, but you are too quick for it, deftly pulling the trigger, and turning the shadow into a cloud of smoke.\n\n\n\tAfter contently watching it swirl away, you turn to face the House of Shadows.");
													break;
												case targetingMinigame.output.GOOD:
													t.say(title, "You quickly survey the field before you, and the three shadows that are before you.\n\n\n\tYou adjust your aim, leveling your "+t.user.weapon+" at the "+(t.gen.nextBoolean() ? "nearest" : "leading")+" "+(t.gen.nextBoolean() ? "apparition" : "shadow")+", prepared to fire.\n\n\n\tAs the "+(t.gen.nextBoolean() ? "figures" : "shadows")+" begin to approach you, you grin ever-so-slightly, and "+(t.gen.nextBoolean() ? "squeeze" : "pull")+" the trigger.\n\n\n\tThe round glides smoothly towards its target, impacting it, and converting it into a swirling cloud of smoke.\n\n\n\tYou turn slightly, ready to fire at the next enemy, and "+(t.gen.nextBoolean() ? "adjust" : "calibrate")+" your aim.\n\n\n\n\tWhen your sights are in place, you fire, and turn the second "+(t.gen.nextBoolean() ? "enemy" : "apparition")+" into swirling smoke.\n\n\n\n\tYou frown slightly.\n\n\tYou could have sworn there where three, yet an empty battlefield and two kills seems to suggest otherwise.\n\n\n\tYou cautiously survey the area, then slowly start to move towards the House of Shadows.\n\n\n\tSuddenly, you see a dark smudge on the edge of your vision, and you turn to grab a better look.\n\n\n\tThe "+(t.gen.nextBoolean() ? "shadow" : "apparition")+", seeing that you found it, charges, as if it was hoping to eliminate you before you can do the same to it, but you fire, and it vanishes, replaced only by smoke.");
													break;
												case targetingMinigame.output.CRITICAL:
													t.say(title, "You stare down the three approaching "+(t.gen.nextBoolean() ? "shadows" : "figures")+", sizing up your opposition.\n\n\n\tThe moment they move to attack, you start to fire, cleanly eliminating your leading "+(t.gen.nextBoolean() ? "target" : "enemy")+", and adjust your aim, targeting the second.\n\n\tIt makes an attempt to flee, but you fire, hitting it in what passes as its head.\n\n\n\tThe last shadow attempts to dodge your line of sight, but you follow it, and eliminate it too.\n\n\n\n\n\tYou pause for only a second to watch the trio disappear into swirling smoke as it diffuses, then turn towards the House of Shadows.");
													break;
												case targetingMinigame.output.BULLSEYE:
													t.say(title, "You effortlessly fire a perfect shot at the "+(t.gen.nextBoolean() ? "nearest" : "leading")+"shadow, a single blast from your"+t.user.weapon+" turning it to smoke.\n\n\tThe remaining shadows hesitate, and thus, you get in a shot at the second "+(t.gen.nextBoolean() ? "figure" : "shadow")+", turning it as well into a swirling cloud of smoke.\n\n\n\tThe last one attempts to flee, but you allow it no such luxury.\n\n\n\n\tWith all three shadows dispatched, you turn to face the House of Shadows.");
													break;
												default:
													t.say("The brightness of a "+t.user.weapon, "In a long and drawn-out engagement, you and your "+t.user.weapon+" outshine the darkness of the shadows.\n\n\n\tFeeling invincible, you turn to face the House of Shadows.");
												}
											}
										}
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
										t.say("A"+(t.gen.nextBoolean() ? " second" : "nother")+" sun", "You hit the detonator on your "+t.user.weapon+".\n\n\n\tA second fireball appears on the horizon, as the people stand in a familiar landscape, and watch you disappear.\n\n\n\tBehind them, the deadly, sickly glow illuminates a terrifying figure.");
									}
								};
								break;
							case Cweapon.TYPE_FUTURE:
								r=new Runnable()
								{
									@Override
									public void run()
									{
										if (t.user.weapon.characteristicSet(Cweapon.CLOSE_RANGE_ONLY)) // CLOSE_RANGE_ONLY is the identifying flag for future weapons. If it's set, we're dealing with a lightsaber. Else, we (probably) have a raygun. These two get different dialogue.
											t.say("Sword of light", "You charge the "+(t.gen.nextBoolean() ? "figures" : "shadows")+", holding your "+t.user.weapon+" up on high.\n\n\n\tThe leading shadow, seemingly feeling courageous, steps toward you, but you counter its approach, dissolving it.\n\n\n\tThe other two "+(t.gen.nextBoolean() ? "shadows" : "apparitions")+" step away, deterred slightly by your charge, but not far enough.\n\n\n\n\n\tA lunge and a quick extension of the arm is enough to turn the shadow to ash almost akin to a hallucination.\n\n\tThe third shadow begins full flight, perhaps attempting to survive, perhaps attempting to draw you away from the House of Shadows.\n\tTo you, it doesn't matter: You chase it down, and eliminate it.\n\n\n\n\tHaving won a simple victory, you grin, and move towards the House of Shadows.");
										else
											t.say("The "+t.capitalize(t.user.weapon.name), "You aim your "+t.user.weapon+" directly at the leading "+t.user.weapon+", and pull the trigger.\n\n\n\tThe bolt of brilliant energy glides to its target, and hits it squarely.\n\n\n\tIt disappears into a mere wisp of smoke, leaving not a mere grain of ash.\n\n\n\n\tThe second and third shadows each seem afraid of you, and your "+t.user.weapon+", but you pay them no attention.\n\n\tYou place your sights in between the bright spots on the second "+(t.gen.nextBoolean() ? "apparition" : "figure")+", and pull the trigger.\n\n\n\tIt attempts to maneuver out of the way, but the bolt strikes it nonetheless, and it disappears as if it were never there.\n\n\n\tThe third turns to "+(t.gen.nextBoolean() ? "escape" : "run away")+", but your bolt outruns it by a factor of several million, and disintegrates it.\n\n\n\n\n\t"+(t.gen.nextBoolean() ? "Victorious" : "Feeling invincible")+", you turn towards the House of Shadows.");
									}
								};
								break;
							default:
								t.user.dead=true;
								r=new Runnable()
								{
									@Override
									public void run()
									{
										t.say("Unknowable", "You suddenly find yourself unable to "+(t.gen.nextBoolean() ? "use" : (t.gen.nextBoolean() ? "grip" : "hold"))+" your "+t.user.weapon+".");
									}
								};
							}
							t.th=new Thread(r);
							t.th.start();
							t.snooze(25); // We need a bit longer to insure processing finishes.
							if (t.user.dead && !undead)
								game.prepContinueButton();
							else
								game.prepContinueButton(new View.OnClickListener()
								{
									@Override
									public void onClick(View v)
									{
										t.th.interrupt();
										t.th=new Thread(new Runnable()
										{
											@Override
											public void run()
											{
												stage=4;
												runStage();
											}
										});
										t.th.start();
									}
								});
						}
						break;
					case 1:
						t.th.interrupt();
						Runnable r1;
						if (t.config.triggerEgg(.05))
						{
							t.user.dead=false;
							r1=new Runnable()
							{
								@Override
								public void run()
								{
									t.say("Impossible", "You face the "+(t.gen.nextBoolean() ? "shadows" : "figures")+" for only a split second before turning, and sprinting for the House of Shadows.\n\n\tYou "+(t.gen.nextBoolean() ? "grab" : "snatch")+" the detonator from its resting place, glancing only for an instant at the cratering charge it controls.\n\n\n\n\tClosely followed by the shadows, you grimace, hoping your distance from the charge is sufficient.\n\n\n\n\n\n\tYou hit the detonator, and the charge goes off, destroying the House and the shadows with it.\n\n\tThe three shadows that had assaulted you are not alone in disappearing, as an invisible wave passes over the pits, turning each shadow it finds into swirling smoke.\n\n\tUnfortunately, you were not quite far enough from the charge, and it throws you up and away, towards the center of the Pits.\n\n\n\n\n\n\n\tIn the mere seconds of flight you get, you resign yourself to your fate of dying, and of never finding out what exists beyond the image of the familiar landscape.\n\n\tAs soon as you do, as if it had been waiting, a strange force slows you and gently redirects your path.\n\n\tYou land, safe but not intact, in the center of "+(t.gen.nextBoolean() ? "Hell." : "the Pits."));
								}
							};
						}
						else
						{
							t.user.dead=true;
							r1=new Runnable()
							{
								@Override
								public void run()
								{
									t.say("Foolhardy", "You quickly turn and sprint towards the House of Shadows.\n\n\n\tUnfortunately, the "+(t.gen.nextBoolean() ? "apparitions" : "shadows")+" sense your advance, and absorb you.");
								}
							};
						}
						t.th.interrupt();
						t.th=new Thread(r1);
						t.th.start();
						if (t.user.dead)
							game.prepContinueButton();
						else
						{
							game.prepContinueButton(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									t.th.interrupt();
									t.th=new Thread(new Runnable()
									{
										@Override
										public void run()
										{
											number=0;
											hasAssaultedHQ=true;
											runStage();
										}
									});
									t.th.start();
								}
							});
						}
						break;
					case 2:
						t.user.dead=true;
						t.th.interrupt();
						t.th=new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								t.say("Outdone", "You attempt to flee from the shadows. They give chase, herding you using their superior numbers.\n\n\tJust when you think you\'ve lost them, you trip on a well, or poorly, placed rock, and tumble down a hill.\n\n\n\n\tWhen you stop, you look at a black sky, only to realize that you\'re staring at a shadow.");
							}
						});
						t.th.start();
						game.prepContinueButton();
						break;
					default:
						game.dataError();
					}
					break;
				}
				break;
			}
		}
	}
}
