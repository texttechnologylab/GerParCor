package org.texttechnologylab.parliament;

import spark.servlet.SparkApplication;

public class GerParCor implements SparkApplication {

    @Override
    public void init() {

    }

    @Override
    public void destroy() {
        SparkApplication.super.destroy();
    }
}
