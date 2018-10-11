/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gearvrf.arpet.character;

import android.util.SparseArray;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.arpet.BallThrowHandler;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.constant.ArPetObjectType;
import org.gearvrf.arpet.mode.BasePetMode;
import org.gearvrf.arpet.movement.IPetAction;
import org.gearvrf.arpet.movement.PetActions;
import org.gearvrf.arpet.service.share.PlayerSceneObject;
import org.gearvrf.arpet.service.share.SharedMixedReality;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRPlane;

public class CharacterController extends BasePetMode {

    private IPetAction mCurrentAction; // default action IDLE
    private final SparseArray<IPetAction> mPetActions;
    private GVRDrawFrameListener mDrawFrameHandler;
    private BallThrowHandler mBallThrowHandler;
    private SharedMixedReality mMixedReality;

    public CharacterController(PetContext petContext) {
        super(petContext, new CharacterView(petContext));

        mPetActions = new SparseArray<>();
        mCurrentAction = null;
        mDrawFrameHandler = null;
        mMixedReality = (SharedMixedReality) mPetContext.getMixedReality();
        mBallThrowHandler = BallThrowHandler.getInstance(mPetContext);

        // Put at same thread that is loading the pet 3d model.
        mPetContext.runOnPetThread(() -> initPet((CharacterView) mModeScene));
    }

    @Override
    protected void onEnter() {
        //mPetContext.runOnPetThread(() -> setCurrentAction(PetActions.TO_PLAYER.ID));
        mPetContext.runDelayedOnPetThread(this::startIdle, 500);
    }

    @Override
    protected void onExit() {
        mPetContext.runOnPetThread(this::disableActions);
    }

    @Override
    protected void onHandleOrientation(GVRCameraRig cameraRig) {
    }

    private void initPet(CharacterView pet) {

        mMixedReality.registerSharedObject(pet, ArPetObjectType.PET);

        PlayerSceneObject player = new PlayerSceneObject(mPetContext);
        mMixedReality.registerSharedObject(player, ArPetObjectType.PLAYER);

        addAction(new PetActions.IDLE(pet, player));

        addAction(new PetActions.TO_BALL(pet, mBallThrowHandler.getBall(), action -> {
            setCurrentAction(PetActions.TO_PLAYER.ID);
            mBallThrowHandler.disable();
            // TODO: Pet take the ball
        }));

        addAction(new PetActions.TO_PLAYER(pet, player, action -> {
            setCurrentAction(PetActions.IDLE.ID);
            // TODO: Improve this Ball handler api
            mBallThrowHandler.enable();
            mBallThrowHandler.reset();
        }));
    }

    public void playBall() {
        mBallThrowHandler.enable();
        enableActions();
    }

    private void startIdle() {
        enableActions();
        setCurrentAction(PetActions.IDLE.ID);
    }

    public void stopBall() {
        mBallThrowHandler.disable();
        disableActions();
    }

    public void setPlane(GVRPlane plane) {
        CharacterView petView = (CharacterView) view();

        petView.setBoundaryPlane(plane);
        petView.setAnchor(mPetContext.getMixedReality().createAnchor(plane.getCenterPose()));
    }

    public void setAnchor(GVRAnchor anchor) {
        CharacterView petView = (CharacterView) view();

        petView.setAnchor(anchor);
    }

    public CharacterView getView() {
        return (CharacterView) view();
    }

    public void setCurrentAction(@PetActions.Action int action) {
        mCurrentAction = mPetActions.get(action);
    }

    private void addAction(IPetAction action) {
        mPetActions.put(action.id(), action);
    }

    private void enableActions() {
        if (mDrawFrameHandler == null) {
            mDrawFrameHandler = new DrawFrameHandler();
            mPetContext.getGVRContext().registerDrawFrameListener(mDrawFrameHandler);
        }
    }

    private void disableActions() {
//        if (mDrawFrameHandler != null) {
//            mPetContext.getGVRContext().unregisterDrawFrameListener(mDrawFrameHandler);
//            mDrawFrameHandler = null;
//        }
    }

    public void setInitialScale() {
        CharacterView petView = (CharacterView) view();
        petView.setInitialScale();
    }

    private class DrawFrameHandler implements GVRDrawFrameListener {
        IPetAction activeAction = null;

        @Override
        public void onDrawFrame(float frameTime) {
            if (mCurrentAction != activeAction) {
                if (activeAction != null) {
                    activeAction.exit();
                }
                activeAction = mCurrentAction;
                activeAction.entry();
            } else if (activeAction != null) {
                activeAction.run(frameTime);
            }

            // FIXME: Move this to a proper place
            if (mBallThrowHandler.canBeReseted()) {
                setCurrentAction(PetActions.TO_PLAYER.ID);
                mBallThrowHandler.reset();
            }
        }
    }
}
