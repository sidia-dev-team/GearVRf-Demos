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

package org.gearvrf.arpet.mode;

import android.view.View;
import android.widget.Button;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.arpet.R;
import org.gearvrf.scene_objects.GVRViewSceneObject;

public class EditScene extends BasePetScene implements View.OnClickListener, IViewEvents {
    private GVRContext mContext;
    private GVRSceneObject mEditModeObject;
    private Button mBackButton, mSaveButton;
    private OnEditModeClickedListener mListenerEditMode;

    public EditScene(GVRContext gvrContext) {
        super(gvrContext);
        mContext = gvrContext;
        mEditModeObject = new GVRViewSceneObject(mContext, R.layout.edit_mode_layout, this);
    }

    public void setListenerEditMode(OnEditModeClickedListener listenerEditMode) {
        this.mListenerEditMode = listenerEditMode;
    }

    @Override
    public void onInitView(GVRViewSceneObject EditModeSceneObject, View view) {
        mBackButton = view.findViewById(R.id.btn_back);
        mSaveButton = view.findViewById(R.id.btn_save);
        mBackButton.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);
    }

    @Override
    public void onStartRendering(GVRViewSceneObject editSceneObject, View view) {
        editSceneObject.getTransform().setScale(3.2f, 3.2f, 1.0f);
        editSceneObject.getTransform().setPosition(0.0f, 1.4f, -5.0f);
        addChildObject(mEditModeObject);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_back) {
            mBackButton.post(new Runnable() {
                @Override
                public void run() {
                    mListenerEditMode.OnBack();
                }
            });
        } else if (view.getId() == R.id.btn_save) {
            mSaveButton.post(new Runnable() {
                @Override
                public void run() {
                    mListenerEditMode.OnSave();
                }
            });
        }
    }
}