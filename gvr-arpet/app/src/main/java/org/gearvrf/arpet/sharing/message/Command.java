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

package org.gearvrf.arpet.sharing.message;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@StringDef({Command.FETCH_BALL,
        Command.SHOW_PAIRED_VIEW,
        Command.SHOW_STAY_IN_POSITION_TO_PAIR})
public @interface Command {
    String FETCH_BALL = "FETCH_BALL";
    String SHOW_PAIRED_VIEW = "SHOW_PAIRED_VIEW";
    String SHOW_STAY_IN_POSITION_TO_PAIR = "SHOW_STAY_IN_POSITION_TO_PAIR";
}
