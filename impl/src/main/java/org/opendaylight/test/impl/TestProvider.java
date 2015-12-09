/*
 * Copyright © 2015 Inocybe, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.test.impl;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.test.impl.rev141210.modules.module.configuration.TestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.test.rev700101.TestService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.test.rev700101.Tests;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.test.rev700101.TestsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.test.rev700101.tests.Test;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.model.util.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@SuppressWarnings("deprecation")
public class TestProvider implements BindingAwareProvider, DataChangeListener, AutoCloseable {

    private ProviderContext providerContext;
    private DataBroker dataService;
    private ListenerRegistration<DataChangeListener> dcReg;
    private BindingAwareBroker.RpcRegistration<TestService> rpcReg;

    public static final InstanceIdentifier<Test> TEST_IID = InstanceIdentifier.builder(Tests.class).child(Test.class)
            .build();
    private static final Logger LOG = LoggerFactory.getLogger(TestProvider.class);
    private static final AtomicLong HEIGHT = new AtomicLong(30);
    private static final AtomicLong WEIDTH = new AtomicLong(20);

    @Override
    public void onSessionInitiated(ProviderContext session) {
        this.providerContext = session;
        this.dataService = session.getSALService(DataBroker.class);

        dcReg = dataService.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION, TEST_IID, this,
                DataChangeScope.SUBTREE);

        //rpcReg = session.addRpcImplementation(TestService.class, this);

        LOG.info("TestProvider Session Initiated");

        iniTelevisionConfiguration();
    }

    @Override
    public void close() throws Exception {
        dcReg.close();
        LOG.info("TestProvider Closed");
    }

   /* @Override
    public Future<RpcResult<java.lang.Void>> heighttochange(final HeighttochangInput input){
        LOG.info("changeHeight: {}", input);

        //HEIGHT.set(Input.);
    }*/

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        DataObject dataObject = change.getUpdatedSubtree();
        if (dataObject instanceof Test) {
            Test test = (Test) dataObject;
            LOG.info("onDataChaged - new Test config: {}", test);
        } else {
            LOG.warn("onDataChange - new Test config:{}", dataObject);
        }
    }

    private void iniTelevisionConfiguration() {
        Tests tests = new TestsBuilder().build();

        WriteTransaction tx = dataService.newWriteOnlyTransaction();
        // tx.put(LogicalDatastoreType.CONFIGURATION, TEST_IID, tests);
        tx.submit();

        Futures.addCallback(tx.submit(), new FutureCallback<Void>() {

            @Override
            public void onFailure(Throwable t) {
                LOG.info("initTestOperational: Transaction succeeded");

            }

            @Override
            public void onSuccess(final Void result) {
                LOG.info("initTestOperational: Transaction faild");

            }
        });
        LOG.info("initTestConfigration: default config populated: {}", tests);
    }

}
