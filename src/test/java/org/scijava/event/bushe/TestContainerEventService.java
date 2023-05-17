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

import junit.framework.TestCase;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/** The DefaultEventService is NOT Swing-safe!  But it's easier to test... */
public class TestContainerEventService extends TestCase {
   private ArrayList subscribedEvents;
   private Object aSource = new Object();
   private JFrame frame;
   private JPanel panel;
   private Object lastEventObject;

   public TestContainerEventService(String name) {
      super(name);
   }

   protected void setUp() throws Exception {
      subscribedEvents = new ArrayList();
      frame = new JFrame();
      panel = new JPanel();
      frame.setContentPane(panel);
   }

   public void testContainerEventServiceFinder() {
      JButton button = new JButton("Foo");
      panel.add(button);
      JButton barButton = new JButton("Bar");
      panel.add(barButton);
      EventService es = ContainerEventServiceFinder.getEventService(button);
      assertTrue(EventBus.getGlobalEventService() != es);
      EventService esBar = ContainerEventServiceFinder.getEventService(barButton);
      assertEquals(esBar, es);
      assertEquals(0, subscribedEvents.size());
      es.subscribe("FooTopic", new EventTopicSubscriber() {
         public void onEvent(String topic, Object evt) {
            subscribedEvents.add(evt);
         }
      });
      esBar.publish("FooTopic", "Foo");
      EDTUtil.waitForEDT();
      assertEquals(1, subscribedEvents.size());
      subscribedEvents.clear();
      Point location = frame.getLocation();
      frame.setLocation(33, 33);
      EDTUtil.waitForEDT();
      assertTrue(subscribedEvents.size() == 0);
   }

   public void testContainerEventServiceSupplier() {
      JButton button = new JButton("Foo");
      JPanel subPanel = new JPanel();
      subPanel.add(button);
      panel.add(subPanel);
      JButton barButton = new JButton("Bar");
      JPanel subPanel2 = new ContainerEventServiceSupplierPanel();
      subPanel2.add(barButton);
      panel.add(subPanel2);
      EventService es = ContainerEventServiceFinder.getEventService(button);
      assertTrue(EventBus.getGlobalEventService() != es);
      EventService esBar = ContainerEventServiceFinder.getEventService(barButton);
      assertTrue(esBar != es);
      assertEquals(0, subscribedEvents.size());
      es.subscribe("FooTopic", new EventTopicSubscriber() {
         public void onEvent(String topic, Object evt) {
            subscribedEvents.add(evt);
         }
      });
      esBar.publish("FooTopic", "Foo");
      EDTUtil.waitForEDT();
      assertEquals(0, subscribedEvents.size());
   }

