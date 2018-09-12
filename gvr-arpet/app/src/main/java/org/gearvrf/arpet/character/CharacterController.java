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

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRTransform;
import org.gearvrf.arpet.BallThrowHandler;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.mode.BasePetMode;
import org.gearvrf.arpet.movement.IPetAction;
import org.gearvrf.arpet.movement.OnPetActionListener;
import org.gearvrf.arpet.movement.PetActions;
import org.gearvrf.mixedreality.GVRPlane;

import java.util.HashMap;
import java.util.Map;

public class CharacterController extends BasePetMode {
    private IPetAction mCurrentAction; // default action IDLE
    private final Map<Integer, IPetAction> mPetActions;
    private GVRDrawFrameListener mDrawFrameHandler;
    private BallThrowHandler mBallThrowHandler;

    public CharacterController(PetContext petContext, GVRPlane petPlane) {
        super(petContext, new CharacterView(petContext, petPlane.getCenterPose()));

        mPetActions = new HashMap<>();
        mCurrentAction = null;
        mDrawFrameHandler = null;

        intPet((CharacterView)mModeScene, petPlane);
    }

    @Override
    protected void onEnter() {
        setCurrentAction(PetActions.TO_CAMERA.ID);
        enableActions();
    }

    @Override
    protected void onExit() {
        disableActions();
    }

    @Override
    protected void onHandleOrientation(GVRCameraRig cameraRig) {

    }

    private void intPet(CharacterView pet, GVRPlane petPlane) {
        pet.setBoundaryPlane(petPlane);

        mBallThrowHandler = BallThrowHandler.getInstance(mPetContext);
        // FIXME: Enalble and disable ball
        mBallThrowHandler.enable();

        // TODO: move this to the Character class
        GVRTransform camTrans = mPetContext.getMainScene().getMainCameraRig().getTransform();

        addAction(new PetActions.IDLE(pet, camTrans));

        addAction(new PetActions.TO_BALL(pet, mBallThrowHandler.getBall().getTransform(),
                new OnPetActionListener() {
                    @Override
                    public void onActionEnd(IPetAction action) {
                        setCurrentAction(PetActions.TO_CAMERA.ID);
                        mBallThrowHandler.disable();
                        // TODO: Pet take the ball
                    }
                }));

        addAction(new PetActions.TO_CAMERA(pet, camTrans,
                new OnPetActionListener() {
                    @Override
                    public void onActionEnd(IPetAction action) {
                        setCurrentAction(PetActions.IDLE.ID);
                        // TODO: Improve this Ball handler api
                        mBallThrowHandler.enable();
                        mBallThrowHandler.reset();
                    }
                }));
    }

    public void addAction(IPetAction action) {
        mPetActions.put(action.id(), action);
    }

    public void setCurrentAction(int action) {
        mCurrentAction = mPetActions.get(action);
    }

    private void enableActions() {
        if (mDrawFrameHandler == null) {
            mDrawFrameHandler = new DrawFrameHandler();
            mPetContext.getGVRContext().registerDrawFrameListener(mDrawFrameHandler);
        }
    }

    private void disableActions() {
        if (mDrawFrameHandler != null) {
            mPetContext.getGVRContext().unregisterDrawFrameListener(mDrawFrameHandler);
            mDrawFrameHandler = null;
        }
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
                mBallThrowHandler.reset();
            }
        }
    }
}