package util;

public class Bit {
    private byte[] data;
    private String filename;
    private String contentType;
    private long size;
    
    public Bit() {}
    
    public Bit(byte[] data, String filename, String contentType) {
        this.data = data;
        this.filename = filename;
        this.contentType = contentType;
        this.size = data != null ? data.length : 0;
    }
    
    public byte[] getData() {
        return data;
    }
    
    public void setData(byte[] data) {
        this.data = data;
        this.size = data != null ? data.length : 0;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public long getSize() {
        return size;
    }
    
    public boolean isEmpty() {
        return data == null || data.length == 0;
    }
    
    public String getExtension() {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
