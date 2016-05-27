/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.mdm.api;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.Platform;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManager;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.app.mgt.ApplicationManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.mdm.api.common.MDMAPIException;
import org.wso2.carbon.mdm.api.context.DeviceOperationContext;
import org.wso2.carbon.mdm.api.util.MDMAPIUtils;
import org.wso2.carbon.mdm.api.util.MDMAndroidOperationUtil;
import org.wso2.carbon.mdm.api.util.MDMIOSOperationUtil;
import org.wso2.carbon.mdm.api.util.ResponsePayload;
import org.wso2.carbon.mdm.beans.ApplicationWrapper;
import org.wso2.carbon.mdm.beans.MobileApp;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Operation related REST-API implementation.
 */
@Produces({"application/json", "application/xml"})
@Consumes({"application/json", "application/xml"})
public class Operation {

    private static Log log = LogFactory.getLog(Operation.class);

    /* @deprecated */
    @GET
    public List<? extends org.wso2.carbon.device.mgt.common.operation.mgt.Operation> getAllOperations()
            throws MDMAPIException {
        List<? extends org.wso2.carbon.device.mgt.common.operation.mgt.Operation> operations;
        DeviceManagementProviderService dmService;
        try {
            dmService = MDMAPIUtils.getDeviceManagementService();
            operations = dmService.getOperations(null);
        } catch (OperationManagementException e) {
            String msg = "Error occurred while fetching the operations for the device.";
            log.error(msg, e);
            throw new MDMAPIException(msg, e);
        }
        return operations;
    }

    @GET
    @Path("paginate/{type}/{id}")
    public PaginationResult getDeviceOperations(
            @PathParam("type") String type, @PathParam("id") String id, @QueryParam("start") int startIdx,
            @QueryParam("length") int length, @QueryParam("search") String search)
            throws MDMAPIException {
        PaginationResult operations;
        DeviceManagementProviderService dmService;
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        PaginationRequest paginationRequest = new PaginationRequest(startIdx, length);
        try {
            deviceIdentifier.setType(type);
            deviceIdentifier.setId(id);
            dmService = MDMAPIUtils.getDeviceManagementService();
            operations = dmService.getOperations(deviceIdentifier, paginationRequest);
        } catch (OperationManagementException e) {
            String msg = "Error occurred while fetching the operations for the device.";
            log.error(msg, e);
            throw new MDMAPIException(msg, e);
        }
        return operations;
    }

    @GET
    @Path("{type}/{id}")
    public List<? extends org.wso2.carbon.device.mgt.common.operation.mgt.Operation> getDeviceOperations(
            @PathParam("type") String type, @PathParam("id") String id)
            throws MDMAPIException {
        List<? extends org.wso2.carbon.device.mgt.common.operation.mgt.Operation> operations;
        DeviceManagementProviderService dmService;
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        try {
            deviceIdentifier.setType(type);
            deviceIdentifier.setId(id);
            dmService = MDMAPIUtils.getDeviceManagementService();
            operations = dmService.getOperations(deviceIdentifier);
        } catch (OperationManagementException e) {
            String msg = "Error occurred while fetching the operations for the device.";
            log.error(msg, e);
            throw new MDMAPIException(msg, e);
        }
        return operations;
    }

    /* @deprecated */
    @POST
    public Response addOperation(DeviceOperationContext operationContext) throws MDMAPIException {
        DeviceManagementProviderService dmService;
        ResponsePayload responseMsg = new ResponsePayload();
        try {
            dmService = MDMAPIUtils.getDeviceManagementService();
            Activity activity = dmService.addOperation(operationContext.getType(), operationContext.getOperation(),
                    operationContext.getDevices());
            if (activity != null) {
                Response.status(HttpStatus.SC_CREATED);
                responseMsg.setMessageFromServer("Operation has added successfully.");
            }
            //Response.status(HttpStatus.SC_CREATED);
            return Response.status(HttpStatus.SC_CREATED).entity(activity).build();
        } catch (OperationManagementException e) {
            String msg = "Error occurred while saving the operation";
            log.error(msg, e);
            throw new MDMAPIException(msg, e);
        }
    }

    @GET
    @Path("{type}/{id}/apps")
    public List<? extends Application> getInstalledApps(
            @PathParam("type") String type,
            @PathParam("id") String id)
            throws MDMAPIException {
        List<Application> applications;
        ApplicationManagementProviderService appManagerConnector;
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        try {
            deviceIdentifier.setType(type);
            deviceIdentifier.setId(id);
            appManagerConnector = MDMAPIUtils.getAppManagementService();
            applications = appManagerConnector.getApplicationListForDevice(deviceIdentifier);
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while fetching the apps of the device.";
            log.error(msg, e);
            throw new MDMAPIException(msg, e);
        }
        return applications;
    }

