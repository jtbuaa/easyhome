package simple.home.jtbuaa;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import base.lib.*;

public abstract class AlphaList<T> {

	abstract String getAlpha(T info);
	abstract String getPackageName(T info);
	abstract String getProcessName(T info);
	abstract String getLabel(T info);
	abstract String getSourceDir(T info);
	abstract Drawable getIcon(T info);
	abstract int getFlag(T info);
	abstract boolean start(T info);
	abstract void sort();
	
	int tag = 2;
	
	//layout
	RelativeLayout view;
	boolean mIsGrid = false;
	
	//alpha list related
	GridView AlphaGrid;
	AlphaAdapter alphaAdapter;
	ArrayList<String> alphaList;
	final int MaxCount = 14;
	int mColumns = 10000;//just a big decimal to ensure not adjust gravity in grid mode
    int mSelected = -1;
	Boolean DuringSelection = false;

	//app list related
	GridView AppList;
	List<T> mApps;
	AppListAdapter appListAdapter;
	AppGridAdapter appGridAdapter;
	
	static int whiteColor = 0xFFFFFFFF, grayColor = 0xDDDDDDDD, redColor = 0xFFFF7777, brownColor = 0xFFF8BF00;

	AlertDialog m_deleteDialog;
	T appToDel = null;
	Context mContext;
	PackageManager pm;
	boolean mLargeScreen;
	HashMap<String, Object> mPackagesSize;

