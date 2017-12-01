package org.theorangealliance.datasync.tabs;

import org.theorangealliance.datasync.DataSyncController;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kyle Flynn on 11/30/2017.
 */
public class SyncController implements Runnable {

    private DataSyncController controller;
    private TOAExecuteAdapter executeAdapter;
    private ScheduledExecutorService service;

    private int totalSyncs;

    public SyncController(DataSyncController instance) {
        this.controller = instance;
        this.service = Executors.newSingleThreadScheduledExecutor();
        this.totalSyncs = 0;
    }

    public void execute(TOAExecuteAdapter toaExecuteAdapter) {
        service.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
        executeAdapter = toaExecuteAdapter;
    }

    public void kill() {
        service.shutdownNow();
    }

    @Override
    public void run() {
        if (executeAdapter != null) {
            totalSyncs++;
            executeAdapter.onExecute(totalSyncs);
        }
    }

    public interface TOAExecuteAdapter {
        void onExecute(int totalSyncs);
    }

}
