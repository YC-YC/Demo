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


import java.util.ArrayList;

/**
 * Presenter for the Incoming call widget.
 */
public class AnswerPresenter extends Presenter<AnswerPresenter.AnswerUi>
        {

    private static final String TAG = AnswerPresenter.class.getSimpleName();


    @Override
    public void onUiReady(AnswerUi ui) {
        super.onUiReady(ui);

       
        // TODO: change so that answer presenter never starts up if it's not incoming.

        // Listen for incoming calls.
    }

    @Override
    public void onUiUnready(AnswerUi ui) {
        super.onUiUnready(ui);

    }


    public void onIncomingCall() {
        // TODO: Ui is being destroyed when the fragment detaches.  Need clean up step to stop
        // getting updates here.
    }

    private void processIncomingCall() {

        // Listen for call updates for the current call.

        getUi().showAnswerUi(true);

        if (true/*call.can(Call.Capabilities.RESPOND_VIA_TEXT) && textMsgs != null*/) {
            getUi().showTextButton(true);
            getUi().configureMessageDialog(null);
        } else {
            getUi().showTextButton(false);
        }
    }


    public void onCallStateChanged() {
            getUi().showAnswerUi(false);
            // mCallId will hold the state of the call. We don't clear the mCall variable here as
            // it may be useful for sending text messages after phone disconnects. 
    }

    public void onAnswer() {
    	
        Log.d(this, "onAnswer " );

    }

    public void onDecline() {
        Log.d(this, "onDecline " );

    }

    public void onText() {
        if (getUi() != null) {
            getUi().showMessageDialog();
        }
    }

    public void rejectCallWithMessage(String message) {
        Log.d(this, "sendTextToDefaultActivity()...");


        onDismissDialog();
    }

    public void onDismissDialog() {
       // InCallPresenter.getInstance().onDismissDialog();
    }

    interface AnswerUi extends Ui {
        public void showAnswerUi(boolean show);
        public void showTextButton(boolean show);
        public void showMessageDialog();
        public void configureMessageDialog(ArrayList<String> textResponses);
    }
}
