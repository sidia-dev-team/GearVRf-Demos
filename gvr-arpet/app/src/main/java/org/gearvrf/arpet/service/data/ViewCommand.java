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

package org.gearvrf.arpet.service.data;

import android.support.annotation.StringDef;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ViewCommand implements Serializable {

    public static final String SHOW_PAIRED_VIEW = "SHOW_PAIRED_VIEW";
    public static final String SHOW_STAY_IN_POSITION_TO_PAIR = "SHOW_STAY_IN_POSITION_TO_PAIR";
    public static final String SHOW_PAIRING_VIEW = "SHOW_PAIRING_VIEW";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            SHOW_PAIRED_VIEW,
            SHOW_STAY_IN_POSITION_TO_PAIR,
            SHOW_PAIRING_VIEW})
    public @interface Type {
    }

    @Type
    private String type;

    public ViewCommand(@Type String type) {
        this.type = type;
    }

    @Type
    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ViewCommand{" +
                "type='" + type + '\'' +
                '}';
    }
}
