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

	public Cshadows shadows; // runShadows
	public Cpits pits; // runPits

	public Cgame()
	{
		stage=0;
		line=0;
		inputted=-1;

		shadows=new Cshadows();
		pits=new Cpits();
	}

	private Cgame (Parcel in)
	{
		stage=in.readInt();
		line=in.readInt();
		inputted=in.readInt();

		shadows=in.readParcelable(Cshadows.class.getClassLoader());
		pits=in.readParcelable(Cpits.class.getClassLoader());
		pits.game=this;
	}

	public void saveTo(SharedPreferences.Editor edit)
	{
		edit.putInt("gameStage", stage-1);
		edit.putInt("gameLine", line);
		edit.putInt("gameInputted", inputted);
		shadows.saveTo(edit);
		pits.saveTo(edit);
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
		shadows.loadFrom(sp);
		pits.loadFrom(sp);
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
					if (inputted==-1)
						t.say("Weaponry is King", "Moving away from the cave yet again, you encounter an abandoned gunstore.\n\n\tInside, there are two shapes.");
					else
						t.say("Back to the action", "You return to the gunstore.\n\n\tLooking inside, the two shapes you saw earlier are still there, wandering the store.\n\tInhaling apprehensively, you debate entering.");
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
							if (t.user.weapon.characteristicSet(Cweapon.LEGENDARY)) // Excalibur check
								t.determineUserDeath(.4); // Easier weighting for Excalibur
							else
								t.determineUserDeath(.75); // Traditional weighting
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
				t.user.clearedGunstore=true;
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
					else if (t.user.clearedGunstore)
						t.say("Return", "You return to the gunstore.\n\n\tLuckily, it\'s still lacking as far as things that want to kill and eat you are concerned.\n\n\tYou look to the walls, filled with more weapons than you can count, again resolving to grab one, then go back to the valley.");
					else {
						stage=29; // Return to the gunstore fight
						inputted=-2;
						runStage();
					}
				}
			});
			t.th.start();
			prepContinueButton();
			break;
		case 35:
			t.fadeout();
			t.user.clearedGunstore=true; // Sanity setting.
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
					b.setText(t.gen.nextBoolean() ? "Revolver" : "Colt .45");
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
									t.user.weapon.setPrimary(new Cweapon(Cweapon.TYPE_MODERN, Cweapon.ANCIENT|Cweapon.CLOSE_RANGE|Cweapon.HIGH_CALIBER|Cweapon.HIGH_POWER_ROUNDS|Cweapon.HIGH_RECOIL|Cweapon.LIGHT|Cweapon.SLOW_RELOAD, .04, t.gen.nextBoolean() ? "Colt .45" : (t.gen.nextBoolean() ? "revolver" : "six-shot"), null));
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
						String str= "";
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
			shadows.runStage();
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
			pits.runStage();
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
										((TextView)t.findViewById(R.id.gameText)).append("\n\n\"" + str + "\"\n\n");
										t.setUi();
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
								t.setUi();
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
								t.setUi();
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

	public static LinearLayout prepInput()
	{
		t.setContentView(R.layout.input);
		t.currentView=R.id.inputLayout;
		t.setUi();
		return (LinearLayout)t.findViewById(R.id.inputButtonBar);
	}

	public static LinearLayout prepInput(String inputTitle)
	{
		t.setContentView(R.layout.input);
		t.currentView=R.id.inputLayout;
		((TextView)t.findViewById(R.id.inputTitle)).setText(inputTitle);
		t.setUi();
		return (LinearLayout)t.findViewById(R.id.inputButtonBar);
	}

	public static void eatenByGrue()
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

	public boolean archeryMinigame (final int threshold, final double time) // Reimplement this like KEYSEC.
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

		out.writeParcelable(shadows, n);
		out.writeParcelable(pits, n);
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
}
