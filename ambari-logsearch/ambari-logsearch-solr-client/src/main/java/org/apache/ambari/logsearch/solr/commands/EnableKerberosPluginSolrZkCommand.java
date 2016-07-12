/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ambari.logsearch.solr.commands;

import org.apache.ambari.logsearch.solr.AmbariSolrCloudClient;
import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.solr.common.cloud.SolrZooKeeper;
import org.apache.zookeeper.CreateMode;

import java.nio.charset.StandardCharsets;

public class EnableKerberosPluginSolrZkCommand extends AbstractZookeeperRetryCommand<String> {

  private static final String SECURITY_JSON = "/security.json";
  private static final String SECURE_CONTENT = "{\"authentication\":{\"class\": \"org.apache.solr.security.KerberosPlugin\"}}";
  private static final String UNSECURE_CONTENT = "{}";

  public EnableKerberosPluginSolrZkCommand(int maxRetries, int interval) {
    super(maxRetries, interval);
  }

  @Override
  protected String executeZkCommand(AmbariSolrCloudClient client, SolrZkClient zkClient, SolrZooKeeper solrZooKeeper) throws Exception {
    String result = "";
    String filePath = client.getZnode() + SECURITY_JSON;
    String fileContent = getFileContent(zkClient, filePath);
    if (client.isSecure()) {
      if (!fileContent.equals(SECURE_CONTENT)) {
        putFileContent(zkClient, filePath, SECURE_CONTENT);
      }
      result = SECURE_CONTENT;
    } else {
      if (!fileContent.equals(UNSECURE_CONTENT)) {
        putFileContent(zkClient, filePath, UNSECURE_CONTENT);
      }
      result = UNSECURE_CONTENT;
    }
    return result;
  }

  private void putFileContent(SolrZkClient zkClient, String fileName, String content) throws Exception {
    if (zkClient.exists(fileName, true)) {
      zkClient.setData(fileName, content.getBytes(StandardCharsets.UTF_8), true);
    } else {
      zkClient.create(fileName, content.getBytes(StandardCharsets.UTF_8), CreateMode.PERSISTENT, true);
    }
  }

  private String getFileContent(SolrZkClient zkClient, String fileName) throws Exception {
    String result;
    if (zkClient.exists(fileName, true)) {
      byte[] data = zkClient.getData(fileName, null, null, true);
      result = new String(data, StandardCharsets.UTF_8);
    } else {
      result = UNSECURE_CONTENT;
    }
    return result;
  }
}
