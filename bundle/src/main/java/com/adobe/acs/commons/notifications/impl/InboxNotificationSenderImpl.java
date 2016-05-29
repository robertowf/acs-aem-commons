/*
 * #%L
 * ACS AEM Commons Bundle
 * %%
 * Copyright (C) 2013 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.adobe.acs.commons.notifications.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.acs.commons.notifications.InboxNotification;
import com.adobe.acs.commons.notifications.InboxNotificationSender;
import com.adobe.granite.taskmanagement.Task;
import com.adobe.granite.taskmanagement.TaskAction;
import com.adobe.granite.taskmanagement.TaskManager;
import com.adobe.granite.taskmanagement.TaskManagerException;
import com.adobe.granite.taskmanagement.TaskManagerFactory;

@Component(
        label = "ACS AEM Commons - AEM Inbox Notification Sender",
        description = "Service for sending AEM Inbox Notification",
        immediate = false,
        metatype = false)
@Service
public class InboxNotificationSenderImpl implements InboxNotificationSender {

    private static final Logger log = LoggerFactory
            .getLogger(InboxNotificationSenderImpl.class);

    public static final String NOTIFICATION_TASK_TYPE = "Notification";

    @Override
    public InboxNotification buildInboxNotification() {
        InboxNotification notification = new InboxNotificationImpl();

        return notification;
    }

    @Override
    public void sendInboxNotification(ResourceResolver resourceResolver,
            InboxNotification inboxNotification) throws TaskManagerException {

        log.debug("Sending Inbox Notification to {} with title {}",
                inboxNotification.getAssignee(), inboxNotification.getTitle());

        TaskManager taskManager = resourceResolver.adaptTo(TaskManager.class);

        Task newTask = createTask(taskManager, inboxNotification);

        taskManager.createTask(newTask);
    }

    private Task createTask(TaskManager taskManager,
            InboxNotification inboxNotification) throws TaskManagerException {

        Task newTask = taskManager.getTaskManagerFactory().newTask(
                NOTIFICATION_TASK_TYPE);

        newTask.setName(inboxNotification.getTitle());
        newTask.setContentPath(inboxNotification.getContentPath());
        newTask.setDescription(inboxNotification.getMessage());
        newTask.setInstructions(inboxNotification.getInstructions());
        newTask.setCurrentAssignee(inboxNotification.getAssignee());

        String[] notificationActions = inboxNotification
                .getNotificationActions();
        if (ArrayUtils.isNotEmpty(notificationActions)) {
            List<TaskAction> taskActions = createTaskActionsList(
                    notificationActions, taskManager);

            newTask.setActions(taskActions);
        }

        return newTask;
    }

    private List<TaskAction> createTaskActionsList(
            String[] notificationActions, TaskManager taskManager) {

        TaskManagerFactory taskManagerFactory = taskManager
                .getTaskManagerFactory();
        List<TaskAction> taskActions = new ArrayList<TaskAction>();

        for (String action : notificationActions) {

            TaskAction newTaskAction = taskManagerFactory.newTaskAction(action);
            taskActions.add(newTaskAction);
        }

        return taskActions;
    }

    @Override
    public void sendInboxNotifications(ResourceResolver resourceResolver,
            List<InboxNotification> notificationDetailList)
            throws TaskManagerException {

        for (InboxNotification notificationDetails : notificationDetailList) {

            sendInboxNotification(resourceResolver, notificationDetails);
        }
    }
}
