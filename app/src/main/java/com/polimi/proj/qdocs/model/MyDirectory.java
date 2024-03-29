package com.polimi.proj.qdocs.model;

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
 *
 * Class that represents a single directory in the user's filesystem
 */
public class MyDirectory extends StorageElement {

    private String directoryName;

    public MyDirectory(String directoryName) {
        this.directoryName = directoryName;
        this.lastAccess = 0L;
    }

    public String getDirectoryName() {
        return directoryName;
    }
}
