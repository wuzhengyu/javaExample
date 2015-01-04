/*
 * Copyright 2004-2006 the original author or authors.
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
package net.sf.cindy.example.http.handler;

import java.io.File;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.Map;

/**
 * Cached file handler. This example represents a simple file handler, it is not
 * production quality.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class CachedFileHandler extends SimpleFileHandler {

    private final Map cache = new Hashtable();
    private final ReferenceQueue queue = new ReferenceQueue();

    private class Reference extends SoftReference {

        private final long lastModified;

        public Reference(ByteBuffer buffer, long lastModified) {
            super(buffer, queue);
            this.lastModified = lastModified;
        }

    }

    protected ByteBuffer getContent(String uri) throws IOException {
        File file = getFile(uri);

        String canonicalPath = file.getCanonicalPath();
        expungeStaleEntries();

        long lastModified = file.lastModified();
        Reference reference = (Reference) cache.get(canonicalPath);
        if (reference != null) {
            ByteBuffer buffer = (ByteBuffer) reference.get();
            if (buffer != null && lastModified == reference.lastModified) {
                return buffer;
            }
        }

        ByteBuffer buffer = getContent(uri, file);
        if (buffer != null)
            cache.put(canonicalPath, new Reference(buffer, lastModified));
        return buffer;
    }

    private void expungeStaleEntries() {
        Object obj = null;
        while ((obj = queue.poll()) != null) {
            cache.values().remove(obj);
        }
    }
}
