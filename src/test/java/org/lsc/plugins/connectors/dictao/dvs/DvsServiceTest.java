/*
 ****************************************************************************
 * Ldap Synchronization Connector provides tools to synchronize
 * electronic identities from a list of data sources including
 * any database with a JDBC connector, another LDAP directory,
 * flat files...
 *
 *                  ==LICENSE NOTICE==
 * 
 * Copyright (c) 2008 - 2014 LSC Project 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of the LSC Project nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *                  ==LICENSE NOTICE==
 *
 *               (c) 2008 - 2014 LSC Project
 *         Sebastien Bahloul <seb@lsc-project.org>
 *         Thomas Chemineau <thomas@lsc-project.org>
 *         Jonathan Clarke <jon@lsc-project.org>
 *         Remy-Christophe Schermesser <rcs@lsc-project.org>
 ****************************************************************************
 */
package org.lsc.plugins.connectors.dictao.dvs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.naming.NamingException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.lsc.Configuration;
import org.lsc.LscDatasetModification;
import org.lsc.LscDatasetModification.LscDatasetModificationType;
import org.lsc.LscDatasets;
import org.lsc.LscModificationType;
import org.lsc.LscModifications;
import org.lsc.beans.IBean;
import org.lsc.configuration.LscConfiguration;
import org.lsc.exception.LscException;
import org.lsc.exception.LscServiceException;
import org.lsc.utils.DateUtils;

public class DvsServiceTest {
    
    private static DvsProvisioningService service;

//    private static final String SAMPLE_EMAIL = "bgates@dictao.com";
    private static final String SAMPLE_GIVENNAME = "Bill";
    private static final String SAMPLE_SURNAME = "Gates";
    private static final String SAMPLE_MODIFYTIMESTAMP = DateUtils.format(new Date());
    private static final String SAMPLE_PASSWORD = "secret";
    private static final String SAMPLE_PHONE = "+33600000000";
    
    @BeforeClass
    public static void setup() throws LscException {
        Configuration.setUp("src/test/resources/etc-opendj2dacs");
        service = new DvsProvisioningService(LscConfiguration.getTask("SyncToDvs"));
    }

    @Test
    public void testDvsProvisioning() throws LscServiceException, NamingException {
        String dvsUuid = "lsc-dvs-provisioning-"  + UUID.randomUUID().toString();
        LscModifications create = new LscModifications(LscModificationType.CREATE_OBJECT);
        create.setMainIdentifer(dvsUuid);
        List<LscDatasetModification> attributes = new ArrayList<LscDatasetModification>();
//        attributes.add(new LscDatasetModification(LscDatasetModificationType.ADD_VALUES, DvsProvisioningService.EMAIL_NAME, Arrays.asList(new Object[] {SAMPLE_EMAIL})));
        attributes.add(new LscDatasetModification(LscDatasetModificationType.ADD_VALUES, DvsProvisioningService.GIVENNAME_NAME, Arrays.asList(new Object[] {SAMPLE_GIVENNAME})));
        attributes.add(new LscDatasetModification(LscDatasetModificationType.ADD_VALUES, DvsProvisioningService.SURNAME_NAME, Arrays.asList(new Object[] {SAMPLE_SURNAME})));
        attributes.add(new LscDatasetModification(LscDatasetModificationType.ADD_VALUES, DvsProvisioningService.MODIFYTIMESTAMP_NAME, Arrays.asList(new Object[] {SAMPLE_MODIFYTIMESTAMP})));
        attributes.add(new LscDatasetModification(LscDatasetModificationType.ADD_VALUES, DvsProvisioningService.PASSWORD_NAME, Arrays.asList(new Object[] {SAMPLE_PASSWORD})));
        attributes.add(new LscDatasetModification(LscDatasetModificationType.ADD_VALUES, DvsProvisioningService.PHONE_NAME, Arrays.asList(new Object[] {SAMPLE_PHONE})));
        create.setLscAttributeModifications(attributes );
        assertTrue(service.apply(create));

        service.flushCache();
        
        // Check that the object has been successfully stored !
        IBean createBean = service.getBean(dvsUuid, new LscDatasets(), false);
        assertEquals(createBean.getMainIdentifier(), dvsUuid);
//        assertEquals(createBean.getDatasetFirstValueById(DvsProvisioningService.EMAIL_NAME), SAMPLE_EMAIL);
        assertEquals(createBean.getDatasetFirstValueById(DvsProvisioningService.GIVENNAME_NAME), SAMPLE_GIVENNAME);
        assertEquals(createBean.getDatasetFirstValueById(DvsProvisioningService.SURNAME_NAME), SAMPLE_SURNAME);
        assertEquals(createBean.getDatasetFirstValueById(DvsProvisioningService.PASSWORD_NAME), DvsProvisioningService.PASSWORD_ENCRYPTED_VALUE);
        assertEquals(createBean.getDatasetFirstValueById(DvsProvisioningService.PHONE_NAME), SAMPLE_PHONE);

        service.flushCache();
        
//        String updateUuid = "lsc-dacs-provisioning-update-"  + UUID.randomUUID().toString();
//        LscModifications update = new LscModifications(LscModificationType.UPDATE_OBJECT);
//        update.setMainIdentifer(updateUuid);
//        attributes = new ArrayList<LscDatasetModification>();
//        attributes.add(new LscDatasetModification(LscDatasetModificationType.REPLACE_VALUES, DvsProvisioningService.LOGIN_NAME, Arrays.asList(new Object[] {SAMPLE_UPDATED_LOGIN})));
//        update.setLscAttributeModifications(attributes);
//        assertTrue(service.apply(update));
//
//        // Check that the object has been successfully updated !
//        IBean updatedBean = service.getBean(updateUuid, new LscDatasets(), false);
//        assertEquals(updatedBean.getMainIdentifier(), createUuid);
//        assertEquals(updatedBean.getDatasetFirstValueById(DvsProvisioningService.EMAIL_NAME), SAMPLE_EMAIL);
//        assertEquals(updatedBean.getDatasetFirstValueById(DvsProvisioningService.GIVENNAME_NAME), SAMPLE_GIVENNAME);
//        assertEquals(updatedBean.getDatasetFirstValueById(DvsProvisioningService.SURNAME_NAME), SAMPLE_SURNAME);
//        assertEquals(updatedBean.getDatasetFirstValueById(DvsProvisioningService.MODIFYTIMESTAMP_NAME), SAMPLE_MODIFYTIMESTAMP);
//        assertEquals(updatedBean.getDatasetFirstValueById(DvsProvisioningService.PASSWORD_NAME), SAMPLE_PASSWORD);
//        assertEquals(updatedBean.getDatasetFirstValueById(DvsProvisioningService.PHONE_NAME), SAMPLE_PHONE);
//
        // Delete it !
        LscModifications delete = new LscModifications(LscModificationType.DELETE_OBJECT);
        delete.setMainIdentifer(dvsUuid);
        assertTrue(service.apply(delete));

        service.flushCache();
        
        // Check that the object has been successfully deleted !
        assertNull(service.getBean(dvsUuid, new LscDatasets(), false));
    }
}
