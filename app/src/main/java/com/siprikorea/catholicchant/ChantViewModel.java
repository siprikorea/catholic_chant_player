package com.siprikorea.catholicchant;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;

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
public class ChantViewModel extends ViewModel {
    private final String BASE_CHANT_SHEET_URL = "https://maria.catholic.or.kr/files/mp3/sungga/img/2012/2012040";
    private final String BASE_CHANT_MP3_URL = "https://maria.catholic.or.kr/musicfiles/mp3/2004090";

    private ExecutorService mWorker = Executors.newSingleThreadExecutor();
    private MutableLiveData<Bitmap> mChantSheet = new MutableLiveData<>();
    private MediaPlayer mChantPlayer = new MediaPlayer();

    public LiveData<Bitmap> getChantSheet() {
        return mChantSheet;
    }

    /**
     * Load
     * @param chantNumber Chant number
     */
    public void load(String chantNumber) {
        mWorker.execute(() -> {
            try {
                String zeroPaddedNumber = getZeroPaddedNumber(chantNumber);
                InputStream in = new URL(BASE_CHANT_SHEET_URL + zeroPaddedNumber + ".jpg").openStream();
                Bitmap chantSheetBitmap = BitmapFactory.decodeStream(in);
                mChantSheet.postValue(chantSheetBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Play
     * @param chantNumber Chant number
     */
    public void play(String chantNumber) {
        mWorker.execute(() -> {
            try {
                String zeroPaddedNumber = getZeroPaddedNumber(chantNumber);
                mChantPlayer.reset();
                mChantPlayer.setDataSource(BASE_CHANT_MP3_URL + zeroPaddedNumber + ".mp3");
                mChantPlayer.prepare();
                mChantPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Stop
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
     * Get zero padded number
     * @param number number
     * @return zero padded number
     */
    private String getZeroPaddedNumber(String number) {
        return String.format("%03d", Integer.parseInt(number));
    }
}
