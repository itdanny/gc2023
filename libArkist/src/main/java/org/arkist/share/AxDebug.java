package org.arkist.share;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class AxDebug {
	static final private String TAG = AxDebug.class.getSimpleName();
	static private Context mContext;
	static private boolean mReportProblem=true;
	
	static public void init(Context context){
		mContext=context;
	}
	public AxDebug(Context context) {
		mContext=context;		
	}	
	public void setContext(Context context){
		mContext=context;		
	}
	static public void setDevelopmentMode(boolean isReportProblem){
		mReportProblem=isReportProblem;
	}
	static public void track(String category, String label, String desc, String detail){
		if (!mReportProblem) return;
    	if (mContext==null) return;
    	EasyTracker easyTracker=EasyTracker.getInstance(mContext);
        if (easyTracker!=null){
        	String appVersionName = "";
    		try {
    			appVersionName = "v"+mContext.getPackageManager().getPackageInfo(mContext.getPackageName(),0).versionName+"_";    			
    		} catch (Exception e){
    			//
    		}
    		String stackTrace = Log.getStackTraceString(new Exception());
//        	MyApp.logError(TAG, "TrackError:"+label+" "+desc+" "+stackTrace);
//        	String desc = new StandardExceptionParser(context, null)                                                                      
//            				.getDescription(Thread.currentThread().getName(),
//            				new Exception("tErr_"+label));
        	// Managable Error...
//        	easyTracker.send(MapBuilder        			
//		           .createException(desc,false) // False indicates a fatal exception
//		           .build()
//        			);
    		String info = "Brand["+android.os.Build.BRAND+"]"+
					  	  "Model["+android.os.Build.MODEL+"]"+
					  	  "Ver["+android.os.Build.VERSION.RELEASE+"]"+
					  	  "SDK["+Build.VERSION.SDK_INT+"]"+
					  	  "User["+AxInstallation.id(mContext)+"]";
    		try {
	    		easyTracker.send(MapBuilder
	      		      .createEvent(appVersionName+category, // Category
	      		                   label+"_"+desc, // Action
	      		                   AxDate.sdfYYYYMMDDHHMMSS.format(Calendar.getInstance().getTime())+detail+info+stackTrace, 	// Label
	      		                   null) // Value
	      		      .build()
	      		  );
    		} catch (Exception e){
    			easyTracker.send(MapBuilder
  	      		      .createEvent(appVersionName+category, // Category
  	      		                   "Tracker Internal Error", // Action
  	      		                   e.getMessage(), 	// Label
  	      		                   null) // Value
  	      		      .build()
  	      		  );
    		}
        }
    }	
	static public void showVisibleFragments(FragmentManager fm, int level) {
		 if (fm==null) return;
    	String space=" ";
    	for (int i=0;i<level;i++){
    		space+="  ";
    	}
	    List<Fragment> allFragments = fm.getFragments();
	    if (allFragments == null || allFragments.isEmpty()) {
	        //return Collections.emptyList();
	    	Log.i("w",space+"FRAGMENT: <NIL>");
	    	return;
	    }
	    List<Fragment> visibleFragments = new ArrayList<Fragment>();
	    for (Fragment fragment : allFragments) {
	    	if (fragment!=null){
	    		String indexNbr=getIndexNbr(fragment);
	    		Log.i("w",space+"FRAGMENT:"+fragment.getClass().getSimpleName()+
		    				" visible="+fragment.isVisible()+
		    				" id="+String.valueOf(fragment.getId())+
		    				" tag="+fragment.getTag()+
		    				(TextUtils.isEmpty(indexNbr)?"":" ("+indexNbr+")"));
		        if (fragment.isVisible()) {
		            visibleFragments.add(fragment);
		        }
		        if (fragment.getChildFragmentManager()!=null && fragment.getChildFragmentManager().getFragments()!=null){
			        if (fragment.getChildFragmentManager().getFragments().size()>0){
			        	showVisibleFragments(fragment.getChildFragmentManager(),level+1);
			        }
		        }
	    	}
	    }
	    //return visibleFragments;
	}	
	static private String getIndexNbr(Object obj){
		String indexNbr="";
		try {
			Class<?> noparams[] = {};
			Method method = obj.getClass().getDeclaredMethod("getIndexNbr", noparams);//(Class[]) null
			if (method!=null){
				//List<Object> values = new ArrayList<Object>();
				//values.add("GB");
				//indexNbr=String.valueOf(method.invoke(fragment, values));
    			indexNbr=String.valueOf(method.invoke(obj, new Object[]{}));
    		}
		} catch (NoSuchMethodException e){
		} catch (InvocationTargetException e){	    			
		} catch (IllegalAccessException e){
		}
		return indexNbr; 
	}
	static public void debug(Object object){showLog(object, "d", "", "", false);}
	static public void info(Object object){showLog(object, "i", "", "", false);}
	static public void error(Object object){showLog(object, "e", "", "", false);}
	static public void warn(Object object){showLog(object, "w", "", "", false);}
	static public void debug(Object object, String message){showLog(object, "d", "", message, false);}
	static public void info(Object object, String message){showLog(object, "i", "", message, false);}
	static public void error(Object object, String message){showLog(object, "e", "", message, false);}
	static public void warn(Object object, String message){showLog(object, "w", "", message, false);}
	static public void exception(Object object, String message){showLog(object, "e", "", message, true);}
	static public void fix(Object object, String message){showLog(object, "a", "", message, false);}
	
	static public void showLog(Object object, String type, String extraTag, String message, boolean isTrackLog){
		if (!mReportProblem && !type.equalsIgnoreCase("A")) return;//Show Assert Only
		String tag="";
		String methodLine="";
		StackTraceElement[] ste=null;
		//if (type.equalsIgnoreCase("E") || isTrackLog){
			ste = new Throwable().getStackTrace();
			int startIndex=0;
			for (int i=0;i<ste.length;i++){
				if (ste[i].getMethodName().equalsIgnoreCase("showLog")){
					startIndex=i;
					if (ste[i+1].getMethodName().equalsIgnoreCase("showLog")){
						startIndex=i+1;	
					}
					if (ste[i+1].getMethodName().equalsIgnoreCase("error")){
						startIndex=i+1;
					} else if (ste[i+1].getMethodName().equalsIgnoreCase("info")){
						startIndex=i+1;
					} else if (ste[i+1].getMethodName().equalsIgnoreCase("warn")){
						startIndex=i+1;
					} else if (ste[i+1].getMethodName().equalsIgnoreCase("debug")){
						startIndex=i+1;
					} else if (ste[i+1].getMethodName().equalsIgnoreCase("fix")){
						startIndex=i+1;
					}
					break;
				}
			}
			String currentMethod=ste[startIndex+1].getMethodName();
			String callerMethod=ste[startIndex+2].getMethodName();
			int currentLine=ste[startIndex+1].getLineNumber();
			
			String sender="";
			String indexNbr=getIndexNbr(object);
			indexNbr = TextUtils.isEmpty(indexNbr)?"":"["+indexNbr+"]";
			if (object.getClass().equals(String.class)){
				sender = (String) object;
				tag = sender+indexNbr;
			} else {
				sender = object.getClass().getSimpleName();
				if (TextUtils.isEmpty(extraTag)){
					tag = sender+indexNbr+":"+callerMethod;
				} else {				
					tag = sender+indexNbr+"("+extraTag+")"+":"+callerMethod;
				}
			}
			methodLine = "["+currentMethod+":"+currentLine+"]";
			
			/*
			 * Send it to tracker
			 */
			if (isTrackLog){
				String msg = message;
				if (ste!=null){
					for (int i=0;i<ste.length;i++){
						String output = ste[i].getClassName().substring(ste[i].getClassName().lastIndexOf("."))
									+" : "+ste[i].getMethodName()+":"+ste[i].getLineNumber();
						Log.e("*Error*",output);
						msg = msg + Character.toChars(10).toString() + Character.toChars(13).toString() + output;
					}
				}
				//static public void track(String category, String label, String desc, String detail){
				AxDebug.track(sender, currentMethod, currentLine+":"+ indexNbr+"["+callerMethod+"]", msg);
			}
		//}
		tag=type+">"+tag;
		if (type.equalsIgnoreCase("I")){
			Log.i(tag,methodLine+message);
		} else if (type.equalsIgnoreCase("W")){
			Log.w(tag,methodLine+message);
		} else if (type.equalsIgnoreCase("D")){
            if (android.os.Build.BRAND.contentEquals("HUAWEI")){
                Log.i(tag,methodLine+message);
            } else {
                Log.d(tag,methodLine+message);
            }
		} else if (type.equalsIgnoreCase("A")){
			String callers="...";
			if (ste!=null){
				for (int i=2;i<Math.min(5, ste.length);i++){
					callers=callers+":"+ste[i].getMethodName();
				}
			}
			Log.wtf(tag,"Please Fix ***"+methodLine+message+"*"+callers);
		} else {
			Log.e(tag,methodLine+message);
		}

	}
	static public void showAllViews(View view){
		showAllViews(view, 0);
	}
	static private void showAllViews(View view, int level){
		String prefix="";
		for (int i=0;i<level;i++){
			prefix+="  ";
		}
		
		AxDebug.info(TAG,prefix+view.getClass().getSimpleName()+" visibility="+view.getVisibility()+" "+view.getTop()+","+view.getLeft()+","+view.getHeight()+","+view.getWidth());
		if (view instanceof ViewGroup){
			ViewGroup viewGroup = (ViewGroup) view;			
			for (int i=0;i<viewGroup.getChildCount();i++){
				showAllViews(viewGroup.getChildAt(i), level+1);
			}
		} 
	}
	static public void showAllPreferenceValue(Context context, String sharePreference){
        //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
      SharedPreferences sharedPref = context.getSharedPreferences(sharePreference, Context.MODE_PRIVATE);
      showMap(sharedPref.getAll());      
    }
//	static public void showMap(ContentValues vals){
//		Set<Entry<String, Object>> s = vals.valueSet();
//		Iterator<Map.Entry<String, Object>> itr = s.iterator();
//		AxDebug.showDebug(TAG, "**************** ContentValue Length :: " +vals.size());
//	    while(itr.hasNext()){
//	        Map.Entry<String, Object> me = (Map.Entry<String, Object>)itr.next(); 
//	        String key = me.getKey().toString();
//	        Object value =  me.getValue();
//	        if (value==null){
//	        	AxDebug.showDebug(TAG, key+":null");
//	        } else {
//	        	AxDebug.showDebug(TAG, key+":["+value.toString()+"]");
//	        }
//	   }
//	}
    static public void showMap(Map<String, ?> map){
    	AxDebug.debug(TAG, "**************** Map Length :: " +map.size());
    	for(Map.Entry<String,?> entry : map.entrySet()){
    		if (entry==null){
    			AxDebug.debug(TAG,"MAP [null]");
    		} else if (entry.getValue()==null){
    			AxDebug.debug(TAG,"MAP ["+entry.getKey() + "][null]");
    		} else {
    			AxDebug.debug(TAG,"MAP ["+entry.getKey() + "][" +entry.getValue().toString()+"]");
    		}
    	}
    }
    static public void showContentValues(ContentValues record){
    	Set<Entry<String, Object>> s=record.valueSet();
    	AxDebug.debug(TAG, "**************** ContentValue Length :: " +record.size());
        for (Entry<String, Object> entry : s) {
        	if (entry==null){
        		AxDebug.debug(TAG,"KEY [null]");
    		} else if (entry.getValue()==null){
    			AxDebug.debug(TAG,"KEY ["+entry.getKey() + "][null]");
    		} else {
    			AxDebug.debug(TAG,"KEY ["+entry.getKey() + "][" +entry.getValue().toString()+"]");
    		}
        }
    }
}
