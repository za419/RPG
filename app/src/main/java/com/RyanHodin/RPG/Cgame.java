package com.RyanHodin.RPG;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Cgame implements Serializable, Parcelable
{
	public static final long serialVersionUID=1;

	public int stage; // Where are we?
	public int line; // Persistent input
	public int inputted; // Overwritten per input
	public boolean doCommitSuicide; // Does exiting the input menu execute user.commitSuicide() or return to runStage()?

	public static MainActivity t;

	public Cgame() 
	{
		stage=0;
		line=0;
		inputted=-1;
	}

	private Cgame (Parcel in)
	{
		stage=in.readInt();
		line=in.readInt();
		inputted=in.readInt();
	}

	public void saveTo(SharedPreferences.Editor edit)
	{
		edit.putInt("gameStage", stage-1);
		edit.putInt("gameLine", line);
		edit.putInt("gameInputted", inputted);
		if (Build.VERSION.SDK_INT>=9)
			edit.apply();
		else
			edit.commit();
	}

	public void loadFrom(SharedPreferences sp)
	{
		stage=sp.getInt("gameStage", stage);
		line=sp.getInt("gameLine", line);
		inputted=sp.getInt("gameInputted", inputted);
	}

	public void runStage()
	{
		t.displayHandler.removeMessages(1);
		if (Thread.interrupted())
			return;
		t.config.computerPaused=true;
		doCommitSuicide=false;
		t.user.dead=false;

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

		switch (stage)
		{
		case -1:
			t.fadeout();
			t.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					t.setContentView(R.layout.dead);
					t.currentView=R.id.deadLayout;
					t.setUi();
				}
			});
			break;
		case 0:
			if (t.config.easterEggs && ("King Arthur".equalsIgnoreCase(t.user.name) || "King Arthur of Camelot".equalsIgnoreCase(t.user.name) || "Arthur, King of the Britons".equalsIgnoreCase(t.user.name) || "It is Arthur, King of the Britons".equalsIgnoreCase(t.user.name)))
			{
				runArthur((byte)0, null);
				return;
			}
			if (t.config.gender)
			{
				t.th=new Thread (new Runnable()
				{
					@Override
					public void run ()
					{
						t.say("Good.\n\n\tNow, "+t.user.name+", since it\'s too dark to see, and for my accounting purposes...");
					}
				});
				t.th.start();
				prepContinueButton();
			}
			else
			{
				stage=2;
				runStage();
				return;
			}
			break;
		case 1:
			t.fadeout();
			t.runOnUiThread(new Runnable()
			{
				@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
				@Override
				public void run()
				{
					LinearLayout l=prepInput("What gender are you?");
					List<String> genders=new ArrayList<String>(100);
					genders.add("Male");
					genders.add("Female");
					if (!t.config.twoGender)
					{
						if (t.config.specialGender)
						{
							genders.add("Transsexual male");
							genders.add("Transsexual female");
							genders.add("Metrosexual male");
							genders.add("Metrosexual female");
							genders.add("Male to Female");
							genders.add("Female to Male");
							genders.add("Uncertain");
							genders.add("Unwilling to say");
							genders.add("It\'s complicated");
							genders.add("Genderqueer");
							genders.add("Dual");
							genders.add("Male, but curious as to what being a female is like");
							genders.add("Female, but curious as to what being a male is like");
							genders.add("Male, but overweight, so have moobs");
							genders.add("Female, but have Adam\'s apple");
							genders.add("Hermaphrodite with strong male leanings");
							genders.add("Hermaphrodite with strong female leanings");
							genders.add("Hermaphrodite with no strong gender leanings");
							genders.add("Conjoined twin - Male");
							genders.add("Conjoined twin - Female");
							genders.add("Conjoined twin - Other");
							genders.add("Born without genitals - Identify as male");
							genders.add("Born without genitals - Identify as female");
							genders.add("Born without genitals - Identify otherwise");
							genders.add("Born without genitals - and proud of it");
							genders.add("Born male, had bad circumcision, raised female");
							genders.add("WOMYN, thank you very much!");
							genders.add("Angel");
							genders.add("Mortal Angel");
							genders.add("Sentient Artificial Intelligence - Identify as ungendered");
							genders.add("Sentient Artificial Intelligence - Identify as male");
							genders.add("Sentient Artificial Intelligence - Identify as female");
							genders.add("Sentient Artificial Intelligence - Identify as other");
							genders.add("Household pet that walked across the device - Male");
							genders.add("Household pet that walked across the device - Female");
							genders.add("Household pet that walked across the device - Other");
							genders.add("Cross Dresser");
							genders.add("In between");
							genders.add("Intersex");
							genders.add("Pangender");
							genders.add("Two spirit");
							genders.add("Other");
							genders.add("Neutrois");
							genders.add("Prefer not to say");
							genders.add("None of your business");
							if (t.config.easterEggs)
							{
								genders.add("Kanye West");
								genders.add("Cheese");
								genders.add("Raygun");
								if (t.config.GoTEggs)
								{
									genders.add("The Dothraki do not follow your Genders");
									genders.add("Khaleesi");
									genders.add("Dragon - Male");
									genders.add("Dragon - Female");
									genders.add("Dragon - Other");
									genders.add("Direwolf");
									genders.add("White Walker");
									genders.add("Child of the Forest");
								}
								if (t.config.ESEggs)
								{
									genders.add("Khajiit");
									genders.add("Dovahkiin");
									genders.add("Dovah");
									genders.add("Draugr");
								}
								if (t.config.schoolEggs)
								{
									genders.add("Student");
									genders.add("IB Student");
									genders.add("Teacher");
									genders.add("IB Teacher");
								}
							}
						}
						else
						{
							genders.add("Multiple");
							genders.add("In between");
							genders.add("Unsure");
							genders.add("None");
							genders.add("Prefer not to say");
							genders.add("Other");
						}
					}
					for (int i=0; i<genders.size(); ++i)
					{
						Button b=new Button(t);
						b.setText(genders.get(i));
						b.setOnClickListener(new OnClickListener ()
						{
							@Override
							public void onClick(View v)
							{
								v.setOnClickListener(null);

								t.user.gender=((TextView)v).getText().toString();
								t.th=new Thread (new Runnable ()
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
						l.addView(b);
					}
					genders.clear();
					if (t.config.customGender)
					{
						Button b=new Button (t);
						b.setText("Custom");
						b.setOnClickListener(new OnClickListener ()
						{
							@Override
							public void onClick (View v)
							{
								v.setOnClickListener(null);

								t.config.computerPaused=true;
								ScrollView sv=new ScrollView (t);
								int id=Build.VERSION.SDK_INT>=17 ? View.generateViewId() : 5;
								sv.setId(id);
								t.currentView=id;
								LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
								t.setContentView(sv);
								LinearLayout l=new LinearLayout(t);
								l.setOrientation(LinearLayout.VERTICAL);
								sv.addView(l);
								t.setUi();
								lp.topMargin=10;
								OnFocusChangeListener ofcl=new OnFocusChangeListener()
								{
									@Override
									public void onFocusChange(View v, boolean hasFocus)
									{
										if (hasFocus && t.config.fullscreen)
										{
											t.setUi();
											EditText ev=((EditText)t.findViewById(t.user.parsedGender));
											LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)ev.getLayoutParams();
											int result=0;
											int resourceId = t.getResources().getIdentifier("status_bar_height", "dimen", "android");
											if (resourceId > 0) {
												result = t.getResources().getDimensionPixelSize(resourceId);
											}
											lp.topMargin=result+5;
										}
									}
								};
								EditText ev=new EditText(t);
								t.user.parsedGender=999999999;
								ev.setId(t.user.parsedGender);
								ev.setOnFocusChangeListener(ofcl);
								ev.setBackgroundColor(Color.rgb(128,128,128));
								ev.setHint("What gender are you?");
								ev.setHintTextColor(Color.WHITE);
								ev.setTextColor(Color.BLACK);
								ev.setShadowLayer(5.0f,5,5,Color.WHITE);
								ev.setSingleLine(false);
								ev.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_AUTO_CORRECT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
								ev.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
								l.addView(ev, lp);
								if (t.config.addressGender)
								{
									ev=new EditText(t);
									lp=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
									lp.topMargin=20;
									t.user.weapon.type=7; // Bad form, but we won't have a real parsed weapon for a while... It can be temporary storage until then.
									ev.setId(t.user.weapon.type);
									ev.setOnFocusChangeListener(ofcl);
									ev.setBackgroundColor(Color.rgb(128,128,128));
									ev.setHint("What should I address one of your gender as?");
									ev.setHintTextColor(Color.WHITE);
									ev.setTextColor(Color.BLACK);
									ev.setShadowLayer(5.0f,5,5,Color.WHITE);
									ev.setSingleLine(false);
									ev.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_AUTO_CORRECT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
									ev.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
									l.addView(ev, lp);
								}
								Button b=new Button(t);
								b.setText("Submit");
								lp=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
								lp.topMargin=250;
								lp.gravity=Gravity.RIGHT;
								b.setOnClickListener(new OnClickListener()
								{
									@Override
									public void onClick (View v)
									{
										v.setOnClickListener(null);

										t.th=new Thread (new Runnable ()
										{
											@Override
											public void run ()
											{
												// Close the soft keyboard, now that there's nothing for it to write to.
												((InputMethodManager)t.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(t.findViewById(t.user.parsedGender).getWindowToken(), 0);

												t.user.gender=((TextView)t.findViewById(t.user.parsedGender)).getText().toString().trim();
												if (t.config.addressGender)
												{
													String str=((TextView)t.findViewById(t.user.weapon.type)).getText().toString().trim();
													if (str.startsWith("the ") || str.startsWith("The "))
														str=str.substring(4); // Trim off the 'the' to avoid the 'The The' quirk.
													t.user.genderAddress="The "+str;
												}
												runStage();
											}
										});
										t.th.start();
									}
								});
								l.addView(b, lp);
							}
						});
						l.addView(b);
					}
				}
			});
			break;
		case 2:
			t.th=new Thread (new Runnable()
			{
				@Override
				public void run()
				{
					if (t.config.gender && t.config.addressGender && t.user.parsedGender!=999999999)
						t.user.parseGenderAddress();
					t.say("I see.\n\n\tWell, "+t.user+", it is time to make a decision.\n\n\tAre you going to try to escape?");
				}
			});
			t.th.start();
			prepContinueButton();
			if (t.config.gender)
				t.user.parseGender();
			break;
		case 3:
			t.fadeout();
			t.runOnUiThread (new Runnable ()
			{
				@Override
				public void run()
				{
					LinearLayout l=prepInput();
					Button b=new Button (t);
					b.setText("Escape");
					b.setOnClickListener(new OnClickListener ()
					{
						@Override
						public void onClick(View v)
						{
							v.setOnClickListener(null);

							t.th.interrupt();
							t.th=new Thread (new Runnable ()
							{
								@Override
								public void run ()
								{
									inputted=0;
									runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button (t);
					b.setText("Stay");
					b.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick (View v)
						{
							v.setOnClickListener(null);

							t.th.interrupt();
							t.th=new Thread (new Runnable ()
							{
								@Override
								public void run ()
								{
									inputted=1;
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
		case 4:
			if (inputted==0)
			{
				if (onMove(99))
					return;
				t.th=new Thread (new Runnable()
				{
					@Override
					public void run()
					{
						String creature;
						boolean plural=false;
						if (t.gen.nextBoolean())
						{
							if (t.gen.nextBoolean())
								creature="troll";
							else
								creature="dinosaur";
						}
						else
						{
							if (t.gen.nextBoolean())
							{
								creature="alligator";
								plural=true;
							}
							else
								creature="goblin";
						}
						t.say("You start to escape...","But you see a shape.\n\tIt\'s "+(plural ? "an" : "a")+" "+creature+"!\n\n\tYou search for a weapon, but only find a stick.");
					}
				});
				t.th.start();
				prepContinueButton();
			}
			else
			{
				if (onMove(50))
					return;
				line=1;
				if (t.config.triggerEgg(.1))
				{
					t.th=new Thread (new Runnable()
					{
						@Override
						public void run()
						{
							t.say("You remain in the cave.","Despite the lack of any way to survive, you stubbornly stay in the cave, starving...\n\tSuddenly, Gandalf the Grey appears.\n\t\t\"Fly, you fool! You will die!!\"\nSince you make no move to escape, Gandalf sighs, and takes you with him.");
						}
					});
					t.th.start();
					prepContinueButton();
				}
				else
				{
					t.th=new Thread (new Runnable()
					{
						@Override
						public void run()
						{
							t.say("You stay in the cave...","...And starve to death because there\'s no food in the cave.");
						}
					});
					t.th.start();
					t.user.dead=true;
					prepContinueButton();
				}
			}
			break;
		case 5:
			if (line==0)
			{
				t.fadeout();
				t.runOnUiThread(new Runnable ()
				{
					@Override
					public void run ()
					{
						doCommitSuicide=true;
						LinearLayout l=prepInput();
						Button b=new Button(t);
						b.setText("Fight it with a stick");
						b.setOnClickListener(new OnClickListener ()
						{
							@Override
							public void onClick (View v)
							{
								v.setOnClickListener(null);

								t.th=new Thread (new Runnable ()
								{
									@Override
									public void run()
									{
										t.user.weapon.name="stick";
										t.user.weapon.type=Cweapon.TYPE_BLUNT;
										inputted=0;
										runStage();
									}
								});
								t.th.start();
							}
						});
						l.addView(b);
						b=new Button(t);
						b.setText("Fight it with your fists");
						b.setOnClickListener(new OnClickListener ()
						{
							@Override
							public void onClick (View v)
							{
								v.setOnClickListener(null);

								t.th=new Thread (new Runnable ()
								{
									@Override
									public void run()
									{
										t.user.weapon.type=Cweapon.TYPE_HAND_TO_HAND;
										inputted=0;
										runStage();
									}
								});
								t.th.start();
							}
						});
						l.addView(b);
						b=new Button(t);
						b.setText("Run away!!");
						b.setOnClickListener(new OnClickListener ()
						{
							@Override
							public void onClick (View v)
							{
								v.setOnClickListener(null);

								t.th=new Thread (new Runnable ()
								{
									@Override
									public void run()
									{
										inputted=1;
										runStage();
									}
								});
								t.th.start();
							}
						});
						l.addView(b);
					}
				});
			}
			else
			{
				t.th=new Thread (new Runnable()
				{
					@Override
					public void run()
					{
						t.say("You win!","You have won the game of life.");
					}
				});
				t.th.start();
				doCommitSuicide=true;
				prepContinueButton(onWin);
			}
			break;
		case 6:
			if (inputted==1)
			{
				if (onMove(78))
					return;
				t.th=new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						t.say("Run away!!!", "You try to run away from the creature, but it\'s too fast.");
					}
				});
				t.th.start();
				t.user.dead=true;
				prepContinueButton();
			}
			else
			{
				if (t.user.weapon.type==Cweapon.TYPE_HAND_TO_HAND)
				{
					if (t.determineUserDeath(.5))
					{
						t.th=new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								t.say("Fight with fists!","You punch it, but it eats you anyway.");
							}
						});
						t.th.start();
						prepContinueButton();
					}
					else
					{
						t.th=new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								t.say("Fight with fists", "You punch the monster, and barely manage to scare it.\n\tYou take a deep breath, and continue.");
							}
						});
						t.th.start();
						prepContinueButton();
					}
				}
				else
				{
					t.th=new Thread(new Runnable ()
					{
						@Override
						public void run ()
						{
							t.say("You grab the stick, and beat the creature until it leaves.");
						}
					});
					t.th.start();
					prepContinueButton();
				}
			}
			break;
		case 7:
			if (onMove(105))
				return;
			t.th=new Thread (new Runnable ()
			{
				@Override
				public void run ()
				{
					t.say ("You Escape!", "You escape from the cave.\n\n\tIt is a bright and sunny day outside.\n\n\tHowever, there is nothing around you besides sand and dust.\nIt would appear that you are in a desert.\n\n\n\tHowever, a while away, there is a lush valley, full of greenery. You decide to head for it.\n\n\tAlong the way, you find a foreboding cave. A sharp iron sword is sitting by the mouth of the cave, glimmering in the sunlight.");
				}
			});
			t.th.start();
			prepContinueButton();
			break;
		case 8:
			t.fadeout();
			t.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					LinearLayout l=prepInput();
					Button b=new Button (t);
					b.setText("Grab the sword and enter the cave");
					b.setOnClickListener(new OnClickListener()
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
									if (t.user.isArthur) {
										t.user.weapon.name="Excalibur"; // King Arthur gets the greatest of swords
										t.user.weapon.setCharacteristics(Cweapon.ACCURATE|Cweapon.CLOSE_RANGE|Cweapon.CLOSE_RANGE_ONLY|Cweapon.HIGH_CALIBER|Cweapon.HIGH_POWER_ROUNDS|Cweapon.LEGENDARY|Cweapon.LIGHT|Cweapon.ONE_ROUND_MAGAZINE|Cweapon.QUICK_RELOAD);
										t.user.weapon.strengthModifier=1+t.gen.nextDouble(); // Excalibur is in epic condition.
									}
									else {
										t.user.weapon.name = "sword";
										t.user.weapon.setCharacteristics(Cweapon.ACCURATE | Cweapon.CLOSE_RANGE | Cweapon.CLOSE_RANGE_ONLY | Cweapon.HIGH_CALIBER  | Cweapon.ONE_ROUND_MAGAZINE | Cweapon.QUICK_RELOAD);
										t.user.weapon.strengthModifier=-0.05*t.gen.nextDouble(); // The sword should be in poor condition
										// This exaggerates the difference between the sword and Excalibur
									}
									t.user.weapon.type = Cweapon.TYPE_SHARP;
									inputted=0;
									runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button (t);
					b.setText("Ignore the sword and enter the cave");
					b.setOnClickListener(new OnClickListener()
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
									inputted=0;
									runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button (t);
					b.setText("Ignore the sword and continue away from the cave");
					b.setOnClickListener(new OnClickListener()
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
									inputted=1;
									runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button (t);
					b.setText("Grab the sword and continue away from the cave");
					b.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							v.setOnClickListener(null);

							t.th.interrupt();
							t.th=new Thread(new Runnable()
							{
								@Override
								public void run() {
									if (t.user.isArthur) {
										t.user.weapon.name="Excalibur"; // King Arthur gets the greatest of swords
										t.user.weapon.setCharacteristics(Cweapon.ACCURATE|Cweapon.CLOSE_RANGE|Cweapon.CLOSE_RANGE_ONLY|Cweapon.HIGH_CALIBER|Cweapon.HIGH_POWER_ROUNDS|Cweapon.LEGENDARY|Cweapon.LIGHT|Cweapon.ONE_ROUND_MAGAZINE|Cweapon.QUICK_RELOAD);
										t.user.weapon.strengthModifier=1+t.gen.nextDouble(); // Excalibur is in epic condition.
									}
									else {
										t.user.weapon.name = "sword";
										t.user.weapon.setCharacteristics(Cweapon.ACCURATE | Cweapon.CLOSE_RANGE | Cweapon.CLOSE_RANGE_ONLY | Cweapon.HIGH_CALIBER  | Cweapon.ONE_ROUND_MAGAZINE | Cweapon.QUICK_RELOAD);
										t.user.weapon.strengthModifier=-0.05*t.gen.nextDouble(); // The sword should be in poor condition
										// This exaggerates the difference between the sword and Excalibur
									}
									t.user.weapon.type = Cweapon.TYPE_SHARP;
									inputted=1;
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
		case 9:
			if (inputted==0)
			{
				if (onMove(67))
					return;
				t.user.dead=(t.user.weapon.type<Cweapon.TYPE_SHARP);
				t.th=new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						String title="You enter the cave";
						if (t.user.dead)
							t.say(title,"Ignoring the sword, you enter.\n\tImmediately, the creature from the last cave attacks you. It is incredibly angry, and no "+t.user.weapon+" will scare it off.");
						else {
							if (t.user.isArthur)
								t.say(title,"You take the sword, and examine it.\n\n\tIt seemed to call to you like an old friend, and suddenly you recognize it: It is "+t.user.weapon+"!\n\n\n\tYou take a moment with the blade, twirling it about to remember.\n\n\tAll of a sudden, a shape approaches: You recognize it as the creature from the last cave.\n\tYou bring "+t.user.weapon+" to bear.");
							else
								t.say(title, "With the sword in hand, you enter.\n\tImmediately, the creature that fled the last cave attacks you. It is incredibly angry, and a stick will not be enough to make it flee.\n\n\tLuckily, you had the prudence to grab the " + t.user.weapon + ".\n\tYou ready it.");
						}
					}
				});
				t.th.start();
				prepContinueButton();
			}
			else
			{
				stage=25;
				runStage();
				return;
			}
			break;
		case 10:
			t.fadeout();
			t.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					doCommitSuicide=true;
					LinearLayout l=prepInput();
					Button b=new Button(t);
					b.setText("Fight the monster");
					b.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							v.setOnClickListener(null);

							t.th.interrupt();
							t.th=new Thread (new Runnable()
							{
								@Override
								public void run()
								{
									inputted=0;
									runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Run away!!");
					b.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick (View v)
						{
							v.setOnClickListener(null);

							t.th.interrupt();
							t.th=new Thread (new Runnable()
							{
								@Override
								public void run()
								{
									inputted=1;
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
		case 11:
			if (onMove(45))
				return;
			t.user.dead=(inputted!=0);
			t.th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					if (t.user.dead)
						t.say("Run away!!!", "You turn to run, but the monster is far too fast.");
					else
						t.say("You fight the monster", "The monster lunges at you, but you swiftly dodge it, turn, and behead it.\n\n\n\tCovered in blood, you contemplate whether you should continue.");
				}
			});
			t.th.start();
			prepContinueButton();
			break;
		case 12:
			t.fadeout();
			t.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					LinearLayout l=prepInput();
					Button b=new Button (t);
					b.setText("Continue into the cave");
					b.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick (View v)
						{
							v.setOnClickListener(null);

							t.th.interrupt();
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
					l.addView(b);
					b=new Button(t);
					b.setText("Escape from the cave");
					b.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick (View v)
						{
							v.setOnClickListener(null);

							t.th.interrupt();
							t.th=new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									stage=24;
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
		case 13:
			if (onMove(15+t.gen.nextInt(20)))
				return;
			t.th=new Thread (new Runnable()
			{
				@Override
				public void run()
				{
					t.say("Forward into Dusk","You continue into the cave.\n\tYou encounter a rather horrifying monster, one that chills you to the bone.\n\n\tIt seems to be extremely afraid of the light.\n\n\n\tUnfortunately, there is no way around it.");
				}
			});
			t.th.start();
			prepContinueButton();
			break;
		case 14:
			t.fadeout();
			t.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					LinearLayout l=prepInput();
					Button b=new Button(t);
					b.setText("Fight the grue");
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
									runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Flee from the grue");
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
									stage=24;
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
		case 15:
			if (t.user.weapon.characteristicSet(Cweapon.LEGENDARY)) // Check for Excalibur
				t.determineUserDeath(1,5); // Slightly reduced odds of death
			else
				t.determineUserDeath(1, 3); // Standard, "Traditional" weighting
			t.th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					if (t.user.dead)
					{
						if (t.config.specMon)
						{
							t.user.dead=false;
							line=1;
						}
						t.say("You lunge at the grue", "But the grue is too strong, and you get eaten.");
					}
					else
						t.say("You swing your sword...", "...and get in a lucky shot. The grue\'s head falls to the floor.\n\n\tYou again consider whether you should continue.");
				}
			});
			t.th.start();
			prepContinueButton();
			break;
		case 16:
			if (line==1)
			{
				eatenByGrue();
				prepContinueButton();
				t.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						((TextView)t.findViewById(R.id.gameContinueButton)).setTextColor(Color.GREEN);
					}
				});
			}
			else
			{
				t.fadeout();
				t.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						LinearLayout l=prepInput();
						Button b=new Button(t);
						b.setText("Continue into the depths of the cave");
						b.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick (View v)
							{
								v.setOnClickListener(null);
								t.th.interrupt();
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
						l.addView(b);
						b=new Button(t);
						b.setText("Escape from the monsters yet to be found within the cave");
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
										stage=24;
										runStage();
									}
								});
								t.th.start();
							}
						});
						l.addView(b);
					}
				});
			}
			break;
		case 17:
			if (onMove(5+t.gen.nextInt(10)))
				return;
			t.th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					t.say("Into the Darkness of Death", "You continue into the foreboding darkness of the cave, trembling in fear, wondering what the next monster to come far too close to ending your life will be.\n\n\n\n\tYour fears are answered in a horrifying way when you see an enormous, hulking, towering shape in front of you.\n\n\tIt lumbers toward you, the ground quaking as it stomps.\n\n\tYou quickly contemplate taking flight.");
				}
			});
			t.th.start();
			prepContinueButton();
			break;
		case 18:
			t.fadeout();
			t.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					LinearLayout l=prepInput();
					Button b=new Button(t);
					b.setText("Run away before you get killed");
					b.setTextColor(Color.rgb(250, 255, 250));
					b.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							v.setOnClickListener(null);

							t.th.interrupt();
							t.th=new Thread (new Runnable()
							{
								@Override
								public void run()
								{
									stage=24;
									runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Challenge the utter despotic power of the creature before you");
					b.setTextColor(Color.rgb(255, 250, 250));
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
		case 19:
			if (t.user.weapon.characteristicSet(Cweapon.LEGENDARY)) // Check for Excalibur
				t.determineUserDeath(3, 10); // Slightly less than one third
			else
				t.determineUserDeath(2, 3); // Two thirds "Traditional" weighting
			t.th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					if (t.user.dead)
						t.say("You fight the monster...", "... But it flattens you.");
					else
						t.say("David vs. Goliath", "David killed Goliath, and you killed.... Whatever that was.\n\n\tCollecting yourself as a massive thud rolls through the cave and the monster lies vanquished before you, you again consider whether you should continue further into the depths of the frightening cave.");
				}
			});
			t.th.start();
			prepContinueButton();
			break;
		case 20:
			t.fadeout();
			t.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					doCommitSuicide=t.config.easterEggs;
					LinearLayout l=prepInput();
					Button b=new Button(t);
					b.setText("Get out of the cave now, before it is too late!");
					b.setPadding(5, 5, 5, 5);
					b.setTextColor(Color.rgb(245, 255, 245));
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
									stage=24;
									runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Bravely, yet perhaps foolishly, continue onwards towards the horrors you know not of");
					b.setTextColor(Color.rgb(245, 235, 235));
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
		case 21:
			if (line==1)
			{
				t.fadeout();
				t.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						doCommitSuicide=true;
						LinearLayout l=prepInput();
						Button b=new Button(t);
						b.setText("Ignore the weapon and exit the cave");
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
										runStage();
									}
								});
								t.th.start();
							}
						});
						l.addView(b);
						b=new Button(t);
						b.setText("Grab the superweapon and exit the cave");
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
										t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_FUTURE, Cweapon.ACCURATE|Cweapon.AUTOMATIC|Cweapon.CLOSE_RANGE|Cweapon.EXPLOSIVE|Cweapon.HIGH_POWER_ROUNDS|Cweapon.LARGE_MAGAZINE|Cweapon.LEGENDARY|Cweapon.LIGHT|Cweapon.LONG_RANGE|Cweapon.SLOW_RELOAD, "raygun", null));
										t.user.weapon.strengthModifier=1;
										runStage();
									}
								});
								t.th.start();
							}
						});
						l.addView(b);
					}
				});
			}
			else
			{
				if (onMove(50))
					return;
				t.th=new Thread (new Runnable()
				{
					@Override
					public void run()
					{
						t.say("You come to a dead end in the cave, sighing with relief.\n\n\tOn the ground before you, there is a semi-automatic pistol.");
					}
				});
				t.th.start();
				prepContinueButton();
			}
			break;
		case 22:
			if (line==1)
			{
				line=0;
				if (t.user.weapon.type==Cweapon.TYPE_FUTURE)
				{
					t.th=new Thread (new Runnable()
					{
						@Override
						public void run()
						{
							t.say("The "+t.capitalize(t.user.weapon.name), "You take the "+t.user.weapon+", considering it.\n\n\tSuddenly, a grue appears!\n\n\n\n\n\tWithout thinking, you aim the "+t.user.weapon+" at the grue, and pull the trigger.\n\n\tA bright green bolt shoots out of the superweapon, and where the grue was, there is only ash.\n\n\n\tYou consider the "+t.user.weapon+", thinking you\'ll never need to worry about any monster ever again.\n\n\tGrinning, you set off to leave the cave.");
						}
					});
				}
				else
				{
					t.th=new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							t.user.dead=true;
							if (t.config.specMon)
							{
								t.user.dead=false;
								line=1;
							}
							t.say("You ignore the superweapon, turning to head out of the cave.\n\n\tSuddenly, a grue appears!\n\n\tYou try to bring your sword to bear, but it\'s too slow.");
						}
					});
				}
				t.th.start();
				prepContinueButton();
			}
			else
			{
				t.fadeout();
				t.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						LinearLayout l=prepInput();
						Button b=new Button(t);
						b.setText("Grab the pistol and escape the cave");
						b.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick (View v)
							{
								v.setOnClickListener(null);

								t.th.interrupt();
								t.th=new Thread(new Runnable()
								{
									@Override
									public void run()
									{
										t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.CLOSE_RANGE|Cweapon.LARGE_MAGAZINE|Cweapon.LIGHT|Cweapon.QUICK_RELOAD, "pistol", null));
										runStage();
									}
								});
								t.th.start();
							}
						});
						l.addView(b);
						b=new Button(t);
						b.setText("Foolishly ignore the pistol and leave the cave");
						b.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick (View v)
							{
								v.setOnClickListener(null);

								t.th.interrupt();
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
						l.addView(b);
					}
				});
			}
			break;
		case 23:
			if (line==1)
			{
				eatenByGrue();
				t.frameTick();
				prepContinueButton();
				t.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						((TextView)t.findViewById(R.id.gameContinueButton)).setTextColor(Color.GREEN);
					}
				});
				break;
			}
			++stage;
			// Fall through
		case 24:
			if (onMove(70))
				return;
			t.th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					t.say("You escape!", "You leave the cave, and gaze towards the lush valley in the distance.");
				}
			});
			t.th.start();
			prepContinueButton();
			break;
		case 25:
			if (onMove(90))
				return;
			t.th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					String address;
					String pronoun;
					if (t.gen.nextBoolean())
					{
						address="him";
						pronoun="he";
					}
					else
					{
						address="her";
						pronoun="she";
					}
					t.say("Continuation", "You continue, towards the lush valley.\n\n\tAlong the way, you encounter an archer.\n\tBefore you\'re able to greet "+address+", "+pronoun+" readies "+address+"self to shoot you.");
				}
			});
			t.th.start();
			prepContinueButton();
			break;
		case 26:
			t.fadeout();
			t.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					doCommitSuicide=true;
					LinearLayout l=prepInput();
					Button b=new Button(t);
					String action;
					switch(t.user.weapon.type)
					{
					case Cweapon.TYPE_HAND_TO_HAND:
					case Cweapon.TYPE_BLUNT:
						action="Fight";
						break;
					case Cweapon.TYPE_SHARP:
						action="Stab";
						break;
					case Cweapon.TYPE_MODERN:
					case Cweapon.TYPE_FUTURE:
						action="Shoot";
						break;
					default:
						action="Kill";
					}
					b.setText(action+" the archer");
					b.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick (View v)
						{
							v.setOnClickListener(null);
							t.th.interrupt();
							t.th=new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									inputted=0;
									runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					t.user.weapon.addSwapperTo(l);
					b=new Button(t);
					b.setText("Run away");
					b.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick (View v)
						{
							v.setOnClickListener(null);
							t.th.interrupt();
							t.th=new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									inputted=1;
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
		case 27:
			if (inputted==0)
			{
				t.th=new Thread (new Runnable()
				{
					@Override
					public void run()
					{
						switch (t.user.weapon.type)
						{
						case Cweapon.TYPE_HAND_TO_HAND:
							t.user.dead=true;
							t.say("Fistfight!", "You try to get in punching range of the archer, but an arrow appears in your chest before you get close.");
							break;
						case Cweapon.TYPE_BLUNT:
							t.user.dead=true;
							t.say(t.capitalize(t.user.weapon.name)+"fight", "You run up to the archer, barely managing to get in a swing, when the archer shoots you.");
							break;
						case Cweapon.TYPE_SHARP:
							if (t.user.weapon.characteristicSet(Cweapon.LEGENDARY)) { // Excalibur check
								t.determineUserDeath(.2); // Excalibur weighting
								if (t.user.dead)
									t.say("The Might of "+t.user.weapon, "With "+t.user.weapon+" in hand, you charge the archer.\n\n\tA few arrows fly towards you, and one strikes you in the stomach, stopping you in your tracks.\n\n\t"+t.user.weapon+" falls beside you as your eyes fixate upon it, barely seeing the archer walk up beside you to draw a killing arrow from his quiver.\n\n\n\tAs you watch, "+t.user.weapon+" is surrounded by a pool of growing, shimmering water, forming a mirror.\n\n\tAs the blade is enveloped, it begins to dissolve, becoming one with the water, until it is but a part of the pool.\n\n\tAs the archer shoots an arrow into your head, you watch the pool that was "+t.user.weapon+" dissolve into the soil, lost once more in the oceans of time.");
								else
									t.say("The Might of "+t.user.weapon, "With "+t.user.weapon+" in hand, you charge the archer.\n\n\tA few arrows fly towards you, but you dodge one half, and "+(t.gen.nextBoolean() ? "swiftly" : "deftly")+" deflect the other.\n\n\tHaving closed the distance between yourself and the archer, you swing your legendary blade at your enemy.\n\n\n\tThe archer deftly rolls away, but "+t.user.weapon+" knows what to do, adjusting its swing, pulling you along, until you cleave the archer in two.");
							}
							else {
								t.determineUserDeath(.5); // Traditional weighting
								t.say(t.capitalize(t.user.weapon.name) + " battle!", "You run up to the archer, and lunge!\n\n\t" + (t.user.dead ? "Unfortunately, you miss, and get shot in the " + (t.config.ESEggs && t.config.triggerEgg(.9) ? "knee." : "back.") : "You connect!\n\n\tThe archer falls, dead.\n\n\tThe bow drops to the ground.\n\tYou eye it."));
							}
							break;
						case Cweapon.TYPE_MODERN:
							t.determineUserDeath(1, 6);
							t.say(t.capitalize(t.user.weapon.name)+" vs. bow", "You carefully take aim at the archer, and pull the trigger.\n\n\t"+(t.user.dead ? "Unfortunately, you miss, and get shot before you can aim again." : (t.gen.nextBoolean() ? "A neat hole appears in the archer\'s chest." : "You wing the archer, the bow swinging off it\'s aim, and then you shoot again, scoring a fatal hit.")+"\n\n\tThe bow falls to the ground.\n\tYou eye it, but decide that you prefer your "+t.user.weapon+"."));
							break;
						case Cweapon.TYPE_FUTURE:
							t.say(t.capitalize(t.user.weapon.name)+" Kill", "You calmly aim at the archer, and pull the trigger.\n\n\tWhere the archer was standing, there is only ash.");
							break;
						default:
							t.user.dead=true;
							t.logError("Unknown weapon code: "+t.user.weapon.type+". Weapon: "+t.user.weapon+".");
							t.say("Your weapon is unknowable to the mere humans, so...");
						}
					}
				});
				t.th.start();
				if (t.user.weapon.type<Cweapon.TYPE_HAND_TO_HAND || t.user.weapon.type==Cweapon.TYPE_ARCHERY || t.user.weapon.type>Cweapon.TYPE_FUTURE)
					prepContinueButton(new OnClickListener ()
					{
						@Override
						public void onClick (View v)
						{
							t.user.weapon.type=Cweapon.TYPE_USED_FOR_CONVENIENCE;
							t.user.commitSuicide();
						}
					});
				else
					prepContinueButton();
			}
			else
			{
				if (onMove(65))
					return;
				t.user.dead=true;
				t.th=new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						t.say("Run away!!!", "You try to run away, but you "+(t.config.easterEggs && t.config.ESEggs ? "take an arrow in the knee." : "get shot before you can get away."));
					}
				});
				t.th.start();
				prepContinueButton();
			}
			break;
		case 28:
			if (t.user.weapon.type==Cweapon.TYPE_MODERN || t.user.weapon.type==Cweapon.TYPE_FUTURE)
			{
				++stage;
				runStage();
				return;
			}
			t.fadeout();
			t.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					LinearLayout l=prepInput("Do you take the bow?");
					Button b=new Button(t);
					b.setText("Yes");
					b.setOnClickListener(new OnClickListener ()
					{
						@Override
						public void onClick (View v)
						{
							v.setOnClickListener(null);

							t.th.interrupt();
							t.th=new Thread(new Runnable()
							{
								@Override
								public void run () {
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_ARCHERY, Cweapon.BOLT_FIRE|Cweapon.CLOSE_RANGE|Cweapon.LOW_POWER|Cweapon.ONE_ROUND_MAGAZINE, 0, "bow", null));
									runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("No");
					b.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick (View v)
						{
							v.setOnClickListener(null);

							t.th.interrupt();
							t.th=new Thread (new Runnable()
							{
								@Override
								public void run ()
								{
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
		case 29:
			if (onMove(80))
				return;
			t.th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					t.say("Weaponry is King", "Moving away from the cave yet again, you encounter an abandoned gunstore.\n\n\tInside, there are two shapes.");
				}
			});
			t.th.start();
			prepContinueButton();
			break;
		case 30:
			t.fadeout();
			t.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					LinearLayout l=prepInput();
					Button b=new Button(t);
					b.setText("Enter the store");
					b.setOnClickListener(new OnClickListener()
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
									runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Continue away from the gunstore");
					b.setOnClickListener(new OnClickListener()
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
									stage=37;
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
		case 31:
			if (onMove(75+t.gen.nextInt(75)))
				return;
			t.th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					String monster;
					if (t.gen.nextBoolean())
					{
						if (t.gen.nextBoolean())
							monster="alligators";
						else
							monster="trolls";
					}
					else
					{
						if (t.gen.nextBoolean())
							monster="goblins";
						else
							monster="dinosaurs";
					}
					t.say("The Gunstore", "Inside the gunstore, you\'re finally able to identify the shapes...\n\tThey\'re "+monster+"!\n\n\tYou eye the shelves, rife with weaponry you could have, if not for the "+monster+"...");
				}
			});
			t.th.start();
			prepContinueButton();
			break;
		case 32:
			t.fadeout();
			t.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					doCommitSuicide=true;
					LinearLayout l=prepInput();
					Button b=new Button(t);
					b.setText("Fight the deadly creatures");
					b.setOnClickListener(new OnClickListener ()
					{
						@Override
						public void onClick (View v)
						{
							v.setOnClickListener(null);

							t.th.interrupt();
							t.th=new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									inputted=0;
									runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					t.user.weapon.addSwapperTo(l);
					b=new Button(t);
					b.setText("Run away!!");
					b.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							v.setOnClickListener(null);

							t.th.interrupt();
							t.th=new Thread(new Runnable()
							{
								@Override
								public void run ()
								{
									inputted=1;
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
		case 33:
			t.user.dead=(inputted==1);
			if (!t.user.dead && onMove(60))
				return;
			if (!t.user.dead && t.user.weapon.type==Cweapon.TYPE_ARCHERY)
			{
				archeryMinigame((int)Math.round(25*(.5+(.5*t.config.difficultyMult))), 5+t.gen.nextGaussian());
				t.th=new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						t.say("Archery", "You shoot one creature, felling it. The other lunges towards you...\n\tYou shoot an arrow at it, "+(t.user.dead ? "but miss." : "and hit it, square in between the eyes.\n\n\tIt falls to the ground, dead."));
					}
				});
				t.th.start();
				if (!t.user.dead)
					t.user.gold.amount=(int)((99*Math.abs(t.gen.nextGaussian()))+1);
				prepContinueButton();
				break;
			}
			t.th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					if (t.user.dead)
						t.say("Run away!!", "You try to run, but the closer creature catches you in the doorway, and drags you back.\n\n\tThey make a delicious meal of you.");
					else
					{
						switch (t.user.weapon.type)
						{
						case Cweapon.TYPE_SHARP:
							t.determineUserDeath(.75);
							if (t.user.dead)
								t.say ("Swordfight!", "You slash at one creature, then the other, trying to keep the creatures away from you, but they overwhelm you.");
							else
								t.say ("Slash and Stab", "You slash at one creature, barely slicing some flesh off.\n\tIt jumps back, stunned by the injury.\n\tYou take the opportunity to focus on the other one, barely managing to defeat it in time to swing around and duel the other.\n\n\tAfter killing it too, you stand in the center of the store, blood dripping off you, examining the shelves.");
							break;
						case Cweapon.TYPE_ARCHERY:
							break;
						case Cweapon.TYPE_MODERN:
							if (t.determineUserDeath(.25))
								t.say("A hit and a miss", "You place one shot cleanly between the eyes of the nearest creature.\n\tIt falls, dead.\n\n\tThe second creature lunges toward you, and you fire, hitting it in the shoulder, failing to kill it.");
							else
								t.say("Sharpshooter", "You cleanly shoot the near creature between the eyes, killing it instantly.\n\n\tYou spin around, and quickly shoot the far creature.\n\tIt stumbles, and you fire another round into it\'s skull.");
							break;
						case Cweapon.TYPE_FUTURE:
							t.say(t.capitalize(t.user.weapon.name)+" kill", "You level your "+t.user.weapon.name+" at the near creature, calmly turning it to ash.\n\n\tThe second creature seems frightened.\n\n\n\tNo matter. You burn it too.");
							break;
						default:
							t.say("Your weapon is unknown to this universe as of the time being, so...");
							t.user.weapon.type=Cweapon.TYPE_USED_FOR_CONVENIENCE;
							t.user.commitSuicide();
						}
					}
				}
			});
			t.th.start();
			Thread.yield();
			if (!t.user.dead)
			{
				Random gen=new Random();
				t.user.gold.amount=(int)((99*Math.abs(gen.nextGaussian()))+1); // If the user is still alive,then they're going to the gunstore. It will be more persistent here.
			}
			prepContinueButton();
			break;
		case 34:
			if (inputted!=-1 && onMove(90))
				return;
			t.th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					if (inputted==-1)
						t.say("With the monsters defeated, you clean their remnants off of you, then survey your prize, walls and walls full of weapons"+(t.user.gold.amount==0 ? "" : ", and the contents of the cash register"+(t.user.gold.amount==1 ? ": " : ", which is oddly full of ")+t.user.gold)+".\n\n\tYou decide to grab a weapon, then depart.");
					else
						t.say("Return", "You return to the gunstore.\n\n\tLuckily, it\'s still lacking as far as things that want to kill and eat you are concerned.\n\n\tYou look to the walls, filled with more weapons than you can count, again resolving to grab one, then go back to the valley.");
				}
			});
			t.th.start();
			prepContinueButton();
			break;
		case 35:
			t.fadeout();
			t.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					doCommitSuicide=true;
					LinearLayout l=prepInput("Choose your weapon");
					Button b=new Button(t);
					b.setText("Combat knife");
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_SHARP, Cweapon.ACCURATE|Cweapon.CLOSE_RANGE|Cweapon.HIGH_POWER_ROUNDS|Cweapon.LEGENDARY|Cweapon.LIGHT|Cweapon.QUICK_RELOAD|Cweapon.WEAK_ROUNDS, .05, "combat knife", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Crossbow");
					b.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick (View v)
						{
							v.setOnClickListener(null);
							t.th.interrupt();
							t.th=new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_ARCHERY, Cweapon.ACCURATE|Cweapon.BOLT_FIRE|Cweapon.CLOSE_RANGE|Cweapon.HIGH_POWER_ROUNDS|Cweapon.ONE_ROUND_MAGAZINE|Cweapon.SLOW_RELOAD, .1, "crossbow", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Composite bow");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_ARCHERY, Cweapon.BOLT_FIRE|Cweapon.HIGH_POWER_ROUNDS|Cweapon.LIGHT|Cweapon.LONG_RANGE|Cweapon.LOW_POWER|Cweapon.ONE_ROUND_MAGAZINE, .025, "composite bow", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					if (t.user.weapon.type!=Cweapon.TYPE_MODERN)
					{
						b.setText("Pistol");
						b.setOnClickListener(new OnClickListener()
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
										t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.CLOSE_RANGE|Cweapon.HIGH_CALIBER|Cweapon.LARGE_MAGAZINE|Cweapon.LIGHT|Cweapon.QUICK_RELOAD, "pistol", null));
										t.game.runStage();	
									}
								});
								t.th.start();
							}
						});
						l.addView(b);
						b=new Button(t);
					}
					b.setText("Revolver");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.CLOSE_RANGE|Cweapon.HIGH_CALIBER|Cweapon.LIGHT|Cweapon.SLOW_RELOAD, -.01, t.gen.nextBoolean() ? "revolver" : "six-shot", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("M16 Assault Rifle");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.AUTOMATIC|Cweapon.LARGE_MAGAZINE|Cweapon.LONG_RANGE|Cweapon.QUICK_RELOAD, .1, "M16", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("M4A1 Assault Rifle");
					b.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick (View v)
						{
							v.setOnClickListener(null);

							t.th.interrupt();
							t.th=new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.AUTOMATIC|Cweapon.CLOSE_RANGE|Cweapon.LARGE_MAGAZINE|Cweapon.LIGHT|Cweapon.QUICK_RELOAD, .05, "M4", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("AK47 Assault Rifle");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.AUTOMATIC|Cweapon.HIGH_CALIBER|Cweapon.LARGE_MAGAZINE|Cweapon.LEGENDARY|Cweapon.LONG_RANGE|Cweapon.QUICK_RELOAD, .1, t.gen.nextBoolean() ? "AK47" : (t.gen.nextBoolean() ? "AK" : "Kalashnikov"), null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("AK74 Assault Rifle");
					b.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick (View v)
						{
							v.setOnClickListener(null);

							t.th.interrupt();
							t.th=new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.AUTOMATIC|Cweapon.CLOSE_RANGE|Cweapon.LARGE_MAGAZINE|Cweapon.QUICK_RELOAD, -.1, "AK74", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("M14 Designated Marksman\'s Rifle");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.HIGH_CALIBER|Cweapon.LARGE_MAGAZINE|Cweapon.LONG_RANGE|Cweapon.SLOW_RELOAD, .075, "M14", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("FN FAL Designated Marksman\'s Rifle");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.HIGH_POWER_ROUNDS|Cweapon.HIGH_RECOIL|Cweapon.LARGE_MAGAZINE|Cweapon.LONG_RANGE|Cweapon.QUICK_RELOAD, -.025, "FN FAL", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Dragunov SVU Designated Marksman\'s Rifle");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.CLOSE_RANGE|Cweapon.HIGH_POWER_ROUNDS|Cweapon.LARGE_MAGAZINE|Cweapon.LONG_RANGE|Cweapon.LOW_POWER|Cweapon.QUICK_RELOAD, .01, "SVU", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Zastava M76 Designated Marksman\'s Rifle");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.CLOSE_RANGE|Cweapon.HIGH_CALIBER|Cweapon.HIGH_POWER_ROUNDS|Cweapon.LARGE_MAGAZINE|Cweapon.LEGENDARY|Cweapon.LONG_RANGE|Cweapon.QUICK_RELOAD, "Zastava", null));
									t.user.weapon.strengthModifier=.25; // Here to circumvent the block on construction of weapons this strong
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Barrett M95 Sniper Rifle");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.CUMBERSOME|Cweapon.HIGH_CALIBER|Cweapon.HIGH_POWER_ROUNDS|Cweapon.LARGE_MAGAZINE|Cweapon.LONG_RANGE|Cweapon.QUICK_RELOAD, .05, "Barrett .50", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Barrett XM109 Sniper Rifle");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.BOLT_FIRE|Cweapon.CUMBERSOME|Cweapon.EXPLOSIVE|Cweapon.HIGH_CALIBER|Cweapon.HIGH_POWER_ROUNDS|Cweapon.HIGH_RECOIL|Cweapon.LONG_RANGE|Cweapon.LONG_RANGE_ONLY|Cweapon.QUICK_RELOAD, .1, "Barrett grenade-firing sniper rifle", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("L115A3 AWM Sniper Rifle");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.BOLT_FIRE|Cweapon.HIGH_CALIBER|Cweapon.HIGH_POWER_ROUNDS|Cweapon.HIGH_RECOIL|Cweapon.LONG_RANGE|Cweapon.LONG_RANGE_ONLY|Cweapon.QUICK_RELOAD, .2, "L115", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Anzio 20mm Anti-Material rifle");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.BOLT_FIRE|Cweapon.CUMBERSOME|Cweapon.EXPLOSIVE|Cweapon.HIGH_CALIBER|Cweapon.HIGH_POWER_ROUNDS|Cweapon.HIGH_RECOIL|Cweapon.LONG_RANGE|Cweapon.LONG_RANGE_ONLY|Cweapon.QUICK_RELOAD, .25, "extra heavy rifle", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("P90 submachine gun");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.AUTOMATIC|Cweapon.CLOSE_RANGE|Cweapon.CLOSE_RANGE_ONLY|Cweapon.LARGE_MAGAZINE|Cweapon.LIGHT|Cweapon.LOW_CALIBER|Cweapon.QUICK_RELOAD, .5, "P90", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("MP5K submachine gun");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.AUTOMATIC|Cweapon.CLOSE_RANGE|Cweapon.CLOSE_RANGE_ONLY|Cweapon.HIGH_RECOIL|Cweapon.LARGE_MAGAZINE|Cweapon.LIGHT|Cweapon.LOW_POWER|Cweapon.QUICK_RELOAD, .1, "MP5K", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("UMP45 Submachine gun");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.AUTOMATIC|Cweapon.CLOSE_RANGE|Cweapon.HIGH_CALIBER|Cweapon.HIGH_RECOIL|Cweapon.LARGE_MAGAZINE|Cweapon.LIGHT|Cweapon.LOW_POWER|Cweapon.QUICK_RELOAD, .02, "UMP45", null));
									t.game.runStage(); 
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Thompson Submachine gun");
					b.setOnClickListener(new OnClickListener()
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
									String weapon;
									if (t.gen.nextBoolean())
									{
										if (t.gen.nextBoolean())
											weapon="Annihilator";
										else
											weapon="Tommy gun";
									}
									else
									{
										if (t.gen.nextBoolean())
											weapon="M1928";
										else
											weapon="Thompson";
									}
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ANCIENT|Cweapon.AUTOMATIC|Cweapon.CLOSE_RANGE|Cweapon.HIGH_CALIBER|Cweapon.LARGE_MAGAZINE|Cweapon.LIGHT|Cweapon.LOW_POWER|Cweapon.QUICK_RELOAD, .1, weapon, null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Uzi submachine gun");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.AUTOMATIC|Cweapon.CLOSE_RANGE|Cweapon.HIGH_POWER_ROUNDS|Cweapon.HIGH_RECOIL|Cweapon.LARGE_MAGAZINE|Cweapon.LEGENDARY|Cweapon.LIGHT|Cweapon.QUICK_RELOAD, .075, "Uzi", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("AA-12 Shotgun");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.AUTOMATIC|Cweapon.CLOSE_RANGE|Cweapon.CLOSE_RANGE_ONLY|Cweapon.HIGH_CALIBER|Cweapon.HIGH_POWER_ROUNDS|Cweapon.LIGHT|Cweapon.QUICK_RELOAD, -.01, "AA-12", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("SPAS-12");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.CLOSE_RANGE|Cweapon.CLOSE_RANGE_ONLY|Cweapon.HIGH_CALIBER|Cweapon.HIGH_POWER_ROUNDS|Cweapon.LEGENDARY|Cweapon.LIGHT|Cweapon.SLOW_RELOAD, .05, "SPAS-12", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Remington 870 Shotgun");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.BOLT_FIRE|Cweapon.CLOSE_RANGE|Cweapon.CLOSE_RANGE_ONLY|Cweapon.HIGH_CALIBER|Cweapon.HIGH_POWER_ROUNDS|Cweapon.LIGHT|Cweapon.SLOW_RELOAD, .1, "Remington 870", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("KS-23");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.BOLT_FIRE|Cweapon.CLOSE_RANGE|Cweapon.CLOSE_RANGE_ONLY|Cweapon.HIGH_CALIBER|Cweapon.HIGH_POWER_ROUNDS|Cweapon.HIGH_RECOIL|Cweapon.LIGHT|Cweapon.SLOW_RELOAD, .15, "KS-23", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Custom-made 3 gauge shotgun");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.BOLT_FIRE|Cweapon.CLOSE_RANGE|Cweapon.CLOSE_RANGE_ONLY|Cweapon.EXPLOSIVE|Cweapon.HIGH_CALIBER|Cweapon.HIGH_POWER_ROUNDS|Cweapon.HIGH_RECOIL|Cweapon.SLOW_RELOAD, .1, "custom shotgun", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("M27-IAR");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.AUTOMATIC|Cweapon.HIGH_POWER_ROUNDS|Cweapon.LARGE_MAGAZINE|Cweapon.LONG_RANGE, .05, "M27", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("HK21");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.AUTOMATIC|Cweapon.HIGH_RECOIL|Cweapon.LARGE_MAGAZINE|Cweapon.LONG_RANGE, -.075, "HK21", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("LSAT");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.AUTOMATIC|Cweapon.HIGH_CALIBER|Cweapon.LARGE_MAGAZINE|Cweapon.LEGENDARY|Cweapon.LIGHT|Cweapon.LONG_RANGE, .075, "LSAT", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("M60");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.AUTOMATIC|Cweapon.CLOSE_RANGE|Cweapon.CUMBERSOME|Cweapon.HIGH_CALIBER|Cweapon.HIGH_POWER_ROUNDS|Cweapon.HIGH_RECOIL|Cweapon.LARGE_MAGAZINE|Cweapon.LONG_RANGE|Cweapon.SLOW_RELOAD, .025, "M60", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					if (t.config.triggerEgg(.5+(t.gen.nextDouble()/2)))
					{
						b=new Button(t);
						b.setText("M134");
						b.setOnClickListener(new OnClickListener()
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
										t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.AUTOMATIC|Cweapon.CLOSE_RANGE|Cweapon.CUMBERSOME|Cweapon.EXPLOSIVE|Cweapon.HIGH_CALIBER|Cweapon.HIGH_POWER_ROUNDS|Cweapon.HIGH_RECOIL|Cweapon.LARGE_MAGAZINE|Cweapon.LEGENDARY|Cweapon.LONG_RANGE, t.gen.nextBoolean() ? "minigun" : "M134", null));
										t.user.weapon.strengthModifier=.5;
										t.game.runStage();
									}
								});
								t.th.start();
							}
						});
						l.addView(b);
					}
					b=new Button(t);
					b.setText("RPG-7 Launcher");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.CUMBERSOME|Cweapon.EXPLOSIVE|Cweapon.HIGH_POWER_ROUNDS|Cweapon.ONE_ROUND_MAGAZINE|Cweapon.SLOW_RELOAD, "RPG", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Bazooka");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.CUMBERSOME|Cweapon.EXPLOSIVE|Cweapon.HIGH_CALIBER|Cweapon.ONE_ROUND_MAGAZINE|Cweapon.SLOW_RELOAD, .01, "bazooka", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Panzerschreck rocket launcher");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.CUMBERSOME|Cweapon.EXPLOSIVE|Cweapon.HIGH_CALIBER|Cweapon.HIGH_POWER_ROUNDS|Cweapon.LONG_RANGE|Cweapon.ONE_ROUND_MAGAZINE|Cweapon.SLOW_RELOAD, .05, "panzerschreck", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Shoulder launched Multipurpose Assault Weapon");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.CUMBERSOME|Cweapon.EXPLOSIVE|Cweapon.HIGH_POWER_ROUNDS|Cweapon.LONG_RANGE|Cweapon.ONE_ROUND_MAGAZINE|Cweapon.SLOW_RELOAD, .1, "SMAW", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("FIM-92");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ACCURATE|Cweapon.CUMBERSOME|Cweapon.EXPLOSIVE|Cweapon.HIGH_CALIBER|Cweapon.LONG_RANGE|Cweapon.ONE_ROUND_MAGAZINE, .075, "Stinger", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("M67 Fragmentation Grenade");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.EXPLOSIVE|Cweapon.HIGH_POWER_ROUNDS|Cweapon.LIGHT|Cweapon.ONE_ROUND_MAGAZINE|Cweapon.QUICK_RELOAD, .175, "frag", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("MK-54");
					b.setOnClickListener(new OnClickListener()
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_NUCLEAR, Cweapon.LIGHT, "Davy Crockett nuke", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Lightsaber");
					b.setOnClickListener(new OnClickListener()
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
									if (t.config.triggerEgg(.5-(t.gen.nextDouble()/2))) // We do NOT always want a working lightsaber.
										t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_FUTURE, Cweapon.CLOSE_RANGE|Cweapon.CLOSE_RANGE_ONLY|Cweapon.LIGHT, "lightsaber", null));
									else
										t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_BLUNT, Cweapon.CLOSE_RANGE|Cweapon.CLOSE_RANGE_ONLY|Cweapon.LIGHT|Cweapon.LEGENDARY, (t.gen.nextBoolean() ? "broken" : "model")+" lightsaber", null));
									t.game.runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Keep your current weapon");
					b.setOnClickListener(new OnClickListener()
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
									inputted=0;
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
		case 36:
			t.th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					if (inputted==-1)
						t.say("You examine your new "+t.user.weapon+", and put your old "+t.user.weapon.backup+" away, making room for your new main weapon, but still keeping your backup within reach.");
					else
						t.say("You decide your "+t.user.weapon+" is better than any weapon in the gunstore, and move off toward the valley.");
				}
			});
			t.th.start();
			prepContinueButton();
			break;
		case 37:
			if (onMove(58))
				return;
			t.th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					t.say("The Valley", "You move away from the gunstore.\n\n\tAfter a good amount of walking, you finally enter the lush valley.\n\tYou find yourself surrounded by greenery and life, but the valley is much smaller than it seemed from a distance. Unfortunately, you need to leave it.\n\n\n\tYou search for a way away.\n\n\tStraight away from the path that leads to the gunstore is a downtrodden set of stones, almost as a staircase, leading to a familiar skyline.\n\n\tFacing it, you see smoke out of the corner of your eye. Turning to your right, you see the light of a huge, hellish inferno on the horizon. You can almost see the fires. A large highway leads directly towards this pit.");
				}
			});
			t.th.start();
			prepContinueButton();
			break;
		case 38:
			t.fadeout();
			t.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					LinearLayout l=prepInput();
					Button b=new Button(t);
					b.setText("Go to familiarity");
					b.setOnClickListener(new OnClickListener()
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
									inputted=0;
									runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Take the highway");
					b.setOnClickListener(new OnClickListener()
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
									inputted=1;
									runStage();
								}
							});
							t.th.start();
						}
					});
					l.addView(b);
					b=new Button(t);
					b.setText("Return to the gunstore, and grab another weapon");
					b.setOnClickListener(new OnClickListener()
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
									inputted=0;
									stage=34;
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
		case 39:
			if (inputted==0)
			{
				if (onMove(1+t.gen.nextInt(10)))
					return;
				t.th=new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						String str=new String();
						boolean say=true;
						if (t.user.weapon.type<=Cweapon.TYPE_BLUNT)
						{
							if (t.user.weapon.backup!=null && t.user.weapon.backup.type<=Cweapon.TYPE_BLUNT) // We do not want to discuss trying to walk the chain of backups with the user. Just kill them.
							{
								t.user.dead=true;
								say=false; // Prevent the usual speech.
								t.say("Familiarity", "You barely manage to reach the summit of the staircase, gazing upon a line of destroyed buildings.\n\n\tAmong the ruined houses and fragmented skyscrapers, you see Gandalf running towards you, as if he was running late to meet you thanks to your choice of weapons, and miss the beast which destroys you.");
							}
							else
							{
								str=", watching your "+t.user.weapon+" slip out of your hand, seemingly floating away.\n\n\tYou feel yourself start to come to earth";
								t.user.weapon=t.user.weapon.backup; // Remove the primary from existence. We don't need any TYPE_BLUNTs floating around.
							}
						}
						else if (t.user.weapon.backup!=null && t.user.weapon.backup.type<=Cweapon.TYPE_BLUNT) // We know we don't need to walk the chain of weapon backups, since the prior if specifies that we already have a valid weapon in hand.
						{
							str=", gazing at your "+t.user.weapon.backup+" as it drifts out of its sheath and disappears.\n\n\tYou watch as you float away from familiarity";
							if (t.user.weapon.backup.backup!=null && t.user.weapon.backup.backup.type>Cweapon.TYPE_BLUNT) // Transparently allow a valid weapon to be swapped in if its only one layer away from being valid.
								t.user.weapon.backup=t.user.weapon.backup.backup;
							else // Just remove the existence of a backup
								t.user.weapon.backup=null;
						}
						if (say)
							t.say("Familiarity", "You go to familiarity.\n\n\n\n\tAs you reach the summit of the stairs, you see a destroyed line of buildings, from tall skyscrapers fallen into pieces, to houses in shambles.\n\n\tBefore you can continue further, Gandalf appears!\n\tHe yells,\n\n\t\tNo!\n\t\tYou must not!\n\t\tYour journey is not complete!\n\n\tBefore you can reply, he slaps you.\n\n\tYou go flying"+str+", and land in the pits.");
					}
				});
				t.th.start();
				prepContinueButton(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						t.th=new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								stage=41;
								runStage();
							}
						});
						t.th.start();
					}
				});
			}
			else
			{
				if (onMove(80))
					return;
				t.th=new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						t.say("The Highway", "You take the Highway towards the fiery pits.\n\n\tAlong the way, you see a shadow!\n\n\tIt approaches you menacingly.");
					}
				});
				t.th.start();
				prepContinueButton();
			}
			break;
		case 40:
			runShadows((byte)((t.config.difficultyMult*t.gen.nextInt(10))+3), (byte)0, (byte)0);
			break;
		case 41:
			if (onMove(85))
				return;
			t.th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					t.say(t.gen.nextBoolean() ? "The Pits" : "Hell", "You arrive in the fiery pits.\n\n\tYour first impression of them is hellish, full of fire everywhere, and generally not the place any person should ever be.\n\n\tSomehow, your second impression is even worse.\n\n\tOff in the distance, you see a structure of some sort, surrounded by shadows: Probably their home.\n\n\tBesides that, there is little more than ruins to be explored.");
				}
			});
			t.th.start();
			prepContinueButton();
			break;
		case 42:
			runPits((byte)(t.gen.nextInt(6)+3), (byte)0, (byte)0, (byte)0, false);
			break;
		default:
			t.logWarning("Unknown stage: "+stage);
			t.th=new Thread (new Runnable()
			{
				@Override
				public void run ()
				{
					String who=t.gen.nextBoolean() ? "Author" : "Programmer";
					String occurrence;
					if (t.gen.nextBoolean())
					{
						if (t.gen.nextBoolean())
							occurrence="a meteor appears, and flattens you.";
						else
							occurrence="a lightning bolt strikes you, and fries you.";
					}
					else
					{
						if (t.gen.nextBoolean())
							occurrence="a dinosaur appears, and eats you.";
						else
							occurrence="a nuclear bomb appears under your feet and explodes, vaporizing you.";
					}
					t.say("The End of the World...?","The "+who+" has run out of ideas, so "+occurrence);
				}
			});
			t.th.start();
			t.user.dead=true;
			prepContinueButton();
		}
		++stage;
		if (stage%(t.gen.nextInt(5)+2)==0 || t.gen.nextInt(100)==0)
			t.config.requestDifficultyUpdate();
		else
			t.config.updateDifficultyOnce();
	}

	public static void signalGameContinue ()
	{
		if (!Thread.interrupted())
			t.runOnUiThread (new Runnable ()
			{
				@Override
				public void run()
				{
					View v=t.findViewById(R.id.gameContinueButton);
					if (v==null)
					{
						t.displayHandler.removeMessages(1);
						return;
					}
					((TextView)v).setTextColor(Color.GREEN);
				}
			});
	}

	public static void unSignalGameContinue () // Undoes the above signaling. Necessary for the use of sayWhat() in non-say() contexts.
	{
		if (!Thread.interrupted())
			t.runOnUiThread (new Runnable ()
			{
				@Override
				public void run()
				{
					View v=t.findViewById(R.id.gameContinueButton);
					if (v==null)
					{
						t.displayHandler.removeMessages(1);
						return;
					}
					((TextView)v).setTextColor(Color.RED);
				}
			});
	}

	public void runArthur(final byte stage, String input)
	{
		t.user.isArthur=true;
		switch (stage)
		{
		case 0:
			t.th.interrupt();
			t.th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					t.say("Bridge of Death", "What is your quest?");
					t.findViewById(R.id.gameContinueButton).setVisibility(View.GONE);
					t.runOnUiThread(new Runnable ()
					{
						@Override
						public void run()
						{
							EditText ev=new EditText(t);
							ev.setOnFocusChangeListener(new OnFocusChangeListener()
							{
								@Override
								public void onFocusChange(View v, boolean hasFocus)
								{
									if (hasFocus && t.config.fullscreen)
									{
										t.setUi();
										TextView tv=((TextView)t.findViewById(R.id.gameTitle));
										LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)tv.getLayoutParams();
										int result=0;
										int resourceId = t.getResources().getIdentifier("status_bar_height", "dimen", "android");
										if (resourceId > 0) {
											result = t.getResources().getDimensionPixelSize(resourceId);
										}
										lp.topMargin=result+10;
										tv.setAlpha(1);
									}
								}
							});
							ev.setBackgroundColor(Color.rgb(128,128,128));
							ev.setHint("What is your quest?");
							ev.setHintTextColor(Color.WHITE);
							ev.setTextColor(Color.BLACK);
							ev.setShadowLayer(5.0f,5,5,Color.WHITE);
							ev.setImeActionLabel("Submit", KeyEvent.KEYCODE_ENTER); // Consider changing this label.
							ev.setImeOptions(EditorInfo.IME_ACTION_SEND);
							ev.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
							View v=t.findViewById(R.id.gameLayout);
							if (v!=null)
								((LinearLayout)v).addView(ev);
							ev.requestFocus();
							((InputMethodManager)t.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(ev, InputMethodManager.SHOW_FORCED); // Force the soft keyboard open.
							t.setUi(); // Doubly ensure sanity. See above comment.
							ev.setOnEditorActionListener(new TextView.OnEditorActionListener() // Listen for submit or enter.
							{
								@Override
								public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
								{
									if (actionId==EditorInfo.IME_ACTION_SEND || actionId==EditorInfo.IME_NULL)
									{
										view.setOnEditorActionListener(null);

										final EditText ev=(EditText)view;
										ev.setVisibility(View.GONE);
										// Close the soft keyboard, now that there's nothing for it to write to.
										((InputMethodManager)t.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
										final String str=ev.getText().toString().trim();
										((TextView)t.findViewById(R.id.gameText)).append("\n\n\""+str+"\"\n\n");
										t.th=new Thread (new Runnable()
										{
											@Override
											public void run ()
											{
												runArthur((byte)1, str);
											}
										});
										t.th.start();
										return true;
									}
									return false;
								}
							});
							t.config.computerPaused=true;
						}
					});
				}
			});
			t.th.start();
			break;
		case 1:
			if (!"To seek the Holy Grail".equalsIgnoreCase(input))
			{
				t.sayWhat("Your voice echoes \"Auuuuuuuugh!\" across the Gorge of Eternal Peril as you are flung from the bridge.");
				t.user.dead=true;
				prepContinueButton();
				return;
			}
			t.sayWhat("What is the air-speed velocity of an unladen swallow?");
			t.findViewById(R.id.gameContinueButton).setVisibility(View.GONE);
			t.runOnUiThread(new Runnable ()
			{
				@Override
				public void run()
				{
					EditText ev=new EditText(t);
					ev.setOnFocusChangeListener(new OnFocusChangeListener()
					{
						@Override
						public void onFocusChange(View v, boolean hasFocus)
						{
							if (hasFocus && t.config.fullscreen)
							{
								t.setUi();
								TextView tv=((TextView)t.findViewById(R.id.gameTitle));
								LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)tv.getLayoutParams();
								int result=0;
								int resourceId = t.getResources().getIdentifier("status_bar_height", "dimen", "android");
								if (resourceId > 0) {
									result = t.getResources().getDimensionPixelSize(resourceId);
								}
								lp.topMargin=result+10;
								tv.setAlpha(1);
							}
						}
					});
					ev.setBackgroundColor(Color.rgb(128,128,128));
					ev.setHint("What is the air-speed velocity of an unladen swallow?");
					ev.setHintTextColor(Color.WHITE);
					ev.setTextColor(Color.BLACK);
					ev.setShadowLayer(5.0f,5,5,Color.WHITE);
					ev.setImeActionLabel("Submit", KeyEvent.KEYCODE_ENTER); // Consider changing this label.
					ev.setImeOptions(EditorInfo.IME_ACTION_SEND);
					ev.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
					View v=t.findViewById(R.id.gameLayout);
					if (v!=null)
						((LinearLayout)v).addView(ev);
					ev.requestFocus();
					((InputMethodManager)t.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(ev, InputMethodManager.SHOW_FORCED); // Force the soft keyboard open.
					t.setUi(); // Doubly ensure sanity. See above comment.
					ev.setOnEditorActionListener(new TextView.OnEditorActionListener() // Listen for submit or enter.
					{
						@Override
						public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
						{
							if (actionId==EditorInfo.IME_ACTION_SEND || actionId==EditorInfo.IME_NULL)
							{
								view.setOnEditorActionListener(null);

								final EditText ev=(EditText)view;
								ev.setVisibility(View.GONE);
								// Close the soft keyboard, now that there's nothing for it to write to.
								((InputMethodManager)t.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
								final String str=ev.getText().toString().trim();
								((TextView)t.findViewById(R.id.gameText)).append("\n\n\""+str+"\"\n\n");
								t.th=new Thread (new Runnable()
								{
									@Override
									public void run ()
									{
										runArthur((byte)2, str);
									}
								});
								t.th.start();
								return true;
							}
							return false;
						}
					});
					t.config.computerPaused=true;
				}
			});
			break;
		case 2:
			if (!("What do you mean? An African or European swallow?".equalsIgnoreCase(input) || "What do you mean? African or European swallow?".equalsIgnoreCase(input)))
			{
				t.sayWhat("Your voice echoes \"Auuuuuuuugh!\" across the Gorge of Eternal Peril as you are flung from the bridge.");
				t.user.dead=true;
				prepContinueButton();
				return;
			}
			t.sayWhat("What? I don\'t know that!\n\tAuuuuuuuugh!\n\n\n\n");
			t.findViewById(R.id.gameContinueButton).setVisibility(View.GONE);
			t.snooze(2500);
			t.sayWhat("How do you know so much about swallows?");
			t.findViewById(R.id.gameContinueButton).setVisibility(View.GONE);
			t.runOnUiThread(new Runnable ()
			{
				@Override
				public void run()
				{
					EditText ev=new EditText(t);
					ev.setOnFocusChangeListener(new OnFocusChangeListener()
					{
						@Override
						public void onFocusChange(View v, boolean hasFocus)
						{
							if (hasFocus && t.config.fullscreen)
							{
								t.setUi();
								TextView tv=((TextView)t.findViewById(R.id.gameTitle));
								LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)tv.getLayoutParams();
								int result=0;
								int resourceId = t.getResources().getIdentifier("status_bar_height", "dimen", "android");
								if (resourceId > 0) {
									result = t.getResources().getDimensionPixelSize(resourceId);
								}
								lp.topMargin=result+10;
								tv.setAlpha(1);
							}
						}
					});
					ev.setBackgroundColor(Color.rgb(128,128,128));
					ev.setHint("How do you know so much about swallows?");
					ev.setHintTextColor(Color.WHITE);
					ev.setTextColor(Color.BLACK);
					ev.setShadowLayer(5.0f,5,5,Color.WHITE);
					ev.setImeActionLabel("Submit", KeyEvent.KEYCODE_ENTER); // Consider changing this label.
					ev.setImeOptions(EditorInfo.IME_ACTION_SEND);
					ev.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
					View v=t.findViewById(R.id.gameLayout);
					if (v!=null)
						((LinearLayout)v).addView(ev);
					ev.requestFocus();
					((InputMethodManager)t.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(ev, InputMethodManager.SHOW_FORCED); // Force the soft keyboard open.
					t.setUi(); // Doubly ensure sanity. See above comment.
					ev.setOnEditorActionListener(new TextView.OnEditorActionListener() // Listen for submit or enter.
					{
						@Override
						public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
						{
							if (actionId==EditorInfo.IME_ACTION_SEND || actionId==EditorInfo.IME_NULL)
							{
								view.setOnEditorActionListener(null);

								final EditText ev=(EditText)view;
								ev.setVisibility(View.GONE);
								// Close the soft keyboard, now that there's nothing for it to write to.
								((InputMethodManager)t.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
								final String str=ev.getText().toString().trim();
								((TextView)t.findViewById(R.id.gameText)).append("\n\n\""+str+"\"\n\n");
								t.th=new Thread (new Runnable()
								{
									@Override
									public void run ()
									{
										runArthur((byte)3, str);
									}
								});
								t.th.start();
								return true;
							}
							return false;
						}
					});
					t.config.computerPaused=true;
				}
			});
			break;
		case 3:
			if (!"Well, you have to know these things when you\'re a king you know".equalsIgnoreCase(input))
			{
				t.sayWhat("Your voice echoes \"Auuuuuuuugh!\" across the Gorge of Eternal Peril as you are flung from the bridge.");
				t.user.dead=true;
				prepContinueButton();
				return;
			}
			t.sayWhat("Ohh.");
			prepContinueButton(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					t.th=new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							t.user.name="Arthur"; // So the egg doesn't trigger again.
							t.game.stage=0;
							t.game.runStage();
						}
					});
					t.th.start();
				}
			});
			break;
		}
	}

	public void runShadows(final byte number, final byte stage, final byte input)
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
						prepContinueButton();
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
						prepContinueButton(new OnClickListener()
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
										runShadows(number, stage, input);
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
					prepContinueButton(new OnClickListener()
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
									runShadows(number, stage, input);
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
						LinearLayout l=prepInput();
						doCommitSuicide=true;
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
										runShadows(number, (byte)1, (byte)0);
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
								runShadows(number, stage, input);
							}
						});
						b=new Button(t);
						b.setText((t.gen.nextBoolean() ? "Run away" : "Flee")+" from the "+(t.gen.nextBoolean() ? "ghostly" : "shadowy")+" vision before you");
						b.setOnClickListener(new OnClickListener()
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
										runShadows(number, (byte)1, (byte)1);
									}
								});
								t.th.start();
							}
						});
						l.addView(b);
						if (t.config.triggerEgg((t.gen.nextInt(10)+1)/11))
						{
							b=new Button(t);
							b.setText("Make friends with the creature");
							b.setOnClickListener(new OnClickListener()
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
											runShadows(number, (byte)1, (byte)2);
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
				if (Thread.interrupted() || onMove(t.gen.nextInt(85)+1))
					return;
				switch (input)
				{
				case 0:
					Runnable r;
					switch (t.user.weapon.type)
					{
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
									if (t.config.triggerEgg(6))
										t.say("Alas, poor Yorick...","...I knew that one once.\n\n\tThey also called that adventurer \""+t.user+"\".\n\n\n\tThen, one day, a shadow appeared along the Highway to Hell, and the "+t.user.weapon+" the poor fool was carrying jammed, and no more was poor Yorick.");
									else
										t.say("A shot and a miss", "You take aim, your sights leveled at the "+(t.gen.nextBoolean() ? "shadow" : "figure")+".\n\n\tJust as you pull the trigger, the pits rumble with fiery power, shaking the ground, making you miss.\n\n\n\tOne day, years later, when the pits will have cooled, a child will come across the spot where the shadow consumed you, and all "+(t.gen.nextBoolean() ? "he" : "she")+" will find of you is your "+t.user.weapon+".");
								}
								else
									t.say("Sharpshooter","You level your "+t.user.weapon+" directly at the shadow, and fire.\n\n\tThe round hits it, and the shadow disappears into a swirl of smoke.\n\n\tThe fired round clatters straight to the ground, as the smoke dissipates.\n\n\n\tYou walk over to it, but you can\'t find the round.\n\n\tYou can\'t quite shake the feeling that what you know isn\'t real.");
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
								t.say("Doomsday", "You get out your "+t.user.weapon+", arm it, set the timer to one second, and start it.\n\n\tFrom familiarity, the shopkeepers gaze at the mushroom cloud from where you once stood, as the shadow of a dragon looms behind them.");
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
					prepContinueButton(new View.OnClickListener()
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
									else
										runShadows((byte)(number-1), (byte)0, input);
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
					prepContinueButton();
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
					prepContinueButton();
				}
			}
		}
	}

	public void runPits(final byte number, final byte stage, final byte input, final byte input2, final boolean hasAssaultedHQ)
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
			switch(stage)
			{
			case 0: // Stage 0: User input on direction
				t.fadeout();
				t.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						LinearLayout l=prepInput();
						Button b=new Button(t);
						if (!hasAssaultedHQ)
						{
							b.setText("Mount an assault on the House of Shadows");
							b.setOnClickListener(new OnClickListener()
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
											runPits(number, (byte)1, (byte)0, input2, true);
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
							b.setOnClickListener(new OnClickListener()
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
											runPits(number, (byte)1, (byte)1, input2, hasAssaultedHQ);
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
							b.setOnClickListener(new OnClickListener()
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
											runPits(number, (byte)1, (byte)2, input2, hasAssaultedHQ);
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
							b.setOnClickListener(new OnClickListener()
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
											runPits(number, (byte)1, (byte)3, input2, hasAssaultedHQ);
										}
									});
									t.th.start();
								}
							});
							l.addView(b);
							b=new Button(t);
						}
						b.setText("Make your way to the depression at the center of the pits");
						b.setOnClickListener(new OnClickListener()
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
										runPits((byte)0, stage, input, input2, hasAssaultedHQ);
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
				if (onMove(111))
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
					prepContinueButton(new OnClickListener()
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
									runPits(number, (byte)2, input, input2, hasAssaultedHQ);
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
						prepContinueButton(new OnClickListener()
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
										runPits(number, (byte)2, input, input2, hasAssaultedHQ);
									}
								});
								t.th.start();
							}
						});
					}
					else
						runPits(number, (byte)100, input, input2, hasAssaultedHQ);
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
						prepContinueButton(new OnClickListener()
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
										runPits(number, (byte)2, input, input2, hasAssaultedHQ);
									}
								});
								t.th.start();
							}
						});
					}
					else
						runPits(number, (byte)100, input, input2, hasAssaultedHQ);
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
						prepContinueButton(new OnClickListener()
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
										runPits(number, (byte)2, input, input2, hasAssaultedHQ);
									}
								});
								t.th.start();
							}
						});
					}
					else
						runPits(number, (byte)100, input, input2, hasAssaultedHQ);
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
							LinearLayout l=prepInput();
							doCommitSuicide=true;
							Button b=new Button(t);
							b.setText("Fight the trio of shadows with your primary "+t.user.weapon);
							b.setOnClickListener(new OnClickListener()
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
											runPits(number, (byte)3, input, (byte)0, hasAssaultedHQ);
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
									runPits(number, stage, input, input2, hasAssaultedHQ);
								}
							});
							b=new Button(t);
							b.setText("Make a run for the demolition charge");
							b.setOnClickListener(new OnClickListener()
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
											runPits(number, (byte)3, input, (byte)1, hasAssaultedHQ);
										}
									});
								}
							});
							l.addView(b);
							b=new Button(t);
							b.setText(t.gen.nextBoolean() ? "Run away!" : "Flee");
							b.setOnClickListener(new OnClickListener()
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
											runPits(number, (byte)3, input, (byte)2, hasAssaultedHQ);
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
							LinearLayout l=prepInput();
							doCommitSuicide=true;
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
							b.setOnClickListener(new OnClickListener()
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
											runPits(number, (byte)3, input, (byte)0, hasAssaultedHQ);
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
									runPits(number, stage, input, input2, hasAssaultedHQ);
								}
							});
							b=new Button(t);
							b.setText(t.gen.nextBoolean() ? "Run away" : "Flee");
							b.setOnClickListener(new OnClickListener()
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
											runPits(number, (byte)3, input, (byte)1, hasAssaultedHQ);
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
							LinearLayout l=prepInput("Your situation is unknown");
							doCommitSuicide=true;
							Button b=new Button(t);
							b.setText("Try this again");
							b.setOnClickListener(new OnClickListener()
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
											runPits(number, (byte)0, (byte)0, (byte)0, true);
										}
									});
									t.th.start();
								}
							});
							l.addView(b);
							b=new Button(t);
							b.setText("Be done with it");
							b.setOnClickListener(new OnClickListener()
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
							archeryMinigame((int)Math.ceil(t.gen.nextInt(40)+(t.config.difficultyMult*10)+5), ((20*(1-t.config.difficultyMult))+5)/5);
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
								prepContinueButton();
							else
								prepContinueButton(new OnClickListener()
								{
									@Override
									public void onClick(View v)
									{
										t.th=new Thread(new Runnable()
										{
											@Override
											public void run()
											{
												runPits(number, (byte)4, input, input2, hasAssaultedHQ);
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
								final targetingMinigame.output res=aimMinigame();
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
								prepContinueButton();
							else
								prepContinueButton(new OnClickListener()
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
												runPits(number, (byte)4, input, input2, hasAssaultedHQ);
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
							prepContinueButton();
						else
						{
							prepContinueButton(new View.OnClickListener()
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
											runPits((byte)0, stage, input, input2, true);
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
						prepContinueButton();
						break;
					default:
						dataError();
					}
					break;
				case 1:
					switch (input2)
					{
					case 0:
						if (t.user.weapon.type==Cweapon.TYPE_ARCHERY && .65>(t.config.difficultyMult*.75*t.user.weapon.getAbsoluteStrength()))
						{
							archeryMinigame((int)Math.ceil(t.gen.nextInt(40)+(t.config.difficultyMult*10)+5), ((20*(1-t.config.difficultyMult))+5)/5);
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
								prepContinueButton();
							else
								prepContinueButton(new OnClickListener()
								{
									@Override
									public void onClick(View v)
									{
										t.th=new Thread(new Runnable()
										{
											@Override
											public void run()
											{
												runPits(number, (byte)4, input, input2, hasAssaultedHQ);
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
								final targetingMinigame.output res=aimMinigame();
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
								prepContinueButton();
							else
								prepContinueButton(new OnClickListener()
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
												runPits(number, (byte)4, input, input2, hasAssaultedHQ);
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
							prepContinueButton();
						else
						{
							prepContinueButton(new View.OnClickListener()
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
											runPits((byte)0, stage, input, input2, true);
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
						prepContinueButton();
						break;
					default:
						dataError();
					}
					break;
				}
				break;
			}
		}
	}

	public void inputClear()
	{
		if (doCommitSuicide)
			t.user.commitSuicide();
		else
		{
			stage-=2;
			runStage();
		}
	}

	public void dataError() // Called when a switch goes to a default case, when this is nonsensical of course.
	{
		t.user.dead=true;
		t.th.interrupt();
		t.th=new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				t.say(t.gen.nextBoolean() ? "Unknowable" : "Legend", "A vortex of Infinite Improbability opens, and consumes you.\n\n\n\n\n\n");
				t.snooze(750+t.gen.nextInt(500));
				Cgame.unSignalGameContinue();
				t.snooze(2500);
				t.sayWhat("That means you have a bug.\n\n");
				Cgame.unSignalGameContinue();
				t.snooze(1500);
				t.sayWhat("Specifically, the game engine managed to set your state to one that makes negative amounts of sense.\n\n");
				Cgame.unSignalGameContinue();
				t.snooze(500);
				t.sayWhat("Look buddy, you got a kill-bug.\n\tJust play it again, hopefully the bug won\'t rear its ugly head.");
			}
		});
	}

	public void prepContinueButton()
	{
		t.snooze(AnimationUtils.loadAnimation(t, R.anim.fadeout).getDuration());
		while(t.waiting)
			t.frameTick();
		t.delay(t.gen.nextInt(250)+250);
		t.runOnUiThread(new Runnable ()
		{
			@Override
			public void run()
			{
				View v=t.findViewById(R.id.gameContinueButton);
				if (v!=null)
				{
					Button b=(Button)v;
					b.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick (View v)
						{
							v.setOnClickListener(null);

							inputted=-1;
							t.th.interrupt();
							t.displayHandler.removeMessages(1);
							t.th=new Thread (new Runnable ()
							{
								@Override
								public void run ()
								{
									t.displayHandler.removeMessages(1);
									if (t.user.dead)
									{
										stage=-1;
										t.config.computerPaused=true;
									}
									else
										t.config.computerPaused=false;
									runStage();
								}
							});
							t.th.start();
						}
					});
					b.setVisibility(View.VISIBLE);
				}
				else
					t.logError("prepContinueButton() called, but could not find button.");
			}
		});
	}

	public void prepContinueButton(final View.OnClickListener onClick)
	{
		t.snooze(AnimationUtils.loadAnimation(t, R.anim.fadeout).getDuration());
		while(t.waiting)
			t.frameTick();
		t.delay(t.gen.nextInt(250)+250);
		t.runOnUiThread(new Runnable ()
		{
			@Override
			public void run()
			{
				View v=t.findViewById(R.id.gameContinueButton);
				if (v!=null)
				{
					Button b=(Button)v;
					b.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick (View v)
						{
							v.setOnClickListener(null);

							inputted=-1;
							t.th.interrupt();
							t.displayHandler.removeMessages(1);
							onClick.onClick(v);
						}
					});
					b.setVisibility(View.VISIBLE);
				}
				else
					t.logError("prepContinueButton() called, but could not find button.");
			}
		});
	}

	private static LinearLayout prepInput()
	{
		t.setContentView(R.layout.input);
		t.currentView=R.id.inputLayout;
		t.setUi();
		return (LinearLayout)t.findViewById(R.id.inputButtonBar);
	}

	private static LinearLayout prepInput(String inputTitle)
	{
		t.setContentView(R.layout.input);
		t.currentView=R.id.inputLayout;
		((TextView)t.findViewById(R.id.inputTitle)).setText(inputTitle);
		t.setUi();
		return (LinearLayout)t.findViewById(R.id.inputButtonBar);
	}

	private static void eatenByGrue()
	{
		t.user.dead=true;
		t.config.computerPaused=true;
		t.fadeout();
		t.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				t.setContentView(R.layout.grue);
				t.currentView=R.id.grueLayout;
				t.setUi();
				if (t.config.triggerEgg(1, 3))
					((TextView)t.findViewById(R.id.grueContent)).setTextColor(Color.rgb(255, t.gen.nextInt(256), t.gen.nextInt(256)));
			}
		});
	}

	public boolean onMove(double IBOdds)
	{
		t.user.gold.accrueInterest();
		if (t.config.schoolEggs && t.config.triggerEgg(IBOdds))
		{
			t.user.dead=!t.config.triggerEgg(99);
			t.th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					if (t.user.dead)
					{
						String message="You black out, and wake up in an IB HL English classroom.\n\n\tAs you look around in confusion, the teacher says,\n\n\t\tEssay test today!\n\t\tHere\'s the book!\n\n\tAs the students prepare themselves for an essay test, and the teacher passes out their books, you decide you have no choice.\n\n\tYou close your eyes...";
						if (t.user.weapon.type==Cweapon.TYPE_FUTURE)
						{
							if (t.user.weapon.characteristicSet(Cweapon.CLOSE_RANGE_ONLY)) // This is the identifying trait of the lightsaber.
								message+="\n\n\n\n\n\n\t...And draw your "+t.user.weapon+"!\n\n\tYou charge at the teacher, intending to fight your way out.\n\n\tAll of a sudden, a student blocks your path, hitting you, saying something about the teacher not yet having handed out the homework.\n\n\tAs you try to stop, your lightsaber slices cleanly through a girl next to you.\n\n\tShe collapses, dead.\n\n\tBefore you can react, ";
							else
								message+="\n\n\n\n\n\t...And draw your "+t.user.weapon+"!\n\n\tYou aim at the teacher, and tense.\n\n\n\tJust as you\'re ready to fire, a student hits you, yelling something about the homework needing explanation.\n\n\t"+(t.gen.nextBoolean() ? "She" : "He")+" throws off your balance, and your aim as well.\n\n\n\tYour shot strikes another student, turning "+(t.gen.nextBoolean() ? "him" : "her")+" into ash.\n\n\tSuddenly, ";
							message+="Gandalf appears.\n\n\tHe yells,\n\n\t\tHow dare you do this!\n\t\tYou are unworthy!!!\n\n\tWith that...";
						}
						t.say("Baccalaureate", message);
					}
					else
					{
						String[] topics={
								"differentiation",
								"integration",
								"the Fundamental Theorem of Calculus",
								"how to find volume via integrals",
								"Riemann sums",
								"limits",
								"relative rates",
								"Taylor polynomials",
								"series",
								"vectors",
								"proof by induction",
								"differential equations",
								"optimization"
						};
						t.say("The Diploma", "You black out, and awake in an IB HL Math classroom.\n\n\tAs you look around in confusion, the teacher begins to teach "+topics[t.gen.nextInt(topics.length)]+".\n\n\tYou feel...\n\tMotivated, strangely enough.\n\n\n\n\tThrough hard work, perseverance, and worship of Lord 2-19, you get the IB diploma.");
					}
				}
			});
			t.th.start();
			prepContinueButton(t.user.dead ? new OnClickListener()
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
							if (t.user.weapon.type==Cweapon.TYPE_FUTURE)
							{
								t.user.weapon.type=Cweapon.TYPE_USED_FOR_CONVENIENCE;
								t.user.commitSuicide();
							}
							else if (t.config.triggerEgg(.2))
								t.user.commitSuicide();
							else
							{
								stage=-1;
								runStage();
							}
						}
					});
					t.th.start();
				}
			} : onWin);
			return true;
		}
		return false;
	}

	public boolean onMove (double IBNumerator, double IBDenominator)
	{
		return onMove(IBNumerator/IBDenominator);
	}

	public boolean onMove (int oneIn)
	{
		return onMove(1, oneIn);
	}

	public OnClickListener onWin=new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			t.th.interrupt();
			t.setContentView(R.layout.quit);
			t.currentView=R.id.quitLayout;
			t.setUi();
			((TextView)t.findViewById(R.id.quitTitle)).setText("You win!!");
			((Button)t.findViewById(R.id.quitContinueButton)).setText("Play again");
		}
	};

	private boolean archeryMinigame (final int threshold, final double time) // Reimplement this like KEYSEC.
	{
		final keypressMinigame game=new keypressMinigame((threshold/time)*.25);
		t.fadeout();
		t.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				t.setContentView(R.layout.archery);
				t.currentView=R.id.archeryLayout;
				t.setUi();
				game.button=t.findViewById(R.id.archeryBullseye);
				((TextView)t.findViewById(R.id.archeryThreshold)).setText(Integer.toString(threshold));
				((TextView)t.findViewById(R.id.archeryCurrent)).setText("0");
				((TextView)t.findViewById(R.id.archeryTiming)).setText(Long.toString((long)game.timeRemaining));
			}
		});
		if (t.snooze((long)((1000*game.timeRemaining)-((long)(1000*game.timeRemaining)))))
			return true;
		while (game.timeRemaining>=1)
		{
			if (t.snooze(1000))
				return true;
			--game.timeRemaining;
			t.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					((TextView)t.findViewById(R.id.archeryTiming)).setText(Long.toString((long)game.timeRemaining));
				}
			});
		}
		game.timeRemaining=time;
		t.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Configuration c=t.getResources().getConfiguration();
				boolean landscape=(c.orientation==Configuration.ORIENTATION_LANDSCAPE); // If we're in landscape mode, we need all the space we can get. 

				t.findViewById(R.id.archeryWait).setVisibility(landscape || t.gen.nextBoolean() ? View.GONE : View.INVISIBLE);
				((TextView)t.findViewById(R.id.archeryTimingLabel)).setText("Time ends in:");
				((TextView)t.findViewById(R.id.archeryTiming)).setText(Long.toString((long)game.timeRemaining));
				game.button.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						game.deltaX+=Math.copySign(Math.max(Math.abs(t.gen.nextGaussian()), t.gen.nextDouble()), game.deltaX);
						game.deltaY+=Math.copySign(Math.max(Math.abs(t.gen.nextGaussian()), t.gen.nextDouble()), game.deltaY);
						++game.presses;
						((TextView)t.findViewById(R.id.archeryCurrent)).setText(Integer.toString(game.presses));
					}
				});
			}
		});
		Thread ticker=new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)game.button.getLayoutParams();
				Runnable r=new Runnable()
				{
					@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
					@Override
					public void run()
					{
						if (!(Build.VERSION.SDK_INT>=18 && game.button.isInLayout()))
							game.button.requestLayout();
					}
				};
				while (!game.done)
				{
					double factor=60.0/MainActivity.TARGET_FPS;
					int dX=(int)Math.round(game.deltaX*factor);
					int dY=(int)Math.round(game.deltaY*factor);
					if ((lp.leftMargin+dX)<0 || (lp.leftMargin+dX)>(t.findViewById(R.id.archeryBullseyeLayout).getWidth()-game.button.getWidth()))
					{
						game.deltaX*=-1;
						dX*=-1;
					}
					if ((lp.topMargin+dY)<0 || (lp.topMargin+dY)>(t.findViewById(R.id.archeryBullseyeLayout).getHeight()-game.button.getHeight()))
					{
						game.deltaY*=-1;
						dY*=-1;
					}
					lp.topMargin+=dY;
					lp.leftMargin+=dX;
					t.runOnUiThread(r);
					if (t.frameTick())
						return;
				}
			}
		});
		ticker.start();
		t.snooze((long)(1000*(game.timeRemaining-Math.floor(game.timeRemaining))));
		game.timeRemaining=Math.floor(game.timeRemaining);
		while (game.timeRemaining>=1)
		{
			t.snooze(1000);
			--game.timeRemaining;
			t.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					((TextView)t.findViewById(R.id.archeryTiming)).setText(Long.toString((long)game.timeRemaining));
				}
			});
		}
		game.done=true;
		t.user.dead=(game.presses<threshold);
		return t.user.dead;
	}

	public targetingMinigame.output aimMinigame()
	{
		final targetingMinigame game=new targetingMinigame();
		t.fadeout();
		t.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				t.setContentView(R.layout.aimer);
				t.currentView=R.id.aimerLayout;
				game.crosshairs=t.findViewById(R.id.aimerCrosshairs);
				game.layout=t.findViewById(R.id.aimerBullseyeLayout);
				game.bullseye=t.findViewById(R.id.aimerBullseye);
				game.crosshairCoord=(RelativeLayout.LayoutParams)game.crosshairs.getLayoutParams();
				((TextView)t.findViewById(R.id.aimerTiming)).setText(Long.toString((long)Math.floor(game.timeRemaining)));


				Configuration c=t.getResources().getConfiguration();
				if (c.orientation==Configuration.ORIENTATION_LANDSCAPE)
					game.layout.getLayoutParams().width=game.layout.getHeight();
			}
		});
		if (t.snooze((long)((1000*game.timeRemaining)-((long)(1000*game.timeRemaining)))))
			return new targetingMinigame.output();
		game.timeRemaining=Math.floor(game.timeRemaining);
		while (game.timeRemaining>=1)
		{
			if (t.snooze(1000))
				return new targetingMinigame.output();
			--game.timeRemaining;
			t.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					((TextView)t.findViewById(R.id.aimerTiming)).setText(Long.toString((long)game.timeRemaining));
				}
			});
		}
		game.timeRemaining=t.gen.nextGaussian()+(t.config.difficultyMult*8)+5;
		t.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				game.crosshairs.setVisibility(View.VISIBLE);
				((TextView)t.findViewById(R.id.aimerTimingTitle)).setText("Time remaining:");
				((TextView)t.findViewById(R.id.aimerTiming)).setText(Long.toString((long)Math.floor(game.timeRemaining)));
				game.crosshairCoord.leftMargin=t.gen.nextInt(game.layout.getWidth()-game.crosshairs.getWidth());
				game.crosshairCoord.topMargin=t.gen.nextInt(game.layout.getHeight()-game.crosshairs.getHeight());
				t.findViewById(R.id.aimerUpArrow).setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						double delta=Math.abs(t.gen.nextGaussian()+((t.gen.nextDouble()+game.deltaY)));
						if (game.crosshairCoord.topMargin<=delta)
							game.crosshairCoord.topMargin=0;
						else
							game.crosshairCoord.topMargin-=delta;
					}
				});
				t.findViewById(R.id.aimerRightArrow).setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						double delta=Math.abs(t.gen.nextGaussian()+((t.gen.nextDouble()+game.deltaX)));
						if (game.crosshairCoord.topMargin+delta+game.crosshairs.getHeight()>=game.layout.getHeight())
							game.crosshairCoord.topMargin=game.layout.getHeight()-game.crosshairs.getHeight();
						else
							game.crosshairCoord.leftMargin+=delta;
					}
				});
				t.findViewById(R.id.aimerDownArrow).setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						double delta=Math.abs(t.gen.nextGaussian()+((t.gen.nextDouble()+game.deltaY)));
						if (game.crosshairCoord.topMargin+delta+game.crosshairs.getHeight()>=game.layout.getHeight())
							game.crosshairCoord.topMargin=game.layout.getHeight()-game.crosshairs.getHeight();
						else
							game.crosshairCoord.topMargin+=delta;
					}
				});
				t.findViewById(R.id.aimerLeftArrow).setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						double delta=Math.abs(t.gen.nextGaussian()+((t.gen.nextDouble()+game.deltaX)));
						if (game.crosshairCoord.topMargin<=delta)
							game.crosshairCoord.topMargin=0;
						else
							game.crosshairCoord.leftMargin-=delta;
					}
				});
			}
		});
		game.startSway();
		t.snooze((long)(1000*(game.timeRemaining-Math.floor(game.timeRemaining))));
		game.timeRemaining=Math.floor(game.timeRemaining);
		while (game.timeRemaining>=1)
		{
			t.snooze(1000);
			--game.timeRemaining;
			t.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					((TextView)t.findViewById(R.id.aimerTiming)).setText(Long.toString((long)game.timeRemaining));
				}
			});
		}
		game.swayer.interrupt();
		game.swayer=null;
		return game.result();
	}

	@Override
	public int describeContents ()
	{
		return 4;
	}

	@Override
	public void writeToParcel (Parcel out, int n)
	{
		out.writeInt(--stage);
		out.writeInt(line);
		out.writeInt(inputted);
	}

	public static final Parcelable.Creator<Cgame> CREATOR=new Parcelable.Creator<Cgame> ()
			{
		@Override
		public Cgame createFromParcel (Parcel in)
		{
			return new Cgame(in);
		}

		@Override
		public Cgame[] newArray (int n)
		{
			return new Cgame[n];
		}
			};

			// Data-Only classes: NOT CONSERVED through onRestoreInstanceState()
			private static class keypressMinigame
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

			private static class targetingMinigame
			{
				public static class output // The type for returned data.
				{
					public double distance; // Distance from a true bullseye
					public int type; // The classification of this shot

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

				public targetingMinigame()
				{
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
}
