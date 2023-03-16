package com.siprikorea.catholicchant;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Chant ViewModel
 */
public class ChantViewModel extends AndroidViewModel {
    private final ExecutorService mWorker = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Bitmap> mChantSheet = new MutableLiveData<>();
    private final MediaPlayer mChantPlayer = new MediaPlayer();
    private final Context mContext;

    public ChantViewModel(@NonNull Application application) {
        super(application);
        mContext = getApplication().getApplicationContext();
    }

    /**
     * get chant sheet
     *
     * @return chant sheet
     */
    public LiveData<Bitmap> getChantSheet() {
        return mChantSheet;
    }

    /**
     * load
     *
     * @param chantNumber chant numbe
     */
    public void load(String chantNumber) {
        mWorker.execute(() -> {
            try {
                String zeroPaddedNumber = getZeroPaddedNumber(chantNumber);
                String fileName = "sheet/" + zeroPaddedNumber + ".jpg";

                InputStream in = mContext.getAssets().open(fileName);
                Bitmap chantSheetBitmap = BitmapFactory.decodeStream(in);
                mChantSheet.postValue(chantSheetBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * play
     *
     * @param chantNumber chant number
     */
    public void play(String chantNumber) {
        mWorker.execute(() -> {
            String zeroPaddedNumber = getZeroPaddedNumber(chantNumber);
            String fileName = "mp3/" + zeroPaddedNumber + ".mp3";

            try (AssetFileDescriptor afd = mContext.getAssets().openFd(fileName)) {
                mChantPlayer.reset();
                mChantPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mChantPlayer.prepare();
                mChantPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * stop
     */
    public void stop() {
        mWorker.execute(() -> {
            try {
                mChantPlayer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * get zero padded number
     *
     * @param number number
     * @return zero padded number
     */
    private String getZeroPaddedNumber(String number) {
        return String.format("%03d", Integer.parseInt(number));
    }
}
