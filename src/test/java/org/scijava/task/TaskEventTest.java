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

    static int nTasks = 3500; // Putting higher value can lead to issues because too many threads cannot be launched in parallel

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
        Thread.sleep(5000);
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
                        System.out.println("Waiting to start task "+taskName);
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