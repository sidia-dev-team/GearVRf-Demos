/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.arpet;

import android.util.Log;
import android.widget.Toast;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRScene;
import org.gearvrf.arpet.character.CharacterController;
import org.gearvrf.arpet.cloud.anchor.CloudAnchorManager;
import org.gearvrf.arpet.connection.socket.ConnectionMode;
import org.gearvrf.arpet.constant.ApiConstants;
import org.gearvrf.arpet.mode.EditMode;
import org.gearvrf.arpet.mode.HudMode;
import org.gearvrf.arpet.mode.IPetMode;
import org.gearvrf.arpet.mode.OnBackToHudModeListener;
import org.gearvrf.arpet.mode.OnModeChange;
import org.gearvrf.arpet.mode.ShareAnchorMode;
import org.gearvrf.arpet.movement.PetActions;
import org.gearvrf.arpet.movement.targetwrapper.BallWrapper;
import org.gearvrf.arpet.sharing.IPetConnectionManager;
import org.gearvrf.arpet.sharing.PetConnectionManager;
import org.gearvrf.arpet.sharing.PetConnectionMessageType;
import org.gearvrf.arpet.util.ContextUtils;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IAnchorEventsListener;
import org.gearvrf.physics.GVRWorld;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class PetMain extends GVRMain {
    private static final String TAG = "GVR_ARPET";

    private GVRScene mScene;
    private PetContext mPetContext;

    private PlaneHandler planeHandler;

    private IPetMode mCurrentMode;
    private HandlerModeChange mHandlerModeChange;
    private HandlerBackToHud mHandlerBackToHud;

    private CloudAnchorManager mCloudAnchorManager;
    private IPetConnectionManager mConnectionManager;

    private CharacterController mPet = null;

    public PetMain(PetContext petContext) {
        mPetContext = petContext;
        EventBus.getDefault().register(this);
    }

    @Override
    public void onInit(final GVRContext gvrContext) throws Throwable {
        super.onInit(gvrContext);
        mPetContext.init(gvrContext);

        mScene = gvrContext.getMainScene();

        GVRWorld world = new GVRWorld(gvrContext);
        world.setGravity(0f, -50f, 0f);
        mScene.getRoot().attachComponent(world);


        //mBallThrowHandler.enable();

        mConnectionManager = PetConnectionManager.getInstance();
        mConnectionManager.init(mPetContext);

        mHandlerModeChange = new HandlerModeChange();
        mHandlerBackToHud = new HandlerBackToHud();

        mCloudAnchorManager = new CloudAnchorManager();

        planeHandler = new PlaneHandler(mPetContext);
        mPetContext.getMixedReality().registerPlaneListener(planeHandler);

        mPet = new CharacterController(mPetContext);

    }

    @Override
    public void onAfterInit() {
        // TEST CONNECTION
        mPetContext.runDelayedOnPetThread(() -> mConnectionManager.addUiMessageHandler(
                message -> {
                    if (message.getType() == PetConnectionMessageType.CONNECTION_ESTABLISHED) {
                        mPetContext.runOnPetThread(() -> {
                            if (mConnectionManager.isConnectedAs(ConnectionMode.CLIENT)) {
                                Toast.makeText(mPetContext.getActivity(), "Connected to host", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(mPetContext.getActivity(), "Guests connected: " + mConnectionManager.getTotalConnected(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                })
                , 1000);

        //mConnectionManager.startUsersInvitation();
        //mConnectionManager.acceptInvitation();
    }

    public void resume() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void pause() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private boolean isCloudAnchorApiKeySet() {
        return ContextUtils.isMetaDataSet(getGVRContext().getContext(),
                ApiConstants.GOOGLE_CLOUD_ANCHOR_KEY_NAME);
    }

    @Subscribe
    public void onPlaneDetected(final GVRPlane plane) {
        mPet.setPlane(plane);
        mPet.enter();

        // Host pet anchor
        if (isCloudAnchorApiKeySet()) {
            mCloudAnchorManager.hostAnchor(mPetContext, (AnchoredObject) mPet.view());
        }

        if (mCurrentMode instanceof EditMode) {
            Log.e(TAG, "Wrong state at first detection!");
        }

        if (mCurrentMode == null) {
            mCurrentMode = new HudMode(mPetContext, mHandlerModeChange);
            mCurrentMode.enter();
        }
    }

    private void setEditModeEnabled(boolean enabled) {
        if (mPet != null) {
            /* FIXME: Should be a state of the pet
            mPet.setRotationEnabled(enabled);
            mPet.setScaleEnabled(enabled);
            mPet.setDraggingEnabled(enabled);*/
        }
    }

    @Override
    public void onStep() {
        super.onStep();
        if (mCurrentMode != null) {
            mCurrentMode.handleOrientation();
        }
    }

    @Subscribe
    public void onBallThrown(BallWrapper ballWrapper) {
        mPet.setCurrentAction(PetActions.TO_BALL.ID);
    }

    private IAnchorEventsListener mAnchorEventsListener = new IAnchorEventsListener() {
        @Override
        public void onAnchorStateChange(GVRAnchor gvrAnchor, GVRTrackingState gvrTrackingState) {

        }
    };

    public class HandlerModeChange implements OnModeChange {

        @Override
        public void onPlayBall() {
            // FIXME: Create this state to the PET
        }

        @Override
        public void onShareAnchor() {
            if (mCurrentMode instanceof ShareAnchorMode) {
                return;
            }

            if (mCurrentMode != null) {
                mCurrentMode.exit();
            }

            mCurrentMode = new ShareAnchorMode(mPetContext);
            mCurrentMode.enter();
        }

        @Override
        public void onEditMode() {
            if (mCurrentMode instanceof EditMode) {
                return;
            }

            if (mCurrentMode != null) {
                mCurrentMode.exit();
            }

            mCurrentMode = new EditMode(mPetContext, mHandlerBackToHud);
            mCurrentMode.enter();
            setEditModeEnabled(true);
        }

        @Override
        public void onScreenshot() {

        }
    }

    public class HandlerBackToHud implements OnBackToHudModeListener {

        @Override
        public void OnBackToHud() {
            mCurrentMode.exit();
            mCurrentMode = new HudMode(mPetContext, mHandlerModeChange);
            mCurrentMode.enter();
        }
    }
}

