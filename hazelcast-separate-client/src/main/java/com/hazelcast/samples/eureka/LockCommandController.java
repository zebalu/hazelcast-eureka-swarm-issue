/*
   Copyright 2019 Balázs Zaicsek

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.hazelcast.samples.eureka;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.cp.lock.FencedLock;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LockCommandController {

    @Autowired
    HazelcastInstance hazelcastInstance;

    @RequestMapping("/lock-put")
    public CommandResponse put(@RequestParam(value = "key") String key, @RequestParam(value = "value") String value) {
        return withLock(() -> {
            IMap<String, String> map = hazelcastInstance.getMap("map");
            String oldValue = map.put(key, value);
            return new CommandResponse(oldValue);
        });
    }

    @RequestMapping("/lock-get")
    public CommandResponse get(@RequestParam(value = "key") String key) {
        return withLock(() -> {
            IMap<String, String> map = hazelcastInstance.getMap("map");
            String value = map.get(key);
            return new CommandResponse(value);
        });
    }

    @RequestMapping("/lock-remove")
    public CommandResponse remove(@RequestParam(value = "key") String key) {
        return withLock(() -> {
            IMap<String, String> map = hazelcastInstance.getMap("map");
            String value = map.remove(key);
            return new CommandResponse(value);
        });
    }

    @RequestMapping("/lock-size")
    public CommandResponse size() {
        return withLock(() -> {
            IMap<String, String> map = hazelcastInstance.getMap("map");
            int size = map.size();
            return new CommandResponse(Integer.toString(size));
        });
    }

    @RequestMapping("/lock-populate")
    public CommandResponse populate() {
        return withLock(() -> {
            IMap<String, String> map = hazelcastInstance.getMap("map");
            for (int i = 0; i < 1000; i++) {
                String s = Integer.toString(i);
                map.put(s, s);
            }
            return new CommandResponse("1000 entry inserted to the map");
        });
    }

    @RequestMapping("/lock-clear")
    public CommandResponse clear() {
        return withLock(() -> {
            IMap<String, String> map = hazelcastInstance.getMap("map");
            map.clear();
            return new CommandResponse("Map cleared");
        });
    }

    private CommandResponse withLock(Supplier<CommandResponse> suplier) {
        FencedLock lock = hazelcastInstance.getCPSubsystem().getLock("cp-lock");
        try {
            lock.lock();
            return suplier.get();
        } finally {
            lock.unlock();
        }
    }
}
