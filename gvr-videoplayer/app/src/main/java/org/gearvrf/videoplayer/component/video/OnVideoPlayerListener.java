package org.gearvrf.videoplayer.component.video;

public interface OnVideoPlayerListener {

    void onProgress(long progress);

    void onPrepare(String title, long duration);

    void onStart();

    void onLoading();

    void onEnd();

    void onAllEnd();
}