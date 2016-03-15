/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.goodocom.rk;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.DialerKeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.EditText;

import java.util.HashMap;


/**
 * Fragment for call control buttons
 */
public class DialpadFragment extends BaseFragment<DialpadPresenter, DialpadPresenter.DialpadUi>
        implements DialpadPresenter.DialpadUi, View.OnTouchListener, View.OnKeyListener,
        View.OnHoverListener, View.OnClickListener,View.OnLongClickListener {

  
    private EditText mDtmfDialerField;

    /** The length of DTMF tones in milliseconds */
    private static final int TONE_LENGTH_MS = 150;

    /** The DTMF tone volume relative to other sounds in the stream */
    private static final int TONE_RELATIVE_VOLUME = 80;

    /** Stream type used to play the DTMF tones off call, and mapped to the volume control keys */
    private static final int DIAL_TONE_STREAM_TYPE = AudioManager.STREAM_DTMF;
    private AccessibilityManager mAccessibilityManager;
    private ToneGenerator mToneGenerator;
    private Object mToneGeneratorLock = new Object();
    private boolean mDTMFToneEnabled;
    static boolean mMicPhoneEnabled = true;  //true :OPEN; false : CLOSE
    static boolean mSpeakerEnabled = true;  //true :OPEN; false : CLOSE
    private View mDelete;
    /** Hash Map to map a view id to a character*/
    private static final HashMap<Integer, Character> mDisplayMap =
        new HashMap<Integer, Character>();
    private static final HashMap<Character,Integer> mDisplayDTMFMap = new HashMap<Character, Integer>();
    
    private static final HashMap<Integer,Integer> mKeyCodeMap = new HashMap<Integer, Integer>();
    
    private BluetoothService mBluetoothService;
    /** Set up the static maps*/
    static {
        // Map the buttons to the display characters
        mDisplayMap.put(R.id.one, '1');
        mDisplayMap.put(R.id.two, '2');
        mDisplayMap.put(R.id.three, '3');
        mDisplayMap.put(R.id.four, '4');
        mDisplayMap.put(R.id.five, '5');
        mDisplayMap.put(R.id.six, '6');
        mDisplayMap.put(R.id.seven, '7');
        mDisplayMap.put(R.id.eight, '8');
        mDisplayMap.put(R.id.nine, '9');
        mDisplayMap.put(R.id.zero, '0');
        mDisplayMap.put(R.id.pound, '#');
        mDisplayMap.put(R.id.star, '*');
        
        // Map the buttons to the display characters
        mDisplayDTMFMap.put('1', ToneGenerator.TONE_DTMF_1);
        mDisplayDTMFMap.put('2',ToneGenerator.TONE_DTMF_2);
        mDisplayDTMFMap.put('3', ToneGenerator.TONE_DTMF_3);
        mDisplayDTMFMap.put('4', ToneGenerator.TONE_DTMF_4);
        mDisplayDTMFMap.put('5', ToneGenerator.TONE_DTMF_5);
        mDisplayDTMFMap.put('6', ToneGenerator.TONE_DTMF_6);
        mDisplayDTMFMap.put('7', ToneGenerator.TONE_DTMF_7);
        mDisplayDTMFMap.put('8',ToneGenerator.TONE_DTMF_8);
        mDisplayDTMFMap.put('9', ToneGenerator.TONE_DTMF_9);
        mDisplayDTMFMap.put('0', ToneGenerator.TONE_DTMF_0);
        mDisplayDTMFMap.put('#',ToneGenerator.TONE_DTMF_P);
        mDisplayDTMFMap.put('*', ToneGenerator.TONE_DTMF_S);
        
        mKeyCodeMap.put(R.id.one, KeyEvent.KEYCODE_1);
        mKeyCodeMap.put(R.id.two, KeyEvent.KEYCODE_2);
        mKeyCodeMap.put(R.id.three, KeyEvent.KEYCODE_3);
        mKeyCodeMap.put(R.id.four, KeyEvent.KEYCODE_4);
        mKeyCodeMap.put(R.id.five, KeyEvent.KEYCODE_5);
        mKeyCodeMap.put(R.id.six, KeyEvent.KEYCODE_6);
        mKeyCodeMap.put(R.id.seven, KeyEvent.KEYCODE_7);
        mKeyCodeMap.put(R.id.eight, KeyEvent.KEYCODE_8);
        mKeyCodeMap.put(R.id.nine, KeyEvent.KEYCODE_9);
        mKeyCodeMap.put(R.id.zero, KeyEvent.KEYCODE_0);
        mKeyCodeMap.put(R.id.pound,KeyEvent.KEYCODE_POUND);
        mKeyCodeMap.put(R.id.star, KeyEvent.KEYCODE_STAR);
    }

    // KeyListener used with the "dialpad digits" EditText widget.
    private DTMFKeyListener mDialerKeyListener;
    private View mDialButton;
    /**
     * Our own key listener, specialized for dealing with DTMF codes.
     *   1. Ignore the backspace since it is irrelevant.
     *   2. Allow ONLY valid DTMF characters to generate a tone and be
     *      sent as a DTMF code.
     *   3. All other remaining characters are handled by the superclass.
     *
     * This code is purely here to handle events from the hardware keyboard
     * while the DTMF dialpad is up.
     */
    private class DTMFKeyListener extends DialerKeyListener {

        private DTMFKeyListener() {
            super();
        }

        /**
         * Overriden to return correct DTMF-dialable characters.
         */
        @Override
        protected char[] getAcceptedChars(){
            return DTMF_CHARACTERS;
        }

        /** special key listener ignores backspace. */
        @Override
        public boolean backspace(View view, Editable content, int keyCode,
                KeyEvent event) {
            return false;
        }

        /**
         * Return true if the keyCode is an accepted modifier key for the
         * dialer (ALT or SHIFT).
         */
        private boolean isAcceptableModifierKey(int keyCode) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_ALT_LEFT:
                case KeyEvent.KEYCODE_ALT_RIGHT:
                case KeyEvent.KEYCODE_SHIFT_LEFT:
                case KeyEvent.KEYCODE_SHIFT_RIGHT:
                    return true;
                default:
                    return false;
            }
        }

        /**
         * Overriden so that with each valid button press, we start sending
         * a dtmf code and play a local dtmf tone.
         */
        @Override
        public boolean onKeyDown(View view, Editable content,
                                 int keyCode, KeyEvent event) {
            Log.w(this,"DTMFKeyListener.onKeyDown, keyCode " + keyCode + ", view " + view);

            // find the character
            char c = (char) lookup(event, content);

            // if not a long press, and parent onKeyDown accepts the input
            if (event.getRepeatCount() == 0 && super.onKeyDown(view, content, keyCode, event)) {

                boolean keyOK = ok(getAcceptedChars(), c);

                // if the character is a valid dtmf code, start playing the tone and send the
                // code.
                if (keyOK) {
                    Log.d(this, "DTMFKeyListener reading '" + c + "' from input.");
                   processDtmf(c);
                } else {
                    Log.d(this, "DTMFKeyListener rejecting '" + c + "' from input.");
                }
                return true;
            }
            return false;
        }

        /**
         * Overriden so that with each valid button up, we stop sending
         * a dtmf code and the dtmf tone.
         */
        @Override
        public boolean onKeyUp(View view, Editable content,
                                 int keyCode, KeyEvent event) {
          Log.d(this,"DTMFKeyListener.onKeyUp, keyCode " + keyCode + ", view " + view);

            super.onKeyUp(view, content, keyCode, event);

            // find the character
            char c = (char) lookup(event, content);

            boolean keyOK = ok(getAcceptedChars(), c);

            if (keyOK) {
                Log.d(this, "Stopping the tone for '" + c + "'");
                getPresenter().stopTone();
                return true;
            }

            return false;
        }

        /**
         * Handle individual keydown events when we DO NOT have an Editable handy.
         */
        public boolean onKeyDown(KeyEvent event) {
            char c = lookup(event);
            Log.d(this, "DTMFKeyListener.onKeyDown: event '" + c + "'");

            // if not a long press, and parent onKeyDown accepts the input
            if (event.getRepeatCount() == 0 && c != 0) {
                // if the character is a valid dtmf code, start playing the tone and send the
                // code.
                if (ok(getAcceptedChars(), c)) {
                    Log.d(this, "DTMFKeyListener reading '" + c + "' from input.");
                    processDtmf(c);
                    return true;
                } else {
                    Log.d(this, "DTMFKeyListener rejecting '" + c + "' from input.");
                }
            }
            return false;
        }

        /**
         * Handle individual keyup events.
         *
         * @param event is the event we are trying to stop.  If this is null,
         * then we just force-stop the last tone without checking if the event
         * is an acceptable dialer event.
         */
        public boolean onKeyUp(KeyEvent event) {
            if (event == null) {
                //the below piece of code sends stopDTMF event unnecessarily even when a null event
                //is received, hence commenting it.
                /*if (DBG) log("Stopping the last played tone.");
                stopTone();*/
                return true;
            }

            char c = lookup(event);
            Log.d(this, "DTMFKeyListener.onKeyUp: event '" + c + "'");

            // TODO: stopTone does not take in character input, we may want to
            // consider checking for this ourselves.
            if (ok(getAcceptedChars(), c)) {
                Log.d(this, "Stopping the tone for '" + c + "'");
                getPresenter().stopTone();
                return true;
            }

            return false;
        }

        /**
         * Find the Dialer Key mapped to this event.
         *
         * @return The char value of the input event, otherwise
         * 0 if no matching character was found.
         */
        private char lookup(KeyEvent event) {
            // This code is similar to {@link DialerKeyListener#lookup(KeyEvent, Spannable) lookup}
            int meta = event.getMetaState();
            int number = event.getNumber();

            if (!((meta & (KeyEvent.META_ALT_ON | KeyEvent.META_SHIFT_ON)) == 0) || (number == 0)) {
                int match = event.getMatch(getAcceptedChars(), meta);
                number = (match != 0) ? match : number;
            }

            return (char) number;
        }

        /**
         * Check to see if the keyEvent is dialable.
         */
        boolean isKeyEventAcceptable (KeyEvent event) {
            return (ok(getAcceptedChars(), lookup(event)));
        }

        /**
         * Overrides the characters used in {@link DialerKeyListener#CHARACTERS}
         * These are the valid dtmf characters.
         */
        public final char[] DTMF_CHARACTERS = new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '#', '*'
        };
    }
    /**
     * Plays the specified tone for TONE_LENGTH_MS milliseconds.
     *
     * The tone is played locally, using the audio stream for phone calls.
     * Tones are played only if the "Audible touch tones" user preference
     * is checked, and are NOT played if the device is in silent mode.
     *
     * @param tone a tone code from {@link ToneGenerator}
     */
    void playTone(int tone) {
    	 Log.d(this, "playTone dtmf request for 1'" + tone + "'");
        // if local tone playback is disabled, just return.
        if (!mDTMFToneEnabled) {
            return;
        }
        Log.d(this, "playTone dtmf request for 2'" + tone + "'");
        // Also do nothing if the phone is in silent mode.
        // We need to re-check the ringer mode for *every* playTone()
        // call, rather than keeping a local flag that's updated in
        // onResume(), since it's possible to toggle silent mode without
        // leaving the current activity (via the ENDCALL-longpress menu.)
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getRingerMode();
        if ((ringerMode == AudioManager.RINGER_MODE_SILENT)
            || (ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
            return;
        }

        synchronized (mToneGeneratorLock) {
            if (mToneGenerator == null) {
                Log.w(this, "playTone: mToneGenerator == null, tone: " + tone);
                return;
            }

            // Start the new tone (will stop any playing tone)
            mToneGenerator.startTone(tone, TONE_LENGTH_MS);
        }
    }
    @Override
    public void onClick(View v) {
        Log.d(this, "onClick");
      
		if (v.getId() == R.id.deleteButton) {
			//keyPressed(KeyEvent.KEYCODE_DEL);
			if (mDtmfDialerField.length() != 0) 
			   mDtmfDialerField.getText().delete(mDtmfDialerField.getText().length()-1, mDtmfDialerField.getText().length());
			return;
		} else if (v.getId() == R.id.digits) {
			if (mDtmfDialerField.length() != 0) {
				mDtmfDialerField.setCursorVisible(true);
			}

		} else if (v.getId() == R.id.dialButton) {
			try {
				MainActivity.service.GOCSDK_phoneDail(mDtmfDialerField.getText().toString());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return;
        }
		 else if (v.getId() ==  R.id.mic_muteButton){
		 	Handler handler = MainActivity.getHandler();
			if(null == handler)return;
			if(mMicPhoneEnabled == true){
				handler.sendEmptyMessage(MainActivity.MSG_SET_MICPHONE_OFF);
				mMicPhoneEnabled=false;
			}else{
				handler.sendEmptyMessage(MainActivity.MSG_SET_MICPHONE_ON);
				mMicPhoneEnabled=true;
			}
			 //mBluetoothService.phoneTransfer();
        	return;}
		 else if (v.getId() == R.id.speaker_muteButton){
			 Handler handler = MainActivity.getHandler();
			 if(mSpeakerEnabled == true){
					handler.sendEmptyMessage(MainActivity.MSG_SET_SPEAERPHONE_OFF);
					mSpeakerEnabled=false;
				}else{
					handler.sendEmptyMessage(MainActivity.MSG_SET_SPEAERPHONE_ON);
					mSpeakerEnabled=true;
				}
			 //mBluetoothService.phoneTransferBack();
        	return;}
		 else if (v.getId() ==  R.id.handupButton){
			 	try {
					MainActivity.service.GOCSDK_phoneHangUp();     //挂断
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//			 mBluetoothService.phoneHangUp();
        	return;
        }else {
			final AccessibilityManager accessibilityManager = (AccessibilityManager) getActivity()
					.getSystemService(Context.ACCESSIBILITY_SERVICE);
			// When accessibility is on, simulate press and release to preserve
			// the
			// semantic meaning of performClick(). Required for Braille support.
			if (accessibilityManager.isEnabled()) {
				final int id = v.getId();
				// Checking the press state prevents double activation.
				if (!v.isPressed() && mDisplayMap.containsKey(id)) {
					keyPressed(mKeyCodeMap.get(id));
					// processDtmf(mDisplayMap.get(id));
				}
			}
		}
	}
   
    @Override
    public boolean onHover(View v, MotionEvent event) {
        // When touch exploration is turned on, lifting a finger while inside
        // the button's hover target bounds should perform a click action.
        final AccessibilityManager accessibilityManager = (AccessibilityManager)
            getActivity().getSystemService(Context.ACCESSIBILITY_SERVICE);

        if (accessibilityManager.isEnabled()
                && accessibilityManager.isTouchExplorationEnabled()) {
            final int left = v.getPaddingLeft();
            final int right = (v.getWidth() - v.getPaddingRight());
            final int top = v.getPaddingTop();
            final int bottom = (v.getHeight() - v.getPaddingBottom());

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    // Lift-to-type temporarily disables double-tap activation.
                    v.setClickable(false);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    final int x = (int) event.getX();
                    final int y = (int) event.getY();
                    if ((x > left) && (x < right) && (y > top) && (y < bottom)) {
                        v.performClick();
                    }
                    v.setClickable(true);
                    break;
            }
        }

        return false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        Log.d(this, "onKey:  keyCode " + keyCode + ", view " + v);

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            int viewId = v.getId();
            if (mDisplayMap.containsKey(viewId)) {
                switch (event.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    if (event.getRepeatCount() == 0) {
                       processDtmf(mDisplayMap.get(viewId));
                    }
                    break;
                case KeyEvent.ACTION_UP:
                    getPresenter().stopTone();
                    break;
                }
                // do not return true [handled] here, since we want the
                // press / click animation to be handled by the framework.
            }
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(this, "onTouch");
        int viewId = v.getId();

        // if the button is recognized
        if (mDisplayMap.containsKey(viewId)) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Append the character mapped to this button, to the display.
                    // start the tone
                    processDtmf(mDisplayMap.get(viewId));
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // stop the tone on ANY other event, except for MOVE.
                    getPresenter().stopTone();
                    break;
            }
            // do not return true [handled] here, since we want the
            // press / click animation to be handled by the framework.
        }
        return false;
    }

    // TODO(klp) Adds hardware keyboard listener

    @Override
    DialpadPresenter createPresenter() {
        return new DialpadPresenter();
    }

    @Override
    DialpadPresenter.DialpadUi getUi() {
        return this;
    }
    public void setBluetoothService(BluetoothService bluetoothService){
    	this.mBluetoothService = bluetoothService;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // if the mToneGenerator creation fails, just continue without it.  It is
        // a local audio signal, and is not as important as the dtmf tone itself.
        synchronized (mToneGeneratorLock) {
            if (mToneGenerator == null) {
                try {
                    mToneGenerator = new ToneGenerator(DIAL_TONE_STREAM_TYPE, TONE_RELATIVE_VOLUME);
                } catch (RuntimeException e) {
                    Log.w(this, "Exception caught while creating local tone generator: " + e);
                    mToneGenerator = null;
                }
            }
        }
        mAccessibilityManager = (AccessibilityManager) getActivity().getSystemService(getActivity().ACCESSIBILITY_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View parent = inflater.inflate(
               R.layout.dialpad, container, false);
        mDtmfDialerField = (EditText) parent.findViewById(R.id.digits);
        if (mDtmfDialerField != null) {
            mDialerKeyListener = new DTMFKeyListener();
            mDtmfDialerField.setKeyListener(mDialerKeyListener);
            // remove the long-press context menus that support
            // the edit (copy / paste / select) functions.
            mDtmfDialerField.setLongClickable(false);
            
            mDtmfDialerField.setOnClickListener(this);
            setupKeypad(parent);
        }
        if (mAccessibilityManager.isEnabled()) {
            // The text view must be selected to send accessibility events.
        	mDtmfDialerField.setSelected(true);
        }
        mDelete = parent.findViewById(R.id.deleteButton);
        mDelete.setOnClickListener(this);
        mDelete.setOnLongClickListener(this);
        
        mDialButton = parent.findViewById(R.id.dialButton);

        parent.findViewById(R.id.mic_muteButton).setOnClickListener(this);
        parent.findViewById(R.id.speaker_muteButton).setOnClickListener(this);
        parent.findViewById(R.id.handupButton).setOnClickListener(this);
        // Check whether we should show the onscreen "Dial" button and co.
        Resources res = getResources();
        if (true) {
            mDialButton.setOnClickListener(this);
        } else {
            mDialButton.setVisibility(View.GONE);
        }
        return parent;
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	  // retrieve the DTMF tone play back setting.
        mDTMFToneEnabled = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;


        // if the mToneGenerator creation fails, just continue without it.  It is
        // a local audio signal, and is not as important as the dtmf tone itself.
        synchronized (mToneGeneratorLock) {
            if (mToneGenerator == null) {
                try {
                    mToneGenerator = new ToneGenerator(AudioManager.STREAM_DTMF,
                            TONE_RELATIVE_VOLUME);
                } catch (RuntimeException e) {
                    Log.w(this, "Exception caught while creating local tone generator: " + e);
                    mToneGenerator = null;
                }
            }
        }

        // Disable the status bar and set the poke lock timeout to medium.
        // There is no need to do anything with the wake lock.
      //  mStatusBarManager.disable(StatusBarManager.DISABLE_EXPAND);

     //   updateDialAndDeleteButtonStateEnabledAttr();
    }
    /**
     * called for long touch events
     */
    @Override
    public boolean onLongClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.deleteButton: {
            	mDtmfDialerField.getText().clear();
                // TODO: The framework forgets to clear the pressed
                // status of disabled button. Until this is fixed,
                // clear manually the pressed status. b/2133127
                mDelete.setPressed(false);
                return true;
            }
            case R.id.zero: {
                keyPressed(KeyEvent.KEYCODE_PLUS);
                return true;
            }
        }
        return false;
    }
    
    private void keyPressed(int keyCode) {
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        mDtmfDialerField.onKeyDown(keyCode, event);
    }
    /**
     * Processes the specified digit as a DTMF key, by playing the appropriate
     * DTMF tone (or short tone if requested), and appending the digit to the
     * EditText field that displays the DTMF digits sent so far.
     */
    
    public final void processDtmf(char c) {
        Log.d(this, "Processing dtmf key " + c);
        // if it is a valid key, then update the display and send the dtmf tone.
        if (PhoneNumberUtils.is12Key(c)) {
            Log.d(this, "updating display and sending dtmf tone for '" + c + "'");

            // Append this key to the "digits" widget.
            appendDigitsToField(c);
            // Plays the tone through CallCommandService
            //CallCommandClient.getInstance().playDtmfTone(c, timedShortTone);
            playTone(mDisplayDTMFMap.get(c));
        } else {
            Log.d(this, "ignoring dtmf request for '" + c + "'");
        }
    }
    
    @Override
    public void onDestroyView() {
        mDialerKeyListener = null;
        super.onDestroyView();
    }
    
	@Override
	public void onDestroy() {
		super.onDestroy();
		  synchronized (mToneGeneratorLock) {
	          if (mToneGenerator != null) {
	              mToneGenerator.release();
	              mToneGenerator = null;
	          }
	      }
	}
	
    @Override
    public void setVisible(boolean on) {
        if (on) {
            getView().setVisibility(View.VISIBLE);
        } else {
            getView().setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void appendDigitsToField(char digit) {
        if (mDtmfDialerField != null) {
            // TODO: maybe *don't* manually append this digit if
            // mDialpadDigits is focused and this key came from the HW
            // keyboard, since in that case the EditText field will
            // get the key event directly and automatically appends
            // whetever the user types.
            // (Or, a cleaner fix would be to just make mDialpadDigits
            // *not* handle HW key presses.  That seems to be more
            // complicated than just setting focusable="false" on it,
            // though.)
            mDtmfDialerField.getText().append(digit);
        }
    }
    /**
     * Update the enabledness of the "Dial" and "Backspace" buttons if applicable.
     */
    private void updateDialAndDeleteButtonStateEnabledAttr() {
        final boolean notEmpty = mDtmfDialerField.length() != 0;

        mDelete.setEnabled(notEmpty);
    }

    /**
     * Called externally (from InCallScreen) to play a DTMF Tone.
     */
    /* package */ boolean onDialerKeyDown(KeyEvent event) {
        Log.d(this, "Notifying dtmf key down.");
        if (mDialerKeyListener != null) {
            return mDialerKeyListener.onKeyDown(event);
        } else {
            return false;
        }
    }

    /**
     * Called externally (from InCallScreen) to cancel the last DTMF Tone played.
     */
    public boolean onDialerKeyUp(KeyEvent event) {
        Log.d(this, "Notifying dtmf key up.");
        if (mDialerKeyListener != null) {
            return mDialerKeyListener.onKeyUp(event);
        } else {
            return false;
        }
    }

    /**
     * setup the keys on the dialer activity, using the keymaps.
     */
    private void setupKeypad(View parent) {
        // for each view id listed in the displaymap
        View button;
        for (int viewId : mDisplayMap.keySet()) {
            // locate the view
            button = parent.findViewById(viewId);
            // Setup the listeners for the buttons
            button.setOnTouchListener(this);
            button.setClickable(true);
            button.setOnKeyListener(this);
            button.setOnHoverListener(this);
            button.setOnClickListener(this);
        }
    }
    
    public boolean isEmpty(){
    	return TextUtils.isEmpty(mDtmfDialerField.getText().toString());
    }
    
    public String getPhoneNumberText(){
    	return mDtmfDialerField.getText().toString();
    }
    
    public void setText(String number){
    	mDtmfDialerField.setText(number);
    }
}
