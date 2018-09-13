package org.gearvrf.arpet.mode;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.R;
import org.gearvrf.scene_objects.GVRViewSceneObject;

public class ShareAnchorView extends BasePetView implements IViewEvents, View.OnClickListener {
    private GVRSceneObject mInvitationObject;
    private Button mGuestButton, mHostButton;
    private TextView mMessage;
    private ProgressBar mProgressBar;
    private ProgressHandler mProgressHandler;
    private ImageView mCheckImage, mPairing;
    private Handler mHandler;
    OnGuestOrHostListener mGuestOrHostListener;

    public ShareAnchorView(PetContext petContext) {
        super(petContext);
        mInvitationObject = new GVRViewSceneObject(petContext.getGVRContext(),
                R.layout.share_anchor_layout, this);
        mHandler = new Handler();
    }

    @Override
    protected void onShow(GVRScene mainScene) {
        mainScene.getMainCameraRig().addChildObject(this);
    }

    @Override
    protected void onHide(GVRScene mainScene) {
        mainScene.getMainCameraRig().removeChildObject(this);
    }

    public void setListenerShareAnchorMode(OnGuestOrHostListener listener) {
        mGuestOrHostListener = listener;
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
        mGuestButton = view.findViewById(R.id.guest_button);
        mHostButton = view.findViewById(R.id.host_button);
        mMessage = view.findViewById(R.id.message);
        mProgressBar = view.findViewById(R.id.progress);
        mCheckImage = view.findViewById(R.id.check);
        mPairing = view.findViewById(R.id.image_action);
        mGuestButton.setOnClickListener(this);
        mHostButton.setOnClickListener(this);
        mProgressHandler = new ProgressHandler(mProgressBar, 10000);
    }

    @Override
    public void onStartRendering(GVRViewSceneObject sendingInvitationObject, View view) {
        sendingInvitationObject.getTransform().setScale(7.2f, 6.2f, 1.0f);
        sendingInvitationObject.getTransform().setPosition(0.0f, 0.0f, -5.0f);
        addChildObject(mInvitationObject);
        mInvitationObject.getRenderData().getMaterial().setColor(0.6f, 0.6f, 0.6f);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.guest_button) {
            mGuestOrHostListener.OnGuest();
        } else if (view.getId() == R.id.host_button) {
            mGuestOrHostListener.OnHost();
        }
    }

    public void modeGuest() {
        mMessage.setText(R.string.waiting_host);
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void modeHost() {
        mMessage.setText(R.string.waiting_guests);
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void inviteAcceptedGuest() {
        mMessage.setText(R.string.connected_host);
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mCheckImage.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    public void inviteAcceptedHost(int total) {
        mMessage.setText(getGVRContext().getContext().getResources().getQuantityString(R.plurals.connected_guests, total, total));
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mCheckImage.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    public void invitationView() {
        mMessage.setText("Let’s start Sharing Anchor!\n" +
                "What role do you want to play?");
        mProgressBar.setVisibility(View.GONE);
        mGuestButton.setVisibility(View.VISIBLE);
        mHostButton.setVisibility(View.VISIBLE);
    }

    private void pairing() {
        mPairing.setBackgroundResource(R.drawable.ic_pairing);
        mMessage.setText(R.string.looking_the_same_thing);
    }

    class ProgressHandler extends Handler {

        private static final int TICK_DELAY = 10000; // millisecond
        private int currentProgress;
        private int duration;

        public ProgressHandler(ProgressBar progressBar, int duration) {
            mProgressBar = progressBar;
            this.duration = duration;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (currentProgress < duration) {
                mProgressBar.setProgress(currentProgress);
                currentProgress += TICK_DELAY;
                sendEmptyMessageDelayed(0, TICK_DELAY);
            }
        }

        void start() {
            currentProgress = 0;
            mProgressBar.setMax(duration);
            sendEmptyMessage(0);
        }

        void stop() {
            removeMessages(0);
        }
    }
}