package com.RyanHodin.RPG;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.SharedPreferences;

import java.util.ArrayList;

/**
 * Created by Ryan on 7/24/2016.
 */
public class SavefileBackupAgent extends BackupAgentHelper {
	public void onCreate() {
		updateHelper();
	}

	// Updates the helper with the current state of game saves.
	// Used in onCreate and in MainActivity, when new saves are added or removed.
	void updateHelper() {
		SharedPreferences sp=getSharedPreferences(MainActivity.SaveDataFile, 0);
		int n=sp.getInt(MainActivity.SaveDataGameCount, -1);
		ArrayList<String> args=new ArrayList<>(n<1 ? 1 : n); // What files will be backed up
		args.add(MainActivity.SaveDataFile); // Add the savegame data file
		args.add(Cconfig.DefaultConfigFile); // Add the default config data file
		if (n>0) { // If we have any savegames
			for (int i=1; i<=n; ++i) // Add all savegame files
				args.add(MainActivity.SaveGameFilePrefix+i);
		}
		SharedPreferencesBackupHelper helper=new SharedPreferencesBackupHelper(this, args.toArray(new String[args.size()]));
		addHelper("RPG SaveGame backup", helper);
	}
}
