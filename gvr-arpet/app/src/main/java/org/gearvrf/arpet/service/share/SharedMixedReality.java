package org.gearvrf.arpet.service.share;

import android.graphics.Bitmap;
import android.opengl.Matrix;
import android.support.annotation.IntDef;

import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.service.IMessageService;
import org.gearvrf.arpet.service.MessageException;
import org.gearvrf.arpet.service.MessageService;
import org.gearvrf.arpet.service.SimpleMessageReceiver;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRAugmentedImage;
import org.gearvrf.mixedreality.GVRHitResult;
import org.gearvrf.mixedreality.GVRLightEstimate;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.IAnchorEventsListener;
import org.gearvrf.mixedreality.IAugmentedImageEventsListener;
import org.gearvrf.mixedreality.ICloudAnchorListener;
import org.gearvrf.mixedreality.IMRCommon;
import org.gearvrf.mixedreality.IPlaneEventsListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class SharedMixedReality implements IMRCommon {

    private static final int OFF = 0;
    public static final int HOST = 1;
    public static final int GUEST = 2;

    private final IMRCommon mMixedReality;
    private final PetContext mPetContext;
    private final List<SharedSceneObject> mSharedSceneObjects;
    private final IMessageService mMessageService;

    private int mMode = OFF;
    private float[] mSpaceMatrix = new float[16];

    @IntDef({OFF, HOST, GUEST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    public SharedMixedReality(PetContext petContext) {
        mMixedReality = new GVRMixedReality(petContext.getGVRContext(), true);
        mPetContext = petContext;
        mSharedSceneObjects = new ArrayList<>();
        mMessageService = MessageService.getInstance();
        mMessageService.addMessageReceiver(new LocalMessageReceiver());
        Matrix.setIdentityM(mSpaceMatrix, 0);
    }

    @Override
    public void resume() {
        mMixedReality.resume();
    }

    @Override
    public void pause() {
        mMixedReality.pause();
    }

    /**
     * Starts the sharing mode
     *
     * @param mode {@link SharedMixedReality#HOST} or {@link SharedMixedReality#GUEST}
     */
    public void startSharing(float[] originPose, @Mode int mode) {

        if (mMode != OFF) {
            return;
        }

        mMode = mode;

        if (mode == HOST) {
            Matrix.invertM(mSpaceMatrix, 0, originPose, 0);
            mPetContext.runOnPetThread(mSharingLoop);
        } else {
            mSpaceMatrix = originPose;
            startGuest();
        }
    }

    public void stopSharing() {
        if (mMode == GUEST) {
            stopGuest();
        }
        mMode = OFF;
    }

    private synchronized void startGuest() {
        for (SharedSceneObject shared : mSharedSceneObjects) {
            shared.parent = shared.object.getParent();
            if (shared.parent != null) {
                shared.parent.removeChildObject(shared.object);
            }
        }
    }

    private synchronized void stopGuest() {
        for (SharedSceneObject shared : mSharedSceneObjects) {
            if (shared.parent != null) {
                shared.parent.addChildObject(shared.object);
            }
        }
    }

    public synchronized void registerSharedObject(GVRSceneObject object) {
        SharedSceneObject shared = new SharedSceneObject();
        shared.object = object;
        mSharedSceneObjects.add(shared);
    }

    public synchronized void unregisterSharedObject(GVRSceneObject object) {
        for (SharedSceneObject shared : mSharedSceneObjects) {
            if (shared.object == object)
                mSharedSceneObjects.remove(shared);
        }
    }

    @Override
    public GVRSceneObject getPassThroughObject() {
        return mMixedReality.getPassThroughObject();
    }

    @Override
    public void registerPlaneListener(IPlaneEventsListener listener) {
        mMixedReality.registerPlaneListener(listener);
    }

    @Override
    public void registerAnchorListener(IAnchorEventsListener listener) {
        mMixedReality.registerAnchorListener(listener);
    }

    @Override
    public void registerAugmentedImageListener(IAugmentedImageEventsListener listener) {
        mMixedReality.registerAugmentedImageListener(listener);
    }

    @Override
    public ArrayList<GVRPlane> getAllPlanes() {
        return mMixedReality.getAllPlanes();
    }

    @Override
    public GVRAnchor createAnchor(float[] pose) {
        return mMixedReality.createAnchor(pose);
    }

    @Override
    public void updateAnchorPose(GVRAnchor gvrAnchor, float[] pose) {
        mMixedReality.updateAnchorPose(gvrAnchor, pose);
    }

    @Override
    public void removeAnchor(GVRAnchor gvrAnchor) {
        mMixedReality.removeAnchor(gvrAnchor);
    }

    @Override
    public void hostAnchor(GVRAnchor gvrAnchor, ICloudAnchorListener listener) {
        mMixedReality.hostAnchor(gvrAnchor, listener);
    }

    @Override
    public void resolveCloudAnchor(String anchorId, ICloudAnchorListener listener) {
        mMixedReality.resolveCloudAnchor(anchorId, listener);
    }

    @Override
    public void setEnableCloudAnchor(boolean enableCloudAnchor) {
        mMixedReality.setEnableCloudAnchor(enableCloudAnchor);
    }

    @Override
    public GVRHitResult hitTest(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
        return mMixedReality.hitTest(gvrSceneObject, gvrPickedObject);
    }

    @Override
    public GVRHitResult hitTest(GVRSceneObject gvrSceneObject, float x, float y) {
        return mMixedReality.hitTest(gvrSceneObject, x, y);
    }

    @Override
    public GVRLightEstimate getLightEstimate() {
        return mMixedReality.getLightEstimate();
    }

    @Override
    public void setAugmentedImage(Bitmap bitmap) {
        mMixedReality.setAugmentedImage(bitmap);
    }

    @Override
    public void setAugmentedImages(ArrayList<Bitmap> arrayList) {
        mMixedReality.setAugmentedImages(arrayList);
    }

    @Override
    public ArrayList<GVRAugmentedImage> getAllAugmentedImages() {
        return mMixedReality.getAllAugmentedImages();
    }

    @Override
    public float[] makeInterpolated(float[] poseA, float[] poseB, float t) {
        return mMixedReality.makeInterpolated(poseA, poseB, t);
    }

    @Override
    public float[] getCameraPoseMatrix() {
        return mMixedReality.getCameraPoseMatrix();
    }

    private synchronized void sendSharedSceneObjects() {
        for (SharedSceneObject shared : mSharedSceneObjects) {
            onSendSharedObject(shared.object.getTransform().getModelMatrix(), shared.object.getTag());
        }
    }

    private void onSendSharedObject(float[] pose, Object id) {
        // FIXME: use shared object matrix
        float[] result = new float[16];
        Matrix.multiplyMM(result, 0, mSpaceMatrix, 0, pose, 0);
    }

    private synchronized void onSharedObjectReceived(float[] pose, Object id) {
        for (SharedSceneObject shared : mSharedSceneObjects) {
            if (shared.object.getTag().equals(id)) {
                float[] result = new float[16];
                Matrix.multiplyMM(result, 0, mSpaceMatrix, 0, pose, 0);
                shared.object.getTransform().setModelMatrix(result);
            }
        }
    }

    private Runnable mSharingLoop = new Runnable() {

        final int LOOP_TIME = 1000;

        @Override
        public void run() {
            if (mMode != OFF) {
                // TOOD: Share plance, anchors and camera pose
                sendSharedSceneObjects();
                mPetContext.runDelayedOnPetThread(this, LOOP_TIME);
            }
        }
    };

    private static class SharedSceneObject {
        // Shared object
        GVRSceneObject object;
        // Parent of shared object.
        GVRSceneObject parent;
    }

    private class LocalMessageReceiver extends SimpleMessageReceiver {
        @Override
        public void onReceiveUpdateSharedObject(SharedObject sharedObject) throws MessageException {

        }
    }
}
