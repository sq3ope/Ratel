/*
 * Copyright 2015 PayU
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.payu.ratel.client.inmemory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.payu.ratel.client.FetchStrategy;
import com.payu.ratel.model.ServiceDescriptor;

public class RatelServerFetcher implements FetchStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(RatelServerFetcher.class);

    private final DiscoveryClient discoveryClient;
    private final AtomicInteger index = new AtomicInteger(0);

    public RatelServerFetcher(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public String fetchServiceAddress(String serviceName) {
        LOGGER.info("Fetching addresses from ratel server for {}", serviceName);
        final ArrayList<String> serviceAddressList = Lists.newArrayList(fetchServiceAddresses(serviceName));

        if (CollectionUtils.isEmpty(serviceAddressList)) {
            return "";
        }

        Collections.sort(serviceAddressList);

        int thisIndex = Math.abs(index.getAndIncrement());
        return serviceAddressList.get(thisIndex % serviceAddressList.size());
    }

    @Override
    public Collection<String> fetchServiceAddresses(final String serviceName) {
        Preconditions.checkNotNull(serviceName, "Please provide service name");

        final Collection<ServiceDescriptor> serviceInstances = Collections2.filter(getAllServiceInstances(),
                new Predicate<ServiceDescriptor>() {
                    @Override
                    public boolean apply(ServiceDescriptor serviceDescriptor) {
                        return serviceName.equals(serviceDescriptor.getName());
                    }
                });

        return Collections2.transform(serviceInstances, new Function<ServiceDescriptor, String>() {
            @Override
            public String apply(ServiceDescriptor serviceDescriptor) {
                return serviceDescriptor.getAddress();
            }
        });

    }

    private Collection<ServiceDescriptor> getAllServiceInstances() {
        return discoveryClient.fetchAllServices();
    }

    @Override
    public Collection<String> getServiceNames() {
       return Collections2.transform(getAllServiceInstances(), new Function<ServiceDescriptor, String>() {

            @Override
            public String apply(ServiceDescriptor input) {
                return input.getName();
            }
        });
    }

}
