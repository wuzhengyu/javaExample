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
package net.sf.cindy.build;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class BuildUtils {

    private static Collection listFilesUseFilter(File directory,
            FileFilter filter) {
        List files = new ArrayList();
        File subFiles[] = directory.listFiles(filter);
        if (subFiles != null)
            for (int i = 0; i < subFiles.length; i++) {
                if (subFiles[i].isDirectory())
                    files.addAll(listFilesUseFilter(subFiles[i], filter));
                else
                    files.add(subFiles[i]);
            }
        return files;
    }

    public static File[] listFiles(File directory, final String suffix) {
        return (File[]) listFilesUseFilter(directory, new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.isDirectory()
                        || pathname.getName().endsWith(suffix);
            }
        }).toArray(new File[0]);
    }
}
