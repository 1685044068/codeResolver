package com.icbc.codeResolver.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo implements Serializable {
    private byte[] bytes;
    private String name;
}