   public void testContainerEventServiceRegistrar() {
      //Set the lastEventObject whenever the event fires on the right Container Event Service
      EventTopicSubscriber buttonContainerTopicSubscriber = new EventTopicSubscriber() {
         public void onEvent(String topic, Object data) {
            System.out.println("topic=" + topic + ", data=" + data);
            setLastEventObject(data);
         }
      };
      //Set the lastEventObject whenever the event fires on the right Container Event Service
      VetoTopicEventListener buttonContainerVetoTopicSubscriber = new VetoTopicEventListener() {
          public boolean shouldVeto(String topic, Object data) {
              return "VetoMe".equals(data);
          }
      };
      //Set the lastEventObject whenever the event fires on the right Container Event Service
      EventSubscriber buttonContainerSubscriber = new EventSubscriber() {
          public void onEvent(Object data) {
             System.out.println("class=" + data);
             setLastEventObject(data);
          }
      };
      //Set the lastEventObject whenever the event fires on the right Container Event Service
      VetoEventListener buttonContainerVetoSubscriber = new VetoEventListener() {
           public boolean shouldVeto(Object data) {
               return "VetoMe".equals(data.toString());
           }
      };
      JButton button = new JButton("Foo");

      ContainerEventServiceRegistrar reg = new ContainerEventServiceRegistrar(button, buttonContainerTopicSubscriber,
              "RegEvent");
      EventService es = reg.getContainerEventService();
      assertTrue(es != null);

      ContainerEventServiceRegistrar reg2 = new ContainerEventServiceRegistrar(button, buttonContainerVetoTopicSubscriber,
              "RegEvent");
      EventService es4reg2 = reg2.getContainerEventService();
      assertTrue(es4reg2 != null);

      ContainerEventServiceRegistrar reg3 = new ContainerEventServiceRegistrar(button, buttonContainerSubscriber,
              String.class);
      EventService es4reg3 = reg3.getContainerEventService();
      assertTrue(es4reg3 != null);

      ContainerEventServiceRegistrar reg4 = new ContainerEventServiceRegistrar(button, buttonContainerVetoSubscriber,
              String.class);
      EventService es4reg4 = reg4.getContainerEventService();
      assertTrue(es4reg4 != null);

      //Publishing on the global event bus should not have an effect
      EventBus.publish("RegEvent", "WrongBus");
      assertEquals(getLastEventObject(), null);

      //Make a container that has another container inside it that supplies a container event service
      JPanel subPanel = new JPanel();
      subPanel.add(button);
      ContainerEventServiceSupplierPanel subPanel2 = new ContainerEventServiceSupplierPanel();
      panel.add(subPanel);
      panel.add(subPanel2);
      EventService es2 = reg.getContainerEventService();
      //the registrar kept up with the move
      assertTrue(es2 != es);

      EventBus.publish("RegEvent", "WrongBus");
      assertEquals(getLastEventObject(), null);
      EventBus.publish("StringClassEvent");
      assertEquals(getLastEventObject(), null);

      EventService topPanelES = ContainerEventServiceFinder.getEventService(panel);

      topPanelES.publish("RegEvent", "TopLevelBus");
      EDTUtil.waitForEDT();
      assertEquals("TopLevelBus", getLastEventObject());

      topPanelES.publish("Don'tVetoMe");
      EDTUtil.waitForEDT();
      assertEquals("Don'tVetoMe", getLastEventObject());

      topPanelES.publish("VetoMe");
      EDTUtil.waitForEDT();
      assertEquals("Don'tVetoMe", getLastEventObject());//veto worked

      topPanelES.publish("RegEvent", "TopLevelBus");
      EDTUtil.waitForEDT();
      assertEquals("TopLevelBus", getLastEventObject());

      EventService subPanel2ES = subPanel2.getContainerEventService();
      subPanel2ES.publish("RegEvent", "SuppliedBus");
      EDTUtil.waitForEDT();
      assertEquals("TopLevelBus", getLastEventObject());//still

      subPanel2.add(button);
      EDTUtil.waitForEDT();
      subPanel2ES.publish("RegEvent", "SuppliedBus");
      EDTUtil.waitForEDT();
      assertEquals("SuppliedBus", getLastEventObject());//detected move

      subPanel2ES.publish("RegEvent", "VetoMe");
      EDTUtil.waitForEDT();
      assertEquals("SuppliedBus", getLastEventObject());//veto moved

      subPanel.add(button);
      topPanelES.publish("RegEvent", "TopLevelBus");
      EDTUtil.waitForEDT();
      assertEquals("TopLevelBus", getLastEventObject());

      subPanel2ES.publish("RegEvent", "SuppliedBus");
      EDTUtil.waitForEDT();
      assertEquals("TopLevelBus", getLastEventObject());
   }

   private synchronized void setLastEventObject(Object data) {
      lastEventObject = data;
   }

   public synchronized Object getLastEventObject() {
      return lastEventObject;
   }

   class ContainerEventServiceSupplierPanel extends JPanel implements ContainerEventServiceSupplier {
      private EventService es = new SwingEventService();

      public EventService getContainerEventService() {
         return es;
      }
   }
}
