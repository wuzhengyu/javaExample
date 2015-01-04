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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Replace backport concurrent to java 5.0 concurrent.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class ReplaceTask {

    private static final Log log = LogFactory.getLog(ReplaceTask.class);

    private static String getContent(File file) throws IOException {
        StringBuffer buffer = new StringBuffer();
        Reader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            char[] c = new char[1024];
            while (true) {
                int size = reader.read(c);
                if (size < 0)
                    break;
                buffer.append(c, 0, size);
            }
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                }
        }
        return buffer.toString();
    }

    private static void setContent(File file, String content)
            throws IOException {
        Writer writer = null;
        try {
            writer = new PrintWriter(file);
            writer.write(content);
        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e) {
                }
        }
    }

    private static void replace(File file) {
        try {
            String content = getContent(file);
            if (content.indexOf("import java.util.concurrent") >= 0)
                log.warn(file + " included java 5.0 concurrent");
            content = content.replaceAll("edu\\.emory\\.mathcs\\.backport\\.",
                    "");
            setContent(file, content);
        } catch (IOException e) {
            log.error(e, e);
        }
    }

    public static void main(String[] args) {
        File[] files = BuildUtils.listFiles(new File(args[0]), ".java");
        for (int i = 0; i < files.length; i++) {
            replace(files[i]);
        }
    }
}
