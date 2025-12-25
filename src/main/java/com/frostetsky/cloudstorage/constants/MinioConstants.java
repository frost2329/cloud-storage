package com.frostetsky.cloudstorage.constants;

import java.io.ByteArrayInputStream;

/**
 * Constants used for MinIO (S3-compatible) operations: bucket name, user base key pattern
 * and helper values for creating "empty directory" marker objects.
 * <p>
 * S3/MinIO has no real directories, so an empty directory can be represented by a zero-byte
 * object with a key ending with "/".
 */


/** MinIO-related constants used in the application. */
public final class MinioConstants {

    private MinioConstants() {
    }

    /** Bucket for user files. */
    public static final String BUCKET_NAME = "user-files";

    /** User root prefix pattern: {@code user-%s-files/}. */
    public static final String USER_BASE_PATH_PATTERN = "user-%s-files/";

    /** {@code -1} means auto part size for multipart uploads. */
    public static final int PART_SIZE = -1;

    /** Empty content for creating "directory marker" objects. */
    public static final ByteArrayInputStream EMPTY_DIR_BYTEARRAY_STREAM =
            new ByteArrayInputStream(new byte[0]);

    /** Size of empty content. */
    public static final long EMPTY_DIR_SIZE = 0L;
}


