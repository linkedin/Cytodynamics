/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linkedin.cytodynamics.test;

import com.linkedin.cytodynamics.nucleus.IsolationLevel;
import com.linkedin.cytodynamics.nucleus.Loader;
import com.linkedin.cytodynamics.nucleus.LoaderBuilder;
import java.net.URI;
import java.util.Collections;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.HotspotClassloadingProfiler;
import org.openjdk.jmh.profile.HotspotMemoryProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;


/**
 * JMH test harness to benchmark hot swapping of classes.
 */
public class BenchmarkHotSwap {
  private static final URI TEST_JAR_URI = TestDynamicLoad.getTestJarUri("a");

  @Benchmark
  public boolean loadClassWithNewClassloader() {
    Loader loader = LoaderBuilder
        .anIsolatingLoader()
        .withClasspath(Collections.singletonList(TEST_JAR_URI))
        .withIsolationLevel(IsolationLevel.FULL)
        .build();

    TestInterface implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    return implementation.classExists("java.lang.String") && implementation.classExists(BenchmarkHotSwap.class.getName());
  }

  public static void main(String[] args) throws Exception {
    Options opt = new OptionsBuilder()
        .include(BenchmarkHotSwap.class.getSimpleName())
        .addProfiler(HotspotClassloadingProfiler.class)
        .addProfiler(HotspotMemoryProfiler.class)
        .addProfiler(GCProfiler.class)
        .warmupForks(0)
        .warmupIterations(0)
        .warmupTime(TimeValue.milliseconds(1))
        .forks(1)
        .measurementIterations(1)
        .measurementTime(TimeValue.seconds(30))
        .mode(Mode.Throughput)
        .build();

    new Runner(opt).run();
  }
}
