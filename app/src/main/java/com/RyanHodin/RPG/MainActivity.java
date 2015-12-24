package com.RyanHodin.RPG;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity
{
	private final MainActivity t=this;
	public int currentView;
	public Random gen=new Random();
	private static final String LogTag="RPG";
	public static double TARGET_FPS=60.0; // This can be altered as time progresses.

	public Cconfig config=new Cconfig(); // To hold our configuration.
	public Cuser user=new Cuser();
	public Cgame game;

	public boolean waiting; // True if certain bugs are being avoided by suspending certain work.

	public Thread th;

	/** Called when the activity is first created. */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		/* DELETE THIS BLOCK FOR RELEASE*/
		/**/
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().penaltyFlashScreen().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
		/**/

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		currentView=R.id.mainLayout;
		getActionBar().hide();
		setUi();
		ActivityManager am=(ActivityManager)getSystemService(ACTIVITY_SERVICE);
		if ((Build.VERSION.SDK_INT>=19 && am.isLowRamDevice()) || 20<am.getMemoryClass())
		{
			if (config.batching<10)
				++config.batching;
			TARGET_FPS=52.5;
		}
		else if (am.getMemoryClass()>50)
			TARGET_FPS=getWindowManager().getDefaultDisplay().getRefreshRate();
		Cconfig.t=this;
		config.readPrefs();
		Cuser.t=this;
		Cgame.t=this;
		Cweapon.t=this;
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
	}

	public void switchLayout(final View source)
	{
		t.th=new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				fadeout();
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						switch (source.getId())
						{
						case R.id.mainConfigureButton:
							recreateConfigUI();
							break;
						case R.id.mainAboutButton:
							setContentView(R.layout.about);
							currentView=R.id.aboutLayout;
							setUi();
							break;
						case R.id.mainHowToButton:
							setContentView(R.layout.howto);
							currentView=R.id.howToLayout;
							setUi();

							formatHowTo();
							break;
						case R.id.mainLoadGameButton:
							setContentView(R.layout.loadgame);
							currentView=R.id.loadGameLayout;
							setUi();

							th.interrupt();
							th=new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									final List<Button> saves=getSavedGameButtons(new SaveCallback()
									{
										@Override
										public void call(int savenum)
										{
											config.gameNumber=savenum;
											t.th=new Thread(new Runnable()
											{
												@Override
												public void run()
												{
													t.loadGame();
													t.game.runStage();
												}
											});

											runOnUiThread(new Runnable()
											{
												@Override
												public void run()
												{
													findViewById(R.id.loadGameLoadingLayout).setVisibility(View.VISIBLE);
												}
											});
											t.th.start();
										}
									});
									if (saves!=null)
									{
										runOnUiThread(new Runnable()
										{
											@Override
											public void run()
											{
												final LinearLayout l=(LinearLayout)t.findViewById(R.id.loadGameSlots);
												t.findViewById(R.id.loadGameScroller).setVisibility(View.VISIBLE);
												t.findViewById(R.id.loadGameManageButton).setVisibility(View.VISIBLE);
												t.findViewById(R.id.loadGameNoneLayout).setVisibility(View.GONE);

												for (Button b : saves)
													l.addView(b);
											}
										});
									}
								}
							});
							th.start();
							break;
						case R.id.loadGameManageButton:
							setContentView(R.layout.savemanager);
							currentView=R.id.saveManagerLayout;
							setUi();
							break;
						case R.id.deadContinue:
							setContentView(R.layout.quit);
							currentView=R.id.quitLayout;
							setUi();
							break;
						case R.id.quitQuitButton:
							config.difficultyComputer.interrupt();
							setContentView(R.layout.game);
							currentView=R.id.gameLayout;
							setUi();

							user.worker=new Thread (new Runnable ()
							{
								@Override
								public void run()
								{
									th=new Thread (new Runnable ()
									{
										@Override
										public void run()
										{
											say("Goodbye!","Goodbye, "+user.name+((config.gender && config.addressGender) ? ", "+user.genderAddress : "")+"!!");
										}
									});
									th.start();
									snooze(AnimationUtils.loadAnimation(t, R.anim.fadeout).getDuration());
									delay(gen.nextInt(250)+250);
									runOnUiThread(new Runnable ()
									{
										@Override
										public void run ()
										{
											View v=findViewById(R.id.gameContinueButton);
											if (v==null)
												finish();
											Button b=(Button)v;
											b.setOnClickListener(new OnClickListener()
											{
												@Override
												public void onClick (View v)
												{
													th.interrupt();
													t.th=new Thread(new Runnable()
													{
														@Override
														public void run()
														{
															fadeout();
															finish();
														}
													});
													t.th.start();
												}
											});
											b.setVisibility(View.VISIBLE);
										}
									});
								}
							});
							user.worker.start();
							break;
						}
					}
				});
			}
		});
		t.th.start();
	}

	@Override
	public void onBackPressed()
	{
		if (th!=null && currentView!=R.id.gameLayout)
			th.interrupt();
		th=new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				switch (currentView)
				{
				case R.id.aboutLayout:
				case R.id.configLayout:
				case R.id.howToLayout:
					if (config.difficultyComputer!=null)
						config.difficultyComputer.interrupt();
					fadeout();
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							setContentView(R.layout.main);
							currentView=R.id.mainLayout;
							setUi();
						}
					});
					break;
				case R.id.inputLayout:
					game.inputClear();
					break;
				case R.id.quitLayout:
					if (game.doCommitSuicide)
						user.commitSuicide();
					else
					{
						if (config.difficultyComputer!=null)
							config.difficultyComputer.interrupt();
						fadeout();
						runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								setContentView(R.layout.main);
								currentView=R.id.mainLayout;
								setUi();
							}
						});
					}
					break;
				case R.id.loadGameLayout:
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							AlertDialog.Builder build=new AlertDialog.Builder(t);
							build.setTitle("Exit loader?");
							build.setCancelable(false)
							.setMessage("Are you sure you would like to exit this screen and cancel any pending loads?")
							.setPositiveButton("Yes", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface d, int id)
								{
									th.interrupt();
									th=new Thread(new Runnable()
									{
										@Override
										public void run()
										{
											if (config.difficultyComputer!=null)
												config.difficultyComputer.interrupt();
											saveGame();
											fadeout();
											runOnUiThread(new Runnable()
											{
												@Override
												public void run()
												{
													setContentView(R.layout.main);
													currentView=R.id.mainLayout;
													setUi();
													config.gameNumber=-1;
												}
											});
										}
									});
									th.start();
								}
							})
							.setNegativeButton("No", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface d, int id)
								{
									d.cancel();
								}
							}).create().show();
						}
					});
					break;
				case R.id.grueLayout:
				case R.id.gameLayout:
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							AlertDialog.Builder build=new AlertDialog.Builder(t);
							build.setTitle("Exit game?");
							build.setCancelable(false)
							.setMessage("Are you sure you would like to exit the game?\n\nYou will lose all unsaved progress.")
							.setPositiveButton("Yes", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface d, int id)
								{
									th.interrupt();
									th=new Thread(new Runnable()
									{
										@Override
										public void run()
										{
											if (config.difficultyComputer!=null)
												config.difficultyComputer.interrupt();
											saveGame();
											fadeout();
											runOnUiThread(new Runnable()
											{
												@Override
												public void run()
												{
													setContentView(R.layout.main);
													currentView=R.id.mainLayout;
													setUi();
													config.gameNumber=-1;
												}
											});
										}
									});
									th.start();
								}
							})
							.setNegativeButton("No", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface d, int id)
								{
									d.cancel();
								}
							}).create().show();
						}
					});
					break;
				case R.id.saveManagerLayout:
					if (config.difficultyComputer!=null)
						config.difficultyComputer.interrupt();
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							View v=new View(t);
							v.setId(R.id.mainLoadGameButton);
							t.switchLayout(v);
						}
					});
					break;
				case R.id.aimerLayout:
				case R.id.archeryLayout:
					break; // Do nothing.
				case R.id.mainLayout:
				default:
					fadeout();
					finish();
				}
			}
		});
		t.th.start();
	}

	public void onBackPressed (View v)
	{
		onBackPressed(); // This function only exists to let buttons call through to onBackPressed().
	}

	public void restartGame (View v)
	{
		game=new Cgame();
		fadeout();
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				setContentView(R.layout.game);
				currentView=R.id.gameLayout;
				setUi();
				th=new Thread (new Runnable ()
				{
					@Override
					public void run()
					{
						game.stage=2;
						user.weapon=new Cweapon();
						user.dead=false;
						game.runStage();
					}
				});
				th.start();
			}
		});
	}

	public void startPlay (View v)
	{
		th=new Thread (new Runnable()
		{
			@Override
			public void run()
			{
				if (config.batching>1)
					TARGET_FPS=Math.max(15.0, TARGET_FPS-config.batching);
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						displayHandler.postDelayed(new Runnable() // Reset our UI and title in a quarter second plus the duration of our fadein animation, since our EditText messed with it. If anyone asks, I did NOT tell you it was okay to program like this.
						{
							public void run()
							{
								setUi();
								((TextView)findViewById(R.id.gameTitle)).setText("Welcome");
							}
						}, 250+AnimationUtils.loadAnimation(t, R.anim.fadein).getDuration());
					}
				});
				say("Welcome","\tYou awake in a cold cave. There are sounds all around you, of creatures and water alike. You try to remember something, anything, but you can barely remember your own name.\n\n\tWhat is it?");
				runOnUiThread(new Runnable ()
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
									TextView tv=((TextView)findViewById(R.id.gameTitle));
									LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)tv.getLayoutParams();
									int result=0;
									int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
									if (resourceId > 0) {
										result = getResources().getDimensionPixelSize(resourceId);
									}
									lp.topMargin=result+10;
									tv.setAlpha(1);
								}
							}
						});
						ev.setBackgroundColor(Color.rgb(128,128,128));
						ev.setHint("What's your name?");
						ev.setHintTextColor(Color.WHITE);
						ev.setTextColor(Color.BLACK);
						ev.setShadowLayer(5.0f,5,5,Color.WHITE);
						ev.setImeActionLabel("Submit", KeyEvent.KEYCODE_ENTER); // Consider changing this label.
						ev.setImeOptions(EditorInfo.IME_ACTION_SEND);
						ev.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
						View v=findViewById(R.id.gameLayout);
						if (v!=null)
							((LinearLayout)v).addView(ev);
						ev.requestFocus();
						((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(ev, InputMethodManager.SHOW_FORCED); // Force the soft keyboard open.
						setUi(); // Doubly ensure sanity. See above comment.
						((TextView)findViewById(R.id.gameTitle)).setText("Welcome");
						ev.setOnEditorActionListener(new TextView.OnEditorActionListener() // Listen for submit or enter.
						{
							@Override
							public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
							{
								if (actionId==EditorInfo.IME_ACTION_SEND || actionId==EditorInfo.IME_NULL)
								{
									view.setOnEditorActionListener(null); // Prevents some accidental double entry bugs.

									game=new Cgame();
									config.startCompute();
									// Close the soft keyboard, now that there's nothing for it to write to.
									((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
									String str=view.getText().toString().trim();
									if ("".equals(str) || str==null)
										user.name="Blondie";
									else
										user.name=capitalize(str);
									th=new Thread (new Runnable()
									{
										@Override
										public void run ()
										{
											config.updateDifficultyOnce();
											frameTick();
											game.runStage();
										}
									});
									th.start();
									return true;
								}
								return false;
							}
						});
						config.computerPaused=true;
					}
				});
			}
		});
		th.start();
	}

	public void resetConfigToDefault(View v)
	{
		if (config.difficultyComputer!=null)
			config.difficultyComputer.interrupt();
		config=new Cconfig();
		if (currentView==R.id.configLayout)
			recreateConfigUI();
		if (game!=null)
			config.startCompute();
	}

	private void recreateConfigUI()
	{
		if (currentView!=R.id.configLayout)
		{
			th=new Thread(new Runnable()
			{
				@Override
				public void run() // Do this again in the config view.
				{
					fadeout();
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							setContentView(R.layout.config);
							currentView=R.id.configLayout;
							TextView tv=(TextView)findViewById(R.id.configAutosaveWarning);
							tv.setText('\t'+tv.getText().toString());
							setUi();
							recreateConfigUI();
						}
					});
				}
			});
			th.start();
			return;
		}
		SeekBar diff=(SeekBar)findViewById(R.id.configDifficulty);
		diff.setMax(100);
		diff.setProgress(config.difficulty);
		diff.setOnSeekBarChangeListener(new OnSeekBarChangeListener ()
		{
			@Override
			public void onProgressChanged(SeekBar s, int lev, boolean user)
			{
				if (user)
				{
					config.difficulty=lev;
					if (config.difficultyComputer!=null)
					{
						config.difficultyComputer.interrupt();
						config.difficultyComputer=null;
					}
				}
			}

			@Override public void onStartTrackingTouch(SeekBar b){}
			@Override public void onStopTrackingTouch(SeekBar b){}
		});

		SeekBar delay=(SeekBar)findViewById(R.id.configDelay);
		delay.setMax(200);
		delay.setProgress((int)(100.0*config.pauseMultiplier));
		delay.setOnSeekBarChangeListener(new OnSeekBarChangeListener ()
		{
			@Override
			public void onProgressChanged(SeekBar s, int lev, boolean user)
			{
				if (user)
				{
					if (lev==0 ^ config.pauseMultiplier==0.0)
						showConfigBatch(lev!=0);
					config.pauseMultiplier=lev/100.0;
					if (lev<=60 && lev>0)
					{
						config.batching=(int)Math.min(Math.pow(config.pauseMultiplier, -1), 10)-1;
						((SeekBar)findViewById(R.id.configBatch)).setProgress(config.batching);
					}
				}
			}

			@Override public void onStartTrackingTouch(SeekBar b){}
			@Override public void onStopTrackingTouch(SeekBar b){}
		});
		showConfigBatch(config.pauseMultiplier!=0);

		Switch EE=(Switch)findViewById(R.id.configEasterEgg);
		EE.setChecked(config.easterEggs);
		EE.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton swit, boolean on)
			{
				showConfigEasterEggOptions(on);
				config.easterEggs = on;
			}
		});
		showConfigEasterEggOptions(config.easterEggs);

		Switch SM=(Switch)findViewById(R.id.configSpecMon);
		SM.setChecked(config.specMon);
		SM.setOnCheckedChangeListener(new OnCheckedChangeListener ()
		{
			@Override
			public void onCheckedChanged(CompoundButton b, boolean on)
			{
				config.specMon=on;
			}
		});

		Switch TG=(Switch)findViewById(R.id.configGenderOptionsTwo);
		TG.setChecked(config.twoGender);
		TG.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton swit, boolean on)
			{
				showConfigTwoGenderOptions(!on);
				config.twoGender = on;
			}
		});

		Switch G=(Switch)findViewById(R.id.configGender);
		G.setChecked(config.gender);
		G.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton swit, boolean on)
			{
				showConfigGenderOptions(on);
				config.gender = on;
			}
		});
		showConfigGenderOptions(config.gender);

		Switch FS=(Switch)findViewById(R.id.configFullscreen);
		FS.setChecked(config.fullscreen);
		FS.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton swit, boolean on)
			{
				config.fullscreen=on;
				t.setUi();
			}
		});

		Switch AS=(Switch)findViewById(R.id.configAutosave);
		AS.setChecked(config.autosave);
		AS.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged (CompoundButton swit, boolean on)
			{
				config.autosave=on;
				showConfigAutosaveWarning(on);
			}
		});
		showConfigAutosaveWarning(config.autosave);
	}

	private void showConfigBatch(boolean show)
	{
		findViewById(R.id.configBatchLayout).setVisibility(show ? View.VISIBLE : View.GONE);
		if (show)
		{
			SeekBar batching=(SeekBar)findViewById(R.id.configBatch);
			batching.setMax(9);
			batching.setProgress(config.batching-1);
			batching.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
			{
				@Override
				public void onProgressChanged(SeekBar s, int lev, boolean user)
				{
					if (user)
						config.batching=lev+1;
				}

				@Override public void onStartTrackingTouch(SeekBar b){}
				@Override public void onStopTrackingTouch(SeekBar b){}
			});
		}
	}

	private void showConfigEasterEggOptions (boolean show)
	{
		findViewById(R.id.configEasterOptionsLayout).setVisibility(show ? View.VISIBLE : View.GONE);
		if (show)
		{
			SeekBar freq=(SeekBar)findViewById(R.id.configEasterOptionsOdds);
			freq.setMax(100);
			freq.setProgress(config.easterFrequency);
			freq.setOnSeekBarChangeListener(new OnSeekBarChangeListener ()
			{
				@Override
				public void onProgressChanged(SeekBar s, int lev, boolean user)
				{
					if (user)
						config.easterFrequency=lev;
				}

				@Override public void onStartTrackingTouch(SeekBar b){}
				@Override public void onStopTrackingTouch(SeekBar b){}
			});

			Switch GoT=(Switch)findViewById(R.id.configEasterOptionsGoT);
			GoT.setChecked(config.GoTEggs);
			GoT.setOnCheckedChangeListener(new OnCheckedChangeListener ()
			{
				@Override
				public void onCheckedChanged(CompoundButton b, boolean check)
				{
					config.GoTEggs=check;
				}
			});

			Switch ES=(Switch)findViewById(R.id.configEasterOptionsES);
			ES.setChecked(config.ESEggs);
			ES.setOnCheckedChangeListener(new OnCheckedChangeListener ()
			{
				@Override
				public void onCheckedChanged(CompoundButton b, boolean check)
				{
					config.ESEggs=check;
				}
			});

			Switch SCH=(Switch)findViewById(R.id.configEasterOptionsScholastic);
			SCH.setChecked(config.schoolEggs);
			SCH.setOnCheckedChangeListener(new OnCheckedChangeListener ()
			{
				@Override
				public void onCheckedChanged(CompoundButton b, boolean check)
				{
					config.schoolEggs=check;
				}
			});

			Switch LIT=(Switch)findViewById(R.id.configEasterOptionsLit);
			LIT.setChecked(config.litEggs);
			LIT.setOnCheckedChangeListener(new OnCheckedChangeListener ()
			{
				@Override
				public void onCheckedChanged(CompoundButton b, boolean check)
				{
					config.litEggs=check;
				}
			});
		}
	}

	private void showConfigTwoGenderOptions (boolean show)
	{
		findViewById(R.id.configGenderOptionsMultiLayout).setVisibility(show ? View.VISIBLE : View.GONE);
		if (show)
		{
			Switch SG=(Switch)findViewById(R.id.configGenderOptionsSpecial);
			SG.setChecked(config.specialGender);
			SG.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				@Override
				public void onCheckedChanged(CompoundButton b, boolean show)
				{
					config.specialGender=show;
				}
			});

			Switch CG=(Switch)findViewById(R.id.configGenderOptionsCustom);
			CG.setChecked(config.customGender);
			CG.setOnCheckedChangeListener(new OnCheckedChangeListener ()
			{
				@Override
				public void onCheckedChanged(CompoundButton b, boolean show)
				{
					config.customGender=show;
				}
			});
		}
	}

	public void showConfigGenderOptions (boolean show)
	{
		findViewById(R.id.configGenderOptionsLayout).setVisibility(show ? View.VISIBLE : View.GONE);
		if (show)
		{
			showConfigTwoGenderOptions(!config.twoGender);

			Switch GA=(Switch)findViewById(R.id.configGenderOptionsAddress);
			GA.setChecked(config.addressGender);
			GA.setOnCheckedChangeListener(new OnCheckedChangeListener ()
			{
				@Override
				public void onCheckedChanged(CompoundButton b, boolean on)
				{
					config.addressGender=on;
				}
			});
		}
	}

	public void showConfigAutosaveWarning (boolean show)
	{
		findViewById(R.id.configAutosaveWarningLayout).setVisibility(show ? View.GONE : View.VISIBLE);
	}

	public void formatHowTo()
	{
		TextView tv=(TextView)findViewById(R.id.howToOne);
		tv.setText('\t'+tv.getText().toString());
		tv=(TextView)findViewById(R.id.howToTwo);
		tv.setText('\t'+tv.getText().toString());
		tv=(TextView)findViewById(R.id.howToThree);
		tv.setText('\t'+tv.getText().toString());
	}

	public void showSaveManagerSaves(final View from)
	{
		if (currentView!=R.id.saveManagerLayout)
		{
			View v=new View(t);
			v.setId(R.id.loadGameManageButton); // Switch quickly to the SaveManager
			switchLayout(v);
		}
		findViewById(R.id.saveManagerProcessingLayout).setVisibility(View.VISIBLE);
		((TextView)findViewById(R.id.saveManagerProcessingTitle)).setText("Please wait, loading list of saves...");
		th.interrupt();
		th=new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				switch (from.getId())
				{
				case R.id.saveManagerDeleteButton:
					t.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							t.findViewById(R.id.saveManagerScroller).setVisibility(View.VISIBLE);
							t.findViewById(R.id.saveManagerSavesLayout).setVisibility(View.VISIBLE);
						}
					});
					List<Button> saves=getSavedGameButtons(new SaveCallback()
					{
						@Override
						public void call(final int savenum)
						{
							th=new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									deleteSave(savenum);
									fadeout();
									runOnUiThread(new Runnable()
									{
										@Override
										public void run()
										{
											setContentView(R.layout.loadgame);
											currentView=R.id.loadGameLayout;
											View v=new View(t);
											v.setId(R.id.loadGameManageButton); // Switch quickly to the SaveManager, to regenerate the layout.
											switchLayout(v);
										}
									});

									// This code causes crashes. It would otherwise reopen the deletion screen
									//									t.frameTick(); // Wait for the layout to be created.
									//									runOnUiThread(new Runnable()
									//									{
									//										@Override
									//										public void run()
									//										{
									//											View v=new View(t);
									//											v.setId(R.id.saveManagerDeleteButton);
									//											showSaveManagerSaves(v); // Regenerate the list of saves.
									//										}
									//									});
								}
							});
							th.start();
						}
					});
					final LinearLayout l=(LinearLayout)t.findViewById(R.id.saveManagerSavesLayout);
					for (final Button b : saves)
					{
						t.runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								b.setTextColor(Color.RED);
								l.addView(b);
							}
						});
					}
					break;
				}
				t.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						findViewById(R.id.saveManagerProcessingLayout).setVisibility(View.GONE);
					}
				});
			}
		});
		th.start();
	}

	public boolean deleteSave(final int savenum) // Returns true if there was an error
	{
		SharedPreferences sp=getSharedPreferences("RPG Savegames", 0);
		int num=sp.getInt("SaveGameCount", 0); // Total number of savegames pre-edit
		if (num<savenum)
			return true;
		SharedPreferences.Editor edit=sp.edit();
		edit.putInt("SaveGameCount", num-1); // There is now one less savegame.
		if (Build.VERSION.SDK_INT>=9)
			edit.apply();
		else
			edit.commit();

		for (int i=savenum; i<num; ++i) // Move all savegames up a slot
		{
			loadGameFrom(i+1);
			saveGameTo(i);
		}

		edit=getSharedPreferences("RPG save game "+num, 0).edit(); // Clear out the last savegame. This is the best we can do with SharedPreferences.
		edit.clear();

		if (config.gameNumber==savenum) // Unset the game number if we deleted the save
			config.gameNumber=-1;
		else if (config.gameNumber>savenum)
			--config.gameNumber; // Make sure that this still points to the same save

		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Toast.makeText(t, "Savegame "+savenum+" deleted.", Toast.LENGTH_LONG).show();
			}
		});
		return false;
	}

	public void sayWhat (String what) // Common say() code.
	{
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY-2);
		what="\t"+what;
		if (config.pauseMultiplier==0.0)
			sendUIMessage(what);
		else
		{
			if (config.batching<=1)
			{
				String strs[]=what.split("\n"); // Split into lines, to pause in between lines.
				for (int i=0; i<strs.length; ++i) // For each line...
				{
					for(int n=0; n<strs[i].length(); ++n) // And each character...
					{
						if (Thread.interrupted())
							return;
						if (sendUICharacter(strs[i].charAt(n))) // Send it to the UI.
							return;
					}
					if (sendUICharacter('\n')) // Finalize the line, enabling input if this is the last line, and setting the input hint to the last line.
						return;
					if (pause(gen.nextInt(1000)+500)) // Wait after each line.
						return;
				}
			}
			else
			{
				String[] strs=what.split("\n");
				for (int i=0; i<strs.length; ++i)
				{
					while (strs[i].length()!=0)
					{
						if (sendUIBatch(strs[i].substring(0, Math.min(config.batching, strs[i].length()))))
							return;
						if (config.batching<=strs[i].length())
							strs[i]=strs[i].substring(config.batching);
						else
							strs[i]="";
					}
					if (sendUICharacter('\n'))
						return;
					if (pause(gen.nextInt(1000)+500))
						return;
				}
			}
		}
		Cgame.signalGameContinue();
	}

	public void say (String what)
	{
		waiting=true; // Mismatch bugs with switching.
		displayHandler.removeMessages(1);
		if (Thread.interrupted())
			return;
		fadeout();
		runOnUiThread(new Runnable ()
		{
			@Override
			public void run()
			{
				setContentView(R.layout.game);
				((TextView)findViewById(R.id.gameText)).setText("");
			}
		});
		currentView=R.id.gameLayout;
		setUi();
		waiting=false;
		sayWhat(what);
	}

	public void say (final String title, String what)
	{
		waiting=true; // Mismatch bugs with switching.
		displayHandler.removeMessages(1);
		if (Thread.interrupted())
			return;
		fadeout();
		runOnUiThread(new Runnable ()
		{
			@Override
			public void run()
			{
				setContentView(R.layout.game);
				((TextView)findViewById(R.id.gameText)).setText("");
				((TextView)findViewById(R.id.gameTitle)).setText(title);
				findViewById(R.id.gameTitle).setVisibility(View.VISIBLE);
			}
		});

		currentView=R.id.gameLayout;
		setUi();
		waiting=false;
		sayWhat(what);
	}

	protected void sendUIMessage (String message) // Send a full message to the UI.
	{
		Message m=Message.obtain();
		if (m==null)
			m=new Message();
		m.obj=message;
		m.what=1;
		m.setTarget(displayHandler);
		m.sendToTarget();
	}

	protected boolean sendUICharacter (char c) // Send a single character to the UI. No input or input hint. Pause to simulate typing.
	{
		Message m=Message.obtain();
		if (m==null)
			m=new Message();
		m.obj=new String(""+c);
		m.what=1;
		if (pause(gen.nextInt(gen.nextInt(80)+1)+gen.nextInt(gen.nextInt(80)+1)+10+m.getWhen())) // This line controls typing speed across the entire Activity.

			/***********************************************************************
			 * Note that the above pause duration is very important.               *
			 * If it is too small, then typing will be unnaturally fast.           *
			 * If it is too large, then typing will be annoyingly slow.            *
			 * If it is too random, then typing speed will vary wildly.            *
			 *   (This may not be a bad thing, if it is intentional.)              *
			 * Finally, if it isn't random enough, then typing will be monotonous. *
			 * Be careful when tweaking it. TAKE BACKUPS!!!                        *
			 ***********************************************************************/

		{
			displayHandler.removeMessages(1);
			return true;
		}

		m.setTarget(displayHandler);
		m.sendToTarget();
		return false;
	}

	protected boolean sendUIBatch (String b) // Send a single batch to the UI. No input or input hint. Pause to simulate typing.
	{
		Message m=Message.obtain();
		if (m==null)
			m=new Message();
		m.obj=b;
		m.what=1;
		int upper=50*b.length();
		if (pause(gen.nextInt(gen.nextInt(upper)+1)+gen.nextInt(gen.nextInt(upper)+1)+10+m.getWhen())) // This line controls typing speed across the entire Activity.

			/***********************************************************************
			 * Note that the above pause duration is very important.               *
			 * If it is too small, then typing will be unnaturally fast.           *
			 * If it is too large, then typing will be annoyingly slow.            *
			 * If it is too random, then typing speed will vary wildly.            *
			 *   (This may not be a bad thing, if it is intentional.)              *
			 * Finally, if it isn't random enough, then typing will be monotonous. *
			 * Be careful when tweaking it. TAKE BACKUPS!!!                        *
			 ***********************************************************************/

		{
			displayHandler.removeMessages(1);
			return true;
		}

		m.setTarget(displayHandler);
		m.sendToTarget();
		return false;
	}

	public static class parcelableView implements Serializable, Parcelable
	{
		public static final long serialVersionUID=1;

		public int view;
		public static final int GAME=0;
		public static final int MAIN=1;
		public static final int CONFIG=2;
		public static final int ABOUT=3;
		public static final int INPUT=4;
		public static final int QUIT=5;
		public static final int DEAD=6;
		public static final int GRUE=7;
		public static final int ARCHERY=8;
		public static final int AIMER=9;
		public static final int HOWTO=10;
		public static final int LOADGAME=11;
		public static final int SAVEMANAGER=12;

		public parcelableView(int v)
		{
			view=v;
		}

		private parcelableView(Parcel in)
		{
			view=in.readInt();
		}

		public static parcelableView getByViewId (int id)
		{
			int view=-1;
			switch (id)
			{
			case R.id.aboutLayout:
				view=ABOUT;
				break;
			case R.id.configLayout:
				view=CONFIG;
				break;
			case R.id.deadLayout:
				view=DEAD;
				break;
			case R.id.gameLayout:
				view=GAME;
				break;
			case R.id.grueLayout:
				view=GRUE;
				break;
			case R.id.inputLayout:
				view=INPUT;
				break;
			case R.id.mainLayout:
				view=MAIN;
				break;
			case R.id.quitLayout:
				view=QUIT;
				break;
			case R.id.archeryLayout:
				view=ARCHERY;
				break;
			case R.id.aimerLayout:
				view=AIMER;
				break;
			case R.id.howToLayout:
				view=HOWTO;
				break;
			case R.id.loadGameLayout:
				view=LOADGAME;
				break;
			case R.id.saveManagerLayout:
				view=SAVEMANAGER;
				break;
			default:
				view=id;
			}
			return new parcelableView(view);
		}

		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		public int getId ()
		{
			switch (view)
			{
			case ABOUT:
				return R.id.aboutLayout;
			case CONFIG:
				return R.id.configLayout;
			case DEAD:
				return R.id.deadLayout;
			case GAME:
				return R.id.gameLayout;
			case GRUE:
				return R.id.grueLayout;
			case INPUT:
				return R.id.inputLayout;
			case MAIN:
				return R.id.mainLayout;
			case QUIT:
				return R.id.quitLayout;
			case ARCHERY:
				return R.id.archeryLayout;
			case AIMER:
				return R.id.aimerLayout;
			case HOWTO:
				return R.id.howToLayout;
			case LOADGAME:
				return R.id.loadGameLayout;
			case SAVEMANAGER:
				return R.id.saveManagerLayout;
			default:
				Random gen=new Random();
				return Build.VERSION.SDK_INT>=17 ? View.generateViewId() : gen.nextInt();
			}
		}

		@Override
		public int describeContents ()
		{
			return 8;
		}

		@Override
		public void writeToParcel (Parcel out, int j)
		{
			out.writeInt(view);
		}

		public static final Parcelable.Creator<parcelableView> CREATOR = new Parcelable.Creator<parcelableView> ()
				{
			@Override
			public parcelableView createFromParcel (Parcel in)
			{
				return new parcelableView(in);
			}

			@Override
			public parcelableView[] newArray (int n)
			{
				return new parcelableView[n];
			}
				};
	}

	public void syncCurrentView()
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run ()
			{
				int resid;
				switch (currentView)
				{
				case R.id.mainLayout:
					resid=R.layout.main;
					break;
				case R.id.configLayout:
					resid=R.layout.config;
					break;
				case R.id.aboutLayout:
					resid=R.layout.about;
					break;
				case R.id.gameLayout:
					resid=R.layout.game;
					break;
				case R.id.grueLayout:
					resid=R.layout.grue;
					break;
				case R.id.deadLayout:
					resid=R.layout.dead;
					break;
				case R.id.quitLayout:
					resid=R.layout.quit;
					break;
				case R.id.archeryLayout:
					resid=R.layout.archery;
					break;
				case R.id.aimerLayout:
					resid=R.layout.aimer;
					break;
				case R.id.howToLayout:
					resid=R.layout.howto;
					break;
				case R.id.loadGameLayout:
					resid=R.layout.loadgame;
					break;
				case R.id.saveManagerLayout:
					resid=R.layout.savemanager;
					break;
				default:
					logError("Unknown currentView passed to syncCurrentView(): "+currentView+". Assume R.id.gameLayout.");
					resid=R.layout.game;
					currentView=R.id.gameLayout;
					log("View set: "+currentView);
				}
				setContentView(resid);
			}
		});
	}

	@SuppressLint("HandlerLeak")
	public Handler displayHandler=new Handler ()
	{
		@Override
		public void handleMessage (Message m)
		{
			View v=findViewById(R.id.gameText);
			if (v!=null)
				((TextView)v).append(m.obj.toString());
		}
	};

	public String capitalize(String str) // Capitalize the fist character of a string. Usually, this can be used to guarantee that an output is gramatically correct.
	{
		return new String(Character.toUpperCase(str.charAt(0))+str.substring(1));
	}

	public boolean frameTick()
	{
		return snooze((long)(1000.0/TARGET_FPS));
	}

	public boolean frameTick(int sub)
	{
		return snooze(((long)(1000.0/TARGET_FPS))-sub);
	}

	public boolean pause(long ms) // Pause for an integer number of milliseconds, respecting completely config.pauseMultiplier.
	{
		Thread.yield();
		ms*=config.pauseMultiplier;
		if (ms==0)
			return false;
		synchronized (t)
		{
			try
			{
				Thread.sleep(ms);
			}
			catch(InterruptedException ie)
			{
				logError("Thread "+Thread.currentThread().getId()+" wait interrupted.");
				log("Thread was waiting for: "+ms+"ms.");
				return true;
			}
		}
		return false;
	}

	public boolean delay(long ms) // Pause for an integer number of milliseconds, clamping config.pauseMultiplier to be between .1 and 1.
	{
		Thread.yield();
		if (ms==0)
			return false;
		ms*=Math.max(Math.min(config.pauseMultiplier, .1), 1);
		synchronized (t)
		{
			try
			{
				Thread.sleep(ms);
			}
			catch(InterruptedException ie)
			{
				logError("Thread "+Thread.currentThread().getId()+" delay interrupted.");
				log("Thread was waiting for: "+ms+"ms.");
				return true;
			}
		}
		return false;
	}

	public boolean snooze (long ms) // Pause for an integer number of milliseconds, disregarding the pauseMultiplier entirely.
	{
		Thread.yield();
		if (ms==0)
			return false;
		synchronized (t)
		{
			try
			{
				Thread.sleep(ms);
			}
			catch(InterruptedException ie)
			{
				logError("Thread "+Thread.currentThread().getId()+" delay interrupted.");
				log("Thread was waiting for: "+ms+"ms.");
				return true;
			}
		}
		return false;
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public void setUi ()
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				findViewById(currentView).setSystemUiVisibility(config.fullscreen ? View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION : View.SYSTEM_UI_FLAG_LOW_PROFILE);
			}
		});
	}

	public void fadeout()
	{
		final Animation a=AnimationUtils.loadAnimation(this, R.anim.fadeout);
		final View v=findViewById(currentView);
		if (v!=null) // Even if v is null, we let the game wait for the animation to not play 
			// This avoids some issues with code that expects fadeout() to play - Even if something happens to currentView, we should still let that code catch up.
		{
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					v.startAnimation(a);
				}
			});
		}
		snooze(a.getDuration());
	}

	public void saveGame()
	{
		if (game==null)
			return; // If game is null, we don't have anything to save.
		SharedPreferences sp=getSharedPreferences("RPG Savegames", 0);
		SharedPreferences.Editor edit=sp.edit();
		int num;
		if (config.gameNumber==-1)
		{
			num=sp.getInt("SaveGameCount", 0)+1;
			config.gameNumber=num;
			edit.putInt("SaveGameCount", num);
		}
		else
			num=config.gameNumber;

		edit.putString("SaveGame"+num, user.toString()); // Update the save name. TODO change this in a later release, when save names can be changed.
		if (Build.VERSION.SDK_INT>=9)
			edit.apply();
		else
			edit.commit();

		sp=getSharedPreferences("RPG save game "+num, 0);
		edit=sp.edit();
		user.saveTo(edit);
		game.saveTo(edit);
		config.writePrefs(edit); // Config gets saved too. Avoids issues with config being mutated mid game

		if (Build.VERSION.SDK_INT>=9)
			edit.apply();
		else
			edit.commit();
	}

	public void saveGameTo (int n) // Saves the current game to slot n or SaveGameCount+1, whichever is lower, without permanently mutating config.
	{
		int tmp=config.gameNumber;
		SharedPreferences sp=getSharedPreferences("RPG Savegames", 0);
		int num=sp.getInt("SaveGameCount", 0)+1;
		config.gameNumber=Math.min(n, num);
		saveGame();
		config.gameNumber=tmp;
	}

	public void loadGame()
	{
		SharedPreferences sp=getSharedPreferences("RPG save game "+config.gameNumber, 0);
		if (Thread.interrupted())
			return;
		user.loadFrom(sp);
		if (Thread.interrupted())
		{
			user=new Cuser();
			return;
		}
		if (game==null)
			game=new Cgame();
		if (Thread.interrupted())
		{
			user=new Cuser();
			return;
		}
		game.loadFrom(sp);

		config.writePrefs(); // Config saves to the default. Just to make sure its there.

		Cconfig bak=null; // For persist.
		if (config.persist)
			bak=config;
		config.readPrefs(sp);
		if (bak.persist) // Restore config values that shouldn't be changed in save load 
		{
			config.batching=bak.batching;
			config.fullscreen=bak.fullscreen;
			config.pauseMultiplier=bak.pauseMultiplier;
		}
	}

	public void loadGameFrom (int n) // Loads the current game from slot n without permanently mutating config.
	{
		int tmp=config.gameNumber;
		SharedPreferences sp=getSharedPreferences("RPG Savegames", 0);
		config.gameNumber=n;
		loadGame();
		config.gameNumber=tmp;
	}

	@Override
	public void onLowMemory()
	{
		super.onLowMemory();

		logWarning("onLowMemory() called: Releasing all non critical resources");
		log("Reducing application FPS target (old target "+TARGET_FPS+"FPS)");
		if (config.difficultyComputer!=null)
		{
			config.difficultyComputer.interrupt();
			config.difficultyComputer=null;
		}
		if (user.genderAddress!=null)
		{
			user.genderAddressComp=null;
			if (user.parsedGender!=-1)
				user.genderComp=null;
		}
		TARGET_FPS=Math.max(15.0, TARGET_FPS-10.0);
		log("FPS target reduced to "+TARGET_FPS+"FPS");
	}

	@Override
	public void onTrimMemory (int level)
	{
		super.onTrimMemory(level);

		logWarning("onTrimMemory() called with level "+level+", releasing cached references and garbage collecting");
		switch (level)
		{
		case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
			logWarning("Memory level critical: Augment batching by two.");
			if (config.batching>=9)
				config.pauseMultiplier=0;
			else
				config.batching+=2;
			log("Batching level is now "+config.batching);
			log("Lowering target framerate from "+TARGET_FPS+"FPS");
			TARGET_FPS=Math.max(15.0, TARGET_FPS-10.0);
			log("New target is "+TARGET_FPS+"FPS");
			// Fall through.
		case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
		case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
			logWarning("Memory level is low, killing and releasing diffcompute thread");
			if (config.difficultyComputer!=null)
			{
				config.difficultyComputer.interrupt();
				config.difficultyComputer=null;
			}
			break;
		case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
		case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
		case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
			log("Memory level dangerous, incrementing batching");
			if (config.batching!=10)
				++config.batching;
			log("Batching is now "+config.batching);
			log("Lowering target framerate from "+TARGET_FPS+"FPS");
			TARGET_FPS=Math.max(20, TARGET_FPS-5.0);
			log("New target is "+TARGET_FPS+"FPS");
			// Fall through
		case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
			config.computerPaused=true;
			break;
		}
		if (user.genderAddress!=null)
			user.genderAddressComp=null;
		if (user.parsedGender!=-1)
			user.genderComp=null;
	}

	@Override
	public void onSaveInstanceState (Bundle out)
	{
		super.onSaveInstanceState(out);
		out.putParcelable("config", config);
		out.putParcelable("user", user);
		if (game!=null)
		{
			--game.stage;
			out.putParcelable("game", game);
		}
		out.putParcelable("view", parcelableView.getByViewId(currentView));
	}

	@Override
	public void onRestoreInstanceState (Bundle in)
	{
		super.onRestoreInstanceState(in);
		config=in.getParcelable("config");
		user=in.getParcelable("user");
		game=in.getParcelable("game");
		if (game!=null && game.stage>1)
		{
			config.startCompute();
			config.updateDifficultyOnce();
		}
		currentView=((parcelableView)in.getParcelable("view")).getId();
		syncCurrentView();
		switch (currentView)
		{
		case R.id.configLayout:
			recreateConfigUI();
			break;
		case R.id.inputLayout:
		case R.id.gameLayout:
			if (game!=null)
				game.runStage();
			else
			{
				logError("Attempted to reload null game interface. Restarting game...");
				startPlay(null); // Since startPlay() doesn't use its view, it doesn't matter if we pass it null, except to the garbage collector and in terms of the few bytes an empty View requires
			}
			break;
		case R.id.howToLayout:
			formatHowTo();
			break;
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		config.computerPaused=true;
		saveGame();
		config.writePrefs();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if (t.game!=null)
			config.startCompute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.empty, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuInflater inflater=getMenuInflater();
		switch (currentView)
		{
		case R.id.configLayout:
			inflater.inflate(R.menu.config, menu);
			break;
		case R.id.aboutLayout:
			inflater.inflate(R.menu.about, menu);
			break;
		case R.id.howToLayout:
			inflater.inflate(R.menu.howto, menu);
			break;
		case R.id.loadGameLayout:
			inflater.inflate(R.menu.loadgame, menu);
			break;
		case R.id.gameLayout:
		case R.id.grueLayout:
		case R.id.inputLayout: // Duplicate menus
			inflater.inflate(R.menu.game, menu);
			break;
		case R.id.saveManagerLayout:
			inflater.inflate(R.menu.savemanager, menu);
			break;
		case R.id.quitLayout:
			inflater.inflate(R.menu.quit, menu);
			break;
		case R.id.mainLayout:
		default:
			inflater.inflate(R.menu.main, menu);
		}
		return true;
	}

	@Override public void onOptionsMenuClosed(Menu menu)
	{
		setUi(); // Ensures that opening and closing the options menu doesn't disrupt our UI, which it usually did otherwise.
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		AlertDialog.Builder build=new AlertDialog.Builder(t);
		switch (item.getItemId())
		{
		case R.id.gameMenuSave:
			(new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					saveGame();
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							Toast.makeText(t, "Game save complete", Toast.LENGTH_SHORT).show();
						}
					});
				}
			})).start();
			Toast.makeText(t, "You may keep playing... Your game will be saved in the background", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.gameMenuLoad:
			build.setTitle("Save game?");
			build.setCancelable(true)
			.setMessage("Have you programmed this dialog to say \"Would you like to save your game before loading?\n\nYou will lose all unsaved progress if you do not.\"?")
			.setPositiveButton("Yes, I have", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface d, int id)
				{
					Toast.makeText(t, "Why you always lying?", Toast.LENGTH_LONG).show();
				}
			})
			.setNegativeButton("No, not yet", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface d, int id)
				{
					d.cancel();
				}
			}).create().show();
			return true;
		case R.id.mainMenuExit:
		case R.id.configMenuExit:
		case R.id.aboutMenuExit:
		case R.id.howToMenuExit:
		case R.id.loadGameMenuExit:
		case R.id.gameMenuExit:
		case R.id.saveManagerMenuExit:
		case R.id.quitMenuExit:
			build.setTitle("Exit game?");
			build.setCancelable(true)
			.setMessage("Are you sure you would like to exit the game?\n\nYou will lose all unsaved progress.")
			.setPositiveButton("Yes", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface d, int id)
				{
					th.interrupt();
					th=new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							saveGame();
							fadeout();
							finish();
						}
					});
					th.start();
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface d, int id)
				{
					d.cancel();
				}
			}).create().show();
			return true;
		case R.id.configMenuBack:
		case R.id.aboutMenuBack:
		case R.id.howToMenuBack:
		case R.id.loadGameMenuBack:
		case R.id.gameMenuBack:
		case R.id.saveManagerMenuBack:
			onBackPressed();
			return true;
		case R.id.quitMenuHome:
			if (th!=null)
				th.interrupt();
			th=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					fadeout();
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							setContentView(R.layout.main);
							currentView=R.id.mainLayout;
							setUi();
						}
					});
				}
			});
			th.start();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_MENU)
		{
			invalidateOptionsMenu();
			return false;
		}
		return super.onKeyUp(keyCode, event);
	}

	public List<Button> getSavedGameButtons (final SaveCallback sc) // sc.call() is called on the UI thread with the number of the save chosen by the user.
	{
		final SharedPreferences sp=getSharedPreferences("RPG Savegames", 0);
		final int max=sp.getInt("SaveGameCount", -1);
		if (max!=-1)
		{
			List<Button> out=new ArrayList<Button>(max);

			for (int i=0; i<=max; ++i)
			{
				Button b=new Button(t);
				String s=sp.getString("SaveGame"+i, ""); // Get the saved name.
				if (!s.equals("")) // If its empty, we want it. Else, we want it formatted.
					s=":\n"+s;
				b.setText("Savegame "+i+s);
				b.setTag(i);
				b.setOnClickListener(new OnClickListener()
				{	
					@Override
					public void onClick(View v)
					{
						v.setOnClickListener(null); // Prevent multiple click bugs
						Integer n=(Integer)v.getTag();
						sc.call(n);
					}
				});
				out.add(b);
			}
			return out;
		}
		return null; // No buttons.
	}

	public boolean determineUserDeath (double oddsOfDeath)
	{
		/*user.dead=true;			// Past algorithm kept for posterity. Testing found this system to be much too difficult.
		if (gen.nextDouble()>oddsOfDeath)
		{
			if (gen.nextDouble()>config.difficultyMult)
				user.dead=false;
		}*/
		user.dead=(((oddsOfDeath+config.difficultyMult)/2)<gen.nextDouble());
		//if (config.difficulty<50) // Runtime debugging
		//	user.dead=false; // Debugging only
		return user.dead;
	}

	public boolean determineUserDeath (double numerator, double denominator)
	{
		return determineUserDeath(numerator/denominator);
	}

	public boolean determineUserDeath (int oneIn)
	{
		return determineUserDeath(1, oneIn);
	}

	public String multiplyString (String what, int count) // Returns a String comprised of what multiplied count times.
	{
		if (count==0)
			return "";
		if (count==1)
			return what;
		StringBuffer sb=new StringBuffer(what);
		for (; count>1; --count)
			sb.append(what);
		return sb.toString();
	}

	public String multiplyString (char what, int count) // Returns a String comprised of what multiplied count times.
	{
		if (count==0)
			return "";
		if (count==1)
			return ""+what;
		StringBuffer sb=new StringBuffer(what);
		for (; count>1; --count)
			sb.append(what);
		return sb.toString();
	}

	public void log (String message) // Log an informational message.
	{
		Log.d(LogTag, message); // Don't worry, there is occasionally a method to my madness.
	}

	public void logWarning (String message) // Log a warning message.
	{
		Log.w(LogTag, message);
	}

	public void logError (String message) // Log an error message.
	{
		Log.e(LogTag, message);
	}

	public abstract class SaveCallback
	{
		public void call(int savenum){}
	}
}
