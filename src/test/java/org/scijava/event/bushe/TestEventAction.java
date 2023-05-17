/**
 * Copyright 2005 Bushe Enterprises, Inc., Hopkinton, MA, USA, www.bushe.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scijava.event.bushe;

import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import junit.framework.TestCase;

/** The DefaultEventService is NOT Swing-safe!  But it's easier to test... */
public class TestEventAction extends TestCase {
   private ArrayList subscribedEvents;
   private Object aSource = new Object();

   private class MyEventServiceEvent extends AbstractEventServiceEvent {
      private ActionEvent evt;

      public MyEventServiceEvent(Object source, ActionEvent evt) {
         super(source);
         this.evt = evt;
      }
   }

   public TestEventAction(String name) {
      super(name);
   }

   protected void setUp() throws Exception {
      subscribedEvents = new ArrayList();
       System.clearProperty(EventServiceLocator.EVENT_BUS_CLASS);
       System.clearProperty(EventServiceLocator.SWING_EVENT_SERVICE_CLASS);
   }

   public void testEventBusTopicAction() {
      EventBusAction action = new EventBusAction();
      action.putValue(Action.ACTION_COMMAND_KEY, "FooAction");
      IEventTopicSubscriber subscriber = new IEventTopicSubscriber() {
         public void onEvent(String topic, Object evt) {
            subscribedEvents.add(evt);
         }
      };
      EventBus.subscribeStrongly("FooAction", subscriber);
      action.actionPerformed(new ActionEvent(this, 0, "FooAction"));
      try {
         Thread.sleep(500);//Calling hte EDT, need to slow this thread
      } catch (InterruptedException e) {
      }
      assertEquals(1, subscribedEvents.size());
      assertNotNull(action);//keeps it from being garbage collected
   }

   public void testEventBusTopicActionEventServiceValueFirst() {
      EventBusAction action = new EventBusAction();
      action.putValue(EventBusAction.EVENT_SERVICE_TOPIC_NAME, "FooAction");
      action.putValue(Action.ACTION_COMMAND_KEY, "BarAction");
      EventBus.subscribeStrongly("FooAction", new IEventTopicSubscriber() {
         public void onEvent(String topic, Object evt) {
            subscribedEvents.add(evt);
         }
      });
      action.actionPerformed(new ActionEvent(this, 0, "FooAction"));
      try {
         Thread.sleep(500);//Calling hte EDT, need to slow this thread
      } catch (InterruptedException e) {
      }
      assertEquals(1, subscribedEvents.size());
   }

   public void testEventBusTopicActionIDValueFirst() {
      EventBusAction action = new EventBusAction();
      action.putValue("ID", "FooAction");
      action.putValue(Action.ACTION_COMMAND_KEY, "BarAction");
      EventBus.subscribeStrongly("FooAction", new IEventTopicSubscriber() {
         public void onEvent(String topic, Object evt) {
            subscribedEvents.add(evt);
         }
      });
      action.actionPerformed(new ActionEvent(this, 0, "FooAction"));
      try {
         Thread.sleep(500);//Calling hte EDT, need to slow this thread
      } catch (InterruptedException e) {
      }
      assertEquals(1, subscribedEvents.size());
   }

   public void testEventBusTopicActionNameWorks() {
      EventBusAction action = new EventBusAction();
      action.putValue(Action.NAME, "FooAction");
      EventBus.subscribeStrongly("FooAction", new IEventTopicSubscriber() {
         public void onEvent(String topic, Object evt) {
            subscribedEvents.add(evt);
         }
      });
      action.actionPerformed(new ActionEvent(this, 0, "FooAction"));
      try {
         Thread.sleep(500);//Calling hte EDT, need to slow this thread
      } catch (InterruptedException e) {
      }
      assertEquals(1, subscribedEvents.size());
   }

   public void testEventBusEventAction() {
      EventBusAction action = new EventBusAction("FooAction", null) {
         protected Object getEventServiceEvent(ActionEvent evt) {
            return new MyEventServiceEvent(aSource, evt);
         }
      };
      EventBus.subscribe(MyEventServiceEvent.class, new IEventSubscriber() {
         public void onEvent(Object evt) {
            assertEquals(((EventServiceEvent) evt).getSource(), aSource);
            subscribedEvents.add(evt);
         }
      });
      action.setPublishesOnTopic(false);
      action.actionPerformed(new ActionEvent(this, 0, "FooAction"));
      try {
         Thread.sleep(500);//Calling the EDT, need to slow this thread
      } catch (InterruptedException e) {
      }
      assertEquals(1, subscribedEvents.size());
   }


   public void testContainerEventAction() {
      JFrame frame = new JFrame();
      JPanel panel = new JPanel();
      frame.setContentPane(panel);
      ContainerEventServiceAction action = new ContainerEventServiceAction("FooAction", null);
      JButton button = new JButton(action);
      panel.add(button);
      EventService es = ContainerEventServiceFinder.getEventService(button);
      assertTrue(EventBus.getGlobalEventService() != es);
      assertEquals(0, subscribedEvents.size());
      es.subscribe("FooAction", new IEventTopicSubscriber() {
         public void onEvent(String topic, Object evt) {
            subscribedEvents.add(evt);
         }
      });
      button.doClick();
      try {
         Thread.sleep(500);//Calling hte EDT, need to slow this thread
      } catch (InterruptedException e) {
      }
      assertEquals(1, subscribedEvents.size());
   }

   public void testContainerEventActionException() {
      ContainerEventServiceAction action = new ContainerEventServiceAction("FooAction", null);
      try {
         action.actionPerformed(new ActionEvent(this, 0, "Foo"));
         fail("Throws exception when no event service");
      } catch (Throwable t) {
      }
      try {
         action.actionPerformed(new ActionEvent(null, 0, "Foo"));
         fail("Throws exception when no event service");
      } catch (Throwable t) {
      }
      action.setThrowsExceptionOnNullEventService(false);
      action.actionPerformed(new ActionEvent(this, 0, "Foo"));
      assertTrue("Set to not throw exception when no event service", true);
   }

}
