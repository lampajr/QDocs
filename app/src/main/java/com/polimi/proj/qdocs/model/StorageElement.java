package com.polimi.proj.qdocs.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;

import java.util.List;
import java.util.Objects;

/**
 * Copyright 2018-2019 Lamparelli Andrea & Chittò Pietro
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Interface that represents either a file or a directory,
 * used in order to display the hierarchy of the user's files
 */
public abstract class StorageElement implements Comparable<StorageElement>{

    Long lastAccess;

    /**
     * the compaison is made in according to the lastAccess
     * @param o object to compare with
     * @return -1   if lastAccess < o.lastAccess,
     *          0   if lastAccess = o.lastAccess,
     *          1   if lastAccess > o.lastAccess
     */
    @Exclude
    @Override
    public int compareTo(StorageElement o) {
        return Long.compare(lastAccess, o.lastAccess);
    }

    /**
     * Retrieve a MyFile object matching the filename passed as parameter
     * @param filename name of the file to retrieve
     * @return the MyFile instance if exists, null otherwise
     */
    public static MyFile retrieveFileByName(String filename, List<StorageElement> storageElements) {
        for(StorageElement el : storageElements) {
            if (el instanceof MyFile) {
                MyFile f = (MyFile) el;
                if (f.getFilename().equals(filename) ||
                        f.getFilename().equals(filename.split("\\.")[0]) ||
                        f.getFilename().split("\\.")[0].equals(filename.split("\\.")[0]))
                    return f;
            }
        }
        return null;
    }

    /**
     * Retrieve a MyFile object matching the key passed as paramater
     * @param key key of the file to retrieve
     * @return the MyFile instance if exists, null otherwise
     */
    public static MyFile retrieveFileByKey(String key, List<StorageElement> storageElements) {
        for(StorageElement el : storageElements) {
            if (el instanceof MyFile) {
                MyFile f = (MyFile) el;
                if (f.getKey() != null && f.getKey().equals(key)) return f;
            }
        }
        return null;
    }

    /**
     * Retrieve a MyDirectory object from the filesList attribute
     * @param name name to check, is unique for folders
     * @return the MyDirectory obj
     */
    public static MyDirectory retrieveDirectoryByName(String name, List<StorageElement> storageElements) {
        for(StorageElement el : storageElements) {
            if (el instanceof MyDirectory) {
                MyDirectory d = (MyDirectory) el;
                if (d.getDirectoryName() != null && d.getDirectoryName().equals(name)) return d;
            }
        }
        return null;
    }

    /**
     * tells if the current datasnapshot stores a file
     * @param dataSnapshot datasnapshot to check
     * @return true if it is a file, false otherwise
     */
    public static boolean isFile(DataSnapshot dataSnapshot) {
        return Objects.requireNonNull(dataSnapshot.getKey()).matches("\\d+");
    }
}
