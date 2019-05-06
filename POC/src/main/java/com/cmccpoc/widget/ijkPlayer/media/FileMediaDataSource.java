/*
 * Copyright (C) 2015 Bilibili
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cmccpoc.widget.ijkPlayer.media;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

public class FileMediaDataSource implements IMediaDataSource {
    private RandomAccessFile mFile;
    private long mFileSize;

    public FileMediaDataSource(File file) throws IOException {
        mFile = new RandomAccessFile(file, "r");
        mFileSize = mFile.length();
    }

    @Override
    public int readAt(long position, byte[] buffer, int offset, int size)  {
        try {
            if (mFile.getFilePointer() != position)
                mFile.seek(position);
        } catch (Exception e){}

        if (size == 0)
            return 0;

        try {
            return mFile.read(buffer, 0, size);
        } catch (Exception e){
            return 0;
        }
    }

    @Override
    public long getSize() {
        return mFileSize;
    }

    @Override
    public void close() {
        mFileSize = 0;
        try {
            mFile.close();
        }catch (Exception e){

        }

        mFile = null;
    }
}
