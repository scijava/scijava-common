/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2022 SciJava developers.
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
package org.scijava.command;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.Priority;
import org.scijava.convert.AbstractConverter;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class CommandArrayConverterTest {

    @Test
    public void testArrayCommandRaw() throws InterruptedException,
            ExecutionException
    {
        final Context context = new Context(CommandService.class);
        final CommandService commandService = context.service(CommandService.class);

        UserClass[] userObjects = new UserClass[2];
        userObjects[0] = new UserClass("User Object 0", new Object());
        userObjects[1] = new UserClass("User Object 1", new Object());

        final CommandModule module = //
                commandService.run(CommandRawArrayInput.class, true, "userObjects", userObjects  ).get(); //
        assertEquals("User Object 0;User Object 1;", module.getOutput("result"));
    }

    @Test
    public void testArrayConvertFromStringCommandRaw() throws InterruptedException,
            ExecutionException
    {
        final Context context = new Context(CommandService.class);
        final CommandService commandService = context.service(CommandService.class);

        final CommandModule module = //
                commandService.run(CommandRawArrayInput.class, true, "userObjects", "User Object 0,User Object 1"  ).get(); //

        assertEquals("User Object 0;User Object 1;", module.getOutput("result"));
    }

    @Test
    public void testArrayCommandWildcardGenerics() throws InterruptedException,
            ExecutionException
    {
        final Context context = new Context(CommandService.class);
        final CommandService commandService = context.service(CommandService.class);

        UserClass[] userObjects = new UserClass[2];
        userObjects[0] = new UserClass("User Object 0", new Object());
        userObjects[1] = new UserClass("User Object 1", new Object());

        final CommandModule module = //
                commandService.run(CommandGenericsWildcardArrayInput.class, true, "userObjects", userObjects  ).get(); //
        assertEquals("User Object 0;User Object 1;", module.getOutput("result"));
    }

    @Test
    public void testArrayConvertFromStringCommandWildcardGenerics() throws InterruptedException,
            ExecutionException
    {
        final Context context = new Context(CommandService.class);
        final CommandService commandService = context.service(CommandService.class);

        final CommandModule module = //
                commandService.run(CommandGenericsWildcardArrayInput.class, true, "userObjects", "User Object 0,User Object 1"  ).get(); //

        assertEquals("User Object 0;User Object 1;", module.getOutput("result"));
    }

    /** A command which uses a UserClass Raw Array parameter. */
    @Plugin(type = Command.class)
    public static class CommandRawArrayInput implements Command {

        @Parameter
        private UserClass[] userObjects;

        @Parameter(type = ItemIO.OUTPUT)
        private String result = "default";

        @Override
        public void run() {
            final StringBuilder sb = new StringBuilder();
            System.out.println("userObjects length : "+userObjects.length);
            System.out.println("class : "+userObjects.getClass());
            for (UserClass obj : userObjects) {
                System.out.println("\t"+obj);
                sb.append(obj.toString()+";");
            }
            result = sb.toString();
        }
    }

    /** A command which uses a UserClass Array with generics wildcard  */
    @Plugin(type = Command.class)
    public static class CommandGenericsWildcardArrayInput implements Command {

        @Parameter
        private UserClass<?>[] userObjects;

        @Parameter(type = ItemIO.OUTPUT)
        private String result = "default";

        @Override
        public void run() {
            final StringBuilder sb = new StringBuilder();
            System.out.println("userObjects length : "+userObjects.length);
            System.out.println("class : "+userObjects.getClass());
            for (UserClass obj : userObjects) {
                System.out.println("\t"+obj);
                sb.append(obj.toString()+";");
            }
            result = sb.toString();
        }
    }

    @Plugin(type = org.scijava.convert.Converter.class, priority = Priority.LOW)
    public static class StringToUserClassConverterNoGenerics extends AbstractConverter<String, UserClass[]> {

        @Override
        public <T> T convert(Object src, Class<T> dest) {
            String str = (String) src;
            String[] names = str.split(",");
            UserClass[] userObjects = new UserClass[names.length];
            for (int index = 0; index < names.length ; index++) {
                userObjects[index] = new UserClass(names[index], new Object());
            }
            return (T) userObjects;
        }

        @Override
        public Class<UserClass[]> getOutputType() {
            return UserClass[].class;
        }

        @Override
        public Class<String> getInputType() {
            return String.class;
        }
    }

    /**
     * Simple class to test input arrays in command
     */
    public static class UserClass<T> {

        String name;

        public UserClass(String objectName, T generic_object) {
            name = objectName;
        }

        public String toString() {
            return  name;
        }
    }

}
