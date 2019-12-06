package com.dawning.upgrade;

import java.io.File;

public interface UpgradeListener {

    void progress(int progress);

    void completed(File file);

    void error();
}
