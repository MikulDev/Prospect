package hms11n.mtp.win32;

import hms11n.mtp.content.PortableDeviceObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PortableDeviceContentWin32 {
    final PortableDeviceWin32 device;
    protected HashMap<String, Boolean> b = new HashMap();

    protected PortableDeviceContentWin32(PortableDeviceWin32 device) {
        this.device = device;
    }

    protected PortableDeviceObject getObjectFromID(String id, String type) {
        if (type.equals("FUNCTIONAL_CATEGORY_STORAGE")) {
            return new PortableDeviceStorageObjectWin32(id, this);
        } else {
            return (PortableDeviceObject)(type.equals("CONTENT_TYPE_FOLDER") ? new PortableDeviceContainerObjectWin32(id, this) : new PortableDeviceObjectWin32(id, this));
        }
    }

    protected String createObject(File file, String parent) throws IOException {
        String type = this.probeContentType(file);
        return this.device.addFileObjectN(file.getName(), parent, file, this.getFileType(type), this.getFileFormat(type));
    }

    protected String addFileFromInputStream(String name, String parentID, InputStream inStream) {
        return this.device.addFileFromInputStreamN(name, parentID, inStream, PropertiesWin32.WPD_CONTENT_TYPE_DOCUMENT.toString(), PropertiesWin32.WPD_OBJECT_FORMAT_UNSPECIFIED.toString());
    }

    protected String addFileFromInputStream(String name, String parentID, InputStream inStream, String mimeType) {
        String type = this.getFileType(mimeType);
        String format = this.getFileFormat(mimeType);
        return this.device.addFileFromInputStreamN(name, parentID, inStream, type, format);
    }

    protected String createFolder(String name, String parent) {
        return this.device.addFolderObjectN(name, parent);
    }

    protected void copyFile(String id, String path) {
        this.device.copyFileN(id, path);
    }

    protected boolean delete(String id, int recursive) {
        boolean did = this.device.deleteFileN(id, recursive);
        this.b.put(id, did);
        return false;
    }

    protected List<String> getObjectsIDs(String id) {
        Map<String, String> objectIDs = this.device.getObjectsN(id);
        return new ArrayList(objectIDs.keySet());
    }

    protected PortableDeviceObject[] getObjects(String containerId) {
        Map<String, String> objects = this.device.getObjectsN(containerId);
        PortableDeviceObject[] retObjs = new PortableDeviceObject[objects.size()];
        int i = 0;

        for(String id : objects.keySet()) {
            retObjs[i] = this.getObjectFromID(id, (String)objects.get(id));
            ++i;
        }

        return retObjs;
    }

    protected void rename(String id, String newName) {
        this.device.updatePropertyN(id, PropertiesWin32.WPD_OBJECT_NAME.guid, PropertiesWin32.WPD_OBJECT_NAME.pid, newName);
        this.device.updatePropertyN(id, PropertiesWin32.WPD_OBJECT_ORIGINAL_FILE_NAME.guid, PropertiesWin32.WPD_OBJECT_ORIGINAL_FILE_NAME.pid, newName);
    }

    protected byte[] getBytes(String id) {
        return this.device.getBytesN(id);
    }

    private String getFileType(String type) {
        if (type == null) {
            type = "";
        }

        if (type.matches("^image/.*")) {
            return PropertiesWin32.WPD_CONTENT_TYPE_IMAGE.toString();
        } else if (type.matches("^audio/.*")) {
            return PropertiesWin32.WPD_CONTENT_TYPE_AUDIO.toString();
        } else if (type.matches("^text/.*")) {
            return PropertiesWin32.WPD_CONTENT_TYPE_DOCUMENT.toString();
        } else {
            return type.matches("^video/.*") ? PropertiesWin32.WPD_CONTENT_TYPE_VIDEO.toString() : PropertiesWin32.WPD_CONTENT_TYPE_GENERIC_FILE.toString();
        }
    }

    private String getFileFormat(String type) {
        if (type == null) {
            type = "";
        }

        if (type.matches("^image/.*")) {
            return PropertiesWin32.WPD_OBJECT_FORMAT_UNSPECIFIED.toString();
        } else if (type.matches("^audio/.*")) {
            if (type.matches(".*/m4[ab]$")) {
                return PropertiesWin32.WPD_OBJECT_FORMAT_M4A.toString();
            } else if (type.matches(".*/mp3$")) {
                return PropertiesWin32.WPD_OBJECT_FORMAT_MP3.toString();
            } else {
                return type.matches(".*/wav$") ? PropertiesWin32.WPD_OBJECT_FORMAT_WAVE.toString() : PropertiesWin32.WPD_OBJECT_FORMAT_AUDIBLE.toString();
            }
        } else if (type.matches("^text/.*")) {
            return type.matches(".*/xml$") ? PropertiesWin32.WPD_OBJECT_FORMAT_XML.toString() : PropertiesWin32.WPD_OBJECT_FORMAT_TEXT.toString();
        } else if (type.matches("^video/.*")) {
            return type.matches(".*/MP4$") ? PropertiesWin32.WPD_OBJECT_FORMAT_MP4.toString() : PropertiesWin32.WPD_OBJECT_FORMAT_UNSPECIFIED.toString();
        } else {
            return PropertiesWin32.WPD_OBJECT_FORMAT_UNSPECIFIED.toString();
        }
    }

    private String probeContentType(File file) throws IOException {
        return Files.probeContentType(file.toPath());
    }
}
