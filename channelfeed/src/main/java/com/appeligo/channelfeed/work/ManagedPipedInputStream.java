/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package com.appeligo.channelfeed.work;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class ManagedPipedInputStream extends PipedInputStream {

	public ManagedPipedInputStream() {
	}

	public ManagedPipedInputStream(int bufferSize) {
		buffer = new byte[bufferSize];
	}

	public ManagedPipedInputStream(int bufferSize,
			PipedOutputStream src) throws IOException {
		super(src);
		buffer = new byte[bufferSize];
	}

	public ManagedPipedInputStream(PipedOutputStream src) throws IOException {
		super(src);
	}

	public int writeBytesBeforeBlocking() throws IOException {
		int bytes = buffer.length - available();
		return bytes;
	}
}
