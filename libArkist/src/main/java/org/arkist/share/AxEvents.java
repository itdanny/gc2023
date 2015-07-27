
package org.arkist.share;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import android.util.Log;
import android.util.Pair;

public class AxEvents {
	static private AxEvents _axEvents;
	static final private boolean DEBUG=false;
	
	private final LinkedHashMap<Integer,AxEventHandler> eventHandlers = new LinkedHashMap<Integer,AxEventHandler>(5);
    private final LinkedList<Integer> mToBeRemovedEventHandlers = new LinkedList<Integer>();
    private final LinkedHashMap<Integer, AxEventHandler> mToBeAddedEventHandlers = new LinkedHashMap<Integer, AxEventHandler>();
    private Pair<Integer, AxEventHandler> mFirstEventHandler;
    private Pair<Integer, AxEventHandler> mToBeAddedFirstEventHandler;
    private volatile int mDispatchInProgressCounter = 0;
    
    public AxEvents() {

	}
	static public class AxEventInfo {
		public Object sender;
		public int indexNbr;
		public int eventType;
		public int intTag=0;
		public Object objTag;
		public String strTag="";
		public AxEventInfo(Object sender, int indexNbr, int type){
			this.sender=sender;
			this.indexNbr=indexNbr;
			this.eventType=type;
			this.intTag=-1;
			this.strTag="";
			this.objTag=null;
		}
//		public AxEventInfo(Object sender, int indexNbr, int type, int intTag){
//			this.sender=sender;
//			this.indexNbr=indexNbr;
//			this.eventType=type;
//			this.intTag=intTag;
//			this.strTag="";
//			this.objTag=null;
//		}
		public AxEventInfo(Object sender, int indexNbr, int type, Object objTag, int intTag){
			this.sender=sender;
			this.indexNbr=indexNbr;
			this.eventType=type;
			this.intTag=intTag;
			this.strTag="";
			this.objTag=objTag;
		}
//		public AxEventInfo(Object sender, int indexNbr, int type, int intTag, Object objTag){
//			this.sender=sender;
//			this.indexNbr=indexNbr;
//			this.eventType=type;
//			this.intTag=intTag;
//			this.strTag="";
//			this.objTag=objTag;
//		}
//		public AxEventInfo(Object sender, int indexNbr, int type, int intTag, String strTag){
//			this.sender=sender;
//			this.indexNbr=indexNbr;
//			this.eventType=type;
//			this.intTag=intTag;
//			this.strTag=strTag;
//			this.objTag=null;
//		}
		public AxEventInfo(Object sender, int indexNbr, int type, String strTag){
			this.sender=sender;
			this.indexNbr=indexNbr;
			this.eventType=type;
			this.intTag=indexNbr;
			this.strTag=strTag;
			this.objTag=null;
		}
	}
    public interface AxEventHandler {
    	int axGetSupportedTypes();
        void axHandleEvent(AxEventInfo event);
        void axEventsChanged();
    }
	static public AxEvents getInstances(){
		if (_axEvents==null){			
			_axEvents = new AxEvents();
			if (DEBUG) AxDebug.warn(_axEvents, "New AxEvent instance ####### First Time OR should not be");
		}
		return _axEvents;
	}
    public void registerEventHandler(int key, AxEventHandler eventHandler) {
        synchronized (this) {
            if (mDispatchInProgressCounter > 0) {
                mToBeAddedEventHandlers.put(key, eventHandler);
            } else {
                eventHandlers.put(key, eventHandler);
            }
        }
    }
    public void registerFirstEventHandler(int key, AxEventHandler eventHandler) {
    	synchronized (this) {
            registerEventHandler(key, eventHandler);
            if (mDispatchInProgressCounter > 0) {
                mToBeAddedFirstEventHandler = new Pair<Integer, AxEventHandler>(key, eventHandler);
            } else {
                mFirstEventHandler = new Pair<Integer, AxEventHandler>(key, eventHandler);
            }
        }
    }
    public void deregisterEventHandler(Integer key) {
        synchronized (this) {
            if (mDispatchInProgressCounter > 0) {
                // To avoid ConcurrencyException, stash away the event handler for now.
                mToBeRemovedEventHandlers.add(key);
            } else {
                eventHandlers.remove(key);
                if (mFirstEventHandler != null && mFirstEventHandler.first == key) {
                    mFirstEventHandler = null;
                }
            }
        }
    }
    public void deregisterAllEventHandlers() {
        synchronized (this) {
            if (mDispatchInProgressCounter > 0) {
                // To avoid ConcurrencyException, stash away the event handler for now.
                mToBeRemovedEventHandlers.addAll(eventHandlers.keySet());
            } else {
                eventHandlers.clear();
                mFirstEventHandler = null;
            }
        }
    }
    public void sendEvent(final AxEventInfo event) {
    	 boolean handled = false;
         synchronized (this) {
        	 /*
        	  * Start Dispatch
        	  */
             mDispatchInProgressCounter ++;
             /*
              * Handle the 'first' one before handling the others
              */
             if (mFirstEventHandler != null) {                 
                 AxEventHandler handler = mFirstEventHandler.second;
                 if (handler != null && (handler.axGetSupportedTypes() & event.eventType) != 0
                         && !mToBeRemovedEventHandlers.contains(mFirstEventHandler.first)) {
                     handler.axHandleEvent(event);
                     handled = true;
                 }
             }
             /*
              * Loop others handlers
              */
             for (Iterator<Entry<Integer, AxEventHandler>> handlers = eventHandlers.entrySet().iterator(); handlers.hasNext();) {
                 Entry<Integer, AxEventHandler> entry = handlers.next();
                 int key = entry.getKey();
                 Object obj = entry.getValue();
                 if (DEBUG) AxDebug.info(this, obj.getClass().getSimpleName()+" "+key);
                 if (mFirstEventHandler != null && key == mFirstEventHandler.first) {
                 	// If this was the 'first' handler it was already handled
                     continue;
                 }
                 AxEventHandler eventHandler = entry.getValue();
                 if (eventHandler != null && (eventHandler.axGetSupportedTypes() & event.eventType) != 0) {
                     if (mToBeRemovedEventHandlers.contains(key)) {
                         continue;
                     }
                     eventHandler.axHandleEvent(event);
                     handled = true;
                 }
             }
             mDispatchInProgressCounter --;
             /*
              * End Dispatch - Clean up
              */
             if (mDispatchInProgressCounter == 0) {
                 // Deregister removed handlers
                 if (mToBeRemovedEventHandlers.size() > 0) {
                     for (Integer zombie : mToBeRemovedEventHandlers) {
                         eventHandlers.remove(zombie);
                         if (mFirstEventHandler != null && zombie.equals(mFirstEventHandler.first)) {
                             mFirstEventHandler = null;
                         }
                     }
                     mToBeRemovedEventHandlers.clear();
                 }
                 // Add new handlers
                 if (mToBeAddedFirstEventHandler != null) {
                     mFirstEventHandler = mToBeAddedFirstEventHandler;
                     mToBeAddedFirstEventHandler = null;
                 }
                 if (mToBeAddedEventHandlers.size() > 0) {
                     for (Entry<Integer, AxEventHandler> food : mToBeAddedEventHandlers.entrySet()) {
                         eventHandlers.put(food.getKey(), food.getValue());
                     }
                 }
             }
         }
         /*
          * Any eventType not handle ?
          */
         if (!handled) {
        	 if (DEBUG) AxDebug.error(this, "Not handle AxEvent type="+event.eventType);
         }
    }
}
