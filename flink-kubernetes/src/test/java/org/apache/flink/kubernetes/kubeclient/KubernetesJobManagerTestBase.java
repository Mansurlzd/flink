/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.kubernetes.kubeclient;

import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.configuration.BlobServerOptions;
import org.apache.flink.configuration.JobManagerOptions;
import org.apache.flink.configuration.ResourceManagerOptions;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.kubernetes.KubernetesTestBase;
import org.apache.flink.kubernetes.configuration.KubernetesConfigOptions;
import org.apache.flink.kubernetes.kubeclient.parameters.KubernetesJobManagerParameters;

import org.junit.Before;

import java.util.HashMap;
import java.util.Map;

/**
 * Base test class for the JobManager side.
 */
public class KubernetesJobManagerTestBase extends KubernetesTestBase {

	protected static final double JOB_MANAGER_CPU = 2.0;
	protected static final int JOB_MANAGER_MEMORY = 768;

	protected static final int REST_PORT = 9081;
	protected static final int RPC_PORT = 7123;
	protected static final int BLOB_SERVER_PORT = 8346;

	protected final Map<String, String> customizedEnvs = new HashMap<String, String>() {
		{
			put("key1", "value1");
			put("key2", "value2");
		}
	};

	protected final Map<String, String> userLabels = new HashMap<String, String>() {
		{
			put("label1", "value1");
			put("label2", "value2");
		}
	};

	protected final Map<String, String> nodeSelector = new HashMap<String, String>() {
		{
			put("env", "production");
			put("disk", "ssd");
		}
	};

	protected KubernetesJobManagerParameters kubernetesJobManagerParameters;

	protected FlinkPod baseFlinkPod;

	@Before
	public void setup() throws Exception {
		super.setup();

		this.flinkConfig.set(RestOptions.PORT, REST_PORT);
		this.flinkConfig.set(JobManagerOptions.PORT, RPC_PORT);
		this.flinkConfig.set(BlobServerOptions.PORT, Integer.toString(BLOB_SERVER_PORT));
		this.flinkConfig.set(KubernetesConfigOptions.JOB_MANAGER_CPU, JOB_MANAGER_CPU);
		this.customizedEnvs.forEach((k, v) ->
				this.flinkConfig.setString(ResourceManagerOptions.CONTAINERIZED_MASTER_ENV_PREFIX + k, v));
		this.flinkConfig.set(KubernetesConfigOptions.JOB_MANAGER_LABELS, userLabels);
		this.flinkConfig.set(KubernetesConfigOptions.JOB_MANAGER_NODE_SELECTOR, nodeSelector);

		final ClusterSpecification clusterSpecification = new ClusterSpecification.ClusterSpecificationBuilder()
			.setMasterMemoryMB(JOB_MANAGER_MEMORY)
			.setTaskManagerMemoryMB(1024)
			.setSlotsPerTaskManager(3)
			.createClusterSpecification();

		this.kubernetesJobManagerParameters = new KubernetesJobManagerParameters(flinkConfig, clusterSpecification);

		this.baseFlinkPod = new FlinkPod.Builder().build();
	}
}
