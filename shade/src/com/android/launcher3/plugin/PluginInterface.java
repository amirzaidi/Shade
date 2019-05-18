package com.android.launcher3.plugin;

/**
 * Interface declaration with an AIDL descriptor name and an integer version.
 */
public final class PluginInterface {
    private final String mDescriptor;
    private final int mVersion;

    /**
     * Create a new instance using the given descriptor and version.
     * @param descriptor The interface descriptor.
     * @param version The interface version.
     */
    public PluginInterface(String descriptor, int version) {
        mDescriptor = descriptor;
        mVersion = version;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PluginInterface) {
            PluginInterface other = (PluginInterface) obj;
            return other.mDescriptor.equals(mDescriptor) && other.mVersion == mVersion;
        }
        return super.equals(obj);
    }
}
