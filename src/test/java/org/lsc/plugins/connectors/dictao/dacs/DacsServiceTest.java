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
package org.lsc.plugins.connectors.dictao.dacs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.naming.NamingException;

import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
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
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class DacsServiceTest {
    
    private static DacsProvisioningService service;

    private static final String SAMPLE_EMAIL = "bgates@dictao.com";
    private static final String SAMPLE_GIVENNAME = "Bill";
    private static final String SAMPLE_SURNAME = "Gates";
    private static final String SAMPLE_LOGIN = "bgates";
    private static final String SAMPLE_UPDATED_LOGIN = "bill.gates";
    private static final String SAMPLE_MODIFYTIMESTAMP = DateUtils.format(new Date());
    private static final String SAMPLE_PASSWORD = "secret";
    private static final String SAMPLE_PHONE = "+33600000000";
    private static final String SAMPLE_COMMON_NAME = SAMPLE_GIVENNAME + " "  + SAMPLE_SURNAME;
    
    @BeforeClass
    public static void setup() throws LscException {
        Configuration.setUp("src/test/resources/etc-opendj2dacs");
        service = new DacsProvisioningService(LscConfiguration.getTask("SyncToDacs"));
    }

    @Test
    public void testDacsProvisioning() throws LscServiceException, NamingException {
    	String uuid = UUID.randomUUID().toString();
        String dacsUuid = "lsc-dacs-provisioning-create-"  + uuid;
        LscModifications create = new LscModifications(LscModificationType.CREATE_OBJECT);
        create.setMainIdentifer(dacsUuid);
        List<LscDatasetModification> attributes = new ArrayList<LscDatasetModification>();
        attributes.add(new LscDatasetModification(LscDatasetModificationType.ADD_VALUES, DacsProvisioningService.EMAIL_NAME, Arrays.asList(new Object[] {SAMPLE_EMAIL})));
        attributes.add(new LscDatasetModification(LscDatasetModificationType.ADD_VALUES, DacsProvisioningService.COMMON_NAME, Arrays.asList(new Object[] {SAMPLE_COMMON_NAME})));
        attributes.add(new LscDatasetModification(LscDatasetModificationType.ADD_VALUES, DacsProvisioningService.LOGIN_NAME, Arrays.asList(new Object[] {SAMPLE_LOGIN + "-" + uuid})));
        attributes.add(new LscDatasetModification(LscDatasetModificationType.ADD_VALUES, DacsProvisioningService.MODIFYTIMESTAMP_NAME, Arrays.asList(new Object[] {SAMPLE_MODIFYTIMESTAMP})));
        attributes.add(new LscDatasetModification(LscDatasetModificationType.ADD_VALUES, DacsProvisioningService.PASSWORD_NAME, Arrays.asList(new Object[] {SAMPLE_PASSWORD})));
        attributes.add(new LscDatasetModification(LscDatasetModificationType.ADD_VALUES, DacsProvisioningService.PHONE_NAME, Arrays.asList(new Object[] {SAMPLE_PHONE})));
        create.setLscAttributeModifications(attributes);
        assertTrue(service.apply(create));

        service.flushCache();
        
        // Check that the object has been successfully stored !
        IBean createBean = service.getBean(dacsUuid, new LscDatasets(), false);
        assertNotNull(createBean);
        assertEquals(createBean.getMainIdentifier(), dacsUuid);
        assertEquals(createBean.getDatasetFirstValueById(DacsProvisioningService.EMAIL_NAME), SAMPLE_EMAIL);
        assertEquals(createBean.getDatasetFirstValueById(DacsProvisioningService.COMMON_NAME), SAMPLE_COMMON_NAME);
        assertEquals(createBean.getDatasetFirstValueById(DacsProvisioningService.LOGIN_NAME), SAMPLE_LOGIN + "-" + uuid);
        assertEquals(createBean.getDatasetFirstValueById(DacsProvisioningService.MODIFYTIMESTAMP_NAME), SAMPLE_MODIFYTIMESTAMP);
        assertEquals(createBean.getDatasetFirstValueById(DacsProvisioningService.PASSWORD_NAME), SAMPLE_PASSWORD);
        assertEquals(createBean.getDatasetFirstValueById(DacsProvisioningService.PHONE_NAME), SAMPLE_PHONE);

        LscModifications update = new LscModifications(LscModificationType.UPDATE_OBJECT);
        update.setMainIdentifer(dacsUuid);
        attributes = new ArrayList<LscDatasetModification>();
        attributes.add(new LscDatasetModification(LscDatasetModificationType.REPLACE_VALUES, DacsProvisioningService.LOGIN_NAME, Arrays.asList(new Object[] {SAMPLE_UPDATED_LOGIN + "-" + uuid})));
        update.setLscAttributeModifications(attributes);
        update.setDestinationBean(createBean);
        assertTrue(service.apply(update));

        service.flushCache();
        
        // Check that the object has been successfully updated !
        IBean updatedBean = service.getBean(dacsUuid, new LscDatasets(), false);
        assertEquals(dacsUuid, updatedBean.getMainIdentifier());
        assertEquals(SAMPLE_EMAIL, updatedBean.getDatasetFirstValueById(DacsProvisioningService.EMAIL_NAME));
        assertEquals(SAMPLE_COMMON_NAME, updatedBean.getDatasetFirstValueById(DacsProvisioningService.COMMON_NAME));
        assertEquals(SAMPLE_MODIFYTIMESTAMP, updatedBean.getDatasetFirstValueById(DacsProvisioningService.MODIFYTIMESTAMP_NAME));
        assertEquals(SAMPLE_PASSWORD, updatedBean.getDatasetFirstValueById(DacsProvisioningService.PASSWORD_NAME));
        assertEquals(SAMPLE_PHONE, updatedBean.getDatasetFirstValueById(DacsProvisioningService.PHONE_NAME));

        // Delete it !
        LscModifications delete = new LscModifications(LscModificationType.DELETE_OBJECT);
        delete.setMainIdentifer(dacsUuid);
        assertTrue(service.apply(delete));

        service.flushCache();
        
        // Check that the object has been successfully deleted !
        assertNull(service.getBean(dacsUuid, new LscDatasets(), false));
    }
}
