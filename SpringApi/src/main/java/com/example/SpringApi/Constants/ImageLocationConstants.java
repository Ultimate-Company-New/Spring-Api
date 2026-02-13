package com.example.SpringApi.Constants;

/**
 * Constants for image storage locations.
 * 
 * These constants define the available image storage providers
 * that can be configured in the application properties.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public final class ImageLocationConstants {
    private ImageLocationConstants() {
    }
    
    /**
     * Constant for ImgBB image storage service.
     */
    public static final String IMGBB = "imgbb";
    
    /**
     * Constant for Firebase image storage service.
     */
    public static final String FIREBASE = "firebase";

}
