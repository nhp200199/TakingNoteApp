package com.example.notekeeper;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;

public class NoteUploaderService extends JobService {

    public static final String EXTRA_DATA_URI = "com.example.notekeeper.DATA_URI";
    private NoteUploader mNoteUploader;

    public NoteUploaderService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {

        //remember this method is executed in the main thread, we should switch to background thread instead!!!
        AsyncTask<JobParameters, Void, Void> task = new AsyncTask<JobParameters, Void, Void>() {
            @Override
            protected Void doInBackground(JobParameters... backgroundParams) {
                JobParameters jobParams = backgroundParams[0];
                String stringDataUri = jobParams.getExtras().getString(EXTRA_DATA_URI);
                Uri dataUri = Uri.parse(stringDataUri);
                mNoteUploader.doUpload(dataUri);

                if(!mNoteUploader.isCanceled()) // còn hơi thắc mắc cái dòng này (tác dụng của nó là gì ấy nhỉ?)
                    jobFinished(jobParams, false);
                return null;
            }
        };

        mNoteUploader = new NoteUploader(this);
        task.execute(params);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mNoteUploader.cancel();
        return true;
    }


}
