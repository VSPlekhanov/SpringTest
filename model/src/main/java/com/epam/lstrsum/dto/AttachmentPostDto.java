package com.epam.lstrsum.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AttachmentPostDto {
    private String fileName;
    private String fileType;
    private byte[] data;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttachmentPostDto that = (AttachmentPostDto) o;

        if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) return false;
        return fileType != null ? fileType.equals(that.fileType) : that.fileType == null;
    }

    @Override
    public int hashCode() {
        int result = fileName != null ? fileName.hashCode() : 0;
        result = 31 * result + (fileType != null ? fileType.hashCode() : 0);
        return result;
    }
}