    @POST
    @Path("installApp/{tenantDomain}")
    public ResponsePayload installApplication(ApplicationWrapper applicationWrapper,
                                              @PathParam("tenantDomain") String tenantDomain) throws MDMAPIException {
        ResponsePayload responseMsg = new ResponsePayload();
        ApplicationManager appManagerConnector;
        org.wso2.carbon.device.mgt.common.operation.mgt.Operation operation = null;
        ArrayList<DeviceIdentifier> deviceIdentifiers;
        try {
            appManagerConnector = MDMAPIUtils.getAppManagementService();
            MobileApp mobileApp = applicationWrapper.getApplication();

            if (applicationWrapper.getDeviceIdentifiers() != null) {
                for (DeviceIdentifier deviceIdentifier : applicationWrapper.getDeviceIdentifiers()) {
                    deviceIdentifiers = new ArrayList<DeviceIdentifier>();

                    if (deviceIdentifier.getType().equals(Platform.android.toString())) {
                        operation = MDMAndroidOperationUtil.createInstallAppOperation(mobileApp);
                    } else if (deviceIdentifier.getType().equals(Platform.ios.toString())) {
                        operation = MDMIOSOperationUtil.createInstallAppOperation(mobileApp);
                    }
                    deviceIdentifiers.add(deviceIdentifier);
                }
                appManagerConnector.installApplicationForDevices(operation, applicationWrapper.getDeviceIdentifiers());
            }
            Response.status(HttpStatus.SC_CREATED);
            responseMsg.setMessageFromServer("Application installation request has been sent to the device.");
            return responseMsg;
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while saving the operation";
            log.error(msg, e);
            throw new MDMAPIException(msg, e);
        }
    }

    @POST
    @Path("uninstallApp/{tenantDomain}")
    public ResponsePayload uninstallApplication(ApplicationWrapper applicationWrapper,
                                                @PathParam("tenantDomain") String tenantDomain) throws MDMAPIException {
        ResponsePayload responseMsg = new ResponsePayload();
        ApplicationManager appManagerConnector;
        org.wso2.carbon.device.mgt.common.operation.mgt.Operation operation = null;
        ArrayList<DeviceIdentifier> deviceIdentifiers;
        try {
            appManagerConnector = MDMAPIUtils.getAppManagementService();
            MobileApp mobileApp = applicationWrapper.getApplication();

            if (applicationWrapper.getDeviceIdentifiers() != null) {
                for (DeviceIdentifier deviceIdentifier : applicationWrapper.getDeviceIdentifiers()) {
                    deviceIdentifiers = new ArrayList<DeviceIdentifier>();

                    if (deviceIdentifier.getType().equals(Platform.android.toString())) {
                        operation = MDMAndroidOperationUtil.createAppUninstallOperation(mobileApp);
                    } else if (deviceIdentifier.getType().equals(Platform.ios.toString())) {
                        operation = MDMIOSOperationUtil.createAppUninstallOperation(mobileApp);
                    }
                    deviceIdentifiers.add(deviceIdentifier);
                }
                appManagerConnector.installApplicationForDevices(operation, applicationWrapper.getDeviceIdentifiers());
            }
            Response.status(HttpStatus.SC_CREATED);
            responseMsg.setMessageFromServer("Application removal request has been sent to the device.");
            return responseMsg;
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while saving the operation";
            log.error(msg, e);
            throw new MDMAPIException(msg, e);
        }
    }

    @GET
    @Path("activity/{id}")
    public Response getActivity(@PathParam("id") String id)
            throws MDMAPIException {
        Activity activity;
        DeviceManagementProviderService dmService;
        try {
            dmService = MDMAPIUtils.getDeviceManagementService();
            activity = dmService.getOperationByActivityId(id);
        } catch (OperationManagementException e) {
            String msg = "Error occurred while fetching the activity for the supplied id.";
            log.error(msg, e);
            throw new MDMAPIException(msg, e);
        }
        return Response.status(Response.Status.OK).entity(activity).build();
    }

    @GET
    @Path("activity/after/{timestamp}")
    public Response getActivityUpdatedAfter(@PathParam("timestamp") String timestamp)
            throws MDMAPIException {
        List<Activity> activities;
        DeviceManagementProviderService dmService;
        try {
            dmService = MDMAPIUtils.getDeviceManagementService();
            activities = dmService.getActivitiesUpdatedAfter(Long.parseLong(timestamp));
        } catch (OperationManagementException e) {
            String msg = "Error occurred while fetching the activities updated after given time stamp.";
            log.error(msg, e);
            throw new MDMAPIException(msg, e);
        }
        return Response.status(Response.Status.OK).entity(activities).build();
    }
}