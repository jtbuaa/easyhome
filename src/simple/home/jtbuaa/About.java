package simple.home.jtbuaa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import base.lib.util;

public class About extends Activity{
	
	CheckBox cbShake, cbAlpha, cbTitle;
	RadioGroup rotateMode, systemApp, userApp;
	SharedPreferences perferences;
	SharedPreferences.Editor editor;

	String mPackageName;
	PackageManager mPM;
	private List<ResolveInfo> mHomeList;
	private int currentHomeIndex = 0;

    String ip() {
        //network
    	StringBuffer sb = new StringBuffer("");
		try {
			Enumeration<NetworkInterface> enumNI = NetworkInterface.getNetworkInterfaces();
			while (enumNI.hasMoreElements()) {
				NetworkInterface ni = enumNI.nextElement();
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					InetAddress local = ips.nextElement();
					if (!local.isLoopbackAddress()) {
						if (sb.length() > 0) sb.append(", ");
						sb.append(local.getHostAddress());
						break;
					}
				}
			}
			return sb.toString().trim();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
    
    String aboutMsg() {
    	DisplayMetrics dm = new DisplayMetrics();  
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		String res = runCmd("cat", "/proc/cpuinfo")
		+ "\nAndroid " + android.os.Build.VERSION.RELEASE
		+ " SDK" + android.os.Build.VERSION.SDK_INT
		+ "\n" + dm.widthPixels+" * "+dm.heightPixels + ", density:" + dm.density;
		
		String ipaddr = ip();
		if (!ipaddr.equals(""))
			res += "\n" + ipaddr;
		return res;
    }

    String runCmd(String cmd, String para) {//performance of runCmd is very low, may cause black screen. do not use it AFAC 
        String line = "";
        try {
            String []cmds={cmd, para};
            java.lang.Process proc;
            if (para != "")
                proc = Runtime.getRuntime().exec(cmds);
            else
                proc = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            while((line=br.readLine())!=null) {
            	if ((line.contains("Processor")) || (line.contains("model name")) || (line.contains("MemTotal:"))) {
            		if (line.contains("processor	: 1")) continue;
            		line = line.split(":")[1].trim();
            		break;
            	}
            }
        	br.close();
        } catch (IOException e) {
            return e.toString();
        }
        return line;
    }

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.about);

        mPackageName = this.getPackageName();
        mPM = getPackageManager();
        
        TextView tvHelp = (TextView) findViewById(R.id.help);
        tvHelp.setText(getString(R.string.help_message));
        
