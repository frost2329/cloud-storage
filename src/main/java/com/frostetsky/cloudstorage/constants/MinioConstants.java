package com.frostetsky.cloudstorage.constants;

import java.io.ByteArrayInputStream;

public class MinioConstants {
    public static final String BUCKET_NAME = "user-files";
    public static final String USER_BASE_PATH_PATTERN = "user-%s-files/";

    public static final int PART_SIZE = -1;
    public static final ByteArrayInputStream EMPTY_DIR_BYTEARRAY_STREAM = new ByteArrayInputStream(new byte[0]);
    public static final long EMPTY_DIR_SIZE = 0L;


}
