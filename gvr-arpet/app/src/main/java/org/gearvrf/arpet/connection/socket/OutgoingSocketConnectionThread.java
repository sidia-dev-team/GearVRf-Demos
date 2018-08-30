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

package org.gearvrf.arpet.connection.socket;

import android.support.annotation.NonNull;

import org.gearvrf.arpet.connection.Connection;
import org.gearvrf.arpet.connection.OnConnectionListener;
import org.gearvrf.arpet.connection.exception.ConnectionException;

import java.io.IOException;

public abstract class OutgoingSocketConnectionThread<S extends Socket> extends SocketConnectionThread {

    private S mSocket;

    protected OnConnectionListener mOnConnectionListener;

    public OutgoingSocketConnectionThread(@NonNull OnConnectionListener connectionListener) {

        mOnConnectionListener = connectionListener;

        try {
            mSocket = getSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        setName("OutgoingSocketConnectionThread");

        try {
            mSocket.connect();
            mOnConnectionListener.onConnectionEstablished(createConnection(mSocket));
        } catch (IOException e) {
            cancel();
            mOnConnectionListener.onConnectionFailure(
                    new ConnectionException("Error connecting to " + mSocket.getRemoteDevice(), e));
        }
    }

    private void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract S getSocket() throws IOException;

    protected abstract Connection createConnection(S socket);
}