        Button btnVote = (Button) findViewById(R.id.vote);
        btnVote.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=simple.home.jtbuaa"));
				if (!util.startActivity(intent, false, getBaseContext())) {
					intent.setAction(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=simple.home.jtbuaa"));
					intent.setComponent(getComponentName());
					util.startActivity(intent, true, getBaseContext());
				}
			}
        });

        TextView tvInfo = (TextView) findViewById(R.id.info);
        tvInfo.setText(aboutMsg());
        
        Button btnShareHome = (Button) findViewById(R.id.title);
        btnShareHome.setText(getString(R.string.app_name) + " " + getIntent().getStringExtra("version"));
        btnShareHome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
    	        String text = getString(R.string.app_name) + ", " 
    	        		+ getString(R.string.sharetext)
       	        		+ " https://https://play.google.com/store/apps/details?id=simple.home.jtbuaa";
        	        
    	        Intent intent = new Intent(Intent.ACTION_SEND);
    	        intent.setType("text/plain");  
    	        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share);
        		intent.putExtra(Intent.EXTRA_TEXT, text);
       			util.startActivity(Intent.createChooser(intent, getString(R.string.sharemode)), true, getBaseContext());
			}
        });

    	final String downloadPath = util.preparePath(getBaseContext());

        /*Button btnShareDesktop = (Button) findViewById(R.id.share_desktop);
        btnShareDesktop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intentShareDesktop = new Intent("simpleHome.action.SHARE_DESKTOP");
                sendBroadcast(intentShareDesktop);//need get screen of home, so send intent to home
			}
        });*/

        Button btnShareWallpaper = (Button) findViewById(R.id.share_wallpaper);
        btnShareWallpaper.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String file = getIntent().getStringExtra("filename");
				if (file.equals("")) {
					file = downloadPath + "snap/snap.png";
					try {
						FileOutputStream fos = new FileOutputStream(file); 
						BitmapDrawable bd = (BitmapDrawable) WallpaperManager.getInstance(getBaseContext()).getDrawable();
				        bd.getBitmap().compress(Bitmap.CompressFormat.PNG, 90, fos);
				        fos.close();
					} catch (Exception e) {
						Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
						return;
					} 
				}
		        Intent intent = new Intent(Intent.ACTION_SEND);
		        intent.setType("image/*");  
		        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share); 
				intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(file)));
		        util.startActivity(Intent.createChooser(intent, getString(R.string.sharemode)), true, getBaseContext());
			}
        });
        
		perferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = perferences.edit();
		 
        cbShake = (CheckBox) findViewById(R.id.change_wallpaper);
        cbShake.setEnabled(perferences.getBoolean("shake_enabled", false));
        cbShake.setChecked(perferences.getBoolean("shake", false));
        cbShake.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
        		editor.putBoolean("shake", cbShake.isChecked());
			}
        });
        
        cbAlpha = (CheckBox) findViewById(R.id.show_index);
        cbAlpha.setChecked(perferences.getBoolean("alpha", true));
        cbAlpha.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
        		editor.putBoolean("alpha", cbAlpha.isChecked());
			}
        });
        
        cbTitle = (CheckBox) findViewById(R.id.show_title);
        cbTitle.setChecked(perferences.getBoolean("title", true));
        cbTitle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
        		editor.putBoolean("title", cbTitle.isChecked());
			}
        });
        
        rotateMode = (RadioGroup) findViewById(R.id.rotate_mode);
		int tmpMode = perferences.getInt("rotate_mode", 1);
		if (tmpMode < 0) tmpMode = 1;
		if (tmpMode > 3) tmpMode = 3;
		((RadioButton) rotateMode.getChildAt(tmpMode)).setChecked(true);
        rotateMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				editor.putInt("rotate_mode", rotateMode.indexOfChild(findViewById(rotateMode.getCheckedRadioButtonId())));
			}
        });

        systemApp = (RadioGroup) findViewById(R.id.system_mode);
		boolean isGrid = perferences.getBoolean("system", true);
		if (isGrid) tmpMode = 1;
		else tmpMode = 2;
		((RadioButton) systemApp.getChildAt(tmpMode)).setChecked(true);
        systemApp.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				int grid = systemApp.indexOfChild(findViewById(systemApp.getCheckedRadioButtonId()));
				if (grid == 1) editor.putBoolean("system", true);
				else editor.putBoolean("system", false);
			}
        });

        userApp = (RadioGroup) findViewById(R.id.user_mode);
		isGrid = perferences.getBoolean("user", false);
		if (isGrid) tmpMode = 1;
		else tmpMode = 2;
		((RadioButton) userApp.getChildAt(tmpMode)).setChecked(true);
		userApp.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				int grid = userApp.indexOfChild(findViewById(userApp.getCheckedRadioButtonId()));
				if (grid == 1) editor.putBoolean("user", true);
				else editor.putBoolean("user", false);
			}
        });

        Button btnSwitchHome = (Button) findViewById(R.id.switch_home);
        btnSwitchHome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Class.forName("oms.content.Action");//ophone
			        selectOphoneHome();
				} catch (ClassNotFoundException e) {//android phone
		        	removeDefaultHome();
				}
			}
        });
	}
	
	@Override
	protected void onPause() {
		editor.commit();
        
		super.onPause();
	}
	
	@Override
	protected void onResume() {
        cbShake.setEnabled(perferences.getBoolean("shake_enabled", false));
        cbShake.setChecked(perferences.getBoolean("shake", false));
        
		super.onResume();
	}
	
	private void removeDefaultHome() {//refer to http://www.dotblogs.com.tw/neil/archive/2011/08/12/33058.aspx
        String activityName = FakeHome.class.getName();
        ComponentName fakeHome = new ComponentName(mPackageName, activityName);
        
        mPM.setComponentEnabledSetting(fakeHome, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 1);
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        startActivity(homeIntent);
        mPM.setComponentEnabledSetting(fakeHome, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 1);
        
        Settings.System.putString(getContentResolver(), "configured_home", "");

        //we don't start myself again for we use system chooser to select home, and won't go back to myself
        //Intent intent = new Intent();
        //intent.setClassName(getApplicationContext(), SelectHome.class.getName());
        //startActivity(intent);
	}

	private void selectOphoneHome() {
    	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    	mainIntent.addCategory(Intent.CATEGORY_HOME);
    	mHomeList = mPM.queryIntentActivities(mainIntent, 0);
    	Collections.sort(mHomeList, new ResolveInfo.DisplayNameComparator(mPM));//sort by name

    	String myName = "";
    	String configuredHome = Settings.System.getString(getContentResolver(), "configured_home");
        if (configuredHome == null) {//first run after install
			ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            for (int i = 0; i < mHomeList.size(); i++) {
            	String oldName = mHomeList.get(i).activityInfo.packageName;
            	if (!oldName.equals(mPackageName))
        			am.restartPackage(oldName);//kill all old home
            	else myName = mHomeList.get(i).activityInfo.name;
            }
            
            //start myself
            Intent intent =  new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setClassName(mPackageName, mPackageName+".simpleHome");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(intent);
            
            Settings.System.putString(getContentResolver(), "configured_home", myName); 

            finish();
        }
        else {
            int N = mHomeList.size();
            CharSequence[] mValue = new CharSequence[N];
            CharSequence[] mTitle = new CharSequence[N];
            for (int i = 0; i < N; i++) {
                ResolveInfo ri = mHomeList.get(i);
                mValue[i] = Integer.toString(i);
                mTitle[i] = ri.activityInfo.loadLabel(mPM);
                if (configuredHome != null && configuredHome.equals(ri.activityInfo.name)) currentHomeIndex = i;
            }
            new AlertDialog.Builder(this).setTitle(R.string.menu_choose_home).setSingleChoiceItems(mTitle, currentHomeIndex,new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Settings.System.putString(getContentResolver(), "configured_home",
                            mHomeList.get(which).activityInfo.name);

                    String oldName = mHomeList.get(currentHomeIndex).activityInfo.packageName;
                    if (!mPackageName.equals(oldName)) {//try to close the old home
    					ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    					am.restartPackage(oldName);
                    }
                    else {//if I'm the old home, not stop me immediately, otherwise the new home may not launch 
    					Intent intentNewhome = new Intent("simpleHome.action.HOME_CHANGED");
    					intentNewhome.putExtra("old_home", oldName);
    	                sendBroadcast(intentNewhome);
                    }
                    
                    //launch the new home
                    Intent intent =  new Intent(Intent.ACTION_MAIN, null);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setClassName(mHomeList.get(which).activityInfo.packageName, 
                    		mHomeList.get(which).activityInfo.name);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    startActivity(intent);
                    
                    dialog.cancel();
                	finish();
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int arg1) {
                    dialog.cancel();
    			}
            }).show();
    	}
	}

}