	AlphaList(Context context, PackageManager pmgr, HashMap<String, Object> packageSize, boolean isGrid, boolean largeScreen) {
		mContext = context;
		pm = pmgr;
		mLargeScreen = largeScreen;//it is largeScreen if dm.widthPixels > 480
		mPackagesSize = packageSize;
		mIsGrid = isGrid;
		
		mApps = new ArrayList<T>();
		if (mIsGrid) appGridAdapter = new AppGridAdapter(mContext, mApps);
		else appListAdapter = new AppListAdapter(mContext, mApps);
		
		alphaList = new ArrayList<String>();
		alphaAdapter = new AlphaAdapter(mContext, alphaList);
				
		//init UI
		if (mIsGrid) view = (RelativeLayout) ((Activity) mContext).getLayoutInflater().inflate(R.layout.grid_apps, null);
		else view = (RelativeLayout) ((Activity) mContext).getLayoutInflater().inflate(R.layout.apps, null);
    	
    	AlphaGrid = (GridView) view.findViewById(R.id.alpha_list);
    	AlphaGrid.inflate(mContext, R.layout.alpha_list, null);
    	
    	AppList = (GridView) view.findViewById(R.id.applist);
    	//if (mIsGrid) AppList.inflate(mContext, R.layout.icon_list, null);
    	//else AppList.inflate(mContext, R.layout.app_list, null);
    	AppList.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if ((mApps.size() > firstVisibleItem) && (!DuringSelection)) {//revert the focus of alpha list when scroll app list
					String alpha = getAlpha(mApps.get(firstVisibleItem));
					int pos = alphaAdapter.getPosition(alpha);
					if (pos != mSelected) {
						TextView tv = (TextView)AlphaGrid.getChildAt(mSelected);
						if (tv != null) tv.setBackgroundResource(R.drawable.circle);//it may be circle_selected when user click in alpha grid, so we need set it back
					
						tv = (TextView)AlphaGrid.getChildAt(pos);
						if (tv != null) tv.requestFocus();//this will change its background color
						
						mSelected = pos;
					}
				}
			}
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				DuringSelection = false;//the scrollState will not change when setSelection(), but will change during scroll manually. so we turn off the flag here.
				//if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) scrolling = false;
				//else scrolling = true;//failed to get app running state
			}
    	});
	}

	void setAdapter() {
    	AlphaGrid.setAdapter(alphaAdapter);
    	if (mIsGrid) AppList.setAdapter(appGridAdapter);
    	else AppList.setAdapter(appListAdapter);
	}
	
	void sortAlpha() {
    	sort();//sort by name
    	
	if (mApps.size() == 0) return;
	String tmp = getAlpha(mApps.get(0));
    	alphaList.add(tmp);
    	for (int i = 1; i < mApps.size(); i++) {
    		String tmp2 = getAlpha(mApps.get(i));
    		if (!tmp.equals(tmp2)) {
    			tmp = tmp2;
    			alphaList.add(tmp);
    		}
    	}
    	
    	setColumns();
	}
	
	void add(T ri) {
		add(ri, true, true);
	}
	
	void add(T ri, boolean sort, boolean updateAlpha) {
		if (mIsGrid) {
			appGridAdapter.add(ri);
	    	Collections.sort(appGridAdapter.localApplist, new myComparator());//sort by name
		}
		else {
			appListAdapter.add(ri);
	    	Collections.sort(appListAdapter.localApplist, new myComparator());//sort by name
		}
    	
    	if (updateAlpha) {
    		String tmp = getAlpha(ri);
    		if (!alphaList.contains(tmp)) {
    			alphaAdapter.add(tmp);
    	    	Collections.sort(alphaAdapter.localList, new StringComparator());
    	    	setColumns();
    		}
    	}
	}
	
	private void removeAlpha(String alpha) {
		boolean found = false;
		for (int i = 0; i < mApps.size(); i++) {
			if (getAlpha(mApps.get(i)).startsWith(alpha)) {
				found = true;
				break;
			}
		}
		if (!found) {
			alphaAdapter.remove(alpha);
			setColumns();
		}		
	}
	
	T remove(String packageName) {
    	T info = null;
		for (int i = 0; i < mApps.size(); i++) {  
			info = mApps.get(i);
			if (getPackageName(info).equals(packageName)) {
				mApps.remove(info);
				if (mIsGrid) appGridAdapter.notifyDataSetChanged();
				else appListAdapter.notifyDataSetChanged();
    			removeAlpha(getAlpha(info));

				return info;
			}
		}
		return null;
	}
	
	void setColumns() {//set column number of alpha grid
		if (mIsGrid) {//keep no scroll in grid mode
		}
		else {//keep not more than 3 lines in list mode, each line should have almost the same count of alpha
			mColumns = MaxCount;
			if (alphaAdapter.getCount() < MaxCount) mColumns = alphaAdapter.getCount();
			else if (alphaAdapter.getCount() < MaxCount*2) mColumns = (int)(alphaAdapter.getCount()/2.0+0.5);
			AlphaGrid.setNumColumns(mColumns);
		}
	}
	

	int getCount() {
		return mApps.size();
	}
	
	static Method forceStopPackage = null;
	static {
		try {
			forceStopPackage = ActivityManager.class.getDeclaredMethod("forceStopPackage", String.class);
		} catch(Exception e) {}
	}
	
    private class AppListAdapter extends ArrayAdapter<T> {
    	ArrayList localApplist;
        public AppListAdapter(Context context, List<T> apps) {
            super(context, 0, apps);
            localApplist = (ArrayList) apps;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final T info = (T) localApplist.get(position);

            if (convertView == null) 
                convertView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.app_list, parent, false);
            
            final TextView textView1 = (TextView) convertView.findViewById(R.id.appname);
            
           	if (getLabel(info).equals(textView1.getText()) && (DuringSelection))//don't update the view here 
           		return convertView;//seldom come here
           	
           	if (!getLabel(info).equals(textView1.getText())) {//only reset the appname, version, icon when needed
               	textView1.setText(getLabel(info));
               	
               	final boolean isUser = (getFlag(info) & ApplicationInfo.FLAG_SYSTEM) == 0;
               	
                final ImageView btnIcon = (ImageView) convertView.findViewById(R.id.appicon);
                btnIcon.setImageDrawable(getIcon(info));
                btnIcon.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {//kill process when click
						ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
						String pn = getPackageName(info);
						if (!pn.equals("simple.home.jtbuaa")) {
							am.restartPackage(pn);
							if (forceStopPackage != null)
								try {forceStopPackage.invoke(am, pn);
								} catch (Exception e) {e.printStackTrace();}
							//but we need to know when will it restart by itself?
							textView1.setTextColor(whiteColor);//set color back after kill it.
						}
    					return false;
					}
                });
                
                LinearLayout lapp = (LinearLayout) convertView.findViewById(R.id.app);
                lapp.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						long pressTime = event.getEventTime() - event.getDownTime();//use this to avoid long click
						if ((pressTime < ViewConfiguration.getLongPressTimeout()) && (event.getAction() == MotionEvent.ACTION_UP)) {//start app when click
	    					if (start(info))//start success
	    						textView1.setTextColor(redColor);//red for running apk
							return true;
						}
						else return false;
					}
                });
            	lapp.setTag(new ricase(info, tag));
                ((Activity) mContext).registerForContextMenu(lapp); 
                
                final TextView btnVersion = (TextView) convertView.findViewById(R.id.appversion);
                try {
                	String version = pm.getPackageInfo(getPackageName(info), 0).versionName;
                	if ((version == null) || (version.trim().equals(""))) version = String.valueOf(pm.getPackageInfo(getPackageName(info), 0).versionCode);
                	btnVersion.setText(version);
    			} catch (NameNotFoundException e) {
    				btnVersion.setText(e.toString());
    			}
                btnVersion.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
    					if (isUser) {//user app
    						Uri uri = Uri.fromParts("package", getPackageName(info), null);
    						Intent intent = new Intent(Intent.ACTION_DELETE, uri);
    						util.startActivity(intent, true, mContext);
    						btnVersion.requestFocus();
    					}
    					else {//system app
    						appToDel = info;
    						showDelDialog(getLabel(info) + " " + btnVersion.getText());
    					}
					}
                });
    			
                final TextView textView3 = (TextView) convertView.findViewById(R.id.appsource);
                String source = "";
                Object o = mPackagesSize.get(getPackageName(info));
                if(o != null) source = o.toString();
                if((getFlag(info) & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE) {
                	textView3.setTextColor(brownColor);//brown for debuggable apk
                	source += " (debuggable)";
                }
                else textView3.setTextColor(grayColor);//gray for normal
            	textView3.setText(source);
           	}
           	
            textView1.setTextColor(whiteColor);//default color
            if (!DuringSelection) {//running state should be updated when not busy, for it is time consuming
                final ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                List<RunningAppProcessInfo> appList = am.getRunningAppProcesses();
                for (int i = 0; i < appList.size(); i++) {//a bottle neck
                	if (DuringSelection) break;//cancel current task if enter scroll mode will raise performance significantly
            		RunningAppProcessInfo as = (RunningAppProcessInfo) appList.get(i);
                	if (getProcessName(info).equals(as.processName)) {
                    	textView1.setTextColor(redColor);//red for running apk
            			break;
            		}
                }
            }
           	
            return convertView;
        }
    }


    private class AppGridAdapter extends ArrayAdapter<T> {
    	ArrayList localApplist;
        public AppGridAdapter(Context context, List<T> apps) {
            super(context, 0, apps);
            localApplist = (ArrayList) apps;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final T info = (T) localApplist.get(position);

            if (convertView == null) 
                convertView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.icon_list, parent, false);
            
            final TextView textView1 = (TextView) convertView.findViewById(R.id.appname);
            
           	if (getLabel(info).equals(textView1.getText()) && (DuringSelection))//don't update the view here 
           		return convertView;//seldom come here
           	
           	if (!getLabel(info).equals(textView1.getText())) {//only reset the appname, icon when needed
               	textView1.setText(getLabel(info));
               	
                final ImageView btnIcon = (ImageView) convertView.findViewById(R.id.appicon);
                btnIcon.setImageDrawable(getIcon(info));
                btnIcon.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {//kill process when click
						long pressTime = event.getEventTime() - event.getDownTime();//use this to avoid long click
						if ((pressTime > 0) && (pressTime < ViewConfiguration.getLongPressTimeout()) && (event.getAction() == MotionEvent.ACTION_UP)) {//start app when click
	    					start(info);
							return true;
						}
						else return false;
					}
                });                
            	btnIcon.setTag(new ricase(info, tag));
                ((Activity) mContext).registerForContextMenu(btnIcon);                    			
           	}
           	
            return convertView;
        }
    }
    
    void showDelDialog(String title) {
    	if (m_deleteDialog == null) {
			m_deleteDialog = new AlertDialog.Builder(mContext).
			setTitle(title).
			setIcon(R.drawable.error).
			setMessage(R.string.warning).
			setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
				}
			}).
			setPositiveButton(mContext.getString(R.string.delete), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {//rm system app
					String apkToDel = getSourceDir(appToDel);
					ShellInterface.doExec(new String[] {"mv " + apkToDel + " " + apkToDel + ".bak"});
					Uri uri = Uri.fromParts("package", getPackageName(appToDel), null);
					Intent intent = new Intent(Intent.ACTION_DELETE, uri);
					util.startActivity(intent, true, mContext);//this will launch package installer. after it close, onResume() will be invoke.
				}
			}).create();
        }
    	else m_deleteDialog.setTitle(title);
    	m_deleteDialog.show();
    }

    
    private class AlphaAdapter extends ArrayAdapter<String> {
    	ArrayList<String> localList;
        public AlphaAdapter(Context context, List<String> alphas) {
            super(context, 0, alphas);
            localList = (ArrayList<String>) alphas;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                final LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(R.layout.alpha_list, parent, false);
            }
            
        	if (alphaList.size() > mColumns) {//only tune it if more than one line
                if (position < mColumns) {//tune gravity to show diversify
                	if (mLargeScreen)
                    	((TextView)convertView).setGravity(Gravity.CENTER);
                	else
                    	((TextView)convertView).setGravity(Gravity.LEFT);
                }
                else 
                	((TextView)convertView).setGravity(Gravity.RIGHT);
        	}
        	
            final TextView btn = (TextView) convertView.findViewById(R.id.alpha);
            btn.setText(localList.get(position));
            btn.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {//find app when click
					String tmp = localList.get(position);
					DuringSelection = true;
					v.requestFocusFromTouch();//this will make app list get focus, very strange
					TextView tv = (TextView)AlphaGrid.getChildAt(mSelected);//restore the background
					if (tv != null) tv.setBackgroundResource(R.drawable.circle);
					mSelected = position;
					for (int i = 0; i < mApps.size(); i++) {
						if (getAlpha(mApps.get(i)).startsWith(tmp)) {
							AppList.requestFocusFromTouch();
							AppList.setSelection(i);
							break;
						}
					}
					v.setBackgroundResource(R.drawable.circle_selected);//only set the background of selected one 
					return false;
				}
            });
            
        	return convertView;
        }
    }
    

}
