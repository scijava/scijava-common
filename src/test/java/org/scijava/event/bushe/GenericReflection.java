
package org.scijava.event.bushe;

import java.lang.reflect.TypeVariable;

import java.lang.reflect.*;
import java.io.*;

/**
 * From OReilly Book Java Generics
 */
public class GenericReflection {

	DataRequestEvent dre;
	private final static PrintStream out = System.out;

	public static void printSuperclass(Type sup) {
		if (sup != null && !sup.equals(Object.class)) {
			out.print("extends ");
			printType(sup);
			out.println();
		}
	}

	public static void printInterfaces(Type[] implementations) {
		if (implementations != null && implementations.length > 0) {
			out.print("implements ");
			int i = 0;
			for (Type impl : implementations) {
				if (i++ > 0) out.print(",");
				printType(impl);
			}
			out.println();
		}
	}

	public static void printTypeParameters(TypeVariable<?>[] vars) {
		if (vars != null && vars.length > 0) {
			out.print("<");
			int i = 0;
			for (TypeVariable<?> var : vars) {
				if (i++ > 0) out.print(",");
				out.print(var.getName());
				printBounds(var.getBounds());
			}
			out.print(">");
		}
	}

	public static void printBounds(Type[] bounds) {
		if (bounds != null && bounds.length > 0 && !(bounds.length == 1 &&
			bounds[0] == Object.class))
		{
			out.print(" extends ");
			int i = 0;
			for (Type bound : bounds) {
				if (i++ > 0) out.print("&");
				printType(bound);
			}
		}
	}

	public static void printParams(Type[] types) {
		if (types != null && types.length > 0) {
			out.print("<");
			int i = 0;
			for (Type type : types) {
				if (i++ > 0) out.print(",");
				printType(type);
			}
			out.print(">");
		}
	}

	public static void printType(Type type) {
		if (type instanceof Class) {
			Class<?> c = (Class) type;
			out.print(c.getName());
		}
		else if (type instanceof ParameterizedType) {
			ParameterizedType p = (ParameterizedType) type;
			Class c = (Class) p.getRawType();
			Type o = p.getOwnerType();
			if (o != null) {
				printType(o);
				out.print(".");
			}
			out.print(c.getName());
			printParams(p.getActualTypeArguments());
		}
		else if (type instanceof TypeVariable<?>) {
			TypeVariable<?> v = (TypeVariable<?>) type;
			out.print(v.getName());
		}
		else if (type instanceof GenericArrayType) {
			GenericArrayType a = (GenericArrayType) type;
			printType(a.getGenericComponentType());
			out.print("[]");
		}
		else if (type instanceof WildcardType) {
			WildcardType w = (WildcardType) type;
			Type[] upper = w.getUpperBounds();
			Type[] lower = w.getLowerBounds();
			if (upper.length == 1 && lower.length == 0) {
				out.print("? extends ");
				printType(upper[0]);
			}
			else if (upper.length == 0 && lower.length == 1) {
				out.print("? super ");
				printType(lower[0]);
			}
			else assert false;
		}
	}

	public static void printClass(Class c) {
		out.print("class ");
		out.print(c.getName());
		printTypeParameters(c.getTypeParameters());
		out.println();
		printSuperclass(c.getGenericSuperclass());
		printInterfaces(c.getGenericInterfaces());
		/*
		out.println("{");
		for (Field f : c.getFields()) {
		  out.println("  "+f.toGenericString()+";");
		}
		for (Constructor k : c.getConstructors()) {
		  out.println("  "+k.toGenericString()+";");
		}
		for (Method m : c.getMethods()) {
		  out.println("  "+m.toGenericString()+";");
		}
		out.println("}");
		*/
	}

	public static void main(String[] args) throws ClassNotFoundException {
		for (String name : args) {
			Class<?> c = Class.forName(name);
			printClass(c);
		}
	}
}
