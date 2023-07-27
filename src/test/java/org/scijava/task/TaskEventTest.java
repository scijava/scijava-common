/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2023 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.scijava.task;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.task.event.TaskEvent;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Tests whether many tasks run in parallel consistently trigger an Event
 * when each task is started and when each task is ended.
 *
 * The test fails inconsistently, sometimes with a few tasks remaining, sometimes with almost all tasks remaining.
 */

public class TaskEventTest {

    private TaskService taskService;
    private TaskEventListener eventListener;

    static int nTasks = 500; // Putting higher value can lead to issues because too many threads cannot be launched in parallel

    @Before
    public void setUp() {
        final Context ctx = new Context(TaskService.class);
        taskService = ctx.service(TaskService.class);
        eventListener = new TaskEventListener();
        ctx.inject(eventListener);
    }

    @After
    public void tearDown() {
        taskService.context().dispose();
    }

    @Test
    public void testManyTasks() throws InterruptedException {
        for (int i=0;i<nTasks;i++) {
            createTask(taskService, "Task_"+i, 100, 10, 100);
        }
        // Wait up to a few seconds for all tasks to complete.
        long start = System.currentTimeMillis();
        long maxWaitTime = 5000;
        while (System.currentTimeMillis() - start < maxWaitTime) {
          if (eventListener.tasks.isEmpty()) break; // done!
          Thread.sleep(10);
        }
        assertEquals(0, eventListener.getLeftOvers().size());
    }

    public static void createTask(
            TaskService taskService,
            String taskName,
            int msBeforeStart,
            int msUpdate,
            int msTaskDuration) {
        Task task = taskService.createTask(taskName);

        new Thread(
                () -> {
                    try {
                        Thread.sleep(msBeforeStart);

                        // Task started
                        task.setProgressMaximum(100);

                        task.run(() -> {
                            int totalMs = 0;
                            while(totalMs<msTaskDuration) {
                                try {
                                    Thread.sleep(msUpdate);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                totalMs+=msUpdate;
                                task.setProgressValue((int)(((double)totalMs/msTaskDuration)*100.0));
                            }
                            // Task ended
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
    }

    public static class TaskEventListener {

        Set<Task> tasks = new HashSet<>();

        @EventHandler
        private synchronized void onEvent(final TaskEvent evt) {
            Task task = evt.getTask();
            if (task.isDone()) {
                tasks.remove(task);
            } else {
                tasks.add(task);
            }
        }

        public synchronized Set<Task> getLeftOvers() {
            return new HashSet<>(tasks);
        }
    }
}
