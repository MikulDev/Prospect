package hms11n.mtp.win32;

import hms11n.mtp.content.PortableDeviceContainerObject;
import hms11n.mtp.content.PortableDeviceFolderObject;
import hms11n.mtp.content.PortableDeviceObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class PortableDeviceContainerObjectWin32 extends PortableDeviceObjectWin32 implements PortableDeviceContainerObject
{
    protected PortableDeviceContainerObjectWin32(String id, PortableDeviceContentWin32 content) {
        super(id, content);
    }

    public PortableDeviceObject[] getChildObjects() {
        return this.content.getObjects(this.id);
    }

    public PortableDeviceObject addFileObject(File file) throws IOException {
        return (PortableDeviceObject)(file.isDirectory() ? this.createFolderObject(file.getName()) : new PortableDeviceObjectWin32(this.content.createObject(file, this.id), this.content));
    }

    public PortableDeviceObject addFileObject(InputStream is, String name) {
        return new PortableDeviceObjectWin32(this.content.addFileFromInputStream(name, this.id, is), this.content);
    }

    public PortableDeviceObject addFileObject(InputStream is, String name, String mimeType) {
        return new PortableDeviceObjectWin32(this.content.addFileFromInputStream(name, this.id, is, mimeType), this.content);
    }

    public PortableDeviceFolderObject createFolderObject(String name) {
        return new PortableDeviceFolderObjectWin32(this.content.createFolder(name, this.id), this.content);
    }

    public void delete() {
        this.content.delete(this.id, 1);
    }

    public void copy(String path) {
        File pathFile = new File(path);
        if (!pathFile.exists()) {
            throw new RuntimeException(new FileNotFoundException("File \"" + path + "\"is not a valid file"));
        } else {
            this.copyFolder(path);
        }
    }

    public InputStream getInputStream() {
        throw new UnsupportedOperationException("Container Objects do not have any resources attached");
    }

    private void copyFolder(String path) {
        PortableDeviceObject[] objects = this.getChildObjects();

        for(PortableDeviceObject obj : objects) {
            if (!(obj instanceof PortableDeviceContainerObject)) {
                this.content.copyFile(obj.getId(), path + "\\" + obj.getOriginalFileName());
            } else {
                String name = obj.getOriginalFileName();
                File newDir = new File(path + "\\" + name);
                newDir.mkdir();
                obj.copy(newDir.getAbsolutePath());
            }
        }

    }
}
