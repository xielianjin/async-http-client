/*
 * Copyright (c) 2010-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.asynchttpclient.async;

import static java.nio.charset.StandardCharsets.*;
import static org.asynchttpclient.async.util.TestUtils.LARGE_IMAGE_FILE;
import static org.asynchttpclient.async.util.TestUtils.createTempFile;
import static org.testng.Assert.assertEquals;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.Response;
import org.asynchttpclient.multipart.FilePart;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;

public abstract class FilePartLargeFileTest extends AbstractBasicTest {

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new AbstractHandler() {

            public void handle(String target, Request baseRequest, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

                ServletInputStream in = req.getInputStream();
                byte[] b = new byte[8192];

                int count = -1;
                int total = 0;
                while ((count = in.read(b)) != -1) {
                    b = new byte[8192];
                    total += count;
                }
                resp.setStatus(200);
                resp.addHeader("X-TRANFERED", String.valueOf(total));
                resp.getOutputStream().flush();
                resp.getOutputStream().close();

                baseRequest.setHandled(true);
            }
        };
    }

    @Test(groups = { "standalone", "default_provider" }, enabled = true)
    public void testPutImageFile() throws Exception {
        try (AsyncHttpClient client = getAsyncHttpClient(new AsyncHttpClientConfig.Builder().setRequestTimeout(100 * 6000).build())) {
            Response response = client.preparePut(getTargetUrl()).addBodyPart(new FilePart("test", LARGE_IMAGE_FILE, "application/octet-stream", UTF_8)).execute().get();
            assertEquals(response.getStatusCode(), 200);
        }
    }

    @Test(groups = { "standalone", "default_provider" }, enabled = true)
    public void testPutLargeTextFile() throws Exception {
        File file = createTempFile(1024 * 1024);

        try (AsyncHttpClient client = getAsyncHttpClient(new AsyncHttpClientConfig.Builder().setRequestTimeout(100 * 6000).build())) {
            Response response = client.preparePut(getTargetUrl()).addBodyPart(new FilePart("test", file, "application/octet-stream", UTF_8)).execute().get();
            assertEquals(response.getStatusCode(), 200);
        }
    }
}
